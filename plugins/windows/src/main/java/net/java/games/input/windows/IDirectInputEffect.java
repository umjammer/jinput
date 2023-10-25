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
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import net.java.games.input.Component;
import net.java.games.input.Rumbler;
import net.java.games.input.windows.User32Ex.DIEFFECT;

import static net.java.games.input.windows.IDirectInputDevice.DIEP_GAIN;


/**
 * Java wrapper for IDirectInputEffect
 *
 * @author elias
 * @version 1.0
 */
final class IDirectInputEffect implements Rumbler {

    private static final Logger log = Logger.getLogger(IDirectInputEffect.class.getName());

    private final Pointer address;
    private final DIEffectInfo info;
    private boolean released;

    public IDirectInputEffect(Pointer address, DIEffectInfo info) {
        this.address = address;
        this.info = info;
    }

    @Override
    public synchronized void rumble(float intensity) {
        try {
            checkReleased();
            if (intensity > 0) {
                int intGain = Math.round(intensity * IDirectInputDevice.DI_FFNOMINALMAX);
                setGain(intGain);
                start(1, 0);
            } else
                stop();
        } catch (IOException e) {
            log.fine("Failed to set rumbler gain: " + e.getMessage());
        }
    }

    @Override
    public Component.Identifier getAxisIdentifier() {
        return null;
    }

    @Override
    public String getAxisName() {
        return null;
    }

    public synchronized void release() {
        if (!released) {
            released = true;

            User32Ex.IDirectInputEffect effect = new User32Ex.IDirectInputEffect(address);
            effect.Release.apply(address);
        }
    }

    private void checkReleased() throws IOException {
        if (released)
            throw new IOException();
    }

    private void setGain(int gain) throws IOException {
        User32Ex.IDirectInputEffect effect = new User32Ex.IDirectInputEffect(address);

        DIEFFECT params = new DIEFFECT();
        params.dwSize = params.size();
        params.dwGain = gain;

        int res = effect.SetParameters.apply(params, DIEP_GAIN);
        if (res != IDirectInputDevice.DI_DOWNLOADSKIPPED &&
                res != IDirectInputDevice.DI_EFFECTRESTARTED &&
                res != IDirectInputDevice.DI_OK &&
                res != IDirectInputDevice.DI_TRUNCATED &&
                res != IDirectInputDevice.DI_TRUNCATEDANDRESTARTED) {
            throw new IOException("Failed to set effect gain (0x" + Integer.toHexString(res) + ")");
        }
    }

    private void start(int iterations, int flags) throws IOException {
        User32Ex.IDirectInputEffect effect = new User32Ex.IDirectInputEffect(address);

        int res = effect.Start.apply(iterations, flags);
        if (res != IDirectInputDevice.DI_OK)
            throw new IOException("Failed to start effect (0x" + Integer.toHexString(res) + ")");
    }

    private void stop() throws IOException {
        User32Ex.IDirectInputEffect effect = new User32Ex.IDirectInputEffect(address);

        int res = effect.Stop.apply();
        if (res != IDirectInputDevice.DI_OK)
            throw new IOException("Failed to stop effect (0x" + Integer.toHexString(res) + ")");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() {
        release();
    }
}
