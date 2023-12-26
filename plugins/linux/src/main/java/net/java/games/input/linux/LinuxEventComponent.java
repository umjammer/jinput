/**
 * Copyright (C) 2003 Jeremy Booth (jeremy@newdawnsoftware.com)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

package net.java.games.input.linux;

import java.io.IOException;

import net.java.games.input.Component;
import net.java.games.input.Controller;


/**
 * @author elias
 * @author Jeremy Booth (jeremy@newdawnsoftware.com)
 */
final class LinuxEventComponent {

    private final LinuxEventDevice device;
    private final Component.Identifier identifier;
    private final Controller.Type buttonTrait;
    private final boolean isRelative;
    private final LinuxAxisDescriptor descriptor;
    private final int min;
    private final int max;
    private final int flat;

    final LinuxAbsInfo absInfo = new LinuxAbsInfo();

    public LinuxEventComponent(LinuxEventDevice device, Component.Identifier identifier, boolean isRelative, int nativeType, int nativeCode) throws IOException {
        this.device = device;
        this.identifier = identifier;
        if (nativeType == NativeDefinitions.EV_KEY)
            this.buttonTrait = LinuxNativeTypesMap.guessButtonTrait(nativeCode);
        else
            this.buttonTrait = Controller.Type.UNKNOWN;
        this.isRelative = isRelative;
        this.descriptor = new LinuxAxisDescriptor();
        descriptor.set(nativeType, nativeCode);
        if (nativeType == NativeDefinitions.EV_ABS) {
            LinuxAbsInfo.ByReference absInfo = new LinuxAbsInfo.ByReference();
            getAbsInfo(absInfo);
            this.min = absInfo.getMin();
            this.max = absInfo.getMax();
            this.flat = absInfo.getFlat();
        } else {
            this.min = Integer.MIN_VALUE;
            this.max = Integer.MAX_VALUE;
            this.flat = 0;
        }
    }

    public LinuxEventDevice getDevice() {
        return device;
    }

    public void getAbsInfo(LinuxAbsInfo absInfo) throws IOException {
        assert descriptor.getType() == NativeDefinitions.EV_ABS;
        device.getAbsInfo(descriptor.getCode(), absInfo);
    }

    public Controller.Type getButtonTrait() {
        return buttonTrait;
    }

    public Component.Identifier getIdentifier() {
        return identifier;
    }

    public LinuxAxisDescriptor getDescriptor() {
        return descriptor;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public boolean isAnalog() {
        return identifier instanceof Component.Identifier.Axis && identifier != Component.Identifier.Axis.POV;
    }

    float convertValue(float value) {
        if (identifier instanceof Component.Identifier.Axis && !isRelative) {
            // Some axes have min = max = 0
            if (min == max)
                return 0;
            if (value > max)
                value = max;
            else if (value < min)
                value = min;
            return 2 * (value - min) / (max - min) - 1;
        } else {
            return value;
        }
    }

    float getDeadZone() {
        return flat / (2f * (max - min));
    }
}
