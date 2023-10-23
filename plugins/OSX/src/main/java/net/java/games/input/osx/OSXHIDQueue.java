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

package net.java.games.input.osx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;
import vavix.rococoa.iokit.IOKitLib;
import vavix.rococoa.iokit.IOKitLib.IOHIDEventStruct;
import vavix.rococoa.iokit.IOKitLib.IOHIDQueueInterface;

import static vavix.rococoa.iokit.IOKitLib.kIOReturnSuccess;
import static vavix.rococoa.iokit.IOKitLib.log;


/**
 * @author elias
 * @version 1.0
 */
final class OSXHIDQueue {

    /** key: IOHIDElementCookie */
    private final Map<Integer, OSXComponent> map = new HashMap<>();
    private final Pointer /* IOHIDQueueInterface** */ queueAddress;
    private final IOHIDQueueInterface queue;

    private boolean released;

    public OSXHIDQueue(Pointer /* IOHIDQueueInterface** */ address, int queueDepth) throws IOException {
        this.queueAddress = address;
        this.queue = new IOHIDQueueInterface(address.getPointer(0));
        try {
            createQueue(queueDepth);
        } catch (IOException e) {
            release();
            throw e;
        }
    }

    public synchronized void setQueueDepth(int queueDepth) throws IOException {
        checkReleased();
        stop();
        close();
        createQueue(queueDepth);
    }

    private void createQueue(int queueDepth) throws IOException {
        open(queueDepth);
        try {
            start();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public OSXComponent mapEvent(OSXEvent event) {
        return map.get(event.getCookie());
    }

    private void open(int queueDepth) throws IOException {
        int ioReturnValue = queue.create.invoke(queueAddress, 0, queueDepth);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue open failed: %x", ioReturnValue));
        }
    }

    private void close() throws IOException {
        int ioReturnValue = queue.dispose.invoke(queueAddress);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue dispose failed: %x", ioReturnValue));
        }
    }

    private void start() throws IOException {
        int ioReturnValue = queue.start.invoke(queueAddress);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue start failed: %x", ioReturnValue));
        }
    }

    private void stop() throws IOException {
        int ioReturnValue = queue.stop.invoke(queueAddress);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue stop failed: %x", ioReturnValue));
        }
    }

    public synchronized void release() throws IOException {
        if (!released) {
            released = true;
            try {
                stop();
                close();
            } finally {
                int ioReturnValue = queue.release.invoke(queueAddress).intValue();
                if (ioReturnValue != kIOReturnSuccess) {
                    log.warning(String.format("Queue Release failed: %x", ioReturnValue));
                }
            }
        }
    }

    public void addElement(OSXHIDElement element, OSXComponent component) throws IOException {
        int ioReturnValue = queue.addElement.invoke(queueAddress, element.getCookie(), 0);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue addElement failed: %x", ioReturnValue));
        }
        map.put(element.getCookie(), component);
    }

    public void removeElement(OSXHIDElement element) throws IOException {
        int ioReturnValue = queue.removeElement.invoke(queueAddress, element.getCookie(), 0);
        if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue removeElement failed: %x", ioReturnValue));
        }
        map.remove(element.getCookie());
    }

    public synchronized boolean getNextEvent(OSXEvent event) throws IOException {
        checkReleased();

        long /* AbsoluteTime */ zeroTime = 0;
        IOHIDEventStruct.ByReference nativeEvent = new IOHIDEventStruct.ByReference();
        int ioReturnValue = queue.getNextEvent.invoke(queueAddress, nativeEvent, zeroTime, 0);
        if (ioReturnValue == IOKitLib.kIOReturnUnderrun) {
            return false;
        } else if (ioReturnValue != kIOReturnSuccess) {
            throw new IOException(String.format("Queue getNextEvent failed: %x", ioReturnValue));
        }
        event.set(nativeEvent);
        return true;
    }

    private void checkReleased() throws IOException {
        if (released)
            throw new IOException("Queue is released");
    }
}
