/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;


/**
 * Which stick the single analog stick of the OCTA acts as.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public enum HoriStick {

    LS(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
    RS(new byte[] {1, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1});

    private final byte[] bytes;

    HoriStick(byte[] bytes) {
        this.bytes = bytes;
    }

    /** @return what is stored at {@link HoriProfileMemory#STICK_OFFSET}, a copy */
    public byte[] bytes() {
        return bytes.clone();
    }
}
