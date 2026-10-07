/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.keyassign;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.PollingComponent;
import net.java.games.input.PollingController;
import net.java.games.input.Rumbler;
import net.java.games.input.plugin.HoriOctaPluginBase;
import net.java.games.input.plugin.hori.HoriButton;
import net.java.games.input.plugin.hori.HoriFunction;
import net.java.games.input.plugin.hori.HoriProfile;
import net.java.games.input.plugin.hori.HoriStick;
import net.java.games.input.usb.HidController;
import net.java.games.input.usb.HidReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * HoriKeyAssignerTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
class HoriKeyAssignerTest {

    static boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * a descriptor with an input report 1 (14 buttons + 2 bits padding, one byte axis)
     * and a vendor feature report 0x80 of 63 bytes
     */
    static final byte[] DESCRIPTOR = bytes(
            0x05, 0x01, 0x09, 0x05, 0xa1, 0x01,             // Usage Page (Generic Desktop), Usage (Game Pad), Collection (Application)
            0x85, 0x01,                                     //   Report ID (1)
            0x05, 0x09, 0x19, 0x01, 0x29, 0x0e,             //   Usage Page (Button), Usage Minimum (1), Usage Maximum (14)
            0x15, 0x00, 0x25, 0x01, 0x75, 0x01, 0x95, 0x0e, //   Logical 0..1, Report Size (1), Report Count (14)
            0x81, 0x02,                                     //   Input (Data, Var)
            0x95, 0x02, 0x81, 0x03,                         //   Report Count (2), Input (Const)
            0x05, 0x01, 0x09, 0x30, 0x26, 0xff, 0x00,       //   Usage Page (Generic Desktop), Usage (X), Logical Maximum (255)
            0x75, 0x08, 0x95, 0x01, 0x81, 0x02,             //   Report Size (8), Report Count (1), Input (Data, Var)
            0x06, 0x00, 0xff, 0x09, 0x20,                   //   Usage Page (0xff00), Usage (0x20)
            0x85, 0x80, 0x95, 0x3f, 0xb1, 0x02,             //   Report ID (0x80), Report Count (63), Feature (Data, Var)
            0xc0                                            // End Collection
    );

    static byte[] bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) b[i] = (byte) values[i];
        return b;
    }

    /** a button whose value the test sets */
    static class FakeButton extends PollingComponent {
        float value;

        FakeButton(int index) {
            super("button " + index, Component.Identifier.Button.values()[index]);
        }

        @Override protected float poll() {
            return value;
        }

        @Override public boolean isRelative() {
            return false;
        }
    }

    /** an OCTA in PS5 mode with 14 buttons and one feature report */
    static class FakeOcta extends PollingController implements HidController {
        final List<String> writes = new ArrayList<>();

        FakeOcta() {
            super("HORI Fighting Commander OCTA", buttons(), new Controller[0], new Rumbler[0]);
        }

        static Component[] buttons() {
            Component[] c = new Component[14];
            for (int i = 0; i < c.length; i++) c[i] = new FakeButton(i);
            return c;
        }

        void press(int index, boolean down) {
            ((FakeButton) getComponents()[index]).value = down ? 1 : 0;
        }

        @Override protected boolean getNextDeviceEvent(Event event) {
            return false;
        }

        @Override public void output(Report report) {
            writes.add("output");
        }

        @Override public int getProductId() {
            return HoriOctaPluginBase.PRODUCT_ID_PS5;
        }

        @Override public int getVendorId() {
            return HoriOctaPluginBase.VENDOR_ID;
        }

        @Override public byte[] getReportDescriptor() {
            return DESCRIPTOR.clone();
        }

        @Override public int readReport(HidReportType type, int reportId, byte[] buffer) throws IOException {
            if (reportId != 0x80) throw new IOException("no report " + reportId);
            for (int i = 0; i < buffer.length; i++) buffer[i] = (byte) i;
            return buffer.length;
        }

        @Override public void writeReport(HidReportType type, int reportId, byte[] data) {
            writes.add(type + ":" + reportId);
        }
    }

    @Test
    @DisplayName("the descriptor walk finds the reports and their lengths")
    void test1() {
        HidDescriptorItems items = HidDescriptorItems.parse(DESCRIPTOR);

        assertEquals(2, items.reports.size());
        assertEquals(new HidDescriptorItems.ReportInfo(HidReportType.INPUT, 1, 3), items.reports.get(0));
        assertEquals(List.of(new HidDescriptorItems.ReportInfo(HidReportType.FEATURE, 0x80, 63)), items.featureReports());

        assertTrue(items.lines.stream().anyMatch(l -> l.contains("Usage Page (0xff00) vendor defined")), items.lines.toString());
        assertTrue(items.lines.get(items.lines.size() - 1).endsWith("End Collection"));
        // nested lines are indented by the collection
        assertTrue(items.lines.get(3).contains("  Report ID (1)"), items.lines.get(3));

        // a cut off descriptor does not throw
        HidDescriptorItems.parse(new byte[] {0x75});
    }

    @Test
    @DisplayName("buttons come in DualShock 4 order")
    void test2() {
        FakeOcta pad = new FakeOcta();
        assertEquals(HoriButton.X, PadButtons.toHori(pad.getComponents()[0])); // square
        assertEquals(HoriButton.A, PadButtons.toHori(pad.getComponents()[1])); // cross
        assertEquals(HoriButton.GUIDE, PadButtons.toHori(pad.getComponents()[12])); // PS
        assertEquals(HoriButton.SHARE, PadButtons.toHori(pad.getComponents()[13])); // touchpad
        assertNull(PadButtons.toHori(new FakeButton(14)));

        pad.press(1, true);
        pad.press(9, true);
        pad.poll();
        assertEquals(EnumSet.of(HoriButton.A, HoriButton.START), PadButtons.pressed(pad));
    }

    @Test
    @DisplayName("editing, saving, and a controller whose protocol is unknown is not written")
    void test3(@TempDir Path dir) throws Exception {
        FakeOcta pad = new FakeOcta();
        assertTrue(HoriOctaPluginBase.isOcta(pad));

        AssignPanel panel = new AssignPanel();
        panel.setController(pad);
        try {
            HoriProfile p = new HoriProfile();
            p.setName("SF6");
            p.setFunction(HoriButton.LT, HoriFunction.DISABLED);
            p.setStick(HoriStick.RS);
            panel.setProfile(1, p);
            assertEquals(p, panel.current());
            assertEquals(new HoriProfile(), panel.getProfile(2));

            Path file = dir.resolve("sf6.properties");
            panel.save(file);
            Properties props = new Properties();
            try (Reader r = Files.newBufferedReader(file)) {
                props.load(r);
            }
            assertEquals(p, HoriProfile.fromProperties(props));

            // the PlayStation model has no transport yet and must not get anything
            UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                    () -> HoriOctaPluginBase.getTransport(pad).writeMemory(1, 0, new byte[1]));
            assertTrue(e.getMessage().contains("not known"), e.getMessage());
            assertTrue(pad.writes.isEmpty());
        } finally {
            panel.stop();
        }
    }

    @Test
    @DisplayName("the probe reads feature reports and writes nothing")
    void test4() {
        FakeOcta pad = new FakeOcta();
        ProbePanel probe = new ProbePanel();
        probe.setController(pad);

        String descriptor = probe.dumpDescriptor();
        assertTrue(descriptor.contains("FEATURE report id 128 (0x80), 63 bytes"), descriptor);

        String features = probe.readFeatureReports();
        assertTrue(features.contains("0000: 00 01 02 03"), features);
        assertTrue(features.contains("0030: 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e \n"), features);
        assertTrue(pad.writes.isEmpty());

        probe.setController(null);
        assertFalse(probe.readFeatureReports().contains("FEATURE"));
    }

    /**
     * Opens the real window and leaves it up. Off by default because it never returns on
     * its own, run it with {@code -Dkeyassigner=true} or through {@code main}.
     */
    @Test
    @DisplayName("show the window")
    @EnabledIfSystemProperty(named = "keyassigner", matches = "true")
    @DisabledIf("headless")
    void test5() throws Exception {
        Controller[] controllers = HoriKeyAssigner.findControllers();
        CountDownLatch closed = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = HoriKeyAssigner.createFrame(controllers);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) {
                    closed.countDown();
                }
            });
            frame.setVisible(true);
        });
        closed.await();
    }
}
