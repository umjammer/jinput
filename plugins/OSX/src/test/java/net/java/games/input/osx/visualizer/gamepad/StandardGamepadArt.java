/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * A pad with two sticks, a d-pad and four face buttons, drawn the way
 * <a href="https://hardwaretester.com/gamepad">hardwaretester.com/gamepad</a> draws it.
 * <p>
 * The geometry is that page's own: the outline, the d-pad petals and the trigger are the
 * path data out of its SVG, at its 441 x 403 view box and its 3 unit stroke, so this comes
 * out dot for dot rather than merely alike. Only the drawing is borrowed, this reads a
 * jinput device rather than the browser's Gamepad API.
 * <p>
 * Two arrangements exist because pads disagree about which corner the left stick lives in.
 * A d-pad above the stick is the Xbox arrangement, the other way round is PlayStation's.
 * The four rings never move, only what sits inside the top left and bottom left ones
 * swaps, which is exactly what the page does. Note that a DualShock reporting itself as
 * plain {@code "Wireless Controller"} is drawn Xbox style, again as on the page: it goes by
 * the name the device gives, not by its vendor id.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class StandardGamepadArt implements GamepadArt {

    /** which corner the left stick sits in */
    public enum Layout {
        /** d-pad below the left stick */
        XBOX,
        /** d-pad above the left stick */
        PLAYSTATION
    }

    /** pads that name themselves after Sony's, and so are drawn with the stick underneath */
    private static final Pattern PLAYSTATION_ID = Pattern.compile("playstation|dualsense|dualshock");

    // the SVG, verbatim. viewBox 0 0 441 403, stroke width 3.

    private static final String BODY =
            "M220.5 92.0001C200.5 92.0001 154 92.0001 128 92.0001C95.5 92.0001 66.5 109.5 55 137.5C43.5 165.5 4 271.1 4 317.5C4 363.9 17.5 378.5 49.5 378.5C81.5 378.5 105 294.5 150 294.5C195 294.5 220.5 294.5 220.5 294.5C220.5 294.5 245.5 294.5 290.5 294.5C335.5 294.5 359 378.5 391 378.5C423 378.5 436.5 363.9 436.5 317.5C436.5 271.1 397 165.5 385.5 137.5C374 109.5 345 92.0001 312.5 92.0001C286.5 92.0001 240 92.0001 220.5 92.0001Z";

    /** the four petals of the d-pad, up, down, left, right, at {@link #DPAD} */
    private static final String[] PETALS = {
            "M177.669 222.335C180.793 219.21 180.816 213.997 176.868 212.014C176.327 211.743 175.776 211.491 175.215 211.258C172.182 210.002 168.931 209.355 165.648 209.355C162.365 209.355 159.114 210.002 156.081 211.258C155.521 211.491 154.969 211.743 154.429 212.014C150.48 213.997 150.503 219.21 153.627 222.335L159.991 228.698C163.116 231.823 168.181 231.823 171.305 228.698L177.669 222.335Z",
            "M154.113 253.447C150.989 256.571 150.966 261.785 154.914 263.767C155.455 264.039 156.006 264.291 156.566 264.523C159.6 265.78 162.85 266.426 166.134 266.426C169.417 266.426 172.667 265.78 175.701 264.523C176.261 264.291 176.812 264.039 177.353 263.767C181.301 261.785 181.279 256.571 178.154 253.447L171.79 247.083C168.666 243.959 163.601 243.959 160.477 247.083L154.113 253.447Z",
            "M150.335 226.113C147.21 222.989 141.997 222.966 140.014 226.914C139.743 227.455 139.491 228.006 139.258 228.566C138.002 231.6 137.355 234.85 137.355 238.134C137.355 241.417 138.002 244.667 139.258 247.701C139.491 248.261 139.743 248.812 140.014 249.353C141.997 253.301 147.21 253.279 150.335 250.154L156.698 243.79C159.823 240.666 159.823 235.601 156.698 232.477L150.335 226.113Z",
            "M181.447 249.669C184.571 252.793 189.785 252.816 191.768 248.868C192.039 248.327 192.291 247.776 192.523 247.215C193.78 244.182 194.426 240.931 194.426 237.648C194.426 234.365 193.78 231.114 192.523 228.081C192.291 227.521 192.039 226.969 191.768 226.429C189.785 222.48 184.571 222.503 181.447 225.627L175.083 231.991C171.959 235.116 171.959 240.181 175.083 243.305L181.447 249.669Z"};

    private static final String LEFT_TRIGGER =
            "M152.5 37C152.5 41.1421 149.142 44.5 145 44.5H132C127.858 44.5 124.5 41.1421 124.5 37V16.5C124.5 8.76801 130.768 2.5 138.5 2.5C146.232 2.5 152.5 8.76801 152.5 16.5V37Z";

    private static final String RIGHT_TRIGGER =
            "M317.5 37C317.5 41.1421 314.142 44.5 310 44.5H297C292.858 44.5 289.5 41.1421 289.5 37V16.5C289.5 8.76801 295.768 2.5 303.5 2.5C311.232 2.5 317.5 8.76801 317.5 16.5V37Z";

    /** the ring in each corner, top left, bottom left, bottom right, top right */
    private static final Point2D.Double LEFT_STICK = new Point2D.Double(113, 160);
    private static final Point2D.Double DPAD = new Point2D.Double(166, 238);
    private static final Point2D.Double RIGHT_STICK = new Point2D.Double(278, 238);
    private static final Point2D.Double FACE = new Point2D.Double(329, 160);

    private static final double STROKE = 3;
    private static final double RING_RADIUS = 37.5;
    private static final double STICK_RADIUS = 28;
    private static final double STICK_DOT_RADIUS = 20;

    /** how far off centre a stick is drawn at full deflection */
    private static final double STICK_TRAVEL = 12;

    private static final double META_RADIUS = 10;

    /** the deflection at which a stick starts to darken, and how fast it does */
    private static final double SHADE_OFFSET = -0.2, SHADE_SCALE = 1.2;

    private final Layout layout;

    private final Path2D.Double body = SvgPath.parse(BODY);
    private final Path2D.Double leftTrigger = SvgPath.parse(LEFT_TRIGGER);
    private final Path2D.Double rightTrigger = SvgPath.parse(RIGHT_TRIGGER);
    private final Path2D.Double[] petals = new Path2D.Double[PETALS.length];

    public StandardGamepadArt(Layout layout) {
        this.layout = layout;
        for (int i = 0; i < PETALS.length; i++) {
            petals[i] = SvgPath.parse(PETALS[i]);
        }
    }

    @Override
    public String getName() {
        return "standard gamepad, " + layout.name().toLowerCase(Locale.ROOT) + " layout";
    }

    @Override
    public boolean supports(GamepadState state) {
        if (!state.isStandardShape()) return false;
        boolean sony = PLAYSTATION_ID.matcher(state.getId().toLowerCase(Locale.ROOT)).find();
        return sony == (layout == Layout.PLAYSTATION);
    }

    @Override
    public Rectangle2D.Double getDesignBounds() {
        return new Rectangle2D.Double(0, 0, 441, 403);
    }

    /** where the left stick is drawn, the d-pad taking whichever ring is left over */
    private Point2D.Double stickAt() {
        return layout == Layout.PLAYSTATION ? DPAD : LEFT_STICK;
    }

    private Point2D.Double dpadAt() {
        return layout == Layout.PLAYSTATION ? LEFT_STICK : DPAD;
    }

    @Override
    public void paint(Graphics2D g, GamepadState state) {
        g.setStroke(new BasicStroke((float) STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // the body, and the same shape again underneath it as its shadow
        g.setColor(Theme.alpha(Theme.ART, 0.3));
        g.fill(AffineTransform.getTranslateInstance(0, 20).createTransformedShape(body));
        g.setColor(Color.WHITE);
        g.fill(body);
        g.setColor(Theme.ART);
        g.draw(body);

        // the grips, two strokes hinting at where the pad is held
        g.setColor(Theme.alpha(Theme.ART, 0.3));
        g.draw(new Line2D.Double(30, 210, 130, 300));
        g.draw(new Line2D.Double(411, 210, 311, 300));

        g.setColor(Theme.ART);
        for (Point2D.Double ring : new Point2D.Double[] {LEFT_STICK, RIGHT_STICK, DPAD, FACE}) {
            g.draw(circle(ring.x, ring.y, RING_RADIUS));
        }

        stick(g, stickAt(), state.getAxis(0), state.getAxis(1), state.getButton(10));
        stick(g, RIGHT_STICK, state.getAxis(2), state.getAxis(3), state.getButton(11));

        // up, down, left, right, then the face buttons, which are the same four shapes
        Shape[] dpad = petals(dpadAt());
        double[] dpadValues = {
                state.getButton(12), state.getButton(13), state.getButton(14), state.getButton(15)};
        Shape[] face = petals(FACE);
        double[] faceValues = {
                state.getButton(3), state.getButton(0), state.getButton(2), state.getButton(1)};
        for (int i = 0; i < dpad.length; i++) {
            petal(g, dpad[i], dpadValues[i]);
            petal(g, face[i], faceValues[i]);
        }

        pressable(g, circle(185, 162, META_RADIUS), state.getButton(8));
        pressable(g, circle(259, 162, META_RADIUS), state.getButton(9));

        pressable(g, new RoundRectangle2D.Double(111.5, 61.5, 41, 13, 13, 13), state.getButton(4));
        pressable(g, new RoundRectangle2D.Double(289.5, 61.5, 41, 13, 13, 13), state.getButton(5));

        pressable(g, leftTrigger, state.getButton(6));
        pressable(g, rightTrigger, state.getButton(7));
    }

    /** the d-pad shapes, moved to wherever the d-pad is drawn for this layout */
    private Shape[] petals(Point2D.Double centre) {
        AffineTransform at = AffineTransform.getTranslateInstance(centre.x - DPAD.x, centre.y - DPAD.y);
        Shape[] shapes = new Shape[petals.length];
        for (int i = 0; i < petals.length; i++) {
            shapes[i] = at.createTransformedShape(petals[i]);
        }
        return shapes;
    }

    /**
     * A stick, drawn where it is pushed and shaded by how far. The inner disc is the stick
     * button: pressing it fills black and rings the fill in white.
     */
    private void stick(Graphics2D g, Point2D.Double home, double x, double y, double pressed) {
        double cx = home.x + x * STICK_TRAVEL;
        double cy = home.y + y * STICK_TRAVEL;
        double shade = Math.max(0, SHADE_OFFSET + SHADE_SCALE * Math.hypot(x, y));

        g.setColor(Theme.alpha(Color.BLACK, shade));
        g.fill(circle(cx, cy, STICK_RADIUS));
        g.setColor(Color.BLACK);
        g.draw(circle(cx, cy, STICK_RADIUS));

        g.setColor(Theme.alpha(Color.BLACK, pressed));
        g.fill(circle(cx, cy, STICK_DOT_RADIUS));
        g.setColor(Theme.alpha(Color.WHITE, pressed));
        g.draw(circle(cx, cy, STICK_DOT_RADIUS));
    }

    /** a button, black when held and outlined when not */
    private void pressable(Graphics2D g, Shape shape, double value) {
        g.setColor(Theme.alpha(Color.BLACK, value));
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
    }

    /**
     * The d-pad and the face buttons, which the page strokes from the inside instead: it
     * masks a double width stroke with the shape itself, so that the petals stay the size
     * they were laid out at and keep the gaps between them. Clipping does the same thing.
     */
    private void petal(Graphics2D g, Shape shape, double value) {
        Shape clip = g.getClip();
        g.clip(shape);
        g.setColor(Theme.alpha(Color.BLACK, value));
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke((float) (STROKE * 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
        g.setStroke(new BasicStroke((float) STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setClip(clip);
    }

    private static Ellipse2D.Double circle(double cx, double cy, double r) {
        return new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
    }
}
