/*
 * Copyright (C) 2004 Jeremy Booth (jeremy@newdawnsoftware.com)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

package net.java.games.awt;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.java.games.input.AbstractComponent;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Mouse;
import net.java.games.input.Rumbler;


/**
 * @author Jeremy
 * @author elias
 */
final class AWTMouse extends Mouse implements AWTEventListener {

    private final static int EVENT_X = 1;
    private final static int EVENT_Y = 2;
    private final static int EVENT_BUTTON = 4;

    private final List<AWTEvent> awt_events = new ArrayList<>();
    private final List<AWTEvent> processed_awt_events = new ArrayList<>();

    private int event_state = EVENT_X;

    AWTMouse() {
        super("AWTMouse", createComponents(), new Controller[] {}, new Rumbler[] {});
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    private static Component[] createComponents() {
        return new Component[] {new Axis(Component.Identifier.Axis.X),
                new Axis(Component.Identifier.Axis.Y),
                new Axis(Component.Identifier.Axis.Z),
                new Button(Component.Identifier.Button.LEFT),
                new Button(Component.Identifier.Button.MIDDLE),
                new Button(Component.Identifier.Button.RIGHT)};
    }

    private void processButtons(int button_enum, float value) {
        Button button = getButton(button_enum);
        if (button != null)
            button.setValue(value);
    }

    private Button getButton(int button_enum) {
        return switch (button_enum) {
            case MouseEvent.BUTTON1 -> (Button) getLeft();
            case MouseEvent.BUTTON2 -> (Button) getMiddle();
            case MouseEvent.BUTTON3 -> (Button) getRight();
            default ->
                // Unknown button
                    null;
        };
    }

    private void processEvent(AWTEvent event) throws IOException {
        if (event instanceof MouseWheelEvent mwe) {
            Axis wheel = (Axis) getWheel();
            wheel.setValue(wheel.poll() + mwe.getWheelRotation());
        } else if (event instanceof MouseEvent me) {
            Axis x = (Axis) getX();
            Axis y = (Axis) getY();
            x.setValue(me.getX());
            y.setValue(me.getY());
            switch (me.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                processButtons(me.getButton(), 1f);
                break;
            case MouseEvent.MOUSE_RELEASED:
                processButtons(me.getButton(), 0f);
                break;
            default:
                break;
            }
        }
    }

    @Override
    public final synchronized void pollDevice() throws IOException {
        Axis wheel = (Axis) getWheel();
        wheel.setValue(0);
        for (AWTEvent event : awt_events) {
            processEvent(event);
            processed_awt_events.add(event);
        }
        awt_events.clear();
    }

    @Override
    protected final synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        while (true) {
            if (processed_awt_events.isEmpty())
                return false;
            AWTEvent awt_event = processed_awt_events.get(0);
            if (awt_event instanceof MouseWheelEvent awt_wheel_event) {
                long nanos = awt_wheel_event.getWhen() * 1000000L;
                event.set(getWheel(), awt_wheel_event.getWheelRotation(), nanos);
                processed_awt_events.remove(0);
            } else if (awt_event instanceof MouseEvent mouse_event) {
                long nanos = mouse_event.getWhen() * 1000000L;
                switch (event_state) {
                case EVENT_X:
                    event_state = EVENT_Y;
                    event.set(getX(), mouse_event.getX(), nanos);
                    return true;
                case EVENT_Y:
                    event_state = EVENT_BUTTON;
                    event.set(getY(), mouse_event.getY(), nanos);
                    return true;
                case EVENT_BUTTON:
                    processed_awt_events.remove(0);
                    event_state = EVENT_X;
                    Button button = getButton(mouse_event.getButton());
                    if (button != null) {
                        switch (mouse_event.getID()) {
                        case MouseEvent.MOUSE_PRESSED:
                            event.set(button, 1f, nanos);
                            return true;
                        case MouseEvent.MOUSE_RELEASED:
                            event.set(button, 0f, nanos);
                            return true;
                        default:
                            break;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown event state: " + event_state);
                }
            }
        }
    }

    @Override
    public final synchronized void eventDispatched(AWTEvent event) {
        awt_events.add(event);
    }

    final static class Axis extends AbstractComponent {

        private float value;

        public Axis(Component.Identifier.Axis axis_id) {
            super(axis_id.getName(), axis_id);
        }

        @Override
        public final boolean isRelative() {
            return false;
        }

        @Override
        public final boolean isAnalog() {
            return true;
        }

        private void setValue(float value) {
            this.value = value;
        }

        @Override
        protected final float poll() throws IOException {
            return value;
        }
    }

    final static class Button extends AbstractComponent {

        private float value;

        public Button(Component.Identifier.Button button_id) {
            super(button_id.getName(), button_id);
        }

        private void setValue(float value) {
            this.value = value;
        }

        @Override
        protected final float poll() throws IOException {
            return value;
        }

        @Override
        public final boolean isAnalog() {
            return false;
        }

        @Override
        public final boolean isRelative() {
            return false;
        }
    }
}
