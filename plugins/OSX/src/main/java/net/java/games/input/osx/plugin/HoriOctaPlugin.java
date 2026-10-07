/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.plugin;

import net.java.games.input.osx.OSXHIDDevice;
import net.java.games.input.plugin.HoriOctaPluginBase;


/**
 * HORI Fighting Commander OCTA support for IOKit.
 * <p>
 * Only the hid modes of the PlayStation model reach IOKit hid (PS4 and PS5 mode),
 * the PC mode and the Xbox model are not hid devices.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public class HoriOctaPlugin extends HoriOctaPluginBase {

    /** @param object OSXHIDDevice */
    @Override
    public boolean match(Object object) {
        if (!(object instanceof OSXHIDDevice device)) return false;

        return isOcta(device.getVendorId(), device.getProductId());
    }
}
