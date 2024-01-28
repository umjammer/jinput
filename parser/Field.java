/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its
 * contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package vavi.hid.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

import net.java.games.input.usb.UsagePage;
import vavi.hid.parser.HidParser.Feature;
import vavi.util.Debug;
import vavi.util.StringUtil;

import static vavi.hid.parser.HidParser.Feature.BUFFERED_BYTE;


/**
 * Represents one field in a hid device descriptor.
 * <p>
 * TODO hid value is little endian???
 *
 * @see "https://github.com/nyholku/purejavahidapi"
 */
public final class Field {

    Report report;
    Collection collection;
    int physical;
    int logical;
    int application;
    int usage;
    /** @see Feature */
    int flags;
    /** bits (first one byte (report id) is excluded) TODO really ??? */
    int reportOffset;
    /** unit depends on BUFFERED_BYTE of flags [bytes/bits] */
    int reportSize;
    int reportType;
    int logicalMinimum;
    int logicalMaximum;
    int physicalMinimum;
    int physicalMaximum;
    int unitExponent;
    int unit;

    int mask;
    int offsetByte;
    int startBit;
    /** data size considered startBits shift */
    int dataBytes;

    public int getUsagePage() {
        return (usage >> 16) & 0xffff;
    }

    public int getUsageId() {
        return usage & 0xffff;
    }

    public int getFeature() {
        return flags;
    }

    public int getLogicalMinimum() {
        return logicalMinimum;
    }

    public int getLogicalMaximum() {
        return logicalMaximum;
    }

    public int getPhysicalMinimum() {
        return physicalMinimum;
    }

    public int getPhysicalMaximum() {
        return physicalMaximum;
    }

    /** for parser */
    Field(Collection collection) {
        this.collection = collection;
        collection.add(this);
    }

    /** not good way (for performance) */
    void init() {
        this.offsetByte = reportOffset / 8;
        this.startBit = reportOffset % 8;
        this.mask = createMask();
        this.dataBytes = isBytes() ? reportSize : (reportSize + startBit + 7) / 8;
        if (dataBytes > 8) {
            throw new IllegalArgumentException(String.format("bad descriptor: isBytes: %s, reportSize: %d, startBit: %d", isBytes(), reportSize, startBit));
        }
    }

    /**
     * for plugin TODO adhoc
     * @param offset in bits (must be excluded first one byte (8 bits) for report id)
     * @param size in bits
     */
    public Field(int offset, int size) {
        this.reportOffset = offset;
        this.reportSize = size;

        init();
    }

    /** */
    boolean isBytes() {
        return Feature.containsIn(BUFFERED_BYTE, flags);
    }

    /** */
    int createMask() {
        String x = new StringBuilder(toBit()).reverse().toString(); // MSB <- LSB
Debug.println(Level.FINER, x);
        return Integer.parseInt(x, 2);
    }

    /** LSB -> MSB */
    String toBit() {
        return toBit("1", "0");
    }

    /** view LSB -> MSB */
    String toBit(String on, String off) {
        int bits;
        if (isBytes()) {
            bits = reportSize * 8;
        } else {
            bits = reportSize;
        }
        return  off.repeat(startBit) +
                on.repeat(bits) +
                off.repeat((startBit + bits) % 8 == 0 ? 0 : 8 - (startBit + bits) % 8);
    }

    // int index;
    void dump(PrintStream out, String tab) {
        UsagePage usagePage = UsagePage.map(getUsagePage());
        out.printf(tab + "-FIELD-------------------------%n");
        out.printf(tab + "        usage: 0x%04X:0x%04X %s:%s%n", getUsagePage(), getUsageId(), usagePage == null ? "" : usagePage, usagePage == null ? "" : usagePage.mapUsage(getUsageId()));
        out.printf(tab + "        flags: %s\n", Feature.asString(Feature.valueOf(flags)));
        out.printf(tab + "    report id: 0x%02X\n", report.id);
        out.printf(tab + "         type: %s\n", new String[] {"input", "output", "feature"}[report.type]);
        out.printf(tab + "       offset: %d byte%s (%d)\n", offsetByte, startBit != 0 ? String.format(" and %d bit", startBit) : "", reportOffset);
        out.printf(tab + "         size: %d: %s\n", reportSize, isBytes() ? reportSize + " bytes" : toBit("*", "_"));
        out.printf(tab + "  logical min: %d\n", logicalMinimum);
        out.printf(tab + "  logical max: %d\n", logicalMaximum);
        out.printf(tab + " physical min: %d\n", physicalMinimum);
        out.printf(tab + " physical max: %d\n", physicalMaximum);
        out.printf(tab + "         unit: %d\n", unit);
        out.printf(tab + "     unit exp: %d\n", unitExponent);
    }

    /** TODO when length + startBits > 64bit */
    private int getValueInternal(byte[] data) {
        int value = 0;

        int p = offsetByte + 1; // + 1 for the report id at the first byte

        switch (dataBytes) {
        case 8: value |= (data[p + 7]) << 56; // fall-through
        case 7: value |= (data[p + 6]) << 48; // fall-through
        case 6: value |= (data[p + 5]) << 40; // fall-through
        case 5: value |= (data[p + 4]) << 32; // fall-through
        case 4: value |= (data[p + 3]) << 24; // fall-through
        case 3: value |= (data[p + 2]) << 16; // fall-through
        case 2: value |= (data[p + 1]) << 8;  // fall-through
        case 1: value |=  data[p + 0];
        }

        return value;
    }

    /** TODO when length + startBits > 64bit */
    private void setValueInternal(byte[] data, int value) {
        int p = offsetByte + 1; // + 1 for the report id at the first byte

        switch (dataBytes) {
        case 8: data[p + 7] = (byte) ((value >> 56) & 0xff); // fall-through
        case 7: data[p + 6] = (byte) ((value >> 48) & 0xff); // fall-through
        case 6: data[p + 5] = (byte) ((value >> 40) & 0xff); // fall-through
        case 5: data[p + 4] = (byte) ((value >> 32) & 0xff); // fall-through
        case 4: data[p + 3] = (byte) ((value >> 24) & 0xff); // fall-through
        case 3: data[p + 2] = (byte) ((value >> 16) & 0xff); // fall-through
        case 2: data[p + 1] = (byte) ((value >> 8 ) & 0xff); // fall-through
        case 1: data[p + 0] = (byte) ( value        & 0xff); // fall-through
        }
    }

    /** utility */
    public int getValue(byte[] data) {
Debug.printf(Level.FINER, "masked: 0x%02x, %s, moved: 0x%02x, %s", getValueInternal(data) & mask, StringUtil.toBits(getValueInternal(data) & mask), (getValueInternal(data) & mask) >> startBit, StringUtil.toBits((getValueInternal(data) & mask) >> startBit));
        return (getValueInternal(data) & mask) >> startBit;
    }

    /** utility */
    public void setValue(byte[] data, int v) {
        setValueInternal(data, ((getValueInternal(data) & ~mask) | ((v << startBit) & mask)));
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dump(new PrintStream(baos), "");
        return baos.toString();
    }

    /** */
    public String getDump(byte[] data) {
        return StringUtil.getDump(data, offsetByte + 1, dataBytes);
    }
}
