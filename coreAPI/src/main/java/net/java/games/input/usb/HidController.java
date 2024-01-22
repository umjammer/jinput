/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;

import net.java.games.input.AbstractController;
import net.java.games.input.Controller;
import net.java.games.input.Rumbler;


/**
 * HidController.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-31 nsano initial version <br>
 */
public interface HidController extends Controller {

    /** hid product id */
    int getProductId();

    /** hid vender id */
    int getVendorId();

    /** data structure to report for a hid device */
    abstract class HidReport implements AbstractController.Report {

        /** hid report id */
        public abstract int getReportId();

        /** data bytes to write */
        public abstract byte[] getData();

        @Override
        public void cascadeTo(Rumbler[] rumblers) {
            for (Rumbler rumbler : rumblers) {
                if (rumbler instanceof HidRumbler hidRumbler) {
                    if (getReportId() == hidRumbler.getReportId()) {
                        cascadeTo(hidRumbler);
                    }
                }
            }
        }

        /** set value from class field to each rumbler */
        protected abstract void cascadeTo(HidRumbler rumbler);

        /** pack rumbler value into bytes */
        private void pack(Rumbler[] rumblers) {
            for (Rumbler rumbler : rumblers) {
                if (rumbler instanceof HidRumbler hidRumbler) {
                    if (hidRumbler.getReportId() == getReportId()) {
                        hidRumbler.fill(getData());
                    }
                }
            }
        }

        /** class fields -> rumblers -> bytes */
        public void setup(Rumbler[] rumblers) {
            this.cascadeTo(rumblers);
            this.pack(rumblers);
        }
    }
}
