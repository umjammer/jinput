/*
 * %W% %E%
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*****************************************************************************
 * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 *
 *****************************************************************************/

package net.java.games.input.windows;

import java.io.IOException;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Mouse;
import net.java.games.input.PollingComponent;
import net.java.games.input.Rumbler;


/**
 * @author elias
 * @version 1.0
 */
final class RawMouse extends Mouse {

    // Because one raw event can contain multiple
    // changes, we'll make a simple state machine
    // to keep track of which change to  report next

    /** Another event should be read */
    private final static int EVENT_DONE = 1;
    /** The X axis should be reported next */
    private final static int EVENT_X = 2;
    /** The Y axis should be reported next */
    private final static int EVENT_Y = 3;
    /** The Z axis should be reported next */
    private final static int EVENT_Z = 4;
    /** Button 0 should be reported next */
    private final static int EVENT_BUTTON_0 = 5;
    /** Button 1 should be reported next */
    private final static int EVENT_BUTTON_1 = 6;
    /** Button 2 should be reported next */
    private final static int EVENT_BUTTON_2 = 7;
    /** Button 3 should be reported next */
    private final static int EVENT_BUTTON_3 = 8;
    /** Button 4 should be reported next */
    private final static int EVENT_BUTTON_4 = 9;

    private final RawDevice device;

    private final RawMouseEvent currentEvent = new RawMouseEvent();
    private int eventState = EVENT_DONE;

    RawMouse(String name, RawDevice device, Component[] components, Controller[] children, Rumbler[] rumblers) throws IOException {
        super(name, components, children, rumblers);
        this.device = device;
    }

    @Override
    public void pollDevice() throws IOException {
        device.pollMouse();
    }

    private static boolean makeButtonEvent(RawMouseEvent mouseEvent, Event event, Component buttonComponent, int downFlag, int upFlag) {
        if ((mouseEvent.getButtonFlags() & downFlag) != 0) {
            event.set(buttonComponent, 1, mouseEvent.getNanos());
            return true;
        } else if ((mouseEvent.getButtonFlags() & upFlag) != 0) {
            event.set(buttonComponent, 0, mouseEvent.getNanos());
            return true;
        } else
            return false;
    }

    @Override
    protected synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        while (true) {
            switch (eventState) {
            case EVENT_DONE:
                if (!device.getNextMouseEvent(currentEvent))
                    return false;
                eventState = EVENT_X;
                break;
            case EVENT_X:
                int relX = device.getEventRelativeX();
                eventState = EVENT_Y;
                if (relX != 0) {
                    event.set(getX(), relX, currentEvent.getNanos());
                    return true;
                }
                break;
            case EVENT_Y:
                int relY = device.getEventRelativeY();
                eventState = EVENT_Z;
                if (relY != 0) {
                    event.set(getY(), relY, currentEvent.getNanos());
                    return true;
                }
                break;
            case EVENT_Z:
                int wheel = currentEvent.getWheelDelta();
                eventState = EVENT_BUTTON_0;
                if (wheel != 0) {
                    event.set(getWheel(), wheel, currentEvent.getNanos());
                    return true;
                }
                break;
            case EVENT_BUTTON_0:
                eventState = EVENT_BUTTON_1;
                if (makeButtonEvent(currentEvent, event, getPrimaryButton(), RawDevice.RI_MOUSE_BUTTON_1_DOWN, RawDevice.RI_MOUSE_BUTTON_1_UP))
                    return true;
                break;
            case EVENT_BUTTON_1:
                eventState = EVENT_BUTTON_2;
                if (makeButtonEvent(currentEvent, event, getSecondaryButton(), RawDevice.RI_MOUSE_BUTTON_2_DOWN, RawDevice.RI_MOUSE_BUTTON_2_UP))
                    return true;
                break;
            case EVENT_BUTTON_2:
                eventState = EVENT_BUTTON_3;
                if (makeButtonEvent(currentEvent, event, getTertiaryButton(), RawDevice.RI_MOUSE_BUTTON_3_DOWN, RawDevice.RI_MOUSE_BUTTON_3_UP))
                    return true;
                break;
            case EVENT_BUTTON_3:
                eventState = EVENT_BUTTON_4;
                if (makeButtonEvent(currentEvent, event, getButton3(), RawDevice.RI_MOUSE_BUTTON_4_DOWN, RawDevice.RI_MOUSE_BUTTON_4_UP))
                    return true;
                break;
            case EVENT_BUTTON_4:
                eventState = EVENT_DONE;
                if (makeButtonEvent(currentEvent, event, getButton4(), RawDevice.RI_MOUSE_BUTTON_5_DOWN, RawDevice.RI_MOUSE_BUTTON_5_UP))
                    return true;
                break;
            default:
                throw new RuntimeException("Unknown event state: " + eventState);
            }
        }
    }

    @Override
    protected void setDeviceEventQueueSize(int size) throws IOException {
        device.setBufferSize(size);
    }

    final static class Axis extends PollingComponent {

        private final RawDevice device;

        public Axis(RawDevice device, Component.Identifier.Axis axis) {
            super(axis.getName(), axis);
            this.device = device;
        }

        @Override
        public boolean isRelative() {
            return true;
        }

        @Override
        public boolean isAnalog() {
            return true;
        }

        @Override
        protected float poll() throws IOException {
            if (getIdentifier() == Component.Identifier.Axis.X) {
                return device.getRelativeX();
            } else if (getIdentifier() == Component.Identifier.Axis.Y) {
                return device.getRelativeY();
            } else if (getIdentifier() == Component.Identifier.Axis.Z) {
                return device.getWheel();
            } else
                throw new RuntimeException("Unknown raw axis: " + getIdentifier());
        }
    }

    final static class Button extends PollingComponent {

        private final RawDevice device;
        private final int buttonId;

        public Button(RawDevice device, Component.Identifier.Button id, int buttonId) {
            super(id.getName(), id);
            this.device = device;
            this.buttonId = buttonId;
        }

        @Override
        protected float poll() throws IOException {
            return device.getButtonState(buttonId) ? 1 : 0;
        }

        @Override
        public boolean isRelative() {
            return false;
        }
    }

    @Override
    public void output(AbstractController.Report report) {

    }
}
