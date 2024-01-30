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
import net.java.games.input.Rumbler;
import net.java.games.input.osx.OSXHIDDevice;
import net.java.games.input.osx.OSXHIDElement;
import net.java.games.input.osx.OSXRumbler;
import net.java.games.input.plugin.DualShock4PluginBase;
import net.java.games.input.usb.ElementType;
import net.java.games.input.usb.GenericDesktopUsageId;
import net.java.games.input.usb.UsagePage;
import net.java.games.input.usb.UsagePair;

import static net.java.games.input.osx.OSXHIDDevice.AXIS_DEFAULT_MAX_VALUE;
import static net.java.games.input.osx.OSXHIDDevice.AXIS_DEFAULT_MIN_VALUE;


/**
 * DualShock4Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-24 nsano initial version <br>
 */
public class DualShock4Plugin extends DualShock4PluginBase {

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
}
