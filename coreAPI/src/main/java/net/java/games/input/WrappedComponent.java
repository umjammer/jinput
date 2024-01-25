/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input;


/**
 * WrappedComponent.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-01-24 nsano initial version <br>
 */
public interface WrappedComponent<T> {

    /** */
    T getWrappedObject();
}
