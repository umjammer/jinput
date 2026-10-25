/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.geom.Path2D;
import java.util.Locale;


/**
 * The corner of the SVG path syntax the artwork in this package is written in.
 * <p>
 * The shapes of a game pad are all curves, and a page of {@link Path2D} calls transcribed by
 * hand is both unreadable and impossible to check against the original. Keeping the path
 * data verbatim and parsing it here means the outline can be compared with its source
 * character for character.
 * <p>
 * Move, line, horizontal, vertical, cubic and close are supported, absolute and relative,
 * with the repeat form where a command letter is followed by several sets of arguments.
 * Anything else throws, rather than quietly drawing the wrong shape.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
final class SvgPath {

    private final String d;
    private int pos;

    /** current point, and where the subpath started, for close */
    private double x, y, startX, startY;

    private SvgPath(String d) {
        this.d = d;
    }

    /**
     * @param d the {@code d} attribute of an SVG {@code path} element
     * @throws IllegalArgumentException the path uses something not supported here
     */
    static Path2D.Double parse(String d) {
        return new SvgPath(d).run();
    }

    private Path2D.Double run() {
        Path2D.Double path = new Path2D.Double();
        char command = 0;
        while (true) {
            skipSeparators();
            if (pos >= d.length()) break;

            char c = d.charAt(pos);
            if (Character.isLetter(c)) {
                command = c;
                pos++;
            } else if (command == 'M') {
                // a move followed by more coordinates draws lines, per the specification
                command = 'L';
            } else if (command == 'm') {
                command = 'l';
            } else if (command == 0) {
                throw new IllegalArgumentException("path does not start with a command: " + d);
            }
            apply(path, command);
        }
        return path;
    }

    private void apply(Path2D.Double path, char command) {
        boolean relative = Character.isLowerCase(command);
        double ox = relative ? x : 0;
        double oy = relative ? y : 0;

        switch (Character.toUpperCase(command)) {
            case 'M' -> {
                x = ox + number();
                y = oy + number();
                path.moveTo(x, y);
                startX = x;
                startY = y;
            }
            case 'L' -> {
                x = ox + number();
                y = oy + number();
                path.lineTo(x, y);
            }
            case 'H' -> {
                x = ox + number();
                path.lineTo(x, y);
            }
            case 'V' -> {
                y = oy + number();
                path.lineTo(x, y);
            }
            case 'C' -> {
                double c1x = ox + number(), c1y = oy + number();
                double c2x = ox + number(), c2y = oy + number();
                x = ox + number();
                y = oy + number();
                path.curveTo(c1x, c1y, c2x, c2y, x, y);
            }
            case 'Z' -> {
                path.closePath();
                x = startX;
                y = startY;
            }
            default -> throw new IllegalArgumentException("unsupported path command '" + command + "' in " + d);
        }
    }

    private void skipSeparators() {
        while (pos < d.length() && (Character.isWhitespace(d.charAt(pos)) || d.charAt(pos) == ',')) {
            pos++;
        }
    }

    private double number() {
        skipSeparators();
        int start = pos;
        if (pos < d.length() && (d.charAt(pos) == '-' || d.charAt(pos) == '+')) pos++;
        while (pos < d.length() && (Character.isDigit(d.charAt(pos)) || d.charAt(pos) == '.')) {
            pos++;
        }
        if (pos < d.length() && (d.charAt(pos) == 'e' || d.charAt(pos) == 'E')) {
            pos++;
            if (pos < d.length() && (d.charAt(pos) == '-' || d.charAt(pos) == '+')) pos++;
            while (pos < d.length() && Character.isDigit(d.charAt(pos))) {
                pos++;
            }
        }
        if (start == pos) {
            throw new IllegalArgumentException("expected a number at " + start + " in " + d);
        }
        return Double.parseDouble(d.substring(start, pos).toLowerCase(Locale.ROOT));
    }
}
