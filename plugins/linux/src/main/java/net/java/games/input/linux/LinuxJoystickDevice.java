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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import net.java.games.input.AbstractController;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import static com.sun.jna.platform.linux.ErrNo.EAGAIN;
import static net.java.games.input.linux.LinuxIO.ABS_MAX;
import static net.java.games.input.linux.LinuxIO.BTN_MISC;
import static net.java.games.input.linux.LinuxIO.JSIOCGAXES;
import static net.java.games.input.linux.LinuxIO.JSIOCGAXMAP;
import static net.java.games.input.linux.LinuxIO.JSIOCGBTNMAP;
import static net.java.games.input.linux.LinuxIO.JSIOCGBUTTONS;
import static net.java.games.input.linux.LinuxIO.JSIOCGNAME;
import static net.java.games.input.linux.LinuxIO.JSIOCGVERSION;
import static net.java.games.input.linux.LinuxIO.KEY_MAX;
import static net.java.games.input.linux.LinuxIO.O_NONBLOCK;
import static net.java.games.input.linux.LinuxIO.O_RDONLY;


/**
 * @author elias
 */
final class LinuxJoystickDevice implements LinuxDevice {

    /** button pressed/released */
    public final static int JS_EVENT_BUTTON = 0x01;
    /** joystick moved */
    public final static int JS_EVENT_AXIS = 0x02;
    /** initial state of device */
    public final static int JS_EVENT_INIT = 0x80;

    public final static int AXIS_MAX_VALUE = 32767;

    private final long fd;
    private final String name;

    private final LinuxJoystickEvent joystick_event = new LinuxJoystickEvent();
    private final Event event = new Event();
    private final LinuxJoystickButton[] buttons;
    private final LinuxJoystickAxis[] axes;
    private final Map<Integer, LinuxJoystickPOV> povXs = new HashMap<>();
    private final Map<Integer, LinuxJoystickPOV> povYs = new HashMap<>();
    private final byte[] axisMap;
    private final char[] buttonMap;

    private EventQueue event_queue;

    /* Closed state variable that protects the validity of the file descriptor.
     *  Access to the closed state must be synchronized
     */
    private boolean closed;

    public LinuxJoystickDevice(String filename) throws IOException {
        this.fd = nOpen(filename);
        try {
            this.name = getDeviceName();
            setBufferSize(AbstractController.EVENT_QUEUE_DEPTH);
            buttons = new LinuxJoystickButton[getNumDeviceButtons()];
            axes = new LinuxJoystickAxis[getNumDeviceAxes()];
            axisMap = getDeviceAxisMap();
            buttonMap = getDeviceButtonMap();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private static long nOpen(String filename) throws IOException {
        if (filename == null)
            return -1;
        int fd = LinuxIO.INSTANCE.open64(filename, O_RDONLY | O_NONBLOCK);
        if (fd == -1)
            throw new IOException(String.format( "Failed to open device %s (%d)", filename, Native.getLastError()));
        return fd;
    }

    public synchronized void setBufferSize(int size) {
        event_queue = new EventQueue(size);
    }

    private void processEvent(LinuxJoystickEvent joystick_event) {
        int index = joystick_event.getNumber();
        // Filter synthetic init event flag
        int type = joystick_event.getType() & ~JS_EVENT_INIT;
        switch (type) {
        case JS_EVENT_BUTTON:
            if (index < getNumButtons()) {
                LinuxJoystickButton button = buttons[index];
                if (button != null) {
                    float value = joystick_event.getValue();
                    button.setValue(value);
                    event.set(button, value, joystick_event.getNanos());
                    break;
                }
            }
            return;
        case JS_EVENT_AXIS:
            if (index < getNumAxes()) {
                LinuxJoystickAxis axis = axes[index];
                if (axis != null) {
                    float value = (float) joystick_event.getValue() / AXIS_MAX_VALUE;
                    axis.setValue(value);
                    if (povXs.containsKey(index)) {
                        LinuxJoystickPOV pov = povXs.get(index);
                        pov.updateValue();
                        event.set(pov, pov.getPollData(), joystick_event.getNanos());
                    } else if (povYs.containsKey(index)) {
                        LinuxJoystickPOV pov = povYs.get(index);
                        pov.updateValue();
                        event.set(pov, pov.getPollData(), joystick_event.getNanos());
                    } else {
                        event.set(axis, value, joystick_event.getNanos());
                    }
                    break;
                }
            }
            return;
        default:
            // Unknown component type
            return;
        }
        if (!event_queue.isFull()) {
            event_queue.add(event);
        }
    }

    public void registerAxis(int index, LinuxJoystickAxis axis) {
        axes[index] = axis;
    }

    public void registerButton(int index, LinuxJoystickButton button) {
        buttons[index] = button;
    }

    public void registerPOV(LinuxJoystickPOV pov) {
        // The x and y on a joystick device are not the same as on an event device
        LinuxJoystickAxis xAxis = pov.getYAxis();
        LinuxJoystickAxis yAxis = pov.getXAxis();
        int xIndex;
        int yIndex;
        for (xIndex = 0; xIndex < axes.length; xIndex++) {
            if (axes[xIndex] == xAxis) {
                break;
            }
        }
        for (yIndex = 0; yIndex < axes.length; yIndex++) {
            if (axes[yIndex] == yAxis) {
                break;
            }
        }
        povXs.put(xIndex, pov);
        povYs.put(yIndex, pov);
    }

    public final synchronized boolean getNextEvent(Event event) throws IOException {
        return event_queue.getNextEvent(event);
    }

    public synchronized void poll() throws IOException {
        checkClosed();
        while (getNextDeviceEvent(joystick_event)) {
            processEvent(joystick_event);
        }
    }

    private boolean getNextDeviceEvent(LinuxJoystickEvent joystick_event) throws IOException {
        return nGetNextEvent(fd, joystick_event);
    }

    private static boolean nGetNextEvent(long fd, LinuxJoystickEvent joystick_event) throws IOException {
        if (LinuxIO.INSTANCE.read((int) fd, joystick_event.getPointer(), new NativeLong(joystick_event.size())).intValue() == -1) {
            if (Native.getLastError() == EAGAIN)
                return false;
            throw new IOException(String.format( "Failed to read next device event (%d)", Native.getLastError()));
        }
        joystick_event.read();
        return true;
    }

    public final int getNumAxes() {
        return axes.length;
    }

    public final int getNumButtons() {
        return buttons.length;
    }

    public final byte[] getAxisMap() {
        return axisMap;
    }

    public final char[] getButtonMap() {
        return buttonMap;
    }

    private final int getNumDeviceButtons() throws IOException {
        return nGetNumButtons(fd);
    }

    private static int nGetNumButtons(long fd) throws IOException {
        ByteByReference num_buttons = new ByteByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGBUTTONS, num_buttons.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get number of buttons (%d)", Native.getLastError()));
        }
        return num_buttons.getValue();
    }

    private int getNumDeviceAxes() throws IOException {
        return nGetNumAxes(fd);
    }

    private static int nGetNumAxes(long fd) throws IOException {
        ByteByReference num_axes = new ByteByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGAXES, num_axes.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get number of buttons (%d)", Native.getLastError()));
        }
        return num_axes.getValue();
    }


    private final byte[] getDeviceAxisMap() throws IOException {
        return nGetAxisMap(fd);
    }

    private static byte[] nGetAxisMap(long fd) throws IOException {
        Memory axis_map = new Memory(ABS_MAX + 1);
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGAXMAP, axis_map) == -1) {
            throw new IOException(String.format( "Failed to get axis map (%d)", Native.getLastError()));
        }

        return axis_map.getByteArray(0, ABS_MAX + 1);
    }

    private char[] getDeviceButtonMap() throws IOException {
        return nGetButtonMap(fd);
    }

    private static char[] nGetButtonMap(long fd) throws IOException {
        Memory button_map = new Memory((KEY_MAX - BTN_MISC + 1) * Character.BYTES);
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGBTNMAP, button_map) == -1) {
            throw new IOException(String.format( "Failed to get button map (%d)", Native.getLastError()));
        }

        return button_map.getCharArray(0, KEY_MAX - BTN_MISC + 1);
    }

    private int getVersion() throws IOException {
        return nGetVersion(fd);
    }

    private static int nGetVersion(long fd) throws IOException {
        IntByReference version = new IntByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGVERSION, version.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get device version (%d)", Native.getLastError()));
        }
        return version.getValue();
    }


    public String getName() {
        return name;
    }

    private String getDeviceName() throws IOException {
        return nGetName(fd);
    }

    private static String nGetName(long fd) throws IOException {
        int BUFFER_SIZE = 1024;
        Memory device_name = new Memory(BUFFER_SIZE);

        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGNAME(BUFFER_SIZE), device_name) == -1) {
            throw new IOException(String.format( "Failed to get device name (%d)", Native.getLastError()));
        }
        return device_name.getString(0, StandardCharsets.UTF_8.name());
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            nClose(fd);
        }
    }

    private static void nClose(long fd) throws IOException {
        int result = LinuxIO.INSTANCE.close((int) fd);
        if (result == -1)
            throw new IOException(String.format( "Failed to close device (%d)", Native.getLastError()));
    }

    private void checkClosed() throws IOException {
        if (closed)
            throw new IOException("Device is closed");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws IOException {
        close();
    }
}
