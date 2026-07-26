/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;


/**
 * Draws the paper plane inside its gimbal cage and paints the HUD over it.
 * <p>
 * What each sensor becomes:
 * <ul>
 *  <li><b>gyro</b> is integrated into the attitude the plane is drawn at, so turning the
 *      pad turns the plane, and a spin around the pad's own axis spins the plane too</li>
 *  <li><b>acceleration</b> is split by {@link AttitudeFilter}. The steady part is gravity,
 *      drawn as the dim shaft down to the horizon disc, which is the tilt reference. The
 *      leftover is the shove, drawn as the bright arrow: its length and its colour are
 *      the magnitude, and the fading ribbon is where its tip has been. A hard enough
 *      shove also fires a ring off the cage, so a flick still registers at a glance even
 *      though the arrow itself is gone again a few frames later</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class PaperPlanePanel extends JPanel {

    private static final Color BACKGROUND_TOP = new Color(0x10, 0x14, 0x22);
    private static final Color BACKGROUND_BOTTOM = new Color(0x05, 0x06, 0x0b);
    private static final Color CAGE = new Color(0x3a, 0x7c, 0xa8, 0x88);
    private static final Color CAGE_EQUATOR = new Color(0x63, 0xc8, 0xe8, 0xbb);
    private static final Color HORIZON = new Color(0x2a, 0x86, 0xb0, 0x2e);
    private static final Color GRAVITY = new Color(0x7f, 0x9d, 0xc0, 0xcc);
    private static final Color HUD_TEXT = new Color(0xd8, 0xe2, 0xf0);
    private static final Color HUD_DIM = new Color(0x7c, 0x8a, 0x9e);

    /** radius of the cage the plane flies in */
    private static final double CAGE_RADIUS = 2.35;

    /** world units drawn per 1g of linear acceleration */
    private static final double ACCEL_SCALE = 2.0;

    /** how many g it takes to fire a shock ring */
    private static final double SHOCK_THRESHOLD = 0.42;

    /** an expanding ring fired off the cage by a sharp shove */
    private static final class Shock {

        final Vec3 direction;
        final double strength;
        double age;

        Shock(Vec3 direction, double strength) {
            this.direction = direction;
            this.strength = strength;
        }
    }

    private final Supplier<SensorSource> sourceFactory;
    private final AttitudeFilter filter = new AttitudeFilter();
    private final Scene3D scene = new Scene3D();

    /** world space positions of the shove arrow tip, newest last */
    private final Deque<Vec3> trail = new ArrayDeque<>();
    private final List<Shock> shocks = new ArrayList<>();

    private SensorSource source;
    private final Timer timer;

    private long lastNanos = System.nanoTime();
    private double fps;
    private boolean paused;
    private boolean showTrail = true;
    private double shockCooldown;

    private int dragX, dragY;

    public PaperPlanePanel(Supplier<SensorSource> sourceFactory) {
        this.sourceFactory = sourceFactory;
        this.source = sourceFactory.get();

        setPreferredSize(new Dimension(900, 700));
        setBackground(BACKGROUND_BOTTOM);
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
                requestFocusInWindow();
            }

            @Override public void mouseDragged(MouseEvent e) {
                scene.setCamera(scene.yaw() + (e.getX() - dragX) * 0.008,
                        scene.pitch() + (e.getY() - dragY) * 0.008,
                        scene.distance());
                dragX = e.getX();
                dragY = e.getY();
            }

            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                scene.setCamera(scene.yaw(), scene.pitch(),
                        scene.distance() + e.getPreciseWheelRotation() * 0.4);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);

        bindKey("R", "reset", () -> {
            filter.reset();
            trail.clear();
            shocks.clear();
        });
        bindKey("G", "flip gravity", filter::flipAccelSign);
        bindKey("C", "recalibrate", () -> {
            if (source instanceof ControllerSensorSource c) c.recalibrate();
            filter.reset();
        });
        bindKey("SPACE", "pause", () -> paused = !paused);
        bindKey("T", "trail", () -> {
            showTrail = !showTrail;
            trail.clear();
        });
        bindKey("D", "rescan", this::rescan);

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
        lastNanos = System.nanoTime();
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
        filter.reset();
        trail.clear();
        shocks.clear();
    }

    private void step() {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1e9;
        lastNanos = now;
        fps = fps == 0 ? 1 / Math.max(dt, 1e-4) : fps * 0.9 + 0.1 / Math.max(dt, 1e-4);

        if (paused) return;

        source.poll();
        if (!source.isAlive()) {
            rescan();
            return;
        }
        filter.update(source.gyro(), source.accel(), dt);

        Vec3 shoveWorld = filter.attitude().rotate(filter.linear());
        Vec3 tip = shoveWorld.scale(ACCEL_SCALE);
        if (showTrail) {
            trail.addLast(tip);
            while (trail.size() > 70) {
                trail.removeFirst();
            }
        }

        shockCooldown = Math.max(0, shockCooldown - dt);
        double magnitude = filter.linear().length();
        if (magnitude > SHOCK_THRESHOLD && shockCooldown == 0) {
            shocks.add(new Shock(shoveWorld.normalize(), magnitude));
            shockCooldown = 0.18;
        }
        for (Iterator<Shock> it = shocks.iterator(); it.hasNext(); ) {
            Shock s = it.next();
            s.age += dt;
            if (s.age > 0.9) it.remove();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, getHeight(), BACKGROUND_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());

            scene.begin(getWidth(), getHeight());
            buildScene();
            scene.end(g);

            drawHud(g);
        } finally {
            g.dispose();
        }
    }

    private void buildScene() {
        Quat attitude = filter.attitude();

        // the cage the plane flies in, one ring per world plane
        scene.ring(CAGE, 1.1f, Vec3.ZERO, new Vec3(1, 0, 0), CAGE_RADIUS, 72);
        scene.ring(CAGE, 1.1f, Vec3.ZERO, new Vec3(0, 0, 1), CAGE_RADIUS, 72);
        scene.ring(CAGE_EQUATOR, 1.5f, Vec3.ZERO, new Vec3(0, 1, 0), CAGE_RADIUS, 72);

        // the horizon, square to whichever way the pad says gravity is
        Vec3 up = attitude.rotate(filter.gravity()).normalize();
        if (up.length() > 0.5) {
            scene.disc(HORIZON, Vec3.ZERO, up, CAGE_RADIUS * 0.98, 64);
            scene.ring(new Color(0x5a, 0xb6, 0xd8, 0x99), 1.3f, Vec3.ZERO, up, CAGE_RADIUS * 0.98, 64);
            scene.arrow(GRAVITY, Vec3.ZERO, up.scale(-CAGE_RADIUS * 0.62), 2.2f, 0.26);
        }

        for (Shock s : shocks) {
            double t = s.age / 0.9;
            int alpha = (int) (200 * (1 - t) * Math.min(1, s.strength));
            if (alpha <= 2) continue;
            double radius = CAGE_RADIUS * (0.35 + 1.15 * t);
            scene.ring(new Color(0xff, 0xa8, 0x3c, alpha), (float) (3.4 * (1 - t) + 0.6),
                    Vec3.ZERO, s.direction, radius, 48);
        }

        if (showTrail && trail.size() > 1) {
            Vec3 prev = null;
            int i = 0;
            for (Vec3 p : trail) {
                if (prev != null) {
                    float f = (float) i / trail.size();
                    scene.line(new Color(0x5c, 0xf0, 0xd0, (int) (170 * f * f)), 1f + 2.2f * f, prev, p);
                }
                prev = p;
                i++;
            }
        }

        PaperPlane.draw(scene, attitude);

        // the shove itself, the part of the acceleration that is not gravity
        Vec3 shove = attitude.rotate(filter.linear()).scale(ACCEL_SCALE);
        double len = shove.length();
        if (len > 0.03) {
            double clamped = Math.min(len, CAGE_RADIUS * 1.25);
            scene.arrow(magnitudeColor(len / ACCEL_SCALE), Vec3.ZERO,
                    shove.normalize().scale(clamped), 4.5f, 0.34);
        }
    }

    /** cyan when gentle, amber in the middle, red when the pad is really being thrown about */
    private static Color magnitudeColor(double g) {
        float t = (float) Math.min(1, g / 1.2);
        float hue = 0.48f - 0.48f * t;
        return Color.getHSBColor(hue, 0.72f + 0.28f * t, 1f);
    }

    private void drawHud(Graphics2D g) {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 14);

        g.setFont(bold);
        g.setColor(HUD_TEXT);
        g.drawString("jinput paper plane", 16, 24);
        g.setFont(mono);
        g.setColor(HUD_DIM);
        g.drawString(source.name(), 16, 42);

        Vec3 gyro = source.gyro();
        Vec3 accel = source.accel();
        String[] labels = {"Gyro X", "Gyro Y", "Gyro Z", "Accel X", "Accel Y", "Accel Z"};
        // the gyro bars run to +-500 deg/s, the accelerometer ones to +-2 g
        double[] values = {
                Math.toDegrees(gyro.x()), Math.toDegrees(gyro.y()), Math.toDegrees(gyro.z()),
                accel.x(), accel.y(), accel.z()};
        double[] ranges = {500, 500, 500, 2, 2, 2};
        String[] units = {"%+7.1f", "%+7.1f", "%+7.1f", "%+7.3f", "%+7.3f", "%+7.3f"};

        int y = 68;
        for (int i = 0; i < labels.length; i++) {
            if (i == 3) y += 8;
            drawMeter(g, 16, y, labels[i], values[i], ranges[i], units[i],
                    i < 3 ? CAGE_EQUATOR : new Color(0x5c, 0xf0, 0xd0));
            y += 20;
        }

        y += 12;
        g.setColor(HUD_DIM);
        g.drawString(String.format("spin    %7.1f deg/s", Math.toDegrees(filter.spin().length())), 16, y);
        y += 16;
        g.drawString(String.format("shove   %7.3f g", filter.linear().length()), 16, y);
        y += 16;
        g.drawString(String.format("gravity %7.3f g, sign %+d", filter.gravity().length(), filter.accelSign()), 16, y);

        g.setColor(HUD_DIM);
        String help = "drag orbit  wheel zoom  R reset  C recalibrate  G flip gravity  T trail  D rescan  SPACE pause";
        g.drawString(help, 16, getHeight() - 14);
        String state = String.format("%s%.0f fps", paused ? "PAUSED  " : "", fps);
        g.drawString(state, getWidth() - g.getFontMetrics().stringWidth(state) - 16, getHeight() - 14);
    }

    /**
     * A bipolar bar, centred on zero.
     *
     * @param range value the bar is full at, in either direction
     */
    private void drawMeter(Graphics2D g, int x, int y, String label, double value,
                           double range, String format, Color color) {
        int barX = x + 62;
        int barW = 150;
        int barH = 9;
        double v = Math.max(-1, Math.min(1, value / range));

        g.setColor(HUD_DIM);
        g.drawString(label, x, y + barH);

        g.setColor(new Color(0xff, 0xff, 0xff, 0x14));
        g.fill(new Rectangle2D.Double(barX, y, barW, barH));

        double mid = barX + barW / 2.0;
        g.setColor(color);
        double w = Math.abs(v) * barW / 2;
        g.fill(new Rectangle2D.Double(v < 0 ? mid - w : mid, y, w, barH));

        g.setColor(new Color(0xff, 0xff, 0xff, 0x40));
        g.setStroke(new BasicStroke(1));
        g.drawLine((int) mid, y, (int) mid, y + barH);

        g.setColor(HUD_DIM);
        g.drawString(String.format(format, value), barX + barW + 10, y + barH);
    }

    /** the filter driving the plane, exposed so a test can look at what it settled on */
    public AttitudeFilter filter() {
        return filter;
    }
}
