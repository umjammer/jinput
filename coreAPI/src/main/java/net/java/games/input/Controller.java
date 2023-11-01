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


/**
 * A Controller represents a physical device, such as a keyboard, mouse,
 * or joystick, or a logical grouping of related controls, such as a button
 * pad or mouse ball.  A controller can be composed of multiple controllers.
 * For example, the ball of a mouse and its buttons are two separate
 * controllers.
 * <ul>
 *  <li>don't start event listener system in the constructor.</li>
 *  <li>start event listener system in {@link #open} method.</li>
 * </ul>
 */
public interface Controller {

    /**
     * Returns the controllers connected to make up this controller, or
     * an empty array if this controller contains no child controllers.
     * The objects in the array are returned in order of assignment priority
     * (primary stick, secondary buttons, etc.).
     */
    Controller[] getControllers();

    /**
     * Returns the type of the Controller.
     */
    Type getType();

    /**
     * Returns the components on this controller, in order of assignment priority.
     * For example, the button controller on a mouse returns an array containing
     * the primary or leftmost mouse button, followed by the secondary or
     * rightmost mouse button (if present), followed by the middle mouse button
     * (if present).
     * The array returned is an empty array if this controller contains no components
     * (such as a logical grouping of child controllers).
     */
    Component[] getComponents();

    /**
     * Returns a single axis based on its type, or null
     * if no axis with the specified type could be found.
     */
    Component getComponent(Component.Identifier id);

    /**
     * Returns the rumblers for sending feedback to this controller, or an
     * empty array if there are no rumblers on this controller.
     */
    Rumbler[] getRumblers();

    /**
     * Starts the device.
     * for start event listener system.
     */
    void open() throws IOException;

    /**
     * Tell the device is open.
     * event listener system is working.
     */
    boolean isOpen();

    /**
     * Closes the device.
     * for start event listener system.
     */
    void close() throws IOException;

    /**
     * Polls axes for data.  Returns false if the controller is no longer valid.
     * Polling reflects the current state of the device when polled.
     */
    boolean poll();

    /**
     * Initialized the controller event queue to a new size. Existing events
     * in the queue are lost.
     */
    void setEventQueueSize(int size);

    /**
     * Get the device event queue
     */
    EventQueue getEventQueue();

    /**
     * Returns the port type for this Controller.
     */
    PortType getPortType();

    /**
     * Returns the zero-based port number for this Controller.
     */
    int getPortNumber();

    /**
     * Returns a human-readable name for this Controller.
     */
    String getName();

    /**
     * Types of controller objects.
     */
    enum Type {

        /**
         * Unkown controller type.
         */
        UNKNOWN,

        /**
         * Mouse controller.
         */
        MOUSE,

        /**
         * A keyboard controller
         */
        KEYBOARD,

        /**
         * Fingerstick controller; note that this may be sometimes treated as a
         * type of mouse or stick.
         */
        FINGERSTICK,

        /**
         * Gamepad controller.
         */
        GAMEPAD,

        /**
         * Headtracker controller.
         */
        HEADTRACKER,

        /**
         * Rudder controller.
         */
        RUDDER,

        /**
         * Stick controller, such as a joystick or flightstick.
         */
        STICK,

        /**
         * A trackball controller; note that this may sometimes be treated as a
         * type of mouse.
         */
        TRACKBALL,

        /**
         * A trackpad, such as a tablet, touchpad, or glidepad;
         * note that this may sometimes be treated as a type of mouse.
         */
        TRACKPAD,

        /**
         * A wheel controller, such as a steering wheel (note
         * that a mouse wheel is considered part of a mouse, not a
         * wheel controller).
         */
        WHEEL,
    }

    /**
     * Common controller port types.
     */
    enum PortType {

        /**
         * Unknown port type
         */
        UNKNOWN,

        /**
         * USB port
         */
        USB,

        /**
         * Standard game port
         */
        GAME,

        /**
         * Network port
         */
        NETWORK,

        /**
         * Serial port
         */
        SERIAL,

        /**
         * i8042 (PS/2)
         */
        I8042,

        /**
         * Parallel port
         */
        PARALLEL
    }
}
