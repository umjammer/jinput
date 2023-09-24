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

import java.io.Serial;

import com.sun.jna.NativeLong;


/**
 * CFIndex.
 *
 * @author semaphore
 * @version May 27, 2010 1:14:19 PM
 */
public class CFIndex extends NativeLong {

    @Serial
    private static final long serialVersionUID = -1;

    public static CFIndex of(int i) {
        CFIndex idx = new CFIndex();
        idx.setValue(i);
        return idx;
    }

    public static CFIndex of(long i) {
        CFIndex idx = new CFIndex();
        idx.setValue(i);
        return idx;
    }

    public static CFIndex of(NativeLong i) {
        return CFIndex.of(i.longValue());
    }
}