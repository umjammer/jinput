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

package net.java.games.input.awt;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Keyboard;
import net.java.games.input.PollingComponent;
import net.java.games.input.Rumbler;


/**
 * @author Jeremy
 * @author elias
 */
final class AWTKeyboard extends Keyboard implements AWTEventListener {

    private final List<KeyEvent> awtEvents = new ArrayList<>();
    private Event[] processedEvents;
    private int processedEventsIndex;

    AWTKeyboard() {
        super("AWTKeyboard", createComponents(), new Controller[] {}, new Rumbler[] {});
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        resizeEventQueue(EVENT_QUEUE_DEPTH);
    }

    private static Component[] createComponents() {
        List<Component> components = new ArrayList<>();
        Field[] vkeyFields = KeyEvent.class.getFields();
        for (Field vkeyField : vkeyFields) {
            try {
                if (Modifier.isStatic(vkeyField.getModifiers()) && vkeyField.getType() == int.class &&
                        vkeyField.getName().startsWith("VK_")) {
                    int vkeyCode = vkeyField.getInt(null);
                    Component.Identifier.Key keyId = AWTKeyMap.mapKeyCode(vkeyCode);
                    if (keyId != Component.Identifier.Key.UNKNOWN)
                        components.add(new Key(keyId));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        components.add(new Key(Component.Identifier.Key.RCONTROL));
        components.add(new Key(Component.Identifier.Key.LCONTROL));
        components.add(new Key(Component.Identifier.Key.RSHIFT));
        components.add(new Key(Component.Identifier.Key.LSHIFT));
        components.add(new Key(Component.Identifier.Key.RALT));
        components.add(new Key(Component.Identifier.Key.LALT));
        components.add(new Key(Component.Identifier.Key.NUMPADENTER));
        components.add(new Key(Component.Identifier.Key.RETURN));
        components.add(new Key(Component.Identifier.Key.NUMPADCOMMA));
        components.add(new Key(Component.Identifier.Key.COMMA));
        return components.toArray(new Component[] {});
    }

    private void resizeEventQueue(int size) {
        processedEvents = new Event[size];
        for (int i = 0; i < processedEvents.length; i++)
            processedEvents[i] = new Event();
        processedEventsIndex = 0;
    }

    @Override
    protected void setDeviceEventQueueSize(int size) throws IOException {
        resizeEventQueue(size);
    }

    @Override
    public synchronized void eventDispatched(AWTEvent event) {
        if (event instanceof KeyEvent)
            awtEvents.add((KeyEvent) event);
    }

    @Override
    public synchronized void pollDevice() throws IOException {
        for (KeyEvent awtEvent : awtEvents) {
            processEvent(awtEvent);
        }
        awtEvents.clear();
    }

    private void processEvent(KeyEvent event) {
        Component.Identifier.Key keyId = AWTKeyMap.map(event);
        if (keyId == null)
            return;
        Key key = (Key) getComponent(keyId);
        if (key == null)
            return;
        long nanos = event.getWhen() * 1000000L;
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            // the key was pressed
            addEvent(key, 1, nanos);
        } else if (event.getID() == KeyEvent.KEY_RELEASED) {
            KeyEvent nextPress = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
            if ((nextPress == null) || (nextPress.getWhen() != event.getWhen())) {
                // the key came really came up
                addEvent(key, 0, nanos);
            }
        }
    }

    private void addEvent(Key key, float value, long nanos) {
        key.setValue(value);
        if (processedEventsIndex < processedEvents.length)
            processedEvents[processedEventsIndex++].set(key, value, nanos);
    }

    @Override
    protected synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        if (processedEventsIndex == 0)
            return false;
        processedEventsIndex--;
        event.set(processedEvents[0]);
        Event tmp = processedEvents[0];
        processedEvents[0] = processedEvents[processedEventsIndex];
        processedEvents[processedEventsIndex] = tmp;
        return true;
    }

    @Override
    public void output(AbstractController.Report report) {

    }

    private final static class Key extends PollingComponent {

        private float value;

        public Key(Component.Identifier.Key keyId) {
            super(keyId.getName(), keyId);
        }

        public void setValue(float value) {
            this.value = value;
        }

        @Override
        protected float poll() {
            return value;
        }

        @Override
        public boolean isRelative() {
            return false;
        }
    }
}
