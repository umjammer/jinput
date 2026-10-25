/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.games.input.Controller;
import net.java.games.input.osx.OSXEnvironmentPlugin;
import net.java.games.input.usb.HidController;

import static java.lang.System.getLogger;


/**
 * A gamepad tester, the one at
 * <a href="https://hardwaretester.com/gamepad">hardwaretester.com/gamepad</a> drawn dot for
 * dot over jinput instead of over the browser's Gamepad API.
 * <p>
 * This is a sample for the OSX plugin, not part of the library. Run it with
 * {@code mvn -pl plugins/OSX test-compile exec:java -Dexec.classpathScope=test
 * -Dexec.mainClass=net.java.games.input.osx.visualizer.gamepad.GamepadTester}, or straight
 * from an IDE.
 * <p>
 * The pad to open is taken from {@code local.properties} in the module directory:
 * <pre>
 * mid=0x54c
 * pid=0x9cc
 * </pre>
 * If that file is missing, the first pad the environment reports is used, and if there is no
 * pad at all it falls back to {@link DemoGamepadSource} so there is still something to look
 * at.
 * <p>
 * The drawing of the pad is pluggable, see {@link GamepadArt}: a controller with a shape of
 * its own needs an art class and a {@link GamepadArts#register} call, and nothing else.
 * {@code A} steps through the registered ones by hand.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
public class GamepadTester {

    private static final Logger logger = getLogger(GamepadTester.class.getName());

    private static final Path LOCAL_PROPERTIES = Path.of("local.properties");

    private GamepadTester() {
    }

    /** @return the pad named in {@code local.properties}, or the demo when there is none */
    public static GamepadSource findSource() {
        JinputGamepadSource source = null;
        try {
            Controller controller = pick(new OSXEnvironmentPlugin().getControllers());
            if (controller != null) {
                source = new JinputGamepadSource(controller);
logger.log(Logger.Level.INFO, "using " + controller.getName());
                return source;
            }
logger.log(Logger.Level.INFO, "no pad found");
        } catch (Exception | UnsatisfiedLinkError e) {
logger.log(Logger.Level.WARNING, "IOKit unusable, " + e);
            if (source != null) source.close();
        }
        return new DemoGamepadSource();
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
        GamepadTesterPanel panel = new GamepadTesterPanel(GamepadTester::findSource);

        JFrame frame = new JFrame("jinput - gamepad tester");
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
