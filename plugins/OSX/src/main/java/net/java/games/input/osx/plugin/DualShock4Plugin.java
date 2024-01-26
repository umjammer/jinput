/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.plugin;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.DeviceSupportPlugin;
import net.java.games.input.Rumbler;
import net.java.games.input.osx.OSXHIDDevice;
import net.java.games.input.osx.OSXHIDElement;
import net.java.games.input.osx.OSXRumbler;
import net.java.games.input.usb.ElementType;
import net.java.games.input.usb.GenericDesktopUsageId;
import net.java.games.input.usb.HidController;
import net.java.games.input.usb.HidRumbler;
import net.java.games.input.usb.UsagePage;
import net.java.games.input.usb.UsagePair;
import vavi.util.ByteUtil;

import static net.java.games.input.osx.OSXHIDDevice.AXIS_DEFAULT_MAX_VALUE;
import static net.java.games.input.osx.OSXHIDDevice.AXIS_DEFAULT_MIN_VALUE;


/**
 * DualShock4Plugin.
 *
 * TODO extract osx independent part
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-24 nsano initial version <br>
 */
public class DualShock4Plugin implements DeviceSupportPlugin {

    // TODO should have report id???
    public enum DualShock4Output implements Component.Identifier.Output {
        SMALL_RUMBLE("smallRumble"),
        BIG_RUMBLE("bigRumble"),
        LED_RED("ledRed"),
        LED_GREEN("ledGreen"),
        LED_BLUE("ledBlue"),
        FLASH_LED1("flashLed1"),
        FLASH_LED2("flashLed2");

        final String name;

        @Override
        public String getName() {
            return name;
        }

        /**
         * Protected constructor
         */
        DualShock4Output(String name) {
            this.name = name;
        }
    }

    /** @param object OSXHIDDevice */
    @Override
    public boolean match(Object object) {
        if (!(object instanceof OSXHIDDevice device)) return false;

        return device.getVendorId() == 0x54c && device.getProductId() == 0x9cc;
    }

    @Override
    public Collection<Component> getExtraComponents(Object object) {
        if (!(object instanceof OSXHIDDevice device)) return Collections.emptyList();

        UsagePair usagePair = new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD);
        int elementCookie = 240;
        ElementType elementType = ElementType.INPUT_AXIS;
        int min = AXIS_DEFAULT_MIN_VALUE;
        int max = AXIS_DEFAULT_MAX_VALUE;
        boolean isRelative = false;

        OSXHIDElement element = new OSXHIDElement(device, usagePair, elementCookie, elementType, min, max, isRelative);

        // TODO unhandled values by IOKit HID (how can we get those values?)
        return List.of(/*new OSXComponent(Component.Identifier.Axis.RX_ACCELERATION, element)*/);
    }

    @Override
    public Collection<Controller> getExtraChildControllers(Object object) {
        return Collections.emptyList();
    }

    /** TODO for bluetooth */
    private static final int BASE_OFFSET = 0;

    @Override
    public Collection<Rumbler> getExtraRumblers(Object object) {
        if (!(object instanceof OSXHIDDevice device)) return Collections.emptyList();

        return List.of(
                new OSXRumbler(device, 5, DualShock4Output.SMALL_RUMBLE, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 3, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.BIG_RUMBLE, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 4, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.LED_RED, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 5, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.LED_GREEN, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 6, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.LED_GREEN, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 7, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.FLASH_LED1, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 8, ElementType.OUTPUT, 0, 255, false)),
                new OSXRumbler(device, 5, DualShock4Output.FLASH_LED2, new OSXHIDElement(device, new UsagePair(UsagePage.GAME, GenericDesktopUsageId.GAME_PAD), BASE_OFFSET + 9, ElementType.OUTPUT, 0, 255, false))
        );
    }

    /** Represents report id 5 data */
    public static class Report5 extends HidController.HidReport {
        private final byte[] data = new byte[31];

        public int smallRumble;
        public int bigRumble;
        public int ledRed;
        public int ledGreen;
        public int ledBlue;
        public int flashLed1;
        public int flashLed2;

        public Report5() {
            data[0] = (byte) 255;
        }

        @Override public int getReportId() {
            return 5;
        }

        @Override public byte[] getData() {
            return data;
        }

        @Override protected void cascadeTo(HidRumbler rumbler) {
            float value = switch ((DualShock4Output) rumbler.getOutputIdentifier()) {
                case SMALL_RUMBLE -> smallRumble;
                case BIG_RUMBLE -> bigRumble;
                case LED_RED -> ledRed;
                case LED_GREEN -> ledGreen;
                case LED_BLUE -> ledBlue;
                case FLASH_LED1 -> flashLed1;
                case FLASH_LED2 -> flashLed2;
            };
            rumbler.setValue(value);
        }
    }

    /**
     * @param data includes the first byte (report id)
     * @see "https://www.psdevwiki.com/ps4/DS4-USB"
     */
    public static void display(byte[] data, PrintStream pr) {
        int l3x = data[1] & 0xff;
        int l3y = data[2] & 0xff;
        int r3x = data[3] & 0xff;
        int r3y = data[4] & 0xff;

        boolean tri	= (data[5] & 0x80) != 0;
        boolean cir	= (data[5] & 0x40) != 0;
        boolean x = (data[5] & 0x20) != 0;
        boolean sqr = (data[5] & 0x10) != 0;
        int dPad = data[5] & 0x0f;

        enum Hat {
            N("↑"), NE("↗"), E("→"), SE("↘"), S("↓"), SW("↙"), W("←"), NW("↖"), Released(" "); final String s; Hat(String s) { this.s = s; }
        }

        boolean r3 = (data[6] & 0x80) != 0;
        boolean l3 = (data[6] & 0x40) != 0;
        boolean opt = (data[6] & 0x20) != 0;
        boolean share = (data[6] & 0x10) != 0;
        boolean r2 = (data[6] & 0x08) != 0;
        boolean l2 = (data[6] & 0x04) != 0;
        boolean r1 = (data[6] & 0x02) != 0;
        boolean l1 = (data[6] & 0x01) != 0;

        int counter = (data[7] >> 2) & 0x3f;
        boolean tPad = (data[7] & 0x02) != 0;
        boolean ps = (data[7] & 0x01) != 0;

        int lTrigger = data[8] & 0xff;
        int rTrigger = data[9] & 0xff;

        int timeStump = ByteUtil.readLeShort(data, 10) & 0xffff;
        int batteryLevel = data[12] & 0xff;

        int gyroX = ByteUtil.readLeShort(data, 13) & 0xffff;
        int gyroY = ByteUtil.readLeShort(data, 15) & 0xffff;
        int gyroZ = ByteUtil.readLeShort(data, 17) & 0xffff;

        int accelX = ByteUtil.readLeShort(data, 19) & 0xffff;
        int accelY = ByteUtil.readLeShort(data, 21) & 0xffff;
        int accelZ = ByteUtil.readLeShort(data, 23) & 0xffff;

        boolean headphone = (data[30] & 0x40) != 0;
        boolean mic = (data[30] & 0x20) != 0;

        int touch1X = (data[36] & 0xff) | ((data[37] & 0x0f) << 8);
        int touch1Y = ((data[38] & 0xff) << 4) | (data[37] & 0xf);

        int touch2X = (data[40] & 0xff) | ((data[41] & 0x0f) << 8);
        int touch2Y = ((data[42] & 0xff) << 4) | (data[41] & 0xf);

        pr.printf("L3 x: %02x%n", l3x);
        pr.printf("L3 y: %02x%n", l3y);
        pr.printf("R3 x: %02x%n", r3x);
        pr.printf("R3 y: %02x%n", r3y);
        pr.printf("hat: %s%n", Hat.values()[dPad].s);

        pr.printf("counter: %d%n", counter);

        pr.printf("▲: %s%n", tri ? "●" : "◯");
        pr.printf("●: %s%n", cir ? "●" : "◯");
        pr.printf("✖: %s%n", x ? "●" : "◯");
        pr.printf("■: %s%n", sqr ? "●" : "◯");

        pr.printf("L1: %s%n", l1 ? "●" : "◯");
        pr.printf("R1: %s%n", r1 ? "●" : "◯");
        pr.printf("L2: %s%n", l2 ? "●" : "◯");
        pr.printf("R2: %s%n", r2 ? "●" : "◯");
        pr.printf("Share: %s%n", share ? "●" : "◯");
        pr.printf("Opt: %s%n", opt ? "●" : "◯");
        pr.printf("L3: %s%n", l3 ? "●" : "◯");
        pr.printf("R3: %s%n", r3 ? "●" : "◯");

        pr.printf("T-PAD: %s%n", tPad ? "●" : "◯");
        pr.printf("PS: %s%n", ps ? "●" : "◯");

        pr.printf("lTrigger: %02x%n", lTrigger);
        pr.printf("rTrigger: %02x%n", rTrigger);

        pr.printf("timeStump: %04x%n", timeStump);
        pr.printf("batteryLevel: %d%n", batteryLevel);

        pr.printf("\uD83C\uDFA7: %s%n", headphone ? "●" : "◯");
        pr.printf("🎤: %s%n", mic ? "●" : "◯");

        pr.printf("gyro x: %04x%n", gyroX);
        pr.printf("gyro y: %04x%n", gyroY);
        pr.printf("gyro z: %04x%n", gyroZ);

        pr.printf("accel x: %04x%n", accelX);
        pr.printf("accel y: %04x%n", accelY);
        pr.printf("accel z: %04x%n", accelZ);

        pr.printf("touch1 x: %d%n", touch1X);
        pr.printf("touch1 y: %d%n", touch1Y);

        pr.printf("touch2 x: %d%n", touch2X);
        pr.printf("touch2 y: %d%n", touch2Y);
        pr.println();
    }
}
