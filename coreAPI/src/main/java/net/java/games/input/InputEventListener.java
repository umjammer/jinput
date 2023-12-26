/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input;


/**
 * InputEventListener.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-11-07 nsano initial version <br>
 */
public interface InputEventListener {

    /**
     *
     * @param event input report data
     */
    void onInput(InputEvent event);
}
