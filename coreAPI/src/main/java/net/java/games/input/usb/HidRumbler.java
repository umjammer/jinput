/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;

import net.java.games.input.Rumbler;


/**
 * HidRumbler.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-12-25 nsano initial version <br>
 */
public interface HidRumbler extends Rumbler {

    /** @since 2.0.15v */
    int getReportId();

    /** */
    void fill(byte[] data);
}
