/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.linux;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * timeval.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-18 nsano initial version <br>
 */
class timeval extends Structure {

    public long tv_sec;
    public long tv_usec;

    public timeval() {
    }

    public timeval(Pointer p) {
        super(p);
    }

    public static class ByReference extends timeval implements Structure.ByReference {
    }

    public static class ByValue extends timeval implements Structure.ByValue {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("tv_sec", "tv_usec");
    }

    public long toNanos() {
        return tv_usec * 1000 * 1000 + tv_usec * 10 * 1000; // TODO
    }
}
