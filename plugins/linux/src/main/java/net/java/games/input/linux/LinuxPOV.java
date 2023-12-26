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

package net.java.games.input.linux;

import java.io.IOException;
import java.util.logging.Logger;

import net.java.games.input.Component;


/**
 * Represents a linux button
 *
 * @author elias
 * @version 1.0
 */
final class LinuxPOV extends LinuxComponent {

    private static final Logger log = Logger.getLogger(LinuxPOV.class.getName());
    
    private final LinuxEventComponent componentX;
    private final LinuxEventComponent componentY;

    private float lastX;
    private float lastY;

    public LinuxPOV(LinuxEventComponent componentX, LinuxEventComponent componentY) {
        super(componentX);
        this.componentX = componentX;
        this.componentY = componentY;
    }

    @Override
    protected float poll() throws IOException {
        lastX = poll(componentX);
        lastY = poll(componentY);
        return convertValue(0f, null);
    }

    @Override
    public float convertValue(float value, LinuxAxisDescriptor descriptor) {
        if (descriptor == componentX.getDescriptor())
            lastX = value;
        if (descriptor == componentY.getDescriptor())
            lastY = value;

        if (lastX == -1 && lastY == -1)
            return Component.POV.UP_LEFT;
        else if (lastX == -1 && lastY == 0)
            return Component.POV.LEFT;
        else if (lastX == -1 && lastY == 1)
            return Component.POV.DOWN_LEFT;
        else if (lastX == 0 && lastY == -1)
            return Component.POV.UP;
        else if (lastX == 0 && lastY == 0)
            return Component.POV.OFF;
        else if (lastX == 0 && lastY == 1)
            return Component.POV.DOWN;
        else if (lastX == 1 && lastY == -1)
            return Component.POV.UP_RIGHT;
        else if (lastX == 1 && lastY == 0)
            return Component.POV.RIGHT;
        else if (lastX == 1 && lastY == 1)
            return Component.POV.DOWN_RIGHT;
        else {
            log.fine("Unknown values x = " + lastX + " | y = " + lastY);
            return Component.POV.OFF;
        }
    }
}
