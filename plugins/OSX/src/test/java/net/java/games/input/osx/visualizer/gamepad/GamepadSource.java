/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

/**
 * Where the readings come from.
 * <p>
 * The panel polls this once a frame and draws whatever comes back, so a real device, a
 * recording or the demo are all the same to it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public interface GamepadSource extends AutoCloseable {

    /** @return the state as of now, the same instance every time */
    GamepadState poll();

    /** {@code false} once the device has gone away and the source is worth replacing */
    default boolean isAlive() {
        return true;
    }

    /**
     * Shakes the pad, if it can be shaken.
     *
     * @param magnitude 0 to stop, 1 as hard as it goes
     */
    default void rumble(double magnitude) {
    }

    @Override
    void close();
}
