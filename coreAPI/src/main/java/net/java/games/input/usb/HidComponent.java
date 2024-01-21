/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.usb;


import net.java.games.input.Component;


/**
 * HidComponent.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-01-17 nsano initial version <br>
 */
public interface HidComponent extends Component {

    /** */
    boolean isValueChanged(byte[] data);

    /** by hid input report */
    float getValue();

    /** by hid input report */
    void setValue(byte[] data);
}
