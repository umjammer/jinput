/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.osx.keyassign;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.java.games.input.usb.HidReportType;


/**
 * A plain walk over a hid report descriptor: one line per item, and how long each report is.
 * <p>
 * Made for finding the vendor reports of a device, the feature reports are where settings
 * usually live, so it keeps every item instead of only the ones jinput turns into components.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
final class HidDescriptorItems {

    /** a report the descriptor declares, length in bytes without the report id */
    record ReportInfo(HidReportType type, int id, int bytes) {

        @Override public String toString() {
            return "%s report id %d (0x%02x), %d bytes".formatted(type, id, id, bytes);
        }
    }

    final List<String> lines = new ArrayList<>();
    final List<ReportInfo> reports = new ArrayList<>();

    private HidDescriptorItems() {
    }

    private static final class Globals implements Cloneable {
        int usagePage, reportSize, reportCount, reportId;

        @Override public Globals clone() {
            try {
                return (Globals) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static HidDescriptorItems parse(byte[] descriptor) {
        HidDescriptorItems result = new HidDescriptorItems();
        Map<String, Integer> bits = new LinkedHashMap<>();
        Globals g = new Globals();
        Deque<Globals> stack = new ArrayDeque<>();
        int depth = 0;

        int i = 0;
        while (i < descriptor.length) {
            int prefix = descriptor[i] & 0xff;
            if (prefix == 0xfe) { // long item
                int size = i + 1 < descriptor.length ? descriptor[i + 1] & 0xff : 0;
                result.lines.add(hex(descriptor, i, Math.min(descriptor.length, i + 3 + size)) + "Long Item");
                i += 3 + size;
                continue;
            }
            int size = prefix & 3;
            if (size == 3) size = 4;
            int type = (prefix >> 2) & 3;
            int tag = (prefix >> 4) & 0xf;
            int end = Math.min(descriptor.length, i + 1 + size);
            long value = 0;
            for (int j = end - 1; j > i; j--) value = (value << 8) | (descriptor[j] & 0xff);
            int v = (int) value;

            String name;
            String indent = "  ".repeat(Math.max(0, depth));
            switch (type) {
            case 0 -> { // main
                switch (tag) {
                case 0x8, 0x9, 0xb -> {
                    HidReportType reportType = tag == 0x8 ? HidReportType.INPUT : tag == 0x9 ? HidReportType.OUTPUT : HidReportType.FEATURE;
                    bits.merge(reportType + ":" + g.reportId, g.reportSize * g.reportCount, Integer::sum);
                    name = (tag == 0x8 ? "Input" : tag == 0x9 ? "Output" : "Feature") + " (" + ((v & 1) != 0 ? "Const" : "Data") + ", " + ((v & 2) != 0 ? "Var" : "Array") + ")";
                }
                case 0xa -> {
                    name = "Collection (0x%02x)".formatted(v);
                    depth++;
                }
                case 0xc -> {
                    depth--;
                    indent = "  ".repeat(Math.max(0, depth));
                    name = "End Collection";
                }
                default -> name = "Main ?" + tag;
                }
            }
            case 1 -> { // global
                switch (tag) {
                case 0x0 -> { g.usagePage = v; name = "Usage Page (0x%04x)%s".formatted(v, v >= 0xff00 ? " vendor defined" : ""); }
                case 0x1 -> name = "Logical Minimum (%d)".formatted(signed(value, size));
                case 0x2 -> name = "Logical Maximum (%d)".formatted(signed(value, size));
                case 0x3 -> name = "Physical Minimum (%d)".formatted(signed(value, size));
                case 0x4 -> name = "Physical Maximum (%d)".formatted(signed(value, size));
                case 0x5 -> name = "Unit Exponent (%d)".formatted(v);
                case 0x6 -> name = "Unit (0x%x)".formatted(v);
                case 0x7 -> { g.reportSize = v; name = "Report Size (%d)".formatted(v); }
                case 0x8 -> { g.reportId = v; name = "Report ID (%d)".formatted(v); }
                case 0x9 -> { g.reportCount = v; name = "Report Count (%d)".formatted(v); }
                case 0xa -> { stack.push(g.clone()); name = "Push"; }
                case 0xb -> { if (!stack.isEmpty()) g = stack.pop(); name = "Pop"; }
                default -> name = "Global ?" + tag;
                }
            }
            case 2 -> { // local
                switch (tag) {
                case 0x0 -> name = "Usage (0x%x)".formatted(v);
                case 0x1 -> name = "Usage Minimum (0x%x)".formatted(v);
                case 0x2 -> name = "Usage Maximum (0x%x)".formatted(v);
                default -> name = "Local ?" + tag;
                }
            }
            default -> name = "Reserved";
            }
            result.lines.add(hex(descriptor, i, end) + indent + name);
            i += 1 + size;
        }

        bits.forEach((key, b) -> {
            String[] kv = key.split(":");
            result.reports.add(new ReportInfo(HidReportType.valueOf(kv[0]), Integer.parseInt(kv[1]), (b + 7) / 8));
        });
        return result;
    }

    /** the feature reports, where a device keeps its settings */
    List<ReportInfo> featureReports() {
        return reports.stream().filter(r -> r.type() == HidReportType.FEATURE).toList();
    }

    private static long signed(long value, int size) {
        return switch (size) {
            case 1 -> (byte) value;
            case 2 -> (short) value;
            case 4 -> (int) value;
            default -> value;
        };
    }

    private static String hex(byte[] b, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) sb.append("%02x ".formatted(b[i] & 0xff));
        while (sb.length() < 16) sb.append(' ');
        return sb.toString();
    }

    /** classic hex dump, 16 per line */
    static String hexDump(byte[] b, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i += 16) {
            sb.append("%04x: ".formatted(i));
            for (int j = i; j < Math.min(length, i + 16); j++) sb.append("%02x ".formatted(b[j] & 0xff));
            sb.append('\n');
        }
        return sb.toString();
    }
}
