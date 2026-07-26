/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * A tiny software 3D renderer, just enough for this visualizer.
 * <p>
 * Primitives are collected in world coordinates, then drawn back to front
 * (painter's algorithm) through a perspective camera orbiting the origin.
 * Everything is plain Java2D, so no 3D library is involved.
 * <p>
 * The world is right handed, {@code +x} right, {@code +y} up, {@code +z} towards the viewer.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class Scene3D {

    /** direction the (single, directional) light comes from, in world space */
    private static final Vec3 LIGHT = new Vec3(-0.4, 0.8, 0.55).normalize();

    /** anything closer than this to the eye is dropped rather than clipped */
    private static final double NEAR = 0.05;

    /** a single drawable, already reduced to view space */
    private abstract static class Prim {

        /** depth used for sorting, larger is farther away */
        final double depth;

        Prim(double depth) {
            this.depth = depth;
        }

        abstract void draw(Graphics2D g);
    }

    private static final class Face extends Prim {

        final GeneralPath path;
        final Color color;

        Face(double depth, GeneralPath path, Color color) {
            super(depth);
            this.path = path;
            this.color = color;
        }

        @Override void draw(Graphics2D g) {
            g.setColor(color);
            g.fill(path);
        }
    }

    private static final class Seg extends Prim {

        final Line2D line;
        final Color color;
        final float width;

        Seg(double depth, Line2D line, Color color, float width) {
            super(depth);
            this.line = line;
            this.color = color;
            this.width = width;
        }

        @Override void draw(Graphics2D g) {
            g.setColor(color);
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(line);
        }
    }

    /** camera orbit around the origin, in radians */
    private double yaw = 0.35, pitch = 0.28;
    /** distance from the origin to the eye */
    private double distance = 7.2;
    /** focal length in pixels at a 800px wide viewport, scaled with the actual size */
    private double focal = 900;

    private final List<Prim> prims = new ArrayList<>();

    private int width, height;

    public void setCamera(double yaw, double pitch, double distance) {
        this.yaw = yaw;
        this.pitch = Math.max(-1.5, Math.min(1.5, pitch));
        this.distance = Math.max(2.5, Math.min(20, distance));
    }

    public double yaw() {
        return yaw;
    }

    public double pitch() {
        return pitch;
    }

    public double distance() {
        return distance;
    }

    /** must be called once per frame before adding any primitive */
    public void begin(int width, int height) {
        this.width = width;
        this.height = height;
        this.focal = Math.min(width, height) * 1.35;
        prims.clear();
    }

    /** world space to eye space */
    private Vec3 toView(Vec3 p) {
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        double x = p.x() * cy - p.z() * sy;
        double z = p.x() * sy + p.z() * cy;
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        double y = p.y() * cp - z * sp;
        double z2 = p.y() * sp + z * cp;
        // the eye sits on +z looking down -z, so a farther point has a larger depth
        return new Vec3(x, y, distance - z2);
    }

    private Point2D project(Vec3 view) {
        double s = focal / view.z();
        return new Point2D.Double(width / 2.0 + view.x() * s, height / 2.0 - view.y() * s);
    }

    /**
     * Adds a flat convex polygon, shaded by its angle to the light.
     * It is lit from both sides, so the winding order does not matter.
     */
    public void face(Color base, Vec3... pts) {
        if (pts.length < 3) return;

        Vec3[] view = new Vec3[pts.length];
        double depth = 0;
        for (int i = 0; i < pts.length; i++) {
            view[i] = toView(pts[i]);
            if (view[i].z() <= NEAR) return;
            depth += view[i].z();
        }
        depth /= pts.length;

        Vec3 normal = pts[1].sub(pts[0]).cross(pts[2].sub(pts[0])).normalize();
        double lambert = Math.abs(normal.dot(LIGHT));
        Color shaded = shade(base, 0.34 + 0.66 * lambert);

        GeneralPath path = new GeneralPath();
        for (int i = 0; i < view.length; i++) {
            Point2D p = project(view[i]);
            if (i == 0) path.moveTo(p.getX(), p.getY());
            else path.lineTo(p.getX(), p.getY());
        }
        path.closePath();
        prims.add(new Face(depth, path, shaded));
    }

    /** adds a flat polygon with no shading applied, for glowing or translucent surfaces */
    public void flatFace(Color color, Vec3... pts) {
        if (pts.length < 3) return;

        GeneralPath path = new GeneralPath();
        double depth = 0;
        for (int i = 0; i < pts.length; i++) {
            Vec3 v = toView(pts[i]);
            if (v.z() <= NEAR) return;
            depth += v.z();
            Point2D p = project(v);
            if (i == 0) path.moveTo(p.getX(), p.getY());
            else path.lineTo(p.getX(), p.getY());
        }
        path.closePath();
        prims.add(new Face(depth / pts.length, path, color));
    }

    public void line(Color color, float width, Vec3 a, Vec3 b) {
        Vec3 va = toView(a), vb = toView(b);
        if (va.z() <= NEAR || vb.z() <= NEAR) return;
        Point2D pa = project(va), pb = project(vb);
        prims.add(new Seg((va.z() + vb.z()) / 2, new Line2D.Double(pa, pb), color, width));
    }

    /** draws a closed ring of {@code steps} segments, centred at {@code center} */
    public void ring(Color color, float width, Vec3 center, Vec3 normal, double radius, int steps) {
        Vec3 u = normal.anyPerpendicular().scale(radius);
        Vec3 v = normal.normalize().cross(u.normalize()).scale(radius);
        Vec3 prev = center.add(u);
        for (int i = 1; i <= steps; i++) {
            double a = 2 * Math.PI * i / steps;
            Vec3 cur = center.add(u.scale(Math.cos(a))).add(v.scale(Math.sin(a)));
            line(color, width, prev, cur);
            prev = cur;
        }
    }

    /** draws a filled disc, used for the translucent horizon */
    public void disc(Color color, Vec3 center, Vec3 normal, double radius, int steps) {
        Vec3 u = normal.anyPerpendicular().scale(radius);
        Vec3 v = normal.normalize().cross(u.normalize()).scale(radius);
        Vec3[] pts = new Vec3[steps];
        for (int i = 0; i < steps; i++) {
            double a = 2 * Math.PI * i / steps;
            pts[i] = center.add(u.scale(Math.cos(a))).add(v.scale(Math.sin(a)));
        }
        flatFace(color, pts);
    }

    /**
     * Draws a shaft with a cone head, used for the acceleration vectors.
     *
     * @param shaftWidth stroke width of the shaft, in pixels
     * @param headLength length of the cone head, in world units
     */
    public void arrow(Color color, Vec3 from, Vec3 to, float shaftWidth, double headLength) {
        Vec3 dir = to.sub(from);
        double len = dir.length();
        if (len < 1e-4) return;
        Vec3 n = dir.scale(1 / len);
        double head = Math.min(len * 0.45, headLength);
        Vec3 neck = to.sub(n.scale(head));

        line(color, shaftWidth, from, neck);

        Vec3 u = n.anyPerpendicular().scale(head * 0.42);
        Vec3 v = n.cross(u.normalize()).scale(head * 0.42);
        int steps = 10;
        Vec3 prev = neck.add(u);
        for (int i = 1; i <= steps; i++) {
            double a = 2 * Math.PI * i / steps;
            Vec3 cur = neck.add(u.scale(Math.cos(a))).add(v.scale(Math.sin(a)));
            face(color, prev, cur, to);
            prev = cur;
        }
    }

    /** sorts everything collected so far and paints it */
    public void end(Graphics2D g) {
        prims.sort(Comparator.comparingDouble((Prim p) -> p.depth).reversed());
        for (Prim p : prims) {
            p.draw(g);
        }
    }

    private static Color shade(Color c, double f) {
        int r = (int) Math.min(255, c.getRed() * f);
        int g = (int) Math.min(255, c.getGreen() * f);
        int b = (int) Math.min(255, c.getBlue() * f);
        return new Color(r, g, b, c.getAlpha());
    }
}
