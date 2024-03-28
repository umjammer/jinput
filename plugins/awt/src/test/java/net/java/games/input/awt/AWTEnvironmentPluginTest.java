/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.awt;

import net.java.games.input.ControllerEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * AWTEnvironmentPluginTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-29 nsano initial version <br>
 */
class AWTEnvironmentPluginTest {

    @Test
    void test1() throws Exception {
        ControllerEnvironment environment = ControllerEnvironment.getEnvironmentByName("net.java.games.input.awt");
        assertInstanceOf(AWTEnvironmentPlugin.class, environment);
    }
}