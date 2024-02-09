/*
 * Copyright (c) 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package net.java.games.input.windows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerListenerSupport;
import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;


/**
 * DirectInput implementation of controller environment
 *
 * @author martak
 * @author elias
 * @version 1.0
 */
final class DirectInputEnvironmentPlugin extends ControllerListenerSupport implements ControllerEnvironment {

    private static final Logger log = Logger.getLogger(DirectInputEnvironmentPlugin.class.getName());

    private static boolean supported = false;

    static {
        String osName = System.getProperty("os.name", "").trim();
        if (osName.startsWith("Windows")) {
            supported = true;
        }
    }

    private final List<Controller> controllers = new ArrayList<>();
    private final List<IDirectInputDevice> activeDevices = new ArrayList<>();
    private final DummyWindow window;

    /** Creates new DirectInputEnvironment */
    public DirectInputEnvironmentPlugin() {
        DummyWindow window = null;
        if (isSupported()) {
            try {
                window = new DummyWindow();
            } catch (IOException e) {
                log.fine("Failed to enumerate devices: " + e.getMessage());
            }
            this.window = window;
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownHook));
        } else {
            // These are final fields, so can't set them, then over ride
            // them if we are supported.
            this.window = null;
        }
    }

    @Override
    public Controller[] getControllers() {
        if (isSupported()) {
            try {
                try {
                    enumControllers(window);
                } catch (IOException e) {
                    window.destroy();
                    throw e;
                }
            } catch (IOException e) {
                log.fine("Failed to enumerate devices: " + e.getMessage());
            }
        }

        return controllers.toArray(Controller[]::new);
    }

    private Component[] createComponents(IDirectInputDevice device, boolean mapMouseButtons) {
        List<DIDeviceObject> deviceObjects = device.getObjects();
        List<DIComponent> controllerComponents = new ArrayList<>();
        for (DIDeviceObject deviceObject : deviceObjects) {
            Component.Identifier identifier = deviceObject.getIdentifier();
            if (identifier == null)
                continue;
            if (mapMouseButtons && identifier instanceof Component.Identifier.Button) {
                identifier = DIIdentifierMap.mapMouseButtonIdentifier((Component.Identifier.Button) identifier);
            }
            DIComponent component = new DIComponent(identifier, deviceObject);
            controllerComponents.add(component);
            device.registerComponent(deviceObject, component);
        }
        return controllerComponents.toArray(Component[]::new);
    }

    private Mouse createMouseFromDevice(IDirectInputDevice device) {
        Component[] components = createComponents(device, true);
        Mouse mouse = new DIMouse(device, components, new Controller[] {}, device.getRumblers());
        if (mouse.getX() != null && mouse.getY() != null && mouse.getPrimaryButton() != null)
            return mouse;
        else
            return null;
    }

    private AbstractController createControllerFromDevice(IDirectInputDevice device, Controller.Type type) {
        Component[] components = createComponents(device, false);
        return new DIAControllerImpl(device, components, new Controller[0], device.getRumblers(), type);
    }

    private Keyboard createKeyboardFromDevice(IDirectInputDevice device) {
        Component[] components = createComponents(device, false);
        return new DIKeyboard(device, components, new Controller[] {}, device.getRumblers());
    }

    private Controller createControllerFromDevice(IDirectInputDevice device) {
        return switch (device.getType()) {
            case IDirectInputDevice.DI8DEVTYPE_MOUSE -> createMouseFromDevice(device);
            case IDirectInputDevice.DI8DEVTYPE_KEYBOARD -> createKeyboardFromDevice(device);
            case IDirectInputDevice.DI8DEVTYPE_GAMEPAD -> createControllerFromDevice(device, Controller.Type.GAMEPAD);
            case IDirectInputDevice.DI8DEVTYPE_DRIVING -> createControllerFromDevice(device, Controller.Type.WHEEL);
            case IDirectInputDevice.DI8DEVTYPE_1STPERSON, IDirectInputDevice.DI8DEVTYPE_FLIGHT, IDirectInputDevice.DI8DEVTYPE_JOYSTICK ->
                    createControllerFromDevice(device, Controller.Type.STICK);
            default -> createControllerFromDevice(device, Controller.Type.UNKNOWN);
        };
    }

    private void enumControllers(DummyWindow window) throws IOException {
        IDirectInput dinput = new IDirectInput(window);
        try {
            List<IDirectInputDevice> devices = dinput.getDevices();
            for (IDirectInputDevice device : devices) {
                Controller controller = createControllerFromDevice(device);
                if (controller != null) {
                    controllers.add(controller);
                    activeDevices.add(device);
                } else
                    device.release();
            }
        } finally {
            dinput.release();
        }
    }

    private void shutdownHook() {
        // Release the devices to kill off active force feedback effects
        for (IDirectInputDevice device : activeDevices) {
            device.release();
        }
        // We won't release the window since it is
        // owned by the thread that created the environment.
    }

    @Override
    public boolean isSupported() {
        return supported;
    }
}