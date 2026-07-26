/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;


/**
 * Turns the raw gyro and accelerometer readings into something drawable.
 * <p>
 * The gyro alone would work for about ten seconds before the integrated attitude drifts
 * off, and the accelerometer alone is far too noisy to point a paper plane with. So this
 * runs the usual complementary pair:
 * <ul>
 *  <li>the attitude is integrated from the gyro, which is smooth but drifts</li>
 *  <li>the accelerometer says which way is down, which is noisy but does not drift, and is
 *      fed back as a small correction around the tilt error</li>
 * </ul>
 * Gravity cannot observe heading, so yaw is left to the gyro and will slowly wander. That
 * is expected, and the pad has no magnetometer to fix it with.
 * <p>
 * Separately, a low pass of the acceleration gives the acceleration something worth
 * drawing: subtracting the steady part from the raw reading leaves the linear
 * acceleration, the part that actually corresponds to shoving the pad around.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class AttitudeFilter {

    /** direction an accelerometer at rest reports, in world space */
    public static final Vec3 UP = new Vec3(0, 1, 0);

    /** time constant of the gravity low pass, in seconds */
    private static final double GRAVITY_TAU = 0.55;

    /** how hard the accelerometer pulls the integrated attitude back, in 1/s */
    private static final double CORRECTION_GAIN = 1.6;

    /** a reading this far off 1g is mostly shove, so trust it less as a gravity reference */
    private static final double TRUST_WINDOW = 0.45;

    private Quat attitude = Quat.IDENTITY;
    /** low passed gravity, held in the world frame so that turning the pad does not lag it */
    private Vec3 gravityWorld = UP;
    private Vec3 gravity = UP;
    private Vec3 linear = Vec3.ZERO;
    private Vec3 spin = Vec3.ZERO;

    /** set to -1 when the pad reports gravity the other way round */
    private int accelSign = 1;

    /**
     * @param gyro  angular velocity in the body frame, in radians per second
     * @param accel proper acceleration in the body frame in g, gravity included
     * @param dt    seconds since the previous update
     */
    public void update(Vec3 gyro, Vec3 accel, double dt) {
        if (dt <= 0) return;
        dt = Math.min(dt, 0.1);

        Vec3 signed = accel.scale(accelSign);
        spin = gyro;

        // Mahony style correction: the cross product of the measured and the predicted
        // gravity direction is, for small errors, the tilt error itself. It is taken from
        // the instantaneous reading, not from the low pass below, because the gain here is
        // already the filter; averaging first would only add lag. A reading well off 1g is
        // mostly shove and says little about which way is down, so it is weighted out.
        Vec3 measured = signed.normalize();
        Vec3 correction = Vec3.ZERO;
        if (measured.length() > 0.5) {
            Vec3 predicted = attitude.rotateInverse(UP);
            double trust = Math.max(0, 1 - Math.abs(signed.length() - 1) / TRUST_WINDOW);
            correction = measured.cross(predicted).scale(CORRECTION_GAIN * trust);
        }

        Vec3 omega = spin.add(correction);
        attitude = attitude.multiply(Quat.fromAxisAngle(omega, omega.length() * dt)).normalize();

        // split the reading into gravity and shove. The low pass runs in the world frame,
        // where gravity really is constant, otherwise spinning the pad alone would smear
        // the estimate around and show up as a shove that never happened.
        double a = 1 - Math.exp(-dt / GRAVITY_TAU);
        gravityWorld = gravityWorld.lerp(attitude.rotate(signed), a);
        gravity = attitude.rotateInverse(gravityWorld);
        linear = signed.sub(gravity);
    }

    /** body to world rotation of the pad */
    public Quat attitude() {
        return attitude;
    }

    /** low passed gravity direction, in the body frame */
    public Vec3 gravity() {
        return gravity;
    }

    /** acceleration with gravity taken out, in the body frame */
    public Vec3 linear() {
        return linear;
    }

    /** angular velocity in radians per second, in the body frame */
    public Vec3 spin() {
        return spin;
    }

    public void reset() {
        attitude = Quat.IDENTITY;
        gravityWorld = UP;
        gravity = UP;
        linear = Vec3.ZERO;
        spin = Vec3.ZERO;
    }

    /** flips the sign of the accelerometer, for a pad that reports gravity the other way */
    public void flipAccelSign() {
        accelSign = -accelSign;
        reset();
    }

    public int accelSign() {
        return accelSign;
    }
}
