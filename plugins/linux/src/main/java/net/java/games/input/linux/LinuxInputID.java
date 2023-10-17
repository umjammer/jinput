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
import net.java.games.input.Controller;


/**
 * struct input_id
 *
 * @author elias
 */
class LinuxInputID extends Structure {

    public int bustype;
    public int vendor;
    public int product;
    public int version;

    public LinuxInputID() {
    }

    public LinuxInputID(Pointer p) {
        super(p);
    }

    public static class ByReference extends LinuxInputID implements Structure.ByReference {

    }

    public static class ByValue extends LinuxInputID implements Structure.ByValue {

    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("bustype", "vendor", "product", "version");
    }

    public Controller.PortType getPortType() {
        return LinuxNativeTypesMap.getPortType(bustype);
    }

    public String toString() {
        return "LinuxInputID: bustype = 0x" + Integer.toHexString(bustype) + " | vendor = 0x" + Integer.toHexString(vendor) +
                " | product = 0x" + Integer.toHexString(product) + " | version = 0x" + Integer.toHexString(version);
    }
}
