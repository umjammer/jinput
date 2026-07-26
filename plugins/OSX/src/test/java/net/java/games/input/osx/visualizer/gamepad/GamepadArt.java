/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


/**
 * The picture of a controller, drawn from a {@link GamepadState}.
 * <p>
 * This is the pluggable half of the tester. The panel knows nothing about what a pad looks
 * like: it asks {@link GamepadArts} which art claims the device in front of it, gives that
 * art a box to draw in, and lets it get on with it. A pad with a shape of its own only needs
 * a class here and a {@link GamepadArts#register} call, no change to the panel.
 * <p>
 * An art draws in its own coordinates, whatever they happen to be, and says so through
 * {@link #getDesignBounds}. The panel scales that box into the room it has, so an art can be
 * lifted straight out of an SVG without any of its numbers being touched.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 * @see StandardGamepadArt
 * @see GenericGamepadArt
 */
public interface GamepadArt {

    /** what to call this drawing, for the picker and for logging */
    String getName();

    /** whether this art is a fair picture of the device that reported the given state */
    boolean supports(GamepadState state);

    /** the box {@link #paint} draws inside, in the art's own coordinates */
    Rectangle2D.Double getDesignBounds();

    /**
     * Draws the pad. The transform is already set so that {@link #getDesignBounds} maps onto
     * the room the panel has, and antialiasing is already on.
     */
    void paint(Graphics2D g, GamepadState state);
}
