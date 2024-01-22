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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An AbstractController is a skeleton implementation of a controller that
 * contains a fixed number of axes, controllers, and rumblers.
 */
public abstract class AbstractController implements Controller {

    /**
     * Human-readable name for this Controller
     */
    private final String name;

    /**
     * Array of components
     */
    private final Component[] components;

    /**
     * Array of child controllers
     */
    private final Controller[] children;

    /**
     * Array of rumblers
     */
    private final Rumbler[] rumblers;

    /**
     * Map from Component.Identifiers to Components
     */
    private final Map<Component.Identifier, Component> idToComponents = new HashMap<>();

    /**
     * Protected constructor for a controller containing the specified
     * axes, child controllers, and rumblers
     *
     * @param name       name for the controller
     * @param components components for the controller
     * @param children   child controllers for the controller
     * @param rumblers   rumblers for the controller
     */
    protected AbstractController(String name, Component[] components, Controller[] children, Rumbler[] rumblers) {
        this.name = name;
        this.components = components;
        this.children = children;
        this.rumblers = rumblers;
        // process from last to first to let earlier listed Components get higher priority
        for (int i = components.length - 1; i >= 0; i--) {
            idToComponents.put(components[i].getIdentifier(), components[i]);
        }
    }

    /**
     * Returns the controllers connected to make up this controller, or
     * an empty array if this controller contains no child controllers.
     * The objects in the array are returned in order of assignment priority
     * (primary stick, secondary buttons, etc.).
     */
    @Override
    public final Controller[] getControllers() {
        return children;
    }

    /**
     * Returns the components on this controller, in order of assignment priority.
     * For example, the button controller on a mouse returns an array containing
     * the primary or leftmost mouse button, followed by the secondary or
     * rightmost mouse button (if present), followed by the middle mouse button
     * (if present).
     * The array returned is an empty array if this controller contains no components
     * (such as a logical grouping of child controllers).
     */
    @Override
    public final Component[] getComponents() {
        return components;
    }

    /**
     * Returns a single component based on its identifier, or null
     * if no component with the specified type could be found.
     */
    @Override
    public final Component getComponent(Component.Identifier id) {
        return idToComponents.get(id);
    }

    /**
     * Returns the rumblers for sending feedback to this controller, or an
     * empty array if there are no rumblers on this controller.
     */
    @Override
    public final Rumbler[] getRumblers() {
        return rumblers;
    }

    /** */
    protected boolean isOpen;

    @Override
    public void open() throws IOException {
        isOpen = true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }

    /**
     * Returns the port type for this Controller.
     *
     * @return PortType.UNKNOWN by default, can be overridden
     */
    @Override
    public PortType getPortType() {
        return PortType.UNKNOWN;
    }

    /**
     * Returns the zero-based port number for this Controller.
     *
     * @return 0 by default, can be overridden
     */
    @Override
    public int getPortNumber() {
        return 0;
    }

    /**
     * Returns a human-readable name for this Controller.
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * Returns a non-localized string description of this controller.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the type of the Controller.
     */
    @Override
    public Type getType() {
        return Type.UNKNOWN;
    }

    /** input event listeners */
    private final List<InputEventListener> listeners = new ArrayList<>();

    /**
     * Adds an input event listener.
     */
    @Override
    public void addInputEventListener(InputEventListener listener) {
        listeners.add(listener);
    }

    /** fire an event */
    protected void fireOnInput(InputEvent event) {
        listeners.forEach(l -> l.onInput(event));
    }

    /** data structure to output */
    public interface Report {

        /** set value for each rumbler from this report class fields */
        void cascadeTo(Rumbler[] rumblers);
    }

    /** do output */
    public abstract void output(Report report) throws IOException;
}
