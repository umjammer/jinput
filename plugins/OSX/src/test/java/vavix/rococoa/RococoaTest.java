/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.rococoa;

import com.sun.jna.ptr.IntByReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import vavi.util.Debug;
import vavix.rococoa.corefoundation.CFAllocator;
import vavix.rococoa.corefoundation.CFLib;
import vavix.rococoa.corefoundation.CFNumber;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * RococoaTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-23 nsano initial version <br>
 */
@EnabledOnOs(OS.MAC)
public class RococoaTest {

    @Test
    void test1() {
Debug.println(CFLib.kCFRunLoopDefaultMode.getString());
        assertEquals("kCFRunLoopDefaultMode", CFLib.kCFRunLoopDefaultMode.getString());
    }

    @Test
    void test2() {
        IntByReference ip = new IntByReference(127);
        CFNumber cfn = CFLib.INSTANCE.CFNumberCreate(CFAllocator.kCFAllocatorDefault, CFLib.CFNumberType.kCFNumberIntType, ip);

Debug.println(cfn);
        assertEquals(127, cfn.getInt());
    }
}
