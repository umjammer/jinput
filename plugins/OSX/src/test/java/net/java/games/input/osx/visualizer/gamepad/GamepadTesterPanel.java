/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;


/**
 * The gamepad tester, laid out like the page it is copied from.
 * <p>
 * Everything is placed in the coordinates of
 * <a href="https://hardwaretester.com/gamepad">hardwaretester.com/gamepad</a> at its own
 * size, 981 by 495, measured off the page itself: the readouts are 45 wide with a 5 wide bar
 * and 10 of white to its right, they wrap at 520, the joystick is a 120 box with its circle
 * at 95%, and so on down to the 41 between one row of buttons and the next. The panel then
 * scales that box into whatever room the window gives it, so the whole thing stays dot for
 * dot at its natural size and merely bigger anywhere else.
 * <p>
 * The controller itself is not drawn here. That is a {@link GamepadArt}, picked per device,
 * see {@link GamepadArts}.
 * <p>
 * Clicking works where the page works: <b>Test Circularity</b> turns on the sweep overlay
 * that colours how round a stick's travel really is, and the vibration buttons drive the
 * motors, if the pad has any. From the keyboard, {@code A} steps through the registered
 * artwork, {@code D} looks for the device again and {@code SPACE} freezes the readings.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class GamepadTesterPanel extends JPanel {

    /** the page's own size, which every coordinate below is in */
    public static final double DESIGN_WIDTH = 981, DESIGN_HEIGHT = 495;

    /** the left edge of the column */
    private static final double X0 = 21.5;

    private static final double TITLE_BASELINE = 37.5;
    private static final double SUBTITLE_BASELINE = 64;

    /** top of the first readout of each block */
    private static final double INFO_TOP = 92;
    private static final double BUTTONS_TOP = 152;

    /** one row of readouts to the next */
    private static final double ROW_PITCH = 41;

    /** baselines inside a readout, from its top */
    private static final double LABEL_BASELINE = 13.5, VALUE_BASELINE = 32.5;

    private static final double BAR_WIDTH = 5, BAR_HEIGHT = 35;

    /** the white the bar is edged with on its right, before the text starts */
    private static final double BAR_GAP = 5;

    private static final double BUTTON_READOUT_WIDTH = 45, AXIS_READOUT_WIDTH = 80;
    private static final double READOUT_GAP = 10, INFO_GAP = 20;

    /** the column wraps here, which is what puts nine buttons on a row */
    private static final double WRAP_WIDTH = 520;

    private static final double STICKS_TOP = 264;
    private static final double STICK_HEADING_BASELINE = 9;
    private static final double STICK_FIRST_READOUT = 20.5, STICK_READOUT_PITCH = 40.5;

    /** the joystick widget, its circle at 95% of it, and where it sits beside the readouts */
    private static final double JOYSTICK_BOX = 120, JOYSTICK_RADIUS = 57, JOYSTICK_DOT = 3.8;
    private static final double JOYSTICK_CENTRE = 49.25;

    /** the gap between the left stick block and the right one */
    private static final double STICK_BLOCK_GAP = 25;

    private static final double PILL_HEIGHT = 32, PILL_RADIUS = 5, PILL_PAD = 12;
    private static final double ICON_SIZE = 20, ICON_GAP = 10, PILL_GAP = 10;

    /** under a row of buttons, and again above the next row of them */
    private static final double PILL_MARGIN = 5, PILL_ROW_GAP = 10;

    /** where the artwork goes, and how wide it is drawn */
    private static final double ART_X = 576.6, ART_Y = 78.2, ART_WIDTH = 350;

    /** a stick has to be pushed this far before the circularity test believes it */
    private static final double CIRCULARITY_MINIMUM = 0.2;

    /** the sweep is remembered in sectors this wide */
    private static final double SECTOR = Math.PI / 16;

    /** one clickable thing on the page */
    private record Pill(Rectangle2D.Double bounds, Runnable action) {
    }

    /**
     * A button waiting to be drawn.
     *
     * @param icons space separated, see {@link #paintIcon}
     */
    private record PillSpec(String icons, String text, boolean active, Runnable action) {
    }

    /**
     * How round a stick's travel is: the furthest it has been pushed in each direction so
     * far. A stick with a square gate reaches further into the corners than along the axes,
     * and that shows up as the wedges being uneven.
     */
    private static final class Circularity {

        final double[] reach = new double[(int) Math.round(2 * Math.PI / SECTOR)];

        void record(double x, double y) {
            double radius = Math.hypot(x, y);
            if (radius <= CIRCULARITY_MINIMUM) return;

            int sector = Math.floorMod((int) Math.round(Math.atan2(y, x) / SECTOR), reach.length);
            reach[sector] = Math.max(reach[sector], radius);
        }

        void reset() {
            Arrays.fill(reach, 0);
        }

        /** the root mean square of how far each direction fell short of the unit circle */
        double error() {
            double sum = 0;
            int n = 0;
            for (double r : reach) {
                if (r == 0) continue;
                sum += (1 - r) * (1 - r);
                n++;
            }
            return n == 0 ? 0 : Math.sqrt(sum / n);
        }
    }

    private final Supplier<GamepadSource> sourceFactory;
    private final Circularity[] circularity = {new Circularity(), new Circularity()};
    private final List<Pill> pills = new ArrayList<>();

    private GamepadSource source;
    private GamepadState state;
    private final Timer timer;

    /** set from the keyboard to override what {@link GamepadArts} would have picked */
    private GamepadArt art;

    private boolean paused;
    private boolean showCircularity;
    private boolean rumbling;

    /** how the design box was placed last time it was painted, for hit testing */
    private AffineTransform placement = new AffineTransform();

    public GamepadTesterPanel(Supplier<GamepadSource> sourceFactory) {
        this.sourceFactory = sourceFactory;
        this.source = sourceFactory.get();
        this.state = source.poll();

        setPreferredSize(new Dimension((int) DESIGN_WIDTH, (int) DESIGN_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                click(e.getPoint());
            }
        });

        bindKey("A", "art", this::cycleArt);
        bindKey("D", "rescan", this::rescan);
        bindKey("SPACE", "pause", () -> paused = !paused);

        timer = new Timer(16, e -> {
            step();
            repaint();
        });
    }

    private void bindKey(String stroke, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
        source.close();
    }

    /** asks the factory for a source again, for when the pad was plugged in late */
    private void rescan() {
        source.close();
        source = sourceFactory.get();
        state = source.poll();
        for (Circularity c : circularity) {
            c.reset();
        }
    }

    /** steps through the registered artwork, and then back to whichever one fits */
    private void cycleArt() {
        List<GamepadArt> all = GamepadArts.all();
        int next = art == null ? 0 : all.indexOf(art) + 1;
        art = next >= all.size() ? null : all.get(next);
    }

    /** the art in use: the one chosen from the keyboard, or the one that claims the pad */
    public GamepadArt getArt() {
        return art != null ? art : GamepadArts.find(state);
    }

    private void step() {
        if (paused) return;

        state = source.poll();
        if (!source.isAlive()) {
            rescan();
            return;
        }
        if (showCircularity) {
            circularity[0].record(state.getAxis(0), state.getAxis(1));
            circularity[1].record(state.getAxis(2), state.getAxis(3));
        }
    }

    private void click(Point2D point) {
        Point2D p;
        try {
            p = placement.inverseTransform(point, null);
        } catch (NoninvertibleTransformException e) {
            return;
        }
        for (Pill pill : pills) {
            if (pill.bounds().contains(p)) {
                pill.action().run();
                repaint();
                return;
            }
        }
    }

    private void toggleCircularity() {
        showCircularity = !showCircularity;
        for (Circularity c : circularity) {
            c.reset();
        }
    }

    /** one second of both motors, the way the page's own button does it */
    private void rumbleOnce() {
        source.rumble(1);
        Timer stop = new Timer(1000, e -> {
            if (!rumbling) source.rumble(0);
        });
        stop.setRepeats(false);
        stop.start();
    }

    private void rumbleForever() {
        rumbling = !rumbling;
        source.rumble(rumbling ? 1 : 0);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            double scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
            placement = new AffineTransform();
            placement.translate((getWidth() - DESIGN_WIDTH * scale) / 2,
                    (getHeight() - DESIGN_HEIGHT * scale) / 2);
            placement.scale(scale, scale);
            g.transform(placement);

            pills.clear();
            paintPanel(g);
            paintArt(g);
        } finally {
            g.dispose();
        }
    }

    private void paintPanel(Graphics2D g) {
        g.setFont(Theme.boldFont(Theme.TITLE_SIZE));
        g.setColor(Theme.TEXT);
        g.drawString(state.getBigLabel(), (float) X0, (float) TITLE_BASELINE);

        String small = state.getSmallLabel();
        if (small != null) {
            g.setFont(Theme.font(Theme.BASE_SIZE));
            g.setColor(Theme.AXES);
            g.drawString(small, (float) X0, (float) SUBTITLE_BASELINE);
        }

        paintInfo(g);
        paintButtons(g);

        double y;
        if (state.isStandardMapping()) {
            paintStick(g, X0, 0, "L STICK", circularity[0]);
            paintStick(g, X0 + AXIS_READOUT_WIDTH + READOUT_GAP + JOYSTICK_BOX + STICK_BLOCK_GAP,
                    2, "R STICK", circularity[1]);
            y = STICKS_TOP + JOYSTICK_CENTRE + JOYSTICK_BOX / 2 + 15;
        } else {
            y = paintAxes(g) + 20;
        }

        y = paintPills(g, y, List.of(
                new PillSpec("toggle", "Test Circularity", showCircularity, this::toggleCircularity)));
        if (state.hasVibration()) {
            paintPills(g, y + PILL_ROW_GAP, List.of(
                    new PillSpec("wifi", "Vibration, 1 sec", false, this::rumbleOnce),
                    new PillSpec("toggle infinity", "Vibration, infinite", rumbling, this::rumbleForever)));
        }
    }

    /** the row of facts about the device, which have values but no bars */
    private void paintInfo(Graphics2D g) {
        String[][] entries = {
                {"INDEX", String.valueOf(state.getIndex())},
                {"CONNECTED", state.isConnected() ? "Yes" : "No"},
                {"MAPPING", state.getMapping().isEmpty() ? "n/a" : state.getMapping()},
                {"TIMESTAMP", format(state.getTimestamp(), 5)},
                {"VIBRATION", state.hasVibration() ? "Yes" : "n/a"}};

        double x = X0;
        for (String[] entry : entries) {
            readout(g, x, INFO_TOP, Double.POSITIVE_INFINITY, entry[0], entry[1], Double.NaN, 0, 0);
            x += Math.max(textWidth(g, entry[0], Theme.LABEL_SIZE),
                    textWidth(g, entry[1], Theme.BASE_SIZE)) + INFO_GAP;
        }
    }

    /** every button the pad has, wrapping the way the page's column does */
    private void paintButtons(Graphics2D g) {
        double pitch = BUTTON_READOUT_WIDTH + READOUT_GAP;
        int perRow = Math.max(1, (int) (WRAP_WIDTH / pitch));
        for (int i = 0; i < state.getButtonCount(); i++) {
            double x = X0 + (i % perRow) * pitch;
            double y = BUTTONS_TOP + (i / perRow) * ROW_PITCH;
            // a button fills its bar from the bottom up
            readout(g, x, y, BUTTON_READOUT_WIDTH, "B" + i, format(state.getButton(i), 2),
                    Math.abs(state.getButton(i)), 1, -1);
        }
    }

    /**
     * The axes on their own, for a pad that is not laid out the standard way and so has no
     * left stick and right stick to speak of.
     *
     * @return the bottom of the block
     */
    private double paintAxes(Graphics2D g) {
        double top = STICKS_TOP;
        double pitch = AXIS_READOUT_WIDTH + READOUT_GAP;
        int perRow = Math.max(1, (int) (WRAP_WIDTH / pitch));
        int rows = 1;
        for (int i = 0; i < state.getAxisCount(); i++) {
            double x = X0 + (i % perRow) * pitch;
            double y = top + (i / perRow) * ROW_PITCH;
            readout(g, x, y, AXIS_READOUT_WIDTH, "AXIS " + i, format(state.getAxis(i), 5),
                    state.getAxis(i), 0.5, 0.5);
            rows = i / perRow + 1;
        }
        return top + rows * ROW_PITCH;
    }

    /**
     * One stick: its two axes as readouts, and the dial beside them.
     *
     * @param axis the index of the stick's horizontal axis
     */
    private void paintStick(Graphics2D g, double x, int axis, String heading, Circularity sweep) {
        g.setFont(Theme.font(Theme.LABEL_SIZE));
        g.setColor(Theme.LABEL);
        g.drawString(heading, (float) x, (float) (STICKS_TOP + STICK_HEADING_BASELINE));

        for (int i = 0; i < 2; i++) {
            readout(g, x, STICKS_TOP + STICK_FIRST_READOUT + i * STICK_READOUT_PITCH,
                    AXIS_READOUT_WIDTH, "AXIS " + (axis + i), format(state.getAxis(axis + i), 5),
                    state.getAxis(axis + i), 0.5, 0.5);
        }

        double cx = x + AXIS_READOUT_WIDTH + READOUT_GAP + JOYSTICK_BOX / 2;
        double cy = STICKS_TOP + JOYSTICK_CENTRE;
        paintJoystick(g, cx, cy, state.getAxis(axis), state.getAxis(axis + 1), sweep);
    }

    private void paintJoystick(Graphics2D g, double cx, double cy, double x, double y, Circularity sweep) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Theme.JOYSTICK_AXES);
        g.draw(new Ellipse2D.Double(cx - JOYSTICK_RADIUS, cy - JOYSTICK_RADIUS,
                JOYSTICK_RADIUS * 2, JOYSTICK_RADIUS * 2));
        g.draw(new Line2D.Double(cx, cy - JOYSTICK_RADIUS, cx, cy + JOYSTICK_RADIUS));
        g.draw(new Line2D.Double(cx - JOYSTICK_RADIUS, cy, cx + JOYSTICK_RADIUS, cy));

        g.setColor(Theme.JOYSTICK_INDICATOR);
        g.draw(new Line2D.Double(cx, cy, cx + x * JOYSTICK_RADIUS, cy + y * JOYSTICK_RADIUS));
        g.fill(new Ellipse2D.Double(cx + x * JOYSTICK_RADIUS - JOYSTICK_DOT,
                cy + y * JOYSTICK_RADIUS - JOYSTICK_DOT, JOYSTICK_DOT * 2, JOYSTICK_DOT * 2));

        if (showCircularity) {
            paintCircularity(g, cx, cy, sweep);
        }
    }

    /** the sweep so far, one wedge per sector, red where the stick overshot and green short */
    private void paintCircularity(Graphics2D g, double cx, double cy, Circularity sweep) {
        for (int i = 0; i < sweep.reach.length; i++) {
            double r = sweep.reach[i];
            if (r == 0) continue;

            double angle = i * SECTOR;
            Path2D.Double wedge = new Path2D.Double();
            wedge.moveTo(cx, cy);
            wedge.lineTo(cx + JOYSTICK_RADIUS * Math.cos(angle - SECTOR / 2) * r,
                    cy + JOYSTICK_RADIUS * Math.sin(angle - SECTOR / 2) * r);
            wedge.lineTo(cx + JOYSTICK_RADIUS * Math.cos(angle + SECTOR / 2) * r,
                    cy + JOYSTICK_RADIUS * Math.sin(angle + SECTOR / 2) * r);
            wedge.closePath();

            g.setColor(Theme.alpha(reachColor(r), 0.5));
            g.fill(wedge);
        }

        g.setColor(Color.WHITE);
        g.setFont(Theme.boldFont(Theme.BASE_SIZE));
        drawCentred(g, "Avg Error:", cx, cy + 20);
        g.setFont(Theme.boldFont(24));
        drawCentred(g, format(sweep.error() * 100, 1) + "%", cx, cy + 40);
    }

    /** blue on the nose, green when the stick fell short, red when it went past */
    private static Color reachColor(double reach) {
        double f = Math.max(-1, Math.min(1, (reach - 1) * 5));
        Color blue = new Color(0x00, 0x33, 0xff);
        return f < 0 ? mix(blue, new Color(0x0c, 0xf2, 0x0c), -f)
                : mix(blue, new Color(0xf2, 0x0c, 0x0c), f);
    }

    private static Color mix(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /**
     * A label over a value, with the bar that shows the value at a glance beside it.
     *
     * @param width  what the readout was given, which the value is cut off at, the way the
     *               page cuts it off. Infinite for one that is only as wide as it needs
     * @param value  what the bar shows, {@link Double#NaN} for a readout that has no bar
     * @param centre where the bar grows from, as a fraction of its height
     * @param coef   how far the value moves it, and which way
     */
    private void readout(Graphics2D g, double x, double top, double width, String label, String text,
                         double value, double centre, double coef) {
        double textX = x;
        if (!Double.isNaN(value)) {
            g.setColor(Theme.BAR_BG);
            g.fill(new Rectangle2D.Double(x, top, BAR_WIDTH, BAR_HEIGHT));

            double from = centre * BAR_HEIGHT;
            double to = (value * coef + centre) * BAR_HEIGHT;
            g.setColor(Theme.AXES);
            g.fill(new Rectangle2D.Double(x, top + Math.min(from, to), BAR_WIDTH, Math.abs(to - from)));

            textX += BAR_WIDTH + BAR_GAP;
        }

        g.setFont(Theme.font(Theme.LABEL_SIZE));
        g.setColor(Theme.LABEL);
        g.drawString(label, (float) textX, (float) (top + LABEL_BASELINE));

        g.setFont(Theme.font(Theme.BASE_SIZE));
        g.setColor(Theme.TEXT);
        Shape clip = g.getClip();
        if (!Double.isInfinite(width)) {
            g.clip(new Rectangle2D.Double(textX, top, x + width - textX, BAR_HEIGHT));
        }
        g.drawString(text, (float) textX, (float) (top + VALUE_BASELINE));
        g.setClip(clip);
    }

    /**
     * A row of buttons, each one clickable.
     *
     * @return where the next row would start
     */
    private double paintPills(Graphics2D g, double top, List<PillSpec> specs) {
        double x = X0;
        for (PillSpec spec : specs) {
            x += paintPill(g, x, top, spec) + PILL_GAP;
        }
        return top + PILL_HEIGHT + PILL_MARGIN;
    }

    /** @return the width of the button that was drawn */
    private double paintPill(Graphics2D g, double x, double top, PillSpec spec) {
        g.setFont(Theme.font(Theme.BASE_SIZE));
        FontMetrics fm = g.getFontMetrics();
        String[] icons = spec.icons().split(" ");
        double width = PILL_PAD * 2 + icons.length * (ICON_SIZE + ICON_GAP) + fm.stringWidth(spec.text());

        g.setColor(spec.active() ? Theme.BUTTON_ACTIVE : Theme.BAR_BG);
        g.fill(new RoundRectangle2D.Double(x, top, width, PILL_HEIGHT, PILL_RADIUS * 2, PILL_RADIUS * 2));

        g.setColor(spec.active() ? Theme.BUTTON_ACTIVE_TEXT : Theme.BUTTON_TEXT);
        double iconX = x + PILL_PAD;
        for (String icon : icons) {
            paintIcon(g, icon, iconX, top + PILL_HEIGHT / 2, spec.active());
            iconX += ICON_SIZE + ICON_GAP;
        }
        g.drawString(spec.text(), (float) iconX, (float) (top + PILL_HEIGHT / 2 + fm.getAscent() / 2f - 1));

        pills.add(new Pill(new Rectangle2D.Double(x, top, width, PILL_HEIGHT), spec.action()));
        return width;
    }

    /** stand-ins for the icons on the page, drawn rather than pulled from a font */
    private void paintIcon(Graphics2D g, String icon, double x, double cy, boolean on) {
        g.setStroke(new BasicStroke(1.5f));
        switch (icon) {
            case "toggle" -> {
                double h = 11;
                g.draw(new RoundRectangle2D.Double(x, cy - h / 2, ICON_SIZE, h, h, h));
                double r = 3.5;
                double dx = on ? ICON_SIZE - r - 2.5 : r + 2.5;
                g.fill(new Ellipse2D.Double(x + dx - r, cy - r, r * 2, r * 2));
            }
            case "wifi" -> {
                for (int i = 1; i <= 2; i++) {
                    double r = i * 5.5;
                    g.draw(new Arc2D.Double(x + ICON_SIZE / 2 - r, cy - r + 4,
                            r * 2, r * 2, 45, 90, Arc2D.OPEN));
                }
                g.fill(new Ellipse2D.Double(x + ICON_SIZE / 2 - 1.5, cy + 2.5, 3, 3));
            }
            case "infinity" -> {
                double r = 4.5;
                g.draw(new Ellipse2D.Double(x + 1, cy - r, r * 2, r * 2));
                g.draw(new Ellipse2D.Double(x + ICON_SIZE - 1 - r * 2, cy - r, r * 2, r * 2));
            }
            default -> {
            }
        }
    }

    /** hands the box on the right to whichever art claims this pad */
    private void paintArt(Graphics2D g) {
        GamepadArt chosen = getArt();
        Rectangle2D.Double design = chosen.getDesignBounds();

        Graphics2D gg = (Graphics2D) g.create();
        try {
            double scale = ART_WIDTH / design.width;
            gg.translate(ART_X, ART_Y);
            gg.scale(scale, scale);
            gg.translate(-design.x, -design.y);
            chosen.paint(gg, state);
        } finally {
            gg.dispose();
        }
    }

    private void drawCentred(Graphics2D g, String text, double cx, double y) {
        g.drawString(text, (float) (cx - g.getFontMetrics().stringWidth(text) / 2.0), (float) y);
    }

    private double textWidth(Graphics2D g, String text, float size) {
        return g.getFontMetrics(Theme.font(size)).stringWidth(text);
    }

    /** the same fixed point form the page prints its numbers in */
    private static String format(double value, int digits) {
        return String.format(Locale.ROOT, "%." + digits + "f", value);
    }

    /** the reading currently on screen, for a test to look at */
    public GamepadState getState() {
        return state;
    }
}
