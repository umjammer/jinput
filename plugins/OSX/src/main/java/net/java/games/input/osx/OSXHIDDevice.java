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
import vavix.rococoa.corefoundation.CFAllocator;
import vavix.rococoa.corefoundation.CFDictionary;
import vavix.rococoa.iokit.IOKitLib;
import vavix.rococoa.iokit.IOKitLib.IOHIDDeviceInterface;
import vavix.rococoa.kernel.KernelLib;

import static net.java.games.input.osx.NativeUtil.copyEvent;
import static net.java.games.input.osx.NativeUtil.createMapFromCFDictionary;
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

    private final static Logger log = Logger.getLogger(OSXHIDDevice.class.getName());

    private final static int AXIS_DEFAULT_MIN_VALUE = 0;
    private final static int AXIS_DEFAULT_MAX_VALUE = 64 * 1024;

    private final Pointer device_address;
    private final Pointer/*IOHIDDeviceInterface**/ device_interface_address;
    private final IOHIDDeviceInterface device_interface;
    private final Map<String, ?> properties;

    private boolean released;

    public OSXHIDDevice(Pointer device_address, Pointer/*IOHIDDeviceInterface**/ device_interface_address) throws IOException {
        this.device_address = device_address;
        this.device_interface_address = device_interface_address;
        this.device_interface = new IOHIDDeviceInterface(device_interface_address.getPointer(0));
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

    private OSXHIDElement createElementFromElementProperties(Map<String, ?> element_properties) {
//		long size = getLongFromProperties(element_properties, kIOHIDElementSizeKey);
//		// ignore elements that can't fit into the 32 bit value field of a hid event
//		if (size > 32)
//			return null;
        Pointer element_cookie = new Pointer(getLongFromProperties(element_properties, kIOHIDElementCookieKey));
        int element_type_id = getIntFromProperties(element_properties, kIOHIDElementTypeKey);
        ElementType element_type = ElementType.map(element_type_id);
        int min = (int) getLongFromProperties(element_properties, kIOHIDElementMinKey, AXIS_DEFAULT_MIN_VALUE);
        int max = (int) getLongFromProperties(element_properties, kIOHIDElementMaxKey, AXIS_DEFAULT_MAX_VALUE);
//		long scaled_min = getLongFromProperties(element_properties, kIOHIDElementScaledMinKey, Long.MIN_VALUE);
//		long scaled_max = getLongFromProperties(element_properties, kIOHIDElementScaledMaxKey, Long.MAX_VALUE);
        UsagePair device_usage_pair = getUsagePair();
        boolean default_relative = device_usage_pair != null && (device_usage_pair.getUsage() == GenericDesktopUsage.POINTER || device_usage_pair.getUsage() == GenericDesktopUsage.MOUSE);

        boolean is_relative = getBooleanFromProperties(element_properties, kIOHIDElementIsRelativeKey, default_relative);
//		boolean is_wrapping = getBooleanFromProperties(element_properties, kIOHIDElementIsWrappingKey);
//		boolean is_non_linear = getBooleanFromProperties(element_properties, kIOHIDElementIsNonLinearKey);
//		boolean has_preferred_state = getBooleanFromProperties(element_properties, kIOHIDElementHasPreferredStateKey);
//		boolean has_null_state = getBooleanFromProperties(element_properties, kIOHIDElementHasNullStateKey);
        int usage = getIntFromProperties(element_properties, kIOHIDElementUsageKey);
        int usage_page = getIntFromProperties(element_properties, kIOHIDElementUsagePageKey);
        UsagePair usage_pair = createUsagePair(usage_page, usage);
        log.finer("element_type = 0x" + element_type + " | usage = " + usage + " | usage_page = " + usage_page);
        if (usage_pair == null || (element_type != ElementType.INPUT_MISC && element_type != ElementType.INPUT_BUTTON && element_type != ElementType.INPUT_AXIS)) {
            //log.info("element_type = 0x" + element_type + " | usage = " + usage + " | usage_page = " + usage_page);
            return null;
        } else {
            return new OSXHIDElement(this, usage_pair, element_cookie, element_type, min, max, is_relative);
        }
    }

    @SuppressWarnings("unchecked")
    private void addElements(List<OSXHIDElement> elements, Map<String, ?> properties) {
        Object[] elements_properties = (Object[]) properties.get(kIOHIDElementKey);
        if (elements_properties == null) {
            log.finer("no elements");
            return;
        }
        log.finer("elements_properties: " + elements_properties.length);
        for (Object elementsProperty : elements_properties) {
            Map<String, ?> element_properties = (Map<String, ?>) elementsProperty;
            log.finer("element_properties: " + element_properties);
            if (element_properties == null) continue;
            OSXHIDElement element = createElementFromElementProperties(element_properties);
            if (element != null) {
                elements.add(element);
            }
            addElements(elements, element_properties);
        }
    }

    public List<OSXHIDElement> getElements() {
        List<OSXHIDElement> elements = new ArrayList<>();
        addElements(elements, properties);
        return elements;
    }

    private static long getLongFromProperties(Map<String, ?> properties, String key, long default_value) {
        Long long_obj = (Long) properties.get(key);
        if (long_obj == null)
            return default_value;
        return long_obj;
    }

    private static boolean getBooleanFromProperties(Map<String, ?> properties, String key, boolean default_value) {
        Object v = properties.get(key);
        return v != null ? (boolean) v : default_value;
    }

    private static int getIntFromProperties(Map<String, ?> properties, String key) {
        return (int) getLongFromProperties(properties, key);
    }

    private static long getLongFromProperties(Map<String, ?> properties, String key) {
        log.finer("key: " + key + ", value: " + properties.get(key));
        Object v = properties.get(key);
        return v != null ? (long) v : 0;
    }

    private static UsagePair createUsagePair(int usage_page_id, int usage_id) {
        UsagePage usage_page = UsagePage.map(usage_page_id);
        if (usage_page != null) {
            Usage usage = usage_page.mapUsage(usage_id);
            if (usage != null)
                return new UsagePair(usage_page, usage);
        }
        return null;
    }

    public final UsagePair getUsagePair() {
        int usage_page_id = getIntFromProperties(properties, kIOHIDPrimaryUsagePageKey);
        int usage_id = getIntFromProperties(properties, kIOHIDPrimaryUsageKey);
        return createUsagePair(usage_page_id, usage_id);
    }

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
        for (Object key : map.keySet()) {
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

    private Map<String, ?> getDeviceProperties() throws IOException {
        PointerByReference pProperties = new PointerByReference();

        int result = INSTANCE.IORegistryEntryCreateCFProperties(
                device_address,
                pProperties,
                CFAllocator.kCFAllocatorDefault,
                KernelLib.kNilOptions);
        if (result != IOKitLib.KERN_SUCCESS) {
            throw new IOException("Failed to create properties for device " + result);
        }
        CFDictionary properties = new CFDictionary(pProperties.getValue());
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, ?> map = (Map) createMapFromCFDictionary(properties);
        log.finer("IORegistryEntryCreateCFProperties: " + map);
        return map;
    }

    public synchronized void release() throws IOException {
        try {
            close();
        } finally {
            released = true;
            device_interface.release.invoke(device_interface_address);
            INSTANCE.IOObjectRelease(device_address);
        }
    }

    public synchronized void getElementValue(Pointer/*IOHIDElementCookie*/ element_cookie, OSXEvent event_return) throws IOException {
        checkReleased();
        IOHIDEventStruct.ByReference event = new IOHIDEventStruct.ByReference();

        int ioReturnValue = device_interface.getElementValue.invoke(device_interface_address, element_cookie, event);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' getElementValue failed: %x", getProductName(), ioReturnValue));
        }
        copyEvent(event, event_return);
    }

    public synchronized OSXHIDQueue createQueue(int queue_depth) throws IOException {
        checkReleased();
        Pointer /* IOHIDQueueInterface** */ queue_address = device_interface.allocQueue.invoke(device_interface_address);
        if (queue_address == Pointer.NULL) {
            throw new IOException("Could not allocate queue");
        }
        return new OSXHIDQueue(queue_address, queue_depth);
    }

    private void open() throws IOException {
        int ioReturnValue = device_interface.open.invoke(device_interface_address, 0);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' open failed: %x", getProductName(), ioReturnValue));
        }
    }

    private void close() throws IOException {
        int ioReturnValue = device_interface.close.invoke(device_interface_address);
        if (ioReturnValue != IOKitLib.kIOReturnSuccess) {
            throw new IOException(String.format("Device '%s' close failed: %x", getProductName(), ioReturnValue));
        }
    }

    private void checkReleased() throws IOException {
        if (released)
            throw new IOException();
    }
}