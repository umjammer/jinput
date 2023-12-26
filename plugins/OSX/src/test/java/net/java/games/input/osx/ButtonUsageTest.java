/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx;

import net.java.games.input.usb.ButtonUsageId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * ButtonUsageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-24 nsano initial version <br>
 */
class ButtonUsageTest {

    @Test
    void test3() throws Exception {
        for (int i = 0; i < 34; i++) {
            ButtonUsageId bu = ButtonUsageId.map(i);
            if (i == 0 || i == 33) {
                assertNull(bu.getIdentifier());
            } else {
                assertEquals(String.valueOf(i - 1), bu.getIdentifier().getName());
            }
        }
    }
}