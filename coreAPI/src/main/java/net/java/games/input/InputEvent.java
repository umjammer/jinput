/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input;

import java.util.EventObject;


/**
 * InputEvent.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-11-07 nsano initial version <br>
 */
public abstract class InputEvent extends EventObject {

    /** */
    public InputEvent(Object source) {
        super(source);
    }

    /** */
    public abstract boolean getNextEvent(Event event);
}
