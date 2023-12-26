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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Rumbler;

import static com.sun.jna.platform.linux.ErrNo.EAGAIN;
import static net.java.games.input.linux.LinuxIO.EVIOCGABS;
import static net.java.games.input.linux.LinuxIO.EVIOCGBIT;
import static net.java.games.input.linux.LinuxIO.EVIOCGEFFECTS;
import static net.java.games.input.linux.LinuxIO.EVIOCGID;
import static net.java.games.input.linux.LinuxIO.EVIOCGKEY;
import static net.java.games.input.linux.LinuxIO.EVIOCGNAME;
import static net.java.games.input.linux.LinuxIO.EVIOCGVERSION;
import static net.java.games.input.linux.LinuxIO.EVIOCRMFF;
import static net.java.games.input.linux.LinuxIO.EVIOCSFF;
import static net.java.games.input.linux.LinuxIO.O_NONBLOCK;
import static net.java.games.input.linux.LinuxIO.O_RDONLY;
import static net.java.games.input.linux.LinuxIO.O_RDWR;
import static net.java.games.input.linux.ffeffect.FF_CONSTANT;
import static net.java.games.input.linux.ffeffect.FF_RUMBLE;


/**
 * @author elias
 */
final class LinuxEventDevice implements LinuxDevice {

    private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());

    private final Map<LinuxAxisDescriptor, LinuxComponent> componentMap = new HashMap<>();
    private final Rumbler[] rumblers;
    private final long fd;
    private final String name;
    private final LinuxInputID inputId;
    private final List<LinuxEventComponent> components;
    private final Controller.Type type;

    final LinuxEvent linuxEvent = new LinuxEvent();

    /**
     * Closed state variable that protects the validity of the file descriptor.
     * Access to the closed state must be synchronized
     */
    private boolean closed;

    /**
     * Access to the keyStates array could be synchronized, but
     * it doesn't hurt to have multiple threads read/write from/to it
     */
    private final byte[] keyStates = new byte[NativeDefinitions.KEY_MAX / 8 + 1];

    public LinuxEventDevice(String filename) throws IOException {
        long fd;
        boolean detectRumblers = true;
        try {
            fd = nOpen(filename, true);
        } catch (IOException e) {
            fd = nOpen(filename, false);
            detectRumblers = false;
        }
        this.fd = fd;
        try {
            this.name = getDeviceName();
            this.inputId = getDeviceInputID();
            this.components = getDeviceComponents();
            if (detectRumblers)
                this.rumblers = enumerateRumblers();
            else
                this.rumblers = new Rumbler[] {};
            this.type = guessType();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private static long nOpen(String filename, boolean rw) throws IOException {
        if (filename == null)
            return -1;
        int flags = rw ? O_RDWR : O_RDONLY;
        flags = flags | O_NONBLOCK;
        int fd = LinuxIO.INSTANCE.open64(filename, flags);
        if (fd == -1)
            throw new IOException(String.format( "Failed to open device %s (%d)", filename, Native.getLastError()));
        return fd;
    }

    public Controller.Type getType() {
        return type;
    }

    private static int countComponents(List<LinuxEventComponent> components, Class<?> idType, boolean relative) {
        int count = 0;
        for (LinuxEventComponent component : components) {
            if (idType.isInstance(component.getIdentifier()) && relative == component.isRelative())
                count++;
        }
        return count;
    }

    private Controller.Type guessType() throws IOException {
        List<LinuxEventComponent> components = getComponents();
        if (components.isEmpty())
            return Controller.Type.UNKNOWN;
        int numRelAxes = countComponents(components, Component.Identifier.Axis.class, true);
        int numAbsAxes = countComponents(components, Component.Identifier.Axis.class, false);
        int numKeys = countComponents(components, Component.Identifier.Key.class, false);
        int mouseTraits = 0;
        int keyboardTraits = 0;
        int joystickTraits = 0;
        int gamepadTraits = 0;
        if (name.toLowerCase().contains("mouse"))
            mouseTraits++;
        if (name.toLowerCase().contains("keyboard"))
            keyboardTraits++;
        if (name.toLowerCase().contains("joystick"))
            joystickTraits++;
        if (name.toLowerCase().contains("gamepad"))
            gamepadTraits++;
        int numKeyboardButtonTraits = 0;
        int numMouseButtonTraits = 0;
        int numJoystickButtonTraits = 0;
        int numGamepadButtonTraits = 0;
        // count button traits
        for (LinuxEventComponent component : components) {
            if (component.getButtonTrait() == Controller.Type.MOUSE)
                numMouseButtonTraits++;
            else if (component.getButtonTrait() == Controller.Type.KEYBOARD)
                numKeyboardButtonTraits++;
            else if (component.getButtonTrait() == Controller.Type.GAMEPAD)
                numGamepadButtonTraits++;
            else if (component.getButtonTrait() == Controller.Type.STICK)
                numJoystickButtonTraits++;
        }
        if ((numMouseButtonTraits >= numKeyboardButtonTraits) && (numMouseButtonTraits >= numJoystickButtonTraits) && (numMouseButtonTraits >= numGamepadButtonTraits)) {
            mouseTraits++;
        } else if ((numKeyboardButtonTraits >= numMouseButtonTraits) && (numKeyboardButtonTraits >= numJoystickButtonTraits) && (numKeyboardButtonTraits >= numGamepadButtonTraits)) {
            keyboardTraits++;
        } else if ((numJoystickButtonTraits >= numKeyboardButtonTraits) && (numJoystickButtonTraits >= numMouseButtonTraits) && (numJoystickButtonTraits >= numGamepadButtonTraits)) {
            joystickTraits++;
        } else if ((numGamepadButtonTraits >= numKeyboardButtonTraits) && (numGamepadButtonTraits >= numMouseButtonTraits) && (numGamepadButtonTraits >= numJoystickButtonTraits)) {
            gamepadTraits++;
        }
        if (numRelAxes >= 2) {
            mouseTraits++;
        }
        if (numAbsAxes >= 2) {
            joystickTraits++;
            gamepadTraits++;
        }

        if ((mouseTraits >= keyboardTraits) && (mouseTraits >= joystickTraits) && (mouseTraits >= gamepadTraits)) {
            return Controller.Type.MOUSE;
        } else if ((keyboardTraits >= mouseTraits) && (keyboardTraits >= joystickTraits) && (keyboardTraits >= gamepadTraits)) {
            return Controller.Type.KEYBOARD;
        } else if ((joystickTraits >= mouseTraits) && (joystickTraits >= keyboardTraits) && (joystickTraits >= gamepadTraits)) {
            return Controller.Type.STICK;
        } else if ((gamepadTraits >= mouseTraits) && (gamepadTraits >= keyboardTraits) && (gamepadTraits >= joystickTraits)) {
            return Controller.Type.GAMEPAD;
        } else
            return null;
    }

    private Rumbler[] enumerateRumblers() {
        List<Rumbler> rumblers = new ArrayList<>();
        try {
            int numEffects = getNumEffects();
            if (numEffects <= 0)
                return rumblers.toArray(Rumbler[]::new);
            byte[] ffBits = getForceFeedbackBits();
            if (isBitSet(ffBits, NativeDefinitions.FF_RUMBLE) && numEffects > rumblers.size()) {
                rumblers.add(new LinuxRumbleFF(this));
            }
        } catch (IOException e) {
            log.fine("Failed to enumerate rumblers: " + e.getMessage());
        }
        return rumblers.toArray(new Rumbler[] {});
    }

    public Rumbler[] getRumblers() {
        return rumblers;
    }

    public synchronized int uploadRumbleEffect(int id, int triggerButton, int direction, int triggerInterval, int replayLength, int replayDelay, int strongMagnitude, int weakMagnitude) throws IOException {
        checkClosed();
        return nUploadRumbleEffect(fd, id, direction, triggerButton, triggerInterval, replayLength, replayDelay, strongMagnitude, weakMagnitude);
    }

    private static int nUploadRumbleEffect(long fd, int id, int direction, int triggerButton, int triggerInterval, int replayLength, int replayDelay, int strongMagnitude, int weakMagnitude) throws IOException {
        ffeffect effect = new ffeffect();

        effect.type = FF_RUMBLE;
        effect.id = (short) id;
        effect.trigger.button = (short) triggerButton;
        effect.trigger.interval = (short) triggerInterval;
        effect.replay.length = (short) replayLength;
        effect.replay.delay = (short) replayDelay;
        effect.direction = (short) direction;
        effect.u.rumble.strongMagnitude = (short) strongMagnitude;
        effect.u.rumble.weakMagnitude = (short) weakMagnitude;

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCSFF(effect.size()), effect.getPointer()) == -1) {
            throw new IOException(String.format("Failed to upload effect (%d)", Native.getLastError()));
        }
        effect.read();
        return effect.id;
    }

    public synchronized int uploadConstantEffect(int id, int triggerButton, int direction, int triggerInterval, int replayLength, int replayDelay, int constantLevel, int constantEnvAttackLength, int constantEnvAttackLevel, int constantEnvFadeLength, int constantEnvFadeLevel) throws IOException {
        checkClosed();
        return nUploadConstantEffect(fd, id, direction, triggerButton, triggerInterval, replayLength, replayDelay, constantLevel, constantEnvAttackLength, constantEnvAttackLevel, constantEnvFadeLength, constantEnvFadeLevel);
    }

    private static int nUploadConstantEffect(long fd, int id, int direction, int triggerButton, int triggerInterval, int replayLength, int replayDelay, int constantLevel, int constantEnvAttackLength, int constantEnvAttackLevel, int constantEnvFadeLength, int constantEnvFadeLevel) throws IOException {
        ffeffect effect = new ffeffect();

        effect.type = FF_CONSTANT;
        effect.id = (short) id;
        effect.trigger.button = (short) triggerButton;
        effect.trigger.interval = (short) triggerInterval;
        effect.replay.length = (short) replayLength;
        effect.replay.delay = (short) replayDelay;
        effect.direction = (short) direction;
        effect.u.constant.level = (short) constantLevel;
        effect.u.constant.envelope.attackLength = (short) constantEnvAttackLength;
        effect.u.constant.envelope.attackLevel = (short) constantEnvAttackLevel;
        effect.u.constant.envelope.fadeLength = (short) constantEnvFadeLength;
        effect.u.constant.envelope.fadeLevel = (short) constantEnvFadeLevel;

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCSFF(effect.size()), effect.getPointer()) == -1) {
            throw new IOException(String.format("Failed to upload effect (%d)", Native.getLastError()));
        }
        return effect.id;
    }

    void eraseEffect(int id) throws IOException {
        nEraseEffect(fd, id);
    }

    private static void nEraseEffect(long fd, int ffId) throws IOException {
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCRMFF, ffId) == -1)
            throw new IOException(String.format("Failed to erase effect (%d)", Native.getLastError()));
    }

    public synchronized void writeEvent(int type, int code, int value) throws IOException {
        checkClosed();
        nWriteEvent(fd, type, code, value);
    }

    private static  void nWriteEvent(long fd, int type, int code, int value) throws IOException {
        LinuxEvent event = new LinuxEvent();
        event.type = type;
        event.code = code;
        event.value = value;

        if (LinuxIO.INSTANCE.write((int) fd, event.getPointer(), new NativeLong(event.size())).intValue() == -1) {
            throw new IOException(String.format("Failed to write to device (%d)", Native.getLastError()));
        }
    }

    public void registerComponent(LinuxAxisDescriptor desc, LinuxComponent component) {
        componentMap.put(desc, component);
    }

    public LinuxComponent mapDescriptor(LinuxAxisDescriptor desc) {
        return componentMap.get(desc);
    }

    public Controller.PortType getPortType() throws IOException {
        return inputId.getPortType();
    }

    public LinuxInputID getInputID() {
        return inputId;
    }

    private LinuxInputID getDeviceInputID() throws IOException {
        return nGetInputID(fd);
    }

    private static LinuxInputID nGetInputID(long fd) throws IOException {
        LinuxInputID id = new LinuxInputID();
        int result = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGID(id.size()), id.getPointer());
        if (result == -1) {
            throw new IOException(String.format("Failed to get input id for device (%d)", Native.getLastError()));
        }
        id.read();
        return id;
    }

    public int getNumEffects() throws IOException {
        return nGetNumEffects(fd);
    }

    private static int nGetNumEffects(long fd) throws IOException {
        IntByReference numEffects = new IntByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGEFFECTS, numEffects.getPointer()) == -1) {
            throw new IOException(String.format("Failed to get number of device effects (%d)", Native.getLastError()));
        }
        return numEffects.getValue();
    }

    private int getVersion() throws IOException {
        return nGetVersion(fd);
    }

    private static int nGetVersion(long fd) throws IOException {
        IntByReference version = new IntByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGVERSION, version.getPointer()) == -1) {
            throw new IOException(String.format("Failed to get device version (%d)", Native.getLastError()));
        }
        return version.getValue();
    }

    public synchronized boolean getNextEvent(LinuxEvent linuxEvent) throws IOException {
        checkClosed();
        return nGetNextEvent(fd, linuxEvent);
    }

    private static boolean nGetNextEvent(long fd, LinuxEvent linuxEvent) throws IOException {
        if (LinuxIO.INSTANCE.read((int) fd, linuxEvent.getPointer(), new NativeLong(linuxEvent.size())).intValue() == -1) {
            if (Native.getLastError() == EAGAIN)
                return false;
            throw new IOException(String.format("Failed to read next device event (%d)", Native.getLastError()));
        }
        return true;
    }

    public synchronized void getAbsInfo(int absAxis, LinuxAbsInfo absInfo) throws IOException {
        checkClosed();
        nGetAbsInfo(fd, absAxis, absInfo);
    }

    private static void nGetAbsInfo(long fd, int absAxis, LinuxAbsInfo absInfo) throws IOException {
        int result = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGABS(absAxis, absInfo.size()), absInfo.getPointer());
        if (result == -1) {
            throw new IOException(String.format("Failed to get abs info for axis (%d)", Native.getLastError()));
        }
    }

    private void addKeys(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getKeysBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getButtonID(i);
                components.add(new LinuxEventComponent(this, id, false, NativeDefinitions.EV_KEY, i));
            }
        }
    }

    private void addAbsoluteAxes(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getAbsoluteAxesBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getAbsAxisID(i);
                components.add(new LinuxEventComponent(this, id, false, NativeDefinitions.EV_ABS, i));
            }
        }
    }

    private void addRelativeAxes(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getRelativeAxesBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getRelAxisID(i);
                components.add(new LinuxEventComponent(this, id, true, NativeDefinitions.EV_REL, i));
            }
        }
    }

    public List<LinuxEventComponent> getComponents() {
        return components;
    }

    private List<LinuxEventComponent> getDeviceComponents() throws IOException {
        List<LinuxEventComponent> components = new ArrayList<>();
        byte[] eventTypeBits = getEventTypeBits();
        if (isBitSet(eventTypeBits, NativeDefinitions.EV_KEY))
            addKeys(components);
        if (isBitSet(eventTypeBits, NativeDefinitions.EV_ABS))
            addAbsoluteAxes(components);
        if (isBitSet(eventTypeBits, NativeDefinitions.EV_REL))
            addRelativeAxes(components);
        return components;
    }

    private byte[] getForceFeedbackBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.FF_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_FF, bits);
        return bits;
    }

    private byte[] getKeysBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.KEY_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_KEY, bits);
        return bits;
    }

    private byte[] getAbsoluteAxesBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.ABS_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_ABS, bits);
        return bits;
    }

    private byte[] getRelativeAxesBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.REL_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_REL, bits);
        return bits;
    }

    private byte[] getEventTypeBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.EV_MAX / 8 + 1];
        nGetBits(fd, 0, bits);
        return bits;
    }

    private static void nGetBits(long fd, int evType, byte[] evTypeBits) throws IOException {
        int len = evTypeBits.length;
        Memory bits = new Memory(len);
        int res = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGBIT(evType, len), bits);
        if (res == -1)
            throw new IOException(String.format( "Failed to get device bits (%d)", Native.getLastError()));
        bits.read(0, evTypeBits, 0, len);
    }

    public synchronized void pollKeyStates() throws IOException {
        nGetKeyStates(fd, keyStates);
    }

    private static void nGetKeyStates(long fd, byte[] states) throws IOException {
        int len = states.length;
        Memory bits = new Memory(len);
        int res = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGKEY(len), bits);
        if (res == -1)
            throw new IOException(String.format( "Failed to get device key states (%d)", Native.getLastError()));
        bits.read(0, states, 0, len);
    }

    public boolean isKeySet(int bit) {
        return isBitSet(keyStates, bit);
    }

    public static boolean isBitSet(byte[] bits, int bit) {
        return (bits[bit / 8] & (1 << (bit % 8))) != 0;
    }

    public String getName() {
        return name;
    }

    private String getDeviceName() throws IOException {
        return nGetName(fd);
    }

    private static String nGetName(long fd) throws IOException {
        int BUFFER_SIZE = 1024;
        Memory deviceName = new Memory(BUFFER_SIZE);

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGNAME(BUFFER_SIZE), deviceName) == -1) {
            throw new IOException(String.format( "Failed to get device name (%d)", Native.getLastError()));
        }
        return deviceName.getString(0, StandardCharsets.UTF_8.name());
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed)
            return;
        closed = true;
        LinuxEnvironmentPlugin.execute(new LinuxDeviceTask() {
            @Override protected Object execute() throws IOException {
                nClose(fd);
                return null;
            }
        });
    }

    private void nClose(long fd) throws IOException {
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
