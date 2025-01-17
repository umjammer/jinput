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


/**
 * Java wrapper of RAWKEYBOARD
 *
 * @author elias
 * @version 1.0
 */
final class RawKeyboardEvent {

    private long millis;
    private int makeCode;
    private int flags;
    private int vkey;
    private int message;
    private long extraInformation;

    public void set(long millis, int makeCode, int flags, int vkey, int message, long extraInformation) {
        this.millis = millis;
        this.makeCode = makeCode;
        this.flags = flags;
        this.vkey = vkey;
        this.message = message;
        this.extraInformation = extraInformation;
    }

    public void set(RawKeyboardEvent event) {
        set(event.millis, event.makeCode, event.flags, event.vkey, event.message, event.extraInformation);
    }

    public int getVKey() {
        return vkey;
    }

    public int getMessage() {
        return message;
    }

    public long getNanos() {
        return millis * 1000000L;
    }
}
