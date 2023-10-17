/*
 * %W% %E%
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materails provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMEN, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DESTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 *
 */

package net.java.games.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import vavix.rococoa.corefoundation.CFLib;
import vavix.rococoa.iokit.IOKitLib;
import vavix.rococoa.iokit.IOKitLib.IOCFPlugInInterface;

import static vavix.rococoa.iokit.IOKitLib.INSTANCE;
import static vavix.rococoa.iokit.IOKitLib.IO_OBJECT_NULL;
import static vavix.rococoa.iokit.IOKitLib.MACH_PORT_NULL;
import static vavix.rococoa.iokit.IOKitLib.kIOCFPlugInInterfaceID;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDDeviceKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDDeviceUserClientTypeID;
import static vavix.rococoa.iokit.IOKitLib.kIOMainPortDefault;
import static vavix.rococoa.iokit.IOKitLib.kIOReturnSuccess;


/**
 * OSX HIDManager implementation
 *
 * @author elias
 * @author gregorypierce
 * @version 1.0
 */
final class OSXHIDDeviceIterator {

    private static final Logger log = Logger.getLogger(OSXHIDDeviceIterator.class.getName());

    private final Pointer iterator_address;

    public OSXHIDDeviceIterator() throws IOException {
        PointerByReference /* io_iterator_t */ hidObjectIterator = new PointerByReference();
        // Set up a matching dictionary to search the I/O Registry by class
        // name for all HID class devices
        Pointer /* CFMutableDictionaryRef */ hidMatchDictionary = INSTANCE.IOServiceMatching(kIOHIDDeviceKey);

        // Now search I/O Registry for matching devices.
        // IOServiceGetMatchingServices consumes a reference to the dictionary so we don't have to release it
        int ioReturnValue = INSTANCE.IOServiceGetMatchingServices(kIOMainPortDefault, hidMatchDictionary, hidObjectIterator);

        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Failed to create iterator " + ioReturnValue);
        }

        if (hidObjectIterator.getValue() == IO_OBJECT_NULL) {
            throw new IOException("Failed to create iterator");
        }

log.finer("iterator: " + hidObjectIterator.getValue());
        this.iterator_address = hidObjectIterator.getValue();
    }

    public void close() {
        INSTANCE.IOObjectRelease(iterator_address);
    }

    public OSXHIDDevice next() throws IOException {
        Pointer /* io_object_t */ hidDevice;

        hidDevice = INSTANCE.IOIteratorNext(iterator_address);
        if (hidDevice == null)
            return null;

ByteBuffer /* io_name_t */ path = ByteBuffer.allocate(512);
int /* IOResult */ result = IOKitLib.INSTANCE.IORegistryEntryGetPath(hidDevice, IOKitLib.kIOServicePlane, path);
if (result != IOKitLib.KERN_SUCCESS) {
 IOKitLib.INSTANCE.IOObjectRelease(hidDevice);
 throw new IOException("Failed to get device path " + result);
}
log.finer("IORegistryEntryGetPath: " + new String(path.array()).replace("\u0000", ""));

        Pointer /* HidDeviceInterface** */ device_interface = createHIDDevice(hidDevice);
log.finer("device_interface: " + device_interface.toString());
        if (device_interface == MACH_PORT_NULL) {
            INSTANCE.IOObjectRelease(hidDevice);
log.fine("device_interface is MACH_PORT_NULL");
            return null;
        }

        OSXHIDDevice device_object = new OSXHIDDevice(hidDevice, device_interface);
//		if (device_object == null) {
//			device_interface.Release();
//			library.IOObjectRelease(hidDevice);
//			return null;
//		}

        return device_object;
    }

    private static Pointer /* HidDeviceInterface** */ createHIDDevice(Pointer /* io_object_t */ hidDevice) throws IOException {
        PointerByReference /* HidDeviceInterface** */ ppHidDeviceInterface = new PointerByReference();
        IntByReference score = new IntByReference();
        PointerByReference /* IOCFPlugInInterface** */ ppPlugInInterface = new PointerByReference();

ByteBuffer /*io_name_t*/ className = ByteBuffer.allocate(512);
int ioReturnValue0 = INSTANCE.IOObjectGetClass(hidDevice, className);
if (ioReturnValue0 != kIOReturnSuccess) {
 log.fine("Failed to get IOObject class name.");
}
log.finer("Found device type: " + new String(className.array()).replace("\u0000", ""));

log.finer("kIOHIDDeviceUserClientTypeID\n" + kIOHIDDeviceUserClientTypeID.dump(0, 32));
log.finer("kIOCFPlugInInterfaceID\n" + kIOCFPlugInInterfaceID.dump(0, 32));

        int ioReturnValue = INSTANCE.IOCreatePlugInInterfaceForService(hidDevice,
                kIOHIDDeviceUserClientTypeID,
                kIOCFPlugInInterfaceID,
                ppPlugInInterface,
                score);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Couldn't create plugin for device interface %08x", ioReturnValue));
        }

        // Call a method of the intermediate plug-in to create the device
        // interface
log.finer(ppPlugInInterface.getValue().getPointer(0).dump(0, 64));
        IOCFPlugInInterface plugInInterface = new IOCFPlugInInterface(ppPlugInInterface.getValue().getPointer(0));
log.finer(plugInInterface.toString());
log.finer("CFUUIDGetUUIDBytes(kIOHIDDeviceInterfaceID):\n" + CFLib.INSTANCE.CFUUIDGetUUIDBytes(IOKitLib.kIOHIDDeviceInterfaceID).getPointer().dump(0, 16));
        int plugInResult = plugInInterface.queryInterface.invoke(
                ppPlugInInterface.getValue(),
                CFLib.INSTANCE.CFUUIDGetUUIDBytes(IOKitLib.kIOHIDDeviceInterfaceID),
                ppHidDeviceInterface);
        plugInInterface.release.invoke(ppPlugInInterface.getValue());
        if (plugInResult != CFLib.S_OK) {
            throw new IOException(String.format("Couldn't create HID class device interface %08x", plugInResult));
        }

        return ppHidDeviceInterface.getValue();
    }
}
