/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;


/**
 * HidControllerEnvironment.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-01-24 nsano initial version <br>
 */
public interface HidControllerEnvironment {

    /**
     * throw NoSuchElementException when there is no matched device of mid and pid.
     */
    HidController getController(int mid, int pid);
}
