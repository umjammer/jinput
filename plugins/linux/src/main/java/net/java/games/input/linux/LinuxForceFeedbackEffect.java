/**
 * Copyright (C) 2003 Jeremy Booth (jeremy@newdawnsoftware.com)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

package net.java.games.input.linux;

import java.io.IOException;
import java.util.logging.Logger;

import net.java.games.input.Component;
import net.java.games.input.Rumbler;


/**
 * @author elias
 */
abstract class LinuxForceFeedbackEffect implements Rumbler {

    private static final Logger log = Logger.getLogger(LinuxForceFeedbackEffect.class.getName());

    private final LinuxEventDevice device;
    private final int ff_id;
    private final WriteTask write_task = new WriteTask();
    private final UploadTask upload_task = new UploadTask();

    public LinuxForceFeedbackEffect(LinuxEventDevice device) throws IOException {
        this.device = device;
        this.ff_id = upload_task.doUpload(-1, 0);
    }

    protected abstract int upload(int id, float intensity) throws IOException;

    protected final LinuxEventDevice getDevice() {
        return device;
    }

    @Override
    public synchronized final void rumble(float intensity) {
        try {
            if (intensity > 0) {
                upload_task.doUpload(ff_id, intensity);
                write_task.write(1);
            } else {
                write_task.write(0);
            }
        } catch (IOException e) {
            log.fine("Failed to rumble: " + e);
        }
    }

//    /**
//     * Erase doesn't seem to be implemented on Logitech joysticks,
//     * so we'll rely on the kernel cleaning up on device close
//     */
//    public final void erase() throws IOException {
//        device.eraseEffect(ff_id);
//    }

    @Override
    public final String getAxisName() {
        return null;
    }

    @Override
    public final Component.Identifier getAxisIdentifier() {
        return null;
    }

    private final class UploadTask extends LinuxDeviceTask {

        private int id;
        private float intensity;

        public int doUpload(int id, float intensity) throws IOException {
            this.id = id;
            this.intensity = intensity;
            LinuxEnvironmentPlugin.execute(this);
            return this.id;
        }

        @Override
        protected Object execute() throws IOException {
            this.id = upload(id, intensity);
            return null;
        }
    }

    private final class WriteTask extends LinuxDeviceTask {

        private int value;

        public void write(int value) throws IOException {
            this.value = value;
            LinuxEnvironmentPlugin.execute(this);
        }

        @Override
        protected Object execute() throws IOException {
            device.writeEvent(NativeDefinitions.EV_FF, ff_id, value);
            return null;
        }
    }
}
