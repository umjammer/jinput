/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.keyassign;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.System.Logger;
import java.util.Arrays;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.games.input.Controller;
import net.java.games.input.osx.OSXEnvironmentPlugin;
import net.java.games.input.plugin.HoriOctaPluginBase;

import static java.lang.System.getLogger;


/**
 * A key assigner for the HORI Fighting Commander OCTA over jinput, after
 * <a href="https://github.com/mbenkmann/hori_device_manager">hori_device_manager</a>, which does
 * the same for the Xbox model on the command line.
 * <p>
 * This is a sample for the OSX plugin, not part of the library. Run it with
 * {@code mvn -pl plugins/OSX test-compile exec:java -Dexec.classpathScope=test
 * -Dexec.mainClass=net.java.games.input.osx.keyassign.HoriKeyAssigner}, or straight from an IDE.
 * <p>
 * Two tabs:
 * <dl>
 *  <dt>Key assign</dt>
 *  <dd>the four profiles: name, stick, and what each button sends. A button is picked by
 *      pressing it on the pad. Profiles are saved to files and can be shown as hori.py
 *      commands. Reading and writing the controller go through the OCTA plugin's transport.</dd>
 *  <dt>HID probe</dt>
 *  <dd>the report descriptor and the feature reports of the device, read only, to find out
 *      how the PlayStation model keeps its settings.</dd>
 * </dl>
 * The OCTA is only a hid device in PS4 or PS5 mode, switch it there for it to show up.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public class HoriKeyAssigner {

    private static final Logger logger = getLogger(HoriKeyAssigner.class.getName());

    private HoriKeyAssigner() {
    }

    /** @return whatever IOKit finds, nothing off a mac */
    static Controller[] findControllers() {
        try {
            return new OSXEnvironmentPlugin().getControllers();
        } catch (Exception | UnsatisfiedLinkError | NoClassDefFoundError e) {
logger.log(Logger.Level.WARNING, "IOKit unusable, " + e);
            return new Controller[0];
        }
    }

    /** the OCTA first, then pads, then the rest */
    static Controller[] sort(Controller[] controllers) {
        return Arrays.stream(controllers)
                .sorted((a, b) -> Integer.compare(rank(a), rank(b)))
                .toArray(Controller[]::new);
    }

    private static int rank(Controller c) {
        if (HoriOctaPluginBase.isOcta(c)) return 0;
        if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) return 1;
        return 2;
    }

    /** builds the window, must be called on the event dispatch thread */
    public static JFrame createFrame(Controller[] controllers) {
        AssignPanel assign = new AssignPanel();
        ProbePanel probe = new ProbePanel();

        JComboBox<Controller> devices = new JComboBox<>(sort(controllers));
        devices.setRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, value instanceof Controller c ? AssignPanel.describe(c) : "none", index, selected, focus);
            }
        });
        Runnable select = () -> {
            Controller c = (Controller) devices.getSelectedItem();
            assign.setController(c);
            probe.setController(c);
        };
        devices.addActionListener(e -> select.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("controller"));
        top.add(devices);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Key assign", assign);
        tabs.addTab("HID probe", probe);

        JFrame frame = new JFrame("jinput - HORI OCTA key assigner");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(tabs, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                assign.stop();
            }
        });

        select.run();
        return frame;
    }

    public static void main(String[] args) {
        Controller[] controllers = findControllers();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createFrame(controllers);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
