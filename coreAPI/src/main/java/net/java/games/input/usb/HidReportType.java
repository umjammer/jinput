/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;


/**
 * The three kinds of HID report, in the order the HID specification and IOKit number them.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public enum HidReportType {

    /** device to host, what the pad sends while it is used */
    INPUT,
    /** host to device, rumble, leds and the like */
    OUTPUT,
    /** both directions on request, where devices keep their settings */
    FEATURE;

    /** @return the number the HID specification gives this type (0, 1, 2) */
    public int value() {
        return ordinal();
    }
}
