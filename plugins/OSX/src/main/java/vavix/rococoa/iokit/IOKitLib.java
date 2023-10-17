/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.rococoa.iokit;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import vavix.rococoa.corefoundation.CFAllocator;
import vavix.rococoa.corefoundation.CFDictionary;
import vavix.rococoa.corefoundation.CFIndex;
import vavix.rococoa.corefoundation.CFLib;
import vavix.rococoa.corefoundation.CFRunLoop;
import vavix.rococoa.corefoundation.CFString;
import vavix.rococoa.corefoundation.CFType;


/**
 * IOKitLib.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-22 nsano initial version <br>
 */
public interface IOKitLib extends Library {

    Logger log = Logger.getLogger(IOKitLib.class.getName());

    IOKitLib INSTANCE = Native.load("IOKit", IOKitLib.class);

    //

    Pointer MACH_PORT_NULL = Pointer.NULL;
    Pointer IO_OBJECT_NULL = Pointer.NULL;

    Pointer kIOMainPortDefault = Pointer.NULL;

    int KERN_SUCCESS = 0;
    int KERN_INVALID_ARGUMENT = 4;

    static int err_sub(int x) {
        return (x & 0xfff) << 14;
    }

    int kIOReturnSuccess = KERN_SUCCESS;
    int kIOReturnInvalid = 0xE0000001;
    /** @see "https://opensource.apple.com/source/xnu/xnu-344/iokit/IOKit/IOReturn.h.auto.html" */
    int kIOReturnUnderrun = err_sub(0) | 0x2e7;

    /** @see "https://github.com/opensource-apple/IOUSBFamily/blob/master/IOUSBFamily/Headers/USBSpec.h" */
    String kUSBInterfaceNumber = "bInterfaceNumber";

//#region IOObject

    /** Releases an object handle previously returned by IOKitLib. */
    int IOObjectRelease(Pointer object);

    int /*kern_return_t*/ IOObjectGetClass(Pointer/*io_object_t*/ object, ByteBuffer/*io_name_t*/ className);

//#endregion IOObject

//#region IOIterator

    /**
     * Returns the next object in an iteration.
     * <p>
     * This function returns the next object in an iteration, or zero if no more remain or the iterator is invalid.
     *
     * @param iterator An IOKit iterator handle.
     * @return If the iterator handle is valid, the next element in the iteration is returned,
     * otherwise zero is returned. The element should be released by the caller when it is finished.
     */
    Pointer /*io_object_t*/ IOIteratorNext(Pointer/*io_iterator_t*/ iterator);

//#endregion IOIterator

//#region IOHID

    int kIOHIDOptionsTypeNone = 0x00;
    int kIOHIDOptionsTypeSeizeDevice = 0x01;

    int kIOHIDReportTypeInput = 0;
    int kIOHIDReportTypeOutput = 1;
    int kIOHIDReportTypeFeature = 2;

    String kIOHIDDeviceKey = "IOHIDDevice";
    String kIOServicePlane = "IOService";
    String kIOHIDDeviceUsagePairsKey = "DeviceUsagePairs";
    String kIOHIDVendorIDKey = "VendorID";
    String kIOHIDProductIDKey = "ProductID";
    String kIOHIDMaxInputReportSizeKey = "MaxInputReportSize";
    String kIOHIDSerialNumberKey = "SerialNumber";
    String kIOHIDManufacturerKey = "Manufacturer";
    String kIOHIDProductKey = "Product";
    String kIOHIDVersionNumberKey = "VersionNumber";
    String kIOHIDTransportKey = "Transport";
    String kIOHIDDeviceUsagePageKey = "DeviceUsagePage";
    String kIOHIDDeviceUsageKey = "DeviceUsage";
    String kIOHIDPrimaryUsagePageKey = "PrimaryUsagePage";
    String kIOHIDPrimaryUsageKey = "PrimaryUsage";
    String kIOHIDReportDescriptorKey = "ReportDescriptor";

    /** @see "https://opensource.apple.com/source/IOHIDFamily/IOHIDFamily-1035.1.4/IOHIDFamily/IOHIDKeys.h" */
    String kIOHIDTransportUSBValue = "USB";
    String kIOHIDTransportBluetoothValue = "Bluetooth";
    String kIOHIDTransportI2CValue = "I2C";
    String kIOHIDTransportSPIValue = "SPI";

    /** @see "https://opensource.apple.com/source/IOHIDFamily/IOHIDFamily-86/IOHIDLib/IOHIDLib.h.auto.html" */

    Pointer/* CFUUIDRef */ kIOHIDDeviceUserClientTypeID = CFLib.INSTANCE.CFUUIDCreateFromString(CFAllocator.kCFAllocatorDefault,
            CFString.buildString("FA12FA38-6F1A-11D4-BA0C-0005028F18D5"));

    Pointer/* CFUUIDRef */ kIOHIDDeviceInterfaceID = CFLib.INSTANCE.CFUUIDCreateFromString(CFAllocator.kCFAllocatorDefault,
            CFString.buildString("78BD420C-6F14-11D4-9474-0005028F18D5"));

    /** @see "https://github.com/opensource-apple/IOKitUser/blob/master/IOCFPlugIn.h#L40" */
    Pointer/* CFUUIDRef */ kIOCFPlugInInterfaceID = CFLib.INSTANCE.CFUUIDCreateFromString(CFAllocator.kCFAllocatorDefault,
            CFString.buildString("C244E858-109C-11D4-91D4-0050E4C6426F"));

    /** Creates an IOHIDManager object. */
    Pointer /*IOHIDManagerRef*/ IOHIDManagerCreate(CFAllocator allocator, int /*IOOptionBits*/ options);

    /** Closes the IOHIDManager. */
    int /*IOReturn*/ IOHIDManagerClose(Pointer/*IOHIDManagerRef*/ manager, int /*IOOptionBits*/ options);

    /** Obtains currently enumerated devices. */
    CFType /*CFSetRef*/ IOHIDManagerCopyDevices(Pointer/*IOHIDManagerRef*/ manager);

    /** Sets matching criteria for device enumeration. */
    void IOHIDManagerSetDeviceMatching(Pointer /*IOHIDManagerRef*/ manager, CFDictionary matching);

    /** Schedules HID manager with run loop. */
    void IOHIDManagerScheduleWithRunLoop(Pointer/*IOHIDManagerRef*/ manager, CFRunLoop runLoop, CFString runLoopMode);

    /** Registers a callback to be used when an input report is issued by the device. */
    void IOHIDDeviceRegisterInputReportCallback(Pointer/*IOHIDDeviceRef*/ device, Pointer report, CFIndex reportLength, IOHIDReportCallback callback, Structure.ByReference context);

    /** Registers a callback to be used when a IOHIDDevice is removed. */
    void IOHIDDeviceRegisterRemovalCallback(Pointer/*IOHIDDeviceRef*/ device, IOHIDCallback callback, Structure.ByReference context);

    /** Unschedules HID device with run loop. */
    void IOHIDDeviceUnscheduleFromRunLoop(Pointer/*IOHIDDeviceRef*/ device, CFRunLoop runLoop, CFString runLoopMode);

    /** Schedules HID device with run loop. */
    void IOHIDDeviceScheduleWithRunLoop(Pointer /*IOHIDDeviceRef*/ device, CFRunLoop runLoop, CFString runLoopMode);

    /** Creates an element from an io_service_t. */
    Pointer /*IOHIDDeviceRef*/ IOHIDDeviceCreate(CFAllocator allocator, Pointer/*io_service_t*/ service);

    /** Opens a HID device for communication. */
    int /*IOReturn*/ IOHIDDeviceOpen(Pointer /*IOHIDDeviceRef*/ device, int /*IOOptionBits*/ options);

    /** Closes communication with a HID device. */
    int /*IOReturn*/ IOHIDDeviceClose(Pointer/*IOHIDDeviceRef*/ device, int/*IOOptionBits*/ options);

    /** Sends a report to the device. */
    int /*IOReturn*/ IOHIDDeviceSetReport(Pointer/*IOHIDDeviceRef*/ device, int /*IOHIDReportType*/ reportType, CFIndex reportID, byte[] report, CFIndex reportLength);

    /** Obtains a report from the device. */
    int/*IOReturn*/ IOHIDDeviceGetReport(Pointer/*IOHIDDeviceRef*/ device, int/*IOHIDReportType*/ reportType, CFIndex reportID, byte[] report, CFIndex[] pReportLength);

    /** Returns the io_service_t for an IOHIDDevice, if it has one. */
    Pointer/*io_service_t*/ IOHIDDeviceGetService(Pointer/*IOHIDDeviceRef*/ device);

    /** Obtains a property from an IOHIDDevice. */
    CFType IOHIDDeviceGetProperty(Pointer/*IOHIDDeviceRef*/ device, CFString key);

    interface IOHIDCallback extends Callback {

        void invoke(Pointer context, int/*IOReturn*/ result, Pointer sender);
    }

    interface IOHIDCallbackFunction extends Callback {

        void invoke(Pointer target, int/*IOReturn*/ result, Pointer refcon, Pointer sender);
    }

    interface IOHIDValueCallback extends Callback {

        void invoke(Pointer context, int/*IOReturn*/ result, Pointer sender, Pointer/*IOHIDValueRef*/ value);
    }

    interface IOHIDReportCallback extends Callback {

        void invoke(Pointer context, int/*IOReturn*/ result, Pointer sender, int /*IOHIDReportType*/ type, int reportID, Pointer report, CFIndex reportLength);
    }

    class IOHIDEventStruct extends Structure {

        public int/*IOHIDElementType*/ type;
        public int/*IOHIDElementCookie*/ elementCookie;
        public int value;
        public long/*AbsoluteTime*/ timestamp;
        public int longValueSize;
        public Pointer longValue;

        public IOHIDEventStruct() {
        }

        public IOHIDEventStruct(Pointer p) {
            super(p);
        }

        public static class ByReference extends IOHIDEventStruct implements Structure.ByReference {

        }

        public static class ByValue extends IOHIDEventStruct implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("type", "elementCookie", "value", "timestamp", "longValueSize", "longValue");
        }
    }

    /**
     * The object you use to access a HID queue from user space, returned by version 1.5 of the IOHIDFamily.
     * <p>
     * The functions listed here will work with any version of the IOHIDDeviceQueueInterface.
     * This behavior is useful when you
     * need to keep track of all values of an input element, rather than just the most recent one.
     * <p>
     * Note: Absolute element values (based on a fixed origin) will only be placed on a queue
     * if there is a change in value.
     *
     * @see "https://github.com/opensource-apple/IOKitUser/blob/b80a5cbc0ebfb5c4954ef6d757918db0e4dc4b7f/hid.subproj/IOHIDDevicePlugIn.h#L363"
     */
    class IOHIDDeviceQueueInterface extends Structure {

        public interface GetAsyncEventSourceCallback extends Callback {

            int invoke(Pointer self, PointerByReference/*CFTypeRef */ pSource);
        }

        public interface SetDepthCallback extends Callback {

            int invoke(Pointer self, int depth, int/*IOOptionBits*/ options);
        }

        public interface GetDepthCallback extends Callback {

            int invoke(Pointer self, IntByReference pDepth);
        }

        public interface AddElementCallback extends Callback {

            int invoke(Pointer self, Pointer/*IOHIDElementRef*/ element, int/*IOOptionBits*/ options);
        }

        public interface RemoveElementCallback extends Callback {

            int invoke(Pointer self, Pointer /*IOHIDElementRef*/ element, int /*IOOptionBits*/ options);
        }

        public interface ContainsElementCallback extends Callback {

            int invoke(Pointer self, Pointer /*IOHIDElementRef*/ element, IntByReference /*Boolean*/pValue, int /*IOOptionBits*/ options);
        }

        public interface StartCallback extends Callback {

            int invoke(Pointer self, int /*IOOptionBits*/ options);
        }

        public interface StopCallback extends Callback {

            int invoke(Pointer self, int /*IOOptionBits*/ options);
        }

        public interface SetValueAvailableCallbackCallback extends Callback {

            int invoke(Pointer self, IOHIDCallbackFunction callback, Pointer context);
        }

        public interface CopyNextValueCallback extends Callback {

            int invoke(Pointer self, PointerByReference/*IOHIDValueRef*/ pValue, int timeout, int/*IOOptionBits*/ options);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback queryInterface;
        public AddRefCallback addRef;
        public ReleaseCallback release;

        //

        public GetAsyncEventSourceCallback getAsyncEventSource;
        public SetDepthCallback setDepth;
        public GetDepthCallback getDepth;
        public AddElementCallback addElement;
        public RemoveElementCallback removeElement;
        public ContainsElementCallback containsElement;
        public StartCallback start;
        public StopCallback stop;
        public SetValueAvailableCallbackCallback setValueAvailableCallback;
        public CopyNextValueCallback copyNextValue;

        public IOHIDDeviceQueueInterface() {
        }

        public IOHIDDeviceQueueInterface(Pointer p) {
            super(p);

            _reserved = getPointer().getPointer(0);
            queryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            addRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            getAsyncEventSource = (GetAsyncEventSourceCallback) CallbackReference.getCallback(GetAsyncEventSourceCallback.class, p.getPointer(0x20));
            setDepth = (SetDepthCallback) CallbackReference.getCallback(SetDepthCallback.class, p.getPointer(0x28));
            getDepth = (GetDepthCallback) CallbackReference.getCallback(GetDepthCallback.class, p.getPointer(0x30));
            addElement = (AddElementCallback) CallbackReference.getCallback(AddElementCallback.class, p.getPointer(0x38));
            removeElement = (RemoveElementCallback) CallbackReference.getCallback(RemoveElementCallback.class, p.getPointer(0x40));
            containsElement = (ContainsElementCallback) CallbackReference.getCallback(ContainsElementCallback.class, p.getPointer(0x48));
            start = (StartCallback) CallbackReference.getCallback(StartCallback.class, p.getPointer(0x50));
            stop = (StopCallback) CallbackReference.getCallback(StopCallback.class, p.getPointer(0x58));
            setValueAvailableCallback = (SetValueAvailableCallbackCallback) CallbackReference.getCallback(SetValueAvailableCallbackCallback.class, p.getPointer(0x60));
            copyNextValue = (CopyNextValueCallback) CallbackReference.getCallback(CopyNextValueCallback.class, p.getPointer(0x68));
        }

        public static class ByReference extends IOHIDDeviceQueueInterface implements Structure.ByReference {

        }

        public static class ByValue extends IOHIDDeviceQueueInterface implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("_reserved", "queryInterface", "addRef", "release",
                    "getAsyncEventSource", "setDepth", "getDepth", "addElement", "removeElement", "containsElement",
                    "start", "stop", "setValueAvailableCallback", "copyNextValue"
            );
        }
    }

    /**
     * The object you use to access a HID queue from user space, returned by version 1.5 of the IOHIDFamily.
     * <p>
     * The functions listed here will work with any version of the IOHIDDeviceQueueInterface.
     * This behavior is useful when you
     * need to keep track of all values of an input element, rather than just the most recent one.
     * <p>
     * Note: Absolute element values (based on a fixed origin) will only be placed on a queue
     * if there is a change in value.
     *
     * @see "https://github.com/opensource-apple/IOKitUser/blob/b80a5cbc0ebfb5c4954ef6d757918db0e4dc4b7f/hid.subproj/IOHIDDevicePlugIn.h#L189"
     */
    class IOHIDDeviceDeviceInterface extends Structure {

        public interface OpenCallback extends Callback {

            int invoke(Pointer self, int /*IOOptionBits*/options);
        }

        public interface CloseCallback extends Callback {

            int invoke(Pointer self, int /*IOOptionBits*/options);
        }

        public interface GetPropertyCallback extends Callback {

            int invoke(Pointer self, CFString key, PointerByReference pProperty);
        }

        public interface SetPropertyCallback extends Callback {

            int invoke(Pointer self, CFString key, CFType pProperty);
        }

        public interface GetAsyncEventSourceCallback extends Callback {

            Pointer invoke(Pointer self, PointerByReference/*CFTypeRef*/ pSource);
        }

        public interface CopyMatchingElementsCallback extends Callback {

            int invoke(Pointer self, CFDictionary matchingDict, PointerByReference/*CFArrayRef*/ pElements, int/*IOOptionBits*/ options);
        }

        public interface SetValueCallback extends Callback {

            int invoke(Pointer self, Pointer/*IOHIDElementRef*/ element, Pointer/*IOHIDValueRef*/ value,
                       int timeout, IOHIDValueCallback callback, Pointer context, int/*IOOptionBits*/ options);
        }

        public interface GetValueCallback extends Callback {

            int invoke(Pointer self, Pointer/*IOHIDElementRef*/ element, PointerByReference/*IOHIDValueRef*/ pValue,
                       int timeout, IOHIDValueCallback callback, Pointer context, int/*IOOptionBits*/ options);
        }

        public interface SetInputReportCallbackCallback extends Callback {

            int invoke(Pointer self, byte[] report, CFIndex reportLength,
                       IOHIDReportCallback callback, Pointer context, int /*IOOptionBits*/ options);
        }

        public interface SetReportCallback extends Callback {

            int invoke(Pointer self, int /*IOHIDReportType*/ reportType, int reportID, Pointer report, CFIndex reportLength, int timeout, IOHIDReportCallback callback, Pointer context, int options);
        }

        public interface GetReportCallback extends Callback {

            int invoke(Pointer self, int /*IOHIDReportType*/ reportType, int reportID, Pointer report, PointerByReference/*CFIndex*/ pReportLength, int timeout, IOHIDReportCallback callback, Pointer context, int options);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback queryInterface;
        public AddRefCallback addRef;
        public ReleaseCallback release;

        public OpenCallback open;
        public CloseCallback close;
        public GetPropertyCallback getProperty;
        public SetPropertyCallback setProperty;
        public GetAsyncEventSourceCallback getAsyncEventSource;
        public CopyMatchingElementsCallback copyMatchingElements;
        public SetValueCallback setValue;
        public GetValueCallback getValue;
        public SetInputReportCallbackCallback setInputReportCallback;
        public SetReportCallback setReport;
        public GetReportCallback getReport;

        public IOHIDDeviceDeviceInterface() {
        }

        public IOHIDDeviceDeviceInterface(Pointer p) {
            super(p);

            _reserved = getPointer().getPointer(0);
            queryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            addRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            open = (OpenCallback) CallbackReference.getCallback(OpenCallback.class, p.getPointer(0x20));
            close = (CloseCallback) CallbackReference.getCallback(CloseCallback.class, p.getPointer(0x28));
            getProperty = (GetPropertyCallback) CallbackReference.getCallback(GetPropertyCallback.class, p.getPointer(0x30));
            setProperty = (SetPropertyCallback) CallbackReference.getCallback(SetPropertyCallback.class, p.getPointer(0x38));
            getAsyncEventSource = (GetAsyncEventSourceCallback) CallbackReference.getCallback(GetAsyncEventSourceCallback.class, p.getPointer(0x40));
            copyMatchingElements = (CopyMatchingElementsCallback) CallbackReference.getCallback(CopyMatchingElementsCallback.class, p.getPointer(0x48));
            setValue = (SetValueCallback) CallbackReference.getCallback(SetValueCallback.class, p.getPointer(0x50));
            getValue = (GetValueCallback) CallbackReference.getCallback(GetValueCallback.class, p.getPointer(0x58));
            setInputReportCallback = (SetInputReportCallbackCallback) CallbackReference.getCallback(SetInputReportCallbackCallback.class, p.getPointer(0x60));
            setReport = (SetReportCallback) CallbackReference.getCallback(SetReportCallback.class, p.getPointer(0x68));
            getReport = (GetReportCallback) CallbackReference.getCallback(GetReportCallback.class, p.getPointer(0x70));
        }

        public static class ByReference extends IOHIDDeviceDeviceInterface implements Structure.ByReference {

        }

        public static class ByValue extends IOHIDDeviceDeviceInterface implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "_reserved", "queryInterface", "addRef", "release",
                    "open", "close", "getProperty", "setProperty", "getAsyncEventSource", "copyMatchingElements",
                    "setValue", "getValue", "setInputReportCallback", "setReport", "getReport"
            );
        }
    }

    /**
     * @see "https://developer.apple.com/documentation/iokit/iohiddeviceinterface/"
     * @see "https://opensource.apple.com/source/IOHIDFamily/IOHIDFamily-870.21.4/IOHIDLib/IOHIDLibObsolete.h.auto.html"
     */
    class IOHIDDeviceInterface extends Structure {

        public interface ReportCallbackFunction extends Callback {

            void invoke(Pointer target, int /*IOReturn*/ result, Pointer refcon, Pointer sender, int bufferSize);
        }

        public interface ElementCallbackFunction extends Callback {

            void invoke(Pointer target, int /*IOReturn*/ result, Pointer refcon, Pointer sender, int/*IOHIDElementCookie*/ elementCookie);
        }

        //

        public interface CreateAsyncEventSourceCallback extends Callback {

            int invoke(Pointer self, Pointer /* CFRunLoopSourceRef */source);
        }

        public interface GetAsyncEventSourceCallback extends Callback {

            Pointer /* CFRunLoopSourceRef*/ invoke(Pointer self);
        }

        public interface CreateAsyncPortCallback extends Callback {

            int invoke(Pointer self, PointerByReference /* mach_port_t */ port);
        }

        public interface GetAsyncPortCallback extends Callback {

            Pointer /* mach_port_t */ invoke(Pointer self);
        }

        public interface OpenCallback extends Callback {

            int invoke(Pointer self, int /*IOOptionBits*/flags);
        }

        public interface CloseCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface SetRemovalCallbackCallback extends Callback {

            int invoke(Pointer self, IOHIDCallbackFunction removalCallback, Pointer removalTarget, Pointer removalRefcon);
        }

        public interface GetElementValueCallback extends Callback {

            int invoke(Pointer self, Pointer /*IOHIDElementCookie*/ elementCookie, IOHIDEventStruct.ByReference valueEvent);
        }

        public interface SetElementValueCallback extends Callback {

            int invoke(Pointer self, int/*IOHIDElementCookie*/ elementCookie, IOHIDEventStruct.ByReference valueEvent, int timeoutMS, ElementCallbackFunction callback, Pointer callbackTarget, Pointer callbackRefcon);
        }

        public interface QueryElementValueCallback extends Callback {

            int invoke(Pointer self, int /*IOHIDElementCookie*/ elementCookie, IOHIDEventStruct.ByReference valueEvent, int timeoutMS, ElementCallbackFunction callback, Pointer callbackTarget, Pointer callbackRefcon);
        }

        public interface StartAllQueuesCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface StopAllQueuesCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface AllocQueueCallback extends Callback {

            Pointer invoke(Pointer self);
        }

        public interface AllocOutputTransactionCallback extends Callback {

            PointerByReference/*IOHIDOutputTransactionInterface.ByReference*/ invoke(Pointer self);
        }

        // since 1.2.1

        public interface SetReportCallback extends Callback {

            int invoke(Pointer self, int /*IOHIDReportType*/ reportType, int reportID, Pointer reportBuffer, int reportBufferSize, int timeoutMS, ReportCallbackFunction callback, Pointer callbackTarget, Pointer callbackRefcon);
        }

        public interface GetReportCallback extends Callback {

            int invoke(Pointer self, int /*IOHIDReportType*/ reportType, int reportID, Pointer reportBuffer, int reportBufferSize, int timeoutMS, ReportCallbackFunction callback, Pointer callbackTarget, Pointer callbackRefcon);
        }

        // since 1.2.2

        public interface CopyMatchingElementsCallback extends Callback {

            int invoke(Pointer self, CFDictionary matchingDict, PointerByReference/*CFArrayRef*/ pElements, int/*IOOptionBits*/ options);
        }

        public interface SetInputReportCallbackCallback extends Callback {

            int invoke(Pointer self, Pointer report, CFIndex reportLength,
                       IOHIDReportCallback callback, Pointer context, int /*IOOptionBits*/ options);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback queryInterface;
        public AddRefCallback addRef;
        public ReleaseCallback release;

        //

        public CreateAsyncEventSourceCallback createAsyncEventSource;
        public GetAsyncEventSourceCallback getAsyncEventSource;
        public CreateAsyncPortCallback createAsyncPort;
        public GetAsyncPortCallback getAsyncPort;
        public OpenCallback open;
        public CloseCallback close;
        public SetRemovalCallbackCallback setRemovalCallback;
        public GetElementValueCallback getElementValue;
        public SetElementValueCallback setElementValue;
        public QueryElementValueCallback queryElementValue;
        public StartAllQueuesCallback startAllQueues;
        public StopAllQueuesCallback stopAllQueues;
        public AllocQueueCallback allocQueue;
        public AllocOutputTransactionCallback allocOutputTransaction;

        public SetReportCallback setReport;
        public GetReportCallback getReport;

        public CopyMatchingElementsCallback copyMatchingElements;
        public SetInputReportCallbackCallback setInputReportCallback;

        public IOHIDDeviceInterface() {
        }

        public IOHIDDeviceInterface(Pointer p) {
            super(p);
log.finer("IOHIDDeviceInterface: " + p.dump(0, this.size()));
            _reserved = getPointer().getPointer(0);
            queryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            addRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            createAsyncEventSource = (CreateAsyncEventSourceCallback) CallbackReference.getCallback(CreateAsyncEventSourceCallback.class, p.getPointer(0x20));
            getAsyncEventSource = (GetAsyncEventSourceCallback) CallbackReference.getCallback(GetAsyncEventSourceCallback.class, p.getPointer(0x28));
            createAsyncPort = (CreateAsyncPortCallback) CallbackReference.getCallback(CreateAsyncPortCallback.class, p.getPointer(0x30));
            getAsyncPort = (GetAsyncPortCallback) CallbackReference.getCallback(GetAsyncPortCallback.class, p.getPointer(0x38));
            open = (OpenCallback) CallbackReference.getCallback(OpenCallback.class, p.getPointer(0x40));
            close = (CloseCallback) CallbackReference.getCallback(CloseCallback.class, p.getPointer(0x48));
            setRemovalCallback = (SetRemovalCallbackCallback) CallbackReference.getCallback(SetRemovalCallbackCallback.class, p.getPointer(0x50));
            getElementValue = (GetElementValueCallback) CallbackReference.getCallback(GetElementValueCallback.class, p.getPointer(0x58));
            setElementValue = (SetElementValueCallback) CallbackReference.getCallback(SetElementValueCallback.class, p.getPointer(0x60));
            queryElementValue = (QueryElementValueCallback) CallbackReference.getCallback(QueryElementValueCallback.class, p.getPointer(0x68));
            startAllQueues = (StartAllQueuesCallback) CallbackReference.getCallback(StartAllQueuesCallback.class, p.getPointer(0x70));
            stopAllQueues = (StopAllQueuesCallback) CallbackReference.getCallback(StopAllQueuesCallback.class, p.getPointer(0x78));
            allocQueue = (AllocQueueCallback) CallbackReference.getCallback(AllocQueueCallback.class, p.getPointer(0x80));
            allocOutputTransaction = (AllocOutputTransactionCallback) CallbackReference.getCallback(AllocOutputTransactionCallback.class, p.getPointer(0x88));

            setReport = (SetReportCallback) CallbackReference.getCallback(SetReportCallback.class, p.getPointer(0x90));
            getReport = (GetReportCallback) CallbackReference.getCallback(GetReportCallback.class, p.getPointer(0x98));

            copyMatchingElements = (CopyMatchingElementsCallback) CallbackReference.getCallback(CopyMatchingElementsCallback.class, p.getPointer(0xa0));
            setInputReportCallback = (SetInputReportCallbackCallback) CallbackReference.getCallback(SetInputReportCallbackCallback.class, p.getPointer(0xa8));
log.finer("IOHIDDeviceInterface:\n" + this);
        }

        public static class ByReference extends IOHIDDeviceInterface implements Structure.ByReference {

        }

        public static class ByValue extends IOHIDDeviceInterface implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "_reserved", "queryInterface", "addRef", "release",
                    "createAsyncEventSource", "getAsyncEventSource", "createAsyncPort", "getAsyncPort",
                    "open", "close", "setRemovalCallback", "getElementValue", "setElementValue", "queryElementValue",
                    "startAllQueues", "stopAllQueues", "allocQueue", "allocOutputTransaction",
                    "setReport", "getReport",
                    "copyMatchingElements", "setInputReportCallback"
            );
        }
    }

    /**
     * @see "https://opensource.apple.com/source/IOHIDFamily/IOHIDFamily-870.21.4/IOHIDLib/IOHIDLibObsolete.h.auto.html"
     */
    class IOHIDQueueInterface extends Structure {

        public interface CreateAsyncEventSourceCallback extends Callback {

            int invoke(Pointer self, Pointer/*CFRunLoopSourceRef*/ source);
        }

        public interface GetAsyncEventSourceCallback extends Callback {

            int invoke(Pointer self, PointerByReference/*CFTypeRef */ pSource);
        }

        public interface CreateAsyncPortCallback extends Callback {

            int invoke(Pointer self, Pointer/*mach_port_t*/port);
        }

        public interface GetAsyncPortCallback extends Callback {

            Pointer/*mach_port_t*/ invoke(Pointer self);
        }

        public interface CreateCallback extends Callback {

            int invoke(Pointer self, int flags, int depth);
        }

        public interface DisposeCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface AddElementCallback extends Callback {

            int invoke(Pointer self, Pointer/*IOHIDElementCookie*/ elementCookie, int/*IOOptionBits*/ flags);
        }

        public interface RemoveElementCallback extends Callback {

            int invoke(Pointer self, Pointer /*IOHIDElementCookie*/ elementCookie, int /*IOOptionBits*/ flags);
        }

        public interface HasElementCallback extends Callback {

            boolean invoke(Pointer self, int/*IOHIDElementCookie*/ elementCookie);
        }

        public interface StartCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface StopCallback extends Callback {

            int invoke(Pointer self);
        }

        public interface GetNextEventCallback extends Callback {

            int invoke(Pointer self, IOHIDEventStruct.ByReference event, long/*AbsoluteTime*/ maxTime, int timeoutMS);
        }

        public interface SetEventCalloutCallback extends Callback {

            int invoke(Pointer self, IOHIDCallbackFunction callback, Pointer callbackTarget, Pointer callbackRefcon);
        }

        public interface GetEventCalloutCallback extends Callback {

            int invoke(Pointer self, Pointer/*IOHIDCallbackFunction*/ outCallback, PointerByReference outCallbackTarget, PointerByReference outCallbackRefcon);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback queryInterface;
        public AddRefCallback addRef;
        public ReleaseCallback release;

        public CreateAsyncEventSourceCallback createAsyncEventSource;
        public GetAsyncEventSourceCallback getAsyncEventSource;
        public CreateAsyncPortCallback createAsyncPort;
        public GetAsyncPortCallback getAsyncPort;
        public CreateCallback create;
        public DisposeCallback dispose;
        public AddElementCallback addElement;
        public RemoveElementCallback removeElement;
        public HasElementCallback hasElement;
        public StartCallback start;
        public StopCallback stop;
        public GetNextEventCallback getNextEvent;
        public SetEventCalloutCallback setEventCallout;
        public GetEventCalloutCallback getEventCallout;

        public IOHIDQueueInterface() {
        }

        public IOHIDQueueInterface(Pointer p) {
            super(p);

            _reserved = getPointer().getPointer(0);
            queryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            addRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            createAsyncEventSource = (CreateAsyncEventSourceCallback) CallbackReference.getCallback(CreateAsyncEventSourceCallback.class, p.getPointer(0x20));
            getAsyncEventSource = (GetAsyncEventSourceCallback) CallbackReference.getCallback(GetAsyncEventSourceCallback.class, p.getPointer(0x28));
            createAsyncPort = (CreateAsyncPortCallback) CallbackReference.getCallback(CreateAsyncPortCallback.class, p.getPointer(0x30));
            getAsyncPort = (GetAsyncPortCallback) CallbackReference.getCallback(GetAsyncPortCallback.class, p.getPointer(0x38));
            create = (CreateCallback) CallbackReference.getCallback(CreateCallback.class, p.getPointer(0x40));
            dispose = (DisposeCallback) CallbackReference.getCallback(DisposeCallback.class, p.getPointer(0x48));
            addElement = (AddElementCallback) CallbackReference.getCallback(AddElementCallback.class, p.getPointer(0x50));
            removeElement = (RemoveElementCallback) CallbackReference.getCallback(RemoveElementCallback.class, p.getPointer(0x58));
            hasElement = (HasElementCallback) CallbackReference.getCallback(HasElementCallback.class, p.getPointer(0x60));
            start = (StartCallback) CallbackReference.getCallback(StartCallback.class, p.getPointer(0x68));
            stop = (StopCallback) CallbackReference.getCallback(StopCallback.class, p.getPointer(0x70));
            getNextEvent = (GetNextEventCallback) CallbackReference.getCallback(GetNextEventCallback.class, p.getPointer(0x78));
            setEventCallout = (SetEventCalloutCallback) CallbackReference.getCallback(SetEventCalloutCallback.class, p.getPointer(0x80));
            getEventCallout = (GetEventCalloutCallback) CallbackReference.getCallback(GetEventCalloutCallback.class, p.getPointer(0x88));
log.finer("IOHIDQueueInterface:\n" + this);
        }

        public static class ByReference extends IOHIDQueueInterface implements Structure.ByReference {

        }

        public static class ByValue extends IOHIDQueueInterface implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "_reserved", "queryInterface", "addRef", "release",
                    "createAsyncEventSource", "getAsyncEventSource", "createAsyncPort", "getAsyncPort",
                    "create", "dispose", "addElement", "removeElement", "hasElement",
                    "start", "stop", "getNextEvent", "getEventCallout", "setEventCallout"
            );
        }
    }

//#endregion IOHID

//#region IOService

    Pointer /*CFMutableDictionaryRef*/ IOServiceMatching(String name);

    int /*kern_return_t*/ IOServiceGetMatchingServices(
            Pointer/*mach_port_t*/ mainPort,
            Pointer/*CFDictionaryRef*/ matching,
            PointerByReference/*io_iterator_t*/ existing);

    /** Look up a registered IOService object that matches a matching dictionary. */
    Pointer /*io_service_t*/ IOServiceGetMatchingService(Pointer/*mach_port_t*/ mainPort, CFDictionary matching);

//#endregion IOService

//#region IORegistryEntry

    /** Create a CF dictionary representation of a registry entry's property table. */
    int /*kern_return_t*/ IORegistryEntryCreateCFProperties(
            Pointer/*io_registry_entry_t*/ entry,
            PointerByReference/*CFMutableDictionaryRef*/ properties,
            CFAllocator/*CFAllocatorRef*/ allocator,
            int/*IOOptionBits*/ options);

    int /*kern_return_t*/ IORegistryEntryGetPath(Pointer/*io_registry_entry_t*/ entry, String /*io_name_t*/ plane, ByteBuffer /*io_string_t*/ path);

    /** Returns an ID for the registry entry that is global to all tasks. */
    int /*kern_return_t*/ IORegistryEntryGetRegistryEntryID(Pointer/*io_registry_entry_t*/ entry, LongByReference entryID);

    /** Create a CF representation of a registry entry's property. */
    CFType IORegistryEntryCreateCFProperty(Pointer/*io_registry_entry_t*/ entry, CFString key, CFAllocator allocator, int /*IOOptionBits*/ options);

    /** Create a matching dictionary that specifies an IOService match based on a registry entry ID. */
    CFType /*CFMutableDictionaryRef*/ IORegistryEntryIDMatching(long entryID);

    /** Looks up a registry entry by path. */
    Pointer/*io_registry_entry_t*/ IORegistryEntryFromPath(Pointer/*mach_port_t*/ mainPort, ByteBuffer/*io_string_t*/ path);

    /** Returns the first parent of a registry entry in a plane. */
    int/*kern_return_t*/ IORegistryEntryGetParentEntry(Pointer/*io_registry_entry_t*/ entry, ByteBuffer/*io_name_t*/ plane, PointerByReference/*io_registry_entry_t*/ parent);

//#endregion IORegistryEntry

//#region PlugIn

    int /*kern_return_t*/ IOCreatePlugInInterfaceForService(
            Pointer/*io_service_t*/ service,
            Pointer/*CFUUIDRef*/ pluginType,
            Pointer/*CFUUIDRef*/ interfaceType,
            PointerByReference/*IOCFPlugInInterface*/theInterface,
            IntByReference theScore);

    // https://opensource.apple.com/source/CF/CF-635/CFPlugInCOM.h.auto.html

    interface QueryInterfaceCallback extends Callback {

        int /*HRESULT*/ invoke(Pointer thisPointer, CFLib.CFUUIDBytes.ByValue iid, PointerByReference ppv);
    }

    interface AddRefCallback extends Callback {

        NativeLong /*ULONG*/ invoke(Pointer thisPointer);
    }

    interface ReleaseCallback extends Callback {

        NativeLong /*ULONG*/ invoke(Pointer thisPointer);
    }

    /**
     * Represents and provides management functions for a UPS device.
     *
     * @see "https://developer.apple.com/documentation/iokit/ioupsplugininterface/"
     * @see "https://github.com/opensource-apple/IOKitUser/blob/master/IOCFPlugIn.h#L45"
     */
    class IOCFPlugInInterface extends Structure {

        public interface ProbeCallback extends Callback {

            void invoke(Pointer thisPointer, Pointer /*CFDictionaryRef*/ propertyTable, Pointer/*io_service_t*/ service, IntByReference order);
        }

        public interface StartCallback extends Callback {

            void invoke(Pointer thisPointer, Pointer /*CFDictionaryRef*/ propertyTable, Pointer/*io_service_t*/ service);
        }

        public interface StopCallback extends Callback {

            void invoke(Pointer thisPointer);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback queryInterface;
        public AddRefCallback addRef;
        public ReleaseCallback release;

        public short version;
        public short revision;

        public ProbeCallback probe;
        public StartCallback start;
        public StopCallback stop;

        public IOCFPlugInInterface() {
        }

        public IOCFPlugInInterface(Pointer p) {
            super(p);

            _reserved = getPointer().getPointer(0);
            queryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            addRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            version = p.getShort(0x20);
            revision = p.getShort(0x22);

            probe = (ProbeCallback) CallbackReference.getCallback(ProbeCallback.class, p.getPointer(0x28));
            start = (StartCallback) CallbackReference.getCallback(StartCallback.class, p.getPointer(0x30));
            stop = (StopCallback) CallbackReference.getCallback(StopCallback.class, p.getPointer(0x38));
        }

        public static class ByReference extends IOCFPlugInInterface implements Structure.ByReference {

        }

        public static class ByValue extends IOCFPlugInInterface implements Structure.ByValue {

        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("_reserved", "queryInterface", "addRef", "release",
                    "version", "revision", "probe", "start", "stop");
        }
    }

//#endregion PlugIn
}
