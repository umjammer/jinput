/*
 * Copyright (c) 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 */

package net.java.games.input;

import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.logging.Logger;


/**
 * A ControllerEnvironment represents a collection of controllers that are
 * physically or logically linked.  By default, this corresponds to the
 * environment for the local machine.
 * <p>
 * In this reference implementation, this class can also be used to register
 * controllers with the default environment as spi.  A spi is
 * created by subclassing ControllerEnvironment with a class that has a public
 * no-argument constructor, extends the net.java.games.input.ControllerEnvironment
 * abstract clsss.
 * (See net.java.games.input.windows.DirectInputEnvironment in the windows-plugin
 * part of the source tree for an example.)
 * <p>
 * <ul>
 *  <li>don't collect controllers in the constructor.</li>
 *  <li>collect controllers in {@link #getControllers} method.</li>
 * </ul>
 *
 * @author Michael Martak
 * @version %I% %G%
 */
public interface ControllerEnvironment {

    Logger log = Logger.getLogger(ControllerEnvironment.class.getName());

    /**
     * Returns the isSupported status of this environment.
     * What makes an environment supported or not is up to the
     * particular plugin, but may include OS or available hardware.
     */
    boolean isSupported();

    /**
     * Returns a list of all controllers available to this environment,
     * or an empty array if there are no controllers in this environment.
     */
    Controller[] getControllers();

    /**
     * Adds a listener for controller state change events.
     */
    void addControllerListener(ControllerListener l);

    /**
     * Removes a listener for controller state change events.
     */
    void removeControllerListener(ControllerListener l);

    /** to avoid conflict we can specify package patterns to exclude */
    static boolean toBeExcluded(String packageName) {
        String prop = System.getProperty("net.java.games.input.ControllerEnvironment.excludes", "");
        String[] excludes = prop.split(":");
log.finer("excludes: " + excludes.length + ", " + Arrays.toString(excludes) + ", " + packageName);
        return !prop.isEmpty() && Arrays.stream(excludes).anyMatch(packageName::contains);
    }

    /**
     * Returns the default environment for input controllers.
     * This usually corresponds to the environment for the local machine.
     */
    static ControllerEnvironment getDefaultEnvironment() {
        try {
log.finer("count: " + ServiceLoader.load(ControllerEnvironment.class).stream().count());
            for (ControllerEnvironment ce : ServiceLoader.load(ControllerEnvironment.class)) {
log.finer("ControllerEnvironment " + ce.getClass().getName() + ", exclude?: " + toBeExcluded(ce.getClass().getPackageName()));
                if (ce.isSupported()) {
                    if (!toBeExcluded(ce.getClass().getPackageName())) {
                        return ce;
                    } else {
log.finer(ce.getClass().getName() + " is excluded");
                    }
                } else {
log.finer(ce.getClass().getName() + " is not supported");
                }
            }
            throw new IllegalStateException("no suitable environment");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the environment by the specified name.
     * @param name name for match (partial is acceptable)
     */
    static ControllerEnvironment getEnvironmentByName(String name) {
        try {
log.finer("count: " + ServiceLoader.load(ControllerEnvironment.class).stream().count());
            for (ControllerEnvironment ce : ServiceLoader.load(ControllerEnvironment.class)) {
log.finer("ControllerEnvironment " + ce.getClass().getName() + ", exclude?: " + toBeExcluded(ce.getClass().getPackageName()));
                if (ce.isSupported()) {
                    if (!toBeExcluded(ce.getClass().getPackageName())) {
                        if (ce.getClass().getPackageName().contains(name)) {
                            return ce;
                        } else  {
log.finer(ce.getClass().getName() + " is not match");
                        }
                    } else {
log.finer(ce.getClass().getName() + " is excluded");
                    }
                } else {
log.finer(ce.getClass().getName() + " is not supported");
                }
            }
            throw new IllegalStateException("no suitable environment for: " + name);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
