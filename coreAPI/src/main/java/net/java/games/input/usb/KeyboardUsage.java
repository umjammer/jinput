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
 * Mapping from Keyboard HID usages to Component.Identifier.Key
 *
 * @author elias
 * @version 1.0
 */
public enum KeyboardUsage implements Usage {

    /** ErrorRollOver */
    ERRORROLLOVER(0x01),
    /** POSTFail */
    POSTFAIL(0x02),
    /** ErrorUndefined */
    ERRORUNDEFINED(0x03),
    /** a or A */
    A(Component.Identifier.Key.A, 0x04),
    /** b or B */
    B(Component.Identifier.Key.B, 0x05),
    /** c or C */
    C(Component.Identifier.Key.C, 0x06),
    /** d or D */
    D(Component.Identifier.Key.D, 0x07),
    /** e or E */
    E(Component.Identifier.Key.E, 0x08),
    /** f or F */
    F(Component.Identifier.Key.F, 0x09),
    /** g or G */
    G(Component.Identifier.Key.G, 0x0A),
    /** h or H */
    H(Component.Identifier.Key.H, 0x0B),
    /** i or I */
    I(Component.Identifier.Key.I, 0x0C),
    /** j or J */
    J(Component.Identifier.Key.J, 0x0D),
    /** k or K */
    K(Component.Identifier.Key.K, 0x0E),
    /** l or L */
    L(Component.Identifier.Key.L, 0x0F),
    /** m or M */
    M(Component.Identifier.Key.M, 0x10),
    /** n or N */
    N(Component.Identifier.Key.N, 0x11),
    /** o or O */
    O(Component.Identifier.Key.O, 0x12),
    /** p or P */
    P(Component.Identifier.Key.P, 0x13),
    /** q or Q */
    Q(Component.Identifier.Key.Q, 0x14),
    /** r or R */
    R(Component.Identifier.Key.R, 0x15),
    /** s or S */
    S(Component.Identifier.Key.S, 0x16),
    /** t or T */
    T(Component.Identifier.Key.T, 0x17),
    /** u or U */
    U(Component.Identifier.Key.U, 0x18),
    /** v or V */
    V(Component.Identifier.Key.V, 0x19),
    /** w or W */
    W(Component.Identifier.Key.W, 0x1A),
    /** x or X */
    X(Component.Identifier.Key.X, 0x1B),
    /** y or Y */
    Y(Component.Identifier.Key.Y, 0x1C),
    /** z or Z */
    Z(Component.Identifier.Key.Z, 0x1D),
    /** 1 or ! */
    _1(Component.Identifier.Key._1, 0x1E),
    /** 2 or @ */
    _2(Component.Identifier.Key._2, 0x1F),
    /** 3 or # */
    _3(Component.Identifier.Key._3, 0x20),
    /** 4 or $ */
    _4(Component.Identifier.Key._4, 0x21),
    /** 5 or % */
    _5(Component.Identifier.Key._5, 0x22),
    /** 6 or ^ */
    _6(Component.Identifier.Key._6, 0x23),
    /** 7 or & */
    _7(Component.Identifier.Key._7, 0x24),
    /** 8 or * */
    _8(Component.Identifier.Key._8, 0x25),
    /** 9 or ( */
    _9(Component.Identifier.Key._9, 0x26),
    /** 0 or ) */
    _0(Component.Identifier.Key._0, 0x27),
    /** Return (Enter) */
    ENTER(Component.Identifier.Key.RETURN, 0x28),
    /** Escape */
    ESCAPE(Component.Identifier.Key.ESCAPE, 0x29),
    /** Delete (Backspace) */
    BACKSPACE(Component.Identifier.Key.BACK, 0x2A),
    /** Tab */
    TAB(Component.Identifier.Key.TAB, 0x2B),
    /** Spacebar */
    SPACEBAR(Component.Identifier.Key.SPACE, 0x2C),
    /** - or _ */
    HYPHEN(Component.Identifier.Key.MINUS, 0x2D),
    /** = or + */
    EQUALSIGN(Component.Identifier.Key.EQUALS, 0x2E),
    /** [ or { */
    OPENBRACKET(Component.Identifier.Key.LBRACKET, 0x2F),
    /** ] or } */
    CLOSEBRACKET(Component.Identifier.Key.RBRACKET, 0x30),
    /** \ or | */
    BACKSLASH(Component.Identifier.Key.BACKSLASH, 0x31),
    /** Non-US # or _ */
    NONUSPOUNT(Component.Identifier.Key.PERIOD, 0x32),
    /** ; or : */
    SEMICOLON(Component.Identifier.Key.SEMICOLON, 0x33),
    /** ' or " */
    QUOTE(Component.Identifier.Key.APOSTROPHE, 0x34),
    /** Grave Accent and Tilde */
    TILDE(Component.Identifier.Key.GRAVE, 0x35),
    /** , or < */
    COMMA(Component.Identifier.Key.COMMA, 0x36),
    /** . or > */
    PERIOD(Component.Identifier.Key.PERIOD, 0x37),
    /** / or ? */
    SLASH(Component.Identifier.Key.SLASH, 0x38),
    /** Caps Lock */
    CAPSLOCK(Component.Identifier.Key.CAPITAL, 0x39),
    /** F1 */
    F1(Component.Identifier.Key.F1, 0x3A),
    /** F2 */
    F2(Component.Identifier.Key.F2, 0x3B),
    /** F3 */
    F3(Component.Identifier.Key.F3, 0x3C),
    /** F4 */
    F4(Component.Identifier.Key.F4, 0x3D),
    /** F5 */
    F5(Component.Identifier.Key.F5, 0x3E),
    /** F6 */
    F6(Component.Identifier.Key.F6, 0x3F),
    /** F7 */
    F7(Component.Identifier.Key.F7, 0x40),
    /** F8 */
    F8(Component.Identifier.Key.F8, 0x41),
    /** F9 */
    F9(Component.Identifier.Key.F9, 0x42),
    /** F10 */
    F10(Component.Identifier.Key.F10, 0x43),
    /** F11 */
    F11(Component.Identifier.Key.F11, 0x44),
    /** F12 */
    F12(Component.Identifier.Key.F12, 0x45),
    /** PrintScreen */
    PRINTSCREEN(Component.Identifier.Key.SYSRQ, 0x46),
    /** Scroll Lock */
    SCROLLLOCK(Component.Identifier.Key.SCROLL, 0x47),
    /** Pause */
    PAUSE(Component.Identifier.Key.PAUSE, 0x48),
    /** Insert */
    INSERT(Component.Identifier.Key.INSERT, 0x49),
    /** Home */
    HOME(Component.Identifier.Key.HOME, 0x4A),
    /** Page Up */
    PAGEUP(Component.Identifier.Key.PAGEUP, 0x4B),
    /** Delete Forward */
    DELETE(Component.Identifier.Key.DELETE, 0x4C),
    /** End */
    END(Component.Identifier.Key.END, 0x4D),
    /** Page Down */
    PAGEDOWN(Component.Identifier.Key.PAGEDOWN, 0x4E),
    /** Right Arrow */
    RIGHTARROW(Component.Identifier.Key.RIGHT, 0x4F),
    /** Left Arrow */
    LEFTARROW(Component.Identifier.Key.LEFT, 0x50),
    /** Down Arrow */
    DOWNARROW(Component.Identifier.Key.DOWN, 0x51),
    /** Up Arrow */
    UPARROW(Component.Identifier.Key.UP, 0x52),
    /** Keypad NumLock or Clear */
    KEYPAD_NUMLOCK(Component.Identifier.Key.NUMLOCK, 0x53),
    /** Keypad / */
    KEYPAD_SLASH(Component.Identifier.Key.DIVIDE, 0x54),
    /** Keypad * */
    KEYPAD_ASTERICK(0x55),
    /** Keypad - */
    KEYPAD_HYPHEN(Component.Identifier.Key.SUBTRACT, 0x56),
    /** Keypad + */
    KEYPAD_PLUS(Component.Identifier.Key.ADD, 0x57),
    /** Keypad Enter */
    KEYPAD_ENTER(Component.Identifier.Key.NUMPADENTER, 0x58),
    /** Keypad 1 or End */
    KEYPAD_1(Component.Identifier.Key.NUMPAD1, 0x59),
    /** Keypad 2 or Down Arrow */
    KEYPAD_2(Component.Identifier.Key.NUMPAD2, 0x5A),
    /** Keypad 3 or Page Down */
    KEYPAD_3(Component.Identifier.Key.NUMPAD3, 0x5B),
    /** Keypad 4 or Left Arrow */
    KEYPAD_4(Component.Identifier.Key.NUMPAD4, 0x5C),
    /** Keypad 5 */
    KEYPAD_5(Component.Identifier.Key.NUMPAD5, 0x5D),
    /** Keypad 6 or Right Arrow */
    KEYPAD_6(Component.Identifier.Key.NUMPAD6, 0x5E),
    /** Keypad 7 or Home */
    KEYPAD_7(Component.Identifier.Key.NUMPAD7, 0x5F),
    /** Keypad 8 or Up Arrow */
    KEYPAD_8(Component.Identifier.Key.NUMPAD8, 0x60),
    /** Keypad 9 or Page Up */
    KEYPAD_9(Component.Identifier.Key.NUMPAD9, 0x61),
    /** Keypad 0 or Insert */
    KEYPAD_0(Component.Identifier.Key.NUMPAD0, 0x62),
    /** Keypad . or Delete */
    KEYPAD_PERIOD(Component.Identifier.Key.DECIMAL, 0x63),
    /** Non-US \ or | */
    NONUSBACKSLASH(Component.Identifier.Key.BACKSLASH, 0x64),
    /** Application */
    APPLICATION(Component.Identifier.Key.APPS, 0x65),
    /** Power */
    POWER(Component.Identifier.Key.POWER, 0x66),
    /** Keypad = */
    KEYPAD_EQUALSIGN(Component.Identifier.Key.NUMPADEQUAL, 0x67),
    /** F13 */
    F13(Component.Identifier.Key.F13, 0x68),
    /** F14 */
    F14(Component.Identifier.Key.F14, 0x69),
    /** F15 */
    F15(Component.Identifier.Key.F15, 0x6A),
    /** F16 */
    F16(0x6B),
    /** F17 */
    F17(0x6C),
    /** F18 */
    F18(0x6D),
    /** F19 */
    F19(0x6E),
    /** F20 */
    F20(0x6F),
    /** F21 */
    F21(0x70),
    /** F22 */
    F22(0x71),
    /** F23 */
    F23(0x72),
    /** F24 */
    F24(0x73),
    /** Execute */
    EXECUTE(0x74),
    /** Help */
    HELP(0x75),
    /** Menu */
    MENU(0x76),
    /** Select */
    SELECT(0x77),
    /** Stop */
    STOP(Component.Identifier.Key.STOP, 0x78),
    /** Again */
    AGAIN(0x79),
    /** Undo */
    UNDO(0x7A),
    /** Cut */
    CUT(0x7B),
    /** Copy */
    COPY(0x7C),
    /** Paste */
    PASTE(0x7D),
    /** Find */
    FIND(0x7E),
    /** Mute */
    MUTE(0x7F),
    /** Volume Up */
    VOLUMEUP(0x80),
    /** Volume Down */
    VOLUMEDOWN(0x81),
    /** Locking Caps Lock */
    LOCKINGCAPSLOCK(Component.Identifier.Key.CAPITAL, 0x82),
    /** Locking Num Lock */
    LOCKINGNUMLOCK(Component.Identifier.Key.NUMLOCK, 0x83),
    /** Locking Scroll Lock */
    LOCKINGSCROLLLOCK(Component.Identifier.Key.SCROLL, 0x84),
    /** Keypad Comma */
    KEYPAD_COMMA(Component.Identifier.Key.COMMA, 0x85),
    /** Keypad Equal Sign for AS/400 */
    KEYPAD_EQUALSSIGNAS400(0x86),
    /** International1 */
    INTERNATIONAL1(0x87),
    /** International2 */
    INTERNATIONAL2(0x88),
    /** International3 */
    INTERNATIONAL3(0x89),
    /** International4 */
    INTERNATIONAL4(0x8A),
    /** International5 */
    INTERNATIONAL5(0x8B),
    /** International6 */
    INTERNATIONAL6(0x8C),
    /** International7 */
    INTERNATIONAL7(0x8D),
    /** International8 */
    INTERNATIONAL8(0x8E),
    /** International9 */
    INTERNATIONAL9(0x8F),
    /** LANG1 */
    LANG1(0x90),
    /** LANG2 */
    LANG2(0x91),
    /** LANG3 */
    LANG3(0x92),
    /** LANG4 */
    LANG4(0x93),
    /** LANG5 */
    LANG5(0x94),
    /** LANG6 */
    LANG6(0x95),
    /** LANG7 */
    LANG7(0x96),
    /** LANG8 */
    LANG8(0x97),
    /** LANG9 */
    LANG9(0x98),
    /** AlternateErase */
    ALTERNATEERASE(0x99),
    /** SysReq/Attention */
    SYSREQORATTENTION(Component.Identifier.Key.SYSRQ, 0x9A),
    /** Cancel */
    CANCEL(0x9B),
    /** Clear */
    CLEAR(0x9C),
    /** Prior */
    PRIOR(Component.Identifier.Key.PAGEUP, 0x9D),
    /** Return */
    RETURN(Component.Identifier.Key.RETURN, 0x9E),
    /** Separator */
    SEPARATOR(0x9F),
    /** Out */
    OUT(0xA0),
    /** Oper */
    OPER(0xA1),
    /** Clear/Again */
    CLEARORAGAIN(0xA2),
    /** CrSel/Props */
    CRSELORPROPS(0xA3),
    /** ExSel */
    EXSEL(0xA4),
    // 0xA5-0xDF Reserved
    /** Left Control */
    LEFTCONTROL(Component.Identifier.Key.LCONTROL, 0xE0),
    /** Left Shift */
    LEFTSHIFT(Component.Identifier.Key.LSHIFT, 0xE1),
    /** Left Alt */
    LEFTALT(Component.Identifier.Key.LALT, 0xE2),
    /** Left GUI */
    LEFTGUI(Component.Identifier.Key.LWIN, 0xE3),
    /** Right Control */
    RIGHTCONTROL(Component.Identifier.Key.RCONTROL, 0xE4),
    /** Right Shift */
    RIGHTSHIFT(Component.Identifier.Key.RSHIFT, 0xE5),
    /** Right Alt */
    RIGHTALT(Component.Identifier.Key.RALT, 0xE6),
    /** Right GUI */
    RIGHTGUI(Component.Identifier.Key.RWIN, 0xE7);

    private final int usage;
    private final Component.Identifier.Key identifier;

    @Override
    public final Component.Identifier.Key getIdentifier() {
        return identifier;
    }

    public static KeyboardUsage map(int usage) {
        return Arrays.stream(values()).filter(e -> e.usage == usage).findFirst().orElse(null);
    }

    KeyboardUsage(int usage) {
        this(Component.Identifier.Key.UNKNOWN, usage);
    }

    KeyboardUsage(Component.Identifier.Key id, int usage) {
        this.identifier = id;
        this.usage = usage;
    }
}
