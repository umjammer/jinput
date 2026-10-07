/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin;

import java.util.Collection;
import java.util.Collections;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.DeviceSupportPlugin;
import net.java.games.input.Rumbler;
import net.java.games.input.plugin.hori.HoriTransport;
import net.java.games.input.usb.HidController;


/**
 * Common part of the HORI Fighting Commander OCTA support plugins.
 * <p>
 * The pad itself works through its report descriptor and needs nothing extra. What it adds is
 * the way to its profile memory, the button reassignment HORI Device Manager does, through
 * {@link #getTransport(HidController)}.
 * <p>
 * The PlayStation model shows up as
 * <ul>
 *  <li>0f0d:0162 in PS4 mode, a hid gamepad</li>
 *  <li>0f0d:0163 in PS5 mode, a hid gamepad</li>
 *  <li>0f0d:0164 in PC mode, an XInput device (class ff), not hid</li>
 * </ul>
 * and the Xbox model as 0f0d:0150, a GIP device, not hid either.
 * How HORI Device Manager talks to the PlayStation model is not known yet, so its transport
 * refuses everything rather than send bytes nobody has seen work.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public abstract class HoriOctaPluginBase implements DeviceSupportPlugin {

    public static final int VENDOR_ID = 0x0f0d;

    /** Xbox model, GIP */
    public static final int PRODUCT_ID_XBOX = 0x0150;
    /** PlayStation model in PS4 mode */
    public static final int PRODUCT_ID_PS4 = 0x0162;
    /** PlayStation model in PS5 mode */
    public static final int PRODUCT_ID_PS5 = 0x0163;
    /** PlayStation model in PC mode, XInput */
    public static final int PRODUCT_ID_PC = 0x0164;

    /** @return true for any mode of either model */
    public static boolean isOcta(int vendorId, int productId) {
        return vendorId == VENDOR_ID &&
                (productId == PRODUCT_ID_XBOX || productId == PRODUCT_ID_PS4 ||
                 productId == PRODUCT_ID_PS5 || productId == PRODUCT_ID_PC);
    }

    /** @return true when the controller is an OCTA */
    public static boolean isOcta(Controller controller) {
        return controller instanceof HidController hid && isOcta(hid.getVendorId(), hid.getProductId());
    }

    /**
     * The way to the profile memory of the controller.
     *
     * @return a transport that throws {@link UnsupportedOperationException} while the protocol of
     *         the model is unknown
     */
    public static HoriTransport getTransport(HidController controller) {
        if (!isOcta(controller.getVendorId(), controller.getProductId())) {
            throw new IllegalArgumentException("not a HORI Fighting Commander OCTA: %04x:%04x"
                    .formatted(controller.getVendorId(), controller.getProductId()));
        }
        return switch (controller.getProductId()) {
            case PRODUCT_ID_XBOX -> HoriTransport.unsupported(
                    "the Xbox model speaks GIP over raw usb, which a hid backend cannot reach, use hori.py");
            default -> HoriTransport.unsupported(
                    "the protocol HORI Device Manager uses for the PlayStation model is not known yet");
        };
    }

    @Override
    public Collection<Component> getExtraComponents(Object object) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Controller> getExtraChildControllers(Object object) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Rumbler> getExtraRumblers(Object object) {
        return Collections.emptyList();
    }
}
