/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 *
 */

package net.java.games.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;
import vavix.rococoa.iokit.IOKitLib;
import vavix.rococoa.iokit.IOKitLib.IOHIDEventStruct;
import vavix.rococoa.iokit.IOKitLib.IOHIDQueueInterface;

import static net.java.games.input.NativeUtil.copyEvent;
import static vavix.rococoa.iokit.IOKitLib.kIOReturnSuccess;
import static vavix.rococoa.iokit.IOKitLib.log;


/**
 * @author elias
 * @version 1.0
 */
final class OSXHIDQueue {

    private final Map<Pointer, OSXComponent> map = new HashMap<>();
    private final Pointer /* IOHIDQueueInterface** */ queue_address;
    private final IOHIDQueueInterface queue;

    private boolean released;

    public OSXHIDQueue(Pointer /* IOHIDQueueInterface** */  address, int queue_depth) throws IOException {
        this.queue_address = address;
        this.queue = new IOHIDQueueInterface(address.getPointer(0));
        try {
            createQueue(queue_depth);
        } catch (IOException e) {
            release();
            throw e;
        }
    }

    public final synchronized void setQueueDepth(int queue_depth) throws IOException {
        checkReleased();
        stop();
        close();
        createQueue(queue_depth);
    }

    private void createQueue(int queue_depth) throws IOException {
        open(queue_depth);
        try {
            start();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public final OSXComponent mapEvent(OSXEvent event) {
        return map.get(new Pointer(event.getCookie()));
    }

    private void open(int queue_depth) throws IOException {
        int ioReturnValue = queue.create.invoke(queue_address, 0, queue_depth);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue open failed: " + ioReturnValue);
        }
    }

    private void close() throws IOException {
        int ioReturnValue = queue.dispose.invoke(queue_address);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue dispose failed: " + ioReturnValue);
        }
    }

    private void start() throws IOException {
        int ioReturnValue = queue.start.invoke(queue_address);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue start failed: " + ioReturnValue);
        }
    }

    private void stop() throws IOException {
        int ioReturnValue = queue.stop.invoke(queue_address);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue stop failed: " + ioReturnValue);
        }
    }

    public synchronized void release() throws IOException {
        if (!released) {
            released = true;
            try {
                stop();
                close();
            } finally {
                int ioReturnValue = queue.release.invoke(queue_address).intValue();
                if (ioReturnValue != kIOReturnSuccess) {
                    log.warning("Queue Release failed: " + ioReturnValue);
                }
            }
        }
    }

    public void addElement(OSXHIDElement element, OSXComponent component) throws IOException {
        int ioReturnValue = queue.addElement.invoke(queue_address, element.getCookie(), 0);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue addElement failed: " + ioReturnValue);
        }
        map.put(element.getCookie(), component);
    }

    private static void nAddElement(IOHIDQueueInterface queue, Pointer /* IOHIDElementCookie */ cookie) throws IOException {
    }

    public void removeElement(OSXHIDElement element) throws IOException {
        int ioReturnValue = queue.removeElement.invoke(queue_address, element.getCookie(), 0);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException("Queue removeElement failed: " + ioReturnValue);
        }
        map.remove(element.getCookie());
    }

    public synchronized boolean getNextEvent(OSXEvent event) throws IOException {
        checkReleased();

        long /* AbsoluteTime */ zeroTime = 0;
        IOHIDEventStruct.ByReference nEvent = new IOHIDEventStruct.ByReference();
        int ioReturnValue = queue.getNextEvent.invoke(queue_address, nEvent, zeroTime, 0);
        if (ioReturnValue == IOKitLib.kIOReturnUnderrun) {
            return false;
        } else if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue getNextEvent failed: %x", ioReturnValue));
        }
        copyEvent(nEvent, event);
        return true;
    }

    private void checkReleased() throws IOException {
        if (released)
            throw new IOException("Queue is released");
    }
}
