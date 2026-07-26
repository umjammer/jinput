/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.io.IOException;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;
import net.java.games.input.Rumbler;
import net.java.games.input.plugin.DualShock4PluginBase.Report5;
import net.java.games.input.usb.HidComponent;
import net.java.games.input.usb.HidController;

import static java.lang.System.getLogger;


/**
 * A jinput {@link Controller} read as a standard gamepad.
 * <p>
 * jinput hands over what the device declares, in the order the device declares it. The
 * browser's Gamepad API hands over a fixed layout, which is what the artwork is drawn
 * against, so something has to sit in between and say which of these buttons is the one
 * under the player's right thumb. That is all this class is.
 * <p>
 * For a DualShock the answer is known: the report puts square, cross, circle and triangle in
 * that order, which is not the standard order, the hat is one axis rather than four buttons,
 * and the triggers are on {@code RX} and {@code RY} as well as being buttons, so the
 * analogue value is worth preferring. For anything else the buttons are taken in the order
 * they arrive, which is what a browser does with a pad it has no table for, and the mapping
 * is reported as unknown so that the tester says so rather than pretending.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class JinputGamepadSource implements GamepadSource {

    private static final Logger logger = getLogger(JinputGamepadSource.class.getName());

    private static final int SONY = 0x054c;

    /**
     * Where each of a DualShock's buttons belongs in the standard layout: square, cross,
     * circle, triangle, L1, R1, L2, R2, share, options, L3, R3, PS, touch pad.
     */
    private static final int[] DUALSHOCK_BUTTONS = {2, 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 17};

    /** which way the hat has to be pushed for each of the four d-pad buttons, up first */
    private static final float[][] POV_FOR_DPAD = {
            {Component.POV.UP_LEFT, Component.POV.UP, Component.POV.UP_RIGHT},
            {Component.POV.DOWN_RIGHT, Component.POV.DOWN, Component.POV.DOWN_LEFT},
            {Component.POV.DOWN_LEFT, Component.POV.LEFT, Component.POV.UP_LEFT},
            {Component.POV.UP_RIGHT, Component.POV.RIGHT, Component.POV.DOWN_RIGHT}};

    private final Controller controller;
    private final GamepadState state = new GamepadState();
    private final long started = System.nanoTime();

    /** the device's own buttons, in the order it declared them */
    private final List<Component> buttons = new ArrayList<>();

    private final Component[] axes = new Component[4];

    /** the hat, when the pad has one instead of four d-pad buttons */
    private Component pov;

    /** the analogue triggers, when they are on their own axes */
    private Component leftTrigger, rightTrigger;

    private final boolean dualShock;
    private boolean alive = true;

    /** kept so that the rumble can be turned off again */
    private final Report5 report = new Report5();

    /**
     * @throws IOException the controller could not be opened
     */
    public JinputGamepadSource(Controller controller) throws IOException {
        this.controller = controller;

        int vendorId = controller instanceof HidController hid ? hid.getVendorId() : 0;
        int productId = controller instanceof HidController hid ? hid.getProductId() : 0;
        this.dualShock = vendorId == SONY;

        bind();

        int buttonCount = dualShock ? GamepadState.STANDARD_BUTTONS
                : Math.max(buttons.size(), pov != null ? 16 : 0);
        state.resize(buttonCount, axisCount());
        state.setConnected(true);
        state.setMapping(state.isStandardShape() && dualShock ? GamepadState.STANDARD : "");
        state.setId(id(vendorId, productId));
        state.setVibration(rumblers().length > 0);

        if (!(controller instanceof PollingController)) {
            // an event driven plugin only refreshes its components while it is open
            controller.open();
        }
logger.log(Logger.Level.DEBUG, state.getId() + ": " + buttons.size() + " buttons, "
        + axisCount() + " axes, hat " + (pov != null));
    }

    /** reads like the browser's, which is what the title and the subtitle are cut out of */
    private String id(int vendorId, int productId) {
        StringBuilder sb = new StringBuilder(controller.getName());
        List<String> inBrackets = new ArrayList<>();
        if (state.isStandardMapping()) inBrackets.add("STANDARD GAMEPAD");
        if (vendorId != 0) inBrackets.add(String.format("Vendor: %04x Product: %04x", vendorId, productId));
        if (!inBrackets.isEmpty()) sb.append(" (").append(String.join(" ", inBrackets)).append(")");
        return sb.toString();
    }

    /** picks out the components this cares about, leaving the rest alone */
    private void bind() {
        for (Component c : controller.getComponents()) {
            Component.Identifier id = c.getIdentifier();
            if (id instanceof Component.Identifier.Button) {
                buttons.add(c);
            } else if (id == Component.Identifier.Axis.X) {
                axes[0] = c;
            } else if (id == Component.Identifier.Axis.Y) {
                axes[1] = c;
            } else if (id == Component.Identifier.Axis.Z) {
                axes[2] = c;
            } else if (id == Component.Identifier.Axis.RZ) {
                axes[3] = c;
            } else if (id == Component.Identifier.Axis.POV) {
                pov = c;
            } else if (id == Component.Identifier.Axis.RX) {
                leftTrigger = c;
            } else if (id == Component.Identifier.Axis.RY) {
                rightTrigger = c;
            }
        }

        // a pad without Z and RZ puts its right stick on RX and RY, and then those are not
        // triggers after all
        if (axes[2] == null && axes[3] == null && leftTrigger != null && rightTrigger != null) {
            axes[2] = leftTrigger;
            axes[3] = rightTrigger;
            leftTrigger = rightTrigger = null;
        }
    }

    private int axisCount() {
        int count = 0;
        for (int i = 0; i < axes.length; i++) {
            if (axes[i] != null) count = i + 1;
        }
        return count;
    }

    @Override
    public GamepadState poll() {
        if (controller instanceof PollingController pc) {
            try {
                // The return value is deliberately ignored. On the IOKit plugin an empty
                // event queue comes back as kIOReturnUnderrun, which OSXHIDQueue means to
                // treat as "nothing waiting" but does not recognise, because the constant in
                // IOKitLib is missing the 0xe0000000 system field and so reads 0x2e7 rather
                // than 0xe00002e7. The queue read is turned into an IOException, poll says
                // false, and a pad sitting still looks unplugged. The component values are
                // read straight off the device rather than out of that queue, so they are
                // right either way, and this reads them and carries on.
                pc.poll();
            } catch (Exception e) {
logger.log(Logger.Level.DEBUG, "polling " + controller.getName(), e);
                alive = false;
                return state;
            }
        }

        for (int i = 0; i < axisCount(); i++) {
            state.setAxis(i, value(axes[i]));
        }

        int[] map = dualShock ? DUALSHOCK_BUTTONS : null;
        for (int i = 0; i < buttons.size(); i++) {
            int index = map == null ? i : i < map.length ? map[i] : -1;
            if (index >= 0 && index < state.getButtonCount()) {
                state.setButton(index, value(buttons.get(i)));
            }
        }

        // the analogue trigger rests at -1 and is worth more than the button next to it
        if (leftTrigger != null) state.setButton(6, (value(leftTrigger) + 1) / 2);
        if (rightTrigger != null) state.setButton(7, (value(rightTrigger) + 1) / 2);

        if (pov != null) {
            double hat = value(pov);
            for (int i = 0; i < POV_FOR_DPAD.length; i++) {
                boolean pushed = false;
                for (float direction : POV_FOR_DPAD[i]) {
                    pushed |= Math.abs(hat - direction) < 1e-3;
                }
                state.setButton(12 + i, pushed ? 1 : 0);
            }
        }

        state.setTimestamp((System.nanoTime() - started) / 1e9);
        return state;
    }

    /** the plugins differ: one is polled and read back, the other pushes and is read live */
    private double value(Component c) {
        if (c == null) return 0;
        if (c instanceof PollingComponent pc) return pc.getPollData();
        if (c instanceof HidComponent hc) return hc.getValue();
        return 0;
    }

    /** the two motors of a DualShock, the LEDs on the same report being left alone */
    private Rumbler[] rumblers() {
        if (!dualShock || !(controller instanceof AbstractController)) return new Rumbler[0];
        return Arrays.stream(controller.getRumblers())
                .filter(r -> r.getOutputName().toLowerCase(Locale.ROOT).contains("rumble"))
                .toArray(Rumbler[]::new);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only a DualShock is shaken, because the output report that carries the motor values is
     * that device's own. A pad the plugin has no report class for reports no vibration and
     * the tester hides the buttons for it, exactly as the browser does.
     */
    @Override
    public void rumble(double magnitude) {
        if (!state.hasVibration()) return;

        int value = (int) Math.round(Math.max(0, Math.min(1, magnitude)) * 255);
        report.smallRumble = value;
        report.bigRumble = value;
        try {
            ((AbstractController) controller).output(report);
        } catch (IOException e) {
logger.log(Logger.Level.DEBUG, "rumbling " + controller.getName(), e);
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void close() {
        try {
            rumble(0);
            if (!(controller instanceof PollingController)) {
                controller.close();
            }
        } catch (Exception e) {
logger.log(Logger.Level.DEBUG, "closing " + controller.getName(), e);
        }
    }
}
