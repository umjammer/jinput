package net.java.games.input.example;

import java.util.Arrays;

import net.java.games.input.Component;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.PollingController;


/**
 * This class shows how to use the event queue system in JInput. It will show
 * how to get the controllers, how to get the event queue for a controller, and
 * how to read and process events from the queue.
 *
 * @author Endolf
 */
public class ReadAllEvents {

    public void exec() {
        // Create an event object for the underlying plugin to populate
        Event event = new Event();
        StringBuffer buffer = new StringBuffer();

        // Get the available controllers
        PollingController[] controllers = Arrays.stream(ControllerEnvironment.getDefaultEnvironment().getControllers()).map(PollingController.class::cast).toArray(PollingController[]::new);
        if (controllers.length == 0) {
            System.out.println("Found no controllers.");
            return;
        }

        while (true) {

            for (PollingController controller : controllers) {
                // Remember to poll each one
                controller.poll();

                // Get the controllers event queue
                EventQueue queue = controller.getEventQueue();

                // For each object in the queue
                while (queue.getNextEvent(event)) {

                    // Create a string buffer and put in it, the controller name,
                    // the time stamp of the event, the name of the component
                    // that changed and the new value.
                    //
                    // Note that the timestamp is a relative thing, not
                    // absolute, we can tell what order events happened in
                    // across controllers this way. We can not use it to tell
                    // exactly *when* an event happened just the order.
                    buffer.setLength(0);
                    buffer.append(" at ");
                    buffer.append(event.getNanos()).append(", ");
                    Component comp = event.getComponent();
                    buffer.append(comp.getName()).append(" changed to ");
                    float value = event.getValue();

                    // Check the type of the component and display an
                    // appropriate value
                    if (comp.isAnalog()) {
                        buffer.append(value);
                    } else {
                        if (value == 1.0f) {
                            buffer.append("On");
                        } else {
                            buffer.append("Off");
                        }
                    }
                    System.out.println(buffer);
                }
            }

            // Sleep for 20 milliseconds, in here only so the example doesn't
            // thrash the system.
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public static void main(String[] args) {
        ReadAllEvents app = new ReadAllEvents();
        app.exec();
    }
}
