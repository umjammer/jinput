/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb.parser;


import java.io.IOException;
import java.util.EnumSet;
import java.util.logging.Level;

import net.java.games.input.plugin.DualShock4PluginBase;
import net.java.games.input.usb.UsagePage;
import net.java.games.input.usb.parser.HidParser.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.StringUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * HidParserTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-12-27 nsano initial version <br>
 */
class HidParserTest {

    @Test
    void test1() {
        int[] testData = HidParserTestData.cheapGamepad;
        byte[] descriptor = new byte[testData.length];
        for (int i = 0; i < descriptor.length; i++)
            descriptor[i] = (byte) testData[i];
        HidParser parser = new HidParser();
        parser.parse(descriptor, descriptor.length);
        parser.dump();
    }

    @Test
    void test2() {
        EnumSet<Feature> feature = HidParser.Feature.valueOf(4);
Debug.println("enumSet: " + Feature.asString(feature));
        assertEquals("Data, Array, Relative, No Wrap, Linear, Preferred State, No Null Position, Non Volatile, Bitfield", Feature.asString(feature));
    }

    @Test
    @DisplayName("get/setValue")
    void test3() {
        Field field = new Field(10, 3);
Debug.printf("offsetByte: %d, startBit: %d", field.offsetByte, field.startBit);
Debug.printf("%s, 0x%02x, %s", field.toBit("*", "_"), field.mask, StringUtil.toBits(field.mask));
        assertEquals("__***___", field.toBit("*", "_"));
        assertEquals(1, field.offsetByte);
        assertEquals(0x1c, field.mask);

        // the first byte is report id
        byte[] data = { 0x01, 0x00, 0x56 }; // _X*_*_X_ .X.*.*X.
                                            //   ~~~       ~~~
                                            //   ~~~        +--- StringUtil#toBits() ... MSB <- LSB
                                            //    +------------- Field#toBit()       ... LSB -> MSB

        int v = field.getValue(data);
        assertEquals(5, v);

        field.setValue(data, (byte) 0x03); // _X**__X_ .X..**X.
                                           //   ~~~       ~~~
        assertEquals(0x4e, data[2]); // index value 2 means offset byte (1) + report id size (1)
    }

    @Test
    @DisplayName("toBit")
    void test4() {
        // s != 0, l <
        Field field = new Field(1, 3);
        assertEquals("_***____", field.toBit("*", "_"));

        field = new Field(6, 2);
        assertEquals("______**", field.toBit("*", "_"));

        field = new Field(5, 6);
        assertEquals("_____******_____", field.toBit("*", "_"));

        field = new Field(4, 12);
        assertEquals("____************", field.toBit("*", "_"));
    }

    // TODO wip
    @Test
    void test5() throws IOException {
        byte[] desk = HidParserTest.class.getResourceAsStream("/ds4_ir_desc.dat").readAllBytes();
        int r = desk.length;

        byte[] ir = HidParserTest.class.getResourceAsStream("/ds4_ir.dat").readAllBytes();
        DualShock4PluginBase.display(ir, System.out);

        HidParser parser = new HidParser();
Debug.println(Level.FINER, "getFields: " + parser.parse(desk, r).enumerateFields().size());
        parser.parse(desk, r).enumerateFields().forEach(f -> {
            if (UsagePage.map(f.getUsagePage()) != null) {
                switch (UsagePage.map(f.getUsagePage())) {
                    case GENERIC_DESKTOP -> {
                        System.out.println(f);
                    }
                    case BUTTON -> {
                        System.out.println(f);
                    }
                    default -> {
                    }
                }
            }
        });
    }
}