/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static net.java.games.input.ControllerEnvironment.toBeExcluded;


/**
 * This plugin supports to provide undocumented elements of a device.
 * i.e. Sony's DualShock4 has gyro sensors and rumblers,
 * but both elements are not appeared in the report descriptor.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-24 nsano initial version <br>
 */
public interface DeviceSupportPlugin {

    Logger logger = Logger.getLogger(DeviceSupportPlugin.class.getName());

    /** check this plugin is a support plugin for the device or not */
    boolean match(Object device);

    /** if none, empty collection should be returned */
    Collection<Component> getExtraComponents(Object object);

    /** if none, empty collection should be returned */
    Collection<Controller> getExtraChildControllers(Object object);

    /** if none, empty collection should be returned */
    Collection<Rumbler> getExtraRumblers(Object object);

    /**
     * utility.
     * filtered by package name specified in "net.java.games.input.ControllerEnvironment.excludes".
     */
    static Iterable<DeviceSupportPlugin> getPlugins() {
        return ServiceLoader.load(DeviceSupportPlugin.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(Predicate.not(p -> toBeExcluded(p.getClass().getPackageName())))
                .toList();
    }
}
