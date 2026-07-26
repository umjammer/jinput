/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;


/**
 * The colours and the type the tester is drawn in.
 * <p>
 * Everything but the artwork is one navy, {@code hsl(210, 90%, 20%)}, laid over the white
 * page at a handful of opacities: the value text is black, the labels are the navy at 60%,
 * the little bars at 50% over a 10% track, and the joystick cross hairs at 20%. Keeping
 * them as real alpha rather than as pre-blended greys is what makes a pressed button darken
 * its own track instead of painting a flat patch over it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
final class Theme {

    /** the one navy everything on the left hand side is mixed from */
    static final Color INK = new Color(0x05, 0x33, 0x61);

    static final Color TEXT = Color.BLACK;
    static final Color LABEL = alpha(INK, 0.6);

    /** the bar fill, and the small print under the title */
    static final Color AXES = alpha(INK, 0.5);

    /** the track a bar runs in, and the background of a button */
    static final Color BAR_BG = alpha(INK, 0.1);

    static final Color JOYSTICK_AXES = alpha(INK, 0.2);
    static final Color JOYSTICK_INDICATOR = INK;

    /** the light blue the controller is drawn in, {@code hsl(210, 50%, 85%)} */
    static final Color ART = new Color(0xc6, 0xd9, 0xec);

    static final Color BUTTON_TEXT = new Color(0x33, 0x33, 0x33);
    static final Color BUTTON_ACTIVE = new Color(0x80, 0x00, 0x80);
    static final Color BUTTON_ACTIVE_TEXT = Color.WHITE;

    /** the page's own font size, which every other size here is relative to */
    static final float BASE_SIZE = 18;

    /** labels are 75% of the base size */
    static final float LABEL_SIZE = BASE_SIZE * 0.75f;

    /** the title is an {@code h2}, 200% of the 16px the page as a whole is set in */
    static final float TITLE_SIZE = 32;

    /**
     * The page asks for Roboto and falls back to Helvetica, so this does the same. Roboto is
     * not usually installed on a Mac, and Helvetica's metrics are not Roboto's, so a string
     * lands a pixel or two off where the browser put it. Everything that is laid out rather
     * than measured still lines up.
     */
    private static final String FAMILY = family();

    private Theme() {
    }

    private static String family() {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String candidate : List.of("Roboto", "Helvetica")) {
            if (installed.contains(candidate)) return candidate;
        }
        return Font.SANS_SERIF;
    }

    static Font font(float size) {
        return new Font(FAMILY, Font.PLAIN, 1).deriveFont(size);
    }

    static Font boldFont(float size) {
        return new Font(FAMILY, Font.BOLD, 1).deriveFont(size);
    }

    /** @param a 0 fully transparent, 1 fully opaque */
    static Color alpha(Color color, double a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int) Math.round(Math.max(0, Math.min(1, a)) * 255));
    }
}
