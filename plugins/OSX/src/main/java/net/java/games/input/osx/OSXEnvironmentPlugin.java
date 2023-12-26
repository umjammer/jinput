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

package net.java.games.input.osx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.DeviceSupportPlugin;
import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;
import net.java.games.input.PollingController;
import net.java.games.input.Rumbler;
import net.java.games.input.usb.GenericDesktopUsageId;
import net.java.games.input.usb.UsagePage;
import net.java.games.input.usb.UsagePair;


/**
 * OSX HIDManager implementation
 *
 * @author elias
 * @author gregorypierce
 * @version 1.0
 */
public final class OSXEnvironmentPlugin extends ControllerEnvironment {

    private static final Logger log = Logger.getLogger(OSXEnvironmentPlugin.class.getName());

    private static boolean supported = false;

    static {
        String osName = System.getProperty("os.name", "").trim();
log.finer(osName);
        if (osName.contains("Mac")) {
            // Could check isMacOSXEqualsOrBetterThan in here too.
            supported = true;
        }
    }

    private static boolean isMacOSXEqualsOrBetterThan(int majorRequired, int minorRequired) {
        String osVersion = System.getProperty("os.version");
        StringTokenizer versionTokenizer = new StringTokenizer(osVersion, ".");
        int major;
        int minor;
        try {
            String majorStr = versionTokenizer.nextToken();
            String minorStr = versionTokenizer.nextToken();
            major = Integer.parseInt(majorStr);
            minor = Integer.parseInt(minorStr);
        } catch (Exception e) {
            log.fine("Exception occurred while trying to determine OS version: " + e);
            // Best guess, no
            return false;
        }
        return major > majorRequired || (major == majorRequired && minor >= minorRequired);
    }

    private List<Controller> controllers;

    @Override
    public Controller[] getControllers() {
        if (controllers == null) {
            enumerateControllers();
        }
        return controllers.toArray(Controller[]::new);
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    private static void addElements(OSXHIDQueue queue, List<OSXHIDElement> elements, List<Component> components, boolean mapMouseButtons) throws IOException {
        for (OSXHIDElement element : elements) {
log.fine(element.toString());
            Component.Identifier id = element.getIdentifier();
            if (id == null)
                continue;
            if (mapMouseButtons) {
                if (id == Component.Identifier.Button._0) {
                    id = Component.Identifier.Button.LEFT;
                } else if (id == Component.Identifier.Button._1) {
                    id = Component.Identifier.Button.RIGHT;
                } else if (id == Component.Identifier.Button._2) {
                    id = Component.Identifier.Button.MIDDLE;
                }
            }
            OSXComponent component = new OSXComponent(id, element);
            components.add(component);
            queue.addElement(element, component);
        }
    }

    private static Keyboard createKeyboardFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements) throws IOException {
        List<Component> components = new ArrayList<>();
        OSXHIDQueue queue = device.createQueue(PollingController.EVENT_QUEUE_DEPTH);
        try {
            addElements(queue, elements, components, false);
        } catch (IOException e) {
            queue.release();
            throw e;
        }
log.fine("@@@ components: " + components.size());
log.fine("@@@ components: " + components);
        return new OSXKeyboard(device, queue, components.toArray(Component[]::new), new Controller[0], new Rumbler[0]);
    }

    private static Mouse createMouseFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements) throws IOException {
        List<Component> components = new ArrayList<>();
        OSXHIDQueue queue = device.createQueue(PollingController.EVENT_QUEUE_DEPTH);
        try {
            addElements(queue, elements, components, true);
        } catch (IOException e) {
            queue.release();
            throw e;
        }
log.fine("@@@ components: " + components.size());
log.fine("@@@ components: " + components);
        Mouse mouse = new OSXMouse(device, queue, components.toArray(Component[]::new), new Controller[0], new Rumbler[0]);
        if (mouse.getPrimaryButton() != null && mouse.getX() != null && mouse.getY() != null) {
            return mouse;
        } else {
            queue.release();
            return null;
        }
    }

    private static AbstractController createControllerFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements, Controller.Type type) throws IOException {
        List<Component> components = new ArrayList<>();
        List<Controller> controllers = new ArrayList<>();
        List<Rumbler> rumblers = new ArrayList<>();
        OSXHIDQueue queue = device.createQueue(PollingController.EVENT_QUEUE_DEPTH);
        // osx elements
        try {
            addElements(queue, elements, components, false);
        } catch (IOException e) {
            queue.release();
            throw e;
        }
        // extra elements by plugin
        for (DeviceSupportPlugin plugin : DeviceSupportPlugin.getPlugins()) {
            if (plugin.match(device)) {
log.finer("@@@ plugin for extra: " + plugin.getClass().getName());
                components.addAll(plugin.getExtraComponents(device));
                controllers.addAll(plugin.getExtraChildControllers(device));
                rumblers.addAll(plugin.getExtraRumblers(device));
            }
        }
log.fine("@@@ components: " + components.size() + ", " + components);
log.fine("@@@ rumblers: " + rumblers.size() + ", " + rumblers);
        return new OSXController(device, queue,
                components.toArray(Component[]::new),
                controllers.toArray(Controller[]::new),
                rumblers.toArray(Rumbler[]::new),
                type);
    }

    private static void createControllersFromDevice(OSXHIDDevice device, List<Controller> controllers) throws IOException {
        UsagePair usagePage = device.getUsagePair();
        if (usagePage == null) {
log.finer("device: '" + device.getProductName() + "' has no usage pair");
            return;
        }
log.fine("-------- device: '" + device.getProductName() + "' --------");
        List<OSXHIDElement> elements = device.getElements();
        if (usagePage.usagePage() == UsagePage.GENERIC_DESKTOP && (usagePage.usageId() == GenericDesktopUsageId.MOUSE ||
                usagePage.usageId() == GenericDesktopUsageId.POINTER)) {
log.fine("mouse device: '" + device.getProductName() + "' --------");
//            Controller mouse = createMouseFromDevice(device, elements);
//            if (mouse != null)
//                controllers.add(mouse);
        } else if (usagePage.usagePage() == UsagePage.GENERIC_DESKTOP && (usagePage.usageId() == GenericDesktopUsageId.KEYBOARD ||
                usagePage.usageId() == GenericDesktopUsageId.KEYPAD)) {
log.fine("keyboard device: '" + device.getProductName() + "' --------");
//            controllers.add(createKeyboardFromDevice(device, elements));
        } else if (usagePage.usagePage() == UsagePage.GENERIC_DESKTOP && usagePage.usageId() == GenericDesktopUsageId.JOYSTICK) {
log.fine("joystick device: '" + device.getProductName() + "' --------");
            controllers.add(createControllerFromDevice(device, elements, Controller.Type.STICK));
        } else if (usagePage.usagePage() == UsagePage.GENERIC_DESKTOP && usagePage.usageId() == GenericDesktopUsageId.MULTI_AXIS_CONTROLLER) {
log.fine("multi-axis device: '" + device.getProductName() + "' --------");
            controllers.add(createControllerFromDevice(device, elements, Controller.Type.STICK));
        } else if (usagePage.usagePage() == UsagePage.GENERIC_DESKTOP && usagePage.usageId() == GenericDesktopUsageId.GAME_PAD) {
log.fine("gamepad device: '" + device.getProductName() + "' --------");
            controllers.add(createControllerFromDevice(device, elements, Controller.Type.GAMEPAD));
        }
    }

    private void enumerateControllers() {
        controllers = new ArrayList<>();
        try {
            OSXHIDDeviceIterator it = new OSXHIDDeviceIterator();
            try {
                while (true) {
                    OSXHIDDevice device;
                    try {
                        device = it.next();
                        if (device == null)
                            break;
                        boolean deviceUsed = false;
                        try {
                            int oldSize = controllers.size();
                            createControllersFromDevice(device, controllers);
                            deviceUsed = oldSize != controllers.size();
                        } catch (IOException e) {
                            log.log(Level.FINE, "Failed to create controllers from device: " + device.getProductName(), e);
                        }
                        if (!deviceUsed)
                            device.release();
                    } catch (IOException e) {
                        log.log(Level.FINE, "Failed to enumerate device: ", e);
                    }
                }
            } finally {
                it.close();
            }
        } catch (IOException e) {
            log.log(Level.FINE, "Failed to enumerate devices: " + e.getMessage(), e);
        }
    }
}
