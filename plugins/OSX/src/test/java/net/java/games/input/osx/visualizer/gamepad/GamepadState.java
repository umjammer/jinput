/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * One reading of a pad, shaped the way the browser's Gamepad API shapes it.
 * <p>
 * That shape is the whole point of the class: buttons and axes are plain arrays at the
 * standard indices, so the artwork can be written once against "button 3 is the top face
 * button" and never has to know which plugin, which report layout or which vendor the
 * reading came from. Mapping a real device onto it is the source's job, see
 * {@link JinputGamepadSource}.
 * <p>
 * The standard mapping is:
 * <pre>
 * buttons  0 bottom face  1 right face   2 left face    3 top face
 *          4 L1           5 R1           6 L2           7 R2
 *          8 select       9 start       10 L3          11 R3
 *         12 d-pad up    13 down       14 left         15 right
 *         16 home        17 touch pad
 * axes     0 left X       1 left Y       2 right X       3 right Y
 * </pre>
 * Buttons are {@code 0 ..= 1}, so an analogue trigger reads part way, and axes are
 * {@code -1 ..= 1} with up and left negative.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class GamepadState {

    /** the mapping name a pad laid out as above reports */
    public static final String STANDARD = "standard";

    /** how many buttons the standard mapping names, the last one being the touch pad */
    public static final int STANDARD_BUTTONS = 18;

    /** splits {@code "Wireless Controller (STANDARD GAMEPAD Vendor: 054c)"} in two */
    private static final Pattern ID = Pattern.compile("([^(]+)(?:\\s*\\(([^)]+)\\))?");

    private String id = "";
    private int index;
    private boolean connected;
    private String mapping = "";
    private double timestamp;
    private boolean vibration;
    private double[] axes = new double[0];
    private double[] buttons = new double[0];

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? "" : id;
    }

    /** the device name, the part of the id before the bracket */
    public String getBigLabel() {
        Matcher m = ID.matcher(id);
        return m.lookingAt() ? m.group(1).trim() : id;
    }

    /** what was in the bracket, {@code null} when there was nothing */
    public String getSmallLabel() {
        Matcher m = ID.matcher(id);
        return m.lookingAt() && m.group(2) != null ? m.group(2).trim() : null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping == null ? "" : mapping;
    }

    /** seconds since the source started, when this reading was taken */
    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasVibration() {
        return vibration;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }

    public int getAxisCount() {
        return axes.length;
    }

    public double getAxis(int index) {
        return index < axes.length ? axes[index] : 0;
    }

    public void setAxis(int index, double value) {
        if (index < axes.length) axes[index] = value;
    }

    public int getButtonCount() {
        return buttons.length;
    }

    public double getButton(int index) {
        return index < buttons.length ? buttons[index] : 0;
    }

    public void setButton(int index, double value) {
        if (index < buttons.length) buttons[index] = value;
    }

    /** sizes the arrays, discarding whatever was in them */
    public void resize(int buttonCount, int axisCount) {
        buttons = new double[buttonCount];
        axes = new double[axisCount];
    }

    /** {@code true} when the pad claims the standard button and axis order */
    public boolean isStandardMapping() {
        return STANDARD.equals(mapping);
    }

    /**
     * Whether there is enough here to draw a pad at all: a device with a handful of buttons
     * and one axis is a stick or a wheel, and the standard artwork would be a lie.
     */
    public boolean isStandardShape() {
        return buttons.length >= 16 && axes.length == 4;
    }
}
