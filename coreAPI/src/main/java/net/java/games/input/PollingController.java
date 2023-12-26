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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An PollingController is a skeleton implementation of a controller that
 * contains a fixed number of axes, controllers, and rumblers.
 */
@Deprecated
public abstract class PollingController extends AbstractController {

    private static final Logger log = Logger.getLogger(PollingController.class.getName());

    public final static int EVENT_QUEUE_DEPTH = 32;

    private final static Event event = new Event();

    private EventQueue eventQueue = new EventQueue(EVENT_QUEUE_DEPTH);

    /**
     * Protected constructor for a controller containing the specified
     * axes, child controllers, and rumblers
     *
     * @param name       name for the controller
     * @param components components for the controller
     * @param children   child controllers for the controller
     * @param rumblers   rumblers for the controller
     */
    protected PollingController(String name, Component[] components, Controller[] children, Rumbler[] rumblers) {
        super(name, components, children, rumblers);
    }

    /**
     * Initialized the controller event queue to a new size. Existing events
     * in the queue are lost.
     */
    public final void setEventQueueSize(int size) {
        try {
            setDeviceEventQueueSize(size);
            eventQueue = new EventQueue(size);
        } catch (IOException e) {
            log.fine("Failed to create new event queue of size " + size + ": " + e);
        }
    }

    /**
     * Plugins override this method to adjust their internal event queue size
     */
    protected void setDeviceEventQueueSize(int size) throws IOException {
    }

    /**
     * Get the device event queue
     */
    public final EventQueue getEventQueue() {
        return eventQueue;
    }

    protected abstract boolean getNextDeviceEvent(Event event) throws IOException;

    protected void pollDevice() throws IOException {
        // must override
    }

    /**
     * Polls axes for data.  Returns false if the controller is no longer valid.
     * Polling reflects the current state of the device when polled.
     *
     * poll() is synchronized to protect the static event
     */
    public synchronized boolean poll() {
        Component[] components = getComponents();
        try {
            pollDevice();
            for (Component item : components) {
                PollingComponent component = (PollingComponent) item;
                if (component.isRelative()) {
                    component.setPollData(0);
                } else {
                    // Let the component poll itself lazily
                    component.resetHasPolled();
                }
            }
            while (getNextDeviceEvent(event)) {
                PollingComponent component = (PollingComponent) event.getComponent();
                float value = event.getValue();
                if (component.isRelative()) {
                    if (value == 0)
                        continue;
                    component.setPollData(component.getPollData() + value);
                } else {
                    if (value == component.getEventValue())
                        continue;
                    component.setEventValue(value);
                }
                if (!eventQueue.isFull())
                    eventQueue.add(event);
            }
            return true;
        } catch (IOException e) {
            log.log(Level.FINER, "Failed to poll device: " + e.getMessage(), e);
            return false;
        }
    }
}
