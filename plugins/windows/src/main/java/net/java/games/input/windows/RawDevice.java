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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import net.java.games.input.AbstractController;
import net.java.games.input.windows.User32Ex.RID_DEVICE_INFO;
import net.java.games.input.windows.User32Ex.RID_DEVICE_INFO_HID;
import net.java.games.input.windows.User32Ex.RID_DEVICE_INFO_KEYBOARD;
import net.java.games.input.windows.User32Ex.RID_DEVICE_INFO_MOUSE;

import static net.java.games.input.windows.User32Ex.User32ExTrait.RIDI_DEVICEINFO;
import static net.java.games.input.windows.User32Ex.User32ExTrait.RIDI_DEVICENAME;


/**
 * Java wrapper of RAWDEVICELIST
 *
 * @author elias
 * @version 1.0
 */
final class RawDevice {

    /** Left Button changed to down. */
    public final static int RI_MOUSE_LEFT_BUTTON_DOWN = 0x0001;
    /** Left Button changed to up. */
    public final static int RI_MOUSE_LEFT_BUTTON_UP = 0x0002;
    /** Right Button changed to down. */
    public final static int RI_MOUSE_RIGHT_BUTTON_DOWN = 0x0004;
    /** Right Button changed to up. */
    public final static int RI_MOUSE_RIGHT_BUTTON_UP = 0x0008;
    /** Middle Button changed to down. */
    public final static int RI_MOUSE_MIDDLE_BUTTON_DOWN = 0x0010;
    /** Middle Button changed to up. */
    public final static int RI_MOUSE_MIDDLE_BUTTON_UP = 0x0020;

    public final static int RI_MOUSE_BUTTON_1_DOWN = RI_MOUSE_LEFT_BUTTON_DOWN;
    public final static int RI_MOUSE_BUTTON_1_UP = RI_MOUSE_LEFT_BUTTON_UP;
    public final static int RI_MOUSE_BUTTON_2_DOWN = RI_MOUSE_RIGHT_BUTTON_DOWN;
    public final static int RI_MOUSE_BUTTON_2_UP = RI_MOUSE_RIGHT_BUTTON_UP;
    public final static int RI_MOUSE_BUTTON_3_DOWN = RI_MOUSE_MIDDLE_BUTTON_DOWN;
    public final static int RI_MOUSE_BUTTON_3_UP = RI_MOUSE_MIDDLE_BUTTON_UP;

    public final static int RI_MOUSE_BUTTON_4_DOWN = 0x0040;
    public final static int RI_MOUSE_BUTTON_4_UP = 0x0080;
    public final static int RI_MOUSE_BUTTON_5_DOWN = 0x0100;
    public final static int RI_MOUSE_BUTTON_5_UP = 0x0200;

    /*
     * If usButtonFlags has RI_MOUSE_WHEEL, the wheel delta is stored in usButtonData.
     * Take it as a signed value.
     */
    public final static int RI_MOUSE_WHEEL = 0x0400;

    public final static int MOUSE_MOVE_RELATIVE = 0;
    public final static int MOUSE_MOVE_ABSOLUTE = 1;
    /** the coordinates are mapped to the virtual desktop */
    public final static int MOUSE_VIRTUAL_DESKTOP = 0x02;
    /** requery for mouse attributes */
    public final static int MOUSE_ATTRIBUTES_CHANGED = 0x04;

    public final static int RIM_TYPEHID = 2;
    public final static int RIM_TYPEKEYBOARD = 1;
    public final static int RIM_TYPEMOUSE = 0;

    public final static int WM_KEYDOWN = 0x0100;
    public final static int WM_KEYUP = 0x0101;
    public final static int WM_SYSKEYDOWN = 0x0104;
    public final static int WM_SYSKEYUP = 0x0105;

    private final RawInputEventQueue queue;
    private final HANDLE handle;
    private final int type;

    // Events from the event queue thread end here
    private DataQueue<RawKeyboardEvent> keyboardEvents;
    private DataQueue<RawMouseEvent> mouseEvents;

    // After processing in poll*(), the events are placed here
    private DataQueue<RawKeyboardEvent> processedKeyboardEvents;
    private DataQueue<RawMouseEvent> processedMouseEvents;

    // mouse state
    private final boolean[] buttonStates = new boolean[5];
    private int wheel;
    private int relativeX;
    private int relativeY;
    private int lastX;
    private int lastY;

    // Last x, y for converting absolute events to relative
    private int eventRelativeX;
    private int eventRelativeY;
    private int eventLastX;
    private int eventLastY;

    /** keyboard state */
    private final boolean[] keyStates = new boolean[0xFF];

    public RawDevice(RawInputEventQueue queue, HANDLE handle, int type) {
        this.queue = queue;
        this.handle = handle;
        this.type = type;
        setBufferSize(AbstractController.EVENT_QUEUE_DEPTH);
    }

    /** Careful, this is called from the event queue thread */
    public synchronized void addMouseEvent(long millis, int flags, int buttonFlags, int buttonData, long rawButtons, long lastX, long lastY, long extraInformation) {
        if (mouseEvents.hasRemaining()) {
            RawMouseEvent event = mouseEvents.get();
            event.set(millis, flags, buttonFlags, buttonData, rawButtons, lastX, lastY, extraInformation);
        }
    }

    /** Careful, this is called from the event queue thread */
    public synchronized void addKeyboardEvent(long millis, int makeCode, int flags, int vkey, int message, long extraInformation) {
        if (keyboardEvents.hasRemaining()) {
            RawKeyboardEvent event = keyboardEvents.get();
            event.set(millis, makeCode, flags, vkey, message, extraInformation);
        }
    }

    public synchronized void pollMouse() {
        relativeX = relativeY = wheel = 0;
        mouseEvents.flip();
        while (mouseEvents.hasRemaining()) {
            RawMouseEvent event = mouseEvents.get();
            boolean hasUpdate = processMouseEvent(event);
            if (hasUpdate && processedMouseEvents.hasRemaining()) {
                RawMouseEvent processedEvent = processedMouseEvents.get();
                processedEvent.set(event);
            }
        }
        mouseEvents.compact();
    }

    public synchronized void pollKeyboard() {
        keyboardEvents.flip();
        while (keyboardEvents.hasRemaining()) {
            RawKeyboardEvent event = keyboardEvents.get();
            boolean hasUpdate = processKeyboardEvent(event);
            if (hasUpdate && processedKeyboardEvents.hasRemaining()) {
                RawKeyboardEvent processedEvent = processedKeyboardEvents.get();
                processedEvent.set(event);
            }
        }
        keyboardEvents.compact();
    }

    private boolean updateButtonState(int buttonId, int buttonFlags, int downFlag, int upFlag) {
        if (buttonId >= buttonStates.length)
            return false;
        if ((buttonFlags & downFlag) != 0) {
            buttonStates[buttonId] = true;
            return true;
        } else if ((buttonFlags & upFlag) != 0) {
            buttonStates[buttonId] = false;
            return true;
        } else
            return false;
    }

    private boolean processKeyboardEvent(RawKeyboardEvent event) {
        int message = event.getMessage();
        int vkey = event.getVKey();
        if (vkey >= keyStates.length)
            return false;
        if (message == WM_KEYDOWN || message == WM_SYSKEYDOWN) {
            keyStates[vkey] = true;
            return true;
        } else if (message == WM_KEYUP || message == WM_SYSKEYUP) {
            keyStates[vkey] = false;
            return true;
        } else
            return false;
    }

    public boolean isKeyDown(int vkey) {
        return keyStates[vkey];
    }

    private boolean processMouseEvent(RawMouseEvent event) {
        boolean hasUpdate = false;
        int buttonFlags = event.getButtonFlags();
        hasUpdate = updateButtonState(0, buttonFlags, RI_MOUSE_BUTTON_1_DOWN, RI_MOUSE_BUTTON_1_UP) || hasUpdate;
        hasUpdate = updateButtonState(1, buttonFlags, RI_MOUSE_BUTTON_2_DOWN, RI_MOUSE_BUTTON_2_UP) || hasUpdate;
        hasUpdate = updateButtonState(2, buttonFlags, RI_MOUSE_BUTTON_3_DOWN, RI_MOUSE_BUTTON_3_UP) || hasUpdate;
        hasUpdate = updateButtonState(3, buttonFlags, RI_MOUSE_BUTTON_4_DOWN, RI_MOUSE_BUTTON_4_UP) || hasUpdate;
        hasUpdate = updateButtonState(4, buttonFlags, RI_MOUSE_BUTTON_5_DOWN, RI_MOUSE_BUTTON_5_UP) || hasUpdate;
        int dx;
        int dy;
        if ((event.getFlags() & MOUSE_MOVE_ABSOLUTE) != 0) {
            dx = event.getLastX() - lastX;
            dy = event.getLastY() - lastY;
            lastX = event.getLastX();
            lastY = event.getLastY();
        } else {
            dx = event.getLastX();
            dy = event.getLastY();
        }
        int dwheel = 0;
        if ((buttonFlags & RI_MOUSE_WHEEL) != 0)
            dwheel = event.getWheelDelta();
        relativeX += dx;
        relativeY += dy;
        wheel += dwheel;
        hasUpdate = dx != 0 || dy != 0 || dwheel != 0 || hasUpdate;
        return hasUpdate;
    }

    public int getWheel() {
        return wheel;
    }

    public int getEventRelativeX() {
        return eventRelativeX;
    }

    public int getEventRelativeY() {
        return eventRelativeY;
    }

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public synchronized boolean getNextKeyboardEvent(RawKeyboardEvent event) {
        processedKeyboardEvents.flip();
        if (!processedKeyboardEvents.hasRemaining()) {
            processedKeyboardEvents.compact();
            return false;
        }
        RawKeyboardEvent nextEvent = processedKeyboardEvents.get();
        event.set(nextEvent);
        processedKeyboardEvents.compact();
        return true;
    }

    public synchronized boolean getNextMouseEvent(RawMouseEvent event) {
        processedMouseEvents.flip();
        if (!processedMouseEvents.hasRemaining()) {
            processedMouseEvents.compact();
            return false;
        }
        RawMouseEvent nextEvent = processedMouseEvents.get();
        if ((nextEvent.getFlags() & MOUSE_MOVE_ABSOLUTE) != 0) {
            eventRelativeX = nextEvent.getLastX() - eventLastX;
            eventRelativeY = nextEvent.getLastY() - eventLastY;
            eventLastX = nextEvent.getLastX();
            eventLastY = nextEvent.getLastY();
        } else {
            eventRelativeX = nextEvent.getLastX();
            eventRelativeY = nextEvent.getLastY();
        }
        event.set(nextEvent);
        processedMouseEvents.compact();
        return true;
    }

    public boolean getButtonState(int buttonId) {
        if (buttonId >= buttonStates.length)
            return false;
        return buttonStates[buttonId];
    }

    public void setBufferSize(int size) {
        keyboardEvents = new DataQueue<>(size, RawKeyboardEvent.class);
        mouseEvents = new DataQueue<>(size, RawMouseEvent.class);
        processedKeyboardEvents = new DataQueue<>(size, RawKeyboardEvent.class);
        processedMouseEvents = new DataQueue<>(size, RawMouseEvent.class);
    }

    public int getType() {
        return type;
    }

    public HANDLE getHandle() {
        return handle;
    }

    public String getName() throws IOException {
        IntByReference nameLength = new IntByReference();

        @SuppressWarnings("UnusedAssignment")
        int res = User32Ex.INSTANCE.GetRawInputDeviceInfoA(handle, RIDI_DEVICENAME, null, nameLength);
        Memory name = new Memory(nameLength.getValue());
        res = User32Ex.INSTANCE.GetRawInputDeviceInfoA(handle, RIDI_DEVICENAME, name, nameLength);
        if (-1 == res) {
            throw new IOException(String.format("Failed to get device name (%d)", Native.getLastError()));
        }
        return name.getString(0, StandardCharsets.UTF_8.name());
    }

    public RawDeviceInfo getInfo() throws IOException {
        return nGetInfo(this, handle);
    }

    private static RawDeviceInfo createKeyboardInfo(RawDevice deviceObj, RID_DEVICE_INFO_KEYBOARD deviceInfo) {
        return new RawKeyboardInfo(deviceObj,
                deviceInfo.dwType, deviceInfo.dwSubType, deviceInfo.dwKeyboardMode,
                deviceInfo.dwNumberOfFunctionKeys, deviceInfo.dwNumberOfIndicators, deviceInfo.dwNumberOfKeysTotal);
    }

    private static RawDeviceInfo createMouseInfo(RawDevice deviceObj, RID_DEVICE_INFO_MOUSE deviceInfo) {
        return new RawMouseInfo(deviceObj, deviceInfo.dwId, deviceInfo.dwNumberOfButtons, deviceInfo.dwSampleRate);
    }

    private static RawDeviceInfo createHIDInfo(RawDevice deviceObj, RID_DEVICE_INFO_HID deviceInfo) {
        return new RawHIDInfo(deviceObj, deviceInfo.dwVendorId, deviceInfo.dwProductId, deviceInfo.dwVersionNumber, deviceInfo.usUsagePage, deviceInfo.usUsage);
    }

    private static RawDeviceInfo nGetInfo(RawDevice deviceObj, HANDLE handle) throws IOException {
        RID_DEVICE_INFO deviceInfo = new User32Ex.RID_DEVICE_INFO();
        IntByReference size = new IntByReference(deviceInfo.size());

        deviceInfo.cbSize = size.getValue();
        int res = User32Ex.INSTANCE.GetRawInputDeviceInfoA(handle, RIDI_DEVICEINFO, deviceInfo.getPointer(), size);
        if (-1 == res) {
            throw new IOException(String.format("Failed to get device info (%d)", Native.getLastError()));
        }
        return switch (deviceInfo.dwType) {
            case RIM_TYPEHID -> createHIDInfo(deviceObj, deviceInfo.u.hid);
            case RIM_TYPEKEYBOARD -> createKeyboardInfo(deviceObj, deviceInfo.u.keyboard);
            case RIM_TYPEMOUSE -> createMouseInfo(deviceObj, deviceInfo.u.mouse);
            default -> throw new IOException(String.format("Unknown device type: %d", deviceInfo.dwType));
        };
    }
}
