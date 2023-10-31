package net.java.games.input.linux;

import java.io.IOException;

import net.java.games.input.AbstractController;
import net.java.games.input.Event;


public class LinuxCombinedController extends AbstractController {

    private LinuxControllerImpl eventController;
    private LinuxJoystickAbstractController joystickController;

    LinuxCombinedController(LinuxControllerImpl eventController, LinuxJoystickAbstractController joystickController) {
        super(eventController.getName(), joystickController.getComponents(), eventController.getControllers(), eventController.getRumblers());
        this.eventController = eventController;
        this.joystickController = joystickController;
    }

    @Override
    protected boolean getNextDeviceEvent(Event event) throws IOException {
        return joystickController.getNextDeviceEvent(event);
    }

    @Override
    public final PortType getPortType() {
        return eventController.getPortType();
    }

    @Override
    public final void pollDevice() throws IOException {
        eventController.pollDevice();
        joystickController.pollDevice();
    }

    @Override
    public Type getType() {
        return eventController.getType();
    }
}
