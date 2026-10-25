/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;

import java.io.IOException;


/**
 * The way profile memory gets to and from a controller.
 * <p>
 * Kept apart from the profile layout because the layout is known (see {@link HoriProfileMemory})
 * while the wire protocol differs per model: the Xbox model speaks GIP over raw usb, and how the
 * PlayStation model is talked to is not known yet.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public interface HoriTransport {

    /** @param profile 1..4 */
    byte[] readMemory(int profile, int offset, int length) throws IOException;

    /** @param profile 1..4 */
    void writeMemory(int profile, int offset, byte[] data) throws IOException;

    /** @return 1..4 */
    int getActiveProfile() throws IOException;

    /** @param profile 1..4 */
    void activateProfile(int profile) throws IOException;

    /** for a model whose protocol is not known, every call fails and nothing is sent */
    static HoriTransport unsupported(String reason) {
        return new HoriTransport() {
            @Override public byte[] readMemory(int profile, int offset, int length) {
                throw new UnsupportedOperationException(reason);
            }

            @Override public void writeMemory(int profile, int offset, byte[] data) {
                throw new UnsupportedOperationException(reason);
            }

            @Override public int getActiveProfile() {
                throw new UnsupportedOperationException(reason);
            }

            @Override public void activateProfile(int profile) {
                throw new UnsupportedOperationException(reason);
            }
        };
    }
}
