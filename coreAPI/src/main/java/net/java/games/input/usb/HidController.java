/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;

import java.io.IOException;

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

    /**
     * The raw report descriptor of the device, the bytes the hid parser reads.
     *
     * @return null when the backend cannot tell
     */
    default byte[] getReportDescriptor() {
        return null;
    }

    /**
     * Sends a report to the device as is, with no rumbler in between.
     * This is the general way to talk to a device, i.e. to change settings kept in feature reports.
     *
     * @param type {@link HidReportType#OUTPUT} or {@link HidReportType#FEATURE}
     * @param reportId 0 when the device does not number its reports
     * @param data the report body, without the report id
     * @throws UnsupportedOperationException when the backend cannot send reports
     */
    default void writeReport(HidReportType type, int reportId, byte[] data) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot write reports");
    }

    /**
     * Asks the device for a report.
     *
     * @param type {@link HidReportType#INPUT} or {@link HidReportType#FEATURE}
     * @param reportId 0 when the device does not number its reports
     * @param buffer filled with the report body, without the report id
     * @return the number of bytes filled
     * @throws UnsupportedOperationException when the backend cannot read reports
     */
    default int readReport(HidReportType type, int reportId, byte[] buffer) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot read reports");
    }

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
