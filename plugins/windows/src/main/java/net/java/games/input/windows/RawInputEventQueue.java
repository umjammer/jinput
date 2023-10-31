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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.IntByReference;

import static com.sun.jna.platform.win32.WinUser.RIM_TYPEKEYBOARD;
import static com.sun.jna.platform.win32.WinUser.RIM_TYPEMOUSE;


/**
 * Java wrapper of RAWDEVICELIST
 *
 * @author elias
 * @version 1.0
 */
final class RawInputEventQueue {

    private final Object monitor = new Object();

    private List<RawDevice> devices;

    public void start(List<RawDevice> devices) throws IOException {
        this.devices = devices;
        QueueThread queue = new QueueThread();
        synchronized (monitor) {
            queue.start();
            // wait for initialization
            while (!queue.isInitialized()) {
                try {
                    monitor.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }
        if (queue.getException() != null)
            throw queue.getException();
    }

    private RawDevice lookupDevice(HANDLE handle) {
        for (RawDevice device : devices) {
            if (device.getHandle().equals(handle))
                return device;
        }
        return null;
    }

    /** Event methods called back from native code in nPoll() */
    private void addMouseEvent(HANDLE handle, long millis, int flags, int buttonFlags, int buttonData, long rawButtons, long lastX, long lastY, long extraInformation) {
        RawDevice device = lookupDevice(handle);
        if (device == null)
            return;
        device.addMouseEvent(millis, flags, buttonFlags, buttonData, rawButtons, lastX, lastY, extraInformation);
    }

    private void addKeyboardEvent(HANDLE handle, long millis, int makeCode, int flags, int vkey, int message, long extraInformation) {
        RawDevice device = lookupDevice(handle);
        if (device == null)
            return;
        device.addKeyboardEvent(millis, makeCode, flags, vkey, message, extraInformation);
    }

    private void poll(DummyWindow window) throws IOException {
        HWND hwnd = window.getHwnd();
        MSG msg = new MSG();

        if (User32.INSTANCE.GetMessage(msg, hwnd, 0, 0) != 0) {
            if (msg.message != User32Ex.WM_INPUT) {
                User32.INSTANCE.DefWindowProc(hwnd, msg.message, msg.wParam, msg.lParam);
                return; // ignore it
            }
            long time = msg.time;
            IntByReference inputSize = new IntByReference();
            User32Ex.RAWINPUT[] inputData = new User32Ex.RAWINPUT[inputSize.getValue()];
            if (User32Ex.INSTANCE.GetRawInputData(msg.lParam.toPointer(), User32Ex.RID_INPUT, null, inputSize, inputData[0].size()) == -1) {
                User32.INSTANCE.DefWindowProc(hwnd, msg.message, msg.wParam, msg.lParam);
                throw new IOException(String.format("Failed to get raw input data size (%d)", Native.getLastError()));
            }
            if (User32Ex.INSTANCE.GetRawInputData(msg.lParam.toPointer(), User32Ex.RID_INPUT, inputData, inputSize, inputData[0].size()) == -1) {
                User32.INSTANCE.DefWindowProc(hwnd, msg.message, msg.wParam, msg.lParam);
                throw new IOException(String.format("Failed to get raw input data (%d)", Native.getLastError()));
            }
            switch (inputData[0].header.dwType) {
            case RIM_TYPEMOUSE:
                handleMouseEvent(time, inputData[0]);
                break;
            case RIM_TYPEKEYBOARD:
                handleKeyboardEvent(time, inputData[0]);
                break;
            default:
                // ignore other types of message
                break;
            }
            User32.INSTANCE.DefWindowProc(hwnd, msg.message, msg.wParam, msg.lParam);
        }
    }

    private void handleMouseEvent(long time, User32Ex.RAWINPUT data) {
        addMouseEvent(
                data.header.hDevice,
                time,
                data.data.mouse.usFlags,
                data.data.mouse.usButtonFlags,
                // The Raw Input spec says that the usButtonData
                // is a signed value, if RI_MOUSE_WHEEL
                // is set in usFlags. However, usButtonData
                // is an unsigned value, for unknown reasons,
                // and since its only known use is the wheel
                // delta, we'll convert it to a signed value here
                data.data.mouse.usButtonData,
                data.data.mouse.ulRawButtons,
                data.data.mouse.lLastX,
                data.data.mouse.lLastY,
                data.data.mouse.ulExtraInformation
        );
    }

    private void handleKeyboardEvent(long time, User32Ex.RAWINPUT data) {
        addKeyboardEvent(
                data.header.hDevice,
                time,
                data.data.keyboard.MakeCode,
                data.data.keyboard.Flags,
                data.data.keyboard.VKey,
                data.data.keyboard.Message,
                data.data.keyboard.ExtraInformation
        );
    }

    private static void registerDevices(DummyWindow window, RawDeviceInfo[] deviceInfos) throws IOException {
        int numDevices = deviceInfos.length;

//        res = GetRegisteredRawInputDevices(null, numDevices, sizeof(RAWINPUTDEVICE));
//        if (numDevices > 0) {
//            devices = new RAWINPUTDEVICE[numDevices];
//            res = GetRegisteredRawInputDevices(devices, numDevices, sizeof(RAWINPUTDEVICE));
//            if (res == -1) {
//                throw new IOException(String.format("Failed to get registered raw devices (%d)", Native.getLastError()));
//            }
//            for (i = 0; i < numDevices; i++) {
//                System.err.printf("from windows: registered: %d %d %s (of %d)", devices[i].usUsagePage, devices[i].usUsage, devices[i].hwndTarget, numDevices);
//            }
//        }
        User32Ex.RAWINPUTDEVICE[] devices = new User32Ex.RAWINPUTDEVICE[numDevices];
        for (int i = 0; i < deviceInfos.length; i++) {
            RawDeviceInfo deviceObj = deviceInfos[i];
            int usage = deviceObj.getUsage();
            int usagePage = deviceObj.getUsagePage();
            devices[i] = new User32Ex.RAWINPUTDEVICE();
            devices[i].usUsagePage = (short) (usagePage & 0xffff);
            devices[i].usUsage = (short) (usage & 0xffff);
            devices[i].dwFlags = 0;
            devices[i].hwndTarget = window.getHwnd();
        }
        boolean res = User32Ex.INSTANCE.RegisterRawInputDevices(devices, numDevices, devices[0].size());
        if (!res)
            throw new IOException(String.format("Failed to register raw devices (%d)", Native.getLastError()));
    }

    private final class QueueThread extends Thread {

        private boolean initialized;
        private DummyWindow window;
        private IOException exception;

        public QueueThread() {
            setDaemon(true);
        }

        public boolean isInitialized() {
            return initialized;
        }

        public IOException getException() {
            return exception;
        }

        @Override
        public void run() {
            // We have to create the window in the (private) queue thread
            // TODO so it's easy to change event listener system?
            try {
                window = new DummyWindow();
            } catch (IOException e) {
                exception = e;
            }
            initialized = true;
            synchronized (monitor) {
                monitor.notify();
            }
            if (exception != null)
                return;
            Set<RawDeviceInfo> activeInfos = new HashSet<>();
            try {
                for (RawDevice device : devices) {
                    activeInfos.add(device.getInfo());
                }
                RawDeviceInfo[] activeInfosArray = new RawDeviceInfo[activeInfos.size()];
                activeInfos.toArray(activeInfosArray);
                try {
                    registerDevices(window, activeInfosArray);
                    while (!isInterrupted()) {
                        poll(window);
                    }
                } finally {
                    window.destroy();
                }
            } catch (IOException e) {
                exception = e;
            }
        }
    }
}
