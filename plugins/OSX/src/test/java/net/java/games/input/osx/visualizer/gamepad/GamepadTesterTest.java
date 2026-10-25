/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.visualizer.gamepad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * GamepadTesterTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-26 nsano initial version <br>
 */
class GamepadTesterTest {

    static boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }

    /** a pad that reads whatever the test says it reads */
    static class Fixed implements GamepadSource {

        final GamepadState state = new GamepadState();

        Fixed(String id, String mapping, int buttons, int axes) {
            state.resize(buttons, axes);
            state.setId(id);
            state.setMapping(mapping);
            state.setConnected(true);
            state.setIndex(3);
            state.setTimestamp(6022.3);
            state.setVibration(true);
        }

        static Fixed standard() {
            return new Fixed("Wireless Controller (STANDARD GAMEPAD)", GamepadState.STANDARD,
                    GamepadState.STANDARD_BUTTONS, 4);
        }

        @Override public GamepadState poll() {
            return state;
        }

        @Override public void close() {
        }
    }

    @Test
    @DisplayName("the path parser puts the outline where the SVG says it is")
    void test1() {
        // the body of the pad, out of the view box the artwork is drawn in
        Rectangle2D bounds = SvgPath.parse(
                "M10 20C10 30 20 40 30 40L60 40C70 40 80 30 80 20V10H10Z").getBounds2D();
        assertEquals(10, bounds.getMinX(), 1e-9);
        assertEquals(10, bounds.getMinY(), 1e-9);
        assertEquals(80, bounds.getMaxX(), 1e-9);
        assertEquals(40, bounds.getMaxY(), 1e-9);

        // relative commands, and the repeated form where the letter is left out
        Rectangle2D relative = SvgPath.parse("m10 10 l10 0 10 10z").getBounds2D();
        assertEquals(10, relative.getMinX(), 1e-9);
        assertEquals(30, relative.getMaxX(), 1e-9);
        assertEquals(20, relative.getMaxY(), 1e-9);
    }

    @Test
    @DisplayName("each pad gets the drawing that fits it")
    void test2() {
        GamepadArt xbox = GamepadArts.find(Fixed.standard().state);
        assertInstanceOf(StandardGamepadArt.class, xbox);
        assertTrue(xbox.getName().contains("xbox"), xbox.getName());

        GamepadState sony = new Fixed("DualShock 4 Wireless Controller", GamepadState.STANDARD,
                GamepadState.STANDARD_BUTTONS, 4).state;
        assertTrue(GamepadArts.find(sony).getName().contains("playstation"),
                GamepadArts.find(sony).getName());

        // a stick is not a pad, and must not be drawn as one
        GamepadState stick = new Fixed("Some Flight Stick", "", 8, 3).state;
        assertInstanceOf(GenericGamepadArt.class, GamepadArts.find(stick));
    }

    @Test
    @DisplayName("the demo drives every button and both sticks")
    void test3() {
        DemoGamepadSource source = new DemoGamepadSource();
        GamepadState state = source.poll();

        boolean[] seen = new boolean[GamepadState.STANDARD_BUTTONS];
        double maxDeflection = 0;
        for (int i = 0; i < 1000; i++) {
            source.step(0.1);
            for (int b = 0; b < seen.length; b++) {
                seen[b] |= state.getButton(b) > 0;
            }
            maxDeflection = Math.max(maxDeflection, Math.hypot(state.getAxis(0), state.getAxis(1)));
        }
        for (int b = 0; b < seen.length; b++) {
            assertTrue(seen[b], "button " + b + " was never pressed");
        }
        assertEquals(1, maxDeflection, 0.01);
    }

    /**
     * The layout is a copy of a page, so the way to know it is still a copy is to measure
     * the drawing again. These are the columns and rows the reference screenshot in
     * {@code tmp/gamepadtester.png} has, at the two times scale it was taken at. Only the
     * parts that are drawn rather than typed are checked: the text is laid out with whatever
     * font is installed and lands a pixel or so off Roboto's widths.
     */
    @Test
    @DisplayName("the drawing lands where the page puts it")
    @DisabledIf("headless")
    void test4() {
        BufferedImage image = render(Fixed::standard);

        // the windows start clear of the readouts to the left, whose width is the font's
        assertEquals("228..457", band(image, true, 508, 745, 210, 500), "left joystick");
        assertEquals("698..927", band(image, true, 508, 745, 680, 960), "right joystick");
        assertEquals("1157..1848", band(image, true, 300, 845, 1050, 1962), "the body");
        assertEquals("300..788", band(image, false, 290, 990, 1050, 1962), "the body");
        assertEquals("158..229", band(image, false, 100, 240, 1050, 1962), "the triggers");

        // the bar of the first button readout, and of the first axis readout under it
        assertEquals("43..52", band(image, true, 300, 378, 0, 60), "button bar");
        assertEquals("43..52", band(image, true, 560, 745, 0, 60), "axis bar");
    }

    @Test
    @DisplayName("a pressed button darkens the drawing that was drawn without it")
    @DisabledIf("headless")
    void test5() {
        BufferedImage released = render(Fixed::standard);
        BufferedImage pressed = render(() -> {
            Fixed source = Fixed.standard();
            source.state.setButton(0, 1);
            return source;
        });

        // the bottom face button, at the middle of the right hand cluster of four
        int x = 1675, y = 1 + 438;
        assertNotEquals(released.getRGB(x, y), pressed.getRGB(x, y));
        assertTrue((pressed.getRGB(x, y) & 0xff) < 0x40, "should have filled black");
    }

    /** paints the panel at the scale the reference screenshot was taken at */
    private static BufferedImage render(java.util.function.Supplier<GamepadSource> source) {
        GamepadTesterPanel panel = new GamepadTesterPanel(source);
        panel.setSize((int) GamepadTesterPanel.DESIGN_WIDTH, (int) GamepadTesterPanel.DESIGN_HEIGHT);

        BufferedImage image = new BufferedImage((int) GamepadTesterPanel.DESIGN_WIDTH * 2,
                (int) GamepadTesterPanel.DESIGN_HEIGHT * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.scale(2, 2);
        panel.paint(g);
        g.dispose();
        return image;
    }

    /**
     * The one run of columns, or rows, that is not white inside the given box.
     *
     * @return {@code "from..to"}, or every run when there turns out to be more than one
     */
    private static String band(BufferedImage image, boolean columns, int y0, int y1, int x0, int x1) {
        StringBuilder sb = new StringBuilder();
        int outerFrom = columns ? x0 : y0, outerTo = columns ? x1 : y1;
        int start = -1;
        for (int i = outerFrom; i < outerTo; i++) {
            boolean any = false;
            for (int j = columns ? y0 : x0; j < (columns ? y1 : x1) && !any; j++) {
                int rgb = image.getRGB(columns ? i : j, columns ? j : i) & 0xffffff;
                any = (rgb >> 16) < 0xf7 || ((rgb >> 8) & 0xff) < 0xf7 || (rgb & 0xff) < 0xf7;
            }
            if (any && start < 0) {
                start = i;
            } else if (!any && start >= 0) {
                sb.append(sb.isEmpty() ? "" : " ").append(start).append("..").append(i - 1);
                start = -1;
            }
        }
        if (start >= 0) sb.append(sb.isEmpty() ? "" : " ").append(start).append("..").append(outerTo - 1);
        return sb.toString();
    }

    /**
     * Opens the real window and leaves it up. Off by default because it never returns on
     * its own, run it with {@code -Dvisualizer=true} or through {@code main}.
     */
    @Test
    @DisplayName("show the window")
    @EnabledIfSystemProperty(named = "visualizer", matches = "true")
    @DisabledIf("headless")
    void test6() throws Exception {
        CountDownLatch closed = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = GamepadTester.createFrame();
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
