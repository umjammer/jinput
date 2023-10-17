/*
 * TinyUmbrella - making iDevice restores possible...
 * Copyright (C) 2009-2010 semaphore
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package vavix.rococoa.corefoundation;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;


/**
 * Brought to you by:
 *
 * @author semaphore
 * @version May 27, 2010 1:07:16 AM
 */
public class CFNumber extends CFType {

    public CFNumber(Pointer address) {
        super(address);
    }

    public CFNumber() {
    }

    public int getInt() {
        IntByReference ibr = new IntByReference();
        if (CFLib.INSTANCE.CFNumberGetValue(this, CFLib.CFNumberType.kCFNumberSInt64Type, ibr))
            return ibr.getValue();
        return -1;
    }

    public long getLong() {
        LongByReference lbr = new LongByReference();
        if (CFLib.INSTANCE.CFNumberGetValue(this, CFLib.CFNumberType.kCFNumberSInt64Type, lbr))
            return lbr.getValue();
        return -1;
    }
}
