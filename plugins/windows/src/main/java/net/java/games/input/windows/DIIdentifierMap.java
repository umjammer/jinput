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

package net.java.games.input.windows;

import net.java.games.input.Component;


/**
 * @author elias
 * @version 1.0
 */
final class DIIdentifierMap {

    public final static int DIK_ESCAPE = 0x01;
    public final static int DIK_1 = 0x02;
    public final static int DIK_2 = 0x03;
    public final static int DIK_3 = 0x04;
    public final static int DIK_4 = 0x05;
    public final static int DIK_5 = 0x06;
    public final static int DIK_6 = 0x07;
    public final static int DIK_7 = 0x08;
    public final static int DIK_8 = 0x09;
    public final static int DIK_9 = 0x0A;
    public final static int DIK_0 = 0x0B;
    /** - on main keyboard */
    public final static int DIK_MINUS = 0x0C;
    public final static int DIK_EQUALS = 0x0D;
    /** backspace */
    public final static int DIK_BACK = 0x0E;
    public final static int DIK_TAB = 0x0F;
    public final static int DIK_Q = 0x10;
    public final static int DIK_W = 0x11;
    public final static int DIK_E = 0x12;
    public final static int DIK_R = 0x13;
    public final static int DIK_T = 0x14;
    public final static int DIK_Y = 0x15;
    public final static int DIK_U = 0x16;
    public final static int DIK_I = 0x17;
    public final static int DIK_O = 0x18;
    public final static int DIK_P = 0x19;
    public final static int DIK_LBRACKET = 0x1A;
    public final static int DIK_RBRACKET = 0x1B;
    /** Enter on main keyboard */
    public final static int DIK_RETURN = 0x1C;
    public final static int DIK_LCONTROL = 0x1D;
    public final static int DIK_A = 0x1E;
    public final static int DIK_S = 0x1F;
    public final static int DIK_D = 0x20;
    public final static int DIK_F = 0x21;
    public final static int DIK_G = 0x22;
    public final static int DIK_H = 0x23;
    public final static int DIK_J = 0x24;
    public final static int DIK_K = 0x25;
    public final static int DIK_L = 0x26;
    public final static int DIK_SEMICOLON = 0x27;
    public final static int DIK_APOSTROPHE = 0x28;
    /** accent grave */
    public final static int DIK_GRAVE = 0x29;
    public final static int DIK_LSHIFT = 0x2A;
    public final static int DIK_BACKSLASH = 0x2B;
    public final static int DIK_Z = 0x2C;
    public final static int DIK_X = 0x2D;
    public final static int DIK_C = 0x2E;
    public final static int DIK_V = 0x2F;
    public final static int DIK_B = 0x30;
    public final static int DIK_N = 0x31;
    public final static int DIK_M = 0x32;
    public final static int DIK_COMMA = 0x33;
    /** . on main keyboard */
    public final static int DIK_PERIOD = 0x34;
    /** / on main keyboard */
    public final static int DIK_SLASH = 0x35;
    public final static int DIK_RSHIFT = 0x36;
    /** * on numeric keypad */
    public final static int DIK_MULTIPLY = 0x37;
    /** left Alt */
    public final static int DIK_LMENU = 0x38;
    public final static int DIK_SPACE = 0x39;
    public final static int DIK_CAPITAL = 0x3A;
    public final static int DIK_F1 = 0x3B;
    public final static int DIK_F2 = 0x3C;
    public final static int DIK_F3 = 0x3D;
    public final static int DIK_F4 = 0x3E;
    public final static int DIK_F5 = 0x3F;
    public final static int DIK_F6 = 0x40;
    public final static int DIK_F7 = 0x41;
    public final static int DIK_F8 = 0x42;
    public final static int DIK_F9 = 0x43;
    public final static int DIK_F10 = 0x44;
    public final static int DIK_NUMLOCK = 0x45;
    public final static int DIK_SCROLL = 0x46;    /* Scroll Lock */
    public final static int DIK_NUMPAD7 = 0x47;
    public final static int DIK_NUMPAD8 = 0x48;
    public final static int DIK_NUMPAD9 = 0x49;
    public final static int DIK_SUBTRACT = 0x4A;    /* - on numeric keypad */
    public final static int DIK_NUMPAD4 = 0x4B;
    public final static int DIK_NUMPAD5 = 0x4C;
    public final static int DIK_NUMPAD6 = 0x4D;
    public final static int DIK_ADD = 0x4E;    /* + on numeric keypad */
    public final static int DIK_NUMPAD1 = 0x4F;
    public final static int DIK_NUMPAD2 = 0x50;
    public final static int DIK_NUMPAD3 = 0x51;
    public final static int DIK_NUMPAD0 = 0x52;
    public final static int DIK_DECIMAL = 0x53;    /* . on numeric keypad */
    public final static int DIK_OEM_102 = 0x56;    /* <> or \| on RT 102-key keyboard (Non-U.S.) */
    public final static int DIK_F11 = 0x57;
    public final static int DIK_F12 = 0x58;
    public final static int DIK_F13 = 0x64;    /*					 (NEC PC98) */
    public final static int DIK_F14 = 0x65;    /*					 (NEC PC98) */
    public final static int DIK_F15 = 0x66;    /*					 (NEC PC98) */
    public final static int DIK_KANA = 0x70;    /* (Japanese keyboard)			*/
    public final static int DIK_ABNT_C1 = 0x73;    /* /? on Brazilian keyboard */
    public final static int DIK_CONVERT = 0x79;    /* (Japanese keyboard)			*/
    public final static int DIK_NOCONVERT = 0x7B;    /* (Japanese keyboard)			*/
    public final static int DIK_YEN = 0x7D;    /* (Japanese keyboard)			*/
    public final static int DIK_ABNT_C2 = 0x7E;    /* Numpad . on Brazilian keyboard */
    public final static int DIK_NUMPADEQUALS = 0x8D;    /* = on numeric keypad (NEC PC98) */
    public final static int DIK_PREVTRACK = 0x90;    /* Previous Track (DIK_CIRCUMFLEX on Japanese keyboard) */
    public final static int DIK_AT = 0x91;    /*					 (NEC PC98) */
    public final static int DIK_COLON = 0x92;    /*					 (NEC PC98) */
    public final static int DIK_UNDERLINE = 0x93;    /*					 (NEC PC98) */
    public final static int DIK_KANJI = 0x94;    /* (Japanese keyboard)			*/
    public final static int DIK_STOP = 0x95;    /*					 (NEC PC98) */
    public final static int DIK_AX = 0x96;    /*					 (Japan AX) */
    public final static int DIK_UNLABELED = 0x97;    /*						(J3100) */
    public final static int DIK_NEXTTRACK = 0x99;    /* Next Track */
    public final static int DIK_NUMPADENTER = 0x9C;    /* Enter on numeric keypad */
    public final static int DIK_RCONTROL = 0x9D;
    public final static int DIK_MUTE = 0xA0;    /* Mute */
    public final static int DIK_CALCULATOR = 0xA1;    /* Calculator */
    public final static int DIK_PLAYPAUSE = 0xA2;    /* Play / Pause */
    public final static int DIK_MEDIASTOP = 0xA4;    /* Media Stop */
    public final static int DIK_VOLUMEDOWN = 0xAE;    /* Volume - */
    public final static int DIK_VOLUMEUP = 0xB0;    /* Volume + */
    public final static int DIK_WEBHOME = 0xB2;    /* Web home */
    public final static int DIK_NUMPADCOMMA = 0xB3;    /* , on numeric keypad (NEC PC98) */
    public final static int DIK_DIVIDE = 0xB5;    /* / on numeric keypad */
    public final static int DIK_SYSRQ = 0xB7;
    public final static int DIK_RMENU = 0xB8;    /* right Alt */
    public final static int DIK_PAUSE = 0xC5;    /* Pause */
    public final static int DIK_HOME = 0xC7;    /* Home on arrow keypad */
    public final static int DIK_UP = 0xC8;    /* UpArrow on arrow keypad */
    public final static int DIK_PRIOR = 0xC9;    /* PgUp on arrow keypad */
    public final static int DIK_LEFT = 0xCB;    /* LeftArrow on arrow keypad */
    public final static int DIK_RIGHT = 0xCD;    /* RightArrow on arrow keypad */
    public final static int DIK_END = 0xCF;    /* End on arrow keypad */
    public final static int DIK_DOWN = 0xD0;    /* DownArrow on arrow keypad */
    public final static int DIK_NEXT = 0xD1;    /* PgDn on arrow keypad */
    public final static int DIK_INSERT = 0xD2;    /* Insert on arrow keypad */
    public final static int DIK_DELETE = 0xD3;    /* Delete on arrow keypad */
    public final static int DIK_LWIN = 0xDB;    /* Left Windows key */
    public final static int DIK_RWIN = 0xDC;    /* Right Windows key */
    public final static int DIK_APPS = 0xDD;    /* AppMenu key */
    public final static int DIK_POWER = 0xDE;    /* System Power */
    public final static int DIK_SLEEP = 0xDF;    /* System Sleep */
    public final static int DIK_WAKE = 0xE3;    /* System Wake */
    public final static int DIK_WEBSEARCH = 0xE5;    /* Web Search */
    public final static int DIK_WEBFAVORITES = 0xE6;    /* Web Favorites */
    public final static int DIK_WEBREFRESH = 0xE7;    /* Web Refresh */
    public final static int DIK_WEBSTOP = 0xE8;    /* Web Stop */
    public final static int DIK_WEBFORWARD = 0xE9;    /* Web Forward */
    public final static int DIK_WEBBACK = 0xEA;    /* Web Back */
    public final static int DIK_MYCOMPUTER = 0xEB;    /* My Computer */
    public final static int DIK_MAIL = 0xEC;    /* Mail */
    public final static int DIK_MEDIASELECT = 0xED;    /* Media Select */

    public static Component.Identifier.Key getKeyIdentifier(int keyCode) {
        return switch (keyCode) {
            case DIK_ESCAPE -> Component.Identifier.Key.ESCAPE;
            case DIK_1 -> Component.Identifier.Key._1;
            case DIK_2 -> Component.Identifier.Key._2;
            case DIK_3 -> Component.Identifier.Key._3;
            case DIK_4 -> Component.Identifier.Key._4;
            case DIK_5 -> Component.Identifier.Key._5;
            case DIK_6 -> Component.Identifier.Key._6;
            case DIK_7 -> Component.Identifier.Key._7;
            case DIK_8 -> Component.Identifier.Key._8;
            case DIK_9 -> Component.Identifier.Key._9;
            case DIK_0 -> Component.Identifier.Key._0;
            case DIK_MINUS -> Component.Identifier.Key.MINUS;
            case DIK_EQUALS -> Component.Identifier.Key.EQUALS;
            case DIK_BACK -> Component.Identifier.Key.BACK;
            case DIK_TAB -> Component.Identifier.Key.TAB;
            case DIK_Q -> Component.Identifier.Key.Q;
            case DIK_W -> Component.Identifier.Key.W;
            case DIK_E -> Component.Identifier.Key.E;
            case DIK_R -> Component.Identifier.Key.R;
            case DIK_T -> Component.Identifier.Key.T;
            case DIK_Y -> Component.Identifier.Key.Y;
            case DIK_U -> Component.Identifier.Key.U;
            case DIK_I -> Component.Identifier.Key.I;
            case DIK_O -> Component.Identifier.Key.O;
            case DIK_P -> Component.Identifier.Key.P;
            case DIK_LBRACKET -> Component.Identifier.Key.LBRACKET;
            case DIK_RBRACKET -> Component.Identifier.Key.RBRACKET;
            case DIK_RETURN -> Component.Identifier.Key.RETURN;
            case DIK_LCONTROL -> Component.Identifier.Key.LCONTROL;
            case DIK_A -> Component.Identifier.Key.A;
            case DIK_S -> Component.Identifier.Key.S;
            case DIK_D -> Component.Identifier.Key.D;
            case DIK_F -> Component.Identifier.Key.F;
            case DIK_G -> Component.Identifier.Key.G;
            case DIK_H -> Component.Identifier.Key.H;
            case DIK_J -> Component.Identifier.Key.J;
            case DIK_K -> Component.Identifier.Key.K;
            case DIK_L -> Component.Identifier.Key.L;
            case DIK_SEMICOLON -> Component.Identifier.Key.SEMICOLON;
            case DIK_APOSTROPHE -> Component.Identifier.Key.APOSTROPHE;
            case DIK_GRAVE -> Component.Identifier.Key.GRAVE;
            case DIK_LSHIFT -> Component.Identifier.Key.LSHIFT;
            case DIK_BACKSLASH -> Component.Identifier.Key.BACKSLASH;
            case DIK_Z -> Component.Identifier.Key.Z;
            case DIK_X -> Component.Identifier.Key.X;
            case DIK_C -> Component.Identifier.Key.C;
            case DIK_V -> Component.Identifier.Key.V;
            case DIK_B -> Component.Identifier.Key.B;
            case DIK_N -> Component.Identifier.Key.N;
            case DIK_M -> Component.Identifier.Key.M;
            case DIK_COMMA -> Component.Identifier.Key.COMMA;
            case DIK_PERIOD -> Component.Identifier.Key.PERIOD;
            case DIK_SLASH -> Component.Identifier.Key.SLASH;
            case DIK_RSHIFT -> Component.Identifier.Key.RSHIFT;
            case DIK_MULTIPLY -> Component.Identifier.Key.MULTIPLY;
            case DIK_LMENU -> Component.Identifier.Key.LALT;
            case DIK_SPACE -> Component.Identifier.Key.SPACE;
            case DIK_CAPITAL -> Component.Identifier.Key.CAPITAL;
            case DIK_F1 -> Component.Identifier.Key.F1;
            case DIK_F2 -> Component.Identifier.Key.F2;
            case DIK_F3 -> Component.Identifier.Key.F3;
            case DIK_F4 -> Component.Identifier.Key.F4;
            case DIK_F5 -> Component.Identifier.Key.F5;
            case DIK_F6 -> Component.Identifier.Key.F6;
            case DIK_F7 -> Component.Identifier.Key.F7;
            case DIK_F8 -> Component.Identifier.Key.F8;
            case DIK_F9 -> Component.Identifier.Key.F9;
            case DIK_F10 -> Component.Identifier.Key.F10;
            case DIK_NUMLOCK -> Component.Identifier.Key.NUMLOCK;
            case DIK_SCROLL -> Component.Identifier.Key.SCROLL;
            case DIK_NUMPAD7 -> Component.Identifier.Key.NUMPAD7;
            case DIK_NUMPAD8 -> Component.Identifier.Key.NUMPAD8;
            case DIK_NUMPAD9 -> Component.Identifier.Key.NUMPAD9;
            case DIK_SUBTRACT -> Component.Identifier.Key.SUBTRACT;
            case DIK_NUMPAD4 -> Component.Identifier.Key.NUMPAD4;
            case DIK_NUMPAD5 -> Component.Identifier.Key.NUMPAD5;
            case DIK_NUMPAD6 -> Component.Identifier.Key.NUMPAD6;
            case DIK_ADD -> Component.Identifier.Key.ADD;
            case DIK_NUMPAD1 -> Component.Identifier.Key.NUMPAD1;
            case DIK_NUMPAD2 -> Component.Identifier.Key.NUMPAD2;
            case DIK_NUMPAD3 -> Component.Identifier.Key.NUMPAD3;
            case DIK_NUMPAD0 -> Component.Identifier.Key.NUMPAD0;
            case DIK_DECIMAL -> Component.Identifier.Key.DECIMAL;
            case DIK_F11 -> Component.Identifier.Key.F11;
            case DIK_F12 -> Component.Identifier.Key.F12;
            case DIK_F13 -> Component.Identifier.Key.F13;
            case DIK_F14 -> Component.Identifier.Key.F14;
            case DIK_F15 -> Component.Identifier.Key.F15;
            case DIK_KANA -> Component.Identifier.Key.KANA;
            case DIK_CONVERT -> Component.Identifier.Key.CONVERT;
            case DIK_NOCONVERT -> Component.Identifier.Key.NOCONVERT;
            case DIK_YEN -> Component.Identifier.Key.YEN;
            case DIK_NUMPADEQUALS -> Component.Identifier.Key.NUMPADEQUAL;
            case DIK_AT -> Component.Identifier.Key.AT;
            case DIK_COLON -> Component.Identifier.Key.COLON;
            case DIK_UNDERLINE -> Component.Identifier.Key.UNDERLINE;
            case DIK_KANJI -> Component.Identifier.Key.KANJI;
            case DIK_STOP -> Component.Identifier.Key.STOP;
            case DIK_AX -> Component.Identifier.Key.AX;
            case DIK_UNLABELED -> Component.Identifier.Key.UNLABELED;
            case DIK_NUMPADENTER -> Component.Identifier.Key.NUMPADENTER;
            case DIK_RCONTROL -> Component.Identifier.Key.RCONTROL;
            case DIK_NUMPADCOMMA -> Component.Identifier.Key.NUMPADCOMMA;
            case DIK_DIVIDE -> Component.Identifier.Key.DIVIDE;
            case DIK_SYSRQ -> Component.Identifier.Key.SYSRQ;
            case DIK_RMENU -> Component.Identifier.Key.RALT;
            case DIK_PAUSE -> Component.Identifier.Key.PAUSE;
            case DIK_HOME -> Component.Identifier.Key.HOME;
            case DIK_UP -> Component.Identifier.Key.UP;
            case DIK_PRIOR -> Component.Identifier.Key.PAGEUP;
            case DIK_LEFT -> Component.Identifier.Key.LEFT;
            case DIK_RIGHT -> Component.Identifier.Key.RIGHT;
            case DIK_END -> Component.Identifier.Key.END;
            case DIK_DOWN -> Component.Identifier.Key.DOWN;
            case DIK_NEXT -> Component.Identifier.Key.PAGEDOWN;
            case DIK_INSERT -> Component.Identifier.Key.INSERT;
            case DIK_DELETE -> Component.Identifier.Key.DELETE;
            case DIK_LWIN -> Component.Identifier.Key.LWIN;
            case DIK_RWIN -> Component.Identifier.Key.RWIN;
            case DIK_APPS -> Component.Identifier.Key.APPS;
            case DIK_POWER -> Component.Identifier.Key.POWER;
            case DIK_SLEEP -> Component.Identifier.Key.SLEEP;
            /* Unassigned keys */
            default -> Component.Identifier.Key.UNKNOWN;
        };
    }

    public static Component.Identifier.Button getButtonIdentifier(int id) {
        return switch (id) {
            case 0 -> Component.Identifier.Button._0;
            case 1 -> Component.Identifier.Button._1;
            case 2 -> Component.Identifier.Button._2;
            case 3 -> Component.Identifier.Button._3;
            case 4 -> Component.Identifier.Button._4;
            case 5 -> Component.Identifier.Button._5;
            case 6 -> Component.Identifier.Button._6;
            case 7 -> Component.Identifier.Button._7;
            case 8 -> Component.Identifier.Button._8;
            case 9 -> Component.Identifier.Button._9;
            case 10 -> Component.Identifier.Button._10;
            case 11 -> Component.Identifier.Button._11;
            case 12 -> Component.Identifier.Button._12;
            case 13 -> Component.Identifier.Button._13;
            case 14 -> Component.Identifier.Button._14;
            case 15 -> Component.Identifier.Button._15;
            case 16 -> Component.Identifier.Button._16;
            case 17 -> Component.Identifier.Button._17;
            case 18 -> Component.Identifier.Button._18;
            case 19 -> Component.Identifier.Button._19;
            case 20 -> Component.Identifier.Button._20;
            case 21 -> Component.Identifier.Button._21;
            case 22 -> Component.Identifier.Button._22;
            case 23 -> Component.Identifier.Button._23;
            case 24 -> Component.Identifier.Button._24;
            case 25 -> Component.Identifier.Button._25;
            case 26 -> Component.Identifier.Button._26;
            case 27 -> Component.Identifier.Button._27;
            case 28 -> Component.Identifier.Button._28;
            case 29 -> Component.Identifier.Button._29;
            case 30 -> Component.Identifier.Button._30;
            case 31 -> Component.Identifier.Button._31;
            default -> null;
        };
    }

    public static Component.Identifier.Button mapMouseButtonIdentifier(Component.Identifier.Button buttonId) {
        if (buttonId == Component.Identifier.Button._0) {
            return Component.Identifier.Button.LEFT;
        } else if (buttonId == Component.Identifier.Button._1) {
            return Component.Identifier.Button.RIGHT;
        } else if (buttonId == Component.Identifier.Button._2) {
            return Component.Identifier.Button.MIDDLE;
        } else
            return buttonId;
    }
}
