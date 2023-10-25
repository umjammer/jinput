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

package net.java.games.input.windows;

import net.java.games.input.Component;
import net.java.games.input.Event;


/**
 * @author elias
 * @version 1.0
 */
final class DIControllers {

    private final static DIDeviceObjectData diEvent = new DIDeviceObjectData();

    /** synchronized to protect diEvent */
    public static synchronized boolean getNextDeviceEvent(Event event, IDirectInputDevice device) {
        if (!device.getNextEvent(diEvent))
            return false;
        DIDeviceObject object = device.mapEvent(diEvent);
        DIComponent component = device.mapObject(object);
        if (component == null)
            return false;
        int eventValue;
        if (object.isRelative()) {
            eventValue = object.getRelativeEventValue(diEvent.getData());
        } else {
            eventValue = diEvent.getData();
        }
        event.set(component, component.getDeviceObject().convertValue(eventValue), diEvent.getNanos());
        return true;
    }

    public static float poll(Component component, DIDeviceObject object) {
        int pollData = object.getDevice().getPollData(object);
        float result;
        if (object.isRelative()) {
            result = object.getRelativePollValue(pollData);
        } else {
            result = pollData;
        }
        return object.convertValue(result);
    }
}
