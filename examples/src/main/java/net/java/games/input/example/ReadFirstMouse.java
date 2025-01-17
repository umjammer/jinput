package net.java.games.input.example;

import java.util.Arrays;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;


/**
 * This class shows how to read the values in a polling loop for the first mouse
 * detected. It will show how to get the available controllers, how to check the
 * type of the controller, how to read the components of the controller, and how
 * to get the data from the component.
 *
 * @author Endolf
 */
public class ReadFirstMouse {

    public void exec() {
        // Get the available controllers
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        // Loop through the controllers, check the type of each one, and save
        // the first mouse we find.
        PollingController firstMouse = null;
        for (int i = 0; i < controllers.length && firstMouse == null; i++) {
            if (controllers[i].getType() == Controller.Type.MOUSE) {
                // Found a mouse
                firstMouse = (PollingController) controllers[i];
            }
        }
        if (firstMouse == null) {
            // Couldn't find a mouse
            System.out.println("Found no mouse");
            System.exit(0);
        }
        PollingComponent[] components = Arrays.stream(firstMouse.getComponents()).map(PollingComponent.class::cast).toArray(PollingComponent[]::new);

        System.out.println("First mouse is: " + firstMouse.getName());

        while (true) {
            // Poll the controller
            firstMouse.poll();

            // Get all the axis and buttons
            StringBuilder buffer = new StringBuilder();

            // For each component, get it's name, and it's current value
            for (int i = 0; i < components.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(components[i].getName());
                buffer.append(": ");
                if (components[i].isAnalog()) {
                    // Get the value at the last poll of this component
                    buffer.append(components[i].getPollData());
                } else {
                    if (components[i].getPollData() == 1.0f) {
                        buffer.append("On");
                    } else {
                        buffer.append("Off");
                    }
                }
            }
            System.out.println(buffer);

            // Sleep for 20 millis, this is just so the example doesn't thrash
            // the system.
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public static void main(String[] args) {
        ReadFirstMouse app = new ReadFirstMouse();
        app.exec();
    }
}
