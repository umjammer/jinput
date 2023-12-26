/*
 * Copyright (C) 2003 Jeremy Booth (jeremy@newdawnsoftware.com)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
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

package net.java.games.input;

public class RumbleTest {

    /** Creates a new instance of RumbleTest */
    public RumbleTest() {
        ControllerEnvironment ca = ControllerEnvironment.getDefaultEnvironment();
        System.out.println("JInput version: " + Version.getVersion());
        Controller[] controllers = ca.getControllers();
        for (Controller controller : controllers) {
            System.out.println("Scanning " + controller.getName());
            Rumbler[] rumblers = controller.getRumblers();
            System.out.println("Found " + rumblers.length + " rumblers");
            for (Rumbler rumbler : rumblers) {
                System.out.println("Rumbler " + rumbler.getOutputName() + " on axis " + rumbler.getOutputIdentifier());
                System.out.println("Rumbling with intensity: " + 0.5f);
                rumbler.setValue(0.5f);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                System.out.println("Rumbling with intensity: " + 1.0f);
                rumbler.setValue(1f);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                System.out.println("Fading rumble to -1");
                for (float k = 1.0f; k > -1.0f; ) {
                    long startTime = System.currentTimeMillis();
                    rumbler.setValue(k);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                    k -= ((float) (System.currentTimeMillis() - startTime)) / 1000f;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                System.out.println("Rumbling with intensity: " + 0.0f);
                rumbler.setValue(0f);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new RumbleTest();
    }

}
