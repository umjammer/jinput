/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;


/**
 * A minimal immutable 3D vector.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public record Vec3(double x, double y, double z) {

    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    public Vec3 add(Vec3 o) {
        return new Vec3(x + o.x, y + o.y, z + o.z);
    }

    public Vec3 sub(Vec3 o) {
        return new Vec3(x - o.x, y - o.y, z - o.z);
    }

    public Vec3 scale(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public double dot(Vec3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vec3 cross(Vec3 o) {
        return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /** @return the zero vector as is when it has no direction */
    public Vec3 normalize() {
        double l = length();
        return l < 1e-9 ? ZERO : scale(1 / l);
    }

    /** linear interpolation, {@code t} 0 returns this, 1 returns {@code o} */
    public Vec3 lerp(Vec3 o, double t) {
        return new Vec3(x + (o.x - x) * t, y + (o.y - y) * t, z + (o.z - z) * t);
    }

    /** @return an arbitrary unit vector perpendicular to this one */
    public Vec3 anyPerpendicular() {
        Vec3 seed = Math.abs(y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        return cross(seed).normalize();
    }
}
