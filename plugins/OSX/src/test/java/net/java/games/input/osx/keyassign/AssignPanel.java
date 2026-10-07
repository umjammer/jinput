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
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.java.games.input.Controller;
import net.java.games.input.PollingController;
import net.java.games.input.plugin.HoriOctaPluginBase;
import net.java.games.input.plugin.hori.HoriButton;
import net.java.games.input.plugin.hori.HoriFunction;
import net.java.games.input.plugin.hori.HoriProfile;
import net.java.games.input.plugin.hori.HoriProfileMemory;
import net.java.games.input.plugin.hori.HoriStick;
import net.java.games.input.plugin.hori.HoriTransport;
import net.java.games.input.usb.HidController;


/**
 * The key assigner proper: the four profiles of the OCTA, what each button sends in them,
 * the name and the stick, the way HORI Device Manager shows them.
 * <p>
 * Profiles are edited here and can always be saved to a file or turned into hori.py command
 * lines. Reading from and writing to the controller go through
 * {@link HoriOctaPluginBase#getTransport(HidController)}, which refuses as long as the protocol
 * of the model is unknown, and then nothing is sent.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
class AssignPanel extends JPanel {

    /** index 1..4 */
    private final HoriProfile[] profiles = new HoriProfile[5];
    /** what was last read from the controller, index 1..4, null until read */
    private final byte[][] deviceMemory = new byte[5][];

    private Controller controller;
    private Set<HoriButton> pressed = EnumSet.noneOf(HoriButton.class);

    private final JComboBox<Integer> profileBox = new JComboBox<>(new Integer[] {1, 2, 3, 4});
    private final JTextField nameField = new JTextField(16);
    private final JRadioButton lsButton = new JRadioButton("LS");
    private final JRadioButton rsButton = new JRadioButton("RS");
    private final JCheckBox learnBox = new JCheckBox("select a button by pressing it", true);
    private final JLabel status = new JLabel(" ");
    private final ButtonTableModel tableModel = new ButtonTableModel();
    private final JTable table = new JTable(tableModel);
    private final Timer timer = new Timer(30, e -> poll());

    /** set while the fields are filled from the profile, so their listeners keep still */
    private boolean updating;

    AssignPanel() {
        super(new BorderLayout(8, 8));
        for (int i = 1; i <= 4; i++) profiles[i] = new HoriProfile();
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // top: profile, name, stick
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("profile"));
        top.add(profileBox);
        top.add(new JLabel("name"));
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new LengthFilter(HoriProfile.MAX_NAME_LENGTH));
        top.add(nameField);
        top.add(new JLabel("stick"));
        ButtonGroup stickGroup = new ButtonGroup();
        stickGroup.add(lsButton);
        stickGroup.add(rsButton);
        top.add(lsButton);
        top.add(rsButton);
        add(top, BorderLayout.NORTH);

        // center: the buttons
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(HoriFunction.values())));
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                boolean remapped = current().isRemapped(HoriButton.values()[row]);
                c.setFont(c.getFont().deriveFont(remapped ? Font.BOLD : Font.PLAIN));
                return c;
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // bottom: actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(learnBox);
        actions.add(button("Default buttons", this::resetButtons));
        actions.add(button("Load...", this::load));
        actions.add(button("Save...", this::save));
        actions.add(button("hori.py commands", this::showCommands));
        actions.add(button("Read from controller", this::readFromDevice));
        actions.add(button("Write to controller", this::writeToDevice));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actions, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        profileBox.addActionListener(e -> showProfile());
        lsButton.addActionListener(e -> current().setStick(HoriStick.LS));
        rsButton.addActionListener(e -> current().setStick(HoriStick.RS));
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { nameChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { nameChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { nameChanged(); }
        });

        showProfile();
    }

    private static JButton button(String label, Runnable action) {
        JButton b = new JButton(label);
        b.addActionListener(e -> action.run());
        return b;
    }

    /** @param controller null for none, editing still works */
    void setController(Controller controller) {
        this.controller = controller;
        pressed = EnumSet.noneOf(HoriButton.class);
        status.setText(controller == null ? "no controller, profiles can still be edited and saved" : "using " + describe(controller));
        if (controller != null && !(controller instanceof PollingController)) {
            try {
                // an event driven plugin only refreshes its components while it is open
                if (!controller.isOpen()) controller.open();
            } catch (IOException e) {
                status.setText("cannot open " + controller.getName() + ": " + e.getMessage());
            }
        }
        if (controller != null) timer.start(); else timer.stop();
        tableModel.fireTableDataChanged();
    }

    void stop() {
        timer.stop();
    }

    static String describe(Controller c) {
        String id = c instanceof HidController h ? " [%04x:%04x]".formatted(h.getVendorId(), h.getProductId()) : "";
        return c.getName() + id + (HoriOctaPluginBase.isOcta(c) ? " OCTA" : "");
    }

    int currentIndex() {
        return (Integer) profileBox.getSelectedItem();
    }

    HoriProfile current() {
        return profiles[currentIndex()];
    }

    /** replaces the profile in the slot, for loading and for tests */
    void setProfile(int index, HoriProfile profile) {
        HoriProfileMemory.checkProfile(index);
        profiles[index] = new HoriProfile(profile);
        if (index == currentIndex()) showProfile();
    }

    HoriProfile getProfile(int index) {
        return new HoriProfile(profiles[index]);
    }

    private void showProfile() {
        updating = true;
        try {
            HoriProfile p = current();
            nameField.setText(p.getName());
            (p.getStick() == HoriStick.RS ? rsButton : lsButton).setSelected(true);
            tableModel.fireTableDataChanged();
        } finally {
            updating = false;
        }
    }

    private void nameChanged() {
        if (!updating) current().setName(nameField.getText());
    }

    private void resetButtons() {
        current().resetButtons();
        tableModel.fireTableDataChanged();
    }

    /** reads the pad, picks the row of a button that just went down */
    private void poll() {
        if (controller == null) return;
        if (controller instanceof PollingController pc) {
            try {
                // the result is ignored on purpose: IOKit says false for a pad sitting still,
                // see JinputGamepadSource#poll, the values are right either way
                pc.poll();
            } catch (Exception e) {
                status.setText(controller.getName() + " is gone");
                setController(null);
                return;
            }
        }
        Set<HoriButton> now = PadButtons.pressed(controller);
        if (!now.equals(pressed)) {
            if (learnBox.isSelected() && !table.isEditing()) {
                for (HoriButton b : now) {
                    if (!pressed.contains(b)) {
                        int row = b.ordinal();
                        table.setRowSelectionInterval(row, row);
                        table.scrollRectToVisible(table.getCellRect(row, 0, true));
                    }
                }
            }
            pressed = now;
            tableModel.fireTableRowsUpdated(0, tableModel.getRowCount() - 1);
        }
    }

//#region file

    private void load() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (Reader r = Files.newBufferedReader(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.load(r);
            setProfile(currentIndex(), HoriProfile.fromProperties(props));
            status.setText("loaded " + chooser.getSelectedFile());
        } catch (IOException | IllegalArgumentException e) {
            error("cannot load", e);
        }
    }

    private void save() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            save(chooser.getSelectedFile().toPath());
            status.setText("saved " + chooser.getSelectedFile());
        } catch (IOException e) {
            error("cannot save", e);
        }
    }

    void save(Path path) throws IOException {
        try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            current().toProperties().store(w, "HORI Fighting Commander OCTA profile");
        }
    }

    private void showCommands() {
        JTextArea text = new JTextArea(String.join("\n", current().toHoriPyCommands(currentIndex())), 4, 60);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        text.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(text), "hori.py commands", JOptionPane.INFORMATION_MESSAGE);
    }

//#endregion

//#region device

    /** @return null with the reason shown when there is no way to the controller */
    private HoriTransport transport() {
        if (!(controller instanceof HidController hid) || !HoriOctaPluginBase.isOcta(controller)) {
            JOptionPane.showMessageDialog(this, "no HORI Fighting Commander OCTA is selected", "key assigner", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return HoriOctaPluginBase.getTransport(hid);
    }

    private void readFromDevice() {
        HoriTransport transport = transport();
        if (transport == null) return;
        int index = currentIndex();
        try {
            byte[] memory = transport.readMemory(index, 0, HoriProfileMemory.SIZE);
            deviceMemory[index] = memory;
            setProfile(index, HoriProfileMemory.decode(memory));
            status.setText("read profile " + index);
        } catch (UnsupportedOperationException | IOException e) {
            error("cannot read the controller", e);
        }
    }

    private void writeToDevice() {
        HoriTransport transport = transport();
        if (transport == null) return;
        int index = currentIndex();
        if (JOptionPane.showConfirmDialog(this, "write profile " + index + " to the controller?",
                "key assigner", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            if (deviceMemory[index] == null) {
                deviceMemory[index] = transport.readMemory(index, 0, HoriProfileMemory.SIZE);
            }
            int writes = HoriProfileMemory.write(transport, index, current(), deviceMemory[index]);
            status.setText("wrote profile " + index + " in " + writes + " writes");
        } catch (UnsupportedOperationException | IOException e) {
            error("cannot write the controller, nothing was sent", e);
        }
    }

    private void error(String what, Exception e) {
        status.setText(what + ": " + e.getMessage());
        JOptionPane.showMessageDialog(this, what + ":\n" + e.getMessage(), "key assigner", JOptionPane.ERROR_MESSAGE);
    }

//#endregion

    /** button | sends | pressed */
    private class ButtonTableModel extends AbstractTableModel {

        @Override public int getRowCount() {
            return HoriButton.values().length;
        }

        @Override public int getColumnCount() {
            return 3;
        }

        @Override public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Button";
                case 1 -> "Sends";
                default -> "Pressed";
            };
        }

        @Override public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 1 -> HoriFunction.class;
                case 2 -> Boolean.class;
                default -> String.class;
            };
        }

        @Override public Object getValueAt(int row, int column) {
            HoriButton b = HoriButton.values()[row];
            return switch (column) {
                case 0 -> b.psLabel + " / " + b.xboxLabel;
                case 1 -> current().getFunction(b);
                default -> pressed.contains(b);
            };
        }

        @Override public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override public void setValueAt(Object value, int row, int column) {
            if (column == 1 && value instanceof HoriFunction f) {
                current().setFunction(HoriButton.values()[row], f);
                fireTableRowsUpdated(row, row);
            }
        }
    }

    /** the controller keeps 16 chars */
    private static class LengthFilter extends DocumentFilter {

        private final int max;

        LengthFilter(int max) {
            this.max = max;
        }

        @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            int room = max - (fb.getDocument().getLength() - length);
            if (text != null && text.length() > room) text = text.substring(0, Math.max(0, room));
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
