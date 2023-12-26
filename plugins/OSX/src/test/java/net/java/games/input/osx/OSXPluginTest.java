/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import net.java.games.input.ControllerEnvironment;
import net.java.games.input.osx.plugin.DualShock4Plugin.Report5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * OSXPluginTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-23 nsano initial version <br>
 */
@EnabledOnOs(OS.MAC)
@PropsEntity(url = "file:local.properties")
public class OSXPluginTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "mid")
    String mid;
    @Property(name = "pid")
    String pid;

    int vendorId;
    int productId;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);

            vendorId = Integer.decode(mid);
            productId = Integer.decode(pid);
        }
    }

    @Test
    void test1() throws Exception {
        OSXEnvironmentPlugin plugin = new OSXEnvironmentPlugin();
Debug.println("getControllers: " + plugin.getControllers().length);
        Arrays.stream(plugin.getControllers()).forEach(System.err::println);
    }

    @Test
    void test2() throws Exception {
        Arrays.stream(ControllerEnvironment.getDefaultEnvironment().getControllers()).forEach(System.err::println);
    }

    @Test
    @EnabledIf("localPropertiesExists")
    @DisplayName("components count")
    void test3() throws Exception {
        OSXEnvironmentPlugin plugin = new OSXEnvironmentPlugin();
        OSXController controller = Arrays.stream(plugin.getControllers())
                .filter(c -> c instanceof OSXController)
                .map(c -> (OSXController) c)
                .filter(c -> c.getProductId() == productId && c.getVendorId() == vendorId)
                .findFirst().get();
        assertEquals(21, controller.getComponents().length);
    }

    @Test
    @EnabledIf("localPropertiesExists")
    @DisplayName("rumbler")
    void test4() throws Exception {
        OSXEnvironmentPlugin plugin = new OSXEnvironmentPlugin();
        OSXController controller = Arrays.stream(plugin.getControllers())
                .filter(c -> c instanceof OSXController)
                .map(c -> (OSXController) c)
                .filter(c -> c.getProductId() == productId && c.getVendorId() == vendorId)
                .findFirst().get();

        Report5 report = new Report5();
        report.smallRumble = 255;
        report.bigRumble = 0;
        report.ledRed = 255;
        report.ledGreen = 0;
        report.ledBlue = 0;
        report.flashLed1 = 0;
        report.flashLed2 = 0;

        controller.output(report);
    }
}
