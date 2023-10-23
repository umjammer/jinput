/**
 * Copyright (C) 2003 Jeremy Booth (jeremy@newdawnsoftware.com)
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

package net.java.games.input.linux;

import java.util.logging.Logger;

import net.java.games.input.Component;
import net.java.games.input.Controller;


/**
 * Mapping utility class between native type ints and string names or
 * Key.Identifiers
 * @author Jeremy Booth (jeremy@newdawnsoftware.com)
 */
class LinuxNativeTypesMap {

    private static LinuxNativeTypesMap INSTANCE = new LinuxNativeTypesMap();
    private static Logger log = Logger.getLogger(LinuxNativeTypesMap.class.getName());

    private final Component.Identifier[] relAxesIDs;
    private final Component.Identifier[] absAxesIDs;
    private final Component.Identifier[] buttonIDs;

    /** create an empty, uninitialsed map
     */
    private LinuxNativeTypesMap() {
        buttonIDs = new Component.Identifier[NativeDefinitions.KEY_MAX];
        relAxesIDs = new Component.Identifier[NativeDefinitions.REL_MAX];
        absAxesIDs = new Component.Identifier[NativeDefinitions.ABS_MAX];
        reInit();
    }

    /** Do the work.
     */
    private void reInit() {
        buttonIDs[NativeDefinitions.KEY_ESC] = Component.Identifier.Key.ESCAPE;
        buttonIDs[NativeDefinitions.KEY_1] = Component.Identifier.Key._1;
        buttonIDs[NativeDefinitions.KEY_2] = Component.Identifier.Key._2;
        buttonIDs[NativeDefinitions.KEY_3] = Component.Identifier.Key._3;
        buttonIDs[NativeDefinitions.KEY_4] = Component.Identifier.Key._4;
        buttonIDs[NativeDefinitions.KEY_5] = Component.Identifier.Key._5;
        buttonIDs[NativeDefinitions.KEY_6] = Component.Identifier.Key._6;
        buttonIDs[NativeDefinitions.KEY_7] = Component.Identifier.Key._7;
        buttonIDs[NativeDefinitions.KEY_8] = Component.Identifier.Key._8;
        buttonIDs[NativeDefinitions.KEY_9] = Component.Identifier.Key._9;
        buttonIDs[NativeDefinitions.KEY_0] = Component.Identifier.Key._0;
        buttonIDs[NativeDefinitions.KEY_MINUS] = Component.Identifier.Key.MINUS;
        buttonIDs[NativeDefinitions.KEY_EQUAL] = Component.Identifier.Key.EQUALS;
        buttonIDs[NativeDefinitions.KEY_BACKSPACE] = Component.Identifier.Key.BACK;
        buttonIDs[NativeDefinitions.KEY_TAB] = Component.Identifier.Key.TAB;
        buttonIDs[NativeDefinitions.KEY_Q] = Component.Identifier.Key.Q;
        buttonIDs[NativeDefinitions.KEY_W] = Component.Identifier.Key.W;
        buttonIDs[NativeDefinitions.KEY_E] = Component.Identifier.Key.E;
        buttonIDs[NativeDefinitions.KEY_R] = Component.Identifier.Key.R;
        buttonIDs[NativeDefinitions.KEY_T] = Component.Identifier.Key.T;
        buttonIDs[NativeDefinitions.KEY_Y] = Component.Identifier.Key.Y;
        buttonIDs[NativeDefinitions.KEY_U] = Component.Identifier.Key.U;
        buttonIDs[NativeDefinitions.KEY_I] = Component.Identifier.Key.I;
        buttonIDs[NativeDefinitions.KEY_O] = Component.Identifier.Key.O;
        buttonIDs[NativeDefinitions.KEY_P] = Component.Identifier.Key.P;
        buttonIDs[NativeDefinitions.KEY_LEFTBRACE] = Component.Identifier.Key.LBRACKET;
        buttonIDs[NativeDefinitions.KEY_RIGHTBRACE] = Component.Identifier.Key.RBRACKET;
        buttonIDs[NativeDefinitions.KEY_ENTER] = Component.Identifier.Key.RETURN;
        buttonIDs[NativeDefinitions.KEY_LEFTCTRL] = Component.Identifier.Key.LCONTROL;
        buttonIDs[NativeDefinitions.KEY_A] = Component.Identifier.Key.A;
        buttonIDs[NativeDefinitions.KEY_S] = Component.Identifier.Key.S;
        buttonIDs[NativeDefinitions.KEY_D] = Component.Identifier.Key.D;
        buttonIDs[NativeDefinitions.KEY_F] = Component.Identifier.Key.F;
        buttonIDs[NativeDefinitions.KEY_G] = Component.Identifier.Key.G;
        buttonIDs[NativeDefinitions.KEY_H] = Component.Identifier.Key.H;
        buttonIDs[NativeDefinitions.KEY_J] = Component.Identifier.Key.J;
        buttonIDs[NativeDefinitions.KEY_K] = Component.Identifier.Key.K;
        buttonIDs[NativeDefinitions.KEY_L] = Component.Identifier.Key.L;
        buttonIDs[NativeDefinitions.KEY_SEMICOLON] = Component.Identifier.Key.SEMICOLON;
        buttonIDs[NativeDefinitions.KEY_APOSTROPHE] = Component.Identifier.Key.APOSTROPHE;
        buttonIDs[NativeDefinitions.KEY_GRAVE] = Component.Identifier.Key.GRAVE;
        buttonIDs[NativeDefinitions.KEY_LEFTSHIFT] = Component.Identifier.Key.LSHIFT;
        buttonIDs[NativeDefinitions.KEY_BACKSLASH] = Component.Identifier.Key.BACKSLASH;
        buttonIDs[NativeDefinitions.KEY_Z] = Component.Identifier.Key.Z;
        buttonIDs[NativeDefinitions.KEY_X] = Component.Identifier.Key.X;
        buttonIDs[NativeDefinitions.KEY_C] = Component.Identifier.Key.C;
        buttonIDs[NativeDefinitions.KEY_V] = Component.Identifier.Key.V;
        buttonIDs[NativeDefinitions.KEY_B] = Component.Identifier.Key.B;
        buttonIDs[NativeDefinitions.KEY_N] = Component.Identifier.Key.N;
        buttonIDs[NativeDefinitions.KEY_M] = Component.Identifier.Key.M;
        buttonIDs[NativeDefinitions.KEY_COMMA] = Component.Identifier.Key.COMMA;
        buttonIDs[NativeDefinitions.KEY_DOT] = Component.Identifier.Key.PERIOD;
        buttonIDs[NativeDefinitions.KEY_SLASH] = Component.Identifier.Key.SLASH;
        buttonIDs[NativeDefinitions.KEY_RIGHTSHIFT] = Component.Identifier.Key.RSHIFT;
        buttonIDs[NativeDefinitions.KEY_KPASTERISK] = Component.Identifier.Key.MULTIPLY;
        buttonIDs[NativeDefinitions.KEY_LEFTALT] = Component.Identifier.Key.LALT;
        buttonIDs[NativeDefinitions.KEY_SPACE] = Component.Identifier.Key.SPACE;
        buttonIDs[NativeDefinitions.KEY_CAPSLOCK] = Component.Identifier.Key.CAPITAL;
        buttonIDs[NativeDefinitions.KEY_F1] = Component.Identifier.Key.F1;
        buttonIDs[NativeDefinitions.KEY_F2] = Component.Identifier.Key.F2;
        buttonIDs[NativeDefinitions.KEY_F3] = Component.Identifier.Key.F3;
        buttonIDs[NativeDefinitions.KEY_F4] = Component.Identifier.Key.F4;
        buttonIDs[NativeDefinitions.KEY_F5] = Component.Identifier.Key.F5;
        buttonIDs[NativeDefinitions.KEY_F6] = Component.Identifier.Key.F6;
        buttonIDs[NativeDefinitions.KEY_F7] = Component.Identifier.Key.F7;
        buttonIDs[NativeDefinitions.KEY_F8] = Component.Identifier.Key.F8;
        buttonIDs[NativeDefinitions.KEY_F9] = Component.Identifier.Key.F9;
        buttonIDs[NativeDefinitions.KEY_F10] = Component.Identifier.Key.F10;
        buttonIDs[NativeDefinitions.KEY_NUMLOCK] = Component.Identifier.Key.NUMLOCK;
        buttonIDs[NativeDefinitions.KEY_SCROLLLOCK] = Component.Identifier.Key.SCROLL;
        buttonIDs[NativeDefinitions.KEY_KP7] = Component.Identifier.Key.NUMPAD7;
        buttonIDs[NativeDefinitions.KEY_KP8] = Component.Identifier.Key.NUMPAD8;
        buttonIDs[NativeDefinitions.KEY_KP9] = Component.Identifier.Key.NUMPAD9;
        buttonIDs[NativeDefinitions.KEY_KPMINUS] = Component.Identifier.Key.SUBTRACT;
        buttonIDs[NativeDefinitions.KEY_KP4] = Component.Identifier.Key.NUMPAD4;
        buttonIDs[NativeDefinitions.KEY_KP5] = Component.Identifier.Key.NUMPAD5;
        buttonIDs[NativeDefinitions.KEY_KP6] = Component.Identifier.Key.NUMPAD6;
        buttonIDs[NativeDefinitions.KEY_KPPLUS] = Component.Identifier.Key.ADD;
        buttonIDs[NativeDefinitions.KEY_KP1] = Component.Identifier.Key.NUMPAD1;
        buttonIDs[NativeDefinitions.KEY_KP2] = Component.Identifier.Key.NUMPAD2;
        buttonIDs[NativeDefinitions.KEY_KP3] = Component.Identifier.Key.NUMPAD3;
        buttonIDs[NativeDefinitions.KEY_KP0] = Component.Identifier.Key.NUMPAD0;
        buttonIDs[NativeDefinitions.KEY_KPDOT] = Component.Identifier.Key.DECIMAL;
//        buttonIDs[NativeDefinitions.KEY_103RD] = null;
        buttonIDs[NativeDefinitions.KEY_F13] = Component.Identifier.Key.F13;
        buttonIDs[NativeDefinitions.KEY_102ND] = null;
        buttonIDs[NativeDefinitions.KEY_F11] = Component.Identifier.Key.F11;
        buttonIDs[NativeDefinitions.KEY_F12] = Component.Identifier.Key.F12;
        buttonIDs[NativeDefinitions.KEY_F14] = Component.Identifier.Key.F14;
        buttonIDs[NativeDefinitions.KEY_F15] = Component.Identifier.Key.F15;
        buttonIDs[NativeDefinitions.KEY_F16] = null;
        buttonIDs[NativeDefinitions.KEY_F17] = null;
        buttonIDs[NativeDefinitions.KEY_F18] = null;
        buttonIDs[NativeDefinitions.KEY_F19] = null;
        buttonIDs[NativeDefinitions.KEY_F20] = null;
        buttonIDs[NativeDefinitions.KEY_KPENTER] = Component.Identifier.Key.NUMPADENTER;
        buttonIDs[NativeDefinitions.KEY_RIGHTCTRL] = Component.Identifier.Key.RCONTROL;
        buttonIDs[NativeDefinitions.KEY_KPSLASH] = Component.Identifier.Key.DIVIDE;
        buttonIDs[NativeDefinitions.KEY_SYSRQ] = Component.Identifier.Key.SYSRQ;
        buttonIDs[NativeDefinitions.KEY_RIGHTALT] = Component.Identifier.Key.RALT;
        buttonIDs[NativeDefinitions.KEY_LINEFEED] = null;
        buttonIDs[NativeDefinitions.KEY_HOME] = Component.Identifier.Key.HOME;
        buttonIDs[NativeDefinitions.KEY_UP] = Component.Identifier.Key.UP;
        buttonIDs[NativeDefinitions.KEY_PAGEUP] = Component.Identifier.Key.PAGEUP;
        buttonIDs[NativeDefinitions.KEY_LEFT] = Component.Identifier.Key.LEFT;
        buttonIDs[NativeDefinitions.KEY_RIGHT] = Component.Identifier.Key.RIGHT;
        buttonIDs[NativeDefinitions.KEY_END] = Component.Identifier.Key.END;
        buttonIDs[NativeDefinitions.KEY_DOWN] = Component.Identifier.Key.DOWN;
        buttonIDs[NativeDefinitions.KEY_PAGEDOWN] = Component.Identifier.Key.PAGEDOWN;
        buttonIDs[NativeDefinitions.KEY_INSERT] = Component.Identifier.Key.INSERT;
        buttonIDs[NativeDefinitions.KEY_DELETE] = Component.Identifier.Key.DELETE;
        buttonIDs[NativeDefinitions.KEY_PAUSE] = Component.Identifier.Key.PAUSE;
/*        buttonIDs[NativeDefinitions.KEY_MACRO] = "Macro";
        buttonIDs[NativeDefinitions.KEY_MUTE] = "Mute";
        buttonIDs[NativeDefinitions.KEY_VOLUMEDOWN] = "Volume Down";
        buttonIDs[NativeDefinitions.KEY_VOLUMEUP] = "Volume Up";
        buttonIDs[NativeDefinitions.KEY_POWER] = "Power";*/
        buttonIDs[NativeDefinitions.KEY_KPEQUAL] = Component.Identifier.Key.NUMPADEQUAL;
        //buttonIDs[NativeDefinitions.KEY_KPPLUSMINUS] = "KeyPad +/-";
/*        buttonIDs[NativeDefinitions.KEY_F21] = "F21";
        buttonIDs[NativeDefinitions.KEY_F22] = "F22";
        buttonIDs[NativeDefinitions.KEY_F23] = "F23";
        buttonIDs[NativeDefinitions.KEY_F24] = "F24";
        buttonIDs[NativeDefinitions.KEY_KPCOMMA] = "KeyPad comma";
        buttonIDs[NativeDefinitions.KEY_LEFTMETA] = "LH Meta";
        buttonIDs[NativeDefinitions.KEY_RIGHTMETA] = "RH Meta";
        buttonIDs[NativeDefinitions.KEY_COMPOSE] = "Compose";
        buttonIDs[NativeDefinitions.KEY_STOP] = "Stop";
        buttonIDs[NativeDefinitions.KEY_AGAIN] = "Again";
        buttonIDs[NativeDefinitions.KEY_PROPS] = "Properties";
        buttonIDs[NativeDefinitions.KEY_UNDO] = "Undo";
        buttonIDs[NativeDefinitions.KEY_FRONT] = "Front";
        buttonIDs[NativeDefinitions.KEY_COPY] = "Copy";
        buttonIDs[NativeDefinitions.KEY_OPEN] = "Open";
        buttonIDs[NativeDefinitions.KEY_PASTE] = "Paste";
        buttonIDs[NativeDefinitions.KEY_FIND] = "Find";
        buttonIDs[NativeDefinitions.KEY_CUT] = "Cut";
        buttonIDs[NativeDefinitions.KEY_HELP] = "Help";
        buttonIDs[NativeDefinitions.KEY_MENU] = "Menu";
        buttonIDs[NativeDefinitions.KEY_CALC] = "Calculator";
        buttonIDs[NativeDefinitions.KEY_SETUP] = "Setup";*/
        buttonIDs[NativeDefinitions.KEY_SLEEP] = Component.Identifier.Key.SLEEP;
        /*buttonIDs[NativeDefinitions.KEY_WAKEUP] = "Wakeup";
        buttonIDs[NativeDefinitions.KEY_FILE] = "File";
        buttonIDs[NativeDefinitions.KEY_SENDFILE] = "Send File";
        buttonIDs[NativeDefinitions.KEY_DELETEFILE] = "Delete File";
        buttonIDs[NativeDefinitions.KEY_XFER] = "Transfer";
        buttonIDs[NativeDefinitions.KEY_PROG1] = "Program 1";
        buttonIDs[NativeDefinitions.KEY_PROG2] = "Program 2";
        buttonIDs[NativeDefinitions.KEY_WWW] = "Web Browser";
        buttonIDs[NativeDefinitions.KEY_MSDOS] = "DOS mode";
        buttonIDs[NativeDefinitions.KEY_COFFEE] = "Coffee";
        buttonIDs[NativeDefinitions.KEY_DIRECTION] = "Direction";
        buttonIDs[NativeDefinitions.KEY_CYCLEWINDOWS] = "Window cycle";
        buttonIDs[NativeDefinitions.KEY_MAIL] = "Mail";
        buttonIDs[NativeDefinitions.KEY_BOOKMARKS] = "Book Marks";
        buttonIDs[NativeDefinitions.KEY_COMPUTER] = "Computer";
        buttonIDs[NativeDefinitions.KEY_BACK] = "Back";
        buttonIDs[NativeDefinitions.KEY_FORWARD] = "Forward";
        buttonIDs[NativeDefinitions.KEY_CLOSECD] = "Close CD";
        buttonIDs[NativeDefinitions.KEY_EJECTCD] = "Eject CD";
        buttonIDs[NativeDefinitions.KEY_EJECTCLOSECD] = "Eject / Close CD";
        buttonIDs[NativeDefinitions.KEY_NEXTSONG] = "Next Song";
        buttonIDs[NativeDefinitions.KEY_PLAYPAUSE] = "Play and Pause";
        buttonIDs[NativeDefinitions.KEY_PREVIOUSSONG] = "Previous Song";
        buttonIDs[NativeDefinitions.KEY_STOPCD] = "Stop CD";
        buttonIDs[NativeDefinitions.KEY_RECORD] = "Record";
        buttonIDs[NativeDefinitions.KEY_REWIND] = "Rewind";
        buttonIDs[NativeDefinitions.KEY_PHONE] = "Phone";
        buttonIDs[NativeDefinitions.KEY_ISO] = "ISO";
        buttonIDs[NativeDefinitions.KEY_CONFIG] = "Config";
        buttonIDs[NativeDefinitions.KEY_HOMEPAGE] = "Home";
        buttonIDs[NativeDefinitions.KEY_REFRESH] = "Refresh";
        buttonIDs[NativeDefinitions.KEY_EXIT] = "Exit";
        buttonIDs[NativeDefinitions.KEY_MOVE] = "Move";
        buttonIDs[NativeDefinitions.KEY_EDIT] = "Edit";
        buttonIDs[NativeDefinitions.KEY_SCROLLUP] = "Scroll Up";
        buttonIDs[NativeDefinitions.KEY_SCROLLDOWN] = "Scroll Down";
        buttonIDs[NativeDefinitions.KEY_KPLEFTPAREN] = "KeyPad LH parenthesis";
        buttonIDs[NativeDefinitions.KEY_KPRIGHTPAREN] = "KeyPad RH parenthesis";
        buttonIDs[NativeDefinitions.KEY_INTL1] = "Intl 1";
        buttonIDs[NativeDefinitions.KEY_INTL2] = "Intl 2";
        buttonIDs[NativeDefinitions.KEY_INTL3] = "Intl 3";
        buttonIDs[NativeDefinitions.KEY_INTL4] = "Intl 4";
        buttonIDs[NativeDefinitions.KEY_INTL5] = "Intl 5";
        buttonIDs[NativeDefinitions.KEY_INTL6] = "Intl 6";
        buttonIDs[NativeDefinitions.KEY_INTL7] = "Intl 7";
        buttonIDs[NativeDefinitions.KEY_INTL8] = "Intl 8";
        buttonIDs[NativeDefinitions.KEY_INTL9] = "Intl 9";
        buttonIDs[NativeDefinitions.KEY_LANG1] = "Language 1";
        buttonIDs[NativeDefinitions.KEY_LANG2] = "Language 2";
        buttonIDs[NativeDefinitions.KEY_LANG3] = "Language 3";
        buttonIDs[NativeDefinitions.KEY_LANG4] = "Language 4";
        buttonIDs[NativeDefinitions.KEY_LANG5] = "Language 5";
        buttonIDs[NativeDefinitions.KEY_LANG6] = "Language 6";
        buttonIDs[NativeDefinitions.KEY_LANG7] = "Language 7";
        buttonIDs[NativeDefinitions.KEY_LANG8] = "Language 8";
        buttonIDs[NativeDefinitions.KEY_LANG9] = "Language 9";
        buttonIDs[NativeDefinitions.KEY_PLAYCD] = "Play CD";
        buttonIDs[NativeDefinitions.KEY_PAUSECD] = "Pause CD";
        buttonIDs[NativeDefinitions.KEY_PROG3] = "Program 3";
        buttonIDs[NativeDefinitions.KEY_PROG4] = "Program 4";
        buttonIDs[NativeDefinitions.KEY_SUSPEND] = "Suspend";
        buttonIDs[NativeDefinitions.KEY_CLOSE] = "Close";*/
        buttonIDs[NativeDefinitions.KEY_UNKNOWN] = Component.Identifier.Key.UNLABELED;
        /*buttonIDs[NativeDefinitions.KEY_BRIGHTNESSDOWN] = "Brightness Down";
        buttonIDs[NativeDefinitions.KEY_BRIGHTNESSUP] = "Brightness Up";*/

        //Misc keys
        buttonIDs[NativeDefinitions.BTN_0] = Component.Identifier.Button._0;
        buttonIDs[NativeDefinitions.BTN_1] = Component.Identifier.Button._1;
        buttonIDs[NativeDefinitions.BTN_2] = Component.Identifier.Button._2;
        buttonIDs[NativeDefinitions.BTN_3] = Component.Identifier.Button._3;
        buttonIDs[NativeDefinitions.BTN_4] = Component.Identifier.Button._4;
        buttonIDs[NativeDefinitions.BTN_5] = Component.Identifier.Button._5;
        buttonIDs[NativeDefinitions.BTN_6] = Component.Identifier.Button._6;
        buttonIDs[NativeDefinitions.BTN_7] = Component.Identifier.Button._7;
        buttonIDs[NativeDefinitions.BTN_8] = Component.Identifier.Button._8;
        buttonIDs[NativeDefinitions.BTN_9] = Component.Identifier.Button._9;

        // Mouse
        buttonIDs[NativeDefinitions.BTN_LEFT] = Component.Identifier.Button.LEFT;
        buttonIDs[NativeDefinitions.BTN_RIGHT] = Component.Identifier.Button.RIGHT;
        buttonIDs[NativeDefinitions.BTN_MIDDLE] = Component.Identifier.Button.MIDDLE;
        buttonIDs[NativeDefinitions.BTN_SIDE] = Component.Identifier.Button.SIDE;
        buttonIDs[NativeDefinitions.BTN_EXTRA] = Component.Identifier.Button.EXTRA;
        buttonIDs[NativeDefinitions.BTN_FORWARD] = Component.Identifier.Button.FORWARD;
        buttonIDs[NativeDefinitions.BTN_BACK] = Component.Identifier.Button.BACK;

        // Joystick
        buttonIDs[NativeDefinitions.BTN_TRIGGER] = Component.Identifier.Button.TRIGGER;
        buttonIDs[NativeDefinitions.BTN_THUMB] = Component.Identifier.Button.THUMB;
        buttonIDs[NativeDefinitions.BTN_THUMB2] = Component.Identifier.Button.THUMB2;
        buttonIDs[NativeDefinitions.BTN_TOP] = Component.Identifier.Button.TOP;
        buttonIDs[NativeDefinitions.BTN_TOP2] = Component.Identifier.Button.TOP2;
        buttonIDs[NativeDefinitions.BTN_PINKIE] = Component.Identifier.Button.PINKIE;
        buttonIDs[NativeDefinitions.BTN_BASE] = Component.Identifier.Button.BASE;
        buttonIDs[NativeDefinitions.BTN_BASE2] = Component.Identifier.Button.BASE2;
        buttonIDs[NativeDefinitions.BTN_BASE3] = Component.Identifier.Button.BASE3;
        buttonIDs[NativeDefinitions.BTN_BASE4] = Component.Identifier.Button.BASE4;
        buttonIDs[NativeDefinitions.BTN_BASE5] = Component.Identifier.Button.BASE5;
        buttonIDs[NativeDefinitions.BTN_BASE6] = Component.Identifier.Button.BASE6;
        buttonIDs[NativeDefinitions.BTN_DEAD] = Component.Identifier.Button.DEAD;

        // Gamepad
        buttonIDs[NativeDefinitions.BTN_A] = Component.Identifier.Button.A;
        buttonIDs[NativeDefinitions.BTN_B] = Component.Identifier.Button.B;
        buttonIDs[NativeDefinitions.BTN_C] = Component.Identifier.Button.C;
        buttonIDs[NativeDefinitions.BTN_X] = Component.Identifier.Button.X;
        buttonIDs[NativeDefinitions.BTN_Y] = Component.Identifier.Button.Y;
        buttonIDs[NativeDefinitions.BTN_Z] = Component.Identifier.Button.Z;
        buttonIDs[NativeDefinitions.BTN_TL] = Component.Identifier.Button.LEFT_THUMB;
        buttonIDs[NativeDefinitions.BTN_TR] = Component.Identifier.Button.RIGHT_THUMB;
        buttonIDs[NativeDefinitions.BTN_TL2] = Component.Identifier.Button.LEFT_THUMB2;
        buttonIDs[NativeDefinitions.BTN_TR2] = Component.Identifier.Button.RIGHT_THUMB2;
        buttonIDs[NativeDefinitions.BTN_SELECT] = Component.Identifier.Button.SELECT;
        buttonIDs[NativeDefinitions.BTN_START] = Component.Identifier.Button.START;
        buttonIDs[NativeDefinitions.BTN_MODE] = Component.Identifier.Button.MODE;
        buttonIDs[NativeDefinitions.BTN_THUMBL] = Component.Identifier.Button.LEFT_THUMB3;
        buttonIDs[NativeDefinitions.BTN_THUMBR] = Component.Identifier.Button.RIGHT_THUMB3;

        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY1] = Component.Identifier.Button.EXTRA_1;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY2] = Component.Identifier.Button.EXTRA_2;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY3] = Component.Identifier.Button.EXTRA_3;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY4] = Component.Identifier.Button.EXTRA_4;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY5] = Component.Identifier.Button.EXTRA_5;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY6] = Component.Identifier.Button.EXTRA_6;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY7] = Component.Identifier.Button.EXTRA_7;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY8] = Component.Identifier.Button.EXTRA_8;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY9] = Component.Identifier.Button.EXTRA_9;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY10] = Component.Identifier.Button.EXTRA_10;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY11] = Component.Identifier.Button.EXTRA_11;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY12] = Component.Identifier.Button.EXTRA_12;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY13] = Component.Identifier.Button.EXTRA_13;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY14] = Component.Identifier.Button.EXTRA_14;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY15] = Component.Identifier.Button.EXTRA_15;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY16] = Component.Identifier.Button.EXTRA_16;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY17] = Component.Identifier.Button.EXTRA_17;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY18] = Component.Identifier.Button.EXTRA_18;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY19] = Component.Identifier.Button.EXTRA_19;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY20] = Component.Identifier.Button.EXTRA_20;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY21] = Component.Identifier.Button.EXTRA_21;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY22] = Component.Identifier.Button.EXTRA_22;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY23] = Component.Identifier.Button.EXTRA_23;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY24] = Component.Identifier.Button.EXTRA_24;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY25] = Component.Identifier.Button.EXTRA_25;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY26] = Component.Identifier.Button.EXTRA_26;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY27] = Component.Identifier.Button.EXTRA_27;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY28] = Component.Identifier.Button.EXTRA_28;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY29] = Component.Identifier.Button.EXTRA_29;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY30] = Component.Identifier.Button.EXTRA_30;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY31] = Component.Identifier.Button.EXTRA_31;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY32] = Component.Identifier.Button.EXTRA_32;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY33] = Component.Identifier.Button.EXTRA_33;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY34] = Component.Identifier.Button.EXTRA_34;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY35] = Component.Identifier.Button.EXTRA_35;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY36] = Component.Identifier.Button.EXTRA_36;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY37] = Component.Identifier.Button.EXTRA_37;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY38] = Component.Identifier.Button.EXTRA_38;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY39] = Component.Identifier.Button.EXTRA_39;
        buttonIDs[NativeDefinitions.BTN_TRIGGER_HAPPY40] = Component.Identifier.Button.EXTRA_40;

        // Digitiser
        buttonIDs[NativeDefinitions.BTN_TOOL_PEN] = Component.Identifier.Button.TOOL_PEN;
        buttonIDs[NativeDefinitions.BTN_TOOL_RUBBER] = Component.Identifier.Button.TOOL_RUBBER;
        buttonIDs[NativeDefinitions.BTN_TOOL_BRUSH] = Component.Identifier.Button.TOOL_BRUSH;
        buttonIDs[NativeDefinitions.BTN_TOOL_PENCIL] = Component.Identifier.Button.TOOL_PENCIL;
        buttonIDs[NativeDefinitions.BTN_TOOL_AIRBRUSH] = Component.Identifier.Button.TOOL_AIRBRUSH;
        buttonIDs[NativeDefinitions.BTN_TOOL_FINGER] = Component.Identifier.Button.TOOL_FINGER;
        buttonIDs[NativeDefinitions.BTN_TOOL_MOUSE] = Component.Identifier.Button.TOOL_MOUSE;
        buttonIDs[NativeDefinitions.BTN_TOOL_LENS] = Component.Identifier.Button.TOOL_LENS;
        buttonIDs[NativeDefinitions.BTN_TOUCH] = Component.Identifier.Button.TOUCH;
        buttonIDs[NativeDefinitions.BTN_STYLUS] = Component.Identifier.Button.STYLUS;
        buttonIDs[NativeDefinitions.BTN_STYLUS2] = Component.Identifier.Button.STYLUS2;

        relAxesIDs[NativeDefinitions.REL_X] = Component.Identifier.Axis.X;
        relAxesIDs[NativeDefinitions.REL_Y] = Component.Identifier.Axis.Y;
        relAxesIDs[NativeDefinitions.REL_Z] = Component.Identifier.Axis.Z;
        relAxesIDs[NativeDefinitions.REL_WHEEL] = Component.Identifier.Axis.Z;
        // There are guesses as I have no idea what they would be used for
        relAxesIDs[NativeDefinitions.REL_HWHEEL] = Component.Identifier.Axis.SLIDER;
        relAxesIDs[NativeDefinitions.REL_DIAL] = Component.Identifier.Axis.SLIDER;
        relAxesIDs[NativeDefinitions.REL_MISC] = Component.Identifier.Axis.SLIDER;

        absAxesIDs[NativeDefinitions.ABS_X] = Component.Identifier.Axis.X;
        absAxesIDs[NativeDefinitions.ABS_Y] = Component.Identifier.Axis.Y;
        absAxesIDs[NativeDefinitions.ABS_Z] = Component.Identifier.Axis.Z;
        absAxesIDs[NativeDefinitions.ABS_RX] = Component.Identifier.Axis.RX;
        absAxesIDs[NativeDefinitions.ABS_RY] = Component.Identifier.Axis.RY;
        absAxesIDs[NativeDefinitions.ABS_RZ] = Component.Identifier.Axis.RZ;
        absAxesIDs[NativeDefinitions.ABS_THROTTLE] = Component.Identifier.Axis.SLIDER;
        absAxesIDs[NativeDefinitions.ABS_RUDDER] = Component.Identifier.Axis.RZ;
        absAxesIDs[NativeDefinitions.ABS_WHEEL] = Component.Identifier.Axis.Y;
        absAxesIDs[NativeDefinitions.ABS_GAS] = Component.Identifier.Axis.SLIDER;
        absAxesIDs[NativeDefinitions.ABS_BRAKE] = Component.Identifier.Axis.SLIDER;
        // Hats are done this way as they are mapped from two axis down to one
        absAxesIDs[NativeDefinitions.ABS_HAT0X] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT0Y] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT1X] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT1Y] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT2X] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT2Y] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT3X] = Component.Identifier.Axis.POV;
        absAxesIDs[NativeDefinitions.ABS_HAT3Y] = Component.Identifier.Axis.POV;
        // erm, yeah
        absAxesIDs[NativeDefinitions.ABS_PRESSURE] = null;
        absAxesIDs[NativeDefinitions.ABS_DISTANCE] = null;
        absAxesIDs[NativeDefinitions.ABS_TILT_X] = null;
        absAxesIDs[NativeDefinitions.ABS_TILT_Y] = null;
        absAxesIDs[NativeDefinitions.ABS_MISC] = null;
    }

    public static Controller.Type guessButtonTrait(int button_code) {
        return switch (button_code) {
            case NativeDefinitions.BTN_TRIGGER, NativeDefinitions.BTN_THUMB, NativeDefinitions.BTN_THUMB2, NativeDefinitions.BTN_TOP, NativeDefinitions.BTN_TOP2, NativeDefinitions.BTN_PINKIE, NativeDefinitions.BTN_BASE, NativeDefinitions.BTN_BASE2, NativeDefinitions.BTN_BASE3, NativeDefinitions.BTN_BASE4, NativeDefinitions.BTN_BASE5, NativeDefinitions.BTN_BASE6, NativeDefinitions.BTN_DEAD ->
                    Controller.Type.STICK;
            case NativeDefinitions.BTN_A, NativeDefinitions.BTN_B, NativeDefinitions.BTN_C, NativeDefinitions.BTN_X, NativeDefinitions.BTN_Y, NativeDefinitions.BTN_Z, NativeDefinitions.BTN_TL, NativeDefinitions.BTN_TR, NativeDefinitions.BTN_TL2, NativeDefinitions.BTN_TR2, NativeDefinitions.BTN_SELECT, NativeDefinitions.BTN_START, NativeDefinitions.BTN_MODE, NativeDefinitions.BTN_THUMBL, NativeDefinitions.BTN_THUMBR ->
                    Controller.Type.GAMEPAD;
            case NativeDefinitions.BTN_0, NativeDefinitions.BTN_1, NativeDefinitions.BTN_2, NativeDefinitions.BTN_3, NativeDefinitions.BTN_4, NativeDefinitions.BTN_5, NativeDefinitions.BTN_6, NativeDefinitions.BTN_7, NativeDefinitions.BTN_8, NativeDefinitions.BTN_9 ->
                    Controller.Type.UNKNOWN;
            case NativeDefinitions.BTN_LEFT, NativeDefinitions.BTN_RIGHT, NativeDefinitions.BTN_MIDDLE, NativeDefinitions.BTN_SIDE, NativeDefinitions.BTN_EXTRA ->
                    Controller.Type.MOUSE;
            //				case NativeDefinitions.KEY_RESERVED:
            //    			case NativeDefinitions.KEY_UNKNOWN:
            case NativeDefinitions.KEY_ESC, NativeDefinitions.KEY_1, NativeDefinitions.KEY_2, NativeDefinitions.KEY_3, NativeDefinitions.KEY_4, NativeDefinitions.KEY_5, NativeDefinitions.KEY_6, NativeDefinitions.KEY_7, NativeDefinitions.KEY_8, NativeDefinitions.KEY_9, NativeDefinitions.KEY_0, NativeDefinitions.KEY_MINUS, NativeDefinitions.KEY_EQUAL, NativeDefinitions.KEY_BACKSPACE, NativeDefinitions.KEY_TAB, NativeDefinitions.KEY_Q, NativeDefinitions.KEY_W, NativeDefinitions.KEY_E, NativeDefinitions.KEY_R, NativeDefinitions.KEY_T, NativeDefinitions.KEY_Y, NativeDefinitions.KEY_U, NativeDefinitions.KEY_I, NativeDefinitions.KEY_O, NativeDefinitions.KEY_P, NativeDefinitions.KEY_LEFTBRACE, NativeDefinitions.KEY_RIGHTBRACE, NativeDefinitions.KEY_ENTER, NativeDefinitions.KEY_LEFTCTRL, NativeDefinitions.KEY_A, NativeDefinitions.KEY_S, NativeDefinitions.KEY_D, NativeDefinitions.KEY_F, NativeDefinitions.KEY_G, NativeDefinitions.KEY_H, NativeDefinitions.KEY_J, NativeDefinitions.KEY_K, NativeDefinitions.KEY_L, NativeDefinitions.KEY_SEMICOLON, NativeDefinitions.KEY_APOSTROPHE, NativeDefinitions.KEY_GRAVE, NativeDefinitions.KEY_LEFTSHIFT, NativeDefinitions.KEY_BACKSLASH, NativeDefinitions.KEY_Z, NativeDefinitions.KEY_X, NativeDefinitions.KEY_C, NativeDefinitions.KEY_V, NativeDefinitions.KEY_B, NativeDefinitions.KEY_N, NativeDefinitions.KEY_M, NativeDefinitions.KEY_COMMA, NativeDefinitions.KEY_DOT, NativeDefinitions.KEY_SLASH, NativeDefinitions.KEY_RIGHTSHIFT, NativeDefinitions.KEY_KPASTERISK, NativeDefinitions.KEY_LEFTALT, NativeDefinitions.KEY_SPACE, NativeDefinitions.KEY_CAPSLOCK, NativeDefinitions.KEY_F1, NativeDefinitions.KEY_F2, NativeDefinitions.KEY_F3, NativeDefinitions.KEY_F4, NativeDefinitions.KEY_F5, NativeDefinitions.KEY_F6, NativeDefinitions.KEY_F7, NativeDefinitions.KEY_F8, NativeDefinitions.KEY_F9, NativeDefinitions.KEY_F10, NativeDefinitions.KEY_NUMLOCK, NativeDefinitions.KEY_SCROLLLOCK, NativeDefinitions.KEY_KP7, NativeDefinitions.KEY_KP8, NativeDefinitions.KEY_KP9, NativeDefinitions.KEY_KPMINUS, NativeDefinitions.KEY_KP4, NativeDefinitions.KEY_KP5, NativeDefinitions.KEY_KP6, NativeDefinitions.KEY_KPPLUS, NativeDefinitions.KEY_KP1, NativeDefinitions.KEY_KP2, NativeDefinitions.KEY_KP3, NativeDefinitions.KEY_KP0, NativeDefinitions.KEY_KPDOT, NativeDefinitions.KEY_ZENKAKUHANKAKU, NativeDefinitions.KEY_102ND, NativeDefinitions.KEY_F11, NativeDefinitions.KEY_F12, NativeDefinitions.KEY_RO, NativeDefinitions.KEY_KATAKANA, NativeDefinitions.KEY_HIRAGANA, NativeDefinitions.KEY_HENKAN, NativeDefinitions.KEY_KATAKANAHIRAGANA, NativeDefinitions.KEY_MUHENKAN, NativeDefinitions.KEY_KPJPCOMMA, NativeDefinitions.KEY_KPENTER, NativeDefinitions.KEY_RIGHTCTRL, NativeDefinitions.KEY_KPSLASH, NativeDefinitions.KEY_SYSRQ, NativeDefinitions.KEY_RIGHTALT, NativeDefinitions.KEY_LINEFEED, NativeDefinitions.KEY_HOME, NativeDefinitions.KEY_UP, NativeDefinitions.KEY_PAGEUP, NativeDefinitions.KEY_LEFT, NativeDefinitions.KEY_RIGHT, NativeDefinitions.KEY_END, NativeDefinitions.KEY_DOWN, NativeDefinitions.KEY_PAGEDOWN, NativeDefinitions.KEY_INSERT, NativeDefinitions.KEY_DELETE, NativeDefinitions.KEY_MACRO, NativeDefinitions.KEY_MUTE, NativeDefinitions.KEY_VOLUMEDOWN, NativeDefinitions.KEY_VOLUMEUP, NativeDefinitions.KEY_POWER, NativeDefinitions.KEY_KPEQUAL, NativeDefinitions.KEY_KPPLUSMINUS, NativeDefinitions.KEY_PAUSE, NativeDefinitions.KEY_KPCOMMA, NativeDefinitions.KEY_HANGUEL, NativeDefinitions.KEY_HANJA, NativeDefinitions.KEY_YEN, NativeDefinitions.KEY_LEFTMETA, NativeDefinitions.KEY_RIGHTMETA, NativeDefinitions.KEY_COMPOSE, NativeDefinitions.KEY_STOP, NativeDefinitions.KEY_AGAIN, NativeDefinitions.KEY_PROPS, NativeDefinitions.KEY_UNDO, NativeDefinitions.KEY_FRONT, NativeDefinitions.KEY_COPY, NativeDefinitions.KEY_OPEN, NativeDefinitions.KEY_PASTE, NativeDefinitions.KEY_FIND, NativeDefinitions.KEY_CUT, NativeDefinitions.KEY_HELP, NativeDefinitions.KEY_MENU, NativeDefinitions.KEY_CALC, NativeDefinitions.KEY_SETUP, NativeDefinitions.KEY_SLEEP, NativeDefinitions.KEY_WAKEUP, NativeDefinitions.KEY_FILE, NativeDefinitions.KEY_SENDFILE, NativeDefinitions.KEY_DELETEFILE, NativeDefinitions.KEY_XFER, NativeDefinitions.KEY_PROG1, NativeDefinitions.KEY_PROG2, NativeDefinitions.KEY_WWW, NativeDefinitions.KEY_MSDOS, NativeDefinitions.KEY_COFFEE, NativeDefinitions.KEY_DIRECTION, NativeDefinitions.KEY_CYCLEWINDOWS, NativeDefinitions.KEY_MAIL, NativeDefinitions.KEY_BOOKMARKS, NativeDefinitions.KEY_COMPUTER, NativeDefinitions.KEY_BACK, NativeDefinitions.KEY_FORWARD, NativeDefinitions.KEY_CLOSECD, NativeDefinitions.KEY_EJECTCD, NativeDefinitions.KEY_EJECTCLOSECD, NativeDefinitions.KEY_NEXTSONG, NativeDefinitions.KEY_PLAYPAUSE, NativeDefinitions.KEY_PREVIOUSSONG, NativeDefinitions.KEY_STOPCD, NativeDefinitions.KEY_RECORD, NativeDefinitions.KEY_REWIND, NativeDefinitions.KEY_PHONE, NativeDefinitions.KEY_ISO, NativeDefinitions.KEY_CONFIG, NativeDefinitions.KEY_HOMEPAGE, NativeDefinitions.KEY_REFRESH, NativeDefinitions.KEY_EXIT, NativeDefinitions.KEY_MOVE, NativeDefinitions.KEY_EDIT, NativeDefinitions.KEY_SCROLLUP, NativeDefinitions.KEY_SCROLLDOWN, NativeDefinitions.KEY_KPLEFTPAREN, NativeDefinitions.KEY_KPRIGHTPAREN, NativeDefinitions.KEY_F13, NativeDefinitions.KEY_F14, NativeDefinitions.KEY_F15, NativeDefinitions.KEY_F16, NativeDefinitions.KEY_F17, NativeDefinitions.KEY_F18, NativeDefinitions.KEY_F19, NativeDefinitions.KEY_F20, NativeDefinitions.KEY_F21, NativeDefinitions.KEY_F22, NativeDefinitions.KEY_F23, NativeDefinitions.KEY_F24, NativeDefinitions.KEY_PLAYCD, NativeDefinitions.KEY_PAUSECD, NativeDefinitions.KEY_PROG3, NativeDefinitions.KEY_PROG4, NativeDefinitions.KEY_SUSPEND, NativeDefinitions.KEY_CLOSE, NativeDefinitions.KEY_PLAY, NativeDefinitions.KEY_FASTFORWARD, NativeDefinitions.KEY_BASSBOOST, NativeDefinitions.KEY_PRINT, NativeDefinitions.KEY_HP, NativeDefinitions.KEY_CAMERA, NativeDefinitions.KEY_SOUND, NativeDefinitions.KEY_QUESTION, NativeDefinitions.KEY_EMAIL, NativeDefinitions.KEY_CHAT, NativeDefinitions.KEY_SEARCH, NativeDefinitions.KEY_CONNECT, NativeDefinitions.KEY_FINANCE, NativeDefinitions.KEY_SPORT, NativeDefinitions.KEY_SHOP, NativeDefinitions.KEY_ALTERASE, NativeDefinitions.KEY_CANCEL, NativeDefinitions.KEY_BRIGHTNESSDOWN, NativeDefinitions.KEY_BRIGHTNESSUP, NativeDefinitions.KEY_MEDIA, NativeDefinitions.KEY_SWITCHVIDEOMODE, NativeDefinitions.KEY_KBDILLUMTOGGLE, NativeDefinitions.KEY_KBDILLUMDOWN, NativeDefinitions.KEY_KBDILLUMUP, NativeDefinitions.KEY_OK, NativeDefinitions.KEY_SELECT, NativeDefinitions.KEY_GOTO, NativeDefinitions.KEY_CLEAR, NativeDefinitions.KEY_POWER2, NativeDefinitions.KEY_OPTION, NativeDefinitions.KEY_INFO, NativeDefinitions.KEY_TIME, NativeDefinitions.KEY_VENDOR, NativeDefinitions.KEY_ARCHIVE, NativeDefinitions.KEY_PROGRAM, NativeDefinitions.KEY_CHANNEL, NativeDefinitions.KEY_FAVORITES, NativeDefinitions.KEY_EPG, NativeDefinitions.KEY_PVR, NativeDefinitions.KEY_MHP, NativeDefinitions.KEY_LANGUAGE, NativeDefinitions.KEY_TITLE, NativeDefinitions.KEY_SUBTITLE, NativeDefinitions.KEY_ANGLE, NativeDefinitions.KEY_ZOOM, NativeDefinitions.KEY_MODE, NativeDefinitions.KEY_KEYBOARD, NativeDefinitions.KEY_SCREEN, NativeDefinitions.KEY_PC, NativeDefinitions.KEY_TV, NativeDefinitions.KEY_TV2, NativeDefinitions.KEY_VCR, NativeDefinitions.KEY_VCR2, NativeDefinitions.KEY_SAT, NativeDefinitions.KEY_SAT2, NativeDefinitions.KEY_CD, NativeDefinitions.KEY_TAPE, NativeDefinitions.KEY_RADIO, NativeDefinitions.KEY_TUNER, NativeDefinitions.KEY_PLAYER, NativeDefinitions.KEY_TEXT, NativeDefinitions.KEY_DVD, NativeDefinitions.KEY_AUX, NativeDefinitions.KEY_MP3, NativeDefinitions.KEY_AUDIO, NativeDefinitions.KEY_VIDEO, NativeDefinitions.KEY_DIRECTORY, NativeDefinitions.KEY_LIST, NativeDefinitions.KEY_MEMO, NativeDefinitions.KEY_CALENDAR, NativeDefinitions.KEY_RED, NativeDefinitions.KEY_GREEN, NativeDefinitions.KEY_YELLOW, NativeDefinitions.KEY_BLUE, NativeDefinitions.KEY_CHANNELUP, NativeDefinitions.KEY_CHANNELDOWN, NativeDefinitions.KEY_FIRST, NativeDefinitions.KEY_LAST, NativeDefinitions.KEY_AB, NativeDefinitions.KEY_NEXT, NativeDefinitions.KEY_RESTART, NativeDefinitions.KEY_SLOW, NativeDefinitions.KEY_SHUFFLE, NativeDefinitions.KEY_BREAK, NativeDefinitions.KEY_PREVIOUS, NativeDefinitions.KEY_DIGITS, NativeDefinitions.KEY_TEEN, NativeDefinitions.KEY_TWEN, NativeDefinitions.KEY_DEL_EOL, NativeDefinitions.KEY_DEL_EOS, NativeDefinitions.KEY_INS_LINE, NativeDefinitions.KEY_DEL_LINE, NativeDefinitions.KEY_FN, NativeDefinitions.KEY_FN_ESC, NativeDefinitions.KEY_FN_F1, NativeDefinitions.KEY_FN_F2, NativeDefinitions.KEY_FN_F3, NativeDefinitions.KEY_FN_F4, NativeDefinitions.KEY_FN_F5, NativeDefinitions.KEY_FN_F6, NativeDefinitions.KEY_FN_F7, NativeDefinitions.KEY_FN_F8, NativeDefinitions.KEY_FN_F9, NativeDefinitions.KEY_FN_F10, NativeDefinitions.KEY_FN_F11, NativeDefinitions.KEY_FN_F12, NativeDefinitions.KEY_FN_1, NativeDefinitions.KEY_FN_2, NativeDefinitions.KEY_FN_D, NativeDefinitions.KEY_FN_E, NativeDefinitions.KEY_FN_F, NativeDefinitions.KEY_FN_S, NativeDefinitions.KEY_FN_B ->
                    Controller.Type.KEYBOARD;
            default -> Controller.Type.UNKNOWN;
        };
    }

    /** Return port type from a native port type int id
     * @param nativeid The native port type
     * @return The jinput port type
     */
    public static Controller.PortType getPortType(int nativeid) {
        // Have to do this one this way as there is no BUS_MAX
        return switch (nativeid) {
            case NativeDefinitions.BUS_GAMEPORT -> Controller.PortType.GAME;
            case NativeDefinitions.BUS_I8042 -> Controller.PortType.I8042;
            case NativeDefinitions.BUS_PARPORT -> Controller.PortType.PARALLEL;
            case NativeDefinitions.BUS_RS232 -> Controller.PortType.SERIAL;
            case NativeDefinitions.BUS_USB -> Controller.PortType.USB;
            default -> Controller.PortType.UNKNOWN;
        };
    }

    /** Gets the identifier for a relative axis
     * @param nativeID The axis type ID
     * @return The jinput id
     */
    public static Component.Identifier getRelAxisID(int nativeID) {
        Component.Identifier retval = null;
        try {
            retval = INSTANCE.relAxesIDs[nativeID];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning("INSTANCE.relAxesIDis only " + INSTANCE.relAxesIDs.length + " long, so " + nativeID + " not contained");
            //ignore, pretend it was null
        }
        if (retval == null) {
            retval = Component.Identifier.Axis.SLIDER_VELOCITY;
        }
        return retval;
    }

    /** Gets the identifier for a absolute axis
     * @param nativeID The native axis type id
     * @return The jinput id
     */
    public static Component.Identifier getAbsAxisID(int nativeID) {
        Component.Identifier retval = null;
        try {
            retval = INSTANCE.absAxesIDs[nativeID];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning("INSTANCE.absAxesIDs is only " + INSTANCE.absAxesIDs.length + " long, so " + nativeID + " not contained");
            //ignore, pretend it was null
        }
        if (retval == null) {
            retval = Component.Identifier.Axis.SLIDER;
        }
        return retval;
    }

    /** Gets the identifier for a button
     * @param nativeID The native button type id
     * @return The jinput id
     */
    public static Component.Identifier getButtonID(int nativeID) {
        Component.Identifier retval = null;
        try {
            retval = INSTANCE.buttonIDs[nativeID];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning("INSTANCE.buttonIDs is only " + INSTANCE.buttonIDs.length + " long, so " + nativeID + " not contained");
            //ignore, pretend it was null
        }
        if (retval == null) {
            retval = Component.Identifier.Button.UNKNOWN;
        }
        return retval;
    }
}
