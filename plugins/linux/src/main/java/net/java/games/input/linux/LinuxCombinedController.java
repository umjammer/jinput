package net.java.games.input.linux;

import java.io.IOException;
import java.util.Arrays;

import net.java.games.input.AbstractController;
import net.java.games.input.Event;
import net.java.games.input.PollingController;


public class LinuxCombinedController extends PollingController {

    private final LinuxControllerImpl eventController;
    private final LinuxJoystickAbstractController joystickController;

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

    @Override
    public void output(AbstractController.Report report) {
        for (LinuxForceFeedbackEffect rumbler : Arrays.stream(getRumblers()).map(LinuxForceFeedbackEffect.class::cast).toArray(LinuxForceFeedbackEffect[]::new)) {
            rumbler.rumble();
        }
    }
}
