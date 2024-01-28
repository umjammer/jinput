/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx;

import java.util.logging.Level;

import net.java.games.input.Component;
import net.java.games.input.Rumbler;
import net.java.games.input.usb.HidRumbler;
import vavi.util.Debug;


/**
 * OSXRumbler.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-24 nsano initial version <br>
 */
public class OSXRumbler implements HidRumbler {

    private final OSXHIDDevice device;
    private final Component.Identifier id;
    private final OSXHIDElement element;
    private final int reportId;

    /**
     * @param element element#cookieId is used as data offset
     */
    public OSXRumbler(OSXHIDDevice device, int reportId, Component.Identifier id, OSXHIDElement element) {
        this.device = device;
        this.reportId = reportId;
        this.id = id;
        this.element = element;
    }

    public final OSXHIDElement getElement() {
        return element;
    }

    private float value;

    @Override
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String getOutputName() {
        return id.getName();
    }

    // TODO return type should be (enum) Output?
    @Override
    public Component.Identifier getOutputIdentifier() {
        return id;
    }

    @Override
    public int getReportId() {
        return reportId;
    }

    @Override
    public void fill(byte[] data) {
        data[element.getCookie()] = (byte) value;
Debug.printf(Level.FINER, "data[%02d] = 0x%2$02x (%2$d)", element.getCookie(), data[element.getCookie()] & 0xff);
    }

    @Override
    public String toString() {
        return getOutputName();
    }
}
