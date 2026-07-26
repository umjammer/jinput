/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gylo;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.games.input.Controller;
import net.java.games.input.osx.OSXEnvironmentPlugin;
import net.java.games.input.usb.HidController;
import vavi.games.input.hid4java.spi.Hid4JavaEnvironmentPlugin;

import static java.lang.System.getLogger;


/**
 * A 3D paper plane flown by the motion sensors of a game pad.
 * <p>
 * This is a sample for the OSX plugin, not part of the library. Run it with
 * {@code mvn -pl plugins/OSX test-compile exec:java -Dexec.classpathScope=test
 * -Dexec.mainClass=net.java.games.input.osx.visualizer.gylo.PaperPlaneVisualizer}, or straight
 * from an IDE.
 * <p>
 * The pad to open is taken from {@code local.properties} in the module directory:
 * <pre>
 * mid=0x54c
 * pid=0x9cc
 * </pre>
 * If that file is missing, the first pad the environment reports is used. If nothing turns
 * up, or no pad exposes motion axes, it falls back to {@link DemoSensorSource} so there is
 * still something to look at.
 * <p>
 * Hold the pad still for the first moment after it opens: that is when the gyro's resting
 * offset is measured out. Press {@code C} to take it again.
 * <p>
 * Everything is drawn with plain Java2D, see {@link Scene3D}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class PaperPlaneVisualizer {

    private static final Logger logger = getLogger(PaperPlaneVisualizer.class.getName());

    private static final Path LOCAL_PROPERTIES = Path.of("local.properties");

    private PaperPlaneVisualizer() {
    }

    /**
     * Picks the first environment that actually offers motion axes, and only falls back to
     * the demo when none does.
     * <p>
     * hid4java is tried first on purpose. The IOKit plugin enumerates the DualShock fine,
     * but the pad declares its gyro and accelerometer inside one 54 byte vendor defined
     * blob rather than as named HID usages, so IOKit hands back no motion elements to map
     * and the plugin has nothing to expose. hid4java's own {@code DualShock4Plugin} slices
     * those bytes out of the raw input report and names them {@code "Gyro X"} and so on,
     * which is what this sample looks for.
     */
    public static SensorSource findSource() {
        SensorSource sensor = tryEnvironment(new Hid4JavaEnvironmentPlugin()::getControllers, "hid4java");
        if (sensor != null) return sensor;

        sensor = tryEnvironment(new OSXEnvironmentPlugin()::getControllers, "IOKit");
        if (sensor != null) return sensor;

logger.log(Logger.Level.INFO, "no pad with motion axes, falling back to the demo");
        return new DemoSensorSource();
    }

    /** @return null when this environment has no pad with motion axes on it */
    private static SensorSource tryEnvironment(Supplier<Controller[]> environment, String label) {
        ControllerSensorSource sensor = null;
        try {
            Controller controller = pick(environment.get());
            if (controller == null) {
logger.log(Logger.Level.INFO, label + ": no pad found");
                return null;
            }
            sensor = new ControllerSensorSource(controller);
            if (sensor.boundChannelCount() > 0) {
logger.log(Logger.Level.INFO, label + ": using " + sensor.name());
                return sensor;
            }
logger.log(Logger.Level.INFO, label + ": '" + controller.getName() + "' has no motion axes, only " + sensor.componentNames());
            sensor.close();
            return null;
        } catch (Exception | UnsatisfiedLinkError e) {
logger.log(Logger.Level.WARNING, label + ": unusable, " + e);
            if (sensor != null) sensor.close();
            return null;
        }
    }

    /** honours {@code local.properties} when it names a pad, otherwise takes the first one */
    private static Controller pick(Controller[] controllers) throws IOException {
        if (controllers.length == 0) return null;

        if (Files.exists(LOCAL_PROPERTIES)) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(LOCAL_PROPERTIES)) {
                props.load(in);
            }
            String mid = props.getProperty("mid");
            String pid = props.getProperty("pid");
            if (mid != null && pid != null) {
                int vendorId = Integer.decode(mid);
                int productId = Integer.decode(pid);
                Controller c = Arrays.stream(controllers)
                        .filter(x -> x instanceof HidController h
                                && h.getVendorId() == vendorId && h.getProductId() == productId)
                        .findFirst()
                        .orElse(null);
                if (c != null) return c;
logger.log(Logger.Level.INFO, "no pad matching " + mid + ":" + pid + ", using the first one");
            }
        }
        return Arrays.stream(controllers)
                .filter(c -> c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK)
                .findFirst()
                .orElse(controllers[0]);
    }

    /** builds the window, must be called on the event dispatch thread */
    public static JFrame createFrame() {
        PaperPlanePanel panel = new PaperPlanePanel(PaperPlaneVisualizer::findSource);

        JFrame frame = new JFrame("jinput - paper plane");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);

        panel.start();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                panel.stop();
            }
        });
        return frame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
