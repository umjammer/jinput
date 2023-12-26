/*
 * Copyright (C) 2003 Jeremy Booth (jeremy@newdawnsoftware.com)
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

package net.java.games.input.linux;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import net.java.games.input.AbstractComponent;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;
import net.java.games.input.Rumbler;


/**
 * Environment plugin for linux
 *
 * @author elias
 * @author Jeremy Booth (jeremy@newdawnsoftware.com)
 */
public final class LinuxEnvironmentPlugin extends ControllerEnvironment {

    private static final Logger log = Logger.getLogger(LinuxEnvironmentPlugin.class.getName());

    private static boolean supported = false;

    private List<Controller> controllers;
    private final List<LinuxDevice> devices = new ArrayList<>();
    private final static LinuxDeviceThread deviceThread = new LinuxDeviceThread();

    static {
        String osName = System.getProperty("os.name", "").trim();
        if (osName.equals("Linux")) {
            supported = true;
        }
    }

    static Object execute(LinuxDeviceTask task) throws IOException {
        return deviceThread.execute(task);
    }

    public LinuxEnvironmentPlugin() {
        if (isSupported()) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownHook));
        }
    }

    /**
     * Returns a list of all controllers available to this environment,
     * or an empty array if there are no controllers in this environment.
     *
     * @return Returns a list of all controllers available to this environment,
     * or an empty array if there are no controllers in this environment.
     */
    @Override
    public Controller[] getControllers() {
        if (this.controllers == null) {
            enumerateControllers();
log.fine("Linux plugin claims to have found " + controllers.size() + " controllers");
        }
        return controllers.toArray(Controller[]::new);
    }

    private static Component[] createComponents(List<LinuxEventComponent> eventComponents, LinuxEventDevice device) {
        LinuxEventComponent[][] povs = new LinuxEventComponent[4][2];
        List<LinuxComponent> components = new ArrayList<>();
        for (LinuxEventComponent eventComponent : eventComponents) {
            Component.Identifier identifier = eventComponent.getIdentifier();

            if (identifier == Component.Identifier.Axis.POV) {
                int nativeCode = eventComponent.getDescriptor().getCode();
                switch (nativeCode) {
                case NativeDefinitions.ABS_HAT0X:
                    povs[0][0] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT0Y:
                    povs[0][1] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT1X:
                    povs[1][0] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT1Y:
                    povs[1][1] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT2X:
                    povs[2][0] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT2Y:
                    povs[2][1] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT3X:
                    povs[3][0] = eventComponent;
                    break;
                case NativeDefinitions.ABS_HAT3Y:
                    povs[3][1] = eventComponent;
                    break;
                default:
                    log.fine("Unknown POV instance: " + nativeCode);
                    break;
                }
            } else if (identifier != null) {
                LinuxComponent component = new LinuxComponent(eventComponent);
                components.add(component);
                device.registerComponent(eventComponent.getDescriptor(), component);
            }
        }
        for (LinuxEventComponent[] pov : povs) {
            LinuxEventComponent x = pov[0];
            LinuxEventComponent y = pov[1];
            if (x != null && y != null) {
                LinuxComponent controllerComponent = new LinuxPOV(x, y);
                components.add(controllerComponent);
                device.registerComponent(x.getDescriptor(), controllerComponent);
                device.registerComponent(y.getDescriptor(), controllerComponent);
            }
        }
        Component[] componentsArray = new Component[components.size()];
        components.toArray(componentsArray);
        return componentsArray;
    }

    private static Mouse createMouseFromDevice(LinuxEventDevice device, Component[] components) throws IOException {
        Mouse mouse = new LinuxMouse(device, components, new Controller[] {}, device.getRumblers());
        if (mouse.getX() != null && mouse.getY() != null && mouse.getPrimaryButton() != null)
            return mouse;
        else
            return null;
    }

    private static Keyboard createKeyboardFromDevice(LinuxEventDevice device, Component[] components) throws IOException {
        Keyboard keyboard = new LinuxKeyboard(device, components, new Controller[] {}, device.getRumblers());
        return keyboard;
    }

    private static Controller createJoystickFromDevice(LinuxEventDevice device, Component[] components, Controller.Type type) throws IOException {
        Controller joystick = new LinuxControllerImpl(device, components, new Controller[] {}, device.getRumblers(), type);
        return joystick;
    }

    private static Controller createControllerFromDevice(LinuxEventDevice device) throws IOException {
        List<LinuxEventComponent> eventComponents = device.getComponents();
        Component[] components = createComponents(eventComponents, device);
        Controller.Type type = device.getType();

        if (type == Controller.Type.MOUSE) {
            return createMouseFromDevice(device, components);
        } else if (type == Controller.Type.KEYBOARD) {
            return createKeyboardFromDevice(device, components);
        } else if (type == Controller.Type.STICK || type == Controller.Type.GAMEPAD) {
            return createJoystickFromDevice(device, components, type);
        } else
            return null;
    }

    private void enumerateControllers() {
        this.controllers = new ArrayList<>();
        List<Controller> eventControllers = new ArrayList<>();
        List<Controller> jsControllers = new ArrayList<>();
        enumerateEventControllers(eventControllers);
        enumerateJoystickControllers(jsControllers);

        for (int i = 0; i < eventControllers.size(); i++) {
            for (int j = 0; j < jsControllers.size(); j++) {
                Controller evController = eventControllers.get(i);
                Controller jsController = jsControllers.get(j);

                // compare
                // Check if the nodes have the same name
                if (evController.getName().equals(jsController.getName())) {
                    // Check they have the same component count
                    Component[] evComponents = evController.getComponents();
                    Component[] jsComponents = jsController.getComponents();
                    if (evComponents.length == jsComponents.length) {
                        boolean foundADifference = false;
                        // check the component pairs are of the same type
                        for (int k = 0; k < evComponents.length; k++) {
                            // Check the type of the component is the same
                            if (!(evComponents[k].getIdentifier() == jsComponents[k].getIdentifier())) {
                                foundADifference = true;
                            }
                        }

                        if (!foundADifference) {
                            controllers.add(new LinuxCombinedController((LinuxControllerImpl) eventControllers.remove(i), (LinuxJoystickAbstractController) jsControllers.remove(j)));
                            i--;
                            j--;
                            break;
                        }
                    }
                }
            }
        }
        controllers.addAll(eventControllers);
        controllers.addAll(jsControllers);
    }

    private static Component.Identifier.Button getButtonIdentifier(int index) {
        return switch (index) {
            case 0 -> Component.Identifier.Button._0;
            case 1 -> Component.Identifier.Button._1;
            case 2 -> Component.Identifier.Button._2;
            case 3 -> Component.Identifier.Button._3;
            case 4 -> Component.Identifier.Button._4;
            case 5 -> Component.Identifier.Button._5;
            case 6 -> Component.Identifier.Button._6;
            case 7 -> Component.Identifier.Button._7;
            case 8 -> Component.Identifier.Button._8;
            case 9 -> Component.Identifier.Button._9;
            case 10 -> Component.Identifier.Button._10;
            case 11 -> Component.Identifier.Button._11;
            case 12 -> Component.Identifier.Button._12;
            case 13 -> Component.Identifier.Button._13;
            case 14 -> Component.Identifier.Button._14;
            case 15 -> Component.Identifier.Button._15;
            case 16 -> Component.Identifier.Button._16;
            case 17 -> Component.Identifier.Button._17;
            case 18 -> Component.Identifier.Button._18;
            case 19 -> Component.Identifier.Button._19;
            case 20 -> Component.Identifier.Button._20;
            case 21 -> Component.Identifier.Button._21;
            case 22 -> Component.Identifier.Button._22;
            case 23 -> Component.Identifier.Button._23;
            case 24 -> Component.Identifier.Button._24;
            case 25 -> Component.Identifier.Button._25;
            case 26 -> Component.Identifier.Button._26;
            case 27 -> Component.Identifier.Button._27;
            case 28 -> Component.Identifier.Button._28;
            case 29 -> Component.Identifier.Button._29;
            case 30 -> Component.Identifier.Button._30;
            case 31 -> Component.Identifier.Button._31;
            default -> null;
        };
    }

    private static Controller createJoystickFromJoystickDevice(LinuxJoystickDevice device) {
        List<AbstractComponent> components = new ArrayList<>();
        byte[] axisMap = device.getAxisMap();
        char[] buttonMap = device.getButtonMap();
        LinuxJoystickAxis[] hatBits = new LinuxJoystickAxis[6];

        for (int i = 0; i < device.getNumButtons(); i++) {
            Component.Identifier buttonId = LinuxNativeTypesMap.getButtonID(buttonMap[i]);
            if (buttonId != null) {
                LinuxJoystickButton button = new LinuxJoystickButton(buttonId);
                device.registerButton(i, button);
                components.add(button);
            }
        }
        for (int i = 0; i < device.getNumAxes(); i++) {
            Component.Identifier.Axis axisId;
            axisId = (Component.Identifier.Axis) LinuxNativeTypesMap.getAbsAxisID(axisMap[i]);
            LinuxJoystickAxis axis = new LinuxJoystickAxis(axisId);

            device.registerAxis(i, axis);

            if (axisMap[i] == NativeDefinitions.ABS_HAT0X) {
                hatBits[0] = axis;
            } else if (axisMap[i] == NativeDefinitions.ABS_HAT0Y) {
                hatBits[1] = axis;
                axis = new LinuxJoystickPOV(Component.Identifier.Axis.POV, hatBits[0], hatBits[1]);
                device.registerPOV((LinuxJoystickPOV) axis);
                components.add(axis);
            } else if (axisMap[i] == NativeDefinitions.ABS_HAT1X) {
                hatBits[2] = axis;
            } else if (axisMap[i] == NativeDefinitions.ABS_HAT1Y) {
                hatBits[3] = axis;
                axis = new LinuxJoystickPOV(Component.Identifier.Axis.POV, hatBits[2], hatBits[3]);
                device.registerPOV((LinuxJoystickPOV) axis);
                components.add(axis);
            } else if (axisMap[i] == NativeDefinitions.ABS_HAT2X) {
                hatBits[4] = axis;
            } else if (axisMap[i] == NativeDefinitions.ABS_HAT2Y) {
                hatBits[5] = axis;
                axis = new LinuxJoystickPOV(Component.Identifier.Axis.POV, hatBits[4], hatBits[5]);
                device.registerPOV((LinuxJoystickPOV) axis);
                components.add(axis);
            } else {
                components.add(axis);
            }
        }

        return new LinuxJoystickAbstractController(device, components.toArray(new Component[] {}), new Controller[] {}, new Rumbler[] {});
    }

    private void enumerateJoystickControllers(List<Controller> controllers) {
        File[] joystickDeviceFiles = enumerateJoystickDeviceFiles("/dev/input");
        if (joystickDeviceFiles == null || joystickDeviceFiles.length == 0) {
            joystickDeviceFiles = enumerateJoystickDeviceFiles("/dev");
            if (joystickDeviceFiles == null)
                return;
        }
        for (File eventFile : joystickDeviceFiles) {
            try {
                String path = getAbsolutePathPrivileged(eventFile);
                LinuxJoystickDevice device = new LinuxJoystickDevice(path);
                Controller controller = createJoystickFromJoystickDevice(device);
                if (controller != null) {
                    controllers.add(controller);
                    devices.add(device);
                } else
                    device.close();
            } catch (IOException e) {
                log.fine("Failed to open device (" + eventFile + "): " + e.getMessage());
            }
        }
    }

    private static File[] enumerateJoystickDeviceFiles(String devPath) {
        File dev = new File(devPath);
        return listFilesPrivileged(dev, (dir, name) -> name.startsWith("js"));
    }

    private static String getAbsolutePathPrivileged(File file) {
        return file.getAbsolutePath();
    }

    private static File[] listFilesPrivileged(File dir, FilenameFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files == null) {
            log.fine("dir " + dir.getName() + " exists: " + dir.exists() + ", is writable: " + dir.isDirectory());
            files = new File[] {};
        } else {
            Arrays.sort(files, Comparator.comparing(File::getName));
        }
        return files;
    }

    private void enumerateEventControllers(List<Controller> controllers) {
        File dev = new File("/dev/input");
        File[] eventDeviceFiles = listFilesPrivileged(dev, (File dir, String name) -> name.startsWith("event"));

        if (eventDeviceFiles == null)
            return;
        for (File eventFile : eventDeviceFiles) {
            try {
                String path = getAbsolutePathPrivileged(eventFile);
                LinuxEventDevice device = new LinuxEventDevice(path);
                try {
                    Controller controller = createControllerFromDevice(device);
                    if (controller != null) {
                        controllers.add(controller);
                        devices.add(device);
                    } else
                        device.close();
                } catch (IOException e) {
                    log.fine("Failed to create Controller: " + e.getMessage());
                    device.close();
                }
            } catch (IOException e) {
                log.fine("Failed to open device (" + eventFile + "): " + e.getMessage());
            }
        }
    }

    private void shutdownHook() {
        for (LinuxDevice linuxDevice : devices) {
            try {
                LinuxDevice device = linuxDevice;
                device.close();
            } catch (IOException e) {
                log.fine("Failed to close device: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isSupported() {
        return supported;
    }
}
