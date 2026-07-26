/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;


/**
 * Supplies the six motion channels the visualizer draws.
 * <p>
 * Both vectors are in physical units, not normalized, so that the scaling argument happens
 * once here at the device and everything downstream can just do geometry:
 * <ul>
 *  <li>{@link #gyro()} angular velocity in radians per second</li>
 *  <li>{@link #accel()} proper acceleration in g, gravity included, so a pad lying still
 *      reads a magnitude of 1</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public interface SensorSource {

    /** shown on the HUD so it is obvious where the numbers come from */
    String name();

    /** reads the device, called once per frame */
    void poll();

    /** angular velocity around the pad's x, y, z axes, in radians per second */
    Vec3 gyro();

    /** proper acceleration along the pad's x, y, z axes in g, gravity included */
    Vec3 accel();

    /** {@code false} once the device went away, so the visualizer can fall back */
    default boolean isAlive() {
        return true;
    }

    /** releases the device, if this source holds one */
    default void close() {
    }
}
