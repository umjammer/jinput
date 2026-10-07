/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;


/**
 * What a button can be made to send, with the code the controller stores for it.
 * Codes are from <a href="https://github.com/mbenkmann/hori_device_manager">hori_device_manager</a>.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public enum HoriFunction {

    DPAD_UP("DPAD-UP", 0x01),
    DPAD_DOWN("DPAD-DOWN", 0x02),
    DPAD_LEFT("DPAD-LEFT", 0x03),
    DPAD_RIGHT("DPAD-RIGHT", 0x04),
    LB("LB", 0x05),
    RB("RB", 0x06),
    LSB("LSB", 0x07),
    RSB("RSB", 0x08),
    LT("LT", 0x0d),
    RT("RT", 0x0e),
    A("A", 0x0f),
    B("B", 0x10),
    X("X", 0x11),
    Y("Y", 0x12),
    GUIDE("GUIDE", 0x15),
    SELECT("SELECT", 0x16),
    START("START", 0x17),
    SHARE("SHARE", 0x18),
    LS_DOWN("LS-DOWN", 0x19),
    LS_UP("LS-UP", 0x1a),
    LS_LEFT("LS-LEFT", 0x1b),
    LS_RIGHT("LS-RIGHT", 0x1c),
    RS_DOWN("RS-DOWN", 0x1d),
    RS_UP("RS-UP", 0x1e),
    RS_LEFT("RS-LEFT", 0x1f),
    RS_RIGHT("RS-RIGHT", 0x20),
    DISABLED("DISABLED", 0x21);

    /** name as hori.py takes it */
    public final String label;
    /** the byte stored in the profile */
    public final int code;

    HoriFunction(String label, int code) {
        this.label = label;
        this.code = code;
    }

    /** @return {@link #DISABLED} for a code nobody knows, as hori.py does */
    public static HoriFunction valueOf(int code) {
        for (HoriFunction f : values()) {
            if (f.code == code) return f;
        }
        return DISABLED;
    }

    /** case insensitive, takes hori.py's names and aliases (VIEW, XBOX, MENU) */
    public static HoriFunction parse(String name) {
        String n = name.trim().toUpperCase();
        switch (n) {
        case "VIEW": return SELECT;
        case "XBOX": return GUIDE;
        case "MENU": return START;
        }
        for (HoriFunction f : values()) {
            if (f.label.equals(n) || f.name().equals(n)) return f;
        }
        throw new IllegalArgumentException("unknown function: " + name);
    }

    @Override
    public String toString() {
        return label;
    }
}
