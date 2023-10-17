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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * struct input_absinfo
 *
 * @author elias
 */
class LinuxAbsInfo extends Structure {

    public int value;
    public int minimum;
    public int maximum;
    public int fuzz;
    public int flat;

    public LinuxAbsInfo() {
    }

    public LinuxAbsInfo(Pointer p) {
        super(p);
    }

    public static class ByReference extends LinuxAbsInfo implements Structure.ByReference {

    }

    public static class ByValue extends LinuxAbsInfo implements Structure.ByValue {

    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("value", "minimum", "maximum", "fuzz", "flat");
    }

    public final void set(int value, int min, int max, int fuzz, int flat) {
        this.value = value;
        this.minimum = min;
        this.maximum = max;
        this.fuzz = fuzz;
        this.flat = flat;
    }

    public final String toString() {
        return "AbsInfo: value = " + value + " | min = " + minimum + " | max = " + maximum + " | fuzz = " + fuzz + " | flat = " + flat;
    }

    public final int getValue() {
        return value;
    }

    final int getMax() {
        return maximum;
    }

    final int getMin() {
        return minimum;
    }

    final int getFlat() {
        return flat;
    }

    final int getFuzz() {
        return fuzz;
    }
}
