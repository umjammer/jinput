/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 *
 */

package net.java.games.input.usb;

import java.util.Arrays;
import java.util.function.Function;


/**
 * HID Usage pages
 *
 * @author elias
 * @version 1.0
 * @see "https://www.usb.org/sites/default/files/hut1_22.pdf"
 */
public enum UsagePage {

    UNDEFINED(0x00),
    GENERIC_DESKTOP(0x01, GenericDesktopUsageId::map),
    SIMULATION(0x02),
    VR(0x03),
    SPORT(0x04),
    GAME(0x05),
    // Reserved 0x06
    /** USB Device Class Definition for Human Interface Devices (HID). Note: the usage type for all key codes is Selector (Sel). */
    KEYBOARD_OR_KEYPAD(0x07, KeyboardUsageId::map),
    LEDS(0x08),
    BUTTON(0x09, ButtonUsageId::map),
    ORDINAL(0x0A),
    TELEPHONY(0x0B),
    CONSUMER(0x0C),
    DIGITIZER(0x0D),
    // Reserved 0x0E
    /** USB Physical Interface Device definitions for force feedback and related devices. */
    PID(0x0F),
    UNICODE(0x10),
    // Reserved 0x11 - 0x13
    ALPHANUMERIC_DISPLAY(0x14),
    // Reserved 0x15 - 0x7F
    // Monitor 0x80 - 0x83   USB Device Class Definition for Monitor Devices
    // Power 0x84 - 0x87     USB Device Class Definition for Power Devices
    /** Power Device Page */
    POWER_DEVICE(0x84),
    /** Battery System Page */
    BATTERY_SYSTEM(0x85),
    // Reserved 0x88 - 0x8B
    /** (Point of Sale) USB Device Class Definition for Bar Code Scanner Devices */
    BAR_CODE_SCANNER(0x8C),
    /** (Point of Sale) USB Device Class Definition for Scale Devices */
    SCALE(0x8D),
    // ReservedPointofSalepages 0x8E - 0X8F
    /** USB Device Class Definition for Image Class Devices */
    CAMERACONTROL(0x90),
    /** OAAF Definitions for arcade and coinop related Devices */
    ARCADE(0x91);

    private final Function<Integer, UsageId> usageMap;
    private final int usagePageId;

    public static UsagePage map(int pageId) {
        return Arrays.stream(values()).filter(e -> e.usagePageId == pageId).findFirst().orElse(null);
    }

    UsagePage(int pageId, Function<Integer, UsageId> usageMap) {
        this.usageMap = usageMap;
        this.usagePageId = pageId;
    }

    UsagePage(int pageId) {
        this(pageId, null);
    }

    public int getId() {
        return usagePageId;
    }

    public final UsageId mapUsage(int usageId) {
        return usageMap == null ? null : usageMap.apply(usageId);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + usagePageId + ")";
    }
}
