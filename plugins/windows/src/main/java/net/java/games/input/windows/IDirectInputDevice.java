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
 *****************************************************************************/

package net.java.games.input.windows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Rumbler;
import net.java.games.input.windows.WinAPI.DIEFFECT;
import net.java.games.input.windows.WinAPI.DIENVELOPE;
import net.java.games.input.windows.WinAPI.DIOBJECTDATAFORMAT;
import net.java.games.input.windows.WinAPI.DIPERIODIC;
import net.java.games.input.windows.WinAPI.DIPROPRANGE;
import net.java.games.input.windows.WinAPI.DIDEVICEOBJECTINSTANCE;
import net.java.games.input.windows.WinAPI.DIDATAFORMAT;
import net.java.games.input.windows.WinAPI.DIPROPDWORD;
import net.java.games.input.windows.WinAPI.IDirectInputDevice8;

import static com.sun.jna.platform.win32.COM.COMUtils.S_FALSE;
import static net.java.games.input.windows.WinAPI.DIENUM_CONTINUE;
import static net.java.games.input.windows.WinAPI.DIPH_BYID;
import static net.java.games.input.windows.WinAPI.DIPH_DEVICE;
import static net.java.games.input.windows.WinAPI.DIPROP_BUFFERSIZE;
import static net.java.games.input.windows.WinAPI.DIPROP_DEADZONE;
import static net.java.games.input.windows.WinAPI.DIPROP_RANGE;


/**
 * Java wrapper for IDirectInputDevice
 *
 * @author martak
 * @author elias
 * @version 1.0
 */
final class IDirectInputDevice {

    private static final Logger log = Logger.getLogger(IDirectInputDevice.class.getName());

    public final static int GUID_XAxis = 1;
    public final static int GUID_YAxis = 2;
    public final static int GUID_ZAxis = 3;
    public final static int GUID_RxAxis = 4;
    public final static int GUID_RyAxis = 5;
    public final static int GUID_RzAxis = 6;
    public final static int GUID_Slider = 7;
    public final static int GUID_Button = 8;
    public final static int GUID_Key = 9;
    public final static int GUID_POV = 10;
    public final static int GUID_Unknown = 11;

    public final static int GUID_ConstantForce = 12;
    public final static int GUID_RampForce = 13;
    public final static int GUID_Square = 14;
    public final static int GUID_Sine = 15;
    public final static int GUID_Triangle = 16;
    public final static int GUID_SawtoothUp = 17;
    public final static int GUID_SawtoothDown = 18;
    public final static int GUID_Spring = 19;
    public final static int GUID_Damper = 20;
    public final static int GUID_Inertia = 21;
    public final static int GUID_Friction = 22;
    public final static int GUID_CustomForce = 23;

    public final static int DI8DEVTYPE_DEVICE = 0x11;
    public final static int DI8DEVTYPE_MOUSE = 0x12;
    public final static int DI8DEVTYPE_KEYBOARD = 0x13;
    public final static int DI8DEVTYPE_JOYSTICK = 0x14;
    public final static int DI8DEVTYPE_GAMEPAD = 0x15;
    public final static int DI8DEVTYPE_DRIVING = 0x16;
    public final static int DI8DEVTYPE_FLIGHT = 0x17;
    public final static int DI8DEVTYPE_1STPERSON = 0x18;
    public final static int DI8DEVTYPE_DEVICECTRL = 0x19;
    public final static int DI8DEVTYPE_SCREENPOINTER = 0x1A;
    public final static int DI8DEVTYPE_REMOTE = 0x1B;
    public final static int DI8DEVTYPE_SUPPLEMENTAL = 0x1C;

    public final static int DISCL_EXCLUSIVE = 0x00000001;
    public final static int DISCL_NONEXCLUSIVE = 0x00000002;
    public final static int DISCL_FOREGROUND = 0x00000004;
    public final static int DISCL_BACKGROUND = 0x00000008;
    public final static int DISCL_NOWINKEY = 0x00000010;

    public final static int DIDFT_ALL = 0x00000000;

    public final static int DIDFT_RELAXIS = 0x00000001;
    public final static int DIDFT_ABSAXIS = 0x00000002;
    public final static int DIDFT_AXIS = 0x00000003;

    public final static int DIDFT_PSHBUTTON = 0x00000004;
    public final static int DIDFT_TGLBUTTON = 0x00000008;
    public final static int DIDFT_BUTTON = 0x0000000C;

    public final static int DIDFT_POV = 0x00000010;
    public final static int DIDFT_COLLECTION = 0x00000040;
    public final static int DIDFT_NODATA = 0x00000080;

    public final static int DIDFT_FFACTUATOR = 0x01000000;
    public final static int DIDFT_FFEFFECTTRIGGER = 0x02000000;
    public final static int DIDFT_OUTPUT = 0x10000000;
    public final static int DIDFT_VENDORDEFINED = 0x04000000;
    public final static int DIDFT_ALIAS = 0x08000000;
    public final static int DIDFT_OPTIONAL = 0x80000000;

    public final static int DIDFT_NOCOLLECTION = 0x00FFFF00;

    public final static int DIDF_ABSAXIS = 0x00000001;
    public final static int DIDF_RELAXIS = 0x00000002;

    public final static int DI_OK = 0x00000000;
    public final static int DI_NOEFFECT = 0x00000001;
    public final static int DI_PROPNOEFFECT = 0x00000001;
    public final static int DI_POLLEDDEVICE = 0x00000002;

    public final static int DI_DOWNLOADSKIPPED = 0x00000003;
    public final static int DI_EFFECTRESTARTED = 0x00000004;
    public final static int DI_TRUNCATED = 0x00000008;
    public final static int DI_SETTINGSNOTSAVED = 0x0000000B;
    public final static int DI_TRUNCATEDANDRESTARTED = 0x0000000C;

    public final static int DI_BUFFEROVERFLOW = 0x00000001;
    public final static int DIERR_INPUTLOST = 0x8007001E;
    public final static int DIERR_NOTACQUIRED = 0x8007001C;
    public final static int DIERR_OTHERAPPHASPRIO = 0x80070005;

    public final static int DIDOI_FFACTUATOR = 0x00000001;
    public final static int DIDOI_FFEFFECTTRIGGER = 0x00000002;
    public final static int DIDOI_POLLED = 0x00008000;
    public final static int DIDOI_ASPECTPOSITION = 0x00000100;
    public final static int DIDOI_ASPECTVELOCITY = 0x00000200;
    public final static int DIDOI_ASPECTACCEL = 0x00000300;
    public final static int DIDOI_ASPECTFORCE = 0x00000400;
    public final static int DIDOI_ASPECTMASK = 0x00000F00;
    public final static int DIDOI_GUIDISUSAGE = 0x00010000;

    public final static int DIEFT_ALL = 0x00000000;

    public final static int DIEFT_CONSTANTFORCE = 0x00000001;
    public final static int DIEFT_RAMPFORCE = 0x00000002;
    public final static int DIEFT_PERIODIC = 0x00000003;
    public final static int DIEFT_CONDITION = 0x00000004;
    public final static int DIEFT_CUSTOMFORCE = 0x00000005;
    public final static int DIEFT_HARDWARE = 0x000000FF;
    public final static int DIEFT_FFATTACK = 0x00000200;
    public final static int DIEFT_FFFADE = 0x00000400;
    public final static int DIEFT_SATURATION = 0x00000800;
    public final static int DIEFT_POSNEGCOEFFICIENTS = 0x00001000;
    public final static int DIEFT_POSNEGSATURATION = 0x00002000;
    public final static int DIEFT_DEADBAND = 0x00004000;
    public final static int DIEFT_STARTDELAY = 0x00008000;

    public final static int DIEFF_OBJECTIDS = 0x00000001;
    public final static int DIEFF_OBJECTOFFSETS = 0x00000002;
    public final static int DIEFF_CARTESIAN = 0x00000010;
    public final static int DIEFF_POLAR = 0x00000020;
    public final static int DIEFF_SPHERICAL = 0x00000040;

    public final static int DIEP_DURATION = 0x00000001;
    public final static int DIEP_SAMPLEPERIOD = 0x00000002;
    public final static int DIEP_GAIN = 0x00000004;
    public final static int DIEP_TRIGGERBUTTON = 0x00000008;
    public final static int DIEP_TRIGGERREPEATINTERVAL = 0x00000010;
    public final static int DIEP_AXES = 0x00000020;
    public final static int DIEP_DIRECTION = 0x00000040;
    public final static int DIEP_ENVELOPE = 0x00000080;
    public final static int DIEP_TYPESPECIFICPARAMS = 0x00000100;
    public final static int DIEP_STARTDELAY = 0x00000200;
    public final static int DIEP_ALLPARAMS_DX5 = 0x000001FF;
    public final static int DIEP_ALLPARAMS = 0x000003FF;
    public final static int DIEP_START = 0x20000000;
    public final static int DIEP_NORESTART = 0x40000000;
    public final static int DIEP_NODOWNLOAD = 0x80000000;
    public final static int DIEB_NOTRIGGER = 0xFFFFFFFF;

    public final static int INFINITE = 0xFFFFFFFF;

    public final static int DI_DEGREES = 100;
    public final static int DI_FFNOMINALMAX = 10000;
    public final static int DI_SECONDS = 1000000;

    public final static int DIPROPRANGE_NOMIN = 0x80000000;
    public final static int DIPROPRANGE_NOMAX = 0x7FFFFFFF;

    private final DummyWindow window;
    private final Pointer address;
    private final int devType;
    private final int devSubtype;
    private final String instanceName;
    private final String productName;
    private final List<DIDeviceObject> objects = new ArrayList<>();
    private final List<DIEffectInfo> effects = new ArrayList<>();
    private final List<Rumbler> rumblers = new ArrayList<>();
    private final int[] deviceState;
    private final Map<DIDeviceObject, DIComponent> objectToComponent = new HashMap<>();
    private final boolean axesInRelativeMode;

    private boolean released;
    private DataQueue<DIDeviceObjectData> queue;

    private int buttonCounter;
    private int currentFormatOffset;

    final DIDeviceObjectData diEvent = new DIDeviceObjectData();

    public IDirectInputDevice(DummyWindow window, Pointer address, byte[] instanceGuid, byte[] productGuid,
                              int devType, int devSubtype, String instanceName, String productName) throws IOException {
        this.window = window;
        this.address = address;
        this.productName = productName;
        this.instanceName = instanceName;
        this.devType = devType;
        this.devSubtype = devSubtype;
        // Assume that the caller (native side) releases the device if setup fails
        enumObjects();
        try {
            enumEffects();
            createRumblers();
        } catch (IOException e) {
            log.fine("Failed to create rumblers: " + e.getMessage());
        }
        // Some DirectInput lamer-designer made the device state
        // axis mode be per-device not per-axis, so I'll just
        // get all axes as absolute and compensate for relative axes.
        //
        // Unless, of course, all axes are relative like a mouse device,
        // in which case setting the DIDF_ABSAXIS flag will result in
        // incorrect axis values returned from GetDeviceData for some
        // obscure reason.
        boolean allRelative = true;
        boolean hasAxis = false;
        for (DIDeviceObject obj : objects) {
            if (obj.isAxis()) {
                hasAxis = true;
                if (!obj.isRelative()) {
                    allRelative = false;
                    break;
                }
            }
        }
        this.axesInRelativeMode = allRelative && hasAxis;
        int axisMode = allRelative ? DIDF_RELAXIS : DIDF_ABSAXIS;
        setDataFormat(axisMode);
        if (!rumblers.isEmpty()) {
            try {
                setCooperativeLevel(DISCL_BACKGROUND | DISCL_EXCLUSIVE);
            } catch (IOException e) {
                setCooperativeLevel(DISCL_BACKGROUND | DISCL_NONEXCLUSIVE);
            }
        } else
            setCooperativeLevel(DISCL_BACKGROUND | DISCL_NONEXCLUSIVE);
        setBufferSize(AbstractController.EVENT_QUEUE_DEPTH);
        acquire();
        this.deviceState = new int[objects.size()];
    }

    public boolean areAxesRelative() {
        return axesInRelativeMode;
    }

    public Rumbler[] getRumblers() {
        return rumblers.toArray(Rumbler[]::new);
    }

    private List<Rumbler> createRumblers() throws IOException {
        DIDeviceObject xAxis = lookupObjectByGUID(GUID_XAxis);
//		DIDeviceObject yAxis = lookupObjectByGUID(GUID_YAxis);
        if (xAxis == null/* || yAxis == null*/)
            return rumblers;
        DIDeviceObject[] axes = {xAxis/*, yAxis*/};
        long[] directions = {0/*, 0*/};
        for (DIEffectInfo info : effects) {
            if ((info.getEffectType() & 0xff) == DIEFT_PERIODIC &&
                    (info.getDynamicParams() & DIEP_GAIN) != 0) {
                rumblers.add(createPeriodicRumbler(axes, directions, info));
            }
        }
        return rumblers;
    }

    private Rumbler createPeriodicRumbler(DIDeviceObject[] axes, long[] directions, DIEffectInfo info) throws IOException {
        int[] axisIds = new int[axes.length];
        for (int i = 0; i < axisIds.length; i++) {
            axisIds[i] = axes[i].getDIIdentifier();
        }
        Pointer effectAddress = nCreatePeriodicEffect(address, info.getGUID(), DIEFF_CARTESIAN | DIEFF_OBJECTIDS, INFINITE, 0, DI_FFNOMINALMAX, DIEB_NOTRIGGER, 0, axisIds, directions, 0, 0, 0, 0, DI_FFNOMINALMAX, 0, 0, 50000, 0);
        return new IDirectInputEffect(effectAddress, info);
    }

    static class EnumContext extends Structure {
        public int id;

        public EnumContext() {
        }

        public EnumContext(Pointer p) {
            super(p);
        }

        static int idMaster = 0;
        static Map<Integer, IDirectInputDevice> map = new HashMap<>();
    }

    private static int mapGUIDType(GUID guid) {
        if (guid.equals(WinAPI.GUID_XAxis)) {
            return GUID_XAxis;
        } else if (guid.equals(WinAPI.GUID_YAxis)) {
            return GUID_YAxis;
        } else if (guid.equals(WinAPI.GUID_ZAxis)) {
            return GUID_ZAxis;
        } else if (guid.equals(WinAPI.GUID_RxAxis)) {
            return GUID_RxAxis;
        } else if (guid.equals(WinAPI.GUID_RyAxis)) {
            return GUID_RyAxis;
        } else if (guid.equals(WinAPI.GUID_RzAxis)) {
            return GUID_RzAxis;
        } else if (guid.equals(WinAPI.GUID_Slider)) {
            return GUID_Slider;
        } else if (guid.equals(WinAPI.GUID_Button)) {
            return GUID_Button;
        } else if (guid.equals(WinAPI.GUID_Key)) {
            return GUID_Key;
        } else if (guid.equals(WinAPI.GUID_POV)) {
            return GUID_POV;
        } else if (guid.equals(WinAPI.GUID_ConstantForce)) {
            return GUID_ConstantForce;
        } else if (guid.equals(WinAPI.GUID_RampForce)) {
            return GUID_RampForce;
        } else if (guid.equals(WinAPI.GUID_Square)) {
            return GUID_Square;
        } else if (guid.equals(WinAPI.GUID_Sine)) {
            return GUID_Sine;
        } else if (guid.equals(WinAPI.GUID_Triangle)) {
            return GUID_Triangle;
        } else if (guid.equals(WinAPI.GUID_SawtoothUp)) {
            return GUID_SawtoothUp;
        } else if (guid.equals(WinAPI.GUID_SawtoothDown)) {
            return GUID_SawtoothDown;
        } else if (guid.equals(WinAPI.GUID_Spring)) {
            return GUID_Spring;
        } else if (guid.equals(WinAPI.GUID_Damper)) {
            return GUID_Damper;
        } else if (guid.equals(WinAPI.GUID_Inertia)) {
            return GUID_Inertia;
        } else if (guid.equals(WinAPI.GUID_Friction)) {
            return GUID_Friction;
        } else if (guid.equals(WinAPI.GUID_CustomForce)) {
            return GUID_CustomForce;
        } else
        return GUID_Unknown;
    }

    private static boolean enumEffectsCallback(DIEffectInfo info, LPVOID context) {
        EnumContext enumContext = new EnumContext(context.getPointer());
        IDirectInputDevice _this = EnumContext.map.get(enumContext.id);

        byte[] guid = NativeUtil.wrapGUID(info.guid);
        String name = new String(info.tszName, StandardCharsets.UTF_8);
        int guidId = mapGUIDType((info.guid));
        _this.addEffect(guid, guidId, info.dwEffType, info.dwStaticParams, info.dwDynamicParams, name);
        return DIENUM_CONTINUE;
    }

    private static boolean enumObjectsCallback(DIDEVICEOBJECTINSTANCE deviceObjInstance, LPVOID context) {
        EnumContext enumContext = new EnumContext(context.getPointer());
        IDirectInputDevice _this = EnumContext.map.get(enumContext.id);

        byte[] guid = NativeUtil.wrapGUID(deviceObjInstance.guidType);
        String name = new String(deviceObjInstance.tszName, StandardCharsets.UTF_8).replace("\u0000", "");
        int instance = (deviceObjInstance.dwType >> 8) & 0xff; // DIDFT_GETINSTANCE
        int type = (deviceObjInstance.dwType) & 0xff; // DIDFT_GETTYPE
        int guidType = mapGUIDType(deviceObjInstance.guidType);
//System.err.printf("name %s guidType %d id %d", deviceObjInstance.tszName, guidType, deviceObjInstance.dwType);
        _this.addObject(guid, guidType, deviceObjInstance.dwType, type, instance, deviceObjInstance.dwFlags, name);
        return DIENUM_CONTINUE;
    }

    /** @return Pointer WinAPI.IDirectInputEffect */
    private static Pointer nCreatePeriodicEffect(Pointer address,
                                                 byte[] effectGuidArray,
                                                 int flags,
                                                 int duration,
                                                 int samplePeriod,
                                                 int gain,
                                                 int triggerButton,
                                                 int triggerRepeatInterval,
                                                 int[] axisIdsArray,
                                                 long[] directionsArray,
                                                 int envelopeAttackLevel,
                                                 int envelopeAttackTime,
                                                 int envelopeFadeLevel,
                                                 int envelopeFadeTime,
                                                 int periodicMagnitude,
                                                 int periodicOffset,
                                                 int periodicPhase,
                                                 int periodicPeriod,
                                                 int startDelay) throws IOException {

        IDirectInputDevice8 lpDevice = new IDirectInputDevice8(address);
        DIEFFECT effect = new DIEFFECT();
        GUID.ByValue effectGuid = new GUID.ByValue();
        DIPERIODIC.ByReference periodic = new DIPERIODIC.ByReference();
        DIENVELOPE.ByReference envelope = new DIENVELOPE.ByReference();

        int numAxes = axisIdsArray.length;
        int numDirections = directionsArray.length;

        if (numAxes != numDirections) {
            throw new IOException("axisIdsArray.length != directions.length");
        }

        NativeUtil.unwrapGUID(effectGuidArray, effectGuid);
        int[] axisIdsDword = new int[numAxes];
        long[] directionsLong = new long[numDirections];
        System.arraycopy(axisIdsArray, 0, axisIdsDword, 0, numAxes);
        System.arraycopy(directionsArray, 0, directionsLong, 0, numDirections);

        envelope.dwSize = envelope.size();
        envelope.dwAttackLevel = envelopeAttackLevel;
        envelope.dwAttackTime = envelopeAttackTime;
        envelope.dwFadeLevel = envelopeFadeLevel;
        envelope.dwFadeTime = envelopeFadeTime;

        periodic.dwMagnitude = periodicMagnitude;
        periodic.lOffset = periodicOffset;
        periodic.dwPhase = periodicPhase;
        periodic.dwPeriod = periodicPeriod;

        effect.dwSize = effect.size();
        effect.dwFlags = flags;
        effect.dwDuration = duration;
        effect.dwSamplePeriod = samplePeriod;
        effect.dwGain = gain;
        effect.dwTriggerButton = triggerButton;
        effect.dwTriggerRepeatInterval = triggerRepeatInterval;
        effect.cAxes = numAxes;
        effect.rgdwAxes = axisIdsDword;
        effect.rglDirection = directionsLong;
        effect.lpEnvelope = envelope;
        effect.cbTypeSpecificParams = periodic.size();
        effect.lpvTypeSpecificParams = periodic;
        effect.dwStartDelay = startDelay;

        PointerByReference pEffect = new PointerByReference();
        int /* HRESULT */ res = lpDevice.CreateEffect.apply(effectGuid, effect, pEffect, null);
        if (res != DI_OK) {
            throw new IOException(String.format("Failed to create effect (0x%x)", res));
        }
        return pEffect.getValue();
    }

    private DIDeviceObject lookupObjectByGUID(int guidId) {
        for (DIDeviceObject object : objects) {
            if (guidId == object.getGUIDType())
                return object;
        }
        return null;
    }

    public int getPollData(DIDeviceObject object) {
        return deviceState[object.getFormatOffset()];
    }

    public DIDeviceObject mapEvent(DIDeviceObjectData event) {
        // Raw event format offsets (dwOfs member) is in bytes,
        // but we're indexing into ints so we have to compensate
        // for the int size (4 bytes)
        int formatOffset = event.getFormatOffset() / 4;
        return objects.get(formatOffset);
    }

    public DIComponent mapObject(DIDeviceObject object) {
        return objectToComponent.get(object);
    }

    public void registerComponent(DIDeviceObject object, DIComponent component) {
        objectToComponent.put(object, component);
    }

    public synchronized void pollAll() throws IOException {
        checkReleased();
        poll();
        getDeviceState(deviceState);
        queue.compact();
        getDeviceData(queue);
        queue.flip();
    }

    public synchronized boolean getNextEvent(DIDeviceObjectData data) {
        DIDeviceObjectData nextEvent = queue.get();
        if (nextEvent == null)
            return false;
        data.set(nextEvent);
        return true;
    }

    private void poll() throws IOException {
        int res = nPoll(address);
        if (res != DI_OK && res != DI_NOEFFECT) {
            if (res == DIERR_NOTACQUIRED) {
                acquire();
                return;
            }
            throw new IOException("Failed to poll device (" + Integer.toHexString(res) + ")");
        }
    }

    private static int nPoll(Pointer address) throws IOException {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        int /* HRESULT */ res = directInputDevice8.Poll.apply();
        return res;
    }

    private void acquire() throws IOException {
        int res = nAcquire(address);
        if (res != DI_OK && res != DIERR_OTHERAPPHASPRIO && res != DI_NOEFFECT)
            throw new IOException("Failed to acquire device (" + Integer.toHexString(res) + ")");
    }

    private static int nAcquire(Pointer address) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        int /* HRESULT */ res = directInputDevice8.Acquire.apply();
        return res;
    }

    private void unacquire() throws IOException {
        int res = nUnacquire(address);
        if (res != DI_OK && res != DI_NOEFFECT)
            throw new IOException("Failed to unAcquire device (" + Integer.toHexString(res) + ")");
    }

    private static int nUnacquire(Pointer address) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        int /* HRESULT */ res = directInputDevice8.Unacquire.apply();
        return res;
    }

    private boolean getDeviceData(DataQueue<DIDeviceObjectData> queue) throws IOException {
        int res = nGetDeviceData(address, 0, queue, queue.getElements(), queue.position(), queue.remaining());
        if (res != DI_OK && res != DI_BUFFEROVERFLOW) {
            if (res == DIERR_NOTACQUIRED) {
                acquire();
                return false;
            }
            throw new IOException("Failed to get device data (" + Integer.toHexString(res) + ")");
        }
        return true;
    }

    private static int nGetDeviceData(Pointer address, int flags, DataQueue<DIDeviceObjectData> queue, DIDeviceObjectData[] queueArray, int position, int remaining) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        IntByReference numEvents = new IntByReference(remaining);
        DIDeviceObjectData[] data = new DIDeviceObjectData[numEvents.getValue()];

        int /* HRESULT */ res = directInputDevice8.GetDeviceData.apply(data[0].size(), data, numEvents, flags);
        if (res == DI_OK || res == DI_BUFFEROVERFLOW) {
            for (int i = 0; i < numEvents.getValue(); i++) {
                queueArray[position + i].set(data[i].dwOfs, data[i].dwData, data[i].dwTimeStamp, data[i].dwSequence);
            }
            queue.position(position + numEvents.getValue());
        }
        return res;
    }

    private void getDeviceState(int[] deviceState) throws IOException {
        int res = nGetDeviceState(address, deviceState);
        if (res != DI_OK) {
            if (res == DIERR_NOTACQUIRED) {
                Arrays.fill(deviceState, 0);
                acquire();
                return;
            }
            throw new IOException("Failed to get device state (" + Integer.toHexString(res) + ")");
        }
    }

    private static int nGetDeviceState(Pointer address, int[] deviceState) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);
        int stateLength = deviceState.length;
        ByteBuffer bb = ByteBuffer.allocate(stateLength * Integer.BYTES);

        int /* HRESULT */ res = directInputDevice8.GetDeviceState.apply(bb.capacity(), bb);
        bb.asIntBuffer().get(deviceState);
        return res;
    }

    /**
     * Set a custom data format that maps each object's data into an int[]
     * array with the same index as in the objects List
     */
    private void setDataFormat(int flags) throws IOException {
        DIDeviceObject[] deviceObjects = new DIDeviceObject[objects.size()];
        objects.toArray(deviceObjects);
        int res = nSetDataFormat(address, flags, deviceObjects);
        if (res != DI_OK)
            throw new IOException("Failed to set data format (" + Integer.toHexString(res) + ")");
    }

    private static int nSetDataFormat(Pointer address, int flags, DIDeviceObject[] objects) throws IOException {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);
        int numObjects = objects.length;
        // Data size must be a multiple of 4, but since sizeof(int) is
        // 4, we're safe
        int dataSize = numObjects * Integer.BYTES;

        DIDATAFORMAT dataFormat = new DIDATAFORMAT();
        dataFormat.dwSize = dataFormat.size();
        dataFormat.dwObjSize = 20; // sizeof(DIOBJECTDATAFORMAT)
        dataFormat.dwFlags = flags;
        dataFormat.dwDataSize = dataSize;
        dataFormat.dwNumObjs = numObjects;

        GUID[] guids = new GUID[numObjects];
        DIOBJECTDATAFORMAT[] objectFormats = new DIOBJECTDATAFORMAT[numObjects];
        for (int i = 0; i < numObjects; i++) {
            DIDeviceObject object = objects[i];
            byte[] guidArray = object.getGUID();
            NativeUtil.unwrapGUID(guidArray, guids[i]);
            int type = object.getType();
            int objectFlags = object.getFlags();
            int instance = object.getInstance();
            int compositeType = type | ((instance << 8) & 0xff00); // DIDFT_MAKEINSTANCE
            int flagsMasked = flags & (DIDOI_ASPECTACCEL | DIDOI_ASPECTFORCE | DIDOI_ASPECTPOSITION | DIDOI_ASPECTVELOCITY);
            DIOBJECTDATAFORMAT objectFormat = objectFormats[i];
            objectFormat.pguid = guids[i];
            objectFormat.dwType = compositeType;
            objectFormat.dwFlags = flagsMasked;
            // dwOfs must be multiple of 4, but sizeof(int) is 4, so we're safe
            objectFormat.dwOfs = i*Integer.BYTES;
        }
        dataFormat.rgodf = objectFormats;
        int /* HRESULT */ res = directInputDevice8.SetDataFormat.apply(dataFormat);
        return res;
    }

    public String getProductName() {
        return productName;
    }

    public int getType() {
        return devType;
    }

    public List<DIDeviceObject> getObjects() {
        return objects;
    }

    private void enumEffects() throws IOException {
        int res = nEnumEffects(address, DIEFT_ALL);
        if (res != DI_OK)
            throw new IOException("Failed to enumerate effects (" + Integer.toHexString(res) + ")");
    }

    private int nEnumEffects(Pointer address, int flags) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        EnumContext enumContext = new EnumContext();
        enumContext.id = EnumContext.idMaster++;
        EnumContext.map.put(enumContext.id, this);

        int /* HRESULT */ res = directInputDevice8.EnumEffects.apply(IDirectInputDevice::enumEffectsCallback, enumContext.getPointer(), flags);
        return res;
    }

    /* Called from native side from nEnumEffects */
    private void addEffect(byte[] guid, int guidId, int effectType, int staticParams, int dynamicParams, String name) {
        effects.add(new DIEffectInfo(guid, guidId, effectType, staticParams, dynamicParams, name));
    }

    private void enumObjects() throws IOException {
        int res = nEnumObjects(address, DIDFT_BUTTON | DIDFT_AXIS | DIDFT_POV);
        if (res != DI_OK)
            throw new IOException("Failed to enumerate objects (" + Integer.toHexString(res) + ")");
    }

    private int nEnumObjects(Pointer address, int flags) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        EnumContext enumContext = new EnumContext();
        enumContext.id = EnumContext.idMaster++;
        EnumContext.map.put(enumContext.id, this);

        int /* HRESULT */ res = directInputDevice8.EnumObjects.apply(IDirectInputDevice::enumObjectsCallback, enumContext.getPointer(), flags);
        return res;
    }

    public synchronized long[] getRangeProperty(int objectIdentifier) {
        checkReleased();
        long[] range = new long[2];
        int res = nGetRangeProperty(address, objectIdentifier, range);
        if (res != DI_OK)
            throw new NoSuchElementException("Failed to get object range (" + res + ")");
        return range;
    }

    private static int nGetRangeProperty(Pointer address, int objectId, long[] rangeArray) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        DIPROPRANGE range = new DIPROPRANGE();
        range.diph.dwSize = range.size();
        range.diph.dwHeaderSize = 16; // sizeof(DIPROPHEADER)
        range.diph.dwObj = objectId;
        range.diph.dwHow = DIPH_BYID;

        int /* HRESULT */ res = directInputDevice8.GetProperty.apply(new GUID.ByValue(DIPROP_RANGE), range.diph);
        rangeArray[0] = range.lMin;
        rangeArray[1] = range.lMax;
        return res;
    }

    public synchronized int getDeadzoneProperty(int objectIdentifier) {
        checkReleased();
        return nGetDeadzoneProperty(address, objectIdentifier);
    }

    private static int nGetDeadzoneProperty(Pointer address, int objectId) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        WinAPI.DIPROPDWORD deadzone = new WinAPI.DIPROPDWORD();
        deadzone.diph.dwSize = deadzone.size();
        deadzone.diph.dwHeaderSize = 16; // sizeof(DIPROPHEADER)
        deadzone.diph.dwObj = objectId;
        deadzone.diph.dwHow = DIPH_BYID;

        int /* HRESULT */ res = directInputDevice8.GetProperty.apply(new GUID.ByValue(DIPROP_DEADZONE), deadzone.diph);
        if (res != DI_OK && res != S_FALSE)
            throw new NoSuchElementException(String.format("Failed to get deadzone property (%x)", res));
        return deadzone.dwData;
    }

    /** Called from native side from nEnumObjects */
    private void addObject(byte[] guid, int guidType, int identifier, int type, int instance, int flags, String name) {
        Component.Identifier id = getIdentifier(guidType, type, instance);
        int formatOffset = currentFormatOffset++;
        DIDeviceObject obj = new DIDeviceObject(this, id, guid, guidType, identifier, type, instance, flags, name, formatOffset);
        objects.add(obj);
    }

    private static Component.Identifier.Key getKeyIdentifier(int keyInstance) {
        return DIIdentifierMap.getKeyIdentifier(keyInstance);
    }

    private Component.Identifier.Button getNextButtonIdentifier() {
        int buttonId = buttonCounter++;
        return DIIdentifierMap.getButtonIdentifier(buttonId);
    }

    private Component.Identifier getIdentifier(int guidType, int type, int instance) {
        return switch (guidType) {
            case GUID_XAxis -> Component.Identifier.Axis.X;
            case GUID_YAxis -> Component.Identifier.Axis.Y;
            case GUID_ZAxis -> Component.Identifier.Axis.Z;
            case GUID_RxAxis -> Component.Identifier.Axis.RX;
            case GUID_RyAxis -> Component.Identifier.Axis.RY;
            case GUID_RzAxis -> Component.Identifier.Axis.RZ;
            case GUID_Slider -> Component.Identifier.Axis.SLIDER;
            case GUID_POV -> Component.Identifier.Axis.POV;
            case GUID_Key -> getKeyIdentifier(instance);
            case GUID_Button -> getNextButtonIdentifier();
            default -> Component.Identifier.Axis.UNKNOWN;
        };
    }

    public synchronized void setBufferSize(int size) throws IOException {
        checkReleased();
        unacquire();
        int res = nSetBufferSize(address, size);
        if (res != DI_OK && res != DI_PROPNOEFFECT && res != DI_POLLEDDEVICE)
            throw new IOException("Failed to set buffer size (" + Integer.toHexString(res) + ")");
        queue = new DataQueue<>(size, DIDeviceObjectData.class);
        queue.position(queue.limit());
        acquire();
    }

    private static int nSetBufferSize(Pointer address, int size) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        DIPROPDWORD dipropdw = new DIPROPDWORD();
        dipropdw.diph.dwSize = dipropdw.size();
        dipropdw.diph.dwHeaderSize = 16; // sizeof(DIPROPHEADER)
        dipropdw.diph.dwObj = 0;
        dipropdw.diph.dwHow = DIPH_DEVICE;
        dipropdw.dwData = size;

        int /* HRESULT */ res = directInputDevice8.SetProperty.apply(new GUID.ByValue(DIPROP_BUFFERSIZE), dipropdw.diph);
        return res;
    }

    public synchronized void setCooperativeLevel(int flags) throws IOException {
        checkReleased();
        int res = nSetCooperativeLevel(address, window.getHwnd(), flags);
        if (res != DI_OK)
            throw new IOException("Failed to set cooperative level (" + Integer.toHexString(res) + ")");
    }

    private static int nSetCooperativeLevel(Pointer address, HWND hwnd, int flags) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        int /* HRESULT */ res = directInputDevice8.SetCooperativeLevel.apply(hwnd, flags);
        return res;
    }

    public synchronized void release() {
        if (!released) {
            released = true;
            for (Rumbler rumbler : rumblers) {
                IDirectInputEffect effect = (IDirectInputEffect) rumbler;
                effect.release();
            }
            nRelease(address);
        }
    }

    private static void nRelease(Pointer address) {
        IDirectInputDevice8 directInputDevice8 = new IDirectInputDevice8(address);

        directInputDevice8.Release.apply(address);
    }

    private void checkReleased() {
        if (released)
            throw new IllegalStateException("Device is released");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() {
        release();
    }
}
