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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.SetupApi.SP_DEVINFO_DATA;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.RAWINPUTDEVICELIST;
import com.sun.jna.ptr.IntByReference;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.windows.User32Ex.HDEVINFO;

import static com.sun.jna.platform.win32.SetupApi.DIGCF_PRESENT;
import static com.sun.jna.platform.win32.SetupApi.SPDRP_DEVICEDESC;
import static com.sun.jna.platform.win32.WinBase.INVALID_HANDLE_VALUE;
import static com.sun.jna.platform.win32.WinError.ERROR_INSUFFICIENT_BUFFER;


/**
 * DirectInput implementation of controller environment
 *
 * @author martak
 * @author elias
 * @version 1.0
 */
public final class RawInputEnvironmentPlugin extends ControllerEnvironment {

    private static final Logger log = Logger.getLogger(RawInputEnvironmentPlugin.class.getName());

    private static boolean supported = false;

    static {
        String osName = System.getProperty("os.name", "").trim();
        if (osName.startsWith("Windows")) {
            supported = true;
        }
    }

    private final Controller[] controllers;

    /** Creates new DirectInputEnvironment */
    public RawInputEnvironmentPlugin() {
        RawInputEventQueue queue;
        Controller[] controllers = new Controller[] {};
        if (isSupported()) {
            try {
                queue = new RawInputEventQueue();
                controllers = enumControllers(queue);
            } catch (IOException e) {
                log.fine("Failed to enumerate devices: " + e.getMessage());
            }
        }
        this.controllers = controllers;
    }

    @Override
    public Controller[] getControllers() {
        return controllers;
    }

    private static SetupAPIDevice lookupSetupAPIDevice(String deviceName, List<SetupAPIDevice> setupapiDevices) {
        // First, replace # with / in the device name, since that
        // seems to be the format in raw input device name
        deviceName = deviceName.replaceAll("#", "\\\\").toUpperCase();
        for (SetupAPIDevice device : setupapiDevices) {
            if (deviceName.contains(device.getInstanceId().toUpperCase()))
                return device;
        }
        return null;
    }

    private static void createControllersFromDevices(RawInputEventQueue queue, List<Controller> controllers, List<RawDevice> devices, List<SetupAPIDevice> setupapiDevices) throws IOException {
        List<RawDevice> activeDevices = new ArrayList<>();
        for (RawDevice device : devices) {
            SetupAPIDevice setupapiDevice = lookupSetupAPIDevice(device.getName(), setupapiDevices);
            if (setupapiDevice == null) {
                // Either the device is an RDP or we failed to locate the
                // SetupAPI device that matches
                continue;
            }
            RawDeviceInfo info = device.getInfo();
            Controller controller = info.createControllerFromDevice(device, setupapiDevice);
            if (controller != null) {
                controllers.add(controller);
                activeDevices.add(device);
            }
        }
        queue.start(activeDevices);
    }

    private static void enumerateDevices(RawInputEventQueue queue, List<RawDevice> devicesList) throws IOException {
        IntByReference numDevices = new IntByReference();
        int res = User32.INSTANCE.GetRawInputDeviceList(null, numDevices, 12 /* sizeof(RAWINPUTDEVICELIST) */);
        if (-1 == res) {
            throw new IOException(String.format("Failed to get number of devices (%d)", Native.getLastError()));
        }
        RAWINPUTDEVICELIST[] devices = new RAWINPUTDEVICELIST[numDevices.getValue()];
        User32.INSTANCE.GetRawInputDeviceList(devices, numDevices, devices[0].size() /* sizeof(RAWINPUTDEVICELIST) */);
        for (int i = 0; i < numDevices.getValue(); i++) {
            RawDevice deviceObject = new RawDevice(queue, devices[i].hDevice, devices[i].dwType);
            devicesList.add(deviceObject);
        }
    }

    private Controller[] enumControllers(RawInputEventQueue queue) throws IOException {
        List<Controller> controllers = new ArrayList<>();
        List<RawDevice> devices = new ArrayList<>();
        enumerateDevices(queue, devices);
        List<SetupAPIDevice> setupapiDevices = enumSetupAPIDevices();
        createControllersFromDevices(queue, controllers, devices, setupapiDevices);
        return controllers.toArray(Controller[]::new);
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    /**
     * The raw input API, while being able to access
     * multiple mice and keyboards, is a bit raw (hah)
     * since it lacks some important features:
     * <ol>
     *  <li>
     *    The list of keyboards and the list of mice
     *    both include useless Terminal Server
     *    devices (RDP_MOU and RDP_KEY) that we'd
     *    like to skip.
     *  </li>
     *  <li>
     *    The device names returned by GetRawInputDeviceInfo()
     *    are not for display, but instead synthesized
     *    from a combination of a device instance id
     *    and a GUID.
     *  </li>
     * </ol>
     * A solution to both problems is the SetupAPI that allows
     * us to enumerate all keyboard and mouse devices and fetch their
     * descriptive names and at the same time filter out the unwanted
     * RDP devices.
     */
    private static List<SetupAPIDevice> enumSetupAPIDevices() throws IOException {
        List<SetupAPIDevice> devices = new ArrayList<>();
        nEnumSetupAPIDevices(getKeyboardClassGUID(), devices);
        nEnumSetupAPIDevices(getMouseClassGUID(), devices);
        return devices;
    }

    private static void nEnumSetupAPIDevices(byte[] guidArray, List<SetupAPIDevice> deviceList) throws IOException {
        SP_DEVINFO_DATA DeviceInfoData = new SP_DEVINFO_DATA();
        GUID setupClassGuid = new GUID();

        NativeUtil.unwrapGUID(guidArray, setupClassGuid);

        HDEVINFO hDevInfo = User32Ex.SetupApiInterface.INSTANCE.SetupDiGetClassDevs(setupClassGuid,
                null,
                null,
                DIGCF_PRESENT);

        if (hDevInfo == INVALID_HANDLE_VALUE) {
            throw new IOException(String.format("Failed to create device enumerator (%d)", Native.getLastError()));
        }

        DeviceInfoData.cbSize = DeviceInfoData.size();
        for (int i = 0; User32Ex.SetupApiInterface.INSTANCE.SetupDiEnumDeviceInfo(hDevInfo, i, DeviceInfoData); i++) {
            int DataT = 0;
            Memory buffer = new Memory(256);
            int buffersize = 0;

            while (!User32Ex.SetupApiInterface.INSTANCE.SetupDiGetDeviceRegistryProperty(
                    hDevInfo,
                    DeviceInfoData,
                    SPDRP_DEVICEDESC,
					DataT,
                    buffer,
                    buffersize,
					buffersize)) {
                if (Native.getLastError() == ERROR_INSUFFICIENT_BUFFER) {
                    buffer.close();
                    buffer = new Memory(buffersize);
                } else {
                    User32Ex.SetupApiInterface.INSTANCE.SetupDiDestroyDeviceInfoList(hDevInfo);
                    throw new IOException(String.format("Failed to get device description (%x)", Native.getLastError()));
                }
            }

            String deviceName = new String(buffer.getByteArray(0, buffersize), StandardCharsets.UTF_8);

            while (!User32Ex.SetupApiInterface.INSTANCE.SetupDiGetDeviceInstanceId(
                    hDevInfo,
                    DeviceInfoData,
                    buffer,
                    buffersize,
					buffersize))
            {
                if (Native.getLastError() == ERROR_INSUFFICIENT_BUFFER) {
                    buffer.close();
                    buffer = new Memory(buffersize);
                } else {
                    User32Ex.SetupApiInterface.INSTANCE.SetupDiDestroyDeviceInfoList(hDevInfo);
                    throw new IOException(String.format("Failed to get device instance id (%x)", Native.getLastError()));
                }
            }

            String deviceInstanceId = new String(buffer.getByteArray(0, buffersize), StandardCharsets.UTF_8);
            SetupAPIDevice setupApiDevice = new SetupAPIDevice(deviceInstanceId, deviceName);
            deviceList.add(setupApiDevice);
        }
        User32Ex.SetupApiInterface.INSTANCE.SetupDiDestroyDeviceInfoList(hDevInfo);
    }

    private static byte[] getKeyboardClassGUID() {
        return NativeUtil.wrapGUID(User32Ex.SetupApiInterface.GUID_DEVCLASS_KEYBOARD);
    }

    private static byte[] getMouseClassGUID() {
        return NativeUtil.wrapGUID(User32Ex.SetupApiInterface.GUID_DEVCLASS_MOUSE);
    }
}
