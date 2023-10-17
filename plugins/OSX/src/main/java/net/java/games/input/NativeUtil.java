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
 * the design, ruction, operation or maintenance of any nuclear facility
 *
 */

package net.java.games.input;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.LongByReference;
import vavix.rococoa.corefoundation.CFArray;
import vavix.rococoa.corefoundation.CFBoolean;
import vavix.rococoa.corefoundation.CFDictionary;
import vavix.rococoa.corefoundation.CFIndex;
import vavix.rococoa.corefoundation.CFLib;
import vavix.rococoa.corefoundation.CFNumber;
import vavix.rococoa.corefoundation.CFRange;
import vavix.rococoa.corefoundation.CFString;
import vavix.rococoa.corefoundation.CFType;
import vavix.rococoa.iokit.IOKitLib;

import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberCFIndexType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberCharType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberDoubleType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberFloat32Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberFloat64Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberFloatType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberIntType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberLongLongType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberLongType;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberSInt16Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberSInt32Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberSInt64Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberSInt8Type;
import static vavix.rococoa.corefoundation.CFLib.CFNumberType.kCFNumberShortType;


/**
 * NativeUtil.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-22 nsano initial version <br>
 */
public abstract class NativeUtil {

    private static final Logger log = Logger.getLogger(NativeUtil.class.getName());

    static {
log.finer("CFDictionary: " + CFLib.INSTANCE.CFDictionaryGetTypeID());
log.finer("CFArray: " + CFLib.INSTANCE.CFArrayGetTypeID());
log.finer("CFString: " + CFLib.INSTANCE.CFStringGetTypeID());
log.finer("CFNumber: " + CFLib.INSTANCE.CFNumberGetTypeID());
log.finer("CFData: " + CFLib.INSTANCE.CFDataGetTypeID());
log.finer("CFBoolean: " + CFLib.INSTANCE.CFBooleanGetTypeID());
    }

    private NativeUtil() {
    }

    private static int mapID = 0;

    private static Map<Integer, Map<Object, Object>> maps = new HashMap<>();

    public static class dict_context_t extends Structure {

        public int mapID;

        public dict_context_t() {
        }

        public dict_context_t(Pointer p) {
            super(p);
            mapID = getPointer().getInt(0); // TODO why?
        }

        public static class ByReference extends dict_context_t implements Structure.ByReference {

        }

        public static class ByValue extends dict_context_t implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Collections.singletonList("mapID");
        }
    }

    private static int arrayID = 0;

    private static Map<Integer, Object[]> arrays = new HashMap<>();

    public static class array_context_t extends Structure {

        public int arrayID;
        public int index;

        public array_context_t() {
        }

        public array_context_t(Pointer p) {
            super(p);
            // TODO why?
            arrayID = getPointer().getInt(0);
            index = getPointer().getInt(4);
        }

        public static class ByReference extends array_context_t implements Structure.ByReference {

        }

        public static class ByValue extends array_context_t implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("arrayID", "index");
        }
    }

    private static String createStringFromCFString(CFString cfstring) {
        CFIndex unicode_length = CFLib.INSTANCE.CFStringGetLength(cfstring);
        int utf8_length = CFLib.INSTANCE.CFStringGetMaximumSizeForEncoding(unicode_length, CFLib.kCFStringEncodingUTF8).intValue();
        // Allocate buffer large enough, plus \0 terminator
        ByteBuffer buffer = ByteBuffer.allocate(utf8_length + 1);
        boolean result = CFLib.INSTANCE.CFStringGetCString(cfstring, buffer, new NativeLong(utf8_length + 1), CFLib.kCFStringEncodingUTF8);
        if (!result) {
log.warning("CFStringGetCString: " + result + ", " + cfstring);
            return null;
        }
        String jstring = new String(buffer.array(), 0, utf8_length, StandardCharsets.UTF_8).replace("\u0000", "");
log.finer("string: " + jstring + ", utf8_length: " + utf8_length);
        return jstring;
    }

    private static Object createDoubleObjectFromCFNumber(CFNumber cfnumber) {
        DoubleByReference value = new DoubleByReference();
        boolean result = CFLib.INSTANCE.CFNumberGetValue(cfnumber, kCFNumberDoubleType, value);
        if (!result) {
log.warning("CFNumberGetValue: " + result + ", " + cfnumber);
            return null;
        }
        return value.getValue();
    }

    private static Object createLongObjectFromCFNumber(CFNumber cfnumber) {
        LongByReference value = new LongByReference();
        boolean result = CFLib.INSTANCE.CFNumberGetValue(cfnumber, kCFNumberSInt64Type, value);
        if (!result) {
log.warning("CFNumberGetValue: " + result + ", " + cfnumber);
            return null;
        }
        return value.getValue();
    }

    private static Object createNumberFromCFNumber(CFNumber cfnumber) {
        int /*CFNumberType*/ number_type = CFLib.INSTANCE.CFNumberGetType(cfnumber).intValue();
        return switch (number_type) {
            case kCFNumberSInt8Type, kCFNumberSInt16Type, kCFNumberSInt32Type, kCFNumberSInt64Type,
                    kCFNumberCharType, kCFNumberShortType, kCFNumberIntType, kCFNumberLongType,
                    kCFNumberLongLongType, kCFNumberCFIndexType -> createLongObjectFromCFNumber(cfnumber);
            case kCFNumberFloat32Type, kCFNumberFloat64Type, kCFNumberFloatType, kCFNumberDoubleType ->
                    createDoubleObjectFromCFNumber(cfnumber);
            default -> {
                log.warning("unknown number type: " + number_type + ", " + cfnumber);
                yield null;
            }
        };
    }

    private static void createArrayEntries(CFType value, Pointer context) {
        array_context_t array_context = new array_context_t(context);
        Object jval = createObjectFromCFObject(value);
        Object[] array = arrays.get(array_context.arrayID);
        if (array == null) {
log.warning("no array for id: " + array_context.arrayID);
            return;
        }
log.finer("array(" + array_context.arrayID + "): [" + array_context.index + "] = " + jval);
        array[array_context.index++] = jval;
        array_context.write();
    }

    private static Object createArrayFromCFArray(CFArray cfarray) {
        NativeLong size = CFLib.INSTANCE.CFArrayGetCount(cfarray);
        CFRange.ByValue range = new CFRange.ByValue();
        range.location = CFIndex.of(0);
        range.length = CFIndex.of(size);
        Object[] array = new Object[size.intValue()];
        array_context_t.ByReference array_context = new array_context_t.ByReference();
        array_context.arrayID = arrayID++;
        array_context.index = 0;
        arrays.put(array_context.arrayID, array);
        CFLib.INSTANCE.CFArrayApplyFunction(cfarray, range, NativeUtil::createArrayEntries, array_context);
log.finer("ARRAY(" + array_context.arrayID + "): " + array.length);
        return array;
    }

    private static boolean createBooleanFromCFBoolean(CFBoolean cfboolean) {
        return CFLib.INSTANCE.CFBooleanGetValue(cfboolean);
    }

    static Object createObjectFromCFObject(CFType cfobject) {
        NativeLong /*CFTypeID*/ type_id = CFLib.INSTANCE.CFGetTypeID(cfobject);
        if (type_id.equals(CFLib.INSTANCE.CFDictionaryGetTypeID())) { // 18
            return createMapFromCFDictionary(cfobject.asDict());
        } else if (type_id.equals(CFLib.INSTANCE.CFArrayGetTypeID())) { // 19
            return createArrayFromCFArray(cfobject.asArray());
        } else if (type_id.equals(CFLib.INSTANCE.CFStringGetTypeID())) { // 7
            return createStringFromCFString(cfobject.asString());
        } else if (type_id.equals(CFLib.INSTANCE.CFNumberGetTypeID())) { // 22
            Object n = createNumberFromCFNumber(cfobject.asNumber());
log.finer("number type: " + n);
            return n;
        } else if (type_id.equals(CFLib.INSTANCE.CFBooleanGetTypeID())) { // 21
            boolean b = createBooleanFromCFBoolean(cfobject.asBoolean());
log.finer("boolean type: " + b);
            return b;
        } else if (type_id.equals(CFLib.INSTANCE.CFDataGetTypeID())) { // 20
            return cfobject.asData().getBuffer().array();
        } else {
            log.warning("unknown type: " + type_id + ", " + cfobject);
            return null;
        }
    }

    static void createMapKeys(CFString key, CFType value, Pointer context) {
        dict_context_t dict_context = new dict_context_t(context);
log.finer(dict_context.getPointer().dump(0, dict_context.size()));
//if (key.getString().equals("Elements")) {
// log.fine("Elements: value: " + value.getType());
//}
        Object jkey = createObjectFromCFObject(key);
        Object jvalue = createObjectFromCFObject(value);
log.finer("map(" + dict_context.mapID + "): put: " + jkey + ", " + jvalue);
        if (jkey == null || jvalue == null) {
            log.warning("map: " + dict_context.mapID + ":: put: " + jkey + ", " + jvalue + "(" + (value != null ? value.getType() : "??") + ")");
            return;
        }
        Map<Object, Object> map = maps.get(dict_context.mapID);
        if (map == null) {
log.warning("no map for id: " + dict_context.mapID);
            return;
        }
        map.put(jkey, jvalue);
    }

    static Map<Object, Object> createMapFromCFDictionary(CFDictionary /*StringCFDictionaryRef*/ dict) {
        Map<Object, Object> map = new HashMap<>();
        dict_context_t.ByReference dict_context = new dict_context_t.ByReference();
        dict_context.mapID = mapID++;
        maps.put(dict_context.mapID, map);
        CFLib.INSTANCE.CFDictionaryApplyFunction(dict, NativeUtil::createMapKeys, dict_context);
log.finer("MAP(" + dict_context.mapID + "): " + map.size());
        return map;
    }

    static void copyEvent(IOKitLib.IOHIDEventStruct.ByReference event, OSXEvent event_return) {
        long nanos64 = System.nanoTime();
        event_return.set(event.type, event.elementCookie, event.value, nanos64);
    }
}
