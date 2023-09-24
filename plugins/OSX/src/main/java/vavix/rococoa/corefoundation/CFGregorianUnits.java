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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;


/**
 * Brought to you by:
 * @author semaphore
 * @version May 27, 2010 1:44:34 PM
 */
public class CFGregorianUnits extends Structure {

    public int years;
    public int months;
    public int days;
    public int hours;
    public int minutes;
    public double seconds;

    public CFGregorianUnits() {
    }

    public CFGregorianUnits(int years, int months, int days, int hours, int minutes, double seconds) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    protected ByReference newByReference() {
        ByReference s = new ByReference();
        s.useMemory(getPointer());
        write();
        s.read();
        return s;
    }

    protected ByValue newByValue() {
        ByValue s = new ByValue();
        s.useMemory(getPointer());
        write();
        s.read();
        return s;
    }

    protected CFGregorianUnits newInstance() {
        CFGregorianUnits s = new CFGregorianUnits();
        s.useMemory(getPointer());
        write();
        s.read();
        return s;
    }

    public static class ByReference extends CFGregorianUnits implements Structure.ByReference {
    }

    public static class ByValue extends CFGregorianUnits implements Structure.ByValue {
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("years", "months", "days", "hours", "minutes", "seconds");
    }
}
