/*
 * TinyUmbrella - making iDevice restores possible...
 * Copyright (C) 2009-2010 semaphore
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package vavix.rococoa.corefoundation;

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
 * Brought to you by:
 *
 * @author semaphore
 * @version May 27, 2010 1:08:16 AM
 */
public class CFDictionary extends CFType {

    private static final Logger log = Logger.getLogger(CFDictionary.class.getName());

    public CFDictionary(Pointer address) {
        super(address);
    }

    public CFDictionary() {
        super();
    }

    public int keyCount(String key) {
        return CFLib.INSTANCE.CFDictionaryGetCountOfKey(this, CFString.buildString(key)).intValue();
    }

    public int valueCount(CFType value) {
        return CFLib.INSTANCE.CFDictionaryGetCountOfValue(this, value).intValue();
    }

    public int size() {
        return CFLib.INSTANCE.CFDictionaryGetCount(this).intValue();
    }

    public CFType getValue(String key) {
        return CFLib.INSTANCE.CFDictionaryGetValue(this, CFString.buildString(key));
    }

    public CFString getString(String key) {
        return new CFString(getValue(key).getPointer());
    }

    public CFNumber getNumber(String key) {
        return new CFNumber(getValue(key).getPointer());
    }

    public CFData getData(String key) {
        return new CFData(getValue(key).getPointer());
    }

    public CFDictionary getDict(String key) {
        return new CFDictionary(getValue(key).getPointer());
    }

    public CFBoolean getBoolean(String key) {
        return new CFBoolean(getValue(key).getPointer());
    }

    public CFArray getArray(String key) {
        return new CFArray(getValue(key).getPointer());
    }

    public CFDate getDate(String key) {
        return new CFDate(getValue(key).getPointer());
    }

//#region utility

    public static class DictContext extends Structure {

        public int mapId;

        public DictContext() {
        }

        public DictContext(Pointer p) {
            super(p);
            mapId = getPointer().getInt(0); // TODO why?
        }

        public static class ByReference extends DictContext implements Structure.ByReference {
        }

        public static class ByValue extends DictContext implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Collections.singletonList("mapId");
        }

        static int mapIdGenerator = 0;

        static final Map<Integer, Map<Object, Object>> maps = new HashMap<>();
    }

    public static class ArrayContext extends Structure {

        public int arrayId;
        public int index;

        public ArrayContext() {
        }

        public ArrayContext(Pointer p) {
            super(p);
            // TODO why?
            arrayId = getPointer().getInt(0);
            index = getPointer().getInt(4);
        }

        public static class ByReference extends ArrayContext implements Structure.ByReference {
        }

        public static class ByValue extends ArrayContext implements Structure.ByValue {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("arrayId", "index");
        }

        static int arrayIdGenerator = 0;

        static final Map<Integer, Object[]> arrays = new HashMap<>();
    }

    private static String createStringFromCFString(CFString cfstring) {
        CFIndex unicodeLength = CFLib.INSTANCE.CFStringGetLength(cfstring);
        int utf8Length = CFLib.INSTANCE.CFStringGetMaximumSizeForEncoding(unicodeLength, CFLib.kCFStringEncodingUTF8).intValue();
        // Allocate buffer large enough, plus \0 terminator
        ByteBuffer buffer = ByteBuffer.allocate(utf8Length + 1);
        boolean result = CFLib.INSTANCE.CFStringGetCString(cfstring, buffer, new NativeLong(utf8Length + 1), CFLib.kCFStringEncodingUTF8);
        if (!result) {
log.warning("CFStringGetCString: " + result + ", " + cfstring);
            return null;
        }
        String string = new String(buffer.array(), 0, utf8Length, StandardCharsets.UTF_8).replace("\u0000", "");
log.finer("string: " + string + ", utf8Length: " + utf8Length);
        return string;
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
        int /* CFNumberType */ numberType = CFLib.INSTANCE.CFNumberGetType(cfnumber).intValue();
        return switch (numberType) {
            case kCFNumberSInt8Type, kCFNumberSInt16Type, kCFNumberSInt32Type, kCFNumberSInt64Type,
                    kCFNumberCharType, kCFNumberShortType, kCFNumberIntType, kCFNumberLongType,
                    kCFNumberLongLongType, kCFNumberCFIndexType -> createLongObjectFromCFNumber(cfnumber);
            case kCFNumberFloat32Type, kCFNumberFloat64Type, kCFNumberFloatType, kCFNumberDoubleType ->
                    createDoubleObjectFromCFNumber(cfnumber);
            default -> {
log.warning("unknown number type: " + numberType + ", " + cfnumber);
                yield null;
            }
        };
    }

    private static void createArrayEntries(CFType value, Pointer context) {
        ArrayContext arrayContext = new ArrayContext(context);
        Object jval = createObjectFromCFObject(value);
        Object[] array = ArrayContext.arrays.get(arrayContext.arrayId);
        if (array == null) {
log.warning("no array for id: " + arrayContext.arrayId);
            return;
        }
log.finer("array(" + arrayContext.arrayId + "): [" + arrayContext.index + "] = " + jval);
        array[arrayContext.index++] = jval;
        arrayContext.write();
    }

    private static Object createArrayFromCFArray(CFArray cfarray) {
        NativeLong size = CFLib.INSTANCE.CFArrayGetCount(cfarray);
        CFRange.ByValue range = new CFRange.ByValue();
        range.location = CFIndex.of(0);
        range.length = CFIndex.of(size);
        Object[] array = new Object[size.intValue()];
        ArrayContext.ByReference arrayContext = new ArrayContext.ByReference();
        arrayContext.arrayId = ArrayContext.arrayIdGenerator++;
        arrayContext.index = 0;
        ArrayContext.arrays.put(arrayContext.arrayId, array);
        CFLib.INSTANCE.CFArrayApplyFunction(cfarray, range, CFDictionary::createArrayEntries, arrayContext);
log.finer("ARRAY(" + arrayContext.arrayId + "): " + array.length);
        return array;
    }

    private static boolean createBooleanFromCFBoolean(CFBoolean cfboolean) {
        return CFLib.INSTANCE.CFBooleanGetValue(cfboolean);
    }

    static Object createObjectFromCFObject(CFType cfobject) {
        NativeLong /* CFTypeID */ typeId = CFLib.INSTANCE.CFGetTypeID(cfobject);
        if (typeId.equals(CFLib.INSTANCE.CFDictionaryGetTypeID())) { // 18
            return cfobject.asDict().toMap();
        } else if (typeId.equals(CFLib.INSTANCE.CFArrayGetTypeID())) { // 19
            return createArrayFromCFArray(cfobject.asArray());
        } else if (typeId.equals(CFLib.INSTANCE.CFStringGetTypeID())) { // 7
            return createStringFromCFString(cfobject.asString());
        } else if (typeId.equals(CFLib.INSTANCE.CFNumberGetTypeID())) { // 22
            Object n = createNumberFromCFNumber(cfobject.asNumber());
            log.finer("number type: " + n);
            return n;
        } else if (typeId.equals(CFLib.INSTANCE.CFBooleanGetTypeID())) { // 21
            boolean b = createBooleanFromCFBoolean(cfobject.asBoolean());
            log.finer("boolean type: " + b);
            return b;
        } else if (typeId.equals(CFLib.INSTANCE.CFDataGetTypeID())) { // 20
            return cfobject.asData().getBuffer().array();
        } else {
            log.warning("unknown type: " + typeId + ", " + cfobject);
            return null;
        }
    }

    static void createMapKeys(CFString key, CFType value, Pointer context) {
        DictContext dictContext = new DictContext(context);
log.finer(dictContext.getPointer().dump(0, dictContext.size()));
//if (key.getString().equals("Elements")) {
// log.fine("Elements: value: " + value.getType());
//}
        Object jkey = createObjectFromCFObject(key);
        Object jvalue = createObjectFromCFObject(value);
log.finer("map(" + dictContext.mapId + "): put: " + jkey + ", " + jvalue);
        if (jkey == null || jvalue == null) {
log.warning("map: " + dictContext.mapId + ":: put: " + jkey + ", " + jvalue + "(" + (value != null ? value.getType() : "??") + ")");
            return;
        }
        Map<Object, Object> map = DictContext.maps.get(dictContext.mapId);
        if (map == null) {
log.warning("no map for id: " + dictContext.mapId);
            return;
        }
        map.put(jkey, jvalue);
    }

    public Map<Object, Object> toMap() {
        Map<Object, Object> map = new HashMap<>();
        DictContext.ByReference dictContext = new DictContext.ByReference();
        dictContext.mapId = DictContext.mapIdGenerator++;
        DictContext.maps.put(dictContext.mapId, map);
        CFLib.INSTANCE.CFDictionaryApplyFunction(this, CFDictionary::createMapKeys, dictContext);
log.finer("MAP(" + dictContext.mapId + "): " + map.size());
        return map;
    }

//#endregion
}
