/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.awt.Color;


/**
 * The folded paper dart, in body coordinates.
 * <p>
 * {@code +x} is the right wing, {@code +y} is up out of the fold, {@code +z} is the nose,
 * which is the same frame the pad's own sensor axes are read in.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public final class PaperPlane {

    private static final Color PAPER = new Color(0xf3, 0xef, 0xe3);
    private static final Color CREASE = new Color(0x8a, 0x93, 0xa8);
    private static final Color NOSE_TIP = new Color(0xff, 0x6b, 0x53);

    private static final Vec3 NOSE = new Vec3(0, 0, 1.55);
    private static final Vec3 RIDGE = new Vec3(0, 0.13, -0.10);
    private static final Vec3 TAIL = new Vec3(0, 0, -1.05);
    private static final Vec3 LEFT_TIP = new Vec3(-1.15, -0.12, -1.05);
    private static final Vec3 RIGHT_TIP = new Vec3(1.15, -0.12, -1.05);
    private static final Vec3 FIN = new Vec3(0, -0.52, -1.05);

    private PaperPlane() {
    }

    /**
     * Adds the plane to the scene under the given attitude.
     *
     * @param attitude body to world rotation
     */
    public static void draw(Scene3D scene, Quat attitude) {
        Vec3 nose = attitude.rotate(NOSE);
        Vec3 ridge = attitude.rotate(RIDGE);
        Vec3 tail = attitude.rotate(TAIL);
        Vec3 left = attitude.rotate(LEFT_TIP);
        Vec3 right = attitude.rotate(RIGHT_TIP);
        Vec3 fin = attitude.rotate(FIN);

        // the two wing halves, folded along the ridge
        scene.face(PAPER, nose, ridge, left);
        scene.face(PAPER, ridge, tail, left);
        scene.face(PAPER, nose, right, ridge);
        scene.face(PAPER, ridge, right, tail);

        // the keel hanging under the fold
        scene.face(PAPER, nose, tail, fin);

        // creases, so the fold reads even when a wing is edge on
        scene.line(CREASE, 1.6f, nose, ridge);
        scene.line(CREASE, 1.6f, ridge, tail);
        scene.line(CREASE, 1.6f, nose, left);
        scene.line(CREASE, 1.6f, nose, right);
        scene.line(CREASE, 1.6f, left, tail);
        scene.line(CREASE, 1.6f, right, tail);
        scene.line(CREASE, 1.6f, tail, fin);
        scene.line(CREASE, 1.6f, nose, fin);

        // a dab of colour on the nose, so which end is forward is never in doubt
        scene.line(NOSE_TIP, 5f, nose, nose.lerp(ridge, 0.22));
    }

    /** where the nose sits in world space, used to trail the flight path */
    public static Vec3 nose(Quat attitude) {
        return attitude.rotate(NOSE);
    }
}
