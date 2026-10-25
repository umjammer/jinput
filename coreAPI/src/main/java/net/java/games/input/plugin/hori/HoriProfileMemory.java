/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * The layout of a profile in the controller's memory, and moving a {@link HoriProfile} in and
 * out of it.
 * <p>
 * All of it is from <a href="https://github.com/mbenkmann/hori_device_manager">hori_device_manager</a>
 * ({@code hori.py}), found on the Xbox model:
 * <pre>
 * 0x000  32 bytes  name, utf-16le, 0 padded
 * 0x020            defaults written by "reset", see {@link #DEFAULTS}
 * 0x073 - 0x185    one 14 byte entry per button, the first 8 bytes say what it sends:
 *                  04 00 00 00 00 00 01 &lt;function code&gt; overrides it
 * 0x18a  18 bytes  stick as LS or RS
 * </pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public final class HoriProfileMemory {

    private HoriProfileMemory() {
    }

    /** bytes of one profile */
    public static final int SIZE = 421;

    public static final int NAME_OFFSET = 0;
    public static final int NAME_LENGTH = 32;

    public static final int DEFAULTS_OFFSET = 0x20;

    public static final int MAPPING_OFFSET = 0x73;
    public static final int MAPPING_END = 0x185;
    /** bytes of a button entry that say what the button sends */
    public static final int ENTRY_LENGTH = 8;

    public static final int STICK_OFFSET = 0x18a;
    public static final int STICK_LENGTH = 18;

    /** hori.py's PROFILE_DEFAULT, what "reset" writes from {@link #DEFAULTS_OFFSET} on */
    private static final byte[] DEFAULTS = bytes(
            0x00, 0x00, 0x01, 0x32, 0x32, 0x01, 0x32, 0x32, 0x01, 0x32, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x20, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x01,
            0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0d, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x13, 0x01,
            0x37, 0x37, 0x00, 0x00, 0x01, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x14, 0x01, 0x37, 0x37,
            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01
    );

    private static byte[] bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) b[i] = (byte) values[i];
        return b;
    }

    /** @return a whole profile as "reset" leaves it, with an empty name */
    public static byte[] defaults() {
        byte[] memory = new byte[SIZE];
        System.arraycopy(DEFAULTS, 0, memory, DEFAULTS_OFFSET, DEFAULTS.length);
        return memory;
    }

    /** @param memory {@link #SIZE} bytes of a profile */
    public static HoriProfile decode(byte[] memory) {
        if (memory.length < SIZE) throw new IllegalArgumentException("need " + SIZE + " bytes: " + memory.length);
        HoriProfile profile = new HoriProfile();

        StringBuilder name = new StringBuilder();
        for (int i = 0; i < NAME_LENGTH / 2; i++) {
            int c = (memory[NAME_OFFSET + 2 * i] & 0xff) | ((memory[NAME_OFFSET + 2 * i + 1] & 0xff) << 8);
            if (c == 0) break;
            name.append((char) c);
        }
        profile.setName(name.toString());

        for (HoriButton b : HoriButton.values()) {
            int o = b.offset;
            if (memory[o] == 4 && memory[o + 6] == 1) { // override active
                profile.setFunction(b, HoriFunction.valueOf(memory[o + 7] & 0xff));
            } else {
                profile.setFunction(b, b.defaultFunction());
            }
        }

        byte[] stick = Arrays.copyOfRange(memory, STICK_OFFSET, STICK_OFFSET + STICK_LENGTH);
        profile.setStick(Arrays.equals(stick, HoriStick.RS.bytes()) ? HoriStick.RS : HoriStick.LS);
        return profile;
    }

    /**
     * Writes the profile over the memory, bytes the profile does not cover are kept.
     * A button sending itself gets the default entry back, any other gets an override,
     * as {@code hori.py map} does.
     *
     * @param memory {@link #SIZE} bytes, changed in place
     */
    public static void encode(HoriProfile profile, byte[] memory) {
        if (memory.length < SIZE) throw new IllegalArgumentException("need " + SIZE + " bytes: " + memory.length);

        Arrays.fill(memory, NAME_OFFSET, NAME_OFFSET + NAME_LENGTH, (byte) 0);
        byte[] name = profile.getName().getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(name, 0, memory, NAME_OFFSET, name.length);

        for (HoriButton b : HoriButton.values()) {
            System.arraycopy(entry(b, profile.getFunction(b)), 0, memory, b.offset, ENTRY_LENGTH);
        }

        System.arraycopy(profile.getStick().bytes(), 0, memory, STICK_OFFSET, STICK_LENGTH);
    }

    /** the first {@link #ENTRY_LENGTH} bytes of a button's entry */
    static byte[] entry(HoriButton button, HoriFunction function) {
        if (function == button.defaultFunction()) {
            int o = button.offset - DEFAULTS_OFFSET;
            return Arrays.copyOfRange(DEFAULTS, o, o + ENTRY_LENGTH);
        } else {
            return bytes(4, 0, 0, 0, 0, 0, 1, function.code);
        }
    }

    /** reads a whole profile through the transport */
    public static HoriProfile read(HoriTransport transport, int profile) throws IOException {
        checkProfile(profile);
        return decode(transport.readMemory(profile, 0, SIZE));
    }

    /**
     * Writes only the parts that differ from what the controller has now: the name,
     * each button entry, the stick.
     *
     * @param current what the controller has now, {@link #SIZE} bytes, it is updated
     * @return the number of writes sent
     */
    public static int write(HoriTransport transport, int profile, HoriProfile p, byte[] current) throws IOException {
        checkProfile(profile);
        byte[] wanted = current.clone();
        encode(p, wanted);

        int writes = 0;
        writes += writeIfChanged(transport, profile, current, wanted, NAME_OFFSET, NAME_LENGTH);
        for (HoriButton b : HoriButton.values()) {
            writes += writeIfChanged(transport, profile, current, wanted, b.offset, ENTRY_LENGTH);
        }
        writes += writeIfChanged(transport, profile, current, wanted, STICK_OFFSET, STICK_LENGTH);
        return writes;
    }

    private static int writeIfChanged(HoriTransport transport, int profile, byte[] current, byte[] wanted, int offset, int length) throws IOException {
        if (Arrays.equals(current, offset, offset + length, wanted, offset, offset + length)) return 0;
        transport.writeMemory(profile, offset, Arrays.copyOfRange(wanted, offset, offset + length));
        System.arraycopy(wanted, offset, current, offset, length);
        return 1;
    }

    /** @throws IllegalArgumentException not 1..4 */
    public static void checkProfile(int profile) {
        if (profile < 1 || profile > 4) throw new IllegalArgumentException("profile must be between 1 and 4: " + profile);
    }
}
