/*
 * Copyright (c) 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package net.java.games.input;

/**
 * An axis is a single button, slider, or dial, which has a single range.  An
 * axis can hold information for motion (linear or rotational), velocity,
 * force, or acceleration.
 */
public interface Component {

    /**
     * Returns the identifier of the axis.
     */
    Identifier getIdentifier();

    /**
     * Returns <code>true</code> if data returned from <code>poll</code>
     * is relative to the last call, or <code>false</code> if data
     * is absolute.
     */
    boolean isRelative();

    /**
     * Returns whether or not the axis is analog, or false if it is digital.
     */
    boolean isAnalog();

    /**
     * Returns the suggested dead zone for this axis.  Dead zone is the
     * amount polled data can vary before considered a significant change
     * in value.  An application can safely ignore changes less than this
     * value in the positive or negative direction.
     *
     * @see #getPollData
     */
    float getDeadZone();

    /**
     * Returns the data from the last time the control has been polled.
     * If this axis is a button, the value returned will be either 0.0f or 1.0f.
     * If this axis is normalized, the value returned will be between -1.0f and
     * 1.0f.
     *
     * @see Controller#poll
     */
    float getPollData();

    /**
     * Returns a human-readable name for this axis.
     */
    String getName();

    /**
     * Identifiers for different Axes.
     */
    interface Identifier {

        /**
         * Returns a non-localized string description of this axis type.
         */
        String getName();

        enum Axis implements Identifier {

            /**
             * An axis for specifying vertical data.
             */
            X("x"),

            /**
             * An axis for specifying horizontal data.
             */
            Y("y"),

            /**
             * An axis for specifying third dimensional up/down
             * data, or linear data in any direction that is
             * neither horizontal nor vertical.
             */
            Z("z"),

            /**
             * An axis for specifying left-right rotational data.
             */
            RX("rx"),

            /**
             * An axis for specifying forward-back rotational data.
             */
            RY("ry"),

            /**
             * An axis for specifying up-down rotational data
             * (rudder control).
             */
            RZ("rz"),

            /**
             * An axis for a slider or mouse wheel.
             */
            SLIDER("slider"),

            /**
             * An axis for slider or mouse wheel acceleration data.
             */
            SLIDER_ACCELERATION("slider-acceleration"),

            /**
             * An axis for slider force data.
             */
            SLIDER_FORCE("slider-force"),

            /**
             * An axis for slider or mouse wheel velocity data.
             */
            SLIDER_VELOCITY("slider-velocity"),

            /**
             * An axis for specifying vertical acceleration data.
             */
            X_ACCELERATION("x-acceleration"),

            /**
             * An axis for specifying vertical force data.
             */
            X_FORCE("x-force"),

            /**
             * An axis for specifying vertical velocity data.
             */
            X_VELOCITY("x-velocity"),

            /**
             * An axis for specifying horizontal acceleration data.
             */
            Y_ACCELERATION("y-acceleration"),

            /**
             * An axis for specifying horizontal force data.
             */
            Y_FORCE("y-force"),

            /**
             * An axis for specifying horizontal velocity data.
             */
            Y_VELOCITY("y-velocity"),

            /**
             * An axis for specifying third dimensional up/down acceleration data.
             */
            Z_ACCELERATION("z-acceleration"),

            /**
             * An axis for specifying third dimensional up/down force data.
             */
            Z_FORCE("z-force"),

            /**
             * An axis for specifying third dimensional up/down velocity data.
             */
            Z_VELOCITY("z-velocity"),

            /**
             * An axis for specifying left-right angular acceleration data.
             */
            RX_ACCELERATION("rx-acceleration"),

            /**
             * An axis for specifying left-right angular force (torque) data.
             */
            RX_FORCE("rx-force"),

            /**
             * An axis for specifying left-right angular velocity data.
             */
            RX_VELOCITY("rx-velocity"),

            /**
             * An axis for specifying forward-back angular acceleration data.
             */
            RY_ACCELERATION("ry-acceleration"),

            /**
             * An axis for specifying forward-back angular force (torque) data.
             */
            RY_FORCE("ry-force"),

            /**
             * An axis for specifying forward-back angular velocity data.
             */
            RY_VELOCITY("ry-velocity"),

            /**
             * An axis for specifying up-down angular acceleration data.
             */
            RZ_ACCELERATION("rz-acceleration"),

            /**
             * An axis for specifying up-down angular force (torque) data.
             */
            RZ_FORCE("rz-force"),

            /**
             * An axis for specifying up-down angular velocity data.
             */
            RZ_VELOCITY("rz-velocity"),

            /**
             * An axis for a point-of-view control.
             */
            POV("pov"),

            /**
             * An unknown axis.
             */
            UNKNOWN("unknown");

            final String name;

            @Override
            public String getName() {
                return name;
            }

            /**
             * @param name name of axis
             */
            Axis(String name) {
                this.name = name;
            }
        }

        enum Button implements Identifier {

            /**
             * First device button
             */
            _0("0"),

            /**
             * Second device button
             */
            _1("1"),

            /**
             * Third device button
             */
            _2("2"),

            /**
             * Fourth device button
             */
            _3("3"),

            /**
             * Fifth device button
             */
            _4("4"),

            /**
             * Sixth device button
             */
            _5("5"),

            /**
             * Seventh device button
             */
            _6("6"),

            /**
             * Eighth device button
             */
            _7("7"),

            /**
             * Ninth device button
             */
            _8("8"),

            /**
             * 10th device button
             */
            _9("9"),
            _10("10"),
            _11("11"),
            _12("12"),
            _13("13"),
            _14("14"),
            _15("15"),
            _16("16"),
            _17("17"),
            _18("18"),
            _19("19"),
            _20("20"),
            _21("21"),
            _22("22"),
            _23("23"),
            _24("24"),
            _25("25"),
            _26("26"),
            _27("27"),
            _28("28"),
            _29("29"),
            _30("30"),
            _31("31"),

            /**
             * Joystick trigger button
             */
            TRIGGER("Trigger"),

            /**
             * Joystick thumb button
             */
            THUMB("Thumb"),

            /**
             * Second joystick thumb button
             */
            THUMB2("Thumb 2"),

            /**
             * Joystick top button
             */
            TOP("Top"),

            /**
             * Second joystick top button
             */
            TOP2("Top 2"),

            /**
             * The joystick button you play with you little finger (Pinkie on *that* side
             * of the pond :P)
             */
            PINKIE("Pinkie"),

            /**
             * Joystick button on the base of the device
             */
            BASE("Base"),

            /**
             * Second joystick button on the base of the device
             */
            BASE2("Base 2"),

            /**
             * Third joystick button on the base of the device
             */
            BASE3("Base 3"),

            /**
             * Fourth joystick button on the base of the device
             */
            BASE4("Base 4"),

            /**
             * Fifth joystick button on the base of the device
             */
            BASE5("Base 5"),

            /**
             * Sixth joystick button on the base of the device
             */
            BASE6("Base 6"),

            /**
             * erm, dunno, but it's in the defines so it might exist.
             */
            DEAD("Dead"),

            /**
             * 'A' button on a gamepad
             */
            A("A"),

            /**
             * 'B' button on a gamepad
             */
            B("B"),

            /**
             * 'C' button on a gamepad
             */
            C("C"),

            /**
             * 'X' button on a gamepad
             */
            X("X"),

            /**
             * 'Y' button on a gamepad
             */
            Y("Y"),

            /**
             * 'Z' button on a gamepad
             */
            Z("Z"),

            /**
             * Left thumb button on a gamepad
             */
            LEFT_THUMB("Left Thumb"),

            /**
             * Right thumb button on a gamepad
             */
            RIGHT_THUMB("Right Thumb"),

            /**
             * Second left thumb button on a gamepad
             */
            LEFT_THUMB2("Left Thumb 2"),

            /**
             * Second right thumb button on a gamepad
             */
            RIGHT_THUMB2("Right Thumb 2"),

            /**
             * 'Select' button on a gamepad
             */
            SELECT("Select"),

            /**
             * 'Start' button on a gamepad
             */
            START("Start"),

            /**
             * 'Mode' button on a gamepad
             */
            MODE("Mode"),

            /**
             * Another left thumb button on a gamepad (how many thumbs do you have??)
             */
            LEFT_THUMB3("Left Thumb 3"),

            /**
             * Another right thumb button on a gamepad
             */
            RIGHT_THUMB3("Right Thumb 3"),

            /**
             * Digitiser pen tool button
             */
            TOOL_PEN("Pen"),

            /**
             * Digitiser rubber (eraser) tool button
             */
            TOOL_RUBBER("Rubber"),

            /**
             * Digitiser brush tool button
             */
            TOOL_BRUSH("Brush"),

            /**
             * Digitiser pencil tool button
             */
            TOOL_PENCIL("Pencil"),

            /**
             * Digitiser airbrush tool button
             */
            TOOL_AIRBRUSH("Airbrush"),

            /**
             * Digitiser finger tool button
             */
            TOOL_FINGER("Finger"),

            /**
             * Digitiser mouse tool button
             */
            TOOL_MOUSE("Mouse"),

            /**
             * Digitiser lens tool button
             */
            TOOL_LENS("Lens"),

            /**
             * Digitiser touch button
             */
            TOUCH("Touch"),

            /**
             * Digitiser stylus button
             */
            STYLUS("Stylus"),

            /**
             * Second digitiser stylus button
             */
            STYLUS2("Stylus 2"),

            /**
             * An unknown button
             */
            UNKNOWN("Unknown"),

            /**
             * Returns the back mouse button.
             */
            BACK("Back"),

            /**
             * Returns the extra mouse button.
             */
            EXTRA("Extra"),

            /**
             * Returns the forward mouse button.
             */
            FORWARD("Forward"),

            /**
             * The primary or leftmost mouse button.
             */
            LEFT("Left"),

            /**
             * Returns the middle mouse button, not present if the mouse has fewer than three buttons.
             */
            MIDDLE("Middle"),

            /**
             * The secondary or rightmost mouse button, not present if the mouse is a single-button mouse.
             */
            RIGHT("Right"),

            /**
             * Returns the side mouse button.
             */
            SIDE("Side"),

            /**
             * Extra, unnamed, buttons
             */
            EXTRA_1("Extra 1"),
            EXTRA_2("Extra 2"),
            EXTRA_3("Extra 3"),
            EXTRA_4("Extra 4"),
            EXTRA_5("Extra 5"),
            EXTRA_6("Extra 6"),
            EXTRA_7("Extra 7"),
            EXTRA_8("Extra 8"),
            EXTRA_9("Extra 9"),
            EXTRA_10("Extra 10"),
            EXTRA_11("Extra 11"),
            EXTRA_12("Extra 12"),
            EXTRA_13("Extra 13"),
            EXTRA_14("Extra 14"),
            EXTRA_15("Extra 15"),
            EXTRA_16("Extra 16"),
            EXTRA_17("Extra 17"),
            EXTRA_18("Extra 18"),
            EXTRA_19("Extra 19"),
            EXTRA_20("Extra 20"),
            EXTRA_21("Extra 21"),
            EXTRA_22("Extra 22"),
            EXTRA_23("Extra 23"),
            EXTRA_24("Extra 24"),
            EXTRA_25("Extra 25"),
            EXTRA_26("Extra 26"),
            EXTRA_27("Extra 27"),
            EXTRA_28("Extra 28"),
            EXTRA_29("Extra 29"),
            EXTRA_30("Extra 30"),
            EXTRA_31("Extra 31"),
            EXTRA_32("Extra 32"),
            EXTRA_33("Extra 33"),
            EXTRA_34("Extra 34"),
            EXTRA_35("Extra 35"),
            EXTRA_36("Extra 36"),
            EXTRA_37("Extra 37"),
            EXTRA_38("Extra 38"),
            EXTRA_39("Extra 39"),
            EXTRA_40("Extra 40");

            final String name;

            @Override
            public String getName() {
                return name;
            }

            Button(String name) {
                this.name = name;
            }
        }

        /**
         * KeyIDs for standard PC (LATIN-1) keyboards
         */
        enum Key implements Identifier {

            // Standard keyboard (LATIN-1) keys
            // UNIX X11 keysym values are listed to the right

            /** MS 0x00 UNIX 0xFFFFFF */
            VOID("Void"),
            /** MS 0x01 UNIX 0xFF1B */
            ESCAPE("Escape"),
            /** MS 0x02 UNIX 0x031 EXCLAM 0x021 */
            _1("1"),
            /** MS 0x03 UNIX 0x032 AT 0x040 */
            _2("2"),
            /** MS 0x04 UNIX 0x033 NUMBERSIGN 0x023 */
            _3("3"),
            /** MS 0x05 UNIX 0x034 DOLLAR 0x024 */
            _4("4"),
            /** MS 0x06 UNIX 0x035 PERCENT 0x025 */
            _5("5"),
            /** MS 0x07 UNIX 0x036 CIRCUMFLEX 0x05e */
            _6("6"),
            /** MS 0x08 UNIX 0x037 AMPERSAND 0x026 */
            _7("7"),
            /** MS 0x09 UNIX 0x038 ASTERISK 0x02a */
            _8("8"),
            /** MS 0x0A UNIX 0x039 PARENLEFT 0x028 */
            _9("9"),
            /** MS 0x0B UNIX 0x030 PARENRIGHT 0x029 */
            _0("0"),
            /** MS 0x0C UNIX 0x02d UNDERSCORE 0x05f */
            MINUS("-"),
            /** MS 0x0D UNIX 0x03d PLUS 0x02b */
            EQUALS("="),
            /** MS 0x0E UNIX 0xFF08 */
            BACK("Back"),
            /** MS 0x0F UNIX 0xFF09 */
            TAB("Tab"),
            /** MS 0x10 UNIX 0x071 UPPER 0x051 */
            Q("Q"),
            /** MS 0x11 UNIX 0x077 UPPER 0x057 */
            W("W"),
            /** MS 0x12 UNIX 0x065 UPPER 0x045 */
            E("E"),
            /** MS 0x13 UNIX 0x072 UPPER 0x052 */
            R("R"),
            /** MS 0x14 UNIX 0x074 UPPER 0x054 */
            T("T"),
            /** MS 0x15 UNIX 0x079 UPPER 0x059 */
            Y("Y"),
            /** MS 0x16 UNIX 0x075 UPPER 0x055 */
            U("U"),
            /** MS 0x17 UNIX 0x069 UPPER 0x049 */
            I("I"),
            /** MS 0x18 UNIX 0x06F UPPER 0x04F */
            O("O"),
            /** MS 0x19 UNIX 0x070 UPPER 0x050 */
            P("P"),
            /** MS 0x1A UNIX 0x05b BRACE 0x07b */
            LBRACKET("["),
            /** MS 0x1B UNIX 0x05d BRACE 0x07d */
            RBRACKET("]"),
            /** MS 0x1C UNIX 0xFF0D */
            RETURN("Return"),
            /** MS 0x1D UNIX 0xFFE3 */
            LCONTROL("Left Control"),
            /** MS 0x1E UNIX 0x061 UPPER 0x041 */
            A("A"),
            /** MS 0x1F UNIX 0x073 UPPER 0x053 */
            S("S"),
            /** MS 0x20 UNIX 0x064 UPPER 0x044 */
            D("D"),
            /** MS 0x21 UNIX 0x066 UPPER 0x046 */
            F("F"),
            /** MS 0x22 UNIX 0x067 UPPER 0x047 */
            G("G"),
            /** MS 0x23 UNIX 0x068 UPPER 0x048 */
            H("H"),
            /** MS 0x24 UNIX 0x06A UPPER 0x04A */
            J("J"),
            /** MS 0x25 UNIX 0x06B UPPER 0x04B */
            K("K"),
            /** MS 0x26 UNIX 0x06C UPPER 0x04C */
            L("L"),
            /** MS 0x27 UNIX 0x03b COLON 0x03a */
            SEMICOLON(";"),
            /** MS 0x28 UNIX 0x027 QUOTEDBL 0x022 */
            APOSTROPHE("'"),
            /** MS 0x29 UNIX 0x060 TILDE 0x07e */
            GRAVE("~"),
            /** MS 0x2A UNIX 0xFFE1 */
            LSHIFT("Left Shift"),
            /** MS 0x2B UNIX 0x05c BAR 0x07c */
            BACKSLASH("\\"),
            /** MS 0x2C UNIX 0x07A UPPER 0x05A */
            Z("Z"),
            /** MS 0x2D UNIX 0x078 UPPER 0x058 */
            X("X"),
            /** MS 0x2E UNIX 0x063 UPPER 0x043 */
            C("C"),
            /** MS 0x2F UNIX 0x076 UPPER 0x056 */
            V("V"),
            /** MS 0x30 UNIX 0x062 UPPER 0x042 */
            B("B"),
            /** MS 0x31 UNIX 0x06E UPPER 0x04E */
            N("N"),
            /** MS 0x32 UNIX 0x06D UPPER 0x04D */
            M("M"),
            /** MS 0x33 UNIX 0x02c LESS 0x03c */
            COMMA(","),
            /** MS 0x34 UNIX 0x02e GREATER 0x03e */
            PERIOD("."),
            /** MS 0x35 UNIX 0x02f QUESTION 0x03f */
            SLASH("/"),
            /** MS 0x36 UNIX 0xFFE2 */
            RSHIFT("Right Shift"),
            /** MS 0x37 UNIX 0xFFAA */
            MULTIPLY("Multiply"),
            /** MS 0x38 UNIX 0xFFE9 */
            LALT("Left Alt"),
            /** MS 0x39 UNIX 0x020 */
            SPACE(" "),
            /** MS 0x3A UNIX 0xFFE5 SHIFTLOCK 0xFFE6 */
            CAPITAL("Caps Lock"),
            /** MS 0x3B UNIX 0xFFBE */
            F1("F1"),
            /** MS 0x3C UNIX 0xFFBF */
            F2("F2"),
            /** MS 0x3D UNIX 0xFFC0 */
            F3("F3"),
            /** MS 0x3E UNIX 0xFFC1 */
            F4("F4"),
            /** MS 0x3F UNIX 0xFFC2 */
            F5("F5"),
            /** MS 0x40 UNIX 0xFFC3 */
            F6("F6"),
            /** MS 0x41 UNIX 0xFFC4 */
            F7("F7"),
            /** MS 0x42 UNIX 0xFFC5 */
            F8("F8"),
            /** MS 0x43 UNIX 0xFFC6 */
            F9("F9"),
            /** MS 0x44 UNIX 0xFFC7 */
            F10("F10"),
            /** MS 0x45 UNIX 0xFF7F */
            NUMLOCK("Num Lock"),
            /** MS 0x46 UNIX 0xFF14 */
            SCROLL("Scroll Lock"),
            /** MS 0x47 UNIX 0xFFB7 HOME 0xFF95 */
            NUMPAD7("Num 7"),
            /** MS 0x48 UNIX 0xFFB8 UP 0xFF97 */
            NUMPAD8("Num 8"),
            /** MS 0x49 UNIX 0xFFB9 PRIOR 0xFF9A */
            NUMPAD9("Num 9"),
            /** MS 0x4A UNIX 0xFFAD */
            SUBTRACT("Num -"),
            /** MS 0x4B UNIX 0xFFB4 LEFT 0xFF96 */
            NUMPAD4("Num 4"),
            /** MS 0x4C UNIX 0xFFB5 */
            NUMPAD5("Num 5"),
            /** MS 0x4D UNIX 0xFFB6 RIGHT 0xFF98 */
            NUMPAD6("Num 6"),
            /** MS 0x4E UNIX 0xFFAB */
            ADD("Num +"),
            /** MS 0x4F UNIX 0xFFB1 END 0xFF9C */
            NUMPAD1("Num 1"),
            /** MS 0x50 UNIX 0xFFB2 DOWN 0xFF99 */
            NUMPAD2("Num 2"),
            /** MS 0x51 UNIX 0xFFB3 NEXT 0xFF9B */
            NUMPAD3("Num 3"),
            /** MS 0x52 UNIX 0xFFB0 INSERT 0xFF9E */
            NUMPAD0("Num 0"),
            /** MS 0x53 UNIX 0xFFAE DELETE 0xFF9F */
            DECIMAL("Num ."),
            /** MS 0x57 UNIX 0xFFC8 */
            F11("F11"),
            /** MS 0x58 UNIX 0xFFC9 */
            F12("F12"),
            /** MS 0x64 UNIX 0xFFCA */
            F13("F13"),
            /** MS 0x65 UNIX 0xFFCB */
            F14("F14"),
            /** MS 0x66 UNIX 0xFFCC */
            F15("F15"),
            /** MS 0x70 UNIX 0xFF2D */
            KANA("Kana"),
            /** MS 0x79 Japanese keyboard */
            CONVERT("Convert"),
            /** MS 0x7B Japanese keyboard */
            NOCONVERT("Noconvert"),
            /** MS 0x7D UNIX 0x0a5 */
            YEN("Yen"),
            /** MS 0x8D UNIX 0xFFBD */
            NUMPADEQUAL("Num ="),
            /** MS 0x90 Japanese keyboard */
            CIRCUMFLEX("Circumflex"),
            /** MS 0x91 UNIX 0x040 */
            AT("At"),
            /** MS 0x92 UNIX 0x03a */
            COLON("Colon"),
            /** MS 0x93 NEC PC98 */
            UNDERLINE("Underline"),
            /** MS 0x94 UNIX 0xFF21 */
            KANJI("Kanji"),
            /** MS 0x95 UNIX 0xFF69 */
            STOP("Stop"),
            /** MS 0x96 Japan AX */
            AX("Ax"),
            /** MS 0x97 J3100 */
            UNLABELED("Unlabeled"),
            /** MS 0x9C UNIX 0xFF8D */
            NUMPADENTER("Num Enter"),
            /** MS 0x9D UNIX 0xFFE4 */
            RCONTROL("Right Control"),
            /** MS 0xB3 UNIX 0xFFAC */
            NUMPADCOMMA("Num ,"),
            /** MS 0xB5 UNIX 0xFFAF */
            DIVIDE("Num /"),
            /** MS 0xB7 UNIX 0xFF15 PRINT 0xFF61 */
            SYSRQ("SysRq"),
            /** MS 0xB8 UNIX 0xFFEA */
            RALT("Right Alt"),
            /** MS 0xC5 UNIX 0xFF13 BREAK 0xFF6B */
            PAUSE("Pause"),
            /** MS 0xC7 UNIX 0xFF50 */
            HOME("Home"),
            /** MS 0xC8 UNIX 0xFF52 */
            UP("Up"),
            /** MS 0xC9 UNIX 0xFF55 */
            PAGEUP("Pg Up"),
            /** MS 0xCB UNIX 0xFF51 */
            LEFT("Left"),
            /** MS 0xCD UNIX 0xFF53 */
            RIGHT("Right"),
            /** MS 0xCF UNIX 0xFF57 */
            END("End"),
            /** MS 0xD0 UNIX 0xFF54 */
            DOWN("Down"),
            /** MS 0xD1 UNIX 0xFF56 */
            PAGEDOWN("Pg Down"),
            /** MS 0xD2 UNIX 0xFF63 */
            INSERT("Insert"),
            /** MS 0xD3 UNIX 0xFFFF */
            DELETE("Delete"),
            /** MS 0xDB UNIX META 0xFFE7 SUPER 0xFFEB HYPER 0xFFED */
            LWIN("Left Windows"),
            /** MS 0xDC UNIX META 0xFFE8 SUPER 0xFFEC HYPER 0xFFEE */
            RWIN("Right Windows"),
            /** MS 0xDD UNIX 0xFF67 */
            APPS("Apps"),
            /** MS 0xDE Sun 0x1005FF76 SHIFT 0x1005FF7D */
            POWER("Power"),
            /** MS 0xDF No UNIX keysym */
            SLEEP("Sleep"),
            UNKNOWN("Unknown");

            final String name;

            @Override
            public String getName() {
                return name;
            }

            /**
             * Protected constructor
             */
            Key(String name) {
                this.name = name;
            }
        }
    }

    /**
     * POV enum for different positions.
     */
    class POV {

        /**
         * Standard value for center HAT position
         */
        public static final float OFF = 0.0f;
        /**
         * Synonmous with OFF
         */
        public static final float CENTER = OFF;
        /**
         * Standard value for up-left HAT position
         */
        public static final float UP_LEFT = 0.125f;
        /**
         * Standard value for up HAT position
         */
        public static final float UP = 0.25f;
        /**
         * Standard value for up-right HAT position
         */
        public static final float UP_RIGHT = 0.375f;
        /**
         * Standard value for right HAT position
         */
        public static final float RIGHT = 0.50f;
        /**
         * Standard value for down-right HAT position
         */
        public static final float DOWN_RIGHT = 0.625f;
        /**
         * Standard value for down HAT position
         */
        public static final float DOWN = 0.75f;
        /**
         * Standard value for down-left HAT position
         */
        public static final float DOWN_LEFT = 0.875f;
        /**
         * Standard value for left HAT position
         */
        public static final float LEFT = 1.0f;
    }
}
