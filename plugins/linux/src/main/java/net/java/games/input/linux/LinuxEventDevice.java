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

    private final Map<LinuxAxisDescriptor, LinuxComponent> component_map = new HashMap<>();
    private final Rumbler[] rumblers;
    private final long fd;
    private final String name;
    private final LinuxInputID input_id;
    private final List<LinuxEventComponent> components;
    private final Controller.Type type;

    /**
     * Closed state variable that protects the validity of the file descriptor.
     * Access to the closed state must be synchronized
     */
    private boolean closed;

    /**
     * Access to the key_states array could be synchronized, but
     * it doesn't hurt to have multiple threads read/write from/to it
     */
    private final byte[] key_states = new byte[NativeDefinitions.KEY_MAX / 8 + 1];

    public LinuxEventDevice(String filename) throws IOException {
        long fd;
        boolean detect_rumblers = true;
        try {
            fd = nOpen(filename, true);
        } catch (IOException e) {
            fd = nOpen(filename, false);
            detect_rumblers = false;
        }
        this.fd = fd;
        try {
            this.name = getDeviceName();
            this.input_id = getDeviceInputID();
            this.components = getDeviceComponents();
            if (detect_rumblers)
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

    private static int countComponents(List<LinuxEventComponent> components, Class<?> id_type, boolean relative) {
        int count = 0;
        for (LinuxEventComponent component : components) {
            if (id_type.isInstance(component.getIdentifier()) && relative == component.isRelative())
                count++;
        }
        return count;
    }

    private final Controller.Type guessType() throws IOException {
        List<LinuxEventComponent> components = getComponents();
        if (components.isEmpty())
            return Controller.Type.UNKNOWN;
        int num_rel_axes = countComponents(components, Component.Identifier.Axis.class, true);
        int num_abs_axes = countComponents(components, Component.Identifier.Axis.class, false);
        int num_keys = countComponents(components, Component.Identifier.Key.class, false);
        int mouse_traits = 0;
        int keyboard_traits = 0;
        int joystick_traits = 0;
        int gamepad_traits = 0;
        if (name.toLowerCase().contains("mouse"))
            mouse_traits++;
        if (name.toLowerCase().contains("keyboard"))
            keyboard_traits++;
        if (name.toLowerCase().contains("joystick"))
            joystick_traits++;
        if (name.toLowerCase().contains("gamepad"))
            gamepad_traits++;
        int num_keyboard_button_traits = 0;
        int num_mouse_button_traits = 0;
        int num_joystick_button_traits = 0;
        int num_gamepad_button_traits = 0;
        // count button traits
        for (LinuxEventComponent component : components) {
            if (component.getButtonTrait() == Controller.Type.MOUSE)
                num_mouse_button_traits++;
            else if (component.getButtonTrait() == Controller.Type.KEYBOARD)
                num_keyboard_button_traits++;
            else if (component.getButtonTrait() == Controller.Type.GAMEPAD)
                num_gamepad_button_traits++;
            else if (component.getButtonTrait() == Controller.Type.STICK)
                num_joystick_button_traits++;
        }
        if ((num_mouse_button_traits >= num_keyboard_button_traits) && (num_mouse_button_traits >= num_joystick_button_traits) && (num_mouse_button_traits >= num_gamepad_button_traits)) {
            mouse_traits++;
        } else if ((num_keyboard_button_traits >= num_mouse_button_traits) && (num_keyboard_button_traits >= num_joystick_button_traits) && (num_keyboard_button_traits >= num_gamepad_button_traits)) {
            keyboard_traits++;
        } else if ((num_joystick_button_traits >= num_keyboard_button_traits) && (num_joystick_button_traits >= num_mouse_button_traits) && (num_joystick_button_traits >= num_gamepad_button_traits)) {
            joystick_traits++;
        } else if ((num_gamepad_button_traits >= num_keyboard_button_traits) && (num_gamepad_button_traits >= num_mouse_button_traits) && (num_gamepad_button_traits >= num_joystick_button_traits)) {
            gamepad_traits++;
        }
        if (num_rel_axes >= 2) {
            mouse_traits++;
        }
        if (num_abs_axes >= 2) {
            joystick_traits++;
            gamepad_traits++;
        }

        if ((mouse_traits >= keyboard_traits) && (mouse_traits >= joystick_traits) && (mouse_traits >= gamepad_traits)) {
            return Controller.Type.MOUSE;
        } else if ((keyboard_traits >= mouse_traits) && (keyboard_traits >= joystick_traits) && (keyboard_traits >= gamepad_traits)) {
            return Controller.Type.KEYBOARD;
        } else if ((joystick_traits >= mouse_traits) && (joystick_traits >= keyboard_traits) && (joystick_traits >= gamepad_traits)) {
            return Controller.Type.STICK;
        } else if ((gamepad_traits >= mouse_traits) && (gamepad_traits >= keyboard_traits) && (gamepad_traits >= joystick_traits)) {
            return Controller.Type.GAMEPAD;
        } else
            return null;
    }

    private final Rumbler[] enumerateRumblers() {
        List<Rumbler> rumblers = new ArrayList<>();
        try {
            int num_effects = getNumEffects();
            if (num_effects <= 0)
                return rumblers.toArray(new Rumbler[] {});
            byte[] ff_bits = getForceFeedbackBits();
            if (isBitSet(ff_bits, NativeDefinitions.FF_RUMBLE) && num_effects > rumblers.size()) {
                rumblers.add(new LinuxRumbleFF(this));
            }
        } catch (IOException e) {
            log.fine("Failed to enumerate rumblers: " + e.getMessage());
        }
        return rumblers.toArray(new Rumbler[] {});
    }

    public final Rumbler[] getRumblers() {
        return rumblers;
    }

    public final synchronized int uploadRumbleEffect(int id, int trigger_button, int direction, int trigger_interval, int replay_length, int replay_delay, int strong_magnitude, int weak_magnitude) throws IOException {
        checkClosed();
        return nUploadRumbleEffect(fd, id, direction, trigger_button, trigger_interval, replay_length, replay_delay, strong_magnitude, weak_magnitude);
    }

    private static int nUploadRumbleEffect(long fd, int id, int direction, int trigger_button, int trigger_interval, int replay_length, int replay_delay, int strong_magnitude, int weak_magnitude) throws IOException {
        ffeffect effect = new ffeffect();

        effect.type = FF_RUMBLE;
        effect.id = (short) id;
        effect.trigger.button = (short) trigger_button;
        effect.trigger.interval = (short) trigger_interval;
        effect.replay.length = (short) replay_length;
        effect.replay.delay = (short) replay_delay;
        effect.direction = (short) direction;
        effect.u.rumble.strong_magnitude = (short) strong_magnitude;
        effect.u.rumble.weak_magnitude = (short) weak_magnitude;

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCSFF(effect.size()), effect.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to upload effect (%d)", Native.getLastError()));
        }
        effect.read();
        return effect.id;
    }

    public final synchronized int uploadConstantEffect(int id, int trigger_button, int direction, int trigger_interval, int replay_length, int replay_delay, int constant_level, int constant_env_attack_length, int constant_env_attack_level, int constant_env_fade_length, int constant_env_fade_level) throws IOException {
        checkClosed();
        return nUploadConstantEffect(fd, id, direction, trigger_button, trigger_interval, replay_length, replay_delay, constant_level, constant_env_attack_length, constant_env_attack_level, constant_env_fade_length, constant_env_fade_level);
    }

    private static int nUploadConstantEffect(long fd, int id, int direction, int trigger_button, int trigger_interval, int replay_length, int replay_delay, int constant_level, int constant_env_attack_length, int constant_env_attack_level, int constant_env_fade_length, int constant_env_fade_level) throws IOException {
        ffeffect effect = new ffeffect();

        effect.type = FF_CONSTANT;
        effect.id = (short) id;
        effect.trigger.button = (short) trigger_button;
        effect.trigger.interval = (short) trigger_interval;
        effect.replay.length = (short) replay_length;
        effect.replay.delay = (short) replay_delay;
        effect.direction = (short) direction;
        effect.u.constant.level = (short) constant_level;
        effect.u.constant.envelope.attack_length = (short) constant_env_attack_length;
        effect.u.constant.envelope.attack_level = (short) constant_env_attack_level;
        effect.u.constant.envelope.fade_length = (short) constant_env_fade_length;
        effect.u.constant.envelope.fade_level = (short) constant_env_fade_level;

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCSFF(effect.size()), effect.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to upload effect (%d)", Native.getLastError()));
        }
        return effect.id;
    }

    void eraseEffect(int id) throws IOException {
        nEraseEffect(fd, id);
    }

    private static void nEraseEffect(long fd, int ff_id) throws IOException {
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCRMFF, ff_id) == -1)
            throw new IOException(String.format( "Failed to erase effect (%d)", Native.getLastError()));
    }


    public final synchronized void writeEvent(int type, int code, int value) throws IOException {
        checkClosed();
        nWriteEvent(fd, type, code, value);
    }

    private static  void nWriteEvent(long fd, int type, int code, int value) throws IOException {
        LinuxEvent event = new LinuxEvent();
        event.type = type;
        event.code = code;
        event.value = value;

        if (LinuxIO.INSTANCE.write((int) fd, event.getPointer(), new NativeLong(event.size())).intValue() == -1) {
            throw new IOException(String.format( "Failed to write to device (%d)", Native.getLastError()));
        }
    }

    public final void registerComponent(LinuxAxisDescriptor desc, LinuxComponent component) {
        component_map.put(desc, component);
    }

    public final LinuxComponent mapDescriptor(LinuxAxisDescriptor desc) {
        return component_map.get(desc);
    }

    public final Controller.PortType getPortType() throws IOException {
        return input_id.getPortType();
    }

    public final LinuxInputID getInputID() {
        return input_id;
    }

    private final LinuxInputID getDeviceInputID() throws IOException {
        return nGetInputID(fd);
    }

    private static LinuxInputID nGetInputID(long fd) throws IOException {
        LinuxInputID id = new LinuxInputID();
        int result = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGID(id.size()), id.getPointer());
        if (result == -1) {
            throw new IOException(String.format( "Failed to get input id for device (%d)", Native.getLastError()));
        }
        id.read();
        return id;
    }

    public final int getNumEffects() throws IOException {
        return nGetNumEffects(fd);
    }

    private static int nGetNumEffects(long fd) throws IOException {
        IntByReference num_effects = new IntByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGEFFECTS, num_effects.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get number of device effects (%d)", Native.getLastError()));
        }
        return num_effects.getValue();
    }


    private final int getVersion() throws IOException {
        return nGetVersion(fd);
    }

    private static int nGetVersion(long fd) throws IOException {
        IntByReference version = new IntByReference();
        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGVERSION, version.getPointer()) == -1) {
            throw new IOException(String.format( "Failed to get device version (%d)", Native.getLastError()));
        }
        return version.getValue();
    }


    public final synchronized boolean getNextEvent(LinuxEvent linux_event) throws IOException {
        checkClosed();
        return nGetNextEvent(fd, linux_event);
    }

    private static boolean nGetNextEvent(long fd, LinuxEvent linux_event) throws IOException {
        if (LinuxIO.INSTANCE.read((int) fd, linux_event.getPointer(), new NativeLong(linux_event.size())).intValue() == -1) {
            if (Native.getLastError() == EAGAIN)
                return false;
            throw new IOException(String.format( "Failed to read next device event (%d)", Native.getLastError()));
        }
        return true;
    }

    public synchronized void getAbsInfo(int abs_axis, LinuxAbsInfo abs_info) throws IOException {
        checkClosed();
        nGetAbsInfo(fd, abs_axis, abs_info);
    }

    private static void nGetAbsInfo(long fd, int abs_axis, LinuxAbsInfo abs_info) throws IOException {
        int result = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGABS(abs_axis, abs_info.size()), abs_info.getPointer());
        if (result == -1) {
            throw new IOException(String.format( "Failed to get abs info for axis (%d)", Native.getLastError()));
        }
    }

    private final void addKeys(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getKeysBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getButtonID(i);
                components.add(new LinuxEventComponent(this, id, false, NativeDefinitions.EV_KEY, i));
            }
        }
    }

    private final void addAbsoluteAxes(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getAbsoluteAxesBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getAbsAxisID(i);
                components.add(new LinuxEventComponent(this, id, false, NativeDefinitions.EV_ABS, i));
            }
        }
    }

    private final void addRelativeAxes(List<LinuxEventComponent> components) throws IOException {
        byte[] bits = getRelativeAxesBits();
        for (int i = 0; i < bits.length * 8; i++) {
            if (isBitSet(bits, i)) {
                Component.Identifier id = LinuxNativeTypesMap.getRelAxisID(i);
                components.add(new LinuxEventComponent(this, id, true, NativeDefinitions.EV_REL, i));
            }
        }
    }

    public final List<LinuxEventComponent> getComponents() {
        return components;
    }

    private final List<LinuxEventComponent> getDeviceComponents() throws IOException {
        List<LinuxEventComponent> components = new ArrayList<>();
        byte[] evtype_bits = getEventTypeBits();
        if (isBitSet(evtype_bits, NativeDefinitions.EV_KEY))
            addKeys(components);
        if (isBitSet(evtype_bits, NativeDefinitions.EV_ABS))
            addAbsoluteAxes(components);
        if (isBitSet(evtype_bits, NativeDefinitions.EV_REL))
            addRelativeAxes(components);
        return components;
    }

    private final byte[] getForceFeedbackBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.FF_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_FF, bits);
        return bits;
    }

    private final byte[] getKeysBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.KEY_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_KEY, bits);
        return bits;
    }

    private final byte[] getAbsoluteAxesBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.ABS_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_ABS, bits);
        return bits;
    }

    private final byte[] getRelativeAxesBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.REL_MAX / 8 + 1];
        nGetBits(fd, NativeDefinitions.EV_REL, bits);
        return bits;
    }

    private final byte[] getEventTypeBits() throws IOException {
        byte[] bits = new byte[NativeDefinitions.EV_MAX / 8 + 1];
        nGetBits(fd, 0, bits);
        return bits;
    }

    private static void nGetBits(long fd, int ev_type, byte[] evtype_bits) throws IOException {
        int len = evtype_bits.length;
        Memory bits = new Memory(len);
        int res = LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGBIT(ev_type, len), bits);
        if (res == -1)
            throw new IOException(String.format( "Failed to get device bits (%d)", Native.getLastError()));
        bits.read(0, evtype_bits, 0, len);
    }

    public synchronized void pollKeyStates() throws IOException {
        nGetKeyStates(fd, key_states);
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
        return isBitSet(key_states, bit);
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
        Memory device_name = new Memory(BUFFER_SIZE);

        if (LinuxIO.INSTANCE.ioctl((int) fd, EVIOCGNAME(BUFFER_SIZE), device_name) == -1) {
            throw new IOException(String.format( "Failed to get device name (%d)", Native.getLastError()));
        }
        return device_name.getString(0, StandardCharsets.UTF_8.name());
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


    private final void checkClosed() throws IOException {
        if (closed)
            throw new IOException("Device is closed");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws IOException {
        close();
    }
}
