/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;


/**
 * A minimal unit quaternion, used to hold the attitude integrated from the gyro.
 * <p>
 * A quaternion is used instead of euler angles so that repeatedly integrating the
 * angular velocity never runs into gimbal lock, whatever way the pad is turned.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public record Quat(double w, double x, double y, double z) {

    public static final Quat IDENTITY = new Quat(1, 0, 0, 0);

    /** @param axis does not need to be normalized, a zero axis yields {@link #IDENTITY} */
    public static Quat fromAxisAngle(Vec3 axis, double radians) {
        Vec3 n = axis.normalize();
        if (n.length() < 1e-9) return IDENTITY;
        double h = radians / 2;
        double s = Math.sin(h);
        return new Quat(Math.cos(h), n.x() * s, n.y() * s, n.z() * s);
    }

    /** hamilton product, {@code this} is applied after {@code o} */
    public Quat multiply(Quat o) {
        return new Quat(
                w * o.w - x * o.x - y * o.y - z * o.z,
                w * o.x + x * o.w + y * o.z - z * o.y,
                w * o.y - x * o.z + y * o.w + z * o.x,
                w * o.z + x * o.y - y * o.x + z * o.w);
    }

    public Quat conjugate() {
        return new Quat(w, -x, -y, -z);
    }

    public Quat normalize() {
        double l = Math.sqrt(w * w + x * x + y * y + z * z);
        return l < 1e-9 ? IDENTITY : new Quat(w / l, x / l, y / l, z / l);
    }

    /** rotates {@code v} from the local frame into the frame this quaternion is expressed in */
    public Vec3 rotate(Vec3 v) {
        Vec3 u = new Vec3(x, y, z);
        double s = w;
        return u.scale(2 * u.dot(v))
                .add(v.scale(s * s - u.dot(u)))
                .add(u.cross(v).scale(2 * s));
    }

    /** rotates {@code v} the other way around, from the outer frame into the local frame */
    public Vec3 rotateInverse(Vec3 v) {
        return conjugate().rotate(v);
    }
}
