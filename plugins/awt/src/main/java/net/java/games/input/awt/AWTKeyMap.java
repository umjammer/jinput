/**
 * Copyright (C) 2004 Jeremy Booth (jeremy@newdawnsoftware.com)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

package net.java.games.input.awt;

import java.awt.event.KeyEvent;

import net.java.games.input.Component;


/**
 * @author Jeremy
 * @author elias
 */
final class AWTKeyMap {

    public static Component.Identifier.Key mapKeyCode(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_0 -> Component.Identifier.Key._0;
            case KeyEvent.VK_1 -> Component.Identifier.Key._1;
            case KeyEvent.VK_2 -> Component.Identifier.Key._2;
            case KeyEvent.VK_3 -> Component.Identifier.Key._3;
            case KeyEvent.VK_4 -> Component.Identifier.Key._4;
            case KeyEvent.VK_5 -> Component.Identifier.Key._5;
            case KeyEvent.VK_6 -> Component.Identifier.Key._6;
            case KeyEvent.VK_7 -> Component.Identifier.Key._7;
            case KeyEvent.VK_8 -> Component.Identifier.Key._8;
            case KeyEvent.VK_9 -> Component.Identifier.Key._9;
            case KeyEvent.VK_Q -> Component.Identifier.Key.Q;
            case KeyEvent.VK_W -> Component.Identifier.Key.W;
            case KeyEvent.VK_E -> Component.Identifier.Key.E;
            case KeyEvent.VK_R -> Component.Identifier.Key.R;
            case KeyEvent.VK_T -> Component.Identifier.Key.T;
            case KeyEvent.VK_Y -> Component.Identifier.Key.Y;
            case KeyEvent.VK_U -> Component.Identifier.Key.U;
            case KeyEvent.VK_I -> Component.Identifier.Key.I;
            case KeyEvent.VK_O -> Component.Identifier.Key.O;
            case KeyEvent.VK_P -> Component.Identifier.Key.P;
            case KeyEvent.VK_A -> Component.Identifier.Key.A;
            case KeyEvent.VK_S -> Component.Identifier.Key.S;
            case KeyEvent.VK_D -> Component.Identifier.Key.D;
            case KeyEvent.VK_F -> Component.Identifier.Key.F;
            case KeyEvent.VK_G -> Component.Identifier.Key.G;
            case KeyEvent.VK_H -> Component.Identifier.Key.H;
            case KeyEvent.VK_J -> Component.Identifier.Key.J;
            case KeyEvent.VK_K -> Component.Identifier.Key.K;
            case KeyEvent.VK_L -> Component.Identifier.Key.L;
            case KeyEvent.VK_Z -> Component.Identifier.Key.Z;
            case KeyEvent.VK_X -> Component.Identifier.Key.X;
            case KeyEvent.VK_C -> Component.Identifier.Key.C;
            case KeyEvent.VK_V -> Component.Identifier.Key.V;
            case KeyEvent.VK_B -> Component.Identifier.Key.B;
            case KeyEvent.VK_N -> Component.Identifier.Key.N;
            case KeyEvent.VK_M -> Component.Identifier.Key.M;
            case KeyEvent.VK_F1 -> Component.Identifier.Key.F1;
            case KeyEvent.VK_F2 -> Component.Identifier.Key.F2;
            case KeyEvent.VK_F3 -> Component.Identifier.Key.F3;
            case KeyEvent.VK_F4 -> Component.Identifier.Key.F4;
            case KeyEvent.VK_F5 -> Component.Identifier.Key.F5;
            case KeyEvent.VK_F6 -> Component.Identifier.Key.F6;
            case KeyEvent.VK_F7 -> Component.Identifier.Key.F7;
            case KeyEvent.VK_F8 -> Component.Identifier.Key.F8;
            case KeyEvent.VK_F9 -> Component.Identifier.Key.F9;
            case KeyEvent.VK_F10 -> Component.Identifier.Key.F10;
            case KeyEvent.VK_F11 -> Component.Identifier.Key.F11;
            case KeyEvent.VK_F12 -> Component.Identifier.Key.F12;
            case KeyEvent.VK_ESCAPE -> Component.Identifier.Key.ESCAPE;
            case KeyEvent.VK_MINUS -> Component.Identifier.Key.MINUS;
            case KeyEvent.VK_EQUALS -> Component.Identifier.Key.EQUALS;
            case KeyEvent.VK_BACK_SPACE -> Component.Identifier.Key.BACKSLASH;
            case KeyEvent.VK_TAB -> Component.Identifier.Key.TAB;
            case KeyEvent.VK_OPEN_BRACKET -> Component.Identifier.Key.LBRACKET;
            case KeyEvent.VK_CLOSE_BRACKET -> Component.Identifier.Key.RBRACKET;
            case KeyEvent.VK_SEMICOLON -> Component.Identifier.Key.SEMICOLON;
            case KeyEvent.VK_QUOTE -> Component.Identifier.Key.APOSTROPHE;
            case KeyEvent.VK_NUMBER_SIGN -> Component.Identifier.Key.GRAVE;
            case KeyEvent.VK_BACK_SLASH -> Component.Identifier.Key.BACKSLASH;
            case KeyEvent.VK_PERIOD -> Component.Identifier.Key.PERIOD;
            case KeyEvent.VK_SLASH -> Component.Identifier.Key.SLASH;
            case KeyEvent.VK_MULTIPLY -> Component.Identifier.Key.MULTIPLY;
            case KeyEvent.VK_SPACE -> Component.Identifier.Key.SPACE;
            case KeyEvent.VK_CAPS_LOCK -> Component.Identifier.Key.CAPITAL;
            case KeyEvent.VK_NUM_LOCK -> Component.Identifier.Key.NUMLOCK;
            case KeyEvent.VK_SCROLL_LOCK -> Component.Identifier.Key.SCROLL;
            case KeyEvent.VK_NUMPAD7 -> Component.Identifier.Key.NUMPAD7;
            case KeyEvent.VK_NUMPAD8 -> Component.Identifier.Key.NUMPAD8;
            case KeyEvent.VK_NUMPAD9 -> Component.Identifier.Key.NUMPAD9;
            case KeyEvent.VK_SUBTRACT -> Component.Identifier.Key.SUBTRACT;
            case KeyEvent.VK_NUMPAD4 -> Component.Identifier.Key.NUMPAD4;
            case KeyEvent.VK_NUMPAD5 -> Component.Identifier.Key.NUMPAD5;
            case KeyEvent.VK_NUMPAD6 -> Component.Identifier.Key.NUMPAD6;
            case KeyEvent.VK_ADD -> Component.Identifier.Key.ADD;
            case KeyEvent.VK_NUMPAD1 -> Component.Identifier.Key.NUMPAD1;
            case KeyEvent.VK_NUMPAD2 -> Component.Identifier.Key.NUMPAD2;
            case KeyEvent.VK_NUMPAD3 -> Component.Identifier.Key.NUMPAD3;
            case KeyEvent.VK_NUMPAD0 -> Component.Identifier.Key.NUMPAD0;
            case KeyEvent.VK_DECIMAL -> Component.Identifier.Key.DECIMAL;
            case KeyEvent.VK_KANA -> Component.Identifier.Key.KANA;
            case KeyEvent.VK_CONVERT -> Component.Identifier.Key.CONVERT;
            case KeyEvent.VK_NONCONVERT -> Component.Identifier.Key.NOCONVERT;
            case KeyEvent.VK_CIRCUMFLEX -> Component.Identifier.Key.CIRCUMFLEX;
            case KeyEvent.VK_AT -> Component.Identifier.Key.AT;
            case KeyEvent.VK_COLON -> Component.Identifier.Key.COLON;
            case KeyEvent.VK_UNDERSCORE -> Component.Identifier.Key.UNDERLINE;
            case KeyEvent.VK_KANJI -> Component.Identifier.Key.KANJI;
            case KeyEvent.VK_STOP -> Component.Identifier.Key.STOP;
            case KeyEvent.VK_DIVIDE -> Component.Identifier.Key.DIVIDE;
            case KeyEvent.VK_PAUSE -> Component.Identifier.Key.PAUSE;
            case KeyEvent.VK_HOME -> Component.Identifier.Key.HOME;
            case KeyEvent.VK_UP -> Component.Identifier.Key.UP;
            case KeyEvent.VK_PAGE_UP -> Component.Identifier.Key.PAGEUP;
            case KeyEvent.VK_LEFT -> Component.Identifier.Key.LEFT;
            case KeyEvent.VK_RIGHT -> Component.Identifier.Key.RIGHT;
            case KeyEvent.VK_END -> Component.Identifier.Key.END;
            case KeyEvent.VK_DOWN -> Component.Identifier.Key.DOWN;
            case KeyEvent.VK_PAGE_DOWN -> Component.Identifier.Key.PAGEDOWN;
            case KeyEvent.VK_INSERT -> Component.Identifier.Key.INSERT;
            case KeyEvent.VK_DELETE -> Component.Identifier.Key.DELETE;
            default -> Component.Identifier.Key.UNKNOWN;
        };
    }

    public static Component.Identifier.Key map(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int keyLocation = event.getKeyLocation();
        switch (keyCode) {
        case KeyEvent.VK_CONTROL:
            if (keyLocation == KeyEvent.KEY_LOCATION_RIGHT)
                return Component.Identifier.Key.RCONTROL;
            else
                return Component.Identifier.Key.LCONTROL;
        case KeyEvent.VK_SHIFT:
            if (keyLocation == KeyEvent.KEY_LOCATION_RIGHT)
                return Component.Identifier.Key.RSHIFT;
            else
                return Component.Identifier.Key.LSHIFT;
        case KeyEvent.VK_ALT:
            if (keyLocation == KeyEvent.KEY_LOCATION_RIGHT)
                return Component.Identifier.Key.RALT;
            else
                return Component.Identifier.Key.LALT;
//        //this is 1.5 only
//        case KeyEvent.VK_WINDOWS:
//            if (keyLocation == KeyEvent.KEY_LOCATION_RIGHT)
//                return Component.Identifier.Key.RWIN;
//            else
//                return Component.Identifier.Key.LWIN;
        case KeyEvent.VK_ENTER:
            if (keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
                return Component.Identifier.Key.NUMPADENTER;
            else
                return Component.Identifier.Key.RETURN;
        case KeyEvent.VK_COMMA:
            if (keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
                return Component.Identifier.Key.NUMPADCOMMA;
            else
                return Component.Identifier.Key.COMMA;
        default:
            return mapKeyCode(keyCode);
        }
    }
}
