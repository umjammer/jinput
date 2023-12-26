/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;


import net.java.games.input.AbstractController;


/**
 * HidController.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-31 nsano initial version <br>
 */
public interface HidController {

    /** */
    int getProductId();

    /** */
    int getVendorId();

    /** */
    interface HidReport extends AbstractController.Report {

        /** */
        int getReportId();

        /** */
        byte[] getData();
    }
}
