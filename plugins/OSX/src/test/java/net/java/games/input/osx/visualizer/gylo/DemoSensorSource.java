/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.util.Random;


/**
 * A synthetic stand-in for a pad that has no motion sensor attached.
 * <p>
 * It flies a virtual pad around, keeping a ground truth attitude of its own and reporting
 * exactly what the real sensors would report from it: the angular velocity in the body
 * frame, plus gravity rotated into the body frame with a bit of shake on top. That makes
 * it a fair test of the filtering the visualizer does, not just of the drawing.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class DemoSensorSource implements SensorSource {

    private final Random random = new Random(20260726L);

    private Quat attitude = Quat.IDENTITY;
    private Vec3 gyro = Vec3.ZERO;
    private Vec3 accel = new Vec3(0, 1, 0);

    private double t;
    private long last = System.nanoTime();

    @Override
    public String name() {
        return "demo (no motion sensor found)";
    }

    @Override
    public void poll() {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - last) / 1e9);
        last = now;
        step(dt);
    }

    /** advances the simulation by an exact step instead of by the wall clock, for tests */
    public void step(double dt) {
        t += dt;

        // a lazily wandering angular velocity, in radians per second, in the body frame
        Vec3 omega = new Vec3(
                1.30 * Math.sin(t * 0.73) + 0.45 * Math.sin(t * 2.10 + 1.1),
                0.95 * Math.sin(t * 0.41 + 0.7) + 0.35 * Math.sin(t * 1.70),
                1.55 * Math.sin(t * 0.29 + 2.3) + 0.30 * Math.sin(t * 3.10));

        attitude = attitude.multiply(Quat.fromAxisAngle(omega, omega.length() * dt)).normalize();

        gyro = omega;

        // what an accelerometer riding on that attitude would see: 1g up at rest, plus a shove
        Vec3 gravityBody = attitude.rotateInverse(new Vec3(0, 1, 0));
        double gust = Math.max(0, Math.sin(t * 0.37)) * 0.55;
        Vec3 linear = new Vec3(
                gust * Math.sin(t * 5.3) + noise(),
                gust * Math.sin(t * 4.1 + 2.0) + noise(),
                gust * Math.sin(t * 6.7 + 4.0) + noise());
        accel = gravityBody.add(attitude.rotateInverse(linear));
    }

    private double noise() {
        return (random.nextDouble() - 0.5) * 0.02;
    }

    /** the attitude the readings were generated from, so a test can check what is recovered */
    public Quat trueAttitude() {
        return attitude;
    }

    @Override
    public Vec3 gyro() {
        return gyro;
    }

    @Override
    public Vec3 accel() {
        return accel;
    }
}
