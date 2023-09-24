/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.rococoa.kernel;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;


/**
 * KernelLib.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-22 nsano initial version <br>
 */
public interface KernelLib extends Library {

    KernelLib INSTANCE = Native.load("Kernel", KernelLib.class);

    int kNilOptions = 0;

    void absolutetime_to_nanoseconds(long abstime, LongByReference result);
}
