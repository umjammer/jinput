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

package net.java.games.input.osx;

import java.io.IOException;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.PollingController;
import net.java.games.input.Rumbler;
import net.java.games.input.usb.HidController;
import net.java.games.input.usb.HidRumbler;

import static vavix.rococoa.iokit.IOKitLib.kIOHIDReportTypeOutput;


/**
 * Represents an OSX AbstractController
 *
 * @author elias
 * @version 1.0
 */
final class OSXController extends PollingController implements HidController {

    private final OSXHIDQueue queue;
    private final Type type;
    private final OSXHIDDevice device;

    OSXController(OSXHIDDevice device, OSXHIDQueue queue, Component[] components, Controller[] children, Rumbler[] rumblers, Type type) {
        super(device.getProductName(), components, children, rumblers);
        this.queue = queue;
        this.type = type;
        this.device = device;
    }

    @Override
    protected synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        // for event listener
        if (queue.getNextEvent(device.osxEvent)) {
            OSXComponent component = queue.mapEvent(device.osxEvent);
            event.set(component, component.getElement().convertValue(), device.osxEvent.getNanos());
            return true;
        } else
            return false;
    }

    @Override
    protected void setDeviceEventQueueSize(int size) throws IOException {
        queue.setQueueDepth(size);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public PortType getPortType() {
        return device.getPortType();
    }

    @Override
    public int getProductId() {
        return device.getProductId();
    }

    @Override
    public int getVendorId() {
        return device.getVendorId();
    }

    @Override
    public void output(Report report) throws IOException {
        report.cascadeTo(getRumblers());

        int reportId = ((HidReport) report).getReportId();
        byte[] data = ((HidReport) report).getData();
        for (Rumbler rumbler : getRumblers()) {
            ((HidRumbler) rumbler).fill(data);
        }
        device.setReport(kIOHIDReportTypeOutput, reportId, data, data.length);
    }
}
