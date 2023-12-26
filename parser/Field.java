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
    /** unit depends on BUFFERED_BYTE of flags [bytes/bits] */
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

    public int getUsagePage() {
        return (usage >> 16) & 0xffff;
    }

    public int getUsageId() {
        return usage & 0xffff;
    }

    public int getFeature() {
        return flags;
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
        this.mask = createMask(startBit, reportSize);
    }

    /** for plugin TODO adhoc */
    public Field(int offset, int size) {
        this.reportOffset = offset;
        this.reportSize = size;

        init();
    }

    /** */
    static int createMask(int s, int l) {
        int m = l % 8 != 0 ? l % 8 : 8;

        int result = 0;
        for (int i = s; i < s + m; i++) {
            result += (1 << i);
Debug.printf(Level.FINER, "%02x, %02x, %s", result, 1 << i, StringUtil.toBits(1 << i));
        }
        return result;
    }

    /** view */
    static String toBit(int s, int l) {
        int m = l % 8 != 0 ? l % 8 : 8;
        return "_".repeat(s) + "*".repeat(m) + "_".repeat(8 - s - m);
    }

    //	int index;
    void dump(PrintStream out, String tab) {
        UsagePage usagePage = UsagePage.map(getUsagePage());
        out.printf(tab + "-FIELD-------------------------%n");
        out.printf(tab + "        usage: 0x%04X:0x%04X %s:%s%n", getUsagePage(), getUsageId(), usagePage == null ? "" : usagePage, usagePage == null ? "" : usagePage.mapUsage(getUsageId()));
        out.printf(tab + "        flags: %s\n", Feature.asString(Feature.valueOf(flags)));
        out.printf(tab + "    report id: 0x%02X\n", report.id);
        out.printf(tab + "         type: %s\n", new String[] {"input", "output", "feature"}[report.type]);
        out.printf(tab + "       offset: %d byte%s (%d)\n", offsetByte, startBit != 0 ? String.format(" and %d bit", startBit) : "", reportOffset);
        out.printf(tab + "         size: %d: %s\n", reportSize, Feature.containsIn(BUFFERED_BYTE, flags) ? reportSize + " bytes" : toBit(startBit, reportSize));
        out.printf(tab + "  logical min: %d\n", logicalMinimum);
        out.printf(tab + "  logical max: %d\n", logicalMaximum);
        out.printf(tab + " physical min: %d\n", physicalMinimum);
        out.printf(tab + " physical max: %d\n", physicalMaximum);
        out.printf(tab + "         unit: %d\n", unit);
        out.printf(tab + "     unit exp: %d\n", unitExponent);
    }

    /** utility */
    public int getValue(byte[] data) {
        // TODO sign, bit/byte, bit size > 8
Debug.printf(Level.FINER, "masked: 0x%02x, %s, moved: 0x%02x, %s", data[offsetByte] & mask, StringUtil.toBits(data[offsetByte] & mask), (data[offsetByte] & mask) >> startBit, StringUtil.toBits((data[offsetByte] & mask) >> startBit));
        return (data[offsetByte] & mask) >> startBit;
    }

    /** utility */
    public void setValue(byte[] data, byte v) {
        data[offsetByte] = (byte) ((data[offsetByte] & ~mask) | ((v << startBit) & mask));
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dump(new PrintStream(baos), "");
        return baos.toString();
    }
}
