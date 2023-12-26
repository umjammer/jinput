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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.ptr.PointerByReference;
import net.java.games.input.windows.WinAPI.DIDEVICEINSTANCE;
import net.java.games.input.windows.WinAPI.DirectInput8Interface;
import net.java.games.input.windows.WinAPI.IDirectInput8;
import net.java.games.input.windows.WinAPI.User32Ex;

import static net.java.games.input.windows.WinAPI.DI8DEVCLASS_ALL;
import static net.java.games.input.windows.WinAPI.DIEDFL_ATTACHEDONLY;
import static net.java.games.input.windows.WinAPI.DIENUM_CONTINUE;
import static net.java.games.input.windows.WinAPI.DIENUM_STOP;
import static net.java.games.input.windows.WinAPI.DIRECTINPUT_VERSION;
import static net.java.games.input.windows.WinAPI.IID_IDirectInput8;


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
    private final Pointer directInputAddress;
    private final DummyWindow window;

    public IDirectInput(DummyWindow window) throws IOException {
        this.window = window;
        this.directInputAddress = createIDirectInput();
        try {
            enumDevices();
        } catch (IOException e) {
            releaseDevices();
            release();
            throw e;
        }
    }

    private static Pointer createIDirectInput() throws IOException {
        PointerByReference pDirectInput = new PointerByReference();
        int /* HRESULT */ res = DirectInput8Interface.INSTANCE.DirectInput8Create(
                User32Ex.INSTANCE.GetModuleHandle(null), DIRECTINPUT_VERSION,
                new Guid.GUID.ByValue(IID_IDirectInput8), pDirectInput, null);
        if (res < 0) {
            throw new IOException(String.format("Failed to create IDirectInput8 (%d)", res));
        }
        return pDirectInput.getValue();
    }

    public List<IDirectInputDevice> getDevices() {
        return devices;
    }

    private void enumDevices() throws IOException {
        nEnumDevices(directInputAddress);
    }

    static class EnumContext extends Structure {
        public int id;

        public EnumContext() {
        }

        public EnumContext(Pointer p) {
            super(p);
        }

        static int idMaster = 0;
        static Map<Integer, IDirectInput> map = new HashMap<>();
    }

    private static boolean enumerateDevicesCallback(DIDEVICEINSTANCE device, LPVOID context) {
        EnumContext enumContext = new EnumContext(context.getPointer());
        IDirectInput _this = EnumContext.map.get(enumContext.id);
        PointerByReference /* LPDIRECTINPUTDEVICE8 */ pDevice = new PointerByReference();

        byte[] instanceGuid = NativeUtil.wrapGUID(device.guidInstance);
        byte[] productGuid = NativeUtil.wrapGUID(device.guidProduct);
        String instanceName = new String(device.tszInstanceName, StandardCharsets.UTF_8);
        String productName = new String(device.tszProductName, StandardCharsets.UTF_8);

        IDirectInput8 directInput8 = new IDirectInput8(_this.directInputAddress);
        int /* HRESULT */ res = directInput8.CreateDevice.apply(new Guid.GUID.ByValue(device.guidInstance), pDevice, null);
        if (res < 0) {
            log.warning(String.format("Failed to create device (%d)", res));
            return DIENUM_STOP;
        }

        int deviceType = device.dwDevType & 0xff; // GET_DIDEVICE_TYPE
        int deviceSubtype = (device.dwDevType >> 8) & 0xff; // GET_DIDEVICE_SUBTYPE

        _this.addDevice(pDevice.getValue(), instanceGuid, productGuid, deviceType, deviceSubtype, instanceName, productName);

        return DIENUM_CONTINUE;
    }

    private void nEnumDevices(Pointer address) throws IOException {
        IDirectInput8 directInput8 = new IDirectInput8(address);

        EnumContext enumContext = new EnumContext();
        enumContext.id = EnumContext.idMaster++;
        EnumContext.map.put(enumContext.id, this);
        int /* HRESULT */ res = directInput8.EnumDevices.apply(DI8DEVCLASS_ALL, IDirectInput::enumerateDevicesCallback, enumContext.getPointer(), DIEDFL_ATTACHEDONLY);
        if (res < 0) {
            throw new IOException(String.format("Failed to enumerate devices (%d)", res));
        }
    }

    /**
     * This method is called from native code in nEnumDevices
     * native side will clean up in case of an exception
     */
    private void addDevice(Pointer address, byte[] instanceGuid, byte[] productGuid, int devType, int devSubtype, String instanceName, String productName) {
        try {
            IDirectInputDevice device = new IDirectInputDevice(window, address, instanceGuid, productGuid, devType, devSubtype, instanceName, productName);
            devices.add(device);
        } catch (IOException e) {
            log.fine("Failed to initialize device " + productName + " because of: " + e);
        }
    }

    public void releaseDevices() {
        for (IDirectInputDevice device : devices) {
            device.release();
        }
    }

    public void release() {
        nRelease(directInputAddress);
    }

    private static void nRelease(Pointer address) {
        IDirectInput8 directInput8 = new IDirectInput8(address);
        directInput8.Release.apply(address);
    }
}
