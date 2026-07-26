/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;


/**
 * A pad drawn from nothing but the number of buttons and axes it has.
 * <p>
 * {@link StandardGamepadArt} can only be right about a device that really is laid out the
 * standard way. A stick, a wheel, a fight pad or anything else the plugin hands over with
 * some other count would come out as a picture of a pad that is not the one on the desk.
 * This draws what is actually there instead: every pair of axes becomes a stick, an odd one
 * over becomes a slider, and the buttons become numbered discs that fill in when held.
 * <p>
 * It is also the worked example for {@link GamepadArt}: about a hundred lines, no path data,
 * and enough to see that the panel does not care what a pad looks like.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class GenericGamepadArt implements GamepadArt {

    private static final double WIDTH = 441, HEIGHT = 403;

    /** the body, roughly where the standard art puts its own */
    private static final Rectangle2D.Double BODY = new Rectangle2D.Double(20, 92, 401, 250);

    private static final double STROKE = 3;
    private static final double RING_RADIUS = 37.5, STICK_RADIUS = 28, STICK_TRAVEL = 12;
    private static final double DISC_RADIUS = 13;

    /** buttons per row, before wrapping */
    private static final int PER_ROW = 8;

    @Override
    public String getName() {
        return "generic";
    }

    /** the fallback: it claims anything, and is only reached when nothing better does */
    @Override
    public boolean supports(GamepadState state) {
        return true;
    }

    @Override
    public Rectangle2D.Double getDesignBounds() {
        return new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
    }

    @Override
    public void paint(Graphics2D g, GamepadState state) {
        g.setStroke(new BasicStroke((float) STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        RoundRectangle2D.Double body =
                new RoundRectangle2D.Double(BODY.x, BODY.y, BODY.width, BODY.height, 90, 90);
        g.setColor(Theme.alpha(Theme.ART, 0.3));
        g.fill(AffineTransform.getTranslateInstance(0, 20).createTransformedShape(body));
        g.setColor(Color.WHITE);
        g.fill(body);
        g.setColor(Theme.ART);
        g.draw(body);

        int pairs = state.getAxisCount() / 2;
        double y = BODY.y + 70;
        for (int i = 0; i < pairs; i++) {
            double x = BODY.x + BODY.width * (i + 0.5) / Math.max(1, pairs);
            stick(g, x, y, state.getAxis(i * 2), state.getAxis(i * 2 + 1));
        }
        if (state.getAxisCount() % 2 == 1) {
            slider(g, BODY.x + 40, y + RING_RADIUS + 22, BODY.width - 80,
                    state.getAxis(state.getAxisCount() - 1));
        }

        buttons(g, state, y + RING_RADIUS + 46);
    }

    private void stick(Graphics2D g, double cx, double cy, double x, double y) {
        g.setColor(Theme.ART);
        g.draw(circle(cx, cy, RING_RADIUS));

        double px = cx + x * STICK_TRAVEL;
        double py = cy + y * STICK_TRAVEL;
        g.setColor(Theme.alpha(Color.BLACK, Math.max(0, -0.2 + 1.2 * Math.hypot(x, y))));
        g.fill(circle(px, py, STICK_RADIUS));
        g.setColor(Color.BLACK);
        g.draw(circle(px, py, STICK_RADIUS));
    }

    /** a bipolar bar, for the axis left over when there is an odd number of them */
    private void slider(Graphics2D g, double x, double y, double width, double value) {
        double height = 10;
        g.setColor(Theme.BAR_BG);
        g.fill(new RoundRectangle2D.Double(x, y, width, height, height, height));
        g.setColor(Theme.AXES);
        double middle = x + width / 2;
        double w = Math.abs(value) * width / 2;
        g.fill(new RoundRectangle2D.Double(value < 0 ? middle - w : middle, y, w, height, height, height));
    }

    private void buttons(Graphics2D g, GamepadState state, double top) {
        int count = state.getButtonCount();
        if (count == 0) return;

        Font font = Theme.font((float) DISC_RADIUS);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        double pitch = DISC_RADIUS * 2 + 10;
        for (int i = 0; i < count; i++) {
            int row = i / PER_ROW;
            int column = i % PER_ROW;
            int inRow = Math.min(PER_ROW, count - row * PER_ROW);
            double cx = BODY.x + BODY.width / 2 + (column - (inRow - 1) / 2.0) * pitch;
            double cy = top + row * pitch;
            double value = state.getButton(i);

            g.setColor(Theme.alpha(Color.BLACK, value));
            g.fill(circle(cx, cy, DISC_RADIUS));
            g.setColor(Color.BLACK);
            g.draw(circle(cx, cy, DISC_RADIUS));

            String label = String.valueOf(i);
            g.setColor(value > 0.5 ? Color.WHITE : Theme.LABEL);
            g.drawString(label, (float) (cx - fm.stringWidth(label) / 2.0),
                    (float) (cy + fm.getAscent() / 2.0 - 1));
        }
    }

    private static Ellipse2D.Double circle(double cx, double cy, double r) {
        return new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
    }
}
