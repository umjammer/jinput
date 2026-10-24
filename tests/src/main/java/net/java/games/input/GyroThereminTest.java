/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input;

import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.java.games.input.plugin.DualShock4PluginBase.Report5;
import net.java.games.input.usb.HidController;
import net.java.games.input.usb.HidInputEvent;


/**
 * GyroThereminTest.
 * <p>
 * A theremin like synthesizer played with the gyroscope of a DualShock 4.
 * <ul>
 *  <li>tilt the controller forward/backward : pitch</li>
 *  <li>tilt the controller left/right : volume</li>
 * </ul>
 * buttons
 * <ul>
 *  <li>△ (hold) : octave up</li>
 *  <li>✖ (hold) : octave down</li>
 *  <li>○ (hold) : vibrato</li>
 *  <li>□ (hold) : tremolo</li>
 *  <li>L1 : change waveform (sine → triangle → saw → square)</li>
 *  <li>R1 : echo on/off</li>
 *  <li>L2 (analog) : low-pass filter</li>
 *  <li>R2 (analog) : distortion</li>
 *  <li>L3 : re-center the gyro</li>
 *  <li>R3 : bit crusher on/off</li>
 *  <li>D-pad ← → : transpose a semitone down/up</li>
 *  <li>D-pad ↑ : change scale snap (off → chromatic → pentatonic)</li>
 *  <li>D-pad ↓ : reset transpose</li>
 *  <li>touch pad click (hold) : power chord (adds 5th and octave)</li>
 *  <li>Share : mute on/off</li>
 *  <li>Options : print help</li>
 *  <li>PS : quit</li>
 * </ul>
 * the gyroscope is read from raw DualShock 4 input reports ({@link HidInputEvent}).
 * when the environment plugin does not deliver raw reports, the left stick is used
 * instead of the gyroscope (x : pitch, y : volume).
 * <p>
 * usage: {@code GyroThereminTest [controller name substring]}
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-24 nsano initial version <br>
 */
public class GyroThereminTest {

    /** sony */
    private static final int VENDOR_ID = 0x54c;
    /** DualShock 4 v1, v2 */
    private static final int[] PRODUCT_IDS = {0x5c4, 0x9cc};

    /** gyro sensitivity, about 16 LSB per degree/second */
    private static final double GYRO_DPS_PER_LSB = 1 / 16.0;
    /** angular velocity below this (degree/second) is treated as noise */
    private static final double GYRO_DEAD_ZONE = 1.5;
    /** the angle range (± degree) mapped to the full pitch/volume range */
    private static final double ANGLE_RANGE = 60;
    /** reports used to measure the gyro bias at start up or re-centering (keep the controller still) */
    private static final int CALIBRATION_REPORTS = 100;

    /** the lowest note (center - 2 octaves) */
    private static final double BASE_FREQUENCY = 110;
    /** pitch range in semitones */
    private static final double PITCH_RANGE = 48;

    /** raw reports older than this are considered as stopped, nanoseconds */
    private static final long RAW_TIMEOUT = 300_000_000L;

    /** controller buttons, raw report and component mapping are unified into this */
    enum Button {
        SQUARE, CROSS, CIRCLE, TRIANGLE, L1, R1, L2, R2, SHARE, OPTIONS, L3, R3, PS, TOUCHPAD, UP, RIGHT, DOWN, LEFT
    }

    /** D-pad hat value (0: N, 1: NE, ... 7: NW, 8: released) to buttons */
    private static void addHat(int hat, Set<Button> buttons) {
        if (hat > 7) return;
        if (hat == 7 || hat == 0 || hat == 1) buttons.add(Button.UP);
        if (hat == 1 || hat == 2 || hat == 3) buttons.add(Button.RIGHT);
        if (hat == 3 || hat == 4 || hat == 5) buttons.add(Button.DOWN);
        if (hat == 5 || hat == 6 || hat == 7) buttons.add(Button.LEFT);
    }

    /** gyro state, updated by the raw report listener */
    private static class Gyro {
        /** degree */
        double pitch, roll;
        /** bias in LSB */
        double biasX, biasY, biasZ;
        double sumX, sumY, sumZ;
        int calibrationCount;
        long lastNanos;
        /** latest raw buttons and triggers */
        EnumSet<Button> buttons = EnumSet.noneOf(Button.class);
        float l2, r2;
        long lastRawNanos;

        synchronized void recenter() {
            pitch = 0;
            roll = 0;
            calibrationCount = 0;
            sumX = sumY = sumZ = 0;
        }

        boolean isCalibrating() {
            return calibrationCount < CALIBRATION_REPORTS;
        }

        /**
         * @param data DualShock 4 input report, includes the first byte (report id)
         * @see "https://www.psdevwiki.com/ps4/DS4-USB"
         */
        synchronized void update(byte[] data) {
            // usb: report id 0x01, bluetooth: report id 0x11 has 2 extra bytes
            int o;
            if (data.length >= 25 && data[0] == 0x01) {
                o = 0;
            } else if (data.length >= 27 && data[0] == 0x11) {
                o = 2;
            } else {
                return;
            }

            EnumSet<Button> b = EnumSet.noneOf(Button.class);
            if ((data[o + 5] & 0x10) != 0) b.add(Button.SQUARE);
            if ((data[o + 5] & 0x20) != 0) b.add(Button.CROSS);
            if ((data[o + 5] & 0x40) != 0) b.add(Button.CIRCLE);
            if ((data[o + 5] & 0x80) != 0) b.add(Button.TRIANGLE);
            addHat(data[o + 5] & 0x0f, b);
            if ((data[o + 6] & 0x01) != 0) b.add(Button.L1);
            if ((data[o + 6] & 0x02) != 0) b.add(Button.R1);
            if ((data[o + 6] & 0x04) != 0) b.add(Button.L2);
            if ((data[o + 6] & 0x08) != 0) b.add(Button.R2);
            if ((data[o + 6] & 0x10) != 0) b.add(Button.SHARE);
            if ((data[o + 6] & 0x20) != 0) b.add(Button.OPTIONS);
            if ((data[o + 6] & 0x40) != 0) b.add(Button.L3);
            if ((data[o + 6] & 0x80) != 0) b.add(Button.R3);
            if ((data[o + 7] & 0x01) != 0) b.add(Button.PS);
            if ((data[o + 7] & 0x02) != 0) b.add(Button.TOUCHPAD);
            buttons = b;
            l2 = (data[o + 8] & 0xff) / 255f;
            r2 = (data[o + 9] & 0xff) / 255f;

            int gx = readLeShort(data, o + 13);
            int gy = readLeShort(data, o + 15);
            int gz = readLeShort(data, o + 17);

            long now = System.nanoTime();
            lastRawNanos = now;

            if (isCalibrating()) {
                sumX += gx;
                sumY += gy;
                sumZ += gz;
                if (++calibrationCount == CALIBRATION_REPORTS) {
                    biasX = sumX / CALIBRATION_REPORTS;
                    biasY = sumY / CALIBRATION_REPORTS;
                    biasZ = sumZ / CALIBRATION_REPORTS;
                }
                lastNanos = now;
                return;
            }

            double dt = Math.min((now - lastNanos) / 1e9, 0.05);
            lastNanos = now;

            // x: pitch (tilt forward/backward), z: roll (tilt left/right), y: yaw is not used
            double wx = deadZone((gx - biasX) * GYRO_DPS_PER_LSB);
            double wz = deadZone((gz - biasZ) * GYRO_DPS_PER_LSB);
            pitch = clamp(pitch + wx * dt, -ANGLE_RANGE, ANGLE_RANGE);
            roll = clamp(roll + wz * dt, -ANGLE_RANGE, ANGLE_RANGE);
        }

        private static int readLeShort(byte[] data, int index) {
            return (short) ((data[index] & 0xff) | (data[index + 1] << 8));
        }

        private static double deadZone(double v) {
            return Math.abs(v) < GYRO_DEAD_ZONE ? 0 : v;
        }
    }

    enum Waveform { SINE, TRIANGLE, SAW, SQUARE }

    /** the synthesizer, parameters are set from the controller thread */
    private static class Synth implements Runnable {
        static final float SAMPLE_RATE = 44100;
        static final int BLOCK = 256;

        volatile double frequency = 440;
        volatile double volume = 0.5;
        volatile Waveform waveform = Waveform.SINE;
        volatile boolean vibrato, tremolo, chord, echo, crusher, mute;
        /** 0 ~ 1 */
        volatile double filter, drive;
        volatile boolean running = true;

        // dsp states
        private double freq = 440, amp;
        private double phase, phase5th, phase8va, lfoPhase;
        private double lp;
        private final double[] delay = new double[(int) (SAMPLE_RATE * 0.35)];
        private int delayIndex;
        private double hold;
        private int holdCount;

        @Override
        public void run() {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format, BLOCK * 2 * 4);
                line.start();
                byte[] buf = new byte[BLOCK * 2];
                while (running) {
                    for (int i = 0; i < BLOCK; i++) {
                        int s = (int) (next() * 32767);
                        buf[i * 2] = (byte) s;
                        buf[i * 2 + 1] = (byte) (s >> 8);
                    }
                    line.write(buf, 0, buf.length);
                }
                line.drain();
            } catch (LineUnavailableException e) {
                throw new IllegalStateException(e);
            }
        }

        private static double osc(Waveform waveform, double phase) {
            return switch (waveform) {
                case SINE -> Math.sin(2 * Math.PI * phase);
                case TRIANGLE -> 1 - 4 * Math.abs(phase - 0.5);
                case SAW -> 2 * phase - 1;
                case SQUARE -> phase < 0.5 ? 1 : -1;
            };
        }

        private static double advance(double phase, double frequency) {
            phase += frequency / SAMPLE_RATE;
            return phase - Math.floor(phase);
        }

        /** @return one sample -1 ~ 1 */
        private double next() {
            // glide like a theremin
            freq += (frequency - freq) * 0.0015;
            amp += ((mute ? 0 : volume) - amp) * 0.002;

            lfoPhase = advance(lfoPhase, 5.5);
            double lfo = Math.sin(2 * Math.PI * lfoPhase);

            double f = vibrato ? freq * Math.pow(2, 0.6 * lfo / 12) : freq;
            phase = advance(phase, f);
            double x = osc(waveform, phase);
            if (chord) {
                phase5th = advance(phase5th, f * 1.5);
                phase8va = advance(phase8va, f * 2);
                x = (x + 0.7 * osc(waveform, phase5th) + 0.5 * osc(waveform, phase8va)) / 2.2;
            }

            // low-pass filter
            double cutoff = 200 + 12000 * Math.pow(1 - filter, 2);
            lp += (x - lp) * (1 - Math.exp(-2 * Math.PI * cutoff / SAMPLE_RATE));
            x = lp;

            // distortion
            if (drive > 0.01) {
                double k = 1 + drive * 20;
                x = Math.tanh(x * k) / Math.tanh(k);
            }

            // bit crusher
            if (crusher) {
                if (holdCount++ % 6 == 0) {
                    hold = Math.round(x * 8) / 8.0;
                }
                x = hold;
            }

            double a = tremolo ? amp * (0.6 + 0.4 * Math.sin(2 * Math.PI * lfoPhase * 1.5)) : amp;
            x *= a;

            // echo
            double d = delay[delayIndex];
            delay[delayIndex] = echo ? x + d * 0.45 : d * 0.45;
            delayIndex = (delayIndex + 1) % delay.length;
            x += d * 0.5;

            return Math.max(-1, Math.min(1, x * 0.35));
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** pentatonic (major) scale degrees */
    private static final int[] PENTATONIC = {0, 2, 4, 7, 9};

    private static double snap(double semitone, int mode) {
        return switch (mode) {
            case 1 -> Math.round(semitone);
            case 2 -> {
                double best = 0, distance = Double.MAX_VALUE;
                int octave = (int) Math.floor(semitone / 12);
                for (int o = octave - 1; o <= octave + 1; o++) {
                    for (int degree : PENTATONIC) {
                        double candidate = o * 12 + degree;
                        if (Math.abs(candidate - semitone) < distance) {
                            distance = Math.abs(candidate - semitone);
                            best = candidate;
                        }
                    }
                }
                yield best;
            }
            default -> semitone;
        };
    }

    private static final String[] SNAP_NAMES = {"off", "chromatic", "pentatonic"};

    private static void printHelp() {
        System.out.println("""
                tilt forward/backward : pitch, tilt left/right : volume
                △ octave up, ✖ octave down, ○ vibrato, □ tremolo (hold)
                L1 waveform, R1 echo, L2 filter, R2 distortion, L3 re-center, R3 bit crusher
                D-pad ←→ transpose, ↑ scale snap, ↓ reset transpose
                touch pad chord (hold), Share mute, Options help, PS quit""");
    }

    private final Controller controller;
    private final Gyro gyro = new Gyro();
    private final Synth synth = new Synth();
    /** latest component values */
    private final Map<Component.Identifier, Float> values = new ConcurrentHashMap<>();

    private Waveform waveform = Waveform.SINE;
    private boolean echo, crusher, mute;
    private int transpose;
    private int snapMode;
    private boolean ledEnabled;

    GyroThereminTest(Controller controller) {
        this.controller = controller;
        this.ledEnabled = isDualShock4(controller) && controller instanceof AbstractController;
    }

    private static boolean isDualShock4(Controller controller) {
        if (controller instanceof HidController hid && hid.getVendorId() == VENDOR_ID) {
            for (int pid : PRODUCT_IDS) {
                if (hid.getProductId() == pid) return true;
            }
        }
        return false;
    }

    /** buttons, triggers and the left stick from the components, used when raw reports are not available */
    private EnumSet<Button> readComponents(double[] analog) {
        EnumSet<Button> buttons = EnumSet.noneOf(Button.class);
        Component.Identifier.Button[] ids = {
                Component.Identifier.Button._0, Component.Identifier.Button._1, Component.Identifier.Button._2,
                Component.Identifier.Button._3, Component.Identifier.Button._4, Component.Identifier.Button._5,
                Component.Identifier.Button._6, Component.Identifier.Button._7, Component.Identifier.Button._8,
                Component.Identifier.Button._9, Component.Identifier.Button._10, Component.Identifier.Button._11,
                Component.Identifier.Button._12, Component.Identifier.Button._13
        };
        // DualShock 4 HID button order is the same as the first 14 of Button
        for (int i = 0; i < ids.length; i++) {
            if (value(ids[i], 0) > 0.5f) buttons.add(Button.values()[i]);
        }
        float pov = value(Component.Identifier.Axis.POV, Component.POV.OFF);
        if (pov != Component.POV.OFF) {
            addHat((Math.round(pov * 8) + 6) % 8, buttons);
        }
        analog[0] = value(Component.Identifier.Axis.X, 0);
        analog[1] = value(Component.Identifier.Axis.Y, 0);
        analog[2] = (value(Component.Identifier.Axis.RX, -1) + 1) / 2; // L2
        analog[3] = (value(Component.Identifier.Axis.RY, -1) + 1) / 2; // R2
        return buttons;
    }

    private float value(Component.Identifier id, float defaultValue) {
        return values.getOrDefault(id, defaultValue);
    }

    /** stores component values of events */
    private void drain(EventSource source) {
        Event event = new Event();
        while (source.getNextEvent(event)) {
            values.put(event.getComponent().getIdentifier(), event.getValue());
        }
    }

    /** {@link InputEvent} or {@link EventQueue} */
    private interface EventSource {
        boolean getNextEvent(Event event);
    }

    void play() throws Exception {
        controller.addInputEventListener(event -> {
            if (event instanceof HidInputEvent hid) {
                gyro.update(hid.getData());
            }
            drain(event::getNextEvent);
        });
        if (!controller.isOpen()) {
            try {
                controller.open();
            } catch (IOException e) {
                System.err.println("open failed, use polling only: " + e);
            }
        }

        Thread audio = new Thread(synth, "synth");
        audio.start();

        printHelp();
        System.out.println("keep the controller still for a moment to calibrate the gyro...");

        EnumSet<Button> previous = EnumSet.noneOf(Button.class);
        double[] analog = new double[4];
        long lastLed = 0;
        long lastPrint = 0;

        while (true) {
            // plugins those don't fire input events
            if (controller instanceof PollingController polling) {
                if (!polling.poll()) {
                    System.out.println("\ncontroller disconnected");
                    break;
                }
                drain(polling.getEventQueue()::getNextEvent);
            }

            long now = System.nanoTime();
            boolean raw = now - gyro.lastRawNanos < RAW_TIMEOUT;

            EnumSet<Button> buttons;
            double pitch, roll, l2, r2;
            if (raw) {
                synchronized (gyro) {
                    buttons = EnumSet.copyOf(gyro.buttons);
                    pitch = gyro.pitch / ANGLE_RANGE;
                    roll = gyro.roll / ANGLE_RANGE;
                    l2 = gyro.l2;
                    r2 = gyro.r2;
                }
            } else {
                buttons = readComponents(analog);
                pitch = analog[0];
                roll = -analog[1];
                l2 = analog[2];
                r2 = analog[3];
            }

            EnumSet<Button> pressed = EnumSet.copyOf(buttons);
            pressed.removeAll(previous);
            previous = buttons;

            if (pressed.contains(Button.PS)) break;
            if (pressed.contains(Button.OPTIONS)) printHelp();
            if (pressed.contains(Button.L1)) waveform = Waveform.values()[(waveform.ordinal() + 1) % Waveform.values().length];
            if (pressed.contains(Button.R1)) echo = !echo;
            if (pressed.contains(Button.R3)) crusher = !crusher;
            if (pressed.contains(Button.SHARE)) mute = !mute;
            if (pressed.contains(Button.L3)) {
                gyro.recenter();
                System.out.println("\nre-centering, keep the controller still...");
            }
            if (pressed.contains(Button.RIGHT)) transpose++;
            if (pressed.contains(Button.LEFT)) transpose--;
            if (pressed.contains(Button.DOWN)) transpose = 0;
            if (pressed.contains(Button.UP)) snapMode = (snapMode + 1) % SNAP_NAMES.length;

            int octave = (buttons.contains(Button.TRIANGLE) ? 12 : 0) - (buttons.contains(Button.CROSS) ? 12 : 0);
            double semitone = snap((pitch + 1) / 2 * PITCH_RANGE + transpose, snapMode) + octave;
            double volume = clamp((roll + 1) / 2, 0, 1);

            synth.frequency = BASE_FREQUENCY * Math.pow(2, semitone / 12);
            synth.volume = volume;
            synth.waveform = waveform;
            synth.vibrato = buttons.contains(Button.CIRCLE);
            synth.tremolo = buttons.contains(Button.SQUARE);
            synth.chord = buttons.contains(Button.TOUCHPAD);
            synth.echo = echo;
            synth.crusher = crusher;
            synth.mute = mute || (raw && gyro.isCalibrating());
            synth.filter = l2;
            synth.drive = r2;

            // light bar follows the pitch
            if (ledEnabled && now - lastLed > 50_000_000L) {
                lastLed = now;
                int rgb = Color.HSBtoRGB((float) (semitone / PITCH_RANGE) * 0.8f, 1, (float) Math.max(0.1, volume));
                Report5 report = new Report5();
                report.ledRed = (rgb >> 16) & 0xff;
                report.ledGreen = (rgb >> 8) & 0xff;
                report.ledBlue = rgb & 0xff;
                try {
                    ((AbstractController) controller).output(report);
                } catch (Exception e) {
                    System.err.println("\nlight bar disabled: " + e);
                    ledEnabled = false;
                }
            }

            if (now - lastPrint > 100_000_000L) {
                lastPrint = now;
                System.out.printf("\r[%s] %7.1f Hz vol %3d%% %-8s snap %-10s tr %+3d%s%s%s%s   ",
                        raw ? (gyro.isCalibrating() ? "calib" : "gyro ") : "stick",
                        synth.frequency, Math.round(volume * 100), waveform, SNAP_NAMES[snapMode], transpose,
                        echo ? " echo" : "", crusher ? " crush" : "", mute ? " mute" : "",
                        buttons.contains(Button.CIRCLE) ? " vib" : "");
            }

            Thread.sleep(10);
        }

        synth.running = false;
        audio.join();
        if (ledEnabled) {
            try {
                ((AbstractController) controller).output(new Report5());
            } catch (Exception ignored) {
            }
        }
        controller.close();
    }

    /**
     * @param args [controller name substring]
     */
    public static void main(String[] args) throws Exception {
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        System.out.println("JInput version: " + Version.getVersion());

        Controller target = null;
        for (Controller controller : ce.getControllers()) {
            if (args.length > 0) {
                if (controller.getName().toLowerCase().contains(args[0].toLowerCase())) {
                    target = controller;
                    break;
                }
            } else if (isDualShock4(controller) || controller.getName().contains("Wireless Controller")) {
                target = controller;
                break;
            } else if (target == null && controller.getType() == Controller.Type.GAMEPAD) {
                target = controller;
            }
        }
        if (target == null) {
            System.err.println("no DualShock 4 found");
            System.exit(1);
        }
        System.out.println("using: " + target.getName());

        new GyroThereminTest(target).play();
        System.exit(0);
    }
}
