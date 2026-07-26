/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The artwork the tester knows about.
 * <p>
 * Registering later wins, so a drawing for one particular pad can be added in front of the
 * general ones without them having to know about it:
 * <pre>
 * GamepadArts.register(new MyPadArt());
 * </pre>
 * {@link #find} then hands it to the panel as soon as a pad it claims turns up.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public final class GamepadArts {

    /** oldest first, so the search runs backwards */
    private static final List<GamepadArt> arts = new ArrayList<>();

    static {
        register(new GenericGamepadArt());
        register(new StandardGamepadArt(StandardGamepadArt.Layout.XBOX));
        register(new StandardGamepadArt(StandardGamepadArt.Layout.PLAYSTATION));
    }

    private GamepadArts() {
    }

    public static void register(GamepadArt art) {
        arts.add(art);
    }

    /** in the order they were registered */
    public static List<GamepadArt> all() {
        return Collections.unmodifiableList(arts);
    }

    /**
     * @return the most recently registered art that claims this device, never {@code null}
     *         as long as the generic one is still registered
     */
    public static GamepadArt find(GamepadState state) {
        for (int i = arts.size() - 1; i >= 0; i--) {
            if (arts.get(i).supports(state)) return arts.get(i);
        }
        return arts.get(0);
    }
}
