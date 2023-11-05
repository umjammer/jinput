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


import java.io.PrintStream;


/**
 * @see "https://github.com/nyholku/purejavahidapi"
 */
public final class Report {

    int id;
    int type;
    Collection collection;
    Field[] fields = new Field[HidParser.HID_MAX_FIELDS];
    int maxField;
    int size;

    Report(int type, int id, Collection collection) {
        this.type = type;
        this.id = id;
        this.collection = collection;
    }

    void dump(PrintStream out, String tab) {
        HidParser.out.printf(tab + "REPORT-------------------------\n");
        HidParser.out.printf(tab + "         type: %s\n", new String[] {"input", "output", "feature"}[type]);
        HidParser.out.printf(tab + "           id: 0x%02X\n", id);
        HidParser.out.printf(tab + "         size: %d\n", size);
        for (int i = 0; i < maxField; i++) {
            fields[i].dump(out, tab + "   ");
        }
        HidParser.out.printf(tab + "-------------------------------\n");
    }
}