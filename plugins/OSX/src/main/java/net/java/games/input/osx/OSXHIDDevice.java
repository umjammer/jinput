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
 */

package net.java.games.input.osx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import net.java.games.input.Controller;
import net.java.games.input.usb.ElementType;
import net.java.games.input.usb.GenericDesktopUsageId;
import net.java.games.input.usb.UsageId;
import net.java.games.input.usb.UsagePage;
import net.java.games.input.usb.UsagePair;
import vavix.rococoa.corefoundation.CFAllocator;
import vavix.rococoa.corefoundation.CFDictionary;
import vavix.rococoa.iokit.IOKitLib;
import vavix.rococoa.iokit.IOKitLib.IOHIDDeviceInterface;
import vavix.rococoa.kernel.KernelLib;

import static vavix.rococoa.iokit.IOKitLib.INSTANCE;
import static vavix.rococoa.iokit.IOKitLib.IOHIDEventStruct;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementCookieKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementIsRelativeKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementMaxKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementMinKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementTypeKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementUsageKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDElementUsagePageKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDPrimaryUsageKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDPrimaryUsagePageKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDProductKey;
import static vavix.rococoa.iokit.IOKitLib.kIOHIDTransportKey;


/**
 * OSX HIDManager implementation
 *
 * @author elias
 * @author gregorypierce
 * @version 1.0
 */
final class OSXHIDDevice {

    private static final Logger log = Logger.getLogger(OSXHIDDevice.class.getName());

    public static final int AXIS_DEFAULT_MIN_VALUE = 0;
    public static final int AXIS_DEFAULT_MAX_VALUE = 64 * 1024;

    private final Pointer deviceAddress;
    private final Pointer /* IOHIDDeviceInterface* */ deviceInterfaceAddress;
    private final IOHIDDeviceInterface deviceInterface;
    private final Map<String, ?> properties;

    private boolean released;

    /** for reuse one instance */
    OSXEvent osxEvent = new OSXEvent();

    public OSXHIDDevice(Pointer deviceAddress, Pointer /* IOHIDDeviceInterface* */ deviceInterfaceAddress) throws IOException {
        this.deviceAddress = deviceAddress;
        this.deviceInterfaceAddress = deviceInterfaceAddress;
        this.deviceInterface = new IOHIDDeviceInterface(deviceInterfaceAddress.getPointer(0));
        this.properties = getDeviceProperties();
        open();
    }

    public Controller.PortType getPortType() {
        String transport = (String) properties.get(kIOHIDTransportKey);
        if (transport == null)
            return Controller.PortType.UNKNOWN;
        if (transport.equals("USB")) {
            return Controller.PortType.USB;
        } else {
            return Controller.PortType.UNKNOWN;
        }
    }

    public String getProductName() {
        return (String) properties.get(kIOHIDProductKey);
    }

    private OSXHIDElement createElementFromElementProperties(Map<String, ?> elementProperties) {
//		long size = getLongFromProperties(elementProperties, kIOHIDElementSizeKey);
//		// ignore elements that can't fit into the 32 bit value field of a hid event
//		if (size > 32)
//			return null;
        int elementCookie = getIntFromProperties(elementProperties, kIOHIDElementCookieKey);
        int elementTypeId = getIntFromProperties(elementProperties, kIOHIDElementTypeKey);
        ElementType elementType = ElementType.map(elementTypeId);
        int min = (int) getLongFromProperties(elementProperties, kIOHIDElementMinKey, AXIS_DEFAULT_MIN_VALUE);
        int max = (int) getLongFromProperties(elementProperties, kIOHIDElementMaxKey, AXIS_DEFAULT_MAX_VALUE);
//		long scaledMin = getLongFromProperties(elementProperties, kIOHIDElementScaledMinKey, Long.MIN_VALUE);
//		long scaledMax = getLongFromProperties(elementProperties, kIOHIDElementScaledMaxKey, Long.MAX_VALUE);
        UsagePair deviceUsagePair = getUsagePair();
        boolean defaultRelative = deviceUsagePair != null && (deviceUsagePair.usageId() == GenericDesktopUsageId.POINTER || deviceUsagePair.usageId() == GenericDesktopUsageId.MOUSE);

        boolean isRelative = getBooleanFromProperties(elementProperties, kIOHIDElementIsRelativeKey, defaultRelative);
//		boolean isWrapping = getBooleanFromProperties(elementProperties, kIOHIDElementIsWrappingKey);
//		boolean isNonLinear = getBooleanFromProperties(elementProperties, kIOHIDElementIsNonLinearKey);
//		boolean hasPreferredState = getBooleanFromProperties(elementProperties, kIOHIDElementHasPreferredStateKey);
//		boolean hasNullState = getBooleanFromProperties(elementProperties, kIOHIDElementHasNullStateKey);
        int usageId = getIntFromProperties(elementProperties, kIOHIDElementUsageKey);
        int usagePage = getIntFromProperties(elementProperties, kIOHIDElementUsagePageKey);
        UsagePair usagePair = createUsagePair(usagePage, usageId);
log.finer("elementType = 0x" + elementType + " | usageId = " + usageId + " | usagePage = " + usagePage);
        if (usagePair == null || (elementType != ElementType.INPUT_MISC && elementType != ElementType.INPUT_BUTTON && elementType != ElementType.INPUT_AXIS)) {
//log.info("elementType = 0x" + elementType + " | usageId = " + usageId + " | usagePage = " + usagePage);
            return null;
        } else {
            return new OSXHIDElement(this, usagePair, elementCookie, elementType, min, max, isRelative);
        }
    }

    @SuppressWarnings("unchecked")
    private void addElements(List<OSXHIDElement> elements, Map<String, ?> properties) {
        Object[] elementsProperties = (Object[]) properties.get(kIOHIDElementKey);
        if (elementsProperties == null) {
            log.finer("no elements");
            return;
        }
        log.finer("elementsProperties: " + elementsProperties.length);
        for (Object elementsProperty : elementsProperties) {
            Map<String, ?> elementProperties = (Map<String, ?>) elementsProperty;
            log.finer("elementProperties: " + elementProperties);
            if (elementProperties == null) continue;
            OSXHIDElement element = createElementFromElementProperties(elementProperties);
            if (element != null) {
                elements.add(element);
            }
            addElements(elements, elementProperties);
        }
    }

    public List<OSXHIDElement> getElements() {
        List<OSXHIDElement> elements = new ArrayList<>();
        addElements(elements, properties);
        return elements;
    }

    private static long getLongFromProperties(Map<String, ?> properties, String key, long defaultValue) {
        Long longObj = (Long) properties.get(key);
        if (longObj == null)
            return defaultValue;
        return longObj;
    }

    private static boolean getBooleanFromProperties(Map<String, ?> properties, String key, boolean defaultValue) {
        Object v = properties.get(key);
        return v != null ? (boolean) v : defaultValue;
    }

    private static int getIntFromProperties(Map<String, ?> properties, String key) {
        return (int) getLongFromProperties(properties, key);
    }

    private static long getLongFromProperties(Map<String, ?> properties, String key) {
        log.finer("key: " + key + ", value: " + properties.get(key));
        Object v = properties.get(key);
        return v != null ? (long) v : 0;
    }

    private static UsagePair createUsagePair(int usagePageId, int usageId) {
        UsagePage usagePage = UsagePage.map(usagePageId);
        if (usagePage != null) {
            UsageId usage = usagePage.mapUsage(usageId);
            if (usage != null)
                return new UsagePair(usagePage, usage);
        }
        return null;
    }

    public UsagePair getUsagePair() {
        int usagePageId = getIntFromProperties(properties, kIOHIDPrimaryUsagePageKey);
        int usageId = getIntFromProperties(properties, kIOHIDPrimaryUsageKey);
        return createUsagePair(usagePageId, usageId);
    }

//#region debug

    private void dumpProperties() {
        log.info(toString());
        dumpMap("", properties);
    }

    private static void dumpArray(String prefix, Object[] array) {
        log.info(prefix + "{");
        for (Object o : array) {
            dumpObject(prefix + "\t", o);
            log.info(prefix + ",");
        }
        log.info(prefix + "}");
    }

    private static void dumpMap(String prefix, Map<String, ?> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            dumpObject(prefix, key);
            dumpObject(prefix + "\t", value);
        }
    }

    @SuppressWarnings("unchecked")
    private static void dumpObject(String prefix, Object obj) {
        if (obj instanceof Long l) {
            log.info(prefix + "0x" + Long.toHexString(l));
        } else if (obj instanceof Map)
            dumpMap(prefix, (Map<String, ?>) obj);
        else if (obj != null && obj.getClass().isArray())
            dumpArray(prefix, (Object[]) obj);
        else
            log.info(prefix + obj);
    }

//#endregion

    private Map<String, ?> getDeviceProperties() throws IOException {
        PointerByReference pProperties = new PointerByReference();

        int result = INSTANCE.IORegistryEntryCreateCFProperties(
                deviceAddress,
                pProperties,
                CFAllocator.kCFAllocatorDefault,
                KernelLib.kNilOptions);
        if (result != IOKitLib.KERN_SUCCESS) {
            throw new IOException("Failed to create properties for device " + result);
        }
        CFDictionary properties = new CFDictionary(pProperties.getValue());
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, ?> map = (Map) properties.toMap();
        log.finer("IORegistryEntryCreateCFProperties: " + map);
        return map;
    }

    public synchronized void release() throws IOException {
        try {
            close();
        } finally {
            released = true;
            deviceInterface.release.invoke(deviceInterfaceAddress);
            INSTANCE.IOObjectRelease(deviceAddress);
        }
    }

    /** @param event value will be filled */
    synchronized void fillElementValue(int /* IOHIDElementCookie */ elementCookie, OSXEvent event) throws IOException {
        checkReleased();
        IOHIDEventStruct.ByReference nativeEvent = new IOHIDEventStruct.ByReference();

        int ioReturnValue = deviceInterface.getElementValue.invoke(deviceInterfaceAddress, elementCookie, nativeEvent);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' getElementValue failed: %x", getProductName(), ioReturnValue));
        }
        event.set(nativeEvent);
    }

    synchronized OSXHIDQueue createQueue(int queueDepth) throws IOException {
        checkReleased();
        Pointer /* IOHIDQueueInterface** */ queueAddress = deviceInterface.allocQueue.invoke(deviceInterfaceAddress);
        if (queueAddress == Pointer.NULL) {
            throw new IOException("Could not allocate queue");
        }
        return new OSXHIDQueue(queueAddress, queueDepth);
    }

    private void open() throws IOException {
        int ioReturnValue = deviceInterface.open.invoke(deviceInterfaceAddress, 0);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' open failed: %x", getProductName(), ioReturnValue));
        }
    }

    private void close() throws IOException {
        int ioReturnValue = deviceInterface.close.invoke(deviceInterfaceAddress);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' close failed: %x", getProductName(), ioReturnValue));
        }
    }

    private void checkReleased() throws IOException {
        if (released)
            throw new IOException();
    }
}
