/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

/**
 * A pad that is not there, so that the tester has something to show with nothing plugged in.
 * <p>
 * The sticks wander round two circles at different rates and the buttons are pressed one
 * after another, the triggers rolling on and off rather than snapping, which is enough to
 * see that every readout and every part of the drawing is wired to the right index.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class DemoGamepadSource implements GamepadSource {

    /** seconds one button stays down */
    private static final double STEP = 0.4;

    private final GamepadState state = new GamepadState();
    private final long started = System.nanoTime();

    private double t;
    private long last = System.nanoTime();

    public DemoGamepadSource() {
        state.setId("Demo Gamepad (STANDARD GAMEPAD)");
        state.setMapping(GamepadState.STANDARD);
        state.setConnected(true);
        state.setVibration(false);
        state.resize(GamepadState.STANDARD_BUTTONS, 4);
    }

    @Override
    public GamepadState poll() {
        long now = System.nanoTime();
        step(Math.min(0.05, (now - last) / 1e9));
        last = now;
        state.setTimestamp((now - started) / 1e9);
        return state;
    }

    /** advances by an exact step instead of by the wall clock, for tests */
    public void step(double dt) {
        t += dt;

        state.setAxis(0, Math.sin(t * 1.1));
        state.setAxis(1, Math.cos(t * 1.1));
        state.setAxis(2, Math.sin(t * 0.7 + 1));
        state.setAxis(3, Math.cos(t * 0.9));

        int lit = (int) (t / STEP) % GamepadState.STANDARD_BUTTONS;
        for (int i = 0; i < GamepadState.STANDARD_BUTTONS; i++) {
            // the triggers are analogue, so they are ramped rather than switched
            double value = i != lit ? 0
                    : i == 6 || i == 7 ? (t % STEP) / STEP
                    : 1;
            state.setButton(i, value);
        }
    }

    @Override
    public void close() {
    }
}
