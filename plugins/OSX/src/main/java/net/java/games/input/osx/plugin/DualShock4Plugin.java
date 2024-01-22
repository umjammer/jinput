/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.plugin;

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

    /** */
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

    /** @see "https://www.psdevwiki.com/ps4/DS4-USB" */
    public static void display(byte[] data) {
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

        int touchX = (data[36] & 0xff) | ((data[37] & 0x0f) << 8);
        int touchY = ((data[38] & 0xff) << 4) | (data[37] & 0xf);

        System.out.printf("L3 x:%02x y:%02x R3 x:%02x y:%02x (%d,%d)%n", l3x, l3y, r3x, r3y, counter, timeStump);
        System.out.printf("%3s %3s %3s %3s %5s %2s %s %s %s%n", tri ? "▲" : "", cir ? "●" : "", x ? "✖" : "", sqr ? "■" : "", tPad ? "T-PAD" : "", ps ? "PS" : "", Hat.values()[dPad].s, headphone ? "\uD83C\uDFA7" : "", mic ? "🎤" : "");
        System.out.printf("touch x:%d y:%d gyro x:%04x y:%04x z:%04x, accel x:%04x y:%04x z:%04x%n%n", touchX, touchY, gyroX, gyroY, gyroZ, accelX, accelY, accelZ);
    }
}
