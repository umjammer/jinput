package net.java.games.input.linux;

import java.util.logging.Logger;

import net.java.games.input.Component;


public class LinuxJoystickPOV extends LinuxJoystickAxis {

    private static final Logger log = Logger.getLogger(LinuxJoystickPOV.class.getName());

    private LinuxJoystickAxis hatX;
    private LinuxJoystickAxis hatY;

    LinuxJoystickPOV(Component.Identifier.Axis id, LinuxJoystickAxis hatX, LinuxJoystickAxis hatY) {
        super(id, false);
        this.hatX = hatX;
        this.hatY = hatY;
    }

    LinuxJoystickAxis getXAxis() {
        return hatX;
    }

    LinuxJoystickAxis getYAxis() {
        return hatY;
    }


    protected void updateValue() {
        float lastX = hatX.getPollData();
        float lastY = hatY.getPollData();

        resetHasPolled();
        if (lastX == -1 && lastY == -1)
            setValue(Component.POV.UP_LEFT);
        else if (lastX == -1 && lastY == 0)
            setValue(Component.POV.LEFT);
        else if (lastX == -1 && lastY == 1)
            setValue(Component.POV.DOWN_LEFT);
        else if (lastX == 0 && lastY == -1)
            setValue(Component.POV.UP);
        else if (lastX == 0 && lastY == 0)
            setValue(Component.POV.OFF);
        else if (lastX == 0 && lastY == 1)
            setValue(Component.POV.DOWN);
        else if (lastX == 1 && lastY == -1)
            setValue(Component.POV.UP_RIGHT);
        else if (lastX == 1 && lastY == 0)
            setValue(Component.POV.RIGHT);
        else if (lastX == 1 && lastY == 1)
            setValue(Component.POV.DOWN_RIGHT);
        else {
            log.fine("Unknown values x = " + lastX + " | y = " + lastY);
            setValue(Component.POV.OFF);
        }
    }
}
