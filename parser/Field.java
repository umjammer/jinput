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
public final class Field {

	Report report;
	Collection collection;
	int physical;
	int logical;
	int application;
	int usage;
	int flags;
	int reportOffset;
	int reportSize;
	int reportType;
	int logicalMinimum;
	int logicalMaximum;
	int physicalMinimum;
	int physicalMaximum;
	int unitExponent;
	int unit;

	Field(Collection collection) {
		this.collection = collection;
		collection.add(this);
	}

	//	int index;
	public void dump(PrintStream out, String tab) {
		out.printf(tab + "-FIELD-------------------------\n");
		out.printf(tab + "        usage: 0x%04X:0x%04X\n", (0xFFFF & usage >> 16) & 0xFFFF, usage & 0xFFFF);
		out.printf(tab + "        flags: 0x%08X\n", flags);
		out.printf(tab + "    report id: 0x%02X\n", report.id);
		out.printf(tab + "         type: %s\n", new String[] { "input", "output", "feature" }[report.type]);
		out.printf(tab + "       offset: %d\n", reportOffset);
		out.printf(tab + "         size: %d\n", reportSize);
		out.printf(tab + "  logical min: %d\n", logicalMinimum);
		out.printf(tab + "  logical max: %d\n", logicalMaximum);
		out.printf(tab + " physical min: %d\n", physicalMinimum);
		out.printf(tab + " physical max: %d\n", physicalMaximum);
		out.printf(tab + "         unit: %d\n", unit);
		out.printf(tab + "     unit exp: %d\n", unitExponent);
	}
}