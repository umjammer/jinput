/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.keyassign;

import java.util.EnumSet;
import java.util.Set;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.PollingComponent;
import net.java.games.input.plugin.hori.HoriButton;
import net.java.games.input.usb.HidComponent;


/**
 * Which OCTA button a jinput button is, so a button can be picked by pressing it.
 * <p>
 * In PS4 and PS5 mode the OCTA reports its buttons in DualShock 4 order (hid button usage 1 is
 * jinput's {@code Button._0}): square, cross, circle, triangle, L1, R1, L2, R2, share/create,
 * options, L3, R3, PS, touchpad.
 * <p>
 * Note this is what the pad sends, so on a button already reassigned in the active profile it
 * is the function that shows, not the button.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
final class PadButtons {

    private PadButtons() {
    }

    private static final HoriButton[] DUALSHOCK4_ORDER = {
            HoriButton.X, HoriButton.A, HoriButton.B, HoriButton.Y,
            HoriButton.LB, HoriButton.RB, HoriButton.LT, HoriButton.RT,
            HoriButton.SELECT, HoriButton.START, HoriButton.LSB, HoriButton.RSB,
            HoriButton.GUIDE, HoriButton.SHARE
    };

    /** @return null for a component that is not one of the OCTA's buttons */
    static HoriButton toHori(Component component) {
        if (!(component.getIdentifier() instanceof Component.Identifier.Button button)) return null;
        int index;
        try {
            index = Integer.parseInt(button.getName());
        } catch (NumberFormatException e) {
            return null;
        }
        return index >= 0 && index < DUALSHOCK4_ORDER.length ? DUALSHOCK4_ORDER[index] : null;
    }

    /** @return the OCTA buttons held down at the last poll */
    static Set<HoriButton> pressed(Controller controller) {
        Set<HoriButton> pressed = EnumSet.noneOf(HoriButton.class);
        for (Component c : controller.getComponents()) {
            HoriButton b = toHori(c);
            if (b != null && value(c) > 0.5f) pressed.add(b);
        }
        return pressed;
    }

    /** the plugins differ: one is polled and read back, the other pushes and is read live */
    static float value(Component c) {
        if (c instanceof PollingComponent pc) return pc.getPollData();
        if (c instanceof HidComponent hc) return hc.getValue();
        return 0;
    }
}
