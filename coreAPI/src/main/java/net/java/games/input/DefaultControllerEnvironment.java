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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The default controller environment.
 *
 * @author Michael Martak
 * @version %I% %G%
 */
class DefaultControllerEnvironment extends ControllerEnvironment {

    private static final Logger log = Logger.getLogger(DefaultControllerEnvironment.class.getName());

    /**
     * List of all controllers in this environment
     */
    private ArrayList<Controller> controllers;

    /**
     * Public no-arg constructor.
     */
    public DefaultControllerEnvironment() {
    }

    /**
     * Returns a list of all controllers available to this environment,
     * or an empty array if there are no controllers in this environment.
     */
    @Override
    public Controller[] getControllers() {
        if (controllers == null) {
            // Controller list has not been scanned.
            controllers = new ArrayList<>();
            scanControllers();
        }

        return controllers.toArray(Controller[]::new);
    }

    /** to avoid conflict we can specify package patterns to exclude */
    static boolean toBeExcluded(String packageName) {
        String prop = System.getProperty("net.java.games.input.ControllerEnvironment.excludes", "");
        String[] excludes = prop.split(":");
log.finer("excludes: " + excludes.length + ", " + Arrays.toString(excludes) + ", " + packageName);
        return !prop.isEmpty() && Arrays.stream(excludes).anyMatch(packageName::contains);
    }

    /** */
    private void scanControllers() {
        try {
log.finer("count: " + ServiceLoader.load(ControllerEnvironment.class).stream().count());
            for (ControllerEnvironment ce : ServiceLoader.load(ControllerEnvironment.class)) {
                try {
log.finer("ControllerEnvironment " + ce.getClass().getName() + ", exclude?: " + toBeExcluded(ce.getClass().getPackageName()));
                    if (ce.isSupported() && !toBeExcluded(ce.getClass().getPackageName())) {
                        Controller[] c = ce.getControllers();
                        Collections.addAll(controllers, c);
                    } else {
                        log.finer(ce.getClass().getName() + " is not supported");
                    }
                } catch (Throwable e) {
                    log.log(Level.FINE, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.log(Level.FINE, e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
