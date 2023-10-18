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

package net.java.games.windows;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * Java wrapper for IDirectInput
 *
 * @author martak
 * @author elias
 * @version 1.0
 */
final class IDirectInput {

    private static final Logger log = Logger.getLogger(IDirectInput.class.getName());

    private final List<IDirectInputDevice> devices = new ArrayList<>();
    private final long idirectinput_address;
    private final DummyWindow window;

    public IDirectInput(DummyWindow window) throws IOException {
        this.window = window;
        this.idirectinput_address = createIDirectInput();
        try {
            enumDevices();
        } catch (IOException e) {
            releaseDevices();
            release();
            throw e;
        }
    }

    private static native long createIDirectInput() throws IOException;

    public final List<IDirectInputDevice> getDevices() {
        return devices;
    }

    private final void enumDevices() throws IOException {
        nEnumDevices(idirectinput_address);
    }

    private final native void nEnumDevices(long addr) throws IOException;

    /* This method is called from native code in nEnumDevices
     * native side will clean up in case of an exception
     */
    private final void addDevice(long address, byte[] instance_guid, byte[] product_guid, int dev_type, int dev_subtype, String instance_name, String product_name) throws IOException {
        try {
            IDirectInputDevice device = new IDirectInputDevice(window, address, instance_guid, product_guid, dev_type, dev_subtype, instance_name, product_name);
            devices.add(device);
        } catch (IOException e) {
            log.fine("Failed to initialize device " + product_name + " because of: " + e);
        }
    }

    public final void releaseDevices() {
        for (IDirectInputDevice device : devices) {
            device.release();
        }
    }

    public void release() {
        nRelease(idirectinput_address);
    }

    private static native void nRelease(long address);
}