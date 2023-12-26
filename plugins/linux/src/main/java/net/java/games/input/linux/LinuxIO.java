/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.linux;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.linux.Udev;
import com.sun.jna.ptr.ByReference;


/**
 * LinuxIO.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-10-18 nsano initial version <br>
 */
public interface LinuxIO extends Library {

    LinuxIO INSTANCE = Native.load("c", LinuxIO.class);

    int PATH_MAX = 4096;

    int O_RDONLY = 0;
    int O_RDWR = 2;
    int O_CLOEXEC = 0x0001;
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

    static NativeLong HIDIOCSFEATURE(int len) { return _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x06, len); }
    static NativeLong HIDIOCGFEATURE(int len) { return _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len); }
    static NativeLong HIDIOCGINPUT(int len) { return _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x0A, len); }
    NativeLong HIDIOCGRDESCSIZE = _IOR('H', 0x01, Integer.BYTES);
    static NativeLong HIDIOCGRDESC(int len) { return _IOR('H', 0x02, len /* struct hidraw_report_descriptor */); }

    class dirent extends Structure {
        public int d_fileno;
        public short d_reclen;
        public byte d_type;
        public byte d_namlen;
        public byte[] d_name = new byte[255 + 1];
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("d_fileno", "d_reclen", "d_type", "d_namlen", "d_name");
        }
    }

    class stat extends Structure {

        /** ID of device containing file */
        public NativeLong st_dev;

        /** Inode number */
        public NativeLong st_ino;

        /** File type and mode */
        public NativeLong st_mode;

        /** Number of hard links */
        public NativeLong st_nlink;
        /** User ID of owner */
        public int st_uid;
        /** Group ID of owner */
        public int st_gid;
        /** Device ID (if special file) */
        public NativeLong st_rdev;
        /** Total size, in bytes */
        public NativeLong st_size;
        /** Block size for filesystem I/O */
        public NativeLong st_blksize;
        /** Number of 512B blocks allocated */
        public NativeLong st_blocks;

        // Since Linux 2.6, the kernel supports nanosecond
        // precision for the following timestamp fields.
        // For the details before Linux 2.6, see NOTES.

        /** Time of last access */
        public timeval st_atim;
        /** Time of last modification */
        public timeval st_mtim;
        /** Time of last status change */
        public timeval st_ctim;

        /**
         * C type : __syscall_slong_t[3]
         */
        public NativeLong[] __unused = new NativeLong[3];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "st_dev", "st_ino",  "st_nlink", "st_mode", "st_uid", "st_gid", "st_rdev", "st_size", "st_blksize",
                    "st_blocks", "st_atim", "st_mtim", "st_ctim", "__unused"
            );
        }

        public static class ByReference extends stat implements Structure.ByReference {
        }

        public static class ByValue extends stat implements Structure.ByValue {
        }
    }

    class input_event extends Structure {
        public timeval time;
        public short type;
        public short code;
        public int value;
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("time", "type", "code", "value");
        }
    }

    class input_id extends Structure {
        public short bustype;
        public short vendor;
        public short product;
        public short version;
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("bustype", "vendor", "product", "version");
        }
    }

    class input_absinfo extends Structure {
        public int value;
        public int minimum;
        public int maximum;
        public int fuzz;
        public int flat;
        public int resolution;
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("value", "minimum", "maximum", "fuzz", "flat", "resolution");
        }
    }

    int HID_MAX_DESCRIPTOR_SIZE = 4096;

    class hidraw_report_descriptor extends Structure {
        public int size;
        public byte[] value = new byte[HID_MAX_DESCRIPTOR_SIZE];
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("size", "value");
        }
    }

    int open64(String path, int flags);

    int open(String pathname, int flags, int /* mode_t */ mode);

    int open(String pathname, int flags);

    NativeLong read(int fd, Pointer buffer, NativeLong size);

    NativeLong write(int fd, Pointer buffer, NativeLong size);

    int close(int fd);

    int ioctl(int fd, NativeLong request, Pointer arg);

    int ioctl(int fd, NativeLong request, ByReference arg);

    int ioctl(int fd, NativeLong request, int arg);

    int ioctl(int fd, NativeLong request, int[] arg);

    int ioctl(int fd, NativeLong request, byte[] arg);

    Pointer /* DIR */ opendir(String name);

    dirent readdir(Pointer /* DIR */ dirp);

    int closedir(Pointer /* DIR */ dirp);

    int stat(String pathname, stat buf);

    int fstat(int fildes, stat buf);

    int sscanf(byte[] str, String fotmat, Object...args);

    NativeLong strlen(byte[] string);

    interface UdevEx extends Udev {

        UdevEx INSTANCE = Native.load("udev", UdevEx.class);

        Udev.UdevDevice udev_device_new_from_devnum(Udev.UdevContext udev, byte type, NativeLong devnum);
    }
}
