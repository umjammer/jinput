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

import net.java.games.input.Component;


/**
 * Generic Desktop Usages
 *
 * @author elias
 * @version 1.0
 */
public enum GenericDesktopUsageId implements UsageId {

    /** Physical Collection */
    POINTER(0x01),
    /** Application Collection */
    MOUSE(0x02),
    // 0x03 Reserved
    /** Application Collection */
    JOYSTICK(0x04),
    /** Application Collection */
    GAME_PAD(0x05),
    /** Application Collection */
    KEYBOARD(0x06),
    /** Application Collection */
    KEYPAD(0x07),
    /** Application Collection */
    MULTI_AXIS_CONTROLLER(0x08),
    // 0x09 - 0x2F Reserved
    /** Dynamic Value */
    X(0x30, Component.Identifier.Axis.X),
    /** Dynamic Value */
    Y(0x31, Component.Identifier.Axis.Y),
    /** Dynamic Value */
    Z(0x32, Component.Identifier.Axis.Z),
    /** Dynamic Value */
    RX(0x33, Component.Identifier.Axis.RX),
    /** Dynamic Value */
    RY(0x34, Component.Identifier.Axis.RY),
    /** Dynamic Value */
    RZ(0x35, Component.Identifier.Axis.RZ),
    /** Dynamic Value */
    SLIDER(0x36, Component.Identifier.Axis.SLIDER),
    /** Dynamic Value */
    DIAL(0x37),
    /** Dynamic Value */
    WHEEL(0x38, Component.Identifier.Axis.Z),
    /** Dynamic Value */
    HATSWITCH(0x39, Component.Identifier.Axis.POV),
    /** Logical Collection */
    COUNTED_BUFFER(0x3A),
    /** Dynamic Value */
    BYTE_COUNT(0x3B),
    /** One-Shot Control */
    MOTION_WAKEUP(0x3C),
    /** On/Off Control */
    START(0x3D),
    /** On/Off Control */
    SELECT(0x3E, Component.Identifier.Button.SELECT),
    // 0x3F Reserved
    /** Dynamic Value */
    VX(0x40),
    /** Dynamic Value */
    VY(0x41),
    /** Dynamic Value */
    VZ(0x42),
    /** Dynamic Value */
    VBRX(0x43),
    /** Dynamic Value */
    VBRY(0x44),
    /** Dynamic Value */
    VBRZ(0x45),
    /** Dynamic Value */
    VNO(0x46),
    // 0x47 - 0x7F Reserved
    /** Application Collection */
    SYSTEM_CONTROL(0x80),
    /** One-Shot Control */
    SYSTEM_POWER_DOWN(0x81),
    /** One-Shot Control */
    SYSTEM_SLEEP(0x82),
    /** One-Shot Control */
    SYSTEM_WAKE_UP(0x83),
    /** One-Shot Control */
    SYSTEM_CONTEXT_MENU(0x84),
    /** One-Shot Control */
    SYSTEM_MAIN_MENU(0x85),
    /** One-Shot Control */
    SYSTEM_APP_MENU(0x86),
    /** One-Shot Control */
    SYSTEM_MENU_HELP(0x87),
    /** One-Shot Control */
    SYSTEM_MENU_EXIT(0x88),
    /** Selector */
    SYSTEM_MENU(0x89),
    /** Re-Trigger Control */
    SYSTEM_MENU_RIGHT(0x8A),
    /** Re-Trigger Control */
    SYSTEM_MENU_LEFT(0x8B),
    /** Re-Trigger Control */
    SYSTEM_MENU_UP(0x8C),
    /** Re-Trigger Control */
    SYSTEM_MENU_DOWN(0x8D),
    // 0x8E - 0x8F Reserved
    /** On/Off Control */
    DPAD_UP(0x90),
    /** On/Off Control */
    DPAD_DOWN(0x91),
    /** On/Off Control */
    DPAD_RIGHT(0x92),
    /** On/Off Control */
    DPAD_LEFT(0x93);
    // 0x94 - 0xFFFF Reserved

    private final int usageId;
    private final Component.Identifier identifier;

    public static GenericDesktopUsageId map(int usageId) {
        return Arrays.stream(values()).filter(e -> e.usageId == usageId).findFirst().orElse(null);
    }

    GenericDesktopUsageId(int usageId) {
        this(usageId, null);
    }

    GenericDesktopUsageId(int usageId, Component.Identifier identifier) {
        this.usageId = usageId;
        this.identifier = identifier;
    }

    @Override
    public int getId() {
        return usageId;
    }

    @Override
    public final Component.Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + usageId + ")";
    }
}
