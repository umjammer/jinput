/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.keyassign;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.java.games.input.Controller;
import net.java.games.input.usb.HidController;
import net.java.games.input.usb.HidReportType;


/**
 * Looks at a hid device without changing it: dumps the report descriptor and reads every
 * feature report it declares. That is the first step to finding how a device keeps its
 * settings when the vendor tool does not run.
 * <p>
 * Nothing is ever written from here.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
class ProbePanel extends JPanel {

    private Controller controller;
    private final JTextArea text = new JTextArea(30, 80);

    ProbePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JLabel("read only: dumps what the device declares and reads its feature reports, nothing is written"), BorderLayout.NORTH);

        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        text.setEditable(false);
        add(new JScrollPane(text), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton descriptor = new JButton("Report descriptor");
        descriptor.addActionListener(e -> show(describe() + dumpDescriptor()));
        actions.add(descriptor);
        JButton features = new JButton("Read feature reports");
        features.addActionListener(e -> show(describe() + readFeatureReports()));
        actions.add(features);
        JButton save = new JButton("Save...");
        save.addActionListener(e -> save());
        actions.add(save);
        add(actions, BorderLayout.SOUTH);
    }

    void setController(Controller controller) {
        this.controller = controller;
        show(controller == null ? "no controller" : describe());
    }

    private void show(String s) {
        text.setText(s);
        text.setCaretPosition(0);
    }

    private String describe() {
        if (controller == null) return "no controller\n";
        return "# " + AssignPanel.describe(controller) + ", " + LocalDateTime.now() + "\n\n";
    }

    /** @return null with the reason in the text when the controller is not hid */
    private HidController hid() {
        return controller instanceof HidController h ? h : null;
    }

    String dumpDescriptor() {
        HidController hid = hid();
        if (hid == null) return "not a hid device\n";
        byte[] descriptor = hid.getReportDescriptor();
        if (descriptor == null) return "the backend does not give the report descriptor\n";

        HidDescriptorItems items = HidDescriptorItems.parse(descriptor);
        StringBuilder sb = new StringBuilder();
        sb.append("report descriptor, ").append(descriptor.length).append(" bytes\n");
        sb.append(HidDescriptorItems.hexDump(descriptor, descriptor.length)).append('\n');
        items.lines.forEach(l -> sb.append(l).append('\n'));
        sb.append('\n');
        items.reports.forEach(r -> sb.append(r).append('\n'));
        return sb.toString();
    }

    String readFeatureReports() {
        HidController hid = hid();
        if (hid == null) return "not a hid device\n";
        byte[] descriptor = hid.getReportDescriptor();
        if (descriptor == null) return "the backend does not give the report descriptor\n";

        List<HidDescriptorItems.ReportInfo> features = HidDescriptorItems.parse(descriptor).featureReports();
        if (features.isEmpty()) return "the device declares no feature report\n";

        StringBuilder sb = new StringBuilder();
        for (HidDescriptorItems.ReportInfo r : features) {
            sb.append(r).append('\n');
            byte[] buffer = new byte[Math.max(1, r.bytes())];
            try {
                int n = hid.readReport(HidReportType.FEATURE, r.id(), buffer);
                sb.append(HidDescriptorItems.hexDump(buffer, n));
            } catch (IOException | UnsupportedOperationException e) {
                sb.append("  failed: ").append(e.getMessage()).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private void save() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        try {
            Files.writeString(path, text.getText(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "cannot save:\n" + e.getMessage(), "probe", JOptionPane.ERROR_MESSAGE);
        }
    }
}
