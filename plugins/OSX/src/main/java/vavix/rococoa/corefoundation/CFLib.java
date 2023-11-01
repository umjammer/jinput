/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.rococoa.corefoundation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.NativeLongByReference;


/**
 * CFLib.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-22 nsano initial version <br>
 */
public interface CFLib extends Library {

    Logger log = Logger.getLogger(CFLib.class.getName());

    CFLib INSTANCE = Native.load("CoreFoundation", CFLib.class);

    NativeLibrary NATIVE_LIBRARY = NativeLibrary.getInstance("CoreFoundation");

    //

    double kCFCoreFoundationVersionNumber = NATIVE_LIBRARY
            .getGlobalVariableAddress("kCFCoreFoundationVersionNumber").getDouble(0);

    /** HRESULT */
    int S_OK = 0;

    int kCFNotFound = -1;

//#region Polymorphic

    NativeLong CFGetTypeID(CFType cf);

    CFString CFCopyDescription(CFType cf);

    CFString CFCopyTypeIDDescription(NativeLong type_id);

    void CFShow(CFType obj);

    /** Releases a Core Foundation object. */
    void CFRelease(CFType cf);

    void CFRelease(Pointer cf);

    void CFRetain(Pointer cf);

//#endregion Polymorphic

//#region CFArray

    class CFArrayCallBacks extends Structure {
        public interface RetqinCallback extends Callback {
            void fn(Pointer a, Pointer b);
        }
        public interface ReleaseCallback extends Callback {
            void fn(Pointer a, Pointer b);
        }
        public interface CopyDescriptionCallback extends Callback {
            Pointer fn(Pointer a);
        }
        public interface EqualCallback extends Callback {
            boolean fn(Pointer a, Pointer b);
        }
        public NativeLong version;
        public RetqinCallback retain;
        public ReleaseCallback release;
        public CopyDescriptionCallback copyDescription;
        public EqualCallback equal;

        public CFArrayCallBacks(Pointer p) {
            super(p);
            version = getPointer().getNativeLong(0);
            retain = (RetqinCallback) CallbackReference.getCallback(RetqinCallback.class, p.getPointer(0x08));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x10));
            copyDescription = (CopyDescriptionCallback) CallbackReference.getCallback(CopyDescriptionCallback.class, p.getPointer(0x18));
            equal = (EqualCallback) CallbackReference.getCallback(EqualCallback.class, p.getPointer(0x20));
            log.fine(this.toString());
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("version", "retain", "release", "copyDescription", "equal");
        }
    }

    CFArrayCallBacks kCFTypeArrayCallBacks = new CFArrayCallBacks(NATIVE_LIBRARY
            .getGlobalVariableAddress("kCFTypeArrayCallBacks"));

    /** Creates a new immutable array with the given values. */
    CFArray CFArrayCreate(CFAllocator allocator, Pointer[] values, CFIndex numValues, CFArrayCallBacks callBacks);

    NativeLong CFArrayGetCount(CFArray theArray);

    CFType CFArrayGetValueAtIndex(CFArray theArray, int idx);

    NativeLong CFArrayGetTypeID();

    interface CFArrayApplierFunction extends Callback {

        void invoke(CFType value, Pointer context);
    }

    void CFArrayApplyFunction(CFArray theArray, Structure.ByValue range, CFArrayApplierFunction applier, Structure.ByReference context);

//#endregion CFArray

//#region CFSet

    /** Returns the number of values currently in a set. */
    CFIndex CFSetGetCount(CFType/*CFSetRef*/ theSet);

    /** Obtains all values in a set. */
    void CFSetGetValues(CFType/*CFSetRef*/ theSet, Pointer[] values);

//#endregion CFSet

//#region CFString

    interface CFComparatorFunction extends Callback {

        /**
         * @see CFComparisonResult
         */
        int invoke(Pointer val1, Pointer val2, Pointer context);
    }

    interface CFComparisonResult {

        int kCFCompareLessThan = -1;
        int kCFCompareEqualTo = 0;
        int kCFCompareGreaterThan = 1;
    }

    int kCFStringEncodingUTF8 = 0x8000100;
    int kCFStringEncodingUTF32LE = 0x1c000100;
    int kCFStringEncodingASCII = 0x0600;

    CFIndex CFStringGetLength(CFString theString);

    boolean CFStringGetCString(CFString theString, ByteBuffer buffer, NativeLong bufferSize, int encoding);

    CFString __CFStringMakeConstantString(String str);

    /** Returns the type identifier for the CFString opaque type. */
    NativeLong CFStringGetTypeID();

    CFIndex CFStringGetMaximumSizeForEncoding(CFIndex length, int/*CFStringEncoding*/ encoding);

    /** Fetches a range of the characters from a string into a byte buffer after converting the characters to a specified encoding. */
    CFIndex CFStringGetBytes(CFString theString, CFRange.ByValue range, int /*CFStringEncoding*/ encoding, byte lossByte, boolean isExternalRepresentation, byte[] buffer, CFIndex maxBufLen, NativeLongByReference/*CFIndexRef*/ usedBufLen);

    /** Creates an immutable string from a C string. */
    CFString CFStringCreateWithCString(CFAllocator alloc, byte[] cStr, int/*CFStringEncoding*/ encoding);

    /** Compares one string with another string. */
    CFIndex /*CFComparisonResult*/ CFStringCompare(CFString theString1, CFString theString2, long/*CFStringCompareFlags*/ compareOptions);

    /** Determines if the character data of a string begin with a specified sequence of characters. */
    boolean CFStringHasPrefix(CFString theString, CFString prefix);

//#endregion CFString

//#region CFBoolean

    boolean CFBooleanGetValue(CFBoolean cfBoolean);

    NativeLong CFBooleanGetTypeID();

//#endregion CFBoolean */

//#region CFNumber

    interface CFNumberType {

        /**
         * Fixed-width types<br>
         * Fixed-width types<br>
         * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:27</i>
         */
        int kCFNumberSInt8Type = 1;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:28</i>
        int kCFNumberSInt16Type = 2;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:29</i>
        int kCFNumberSInt32Type = 3;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:30</i>
        int kCFNumberSInt64Type = 4;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:31</i>
        int kCFNumberFloat32Type = 5;
        /**
         * 64-bit IEEE 754<br>
         * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:32</i>
         */
        int kCFNumberFloat64Type = 6;
        /**
         * Basic C types<br>
         * Basic C types<br>
         * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:34</i>
         */
        int kCFNumberCharType = 7;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:35</i>
        int kCFNumberShortType = 8;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:36</i>
        int kCFNumberIntType = 9;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:37</i>
        int kCFNumberLongType = 10;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:38</i>
        int kCFNumberLongLongType = 11;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:39</i>
        int kCFNumberFloatType = 12;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:40</i>
        int kCFNumberDoubleType = 13;
        /**
         * Other<br>
         * Other<br>
         * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:42</i>
         */
        int kCFNumberCFIndexType = 14;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:44</i>
        int kCFNumberNSIntegerType = 15;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:45</i>
        int kCFNumberCGFloatType = 16;
        /// <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFNumber.h:46</i>
        int kCFNumberMaxType = 16;
    }

    boolean CFNumberGetValue(CFNumber number, int theType, ByReference valuePtr);

    CFIndex CFNumberGetType(CFNumber number);

    NativeLong CFNumberGetTypeID();

    /** Creates a CFNumber object using a specified value. */
    CFNumber CFNumberCreate(CFAllocator allocator, int/*CFNumberType*/ theType, ByReference valuePtr);

//#endregion CFNumber */

//#region CFData

    NativeLong CFDataGetTypeID();

    CFData CFDataCreate(CFAllocator allocator, byte[] bytes, NativeLong length);

    CFData CFDataCreateWithBytesNoCopy(CFAllocator allocator, byte[] bytes, NativeLong length, CFAllocator bytesDeallocator);

    CFData CFDataCreateCopy(CFAllocator allocator, CFData theData);

    CFData CFDataCreateMutable(CFAllocator allocator, NativeLong capacity);

    CFData CFDataCreateMutableCopy(CFAllocator allocator, NativeLong capacity, CFData theData);

    NativeLong CFDataGetLength(CFData theData);

    Pointer CFDataGetBytePtr(CFData theData);

    Pointer CFDataGetMutableBytePtr(CFData theData);

    void CFDataGetBytes(CFData theData, CFRange.ByValue range, ByteBuffer buffer);

    void CFDataSetLength(CFData theData, NativeLong length);

    void CFDataIncreaseLength(CFData theData, NativeLong extraLength);

    void CFDataAppendBytes(CFData theData, byte[] bytes, NativeLong length);

    void CFDataReplaceBytes(CFData theData, CFRange.ByValue range, byte[] newBytes, NativeLong newLength);

    void CFDataDeleteBytes(CFData theData, CFRange.ByValue range);

//#endregion CFData

//#region CFDictionary

    interface CFDictionaryRetainCallBack extends Callback {

        Pointer invoke(CFAllocator allocator, CFType value);
    }

    interface CFDictionaryReleaseCallBack extends Callback {

        void invoke(CFAllocator allocator, CFType value);
    }

    interface CFDictionaryCopyDescriptionCallBack extends Callback {

        CFString invoke(CFType value);
    }

    interface CFDictionaryEqualCallBack extends Callback {

        boolean invoke(CFType value1, CFType value2);
    }

    interface CFDictionaryHashCallBack extends Callback {

        NativeLong invoke(CFType value);
    }

    interface CFDictionaryApplierFunction extends Callback {

        void invoke(CFString key, CFType value, Pointer/*Structure.ByReference*/ context);
    }

    NativeLong CFDictionaryGetTypeID();

    class CFDictionaryKeyCallBacks extends Structure {

        public NativeLong version;
        public CFDictionaryRetainCallBack retain;
        public CFDictionaryReleaseCallBack release;
        public CFDictionaryCopyDescriptionCallBack copyDescription;
        public CFDictionaryEqualCallBack equal;
        public CFDictionaryHashCallBack hash;

        public CFDictionaryKeyCallBacks() {
        }

        public CFDictionaryKeyCallBacks(Pointer p) {
            super(p);

            version = getPointer().getNativeLong(0);
            retain = (CFDictionaryRetainCallBack) CallbackReference.getCallback(CFDictionaryRetainCallBack.class, p.getPointer(0x08));
            release = (CFDictionaryReleaseCallBack) CallbackReference.getCallback(CFDictionaryReleaseCallBack.class, p.getPointer(0x10));
            copyDescription = (CFDictionaryCopyDescriptionCallBack) CallbackReference.getCallback(CFDictionaryCopyDescriptionCallBack.class, p.getPointer(0x18));
            equal = (CFDictionaryEqualCallBack) CallbackReference.getCallback(CFDictionaryEqualCallBack.class, p.getPointer(0x20));
            hash = (CFDictionaryHashCallBack) CallbackReference.getCallback(CFDictionaryHashCallBack.class, p.getPointer(0x28));
        }

        public CFDictionaryKeyCallBacks(NativeLong version, CFDictionaryRetainCallBack retain, CFDictionaryReleaseCallBack release, CFDictionaryCopyDescriptionCallBack copyDescription, CFDictionaryEqualCallBack equal, CFDictionaryHashCallBack hash) {
            this.version = version;
            this.retain = retain;
            this.release = release;
            this.copyDescription = copyDescription;
            this.equal = equal;
            this.hash = hash;
        }

        protected ByReference newByReference() {
            ByReference s = new ByReference();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected ByValue newByValue() {
            ByValue s = new ByValue();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected CFDictionaryKeyCallBacks newInstance() {
            CFDictionaryKeyCallBacks s = new CFDictionaryKeyCallBacks();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        public static class ByReference extends CFDictionaryKeyCallBacks implements Structure.ByReference {

        }

        public static class ByValue extends CFDictionaryKeyCallBacks implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("version", "retain", "release", "copyDescription", "equal", "hash");
        }
    }

    class CFDictionaryValueCallBacks extends Structure {

        public NativeLong version;
        public CFDictionaryRetainCallBack retain;
        public CFDictionaryReleaseCallBack release;
        public CFDictionaryCopyDescriptionCallBack copyDescription;
        public CFDictionaryEqualCallBack equal;

        public CFDictionaryValueCallBacks() {
        }

        public CFDictionaryValueCallBacks(Pointer p) {
            super(p);

            version = getPointer().getNativeLong(0);
            retain = (CFDictionaryRetainCallBack) CallbackReference.getCallback(CFDictionaryRetainCallBack.class, p.getPointer(0x08));
            release = (CFDictionaryReleaseCallBack) CallbackReference.getCallback(CFDictionaryReleaseCallBack.class, p.getPointer(0x10));
            copyDescription = (CFDictionaryCopyDescriptionCallBack) CallbackReference.getCallback(CFDictionaryCopyDescriptionCallBack.class, p.getPointer(0x18));
            equal = (CFDictionaryEqualCallBack) CallbackReference.getCallback(CFDictionaryEqualCallBack.class, p.getPointer(0x20));
        }

        public CFDictionaryValueCallBacks(NativeLong version, CFDictionaryRetainCallBack retain, CFDictionaryReleaseCallBack release, CFDictionaryCopyDescriptionCallBack copyDescription, CFDictionaryEqualCallBack equal) {
            super();
            this.version = version;
            this.retain = retain;
            this.release = release;
            this.copyDescription = copyDescription;
            this.equal = equal;
        }

        protected ByReference newByReference() {
            ByReference s = new ByReference();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected ByValue newByValue() {
            ByValue s = new ByValue();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected CFDictionaryValueCallBacks newInstance() {
            CFDictionaryValueCallBacks s = new CFDictionaryValueCallBacks();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        public static class ByReference extends CFDictionaryValueCallBacks implements Structure.ByReference {

        }

        public static class ByValue extends CFDictionaryValueCallBacks implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("version", "retain", "release", "copyDescription", "equal");
        }
    }

    CFDictionaryKeyCallBacks kCFTypeDictionaryKeyCallBacks = new CFDictionaryKeyCallBacks(NATIVE_LIBRARY.getGlobalVariableAddress("kCFTypeDictionaryKeyCallBacks"));

    CFDictionaryValueCallBacks kCFTypeDictionaryValueCallBacks = new CFDictionaryValueCallBacks(NATIVE_LIBRARY.getGlobalVariableAddress("kCFTypeDictionaryValueCallBacks"));

    CFDictionary CFDictionaryCreate(CFAllocator allocator, CFString[] keys, CFType[] values, NativeLong numValues, CFDictionaryKeyCallBacks keyCallBacks, CFDictionaryValueCallBacks valueCallBacks);

    CFDictionary CFDictionaryCreateCopy(CFAllocator allocator, CFDictionary theDict);

    CFDictionary CFDictionaryCreateMutable(CFAllocator allocator, NativeLong capacity, CFDictionaryKeyCallBacks keyCallBacks, CFDictionaryValueCallBacks valueCallBacks);

    CFDictionary CFDictionaryCreateMutableCopy(CFAllocator allocator, NativeLong capacity, CFDictionary theDict);

    NativeLong CFDictionaryGetCount(CFDictionary theDict);

    NativeLong CFDictionaryGetCountOfKey(CFDictionary theDict, CFString key);

    NativeLong CFDictionaryGetCountOfValue(CFDictionary theDict, CFType value);

    boolean CFDictionaryContainsKey(CFDictionary theDict, CFString key);

    boolean CFDictionaryContainsValue(CFDictionary theDict, CFType value);

    CFType CFDictionaryGetValue(CFDictionary theDict, CFString key);

    boolean CFDictionaryGetValueIfPresent(CFDictionary theDict, CFString key, CFType[] value);

    void CFDictionaryGetKeysAndValues(CFDictionary theDict, CFString[] keys, CFType[] values);

    void CFDictionaryApplyFunction(CFDictionary theDict, CFDictionaryApplierFunction applier, Structure.ByReference context);

    void CFDictionaryAddValue(CFDictionary theDict, CFString key, CFType value);

    void CFDictionarySetValue(CFDictionary theDict, CFString key, CFType value);

    void CFDictionaryReplaceValue(CFDictionary theDict, CFString key, CFType value);

    void CFDictionaryRemoveValue(CFDictionary theDict, CFString key);

    void CFDictionaryRemoveAllValues(CFDictionary theDict);

    Pointer /*CFMutableDictionaryRef*/ CFDictionaryCreateMutable(CFAllocator allocator, CFIndex capacity, CFDictionaryKeyCallBacks keyCallBacks, CFDictionaryValueCallBacks valueCallBacks);

//#endregion CFDictionary

//#region CFDate

    NativeLong CFAbsoluteTimeGetCurrent();

    CFDate CFDateCreate(CFAllocator allocator, NativeLong absTime);

    NativeLong CFDateGetAbsoluteTime(CFDate theDate);

    NativeLong CFDateGetTimeIntervalSinceDate(CFDate theDate, CFDate otherDate);

//#endregion CFDate

//#region CFPropertyList

    CFPropertyList CFPropertyListCreateWithData(CFAllocator allocator, CFData data, NativeLong options, NativeLong format, CFError error);

//#endregion CFPropertyList

//#region CFError

    CFString CFErrorGetDomain(CFError err);

    CFIndex CFErrorGetCode(CFError err);

    CFString CFErrorCopyDescription(CFError err);

    CFString CFErrorCopyFailureReason(CFError err);

//#endregion CFError

//#region CFLogging

    interface CFLogLevel {

        int kCFLogLevelEmergency = 0;
        int kCFLogLevelAlert = 1;
        int kCFLogLevelCritical = 2;
        int kCFLogLevelError = 3;
        int kCFLogLevelWarning = 4;
        int kCFLogLevelNotice = 5;
        int kCFLogLevelInfo = 6;
        int kCFLogLevelDebug = 7;

    }
    /*
        %@     Object
        %d, %i signed int
        %u     unsigned int
        %f     float/double

        %x, %X hexadecimal int
        %o     octal int
        %zu    size_t
        %p     pointer
        %e     float/double (in scientific notation)
        %g     float/double (as %f or %e, depending on value)
        %s     C string (bytes)
        %S     C string (unichar)
        %.*s   Pascal string (requires two arguments, pass pstr[0] as the first, pstr+1 as the second)
        %c     character
        %C     unichar

        %lld   long long
        %llu   unsigned long long
        %Lf    long double
     */

    void CFLog(int level, CFString msgFormat, Object... varargs);

//#endregion CFLogging

//#region CFRunLoop

    CFString kCFRunLoopDefaultMode = new CFString(NATIVE_LIBRARY
            .getGlobalVariableAddress("kCFRunLoopDefaultMode").getPointer(0));

    int kCFRunLoopRunFinished = 1;
    int kCFRunLoopRunStopped = 2;
    int kCFRunLoopRunTimedOut = 3;
    int kCFRunLoopRunHandledSource = 4;

    class CFRunLoopSourceContext extends Structure {

        public interface CFRunLoopEqualCallBack extends Callback {

            boolean invoke(Pointer info1, Pointer info2);
        }

        public interface CFRunLoopHashCallBack extends Callback {

            long /*CFHashCode*/ invoke(Pointer info);
        }

        public interface CFRunLoopScheduleCallBack extends Callback {

            void invoke(Pointer info, CFRunLoop rl, CFString mode);
        }

        public interface CFRunLoopCancelCallBack extends Callback {

            void invoke(Pointer info, CFRunLoop rl, CFString mode);
        }

        public interface CFRunLoopPerformCallBack extends Callback {

            void invoke(Pointer info);
        }

        public CFIndex version;
        public Pointer info;
        public CFAllocatorRetainCallBack retain;
        public CFAllocatorReleaseCallBack release;
        public CFAllocatorCopyDescriptionCallBack copyDescription;
        public CFRunLoopEqualCallBack equal;
        public CFRunLoopHashCallBack hash;
        public CFRunLoopScheduleCallBack schedule;
        public CFRunLoopCancelCallBack cancel;
        public CFRunLoopPerformCallBack perform;

        public CFRunLoopSourceContext() {
        }

        public CFRunLoopSourceContext(Pointer p) {
            super(p);

            version = CFIndex.of(0);
            info = Pointer.NULL;

            retain = (CFAllocatorRetainCallBack) CallbackReference.getCallback(CFAllocatorRetainCallBack.class, p.getPointer(0x10));
            release = (CFAllocatorReleaseCallBack) CallbackReference.getCallback(CFAllocatorReleaseCallBack.class, p.getPointer(0x18));
            copyDescription = (CFAllocatorCopyDescriptionCallBack) CallbackReference.getCallback(CFAllocatorCopyDescriptionCallBack.class, p.getPointer(0x20));

            equal = (CFRunLoopEqualCallBack) CallbackReference.getCallback(CFRunLoopEqualCallBack.class, p.getPointer(0x28));
            hash = (CFRunLoopHashCallBack) CallbackReference.getCallback(CFRunLoopHashCallBack.class, p.getPointer(0x30));
            schedule = (CFRunLoopScheduleCallBack) CallbackReference.getCallback(CFRunLoopScheduleCallBack.class, p.getPointer(0x38));
            cancel = (CFRunLoopCancelCallBack) CallbackReference.getCallback(CFRunLoopCancelCallBack.class, p.getPointer(0x40));
            perform = (CFRunLoopPerformCallBack) CallbackReference.getCallback(CFRunLoopPerformCallBack.class, p.getPointer(0x48));
        }

        public static class ByReference extends CFRunLoopSourceContext implements Structure.ByReference {

        }

        public static class ByValue extends CFRunLoopSourceContext implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("version", "info",
                    "retain", "release", "copyDescription",
                    "equal", "hash", "schedule", "cancel", "perform");
        }
    }

    void CFRunLoopRun();

    void CFRunLoopStop(CFRunLoop rl);

    CFRunLoop CFRunLoopGetCurrent();

    CFRunLoop CFRunLoopGetMain();

    /** Creates a CFRunLoopSource object. */
    Pointer /*CFRunLoopSourceRef*/ CFRunLoopSourceCreate(CFAllocator allocator, CFIndex order, CFRunLoopSourceContext.ByReference context);

    /** Adds a CFRunLoopSource object to a run loop mode. */
    void CFRunLoopAddSource(CFRunLoop rl, Pointer /*CFRunLoopSourceRef*/ source, CFString/*CFRunLoopMode*/ mode);

    void CFRunLoopSourceSignal(Pointer /*CFRunLoopSourceRef*/ source);

    void CFRunLoopWakeUp(CFRunLoop rl);

    /** Runs the current thread’s CFRunLoop object in a particular mode. */
    int /*CFRunLoopRunResult*/ CFRunLoopRunInMode(CFString/*CFRunLoopMode*/ mode, double/*CFTimeInterval*/ seconds, boolean returnAfterSourceHandled);

//#endregion CFRunLoop

//#region CFAllocator

    interface CFAllocatorRetainCallBack extends Callback {

        Pointer invoke(Pointer info);
    }

    interface CFAllocatorReleaseCallBack extends Callback {

        void invoke(Pointer info);
    }

    interface CFAllocatorCopyDescriptionCallBack extends Callback {

        CFString invoke(Pointer info);
    }

    interface CFAllocatorAllocateCallBack extends Callback {

        Pointer invoke(NativeLong allocSize, NativeLong hint, Pointer info);
    }

    interface CFAllocatorReallocateCallBack extends Callback {

        Pointer invoke(Pointer ptr, NativeLong newsize, NativeLong hint, Pointer info);
    }

    interface CFAllocatorDeallocateCallBack extends Callback {

        void invoke(Pointer ptr, Pointer info);
    }

    interface CFAllocatorPreferredSizeCallBack extends Callback {

        NativeLong invoke(NativeLong size, NativeLong hint, Pointer info);
    }

    class CFAllocatorContext extends Structure {

        public NativeLong version;
        public Pointer info;
        public CFAllocatorRetainCallBack retain;
        public CFAllocatorReleaseCallBack release;
        public CFAllocatorCopyDescriptionCallBack copyDescription;
        public CFAllocatorAllocateCallBack allocate;
        public CFAllocatorReallocateCallBack reallocate;
        public CFAllocatorDeallocateCallBack deallocate;
        public CFAllocatorPreferredSizeCallBack preferredSize;

        public CFAllocatorContext() {
        }

        public CFAllocatorContext(Pointer p) {
            super(p);
        }

        public CFAllocatorContext(NativeLong version, Pointer info, CFAllocatorRetainCallBack retain, CFAllocatorReleaseCallBack release, CFAllocatorCopyDescriptionCallBack copyDescription, CFAllocatorAllocateCallBack allocate, CFAllocatorReallocateCallBack reallocate, CFAllocatorDeallocateCallBack deallocate, CFAllocatorPreferredSizeCallBack preferredSize) {
            this.version = version;
            this.info = info;
            this.retain = retain;
            this.release = release;
            this.copyDescription = copyDescription;
            this.allocate = allocate;
            this.reallocate = reallocate;
            this.deallocate = deallocate;
            this.preferredSize = preferredSize;
        }

        protected ByReference newByReference() {
            ByReference s = new ByReference();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected ByValue newByValue() {
            ByValue s = new ByValue();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        protected CFAllocatorContext newInstance() {
            CFAllocatorContext s = new CFAllocatorContext();
            s.useMemory(getPointer());
            write();
            s.read();
            return s;
        }

        public static class ByReference extends CFAllocatorContext implements Structure.ByReference {

        }

        public static class ByValue extends CFAllocatorContext implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("version", "info",
                    "retain", "release", "copyDescription",
                    "allocate", "reallocate", "deallocate", "preferredSize");
        }
    }

//#endregion CFAllocator

//#region CFUUID

    class CFUUIDBytes extends Structure {

        public byte byte0;
        public byte byte1;
        public byte byte2;
        public byte byte3;
        public byte byte4;
        public byte byte5;
        public byte byte6;
        public byte byte7;
        public byte byte8;
        public byte byte9;
        public byte byte10;
        public byte byte11;
        public byte byte12;
        public byte byte13;
        public byte byte14;
        public byte byte15;

        public static class ByReference extends CFUUIDBytes implements Structure.ByReference {

        }

        public static class ByValue extends CFUUIDBytes implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "byte0", "byte1", "byte2", "byte3", "byte4", "byte5", "byte6", "byte7",
                    "byte8", "byte9", "byte10", "byte11", "byte12", "byte13", "byte14", "byte15"
            );
        }
    }

    /**
     * Returns a CFUUID object from raw UUID bytes.
     * TODO doesn't work well
     */
    Pointer /*CFUUIDRef*/ CFUUIDCreateWithBytes(
            CFAllocator alloc,
            byte byte0, byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7,
            byte byte8, byte byte9, byte byte10, byte byte11, byte byte12, byte byte13, byte byte14, byte byte15);

    /** TODO doesn't work well */
    Pointer /*CFUUIDRef*/ CFUUIDGetConstantUUIDWithBytes(
            CFAllocator alloc,
            byte byte0, byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7,
            byte byte8, byte byte9, byte byte10, byte byte11, byte byte12, byte byte13, byte byte14, byte byte15);

    /**
     * @param uuidStr A string containing a UUID. The standard format for UUIDs represented
     *                in ASCII is a string punctuated by hyphens, for example 68753A44-4D6F-1226-9C60-0050E4C00067.
     */
    Pointer /*CFUUIDRef*/ CFUUIDCreateFromString(CFAllocator alloc, CFString uuidStr);

    /** Returns the value of a UUID object as raw bytes. */
    CFUUIDBytes.ByValue CFUUIDGetUUIDBytes(Pointer/*CFUUIDRef*/ uuid);

//#endregion CFUUID
}
