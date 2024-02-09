/**
 * Copyright (C) 2007 Jeremy Booth (jeremy@newdawnsoftware.com)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

package net.java.games.input.windows;

import java.util.ArrayList;
import java.util.List;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerListenerSupport;


/**
 * Combines the list of seperate keyboards and mice found with the raw plugin,
 * with the game controllers found with direct input.
 *
 * @author Jeremy
 */
public class DirectAndRawInputEnvironmentPlugin extends ControllerListenerSupport implements ControllerEnvironment {

    private final RawInputEnvironmentPlugin rawPlugin;
    private final DirectInputEnvironmentPlugin dinputPlugin;
    private Controller[] controllers = null;

    public DirectAndRawInputEnvironmentPlugin() {
        // These two *must* be loaded in this order for raw devices to work.
        dinputPlugin = new DirectInputEnvironmentPlugin();
        rawPlugin = new RawInputEnvironmentPlugin();
    }

    @Override
    public Controller[] getControllers() {
        if (controllers == null) {
            boolean rawKeyboardFound = false;
            boolean rawMouseFound = false;
            List<Controller> tempControllers = new ArrayList<>();
            Controller[] dinputControllers = dinputPlugin.getControllers();
            Controller[] rawControllers = rawPlugin.getControllers();
            for (Controller rawController : rawControllers) {
                tempControllers.add(rawController);
                if (rawController.getType() == Controller.Type.KEYBOARD) {
                    rawKeyboardFound = true;
                } else if (rawController.getType() == Controller.Type.MOUSE) {
                    rawMouseFound = true;
                }
            }
            for (Controller dinputController : dinputControllers) {
                if (dinputController.getType() == Controller.Type.KEYBOARD) {
                    if (!rawKeyboardFound) {
                        tempControllers.add(dinputController);
                    }
                } else if (dinputController.getType() == Controller.Type.MOUSE) {
                    if (!rawMouseFound) {
                        tempControllers.add(dinputController);
                    }
                } else {
                    tempControllers.add(dinputController);
                }
            }

            controllers = tempControllers.toArray(new Controller[] {});
        }

        return controllers;
    }

    @Override
    public boolean isSupported() {
        return rawPlugin.isSupported() || dinputPlugin.isSupported();
    }
}
