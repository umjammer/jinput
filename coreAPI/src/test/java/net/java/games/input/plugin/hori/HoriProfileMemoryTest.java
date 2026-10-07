/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * HoriProfileMemoryTest. Expected bytes are what hori.py writes.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
class HoriProfileMemoryTest {

    /** keeps a profile's memory, records the writes */
    static class FakeTransport implements HoriTransport {
        final byte[][] memory = new byte[5][];
        final List<int[]> writes = new ArrayList<>();
        int active = 1;

        FakeTransport() {
            for (int i = 1; i <= 4; i++) memory[i] = HoriProfileMemory.defaults();
        }

        @Override public byte[] readMemory(int profile, int offset, int length) {
            return Arrays.copyOfRange(memory[profile], offset, offset + length);
        }

        @Override public void writeMemory(int profile, int offset, byte[] data) {
            writes.add(new int[] {profile, offset, data.length});
            System.arraycopy(data, 0, memory[profile], offset, data.length);
        }

        @Override public int getActiveProfile() {
            return active;
        }

        @Override public void activateProfile(int profile) {
            active = profile;
        }
    }

    @Test
    @DisplayName("a reset profile reads as every button sending itself, stick LS")
    void test1() {
        HoriProfile p = HoriProfileMemory.decode(HoriProfileMemory.defaults());
        assertEquals("", p.getName());
        assertEquals(HoriStick.LS, p.getStick());
        for (HoriButton b : HoriButton.values()) {
            assertEquals(b.defaultFunction(), p.getFunction(b), b.name());
            assertFalse(p.isRemapped(b));
        }
        // LT and RT are stored as overrides to themselves in the defaults
        byte[] m = HoriProfileMemory.defaults();
        assertEquals(4, m[HoriButton.LT.offset]);
        assertEquals(0x0d, m[HoriButton.LT.offset + 7]);
    }

    @Test
    @DisplayName("the bytes are those of hori.py map, name and stick")
    void test2() {
        HoriProfile p = new HoriProfile();
        p.setName("Tekken");
        p.setFunction(HoriButton.A, HoriFunction.B);
        p.setFunction(HoriButton.SHARE, HoriFunction.DISABLED);
        p.setStick(HoriStick.RS);

        byte[] m = HoriProfileMemory.defaults();
        HoriProfileMemory.encode(p, m);

        // hori.py map: CMD_WRITEMEM, profile, ofs >> 8, ofs & 255, 8, 4, 0, 0, 0, 0, 0, 1, code
        assertArrayEquals(new byte[] {4, 0, 0, 0, 0, 0, 1, 0x10}, Arrays.copyOfRange(m, 0xff, 0xff + 8));
        assertArrayEquals(new byte[] {4, 0, 0, 0, 0, 0, 1, 0x21}, Arrays.copyOfRange(m, 0x17d, 0x17d + 8));
        // hori.py name: utf-16le, 0 padded to 32
        assertEquals('T', m[0]);
        assertEquals(0, m[1]);
        assertEquals('n', m[10]);
        assertEquals(0, m[12]);
        // hori.py stick RS
        assertArrayEquals(new byte[] {1, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1}, Arrays.copyOfRange(m, 0x18a, 0x18a + 18));

        assertEquals(p, HoriProfileMemory.decode(m));

        // and back to default puts the default bytes back
        p.resetButtons();
        p.setStick(HoriStick.LS);
        p.setName("");
        HoriProfileMemory.encode(p, m);
        assertArrayEquals(HoriProfileMemory.defaults(), m);
    }

    @Test
    @DisplayName("only what changed is written")
    void test3() throws IOException {
        FakeTransport t = new FakeTransport();
        HoriProfile p = HoriProfileMemory.read(t, 2);
        byte[] current = t.readMemory(2, 0, HoriProfileMemory.SIZE);

        p.setFunction(HoriButton.X, HoriFunction.DPAD_UP);
        assertEquals(1, HoriProfileMemory.write(t, 2, p, current));
        assertArrayEquals(new int[] {2, 0x11b, 8}, t.writes.get(0));

        assertEquals(0, HoriProfileMemory.write(t, 2, p, current));

        p.setName("x");
        p.setStick(HoriStick.RS);
        assertEquals(2, HoriProfileMemory.write(t, 2, p, current));
        assertEquals(p, HoriProfileMemory.read(t, 2));
        // other profiles are not touched
        assertArrayEquals(HoriProfileMemory.defaults(), t.memory[1]);

        assertThrows(IllegalArgumentException.class, () -> HoriProfileMemory.read(t, 5));
    }

    @Test
    @DisplayName("properties and hori.py commands")
    void test4() {
        HoriProfile p = new HoriProfile();
        p.setName("it's");
        p.setFunction(HoriButton.LB, HoriFunction.LS_UP);
        p.setStick(HoriStick.RS);

        Properties props = p.toProperties();
        assertEquals("LS-UP", props.getProperty("button.LB"));
        assertEquals(p, HoriProfile.fromProperties(props));

        List<String> commands = p.toHoriPyCommands(3);
        assertEquals("hori.py name 3 'it'\\''s'", commands.get(0));
        assertEquals("hori.py map 3 default LB=LS-UP", commands.get(1));
        assertEquals("hori.py stick 3 RS", commands.get(2));

        assertThrows(IllegalArgumentException.class, () -> p.setName("12345678901234567"));
        assertEquals(HoriButton.SELECT, HoriButton.parse("view"));
        assertEquals(HoriButton.A, HoriButton.parse("cross"));
        assertEquals(HoriFunction.START, HoriFunction.parse("menu"));
        assertEquals(HoriFunction.DISABLED, HoriFunction.valueOf(0x99));
    }

    @Test
    @DisplayName("an unknown protocol sends nothing")
    void test5() {
        HoriTransport t = HoriTransport.unsupported("no");
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> t.writeMemory(1, 0, new byte[1]));
        assertEquals("no", e.getMessage());
    }
}
