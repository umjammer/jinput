/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * PaperPlaneVisualizerTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
class PaperPlaneVisualizerTest {

    static boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }

    @Test
    @DisplayName("the quaternion rotates the way the renderer assumes")
    void test1() {
        Quat q = Quat.fromAxisAngle(new Vec3(0, 1, 0), Math.PI / 2);
        Vec3 v = q.rotate(new Vec3(0, 0, 1));
        assertEquals(1, v.x(), 1e-9);
        assertEquals(0, v.y(), 1e-9);
        assertEquals(0, v.z(), 1e-9);

        // and back again
        Vec3 back = q.rotateInverse(v);
        assertEquals(0, back.x(), 1e-9);
        assertEquals(1, back.z(), 1e-9);
    }

    @Test
    @DisplayName("the accelerometer pulls the drifting gyro attitude back level")
    void test2() {
        DemoSensorSource source = new DemoSensorSource();
        AttitudeFilter filter = new AttitudeFilter();

        // start the filter out badly wrong, so only the correction term can recover it
        filter.update(new Vec3(0, 0, 4 / 0.5), source.accel(), 0.5);

        double dt = 1 / 120d;
        double tiltError = Double.NaN;
        for (int i = 0; i < 120 * 30; i++) {
            source.step(dt);
            filter.update(source.gyro(), source.accel(), dt);

            // where the filter thinks up is, against where it really is
            Vec3 estimated = filter.attitude().rotate(AttitudeFilter.UP);
            Vec3 actual = source.trueAttitude().rotate(AttitudeFilter.UP);
            tiltError = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, estimated.dot(actual)))));
        }
        // it starts out about 160 deg wrong and settles to a couple of degrees
        assertTrue(tiltError < 8, "tilt error should have converged, was " + tiltError + " deg");
    }

    @Test
    @DisplayName("the gravity split leaves the shove behind")
    void test3() {
        AttitudeFilter filter = new AttitudeFilter();

        // sit still for a while, the whole reading is gravity and nothing is left over
        for (int i = 0; i < 600; i++) {
            filter.update(Vec3.ZERO, AttitudeFilter.UP, 1 / 120d);
        }
        assertTrue(filter.linear().length() < 0.02, "at rest: " + filter.linear());

        // then a shove sideways, which the low pass has not caught up with yet
        filter.update(Vec3.ZERO, AttitudeFilter.UP.add(new Vec3(0.8, 0, 0)), 1 / 120d);
        assertTrue(filter.linear().x() > 0.7, "shoved: " + filter.linear());
    }

    @Test
    @DisplayName("a frame renders without a device attached")
    @DisabledIf("headless")
    void test4() throws Exception {
        PaperPlanePanel panel = new PaperPlanePanel(DemoSensorSource::new);
        panel.setSize(640, 480);

        BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();

        // something other than the background must have been drawn in the middle
        assertNotNull(panel.filter());
        boolean painted = false;
        for (int y = 200; y < 280 && !painted; y++) {
            for (int x = 280; x < 360; x++) {
                if ((image.getRGB(x, y) & 0xffffff) > 0x303030) {
                    painted = true;
                    break;
                }
            }
        }
        assertTrue(painted, "the plane should be visible in the middle of the frame");
    }

    /**
     * Opens the real window and leaves it up. Off by default because it never returns on
     * its own, run it with {@code -Dvisualizer=true} or through {@code main}.
     */
    @Test
    @DisplayName("show the window")
    @EnabledIfSystemProperty(named = "visualizer", matches = "true")
    @DisabledIf("headless")
    void test5() throws Exception {
        CountDownLatch closed = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = PaperPlaneVisualizer.createFrame();
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) {
                    closed.countDown();
                }
            });
            frame.setVisible(true);
        });
        closed.await();
    }
}
