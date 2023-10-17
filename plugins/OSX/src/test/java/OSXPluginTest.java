/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Arrays;

import net.java.games.input.ControllerEnvironment;
import net.java.games.input.OSXEnvironmentPlugin;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * OSXPluginTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-23 nsano initial version <br>
 */
public class OSXPluginTest {

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
}
