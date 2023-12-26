/*
 * https://github.com/bozjator/JInput-Joystick
 */

package bozjator;

import java.util.Arrays;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;
import net.java.games.input.Version;


/**
 * @author <a href="http://www.java-gaming.org/index.php/topic,16866.0">endolf</a>
 */
public class JInputJoystickTest {

    /**
     * Prints all the controllers and its components.
     */
    public static void getAllControllersInfo() {
        System.out.println("JInput version: " + Version.getVersion());
        System.out.println();

        // Get a list of the controllers JInput knows about and can interact with.
        Controller[] controllersList = ControllerEnvironment.getDefaultEnvironment().getControllers();

        // First print all controllers names.
        for (Controller controller : controllersList) {
            System.out.println(controller.getName());
        }

        // Print all components of controllers.
        for (Controller controller : controllersList) {
            System.out.println("\n");
            System.out.println("-----------------------------------------------------------------");

            // Get the name of the controller
            System.out.println(controller.getName());
            // Get the type of the controller, e.g. GAMEPAD, MOUSE, KEYBOARD,
            // see http://www.newdawnsoftware.com/resources/jinput/apidocs/net/java/games/input/Controller.Type.html
            System.out.println("Type: " + controller.getType().toString());

            // Get these controllers components (buttons and axis)
            Component[] components = controller.getComponents();
            System.out.print("Component count: " + components.length);
            for (int j = 0; j < components.length; j++) {
                System.out.println();

                // Get the components name
                System.out.println("Component " + j + ": " + components[j].getName());
                // Get it's identifier, E.g. BUTTON.PINKIE, AXIS.POV and KEY.Z, 
                // see http://www.newdawnsoftware.com/resources/jinput/apidocs/net/java/games/input/Component.Identifier.html
                System.out.println("    Identifier: " + components[j].getIdentifier().getName());
                System.out.print("    ComponentType: ");
                if (components[j].isRelative())
                    System.out.print("Relative");
                else
                    System.out.print("Absolute");

                if (components[j].isAnalog())
                    System.out.print(" Analog");
                else
                    System.out.print(" Digital");
            }

            System.out.println("\n");
            System.out.println("-----------------------------------------------------------------");
        }
    }

    /**
     * Prints controllers components and its values.
     *
     * @param controllerType Desired type of the controller.
     */
    public static void pollControllerAndItsComponents(Controller.Type controllerType) {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

        // First controller of the desired type.
        PollingController firstController = null;

        for (int i = 0; i < controllers.length && firstController == null; i++) {
            if (controllers[i].getType() == controllerType) {
                // Found a controller
                firstController = (PollingController) controllers[i];
                break;
            }
        }

        if (firstController == null) {
            // Couldn't find a controller
            System.out.println("Found no desired controller!");
            System.exit(0);
        }

        System.out.println("First controller of a desired type is: " + firstController.getName());

        while (true) {
            firstController.poll();
            PollingComponent[] components = Arrays.stream(firstController.getComponents())
                    .map(PollingComponent.class::cast).toArray(PollingComponent[]::new);
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < components.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(components[i].getName());
                buffer.append(": ");
                if (components[i].isAnalog()) {
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

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}