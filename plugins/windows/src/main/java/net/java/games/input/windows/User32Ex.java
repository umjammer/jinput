/*
 * https://github.com/ykon/w10wheel/blob/master/src/main/scala/hooktest/win32ex/Win32Ex.scala
 */

package net.java.games.input.windows;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.SetupApi.SP_DEVINFO_DATA;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.MSLLHOOKSTRUCT;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;


/**
 * @see "http://stackoverflow.com/questions/7004810/from-java-capture-mouse-click-and-use-as-hotkey"
 * @see "https://msdn.microsoft.com/library/windows/desktop/ms644986.aspx"
 */
public interface User32Ex {

    int DIPH_DEVICE = 32;

    int CS_VREDRAW = 0x0001;
    int CS_HREDRAW = 0x0002;

    int COLOR_WINDOW = 5;

    /** @see "https://msdn.microsoft.com/library/windows/desktop/ms645565.aspx" */
    class RAWINPUTDEVICE extends Structure {
        public short usUsagePage;
        public short usUsage;
        public int dwFlags;
        public HWND hwndTarget;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("usUsagePage", "usUsage", "dwFlags", "hwndTarget");
        }
    }

    class RAWINPUTHEADER extends Structure {
        public int dwType;
        public int dwSize;
        public HANDLE hDevice;
        public WPARAM wParam;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwType", "dwSize", "hDevice", "wParam");
        }
    }

    class RAWMOUSE extends Structure {
        public short usFlags;
        public short usButtonFlags;
        public short usButtonData;
        public int ulRawButtons;
        public int lLastX;
        public int lLastY;
        public int ulExtraInformation;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("usFlags", "usButtonFlags", "usButtonData",
                    "ulRawButtons", "lLastX", "lLastY", "ulExtraInformation");
        }
    }

    class RAWKEYBOARD extends Structure {
        public short MakeCode;
        public short Flags;
        public short Reserved;
        public short VKey;
        public int Message;
        public long ExtraInformation;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("usMakeCode", "usFlags", "usReserved",
                    "usVKey", "uiMessage", "ulExtraInformation");
        }
    }

    class RAWHID extends Structure {
        public short dwSizeHid;
        public short dwCount;
        public byte[] bRawData = new byte[1];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("usMakeCode", "usFlags", "usReserved",
                    "usVKey", "uiMessage", "ulExtraInformation");
        }
    }

    class RAWINPUT extends Structure {

        public static class Data extends Union {

            public RAWMOUSE mouse;
            public RAWKEYBOARD keyboard;
            public RAWHID hid;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("mouse", "keyboard", "hid");
            }
        }

        public RAWINPUTHEADER header;
        public Data data;

        public RAWINPUT() {
            super();
        }

        public RAWINPUT(Pointer ptr) {
            super(ptr);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("header", "mouse");
        }
    }

    class RID_DEVICE_INFO_KEYBOARD extends Structure {
        public int dwType;
        public int dwSubType;
        public int dwKeyboardMode;
        public int dwNumberOfFunctionKeys;
        public int dwNumberOfIndicators;
        public int dwNumberOfKeysTotal;
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwType", "dwSubType", "dwKeyboardMode", "dwNumberOfFunctionKeys", "dwNumberOfIndicators", "dwNumberOfKeysTotal");
        }
    }

    class RID_DEVICE_INFO_MOUSE extends Structure {
        public int dwId;
        public int dwNumberOfButtons;
        public int dwSampleRate;
        public boolean fHasHorizontalWheel;
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwId", "dwNumberOfButtons", "dwSampleRate", "fHasHorizontalWheel");
        }
    }

    class RID_DEVICE_INFO_HID extends Structure {
        public int  dwVendorId;
        public int  dwProductId;
        public int  dwVersionNumber;
        public short usUsagePage;
        public short usUsage;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwVendorId", "dwProductId", "dwVersionNumber", "usUsagePage", "usUsage");
        }
    }

    class RID_DEVICE_INFO extends Structure {
        public int cbSize;
        public int dwType;
        public static class DUMMYUNIONNAME extends Union {
            RID_DEVICE_INFO_MOUSE    mouse;
            RID_DEVICE_INFO_KEYBOARD keyboard;
            RID_DEVICE_INFO_HID      hid;
            @Override protected List<String> getFieldOrder() {
                return Arrays.asList("mouse", "keyboard", "hid");
            }
        }
        public DUMMYUNIONNAME u;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("cbSize", "dwType", "u");
        }
    }

    class DIOBJECTDATAFORMAT extends Structure  {
        public GUID pguid;
        public int dwOfs;
        public int dwType;
        public int dwFlags;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("pguid", "dwOfs", "dwType", "dwFlags");
        }
    }

    class DIDATAFORMAT extends Structure {
        public int dwSize;
        public int dwObjSize;
        public int dwFlags;
        public int dwDataSize;
        public int dwNumObjs;
        public DIOBJECTDATAFORMAT[] rgodf;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwObjSize", "dwFlags", "dwDataSize", "dwNumObjs", "rgodf");
        }
    }

    class DIPROPHEADER extends Structure {
        public int dwSize;
        public int dwHeaderSize;
        public int dwObj;
        public int dwHow;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwHeaderSize", "dwObj", "dwHow");
        }
    }

    int DIPH_BYID = 2;

    class DIPROPDWORD extends Structure {
        public DIPROPHEADER diph;
        public int dwData;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("diph", "dwData");
        }
    }

    int MAX_PATH = 260;

    class DIDEVICEINSTANCE extends Structure {
        public int dwSize;
        public GUID guidInstance;
        public GUID guidProduct;
        public int dwDevType;
        public byte[] tszInstanceName = new byte[MAX_PATH];
        public byte[] tszProductName = new byte[MAX_PATH];
        public GUID guidFFDriver;
        public short wUsagePage;
        public short wUsage;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "guidInstance", "guidProduct", "dwDevType", "tszInstanceName",
                    "tszProductName", "guidFFDriver", "wUsagePage", "wUsage");
        }
    }

    class DIENVELOPE extends Structure {
        public int dwSize;
        public int dwAttackLevel;
        public int dwAttackTime;
        public int dwFadeLevel;
        public int dwFadeTime;
        public static class ByReference extends DIENVELOPE implements Structure.ByReference {
        }
        public static class ByValue extends DIENVELOPE implements Structure.ByValue {
        }
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwAttackLevel", "dwAttackTime", "dwFadeLevel", "dwFadeTime");
        }
    }

    class DIEFFECT extends Structure {
        public int dwSize;
        public int dwFlags;
        public int dwDuration;
        public int dwSamplePeriod;
        public int dwGain;
        public int dwTriggerButton;
        public int dwTriggerRepeatInterval;
        public int cAxes;
        public int[] rgdwAxes;
        public long[] rglDirection;
        DIENVELOPE.ByReference lpEnvelope;
        public int cbTypeSpecificParams;
        public DIPERIODIC.ByReference lpvTypeSpecificParams;
        public int dwStartDelay;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwFlags", "dwDuration", "dwSamplePeriod", "dwGain",
                    "dwTriggerButton", "dwTriggerRepeatInterval", "cAxes", "rgdwAxes", "rglDirection",
                    "lpEnvelope", "cbTypeSpecificParams", "lpvTypeSpecificParams", "dwStartDelay");
        }
    }

    class DIEFFESCAPE extends Structure {
        public int dwSize;
        public int dwCommand;
        public Pointer lpvInBuffer;
        public int cbInBuffer;
        public Pointer lpvOutBuffer;
        public int cbOutBuffer;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwCommand", "lpvInBuffer", "cbInBuffer", "lpvOutBuffer", "cbOutBuffer");
        }
    }

    class DIPERIODIC extends Structure {
        int dwMagnitude;
        long lOffset;
        int dwPhase;
        int dwPeriod;
        public static class ByReference extends DIPERIODIC implements Structure.ByReference {
        }
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("dwMagnitude", "lOffset", "dwPhase", "dwPeriod");
        }
    }

    class DIDEVICEOBJECTINSTANCE extends Structure {

        public int dwSize;
        public GUID guidType;
        public int dwOfs;
        public int dwType;
        public int dwFlags;
        public byte[] tszName = new byte[MAX_PATH];
        public int dwFFMaxForce;
        public int dwFFForceResolution;
        public short wCollectionNumber;
        public short wDesignatorIndex;
        public short wUsagePage;
        public short wUsage;
        public int dwDimension;
        public short wExponent;
        public short wReportId;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "guidType", "dwOfs", "dwType", "dwFlags", "tszName",
                    "dwFFMaxForce", "dwFFForceResolution", "wCollectionNumber", "wDesignatorIndex", "wUsagePage",
                    "wUsage", "dwDimension", "wExponent", "wReportId");
        }
    }

    class DIPROPRANGE extends Structure {
        DIPROPHEADER diph;
        long lMin;
        long lMax;
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("diph", "lMin", "lMax");
        }
    }

    interface QueryInterfaceCallback extends Callback {
        int /*HRESULT*/ apply(Pointer thisPointer, GUID.ByValue iid, PointerByReference ppv);
    }

    interface AddRefCallback extends Callback {
        NativeLong /*ULONG*/ apply(Pointer thisPointer);
    }

    interface ReleaseCallback extends Callback {
        NativeLong /*ULONG*/ apply(Pointer thisPointer);
    }

    class IDirectInputEffect extends Structure {
        interface DownloadCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface EscapeCallback extends Callback {
            int /* HRESULT */ apply(DIEFFESCAPE pesc);
        }
        interface GetEffectGuidCallback extends Callback {
            int /* HRESULT */ apply(GUID pguid);
        }
        interface GetEffectStatusCallback extends Callback {
            int /* HRESULT */ apply(int[] pdwFlags);
        }
        interface GetParametersCallback extends Callback {
            int /* HRESULT */ apply(DIEFFECT peff, int dwFlags);
        }
        interface InitializeCallback extends Callback {
            int /* HRESULT */ apply(HINSTANCE hinst, int dwVersion, GUID.ByValue rguid);
        }
        interface SetParametersCallback extends Callback {
            int /* HRESULT */ apply(DIEFFECT peff, int dwFlags);
        }
        interface StartCallback extends Callback {
            int /* HRESULT */ apply(int dwIterations, int dwFlags);
        }
        interface StopCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface UnloadCallback extends Callback {
            int /* HRESULT */ apply();
        }
        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback QueryInterface;
        public AddRefCallback AddRef;
        public ReleaseCallback Release;

        /** Places the effect on the device. */
        public DownloadCallback Download;
        /** Sends a hardware-specific command to the driver. */
        public EscapeCallback Escape;
        /**
         * Retrieves the globally unique identifier (GUID) for the effect represented by
         * the IDirectInputEffect Interface object.
         */
        public GetEffectGuidCallback GetEffectGuid;
        /** Retrieves the status of an effect. */
        public GetEffectStatusCallback GetEffectStatus;
        /** Retrieves information about an effect. */
        public GetParametersCallback GetParameters;
        /** Initializes a DirectInputEffect object. */
        public InitializeCallback Initialize;
        /** Sets the characteristics of an effect. */
        public SetParametersCallback SetParameters;
        /** Begins playing an effect. */
        public StartCallback Start;
        /** Stops playing an effect. */
        public StopCallback Stop;
        /** Removes the effect from the device. */
        public UnloadCallback Unload;
        public IDirectInputEffect(Pointer p) {
            super(p);
            _reserved = getPointer().getPointer(0);
            QueryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            AddRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            Release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            Download = (DownloadCallback) CallbackReference.getCallback(DownloadCallback.class, p.getPointer(0x20));
            Escape = (EscapeCallback) CallbackReference.getCallback(EscapeCallback.class, p.getPointer(0x28));
            GetEffectGuid = (GetEffectGuidCallback) CallbackReference.getCallback(GetEffectGuidCallback.class, p.getPointer(0x30));
            GetEffectStatus = (GetEffectStatusCallback) CallbackReference.getCallback(GetEffectStatusCallback.class, p.getPointer(0x38));
            GetParameters = (GetParametersCallback) CallbackReference.getCallback(GetParametersCallback.class, p.getPointer(0x40));
            Initialize = (InitializeCallback) CallbackReference.getCallback(InitializeCallback.class, p.getPointer(0x48));
            SetParameters = (SetParametersCallback) CallbackReference.getCallback(SetParametersCallback.class, p.getPointer(0x50));
            Start = (StartCallback) CallbackReference.getCallback(StartCallback.class, p.getPointer(0x58));
            Stop = (StopCallback) CallbackReference.getCallback(StopCallback.class, p.getPointer(0x60));
            Unload = (UnloadCallback) CallbackReference.getCallback(UnloadCallback.class, p.getPointer(0x68));
        }
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("Download", "Escape", "GetEffectGuid", "GetEffectStatus", "GetParameters", "Initialize",
                    "SetParameters", "Start", "Stop", "Unload");
        }
    }

    class IDirectInput8 extends Structure {

        interface DIEnumDevicesCallback extends Callback {
            boolean apply(DIDEVICEINSTANCE lpddi, LPVOID pvRef);
        }

        //

        interface ConfigureDevicesCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface CreateDeviceCallback extends Callback {
            int /* HRESULT */ apply(GUID.ByValue rguid,
                                    PointerByReference /* LPDIRECTINPUTDEVICE */ lplpDirectInputDevice,
                                    Pointer /* LPUNKNOWN */ pUnkOuter);
        }
        interface EnumDevicesCallback extends Callback {
            int /* HRESULT */ apply(int dwDevType,
                                    DIEnumDevicesCallback lpCallback,
                                    Pointer pvRef,
                                    int dwFlags);
        }
        interface EnumDevicesBySemanticsCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface FindDeviceCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface GetDeviceStatusCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface InitializeCallback extends Callback {
            int /* HRESULT */ apply();
        }
        interface RunControlPanelCallback extends Callback {
            int /* HRESULT */ apply();
        }
        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback QueryInterface;
        public AddRefCallback AddRef;
        public ReleaseCallback Release;

        /** Displays property pages for connected input devices and enables the user to map actions to device controls. */
        ConfigureDevicesCallback ConfigureDevices;
        /** Creates and initializes an instance of a device based on a given globally unique identifier (GUID), and obtains an IDirectInputDevice8 Interface interface. */
        CreateDeviceCallback CreateDevice;
        /** Enumerates available devices. */
        EnumDevicesCallback EnumDevices;
        /** Enumerates devices that most closely match the application-specified action map. */
        EnumDevicesBySemanticsCallback EnumDevicesBySemantics;
        /** Retrieves the instance globally unique identifier (GUID) of a device that has been newly attached to the system. It is called in response to a Microsoft Win32 device management notification. */
        FindDeviceCallback FindDevice;
        /** Retrieves the status of a specified device. */
        GetDeviceStatusCallback GetDeviceStatus;
        /** Initializes a DirectInput object. Applications normally do not need to call this method. The DirectInput8Create function automatically initializes the DirectInput object after creating it. */
        InitializeCallback Initialize;
        /** Runs Control Panel to enable the user to install a new input device or modify configurations. */
        RunControlPanelCallback RunControlPanel;
        public IDirectInput8(Pointer p) {
            super(p);
            _reserved = getPointer().getPointer(0);
            QueryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            AddRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            Release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            ConfigureDevices = (ConfigureDevicesCallback) CallbackReference.getCallback(ConfigureDevicesCallback.class, p.getPointer(0x20));
            CreateDevice = (CreateDeviceCallback) CallbackReference.getCallback(CreateDeviceCallback.class, p.getPointer(0x20));
            EnumDevices = (EnumDevicesCallback) CallbackReference.getCallback(EnumDevicesCallback.class, p.getPointer(0x20));
            EnumDevicesBySemantics = (EnumDevicesBySemanticsCallback) CallbackReference.getCallback(EnumDevicesBySemanticsCallback.class, p.getPointer(0x20));
            FindDevice = (FindDeviceCallback) CallbackReference.getCallback(FindDeviceCallback.class, p.getPointer(0x20));
            GetDeviceStatus = (GetDeviceStatusCallback) CallbackReference.getCallback(GetDeviceStatusCallback.class, p.getPointer(0x20));
            Initialize = (InitializeCallback) CallbackReference.getCallback(InitializeCallback.class, p.getPointer(0x20));
            RunControlPanel = (RunControlPanelCallback) CallbackReference.getCallback(RunControlPanelCallback.class, p.getPointer(0x20));
        }
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("ConfigureDevices", "CreateDevice", "EnumDevices", "EnumDevicesBySemantics", "FindDevice", "GetDeviceStatus",
                    "GetDeviceStatus", "Initialize", "RunControlPanel");
        }
    }

    class IDirectInputDevice8 extends Structure {

        interface DIEnumEffectsInFileCallback extends Callback {
            int /* HRESULT */ apply(Pointer /* DIFILEEFFECT */ lpDiFileEf, Pointer pvRef);
        }

        interface DIEnumEffectsCallback extends Callback {

            boolean apply(DIEffectInfo pdei, LPVOID pvRef);
        }

        interface DIEnumDeviceObjectsCallback extends Callback {

            boolean apply(DIDEVICEOBJECTINSTANCE pdei, LPVOID pvRef);
        }

        //

        interface AcquireCallBack extends Callback {
            int /* HRESULT */ apply();
        }
        interface BuildActionMapCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* DIACTIONFORMAT */ lpdiaf,
                                    byte[] lpszUserName,
                                    int dwFlags);
        }
        interface CreateEffectCallBack extends Callback {
            int /* HRESULT */ apply(GUID.ByValue rguid,
                                    DIEFFECT lpeff,
                                    PointerByReference ppdeff,
                                    Pointer /* LPUNKNOWN */ punkOuter);
        }
        interface EnumCreatedEffectObjectsCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* LPDIENUMCREATEDEFFECTOBJECTSCALLBACK */ lpCallback,
                                    Pointer pvRef,
                                    int fl);
        }
        interface EnumEffectsCallBack extends Callback {
            int /* HRESULT */ apply(DIEnumEffectsCallback lpCallback,
                                    Pointer pvRef,
                                    int dwEffType);
        }
        interface EnumEffectsInFileCallBack extends Callback {
            int /* HRESULT */ apply(byte[] lpszFileName,
                                    DIEnumEffectsInFileCallback pec,
                                    Pointer pvRef,
                                    int dwFlags);
        }
        interface EnumObjectsCallBack extends Callback {
            int /* HRESULT */ apply(DIEnumDeviceObjectsCallback lpCallback,
                                    Pointer pvRef,
                                    int dwFlags);
        }
        interface EscapeCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* LPDIEFFESCAPE */ pesc);
        }
        interface GetCapabilitiesCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* LPDIDEVCAPS */ lpDIDevCaps);
        }
        interface GetDeviceDataCallBack extends Callback {
            int /* HRESULT */ apply(int cbObjectData,
                                    DIDeviceObjectData[] rgdod,
                                    IntByReference pdwInOut,
                                    int dwFlags);
        }
        interface GetDeviceInfoCallBack extends Callback {
            int /* HRESULT */ apply(DIDEVICEINSTANCE pdidi);
        }
        interface GetDeviceStateCallBack extends Callback {
            int /* HRESULT */ apply(int cbData, ByteBuffer lpvData);
        }
        interface GetEffectInfoCallBack extends Callback {
            int /* HRESULT */ apply(DIEffectInfo pdei, GUID.ByValue rguid);
        }
        interface GetForceFeedbackStateCallBack extends Callback {
            int /* HRESULT */ apply(IntByReference pdwOut);
        }
        interface GetImageInfoCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* DIDEVICEIMAGEINFOHEADER */ lpdiDevImageInfoHeader);
        }
        interface GetObjectInfoCallBack extends Callback {
            int /* HRESULT */ apply(DIDEVICEOBJECTINSTANCE pdidoi,
                                    int dwObj,
                                    int dwHow);
        }
        interface GetPropertyCallBack extends Callback {
            int /* HRESULT */ apply(GUID.ByValue rguidProp,
                                    DIPROPHEADER pdiph);
        }
        interface InitializeCallBack extends Callback {
            int /* HRESULT */ apply(HINSTANCE hinst,
                                    int dwVersion,
                                    GUID.ByValue rguid);
        }
        interface PollCallBack extends Callback {
            int /* HRESULT */ apply();
        }
        interface RunControlPanelCallBack extends Callback {
            int /* HRESULT */ apply(HWND hwndOwner, int dwFlags);
        }
        interface SendDeviceDataCallBack extends Callback {
            int /* HRESULT */ apply(int cbObjectData,
                                    DIDeviceObjectData rgdod,
                                    IntByReference pdwInOut,
                                    int fl);
        }
        interface SendForceFeedbackCommandCallBack extends Callback {
            int /* HRESULT */ apply(int dwFlags);
        }
        interface SetActionMapCallBack extends Callback {
            int /* HRESULT */ apply(Pointer /* DIACTIONFORMAT */ lpdiActionFormat,
                                    byte[] lptszUserName,
                                    int dwFlags);
        }
        interface SetCooperativeLevelCallBack extends Callback {
            int /* HRESULT */ apply(HWND hwnd, int dwFlags);
        }
        interface SetDataFormatCallBack extends Callback {
            int /* HRESULT */ apply(DIDATAFORMAT lpdf);
        }
        interface SetEventNotificationCallBack extends Callback {
            int /* HRESULT */ apply(HANDLE hEvent);
        }
        interface SetPropertyCallBack extends Callback {
            int /* HRESULT */ apply(GUID.ByValue rguidProp, DIPROPHEADER pdiph);
        }
        interface UnacquireCallBack extends Callback {
            int /* HRESULT */ apply();
        }
        interface WriteEffectToFileCallBack extends Callback {
            int /* HRESULT */ apply(byte[] lpszFileName,
                                    int dwEntries,
                                    Pointer /* DIFILEEFFECT */ rgDiFileEft,
                                    int dwFlags);
        }

        // IUNKNOWN_C_GUTS
        public Pointer _reserved;
        public QueryInterfaceCallback QueryInterface;
        public AddRefCallback AddRef;
        public ReleaseCallback Release;

        /** Obtains access to the input device. */
        AcquireCallBack Acquire;
        /** Builds an action map for the device and retrieves information about it. */
        BuildActionMapCallBack BuildActionMap;
        /** Creates and initializes an instance of an effect identified by the effect globally unique identifier (GUID). */
        CreateEffectCallBack CreateEffect;
        /** Enumerates all the currently created effects for this device. */
        EnumCreatedEffectObjectsCallBack EnumCreatedEffectObjects;
        /** Enumerates all the effects supported by the force-feedback system on the device. */
        EnumEffectsCallBack EnumEffects;
        /** Enumerates all the effects in a file created by the Force Editor utility or another application using the same file format. */
        EnumEffectsInFileCallBack EnumEffectsInFile;
        /** Enumerates the input and output objects available on a device. */
        EnumObjectsCallBack EnumObjects;
        /** Sends a hardware-specific command to the force-feedback driver. */
        EscapeCallBack Escape;
        /** Obtains the capabilities of the DirectInputDevice object. */
        GetCapabilitiesCallBack GetCapabilities;
        /** Retrieves buffered data from the device. */
        GetDeviceDataCallBack GetDeviceData;
        /** Obtains information about the device's identity. */
        GetDeviceInfoCallBack GetDeviceInfo;
        /** Retrieves immediate data from the device. */
        GetDeviceStateCallBack GetDeviceState;
        /** Obtains information about an effect. */
        GetEffectInfoCallBack GetEffectInfo;
        /** Retrieves the state of the device's force-feedback system. */
        GetForceFeedbackStateCallBack GetForceFeedbackState;
        /** Retrieves information about a device image for use in a configuration property sheet. */
        GetImageInfoCallBack GetImageInfo;
        /** Retrieves information about a device object, such as a button or axis. */
        GetObjectInfoCallBack GetObjectInfo;
        /** Retrieves information about the input device. */
        GetPropertyCallBack GetProperty;
        /** Initializes a DirectInputDevice object. */
        InitializeCallBack Initialize;
        /** Retrieves data from polled objects on a DirectInput device. */
        PollCallBack Poll;
        /** Runs the DirectInput control panel associated with this device. */
        RunControlPanelCallBack RunControlPanel;
        /** Sends data to a device that accepts output. */
        SendDeviceDataCallBack SendDeviceData;
        /** Sends a command to the device's force-feedback system. */
        SendForceFeedbackCommandCallBack SendForceFeedbackCommand;
        /** Sets the data format for a device and maps application-defined actions to device objects. */
        SetActionMapCallBack SetActionMap;
        /** Establishes the cooperative level for this instance of the device. */
        SetCooperativeLevelCallBack SetCooperativeLevel;
        /** Sets the data format for the DirectInput device. */
        SetDataFormatCallBack SetDataFormat;
        /** Specifies an event that is to be set when the device state changes. */
        SetEventNotificationCallBack SetEventNotification;
        /** Sets properties that define the device behavior. */
        SetPropertyCallBack SetProperty;
        /** Releases access to the device. */
        UnacquireCallBack Unacquire;
        /** Saves information about one or more force-feedback effects to a file that can be read by using EnumEffectsInFile. */
        WriteEffectToFileCallBack WriteEffectToFile;
        public IDirectInputDevice8(Pointer p) {
            super(p);
            _reserved = getPointer().getPointer(0);
            QueryInterface = (QueryInterfaceCallback) CallbackReference.getCallback(QueryInterfaceCallback.class, p.getPointer(0x08));
            AddRef = (AddRefCallback) CallbackReference.getCallback(AddRefCallback.class, p.getPointer(0x10));
            Release = (ReleaseCallback) CallbackReference.getCallback(ReleaseCallback.class, p.getPointer(0x18));

            AcquireCallBack Acquire = (AcquireCallBack) CallbackReference.getCallback(AcquireCallBack.class, p.getPointer(0x20));
            BuildActionMapCallBack BuildActionMap = (BuildActionMapCallBack) CallbackReference.getCallback(BuildActionMapCallBack.class, p.getPointer(0x28));
            CreateEffectCallBack CreateEffect = (CreateEffectCallBack) CallbackReference.getCallback(CreateEffectCallBack.class, p.getPointer(0x30));
            EnumCreatedEffectObjectsCallBack EnumCreatedEffectObjects = (EnumCreatedEffectObjectsCallBack) CallbackReference.getCallback(EnumCreatedEffectObjectsCallBack.class, p.getPointer(0x38));
            EnumEffectsCallBack EnumEffects = (EnumEffectsCallBack) CallbackReference.getCallback(EnumEffectsCallBack.class, p.getPointer(0x40));
            EnumEffectsInFileCallBack EnumEffectsInFile = (EnumEffectsInFileCallBack) CallbackReference.getCallback(EnumEffectsInFileCallBack.class, p.getPointer(0x48));
            EnumObjectsCallBack EnumObjects = (EnumObjectsCallBack) CallbackReference.getCallback(EnumObjectsCallBack.class, p.getPointer(0x50));
            EscapeCallBack Escape = (EscapeCallBack) CallbackReference.getCallback(EscapeCallBack.class, p.getPointer(0x58));
            GetCapabilitiesCallBack GetCapabilities = (GetCapabilitiesCallBack) CallbackReference.getCallback(GetCapabilitiesCallBack.class, p.getPointer(0x60));
            GetDeviceDataCallBack GetDeviceData = (GetDeviceDataCallBack) CallbackReference.getCallback(GetDeviceDataCallBack.class, p.getPointer(0x68));
            GetDeviceInfoCallBack GetDeviceInfo = (GetDeviceInfoCallBack) CallbackReference.getCallback(GetDeviceInfoCallBack.class, p.getPointer(0x70));
            GetDeviceStateCallBack GetDeviceState = (GetDeviceStateCallBack) CallbackReference.getCallback(GetDeviceStateCallBack.class, p.getPointer(0x78));
            GetEffectInfoCallBack GetEffectInfo = (GetEffectInfoCallBack) CallbackReference.getCallback(GetEffectInfoCallBack.class, p.getPointer(0x80));
            GetForceFeedbackStateCallBack GetForceFeedbackState = (GetForceFeedbackStateCallBack) CallbackReference.getCallback(GetForceFeedbackStateCallBack.class, p.getPointer(0x88));
            GetImageInfoCallBack GetImageInfo = (GetImageInfoCallBack) CallbackReference.getCallback(GetImageInfoCallBack.class, p.getPointer(0x90));
            GetObjectInfoCallBack GetObjectInfo = (GetObjectInfoCallBack) CallbackReference.getCallback(GetObjectInfoCallBack.class, p.getPointer(0x98));
            GetPropertyCallBack GetProperty = (GetPropertyCallBack) CallbackReference.getCallback(GetPropertyCallBack.class, p.getPointer(0xa0));
            InitializeCallBack Initialize = (InitializeCallBack) CallbackReference.getCallback(InitializeCallBack.class, p.getPointer(0xa8));
            PollCallBack Poll = (PollCallBack) CallbackReference.getCallback(PollCallBack.class, p.getPointer(0xb0));
            RunControlPanelCallBack RunControlPanel = (RunControlPanelCallBack) CallbackReference.getCallback(RunControlPanelCallBack.class, p.getPointer(0xb8));
            SendDeviceDataCallBack SendDeviceData = (SendDeviceDataCallBack) CallbackReference.getCallback(SendDeviceDataCallBack.class, p.getPointer(0xc0));
            SendForceFeedbackCommandCallBack SendForceFeedbackCommand = (SendForceFeedbackCommandCallBack) CallbackReference.getCallback(SendForceFeedbackCommandCallBack.class, p.getPointer(0xc8));
            SetActionMapCallBack SetActionMap = (SetActionMapCallBack) CallbackReference.getCallback(SetActionMapCallBack.class, p.getPointer(0xd0));
            SetCooperativeLevelCallBack SetCooperativeLevel = (SetCooperativeLevelCallBack) CallbackReference.getCallback(SetCooperativeLevelCallBack.class, p.getPointer(0xd8));
            SetDataFormatCallBack SetDataFormat = (SetDataFormatCallBack) CallbackReference.getCallback(SetDataFormatCallBack.class, p.getPointer(0xe0));
            SetEventNotificationCallBack SetEventNotification = (SetEventNotificationCallBack) CallbackReference.getCallback(SetEventNotificationCallBack.class, p.getPointer(0xe8));
            SetPropertyCallBack SetProperty = (SetPropertyCallBack) CallbackReference.getCallback(SetPropertyCallBack.class, p.getPointer(0xf0));
            UnacquireCallBack Unacquire = (UnacquireCallBack) CallbackReference.getCallback(UnacquireCallBack.class, p.getPointer(0xf8));
            WriteEffectToFileCallBack WriteEffectToFile = (WriteEffectToFileCallBack) CallbackReference.getCallback(WriteEffectToFileCallBack.class, p.getPointer(0x100));
        }
        @Override protected List<String> getFieldOrder() {
            return Arrays.asList("_reserved", "QueryInterface", "AddRef", "Release",
                    "AcquireCallBack", "BuildActionMapCallBack", "CreateEffectCallBack",
                    "EnumCreatedEffectObjectsCallBack", "EnumEffectsCallBack",
                    "GetCapabilitiesCallBack", "GetDeviceDataCallBack", "GetDeviceInfoCallBack",
                    "GetDeviceStateCallBack", "GetEffectInfoCallBack", "GetForceFeedbackStateCallBack", "GetImageInfoCallBack",
                    "GetObjectInfoCallBack", "GetPropertyCallBack", "InitializeCallBack", "PollCallBack",
                    "RunControlPanelCallBack", "SendDeviceDataCallBack", "SendForceFeedbackCommandCallBack",
                    "SetActionMapCallBack", "SetCooperativeLevelCallBack", "SetDataFormatCallBack",
                    "SetEventNotificationCallBack", "SetPropertyCallBack", "UnacquireCallBack", "WriteEffectToFileCallBack");
        }
    }

    boolean DIENUM_CONTINUE = true;
    boolean DIENUM_STOP = false;

    interface LowLevelMouseProc extends HOOKPROC {

        LRESULT apply(int nCode, WPARAM wParam, MSLLHOOKSTRUCT lParam);
    }

    // https://msdn.microsoft.com/library/ff468877.aspx
    int WM_MOUSEMOVE = 0x0200;
    int WM_LBUTTONDOWN = 0x0201;
    int WM_LBUTTONUP = 0x0202;
    int WM_LBUTTONDBLCLK = 0x0203;
    int WM_RBUTTONDOWN = 0x0204;
    int WM_RBUTTONUP = 0x0205;
    int WM_RBUTTONDBLCLK = 0x0206;
    int WM_MBUTTONDOWN = 0x0207;
    int WM_MBUTTONUP = 0x0208;
    int WM_MBUTTONDBLCLK = 0x0209;
    int WM_MOUSEWHEEL = 0x020A;
    int WM_XBUTTONDOWN = 0x020B;
    int WM_XBUTTONUP = 0x020C;
    int WM_XBUTTONDBLCLK = 0x020D;
    int WM_MOUSEHWHEEL = 0x020E;

    // https://msdn.microsoft.com/library/ms646245.aspx
    // low-order
    int MK_LBUTTON = 0x0001;
    int MK_RBUTTON = 0x0002;
    int MK_SHIFT = 0x0004;
    int MK_CONTROL = 0x0008;
    int MK_MBUTTON = 0x0010;
    int MK_XBUTTON1 = 0x0020;
    int MK_XBUTTON2 = 0x0040;

    // high-order
    int XBUTTON1 = 0x0001;
    int XBUTTON2 = 0x0002;

    // https://msdn.microsoft.com/library/ms646273.aspx
    int WHEEL_DELTA = 120;
    int MOUSEEVENTF_ABSOLUTE = 0x8000;
    int MOUSEEVENTF_HWHEEL = 0x01000;
    int MOUSEEVENTF_MOVE = 0x0001;
    int MOUSEEVENTF_LEFTDOWN = 0x0002;
    int MOUSEEVENTF_LEFTUP = 0x0004;
    int MOUSEEVENTF_RIGHTDOWN = 0x0008;
    int MOUSEEVENTF_RIGHTUP = 0x0010;
    int MOUSEEVENTF_MIDDLEDOWN = 0x0020;
    int MOUSEEVENTF_MIDDLEUP = 0x0040;
    int MOUSEEVENTF_WHEEL = 0x0800;
    int MOUSEEVENTF_XDOWN = 0x0080;
    int MOUSEEVENTF_XUP = 0x0100;

    // https://msdn.microsoft.com/library/windows/desktop/dd375731.aspx
    int VK_LBUTTON = 0x01;
    int VK_RBUTTON = 0x02;
    int VK_MBUTTON = 0x04;
    int VK_XBUTTON1 = 0x05;
    int VK_XBUTTON2 = 0x06;
    int VK_ESCAPE = 0x1B;

    // https://msdn.microsoft.com/library/windows/desktop/ms648395.aspx
    int OCR_APPSTARTING = 32650;
    int OCR_NORMAL = 32512;
    int OCR_CROSS = 32515;
    int OCR_HAND = 32649;
    int OCR_HELP = 32651;
    int OCR_IBEAM = 32513;
    int OCR_NO = 32648;
    int OCR_SIZEALL = 32646;
    int OCR_SIZENESW = 32643;
    int OCR_SIZENS = 32645;
    int OCR_SIZENWSE = 32642;
    int OCR_SIZEWE = 32644;
    int OCR_UP = 32516;
    int OCR_WAIT = 32514;

    // https://msdn.microsoft.com/library/windows/desktop/ms724947.aspx
    int SPI_SETCURSORS = 0x0057;

    int MSGFLT_ALLOW = 1;
    int MSGFLT_DISALLOW = 2;
    int MSGFLT_RESET = 0;

    int WM_QUERYENDSESSION = 0x0011;
    int WM_INPUT = 0x00ff;
    int RIM_INPUT = 0;
    int RIM_INPUTSINK = 1;

    short HID_USAGE_PAGE_GENERIC = 0x01;
    short HID_USAGE_GENERIC_MOUSE = 0x02;

    int RIDEV_INPUTSINK = 0x00000100;
    int RIDEV_REMOVE = 0x00000001;

    int RID_INPUT = 0x10000003;
    int MOUSE_MOVE_RELATIVE = 0;
    //int RIM_TYPEMOUSE = 0;

    User32ExTrait INSTANCE = Native.load("user32", User32ExTrait.class);

    GUID GUID_XAxis              = GUID.fromString("{A36D02E0-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_YAxis              = GUID.fromString("{A36D02E1-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_ZAxis              = GUID.fromString("{A36D02E2-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_RAxis              = GUID.fromString("{A36D02E3-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_UAxis              = GUID.fromString("{A36D02E4-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_VAxis              = GUID.fromString("{A36D02E5-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_RxAxis             = GUID.fromString("{A36D02F4-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_RyAxis             = GUID.fromString("{A36D02F5-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_RzAxis             = GUID.fromString("{A36D02E3-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_Slider             = GUID.fromString("{A36D02E4-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_Button             = GUID.fromString("{A36D02F0-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_Key                = GUID.fromString("{55728220-D33C-11CF-BFC7-444553540000}");
    GUID GUID_POV                = GUID.fromString("{A36D02F2-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_Unknown            = GUID.fromString("{A36D02F3-C9F3-11CF-BFC7-444553540000}");
    GUID GUID_SysMouse           = GUID.fromString("{6F1D2B60-D5A0-11CF-BFC7-444553540000}");
    GUID GUID_SysKeyboard        = GUID.fromString("{6F1D2B61-D5A0-11CF-BFC7-444553540000}");
    GUID GUID_Joystick           = GUID.fromString("{6F1D2B70-D5A0-11CF-BFC7-444553540000}");

    GUID GUID_ConstantForce      = GUID.fromString("{13541C20-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_RampForce          = GUID.fromString("{13541C21-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Square             = GUID.fromString("{13541C22-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Sine               = GUID.fromString("{13541C23-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Triangle           = GUID.fromString("{13541C24-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_SawtoothUp         = GUID.fromString("{13541C25-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_SawtoothDown       = GUID.fromString("{13541C26-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Spring             = GUID.fromString("{13541C27-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Damper             = GUID.fromString("{13541C28-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Inertia            = GUID.fromString("{13541C29-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_Friction           = GUID.fromString("{13541C2A-8E33-11D0-9AD0-00A0C9A06E35}");
    GUID GUID_CustomForce        = GUID.fromString("{13541C2B-8E33-11D0-9AD0-00A0C9A06E35}");

    int DI8DEVCLASS_ALL = 0;
    int DIEDFL_ATTACHEDONLY = 0x00000001;

    GUID IID_IDirectInput8 = GUID.fromString("{59c18ef56d3d33b98e319e8c0cd30c24fc363e4e}"); // TODO

    static GUID MAKEDIPROP(int prop) { return GUID.fromBinary(new byte[prop]); }

    GUID DIPROP_BUFFERSIZE = MAKEDIPROP(1);
    GUID DIPROP_RANGE = MAKEDIPROP(4);
    GUID DIPROP_DEADZONE = MAKEDIPROP(5);

    interface User32ExTrait extends User32 {

        short GetKeyState(int vKey);

        // https://msdn.microsoft.com/library/windows/desktop/aa383751.aspx
        Pointer LoadImageW(HINSTANCE hinst, Pointer ptr, int uType, int xDesired, int yDesired, int load);

        Pointer LoadCursorW(HINSTANCE hInstance, Pointer lpCursorName);

        Boolean SystemParametersInfoW(int uiAction, int uiParam, Pointer pvParam, int fWinIni);

        Boolean SetSystemCursor(Pointer hcur, int id);

        HMONITOR MonitorFromPoint(POINT pt, int dwFlags);

        Boolean ChangeWindowMessageFilterEx(HWND hWnd, int msg, int action, Pointer pcfs);

        boolean RegisterRawInputDevices(RAWINPUTDEVICE[] pRawInputDevices, int uiNumDevices, int cbSize);

        int GetRawInputData(Pointer hRawInput, int uiCommand, RAWINPUT[] pData, ByReference pcbSize, int cbSizeHeader);
        int RIDI_DEVICENAME = 0x20000007;
        int RIDI_DEVICEINFO = 0x2000000b;
        int GetRawInputDeviceInfoA(HANDLE hDevice, int uiCommand, Pointer pData, IntByReference pcbSize);

        HMODULE GetModuleHandle(byte[] lpModuleName);

        int DIRECTINPUT_VERSION = 0x0800;
        /** Creates a DirectInput object and returns an IDirectInput8 Interface or later interface. */
        int /* HRESULT */ DirectInput8Create(HINSTANCE hinst, int dwVersion, GUID.ByValue riidltf, PointerByReference ppvOut, Pointer /* LPUNKNOWN */ punkOuter
        );
    }

    interface Kernel32Ex extends Library {

        // https://msdn.microsoft.com/library/windows/desktop/ms686219.aspx
        int ABOVE_NORMAL_PRIORITY_CLASS = 0x00008000;
        int HIGH_PRIORITY_CLASS = 0x00000080;
        int NORMAL_PRIORITY_CLASS = 0x00000020;

        Kernel32Ex INSTANCE = Native.load("kernel32", Kernel32Ex.class);
    }

    interface Kernel32ExTrait extends Kernel32 {

        boolean SetPriorityClass(HANDLE hProcess, int dwPriorityClass);
    }

    class HDEVINFO extends HANDLE {

        public HDEVINFO() {
        }

        public HDEVINFO(Pointer ptr) {
            super(ptr);
        }
    }

    interface SetupApiInterface extends Library {

        SetupApiInterface INSTANCE = Native.load("setupapi", SetupApiInterface.class,
                W32APIOptions.UNICODE_OPTIONS);

        GUID GUID_DEVCLASS_KEYBOARD = GUID.fromString("{4d36e96b-e325-11ce-bfc1-08002be10318}");
        GUID GUID_DEVCLASS_MOUSE = GUID.fromString("{4d36e96f-e325-11ce-bfc1-08002be10318}");

        HDEVINFO SetupDiGetClassDevs(GUID ClassGuid, String Enumerator, HWND hwndParent, int Flags);

        boolean SetupDiEnumDeviceInfo(HDEVINFO DeviceInfoSet, int MemberIndex, SP_DEVINFO_DATA DeviceInfoData);

        boolean SetupDiGetDeviceRegistryProperty(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData, int Property,
                                                 int PropertyRegDataType, Memory PropertyBuffer, int PropertyBufferSize, int RequiredSize);

        boolean SetupDiGetDeviceInstanceId(HDEVINFO DeviceInfoSet, SP_DEVINFO_DATA DeviceInfoData,
                                           Memory DeviceInstanceId, int DeviceInstanceIdSize, int RequiredSize);

        boolean SetupDiDestroyDeviceInfoList(HDEVINFO DeviceInfoSet);
    }
}
