/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;


/**
 * LinuxIO.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-18 nsano initial version <br>
 */
public interface LinuxIO extends Library {

    LinuxIO INSTANCE = Native.load("c", LinuxIO.class);

    int O_RDONLY = 0;
    int O_RDWR = 2;
    int O_NONBLOCK = 2048;

    int KEY_MAX = 0x2ff;
    int ABS_MAX = 0x3f;
    int BTN_MISC = 0x100;
    int ABS_CNT = ABS_MAX + 1;

    int _IOC_READ = 2;
    int _IOC_WRITE = 4;

    int _IOC_NRBITS = 8;
    int _IOC_TYPEBITS = 8;
    int _IOC_SIZEBITS = 13;
    int _IOC_DIRBITS = 3;

    int _IOC_NRSHIFT = 0;

    long _IOC_TYPESHIFT = _IOC_NRSHIFT + _IOC_NRBITS;
    long _IOC_SIZESHIFT = _IOC_TYPESHIFT + _IOC_TYPEBITS;
    long _IOC_DIRSHIFT = _IOC_SIZESHIFT + _IOC_SIZEBITS;

    static NativeLong _IOC(int dir, int type, int nr, int size) {
        return new NativeLong(((long) dir << _IOC_DIRSHIFT) |
                ((long) type << _IOC_TYPESHIFT) |
                ((long) nr << _IOC_NRSHIFT) |
                ((long) size << _IOC_SIZESHIFT));
    }

    static NativeLong _IOR(int type, int nr, int size) { return _IOC(_IOC_READ, type, nr, size); }
    static NativeLong _IOW(int type, int nr, int size) { return _IOC(_IOC_WRITE, type, nr, size); }

    static NativeLong EVIOCGID(int len) { return _IOR('E', 0x02, len /* input_id */); }
    NativeLong EVIOCGEFFECTS = _IOR('E', 0x84, Integer.BYTES);
    NativeLong EVIOCGVERSION = _IOR('E', 0x01, Integer.BYTES);
    static NativeLong EVIOCGABS(int abs, int len) { return _IOR('E', 0x40 + abs, len /* struct input_absinfo */); }
    static NativeLong EVIOCGBIT(int ev, int len) { return _IOC(_IOC_READ, 'E', 0x20 + ev, len); }
    static NativeLong EVIOCGKEY(int len) { return _IOC(_IOC_READ, 'E', 0x18, len); }
    static NativeLong EVIOCGNAME(int len) { return _IOC(_IOC_READ, 'E', 0x06, len); }
    static NativeLong EVIOCSFF(int len) { return  _IOC(_IOC_WRITE, 'E', 0x80, len /* struct ff_effect*/); }
    NativeLong EVIOCRMFF = _IOW('E', 0x81, Integer.BYTES);

    NativeLong JSIOCGBUTTONS = _IOR('j', 0x12, Byte.BYTES);
    NativeLong JSIOCGAXES = _IOR('j', 0x11, Byte.BYTES);
    NativeLong JSIOCGAXMAP = _IOR('j', 0x32, ABS_CNT);
    NativeLong JSIOCGBTNMAP = _IOR('j', 0x34, Short.BYTES * (KEY_MAX - BTN_MISC + 1));
    NativeLong JSIOCGVERSION = _IOR('j', 0x01, Integer.BYTES);
    static NativeLong JSIOCGNAME(int len) { return _IOC(_IOC_READ, 'j', 0x13, len); }

    int open64(String path, int flags);

    NativeLong read(int fd, Pointer buffer, NativeLong size);

    NativeLong write(int fd, Pointer buffer, NativeLong size);

    int close(int fd);

    int ioctl(int fd, NativeLong request, Pointer arg);

    int ioctl(int fd, NativeLong request, int arg);
}
