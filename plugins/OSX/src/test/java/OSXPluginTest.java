/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import net.java.games.input.OSXEnvironmentPlugin;
import org.junit.jupiter.api.Test;


/**
 * OSXPluginTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-09-28 nsano initial version <br>
 */
public class OSXPluginTest {

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "net\\.java\\.games\\.input\\.ControllerEnvironment#log");
    }

    @Test
    void test1() throws Exception {
        OSXEnvironmentPlugin plugin = new OSXEnvironmentPlugin();
    }
}
