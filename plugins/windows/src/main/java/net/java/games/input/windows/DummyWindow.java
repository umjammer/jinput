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

import java.io.IOException;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.ATOM;
import com.sun.jna.platform.win32.WinDef.HBRUSH;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.ptr.IntByReference;

import static com.sun.jna.platform.win32.User32.WS_EX_TOPMOST;
import static com.sun.jna.platform.win32.WinUser.WS_ICONIC;
import static com.sun.jna.platform.win32.WinUser.WS_POPUP;
import static net.java.games.input.windows.WinAPI.COLOR_WINDOW;
import static net.java.games.input.windows.WinAPI.CS_HREDRAW;
import static net.java.games.input.windows.WinAPI.CS_VREDRAW;


/**
 * Java wrapper for a (dummy) window
 *
 * @author martak
 * @author elias
 * @version 1.0
 */
final class DummyWindow {

    private final HWND hwndAddress;

    public DummyWindow() throws IOException {
        this.hwndAddress = createWindow();
    }

    private static final String DUMMY_WINDOW_NAME = "JInputControllerWindow";

    private static final Callback DummyWndProc = new Callback() {
        public LRESULT apply(HWND hWnd, int message, WPARAM wParam, LPARAM lParam) {
            return User32.INSTANCE.DefWindowProc(hWnd, message, wParam, lParam);
        }
    };

    private static boolean RegisterDummyWindow(HINSTANCE hInstance) {
        HBRUSH brush = new HBRUSH();
        brush.setPointer(new IntByReference(COLOR_WINDOW + 1).getPointer());
        WNDCLASSEX wcex = new WNDCLASSEX();
        wcex.cbSize = wcex.size();
        wcex.style			= CS_HREDRAW | CS_VREDRAW;
        wcex.lpfnWndProc	= DummyWndProc;
        wcex.cbClsExtra		= 0;
        wcex.cbWndExtra		= 0;
        wcex.hInstance		= hInstance;
        wcex.hIcon			= null;
        wcex.hCursor		= null;
        wcex.hbrBackground	= brush;
        wcex.lpszMenuName	= /* LPCSTR */ null;
        wcex.lpszClassName	= DUMMY_WINDOW_NAME;
        wcex.hIconSm		= null;
        ATOM r = User32.INSTANCE.RegisterClassEx(wcex);
        return r.intValue() != 0;
    }

    private static HWND createWindow() throws IOException {
        HINSTANCE hInst = Kernel32.INSTANCE.GetModuleHandle(null);
        HWND hwndDummy;
        WNDCLASSEX classInfo = new WNDCLASSEX();
        classInfo.cbSize = classInfo.size();
        classInfo.cbClsExtra = 0;
        classInfo.cbWndExtra = 0;

        if (!User32.INSTANCE.GetClassInfoEx(hInst, DUMMY_WINDOW_NAME, classInfo)) {
            // Register the dummy input window
            if (!RegisterDummyWindow(hInst)) {
                throw new IOException(String.format("Failed to register window class (%d)", Native.getLastError()));
            }
        }

        // Create the dummy input window
        hwndDummy = User32.INSTANCE.CreateWindowEx(WS_EX_TOPMOST, DUMMY_WINDOW_NAME, null,
                WS_POPUP | WS_ICONIC,
                0, 0, 0, 0, null, null, hInst, null);
        if (hwndDummy == null) {
            throw new IOException(String.format("Failed to create window (%d)", Native.getLastError()));
        }
        return hwndDummy;
    }

    public void destroy() throws IOException {
        nDestroy(hwndAddress);
    }

    private static void nDestroy(HWND hwndDummy) throws IOException {
        boolean result = User32.INSTANCE.DestroyWindow(hwndDummy);
        if (!result) {
            throw new IOException(String.format("Failed to destroy window (%d)", Native.getLastError()));
        }
    }

    public HWND getHwnd() {
        return hwndAddress;
    }
}
