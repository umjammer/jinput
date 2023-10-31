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

    private final LinuxJoystickEvent joystickEvent = new LinuxJoystickEvent();
    private final Event event = new Event();
    private final LinuxJoystickButton[] buttons;
    private final LinuxJoystickAxis[] axes;
    private final Map<Integer, LinuxJoystickPOV> povXs = new HashMap<>();
    private final Map<Integer, LinuxJoystickPOV> povYs = new HashMap<>();
    private final byte[] axisMap;
    private final char[] buttonMap;

    private EventQueue eventQueue;

    /**
     * Closed state variable that protects the validity of the file descriptor.
     * Access to the closed state must be synchronized
     */
    private boolean closed;

    public LinuxJoystickDevice(String filename) throws IOException {
        if (filename == null)
            this.fd = -1;
        else
            this.fd = LinuxIO.INSTANCE.open64(filename, O_RDONLY | O_NONBLOCK);
        if (this.fd == -1)
            throw new IOException(String.format("Failed to open device %s (%d)", filename, Native.getLastError()));
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

    public synchronized void setBufferSize(int size) {
        eventQueue = new EventQueue(size);
    }

    private void processEvent(LinuxJoystickEvent joystickEvent) {
        int index = joystickEvent.getNumber();
        // Filter synthetic init event flag
        int type = joystickEvent.getType() & ~JS_EVENT_INIT;
        switch (type) {
        case JS_EVENT_BUTTON:
            if (index < getNumButtons()) {
                LinuxJoystickButton button = buttons[index];
                if (button != null) {
                    float value = joystickEvent.getValue();
                    button.setValue(value);
                    event.set(button, value, joystickEvent.getNanos());
                    break;
                }
            }
            return;
        case JS_EVENT_AXIS:
            if (index < getNumAxes()) {
                LinuxJoystickAxis axis = axes[index];
                if (axis != null) {
                    float value = (float) joystickEvent.getValue() / AXIS_MAX_VALUE;
                    axis.setValue(value);
                    if (povXs.containsKey(index)) {
                        LinuxJoystickPOV pov = povXs.get(index);
                        pov.updateValue();
                        event.set(pov, pov.getPollData(), joystickEvent.getNanos());
                    } else if (povYs.containsKey(index)) {
                        LinuxJoystickPOV pov = povYs.get(index);
                        pov.updateValue();
                        event.set(pov, pov.getPollData(), joystickEvent.getNanos());
                    } else {
                        event.set(axis, value, joystickEvent.getNanos());
                    }
                    break;
                }
            }
            return;
        default:
            // Unknown component type
            return;
        }
        if (!eventQueue.isFull()) {
            eventQueue.add(event);
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

    public synchronized boolean getNextEvent(Event event) throws IOException {
        return eventQueue.getNextEvent(event);
    }

    public synchronized void poll() throws IOException {
        checkClosed();
        while (getNextDeviceEvent(joystickEvent)) {
            processEvent(joystickEvent);
        }
    }

    private boolean getNextDeviceEvent(LinuxJoystickEvent joystickEvent) throws IOException {
        if (LinuxIO.INSTANCE.read((int) fd, joystickEvent.getPointer(), new NativeLong(joystickEvent.size())).intValue() == -1) {
            if (Native.getLastError() == EAGAIN)
                return false;
            throw new IOException(String.format( "Failed to read next device event (%d)", Native.getLastError()));
        }
        joystickEvent.read();
        return true;
    }

    public int getNumAxes() {
        return axes.length;
    }

    public int getNumButtons() {
        return buttons.length;
    }

    public byte[] getAxisMap() {
        return axisMap;
    }

    public char[] getButtonMap() {
        return buttonMap;
    }

    private int getNumDeviceButtons() throws IOException {
        ByteByReference numButtons = new ByteByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGBUTTONS, numButtons.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get number of buttons (%d)", Native.getLastError()));
        }
        return numButtons.getValue();
    }

    private int getNumDeviceAxes() throws IOException {
        ByteByReference numAxes = new ByteByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGAXES, numAxes.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get number of buttons (%d)", Native.getLastError()));
        }
        return numAxes.getValue();
    }


    private byte[] getDeviceAxisMap() throws IOException {
        Memory axisMap = new Memory(ABS_MAX + 1);
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGAXMAP, axisMap) == -1) {
            throw new IOException(String.format( "Failed to get axis map (%d)", Native.getLastError()));
        }

        return axisMap.getByteArray(0, ABS_MAX + 1);
    }

    private char[] getDeviceButtonMap() throws IOException {
        Memory buttonMap = new Memory((KEY_MAX - BTN_MISC + 1) * Character.BYTES);
        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGBTNMAP, buttonMap) == -1) {
            throw new IOException(String.format( "Failed to get button map (%d)", Native.getLastError()));
        }

        return buttonMap.getCharArray(0, KEY_MAX - BTN_MISC + 1);
    }

    private int getVersion() throws IOException {
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
        int BUFFER_SIZE = 1024;
        Memory deviceName = new Memory(BUFFER_SIZE);

        if (LinuxIO.INSTANCE.ioctl((int) fd, JSIOCGNAME(BUFFER_SIZE), deviceName) == -1) {
            throw new IOException(String.format( "Failed to get device name (%d)", Native.getLastError()));
        }
        return deviceName.getString(0, StandardCharsets.UTF_8.name());
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            int result = LinuxIO.INSTANCE.close((int) fd);
            if (result == -1)
                throw new IOException(String.format( "Failed to close device (%d)", Native.getLastError()));
        }
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
