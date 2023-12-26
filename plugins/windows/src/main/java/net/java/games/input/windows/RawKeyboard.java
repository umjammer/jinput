/*
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
 */

package net.java.games.input.windows;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Keyboard;
import net.java.games.input.PollingComponent;
import net.java.games.input.Rumbler;


/**
 * @author elias
 * @version 1.0
 */
final class RawKeyboard extends Keyboard {

    private final RawKeyboardEvent rawEvent = new RawKeyboardEvent();
    private final RawDevice device;

    RawKeyboard(String name, RawDevice device, Controller[] children, Rumbler[] rumblers) throws IOException {
        super(name, createKeyboardComponents(device), children, rumblers);
        this.device = device;
    }

    private static Component[] createKeyboardComponents(RawDevice device) {
        List<Component> components = new ArrayList<>();
        Field[] vkeyFields = RawIdentifierMap.class.getFields();
        for (Field vkeyField : vkeyFields) {
            try {
                if (Modifier.isStatic(vkeyField.getModifiers()) && vkeyField.getType() == int.class) {
                    int vkeyCode = vkeyField.getInt(null);
                    Component.Identifier.Key keyId = RawIdentifierMap.mapVKey(vkeyCode);
                    if (keyId != Component.Identifier.Key.UNKNOWN)
                        components.add(new Key(device, vkeyCode, keyId));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return components.toArray(new Component[] {});
    }

    @Override
    protected synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        while (true) {
            if (!device.getNextKeyboardEvent(rawEvent))
                return false;
            int vkey = rawEvent.getVKey();
            Component.Identifier.Key keyId = RawIdentifierMap.mapVKey(vkey);
            Component key = getComponent(keyId);
            if (key == null)
                continue;
            int message = rawEvent.getMessage();
            if (message == RawDevice.WM_KEYDOWN || message == RawDevice.WM_SYSKEYDOWN) {
                event.set(key, 1, rawEvent.getNanos());
                return true;
            } else if (message == RawDevice.WM_KEYUP || message == RawDevice.WM_SYSKEYUP) {
                event.set(key, 0, rawEvent.getNanos());
                return true;
            }
        }
    }

    @Override
    public void pollDevice() throws IOException {
        device.pollKeyboard();
    }

    @Override
    protected void setDeviceEventQueueSize(int size) throws IOException {
        device.setBufferSize(size);
    }

    final static class Key extends PollingComponent {

        private final RawDevice device;
        private final int vkeyCode;

        public Key(RawDevice device, int vkeyCode, Component.Identifier.Key keyId) {
            super(keyId.getName(), keyId);
            this.device = device;
            this.vkeyCode = vkeyCode;
        }

        @Override
        protected float poll() throws IOException {
            return device.isKeyDown(vkeyCode) ? 1f : 0f;
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
