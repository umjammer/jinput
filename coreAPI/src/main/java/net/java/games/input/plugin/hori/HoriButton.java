/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;


/**
 * The physical buttons of a HORI Fighting Commander OCTA whose function can be reassigned,
 * with where each one's entry sits in a profile's memory.
 * <p>
 * Names and offsets come from <a href="https://github.com/mbenkmann/hori_device_manager">hori_device_manager</a>,
 * which reverse engineered the Xbox model. The PlayStation labels are the buttons at the same
 * place on the PlayStation model; the one under {@link #SHARE} is a guess.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public enum HoriButton {

    A("A", "Cross", 0xff),
    B("B", "Circle", 0x10d),
    X("X", "Square", 0x11b),
    Y("Y", "Triangle", 0x129),
    LB("LB", "L1", 0x73),
    RB("RB", "R1", 0x81),
    LT("LT", "L2", 0xe3),
    RT("RT", "R2", 0xf1),
    LSB("LSB", "L3", 0x8f),
    RSB("RSB", "R3", 0x9d),
    SELECT("SELECT", "Create", 0x161),
    GUIDE("GUIDE", "PS", 0x153),
    SHARE("SHARE", "Touchpad", 0x17d),
    START("START", "Options", 0x16f);

    /** name as hori.py takes it */
    public final String xboxLabel;
    /** name printed on the PlayStation model */
    public final String psLabel;
    /** where the 8 byte assignment entry of this button starts in a profile */
    public final int offset;

    HoriButton(String xboxLabel, String psLabel, int offset) {
        this.xboxLabel = xboxLabel;
        this.psLabel = psLabel;
        this.offset = offset;
    }

    /** what the button sends when nothing is assigned to it */
    public HoriFunction defaultFunction() {
        return HoriFunction.valueOf(name());
    }

    /** case insensitive, takes hori.py's aliases (VIEW, XBOX, MENU) and the PlayStation labels too */
    public static HoriButton parse(String name) {
        String n = name.trim().toUpperCase();
        switch (n) {
        case "VIEW": return SELECT;
        case "XBOX": return GUIDE;
        case "MENU": return START;
        }
        for (HoriButton b : values()) {
            if (b.name().equals(n) || b.psLabel.equalsIgnoreCase(n)) return b;
        }
        throw new IllegalArgumentException("unknown button: " + name);
    }
}
