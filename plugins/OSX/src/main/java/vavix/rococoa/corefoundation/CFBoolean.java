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


/**
 * Brought to you by:
 * @author semaphore
 * @version May 27, 2010 1:07:37 AM
 */
public class CFBoolean extends CFType {

    public CFBoolean(Pointer address) {
        super(address);
    }

    public CFBoolean() {
        super();
    }

    public boolean getValue() {
        return CFLib.INSTANCE.CFBooleanGetValue(this);
    }
}
