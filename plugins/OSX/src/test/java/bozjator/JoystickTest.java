/*
 * https://github.com/bozjator/JInput-Joystick
 */

package bozjator;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;
import vavi.util.Debug;


/**
 * Joystick Test with JInput
 *
 * @author <a href="http://theuzo007.wordpress.com">TheUzo007</a>
 * @version 22 Oct 2013
 */
public class JoystickTest {

    public static void main(String[] args) {
        //JInputJoystickTest jinputJoystickTest = new JInputJoystickTest();
        // Writes (into console) informations of all controllers that are found.
        //jinputJoystickTest.getAllControllersInfo();
        // In loop writes (into console) all joystick components and its current values.
        //jinputJoystickTest.pollControllerAndItsComponents(Controller.Type.STICK);
        //jinputJoystickTest.pollControllerAndItsComponents(Controller.Type.GAMEPAD);

        new JoystickTest();
    }

    final JFrameWindow window;
    private ArrayList<Controller> foundControllers;

    public JoystickTest() {
        window = new JFrameWindow();

        foundControllers = new ArrayList<>();
        searchForControllers();

        // If at least one controller was found we start showing controller data on window.
        if (!foundControllers.isEmpty())
            startShowingControllerData();
        else
            window.addControllerName("No controller found!");
    }

    /**
     * Search (and save) for controllers of type Controller.Type.STICK,
     * Controller.Type.GAMEPAD, Controller.Type.WHEEL and Controller.Type.FINGERSTICK.
     */
    private void searchForControllers() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
Debug.println("controllers: " + controllers.length);

        for (Controller controller : controllers) {
Debug.println("controller: " + controller + ", type: " + controller.getType());
            if (
                    controller.getType() == Controller.Type.STICK ||
                            controller.getType() == Controller.Type.GAMEPAD ||
                            controller.getType() == Controller.Type.WHEEL ||
                            controller.getType() == Controller.Type.FINGERSTICK
            ) {
                // Add new controller to the list of all controllers.
                foundControllers.add(controller);
Debug.println("foundControllers: " + foundControllers.size());

                // Add new controller to the list on the window.
                window.addControllerName(controller.getName() + " - " + controller.getType().toString() + " type");
            }
        }
    }

    /**
     * Starts showing controller data on the window.
     */
    private void startShowingControllerData() {
        Event event = new Event();
        while (true) {
            // Currently selected controller.
            int selectedControllerIndex = window.getSelectedControllerName();
//Debug.println("selectedControllerIndex: " + selectedControllerIndex);
            PollingController controller = (PollingController) foundControllers.get(selectedControllerIndex);
//Debug.println("controller: " + controller);

            // Pull controller for current data, and break while loop if controller is disconnected.
            controller.poll();

            // X axis and Y axis
            int xAxisPercentage = 0;
            int yAxisPercentage = 0;
            // JPanel for other axes.
            JPanel axesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 2));
            axesPanel.setBounds(0, 0, 200, 190);

            // JPanel for controller buttons
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
            buttonsPanel.setBounds(6, 19, 246, 110);

            EventQueue queue = controller.getEventQueue();

            // Go through all components of the controller.
            while (queue.getNextEvent(event)) {
                PollingComponent component = (PollingComponent) event.getComponent();
                Identifier componentIdentifier = component.getIdentifier();

                // Buttons
                //if(component.getName().contains("Button")){ // If the language is not english, this won't work.
                if (componentIdentifier.getName().matches("^[0-9]*$")) { // If the component identifier name contains only numbers, then this is a button.
                    // Is button pressed?
                    boolean isItPressed = component.getPollData() != 0.0f;

                    // Button index
                    String buttonIndex;
                    buttonIndex = component.getIdentifier().toString();

                    // Create and add new button to panel.
                    JToggleButton aToggleButton = new JToggleButton(buttonIndex, isItPressed);
                    aToggleButton.setPreferredSize(new Dimension(48, 25));
                    aToggleButton.setEnabled(false);
                    buttonsPanel.add(aToggleButton);

                    // We know that this component was button so we can skip to next component.
                    continue;
                }

                // Hat switch
                if (componentIdentifier == Identifier.Axis.POV) {
                    float hatSwitchPosition = component.getPollData();
                    window.setHatSwitch(hatSwitchPosition);

                    // We know that this component was hat switch so we can skip to next component.
                    continue;
                }

                // Axes
                if (component.isAnalog()) {
                    float axisValue = component.getPollData();
                    int axisValueInPercentage = getAxisValueInPercentage(axisValue);

                    // X axis
                    if (componentIdentifier == Identifier.Axis.X) {
                        xAxisPercentage = axisValueInPercentage;
                        continue; // Go to next component.
                    }
                    // Y axis
                    if (componentIdentifier == Identifier.Axis.Y) {
                        yAxisPercentage = axisValueInPercentage;
                        continue; // Go to next component.
                    }

                    // Other axis
                    JLabel progressBarLabel = new JLabel(component.getName());
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setValue(axisValueInPercentage);
                    axesPanel.add(progressBarLabel);
                    axesPanel.add(progressBar);
                }
            }

            // Now that we go through all controller components,
            // we add butons panel to window,
            window.setControllerButtons(buttonsPanel);
            // set x and y axes,
            window.setXYAxis(xAxisPercentage, yAxisPercentage);
            // add other axes panel to window.
            window.addAxisPanel(axesPanel);

            // We have to give processor some rest.
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//        window.showControllerDisconnected();
    }

    /**
     * Given value of axis in percentage.
     * Percentages increases from left/top to right/bottom.
     * If idle (in center) returns 50, if joystick axis is pushed to the left/top
     * edge returns 0 and if it's pushed to the right/bottom returns 100.
     *
     * @return value of axis in percentage.
     */
    public static int getAxisValueInPercentage(float axisValue) {
        return (int) (((2 - (1 - axisValue)) * 100) / 2);
    }
}