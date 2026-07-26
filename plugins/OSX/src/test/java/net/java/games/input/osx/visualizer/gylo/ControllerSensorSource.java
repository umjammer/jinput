/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.io.IOException;
import java.lang.System.Logger;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;
import net.java.games.input.usb.HidComponent;

import static java.lang.System.getLogger;


/**
 * Reads the gyro and the accelerometer out of a jinput {@link Controller}.
 * <p>
 * The six channels are looked up <b>by name</b>, {@code "Gyro X"} through {@code "Accel Z"},
 * which is what a device support plugin calls them once it has carved them out of the input
 * report. That is the only reliable handle: the identifiers differ between plugins, and on
 * the DualShock they are not even the obvious ones, the gyro is filed under
 * {@code X_ACCELERATION} and the accelerometer under {@code X_VELOCITY}.
 * <p>
 * Two ways of getting a value are supported, because the plugins differ:
 * <ul>
 *  <li>a {@link PollingController} is polled and its components read back, which is how
 *      the IOKit plugin works. Those values arrive already normalized to {@code -1 ..= 1},
 *      so they are scaled by the nominal full range of the sensor</li>
 *  <li>anything else is opened and left to push input events, and the components are read
 *      through {@link HidComponent#getValue()}, which is how the hid4java plugin works.
 *      Those are raw report fields, unsigned, so the sign has to be put back first</li>
 * </ul>
 * A channel that does not resolve reads zero, so a pad exposing only some of them still
 * drives what it can.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class ControllerSensorSource implements SensorSource {

    private static final Logger logger = getLogger(ControllerSensorSource.class.getName());

    /**
     * Counts per g of the DualShock's accelerometer, the same constant the Linux
     * {@code hid-sony} driver uses. A pad lying still reads about this on one axis.
     */
    private static final double ACCEL_COUNTS_PER_G = 8192;

    /** nominal full scale of the DualShock's gyro, in degrees per second */
    private static final double GYRO_FULL_SCALE_DEG_S = 2000;

    /** counts per degree per second, from the full scale over a signed 16 bit field */
    private static final double GYRO_COUNTS_PER_DEG_S = 0x8000 / GYRO_FULL_SCALE_DEG_S;

    /** nominal full scale of the accelerometer, in g, for a plugin that pre-normalizes */
    private static final double ACCEL_FULL_SCALE_G = 0x8000 / ACCEL_COUNTS_PER_G;

    /** frames averaged at startup to work out the gyro's zero offset */
    private static final int BIAS_FRAMES = 90;

    /** an acceleration below this is taken to be a torn read rather than a real reading */
    private static final double MIN_PLAUSIBLE_G = 0.33;

    /** one of the six motion channels, named the way a device support plugin names it */
    private enum Channel {
        GYRO_X("gyro x", "gyrox", "gyro-x"),
        GYRO_Y("gyro y", "gyroy", "gyro-y"),
        GYRO_Z("gyro z", "gyroz", "gyro-z"),
        ACCEL_X("accel x", "accelx", "accel-x", "acceleration x"),
        ACCEL_Y("accel y", "accely", "accel-y", "acceleration y"),
        ACCEL_Z("accel z", "accelz", "accel-z", "acceleration z");

        final String[] aliases;

        Channel(String... aliases) {
            this.aliases = aliases;
        }

        boolean matches(String name) {
            return Arrays.asList(aliases).contains(name.toLowerCase(Locale.ROOT).trim());
        }
    }

    private final Controller controller;
    private final Component[] bound = new Component[Channel.values().length];

    /** the gyro reads a steady tens of counts even at rest, so that offset is measured out */
    private final double[] gyroBias = new double[3];
    private int biasFrames;

    private Vec3 gyro = Vec3.ZERO;
    private Vec3 accel = new Vec3(0, 1, 0);
    private boolean alive = true;

    /**
     * @throws IOException the controller could not be opened
     */
    public ControllerSensorSource(Controller controller) throws IOException {
        this.controller = controller;
        for (Channel c : Channel.values()) {
            bound[c.ordinal()] = resolve(c);
logger.log(Logger.Level.DEBUG, c + " -> " + (bound[c.ordinal()] == null ? "(none)" : bound[c.ordinal()].getName()));
        }
        if (!(controller instanceof PollingController)) {
            // an event driven plugin only refreshes its components while it is open
            controller.open();
        }
    }

    private Component resolve(Channel channel) {
        Optional<Component> found = Arrays.stream(controller.getComponents())
                .filter(c -> channel.matches(c.getName()))
                .findFirst();
        return found.orElse(null);
    }

    /** @return how many of the six channels were found on this device */
    public int boundChannelCount() {
        return (int) Arrays.stream(bound).filter(c -> c != null).count();
    }

    /** the names this device actually offers, for when nothing matched and it needs saying */
    public String componentNames() {
        return Arrays.stream(controller.getComponents())
                .map(Component::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("(none)");
    }

    /** {@code true} while the gyro zero offset is still being measured */
    public boolean isCalibrating() {
        return biasFrames < BIAS_FRAMES;
    }

    @Override
    public String name() {
        return controller.getName() + " (" + boundChannelCount() + "/6 motion axes"
                + (isCalibrating() ? ", calibrating" : "") + ")";
    }

    @Override
    public void poll() {
        if (controller instanceof PollingController pc && !pc.poll()) {
            alive = false;
            return;
        }

        double gx = read(Channel.GYRO_X, GYRO_COUNTS_PER_DEG_S, GYRO_FULL_SCALE_DEG_S);
        double gy = read(Channel.GYRO_Y, GYRO_COUNTS_PER_DEG_S, GYRO_FULL_SCALE_DEG_S);
        double gz = read(Channel.GYRO_Z, GYRO_COUNTS_PER_DEG_S, GYRO_FULL_SCALE_DEG_S);

        // hold the pad still for the first moment and the resting offset is averaged out
        if (biasFrames < BIAS_FRAMES) {
            double n = ++biasFrames;
            gyroBias[0] += (gx - gyroBias[0]) / n;
            gyroBias[1] += (gy - gyroBias[1]) / n;
            gyroBias[2] += (gz - gyroBias[2]) / n;
        }

        gyro = new Vec3(Math.toRadians(gx - gyroBias[0]),
                Math.toRadians(gy - gyroBias[1]),
                Math.toRadians(gz - gyroBias[2]));

        Vec3 sample = new Vec3(read(Channel.ACCEL_X, ACCEL_COUNTS_PER_G, ACCEL_FULL_SCALE_G),
                read(Channel.ACCEL_Y, ACCEL_COUNTS_PER_G, ACCEL_FULL_SCALE_G),
                read(Channel.ACCEL_Z, ACCEL_COUNTS_PER_G, ACCEL_FULL_SCALE_G));

        // An event driven plugin writes its components from the HID reader thread while
        // this reads them from the render thread, so now and then one axis is caught a
        // report behind and the vector collapses. A hand held pad never really sees less
        // than a third of a g, so such a sample is dropped rather than drawn as a lurch.
        if (sample.length() > MIN_PLAUSIBLE_G) {
            accel = sample;
        }
    }

    /**
     * @param countsPerUnit divisor for a raw report field
     * @param fullScale     value a pre-normalized {@code 1.0} stands for
     */
    private double read(Channel channel, double countsPerUnit, double fullScale) {
        Component c = bound[channel.ordinal()];
        if (c == null) return 0;

        if (c instanceof PollingComponent pc) {
            return pc.getPollData() * fullScale;
        } else if (c instanceof HidComponent hc) {
            return (short) (int) hc.getValue() / countsPerUnit;
        }
        return 0;
    }

    /** discards the measured gyro offset so it is taken again */
    public void recalibrate() {
        biasFrames = 0;
        Arrays.fill(gyroBias, 0);
    }

    @Override
    public Vec3 gyro() {
        return gyro;
    }

    @Override
    public Vec3 accel() {
        return accel;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void close() {
        try {
            if (!(controller instanceof PollingController)) {
                controller.close();
            }
        } catch (IOException e) {
logger.log(Logger.Level.DEBUG, "closing " + controller.getName(), e);
        }
    }
}
