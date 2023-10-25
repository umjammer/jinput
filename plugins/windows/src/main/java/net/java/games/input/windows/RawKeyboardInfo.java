/*
 * %W% %E%
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*****************************************************************************
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
 *****************************************************************************/

package net.java.games.input.windows;

import java.io.IOException;

import com.sun.jna.platform.win32.WinNT.HANDLE;
import net.java.games.input.Controller;
import net.java.games.input.Rumbler;


/**
 * Java wrapper of RID_DEVICE_INFO_KEYBOARD
 *
 * @author elias
 * @version 1.0
 */
class RawKeyboardInfo extends RawDeviceInfo {

    private final RawDevice device;
    private final int type;
    private final int subType;
    private final int keyboardMode;
    private final int numFunctionKeys;
    private final int numIndicators;
    private final int numKeysTotal;

    public RawKeyboardInfo(RawDevice device, int type, int subType, int keyboardMode, int numFunctionKeys, int numIndicators, int numKeysTotal) {
        this.device = device;
        this.type = type;
        this.subType = subType;
        this.keyboardMode = keyboardMode;
        this.numFunctionKeys = numFunctionKeys;
        this.numIndicators = numIndicators;
        this.numKeysTotal = numKeysTotal;
    }

    @Override
    public final int getUsage() {
        return 6;
    }

    @Override
    public final int getUsagePage() {
        return 1;
    }

    @Override
    public final HANDLE getHandle() {
        return device.getHandle();
    }

    @Override
    public final Controller createControllerFromDevice(RawDevice device, SetupAPIDevice setupapiDevice) throws IOException {
        return new RawKeyboard(setupapiDevice.getName(), device, new Controller[] {}, new Rumbler[] {});
    }
}
