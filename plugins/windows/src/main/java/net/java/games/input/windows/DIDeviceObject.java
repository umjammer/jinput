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

import net.java.games.input.Component;


/**
 * Java wrapper for DIDEVICEOBJECTINSTANCE
 *
 * @author elias
 * @version 1.0
 */
final class DIDeviceObject {

    // DirectInput scales wheel deltas by 120
    private final static int WHEEL_SCALE = 120;

    private final IDirectInputDevice device;
    private final byte[] guid;
    private final int identifier;
    private final int type;
    private final int instance;
    private final int guidType;
    private final int flags;
    private final String name;
    private final Component.Identifier id;
    private final int formatOffset;
    private final int min;
    private final int max;
    private final int deadzone;

    // These are used for emulating relative axes
    private int lastPollValue;
    private int lastEventValue;

    public DIDeviceObject(IDirectInputDevice device, Component.Identifier id, byte[] guid, int guidType, int identifier, int type, int instance, int flags, String name, int formatOffset) {
        this.device = device;
        this.id = id;
        this.guid = guid;
        this.identifier = identifier;
        this.type = type;
        this.instance = instance;
        this.guidType = guidType;
        this.flags = flags;
        this.name = name;
        this.formatOffset = formatOffset;
        if (isAxis() && !isRelative()) {
            long[] range = device.getRangeProperty(identifier);
            this.min = (int) range[0];
            this.max = (int) range[1];
            this.deadzone = device.getDeadzoneProperty(identifier);
        } else {
            this.min = IDirectInputDevice.DIPROPRANGE_NOMIN;
            this.max = IDirectInputDevice.DIPROPRANGE_NOMAX;
            this.deadzone = 0;
        }
    }

    public synchronized int getRelativePollValue(int currentAbsValue) {
        if (device.areAxesRelative())
            return currentAbsValue;
        int relValue = currentAbsValue - lastPollValue;
        lastPollValue = currentAbsValue;
        return relValue;
    }

    public synchronized int getRelativeEventValue(int currentAbsValue) {
        if (device.areAxesRelative())
            return currentAbsValue;
        int relValue = currentAbsValue - lastEventValue;
        lastEventValue = currentAbsValue;
        return relValue;
    }

    public int getGUIDType() {
        return guidType;
    }

    public int getFormatOffset() {
        return formatOffset;
    }

    public IDirectInputDevice getDevice() {
        return device;
    }

    public int getDIIdentifier() {
        return identifier;
    }

    public Component.Identifier getIdentifier() {
        return id;
    }

    public String getTszName() {
        return name;
    }

    public int getInstance() {
        return instance;
    }

    public int getType() {
        return type;
    }

    public byte[] getGUID() {
        return guid;
    }

    public int getFlags() {
        return flags;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public float getDeadzone() {
        return deadzone;
    }

    public boolean isButton() {
        return (type & IDirectInputDevice.DIDFT_BUTTON) != 0;
    }

    public boolean isAxis() {
        return (type & IDirectInputDevice.DIDFT_AXIS) != 0;
    }

    public boolean isRelative() {
        return isAxis() && (type & IDirectInputDevice.DIDFT_RELAXIS) != 0;
    }

    public boolean isAnalog() {
        return isAxis() && id != Component.Identifier.Axis.POV;
    }

    public float convertValue(float value) {
        if (getDevice().getType() == IDirectInputDevice.DI8DEVTYPE_MOUSE && id == Component.Identifier.Axis.Z) {
            return value / WHEEL_SCALE;
        } else if (isButton()) {
            return (((int) value) & 0x80) != 0 ? 1 : 0;
        } else if (id == Component.Identifier.Axis.POV) {
            int intValue = (int) value;
            if ((intValue & 0xFFFF) == 0xFFFF)
                return Component.POV.OFF;
            // DirectInput returns POV directions in hundredths of degree clockwise from north
            int slice = 360 * 100 / 16;
            if (intValue >= 0 && intValue < slice)
                return Component.POV.UP;
            else if (intValue < 3 * slice)
                return Component.POV.UP_RIGHT;
            else if (intValue < 5 * slice)
                return Component.POV.RIGHT;
            else if (intValue < 7 * slice)
                return Component.POV.DOWN_RIGHT;
            else if (intValue < 9 * slice)
                return Component.POV.DOWN;
            else if (intValue < 11 * slice)
                return Component.POV.DOWN_LEFT;
            else if (intValue < 13 * slice)
                return Component.POV.LEFT;
            else if (intValue < 15 * slice)
                return Component.POV.UP_LEFT;
            else
                return Component.POV.UP;
        } else if (isAxis() && !isRelative()) {
            return 2 * (value - min) / (float) (max - min) - 1;
        } else
            return value;
    }
}
