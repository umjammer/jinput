/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.windows;


import com.sun.jna.platform.win32.Guid;


/**
 * NativeUtil.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-18 nsano initial version <br>
 */
public final class NativeUtil {

    private NativeUtil() {}

    /** guid -> byte[] */
    static byte[] wrapGUID(Guid.GUID guid) {
        byte[] guidArray = new byte[guid.size()];
        guid.getPointer().read(0, guidArray, 0, guidArray.length);
        return guidArray;
    }

    /** byteArray -> guid */
    static void unwrapGUID(byte[] byteArray, Guid.GUID guid) {
        guid.getPointer().write(0, byteArray, 0, guid.size());
    }
}
