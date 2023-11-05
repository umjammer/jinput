/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its
 * contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package vavi.hid.parser;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Mandatory items for REPORT
 * <pre>
 * Input (Output or Feature)
 * Usage
 *  Usage Page
 *  Logical Minimum
 *  Logical Maximum
 *  Report Size
 *  Report Count
 * </pre>
 *
 * @see "http://msdn.microsoft.com/en-us/library/windows/hardware/hh975383.aspx"
 * @see "https://github.com/nyholku/purejavahidapi"
 */
public class HidParser {

    private static final Logger logger = Logger.getLogger(HidParser.class.getName());

    private Collection rootCollection;
    private Collection topCollection;
    private final Deque<Global> globalStack = new LinkedList<>();
    private int delimiterDepth;
    private int parseIndex;
    private byte[] descriptor;
    private int descriptorLength;
    private Local local;
    private Global global;
    private LinkedList<Report> reports;

    public final static int HID_MAX_FIELDS = 256;

    private final static int HID_MAX_IDS = 256;
    private final static int HID_MAX_APPLICATIONS = 16;
    private final static int HID_MAX_USAGES = 12288;

    private static final int HID_INPUT_REPORT = 0;
    private static final int HID_OUTPUT_REPORT = 1;
    private static final int HID_FEATURE_REPORT = 2;

    private static final int HID_COLLECTION_PHYSICAL = 0;
    private static final int HID_COLLECTION_APPLICATION = 1;
    private static final int HID_COLLECTION_LOGICAL = 2;

    public interface Tag {
        void parse(HidParser context, Item item);
    }

    public enum ItemType { // order import, do not change
        MAIN     { @Override Tag valueOf(int tag) { return MainTag.valueOf(tag); }},
        GLOBAL   { @Override Tag valueOf(int tag) { return GlobalTag.valueOf(tag); }},
        LOCAL    { @Override Tag valueOf(int tag) { return LocalTag.valueOf(tag); }},
        RESERVED { @Override Tag valueOf(int tag) { throw new UnsupportedOperationException(); }},
        LONG     { @Override Tag valueOf(int tag) { throw new UnsupportedOperationException(); }};
        abstract Tag valueOf(int tag);
    }

    public enum MainTag implements Tag { // order import, do not change
        PADDING_0 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_1 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_2 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_3 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_4 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_5 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_6 { @Override public void parse(HidParser context, Item item) {}},
        PADDING_7 { @Override public void parse(HidParser context, Item item) {}},
        INPUT {
            @Override public void parse(HidParser context, Item item) {
                context.addField(HID_INPUT_REPORT, item.uValue);
                context.local.reset();
            }
        },
        OUTPUT {
            @Override public void parse(HidParser context, Item item) {
                context.addField(HID_OUTPUT_REPORT, item.uValue);
                context.local.reset();
            }
        },
        COLLECTION {
            @Override public void parse(HidParser context, Item item) {
                context.topCollection = new Collection(context.topCollection, context.local.usages[0], item.uValue & 3);
            }
        },
        FEATURE {
            @Override public void parse(HidParser context, Item item) {
                context.addField(HID_FEATURE_REPORT, item.uValue);
                context.local.reset();
            }
        },
        ENDCOLLECTION {
            @Override public void parse(HidParser context, Item item) {
                if (context.topCollection.getParent() == null)
                    throw new IllegalStateException("collection stack underflow");
                context.topCollection = context.topCollection.getParent();
                context.local.reset();
            }
        };
        static Tag valueOf(int tag) {
            if (tag < 8 || tag >= values().length)
                throw new IllegalStateException(String.format("illegal/unsupported main tag %d", tag));
            return values()[tag];
        }
    }

    public enum LocalTag implements Tag { // order import, do not change
        USAGE {
            @Override public void parse(HidParser context, Item item) {
                if (item.size == 0)
                    throw new IllegalStateException("item data expected for local item");

                int usage = item.uValue;
                if (item.size <= 2) // FIXME is this in the spec?
                    usage = (context.global.usagePage << 16) + usage;

                if (context.local.delimiterBranch > 1) {
                    // alternative usage ignored
                    return;
                }
                context.addUsage(usage);
            }
        },
        USAGE_MINIMUM {
            @Override public void parse(HidParser context, Item item) {
logger.finer("USAGE_MAXIMUM: " + item.uValue + ", " + context.local.delimiterBranch);
                context.local.usageMinimum = item.uValue;
            }
        },
        USAGE_MAXIMUM {
            @Override public void parse(HidParser context, Item item) {
logger.finer("USAGE_MAXIMUM: " + item.uValue + ", " + context.local.delimiterBranch);
                for (int n = context.local.usageMinimum; n <= item.uValue; n++) {
logger.finer("USAGE_MAXIMUM: " + n);
                    context.addUsage(context.global.usagePage << 16 | n);
                }
            }
        },
        DESIGNATOR_INDEX   { @Override public void parse(HidParser context, Item item) {}},
        DESIGNATOR_MINIMUM { @Override public void parse(HidParser context, Item item) {}},
        DESIGNATOR_MAXIMUM { @Override public void parse(HidParser context, Item item) {}},
        STRING_INDEX       { @Override public void parse(HidParser context, Item item) {}},
        STRING_MINIMUM     { @Override public void parse(HidParser context, Item item) {}},
        STRING_MAXIMUM     { @Override public void parse(HidParser context, Item item) {}},
        DELIMITER {
            @Override public void parse(HidParser context, Item item) {
                if (item.uValue > 0) {
                    if (context.local.delimiterDepth != 0)
                        throw new IllegalStateException("nested delimiters");
                    context.local.delimiterDepth++;
                    context.local.delimiterBranch++;
                } else {
                    if (context.local.delimiterDepth < 1)
                        throw new IllegalStateException("extra delimiters");
                    context.local.delimiterDepth--;
                }
            }
        };
        static Tag valueOf(int tag) {
            if (tag < 0 || tag >= values().length)
                throw new IllegalStateException(String.format("illegal/unsupported local tag %d", tag));
            return values()[tag];
        }
    }

    public enum GlobalTag implements Tag {
        USAGE_PAGE {
            @Override public void parse(HidParser context, Item item) {
                context.global.usagePage = item.uValue;
            }
        },
        LOGICAL_MINIMUM {
            @Override public void parse(HidParser context, Item item) {
                context.global.logicalMinimum = item.sValue;
            }
        },
        LOGICAL_MAXIMUM {
            @Override public void parse(HidParser context, Item item) {
                context.global.logicalMaximum = item.sValue;
            }
        },
        PHYSICAL_MINIMUM {
            @Override public void parse(HidParser context, Item item) {
                context.global.physicalMinimum = item.sValue;
            }
        },
        PHYSICAL_MAXIMUM {
            @Override public void parse(HidParser context, Item item) {
                context.global.physicalMaximum = item.sValue;
logger.finer("global.physicalMaximum " + context.global.physicalMaximum);
            }
        },
        UNIT_EXPONENT {
            @Override public void parse(HidParser context, Item item) {
                context.global.unitExponent = item.sValue;
            }
        },
        UNIT {
            @Override public void parse(HidParser context, Item item) {
                context.global.unit = item.uValue;
            }
        },
        REPORT_SIZE {
            @Override public void parse(HidParser context, Item item) {
                if (item.uValue < 0 || item.uValue > 32)
                    throw new IllegalStateException(String.format("invalid report size %d", item.uValue));
                context.global.reportSize = item.uValue;
            }
        },
        REPORT_ID {
            @Override public void parse(HidParser context, Item item) {
                if (item.uValue == 0)
                    throw new IllegalStateException("report_id 0 is invalid");
                context.global.reportId = item.uValue;
            }
        },
        REPORT_COUNT {
            @Override public void parse(HidParser context, Item item) {
                if (item.uValue < 0 || item.uValue > HID_MAX_USAGES)
                    throw new IllegalStateException(String.format("invalid report count %d", item.uValue));
                context.global.reportCount = item.uValue;
            }
        },
        PUSH {
            @Override public void parse(HidParser context, Item item) {
                context.globalStack.push((Global) context.global.clone());
            }
        },
        POP {
            @Override public void parse(HidParser context, Item item) {
                if (context.globalStack.isEmpty())
                    throw new IllegalStateException("global environment stack underflow");
                context.global = context.globalStack.pop();
            }
        };
        static Tag valueOf(int tag) {
            if (tag < 0 || tag >= values().length)
                throw new IllegalStateException(String.format("illegal/unsupported global tag %d", tag));
            return values()[tag];
        }
    }

    public enum Feature {
        CONSTANT("Constant", "Data"),
        VARIABLE("Variable", "Array"),
        RELATIVE("Relative", "Absolute"),
        WRAP("Wrap", "No Wrap"),
        NONLINEAR("Non Linear", "Linear"),
        NO_PREFERRED("No Preferred State", "Preferred State"),
        NULL_STATE("Null State", "No Null Position"),
        VOLATILE("Volatile", "Non Volatile"),
        BUFFERED_BYTE("Buffered Bytes", "Bitfield");
        final int mask;
        final String on;
        final String off;
        Feature(String on, String off) {
            this.on = on;
            this.off = off;
            this.mask = 0x001 << ordinal();
        }
        static EnumSet<Feature> valueOf(int v) {
            return Arrays.stream(values()).filter(e -> (v & e.mask) != 0).collect(Collectors.toCollection(() -> EnumSet.noneOf(Feature.class)));
        }
        static String asString(EnumSet<Feature> es) {
            return Arrays.stream(values()).map(e -> es.contains(e) ? e.on : e.off).collect(Collectors.joining(", "));
        }
    }

    private static final int HID_LONG_ITEM_PREFIX = 0xfe;

    static PrintStream out = System.out;

    private static final class Local {

        public int[] usages = new int[HID_MAX_USAGES];
        public int[] collectionIndex = new int[HID_MAX_USAGES];
        public int usageIndex;
        public int usageMinimum;
        public int delimiterDepth;
        public int delimiterBranch;

        void reset() {
            usageIndex = 0;
            usageMinimum = 0;
            delimiterDepth = 0;
            delimiterBranch = 0;
            Arrays.fill(usages, 0);
            Arrays.fill(collectionIndex, 0);
        }
    }

    public static final class Global implements Cloneable {

        int usagePage;
        int logicalMinimum;
        int logicalMaximum;
        int physicalMinimum;
        int physicalMaximum;
        int unitExponent;
        int unit;
        int reportId;
        int reportSize;
        int reportCount;

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace(System.err);
                return null;
            }
        }
    }

    public static class EORException extends IllegalStateException {}

    public static final class Item {

        int size;
        ItemType type;
        Tag tag;
        int uValue;
        int sValue;

        Item(int size, ItemType type, int tag, int value) {
            this.size = size;
            this.type = type;
            this.tag = type.valueOf(tag);

            uValue = value;
            sValue = value;
            switch (size) { // for long items 'size' is not valid, but they are no supported anyway and have value==0
            case 1:
                if ((value & 0xffff_ff80) != 0)
                    sValue |= 0xffff_ff00;
                break;
            case 2:
                if ((value & 0xffff_8000) != 0)
                    sValue |= 0xffff_0000;
                break;
            default:
                break;
            }
        }

        /** */
        static boolean processNext(HidParser context) {
            if (context.parseIndex >= context.descriptorLength) {
logger.finer("EOD");
                return false;
            }
            Item item;
            int at = context.parseIndex;
            int prev = context.descriptor[context.parseIndex++] & 0xff;

            if (prev == HID_LONG_ITEM_PREFIX) {
                if (context.parseIndex >= context.descriptorLength)
                    throw new IllegalStateException("unexpected end of data white fetching long item size");

                int size = context.descriptor[context.parseIndex++] & 0xff;
                if (context.parseIndex >= context.descriptorLength)
                    throw new IllegalStateException("unexpected end of data white fetching long item tag");
                int tag = context.descriptor[context.parseIndex++] & 0xff;

                if (context.parseIndex + size - 1 >= context.descriptorLength)
                    throw new IllegalStateException("unexpected end of data white fetching long item");
                context.parseIndex += size;
                item = new Item(size, ItemType.LONG, tag, 0);
            } else {
                int type = (prev >> 2) & 3;
                int tag = (prev >> 4) & 15;
                int size = prev & 3;
                int value = 0;
                switch (size) {
                case 0:
                    break;

                case 1:
                    if (context.parseIndex >= context.descriptorLength)
                        throw new IllegalStateException("unexpected end of data white fetching item size==1");
                    value = context.descriptor[context.parseIndex++] & 0xFF;
                    break;

                case 2:
                    if (context.parseIndex + 1 >= context.descriptorLength)
                        throw new IllegalStateException("unexpected end of data white fetching item size==1");
                    value = (context.descriptor[context.parseIndex++] & 0xFF) |
                            ((context.descriptor[context.parseIndex++] & 0xFF) << 8);
                    break;

                case 3:
                    size++; // 3 means 4 bytes
                    if (context.parseIndex + 1 >= context.descriptorLength)
                        throw new IllegalStateException("unexpected end of data white fetching item size==1");
                    value = (context.descriptor[context.parseIndex++] & 0xFF) |
                            ((context.descriptor[context.parseIndex++] & 0xFF) << 8) |
                            ((context.descriptor[context.parseIndex++] & 0xFF) << 16) |
                            (context.descriptor[context.parseIndex++] << 24);
                }

                if (tag == 0 && type == 0) throw new EORException();
                if (type >= ItemType.values().length)
                    throw new IllegalStateException(String.format("illegal/unsupported type %d", type));
                item = new Item(size, ItemType.values()[type], tag, value);
            }

            if (logger.isLoggable(Level.FINEST)) {
                String tags = "?";
                if (item.tag != null)
                    tags = item.tag.toString();
                out.printf("[%3d] = 0x%02X:  size %d  type %-8s  tag %-20s  value 0x%6$08X (%6$d)\n", at, prev, item.size, item.type, tags, item.sValue);
            }
            item.tag.parse(context, item);
            return true;
        }
    }

    private Report registerReport(int type, int id) {
        for (Report r : reports)
            if (r.type == type && r.id == id)
                return r;
        Report r = new Report(type, id, topCollection);
        reports.add(r);
        return r;
    }

    private Field registerField(Report report, int values) {
        if (report.maxField == HID_MAX_FIELDS)
            throw new IllegalStateException("too many fields in report");

        Field field = new Field(topCollection);
        report.fields[report.maxField++] = field;
        field.report = report;

        return field;
    }

    private int lookUpCollection(int type) {
        for (Collection c = topCollection; c.getParent() != null; c = c.getParent()) {
            if (c.getType() == type)
                return c.getUsagePair();
        }
        return 0;
    }

    private void addUsage(int usagePair) {
        if (local.usageIndex >= local.usages.length)
            throw new IllegalStateException("usage index exceeded");
logger.finer(String.format("usage: %08x", usagePair));
        local.usages[local.usageIndex++] = usagePair;
    }

    private void addField(int reportType, int flags) {
        Report report = registerReport(reportType, global.reportId);

//		if ((parser.global.logical_minimum < 0 &&
//			parser.global.logical_maximum < parser.global.logical_minimum) ||
//			(parser.global.logical_minimum >= 0 &&
//			parser.global.logical_maximum < parser.global.logical_minimum)) {
//				System.err.printf("logical range invalid 0x%x 0x%x\n",
//				    parser.global.logical_minimum,
//				    parser.global.logical_maximum);
//				return;
//		}

        int j = 0;
        for (int i = 0; i < global.reportCount; i++) {
            if (i < local.usageIndex)
                j = i;
            int offset = report.size;
            report.size += global.reportSize;
            Field field = registerField(report, global.reportCount);

            field.physical = lookUpCollection(HID_COLLECTION_PHYSICAL);
            field.logical = lookUpCollection(HID_COLLECTION_LOGICAL);
            field.application = lookUpCollection(HID_COLLECTION_APPLICATION);

            field.usage = local.usages[j];
            field.flags = flags;
            field.reportOffset = offset;
            field.reportType = reportType;
            field.reportSize = global.reportSize;
            field.logicalMinimum = global.logicalMinimum;
            field.logicalMaximum = global.logicalMaximum;
            field.physicalMinimum = global.physicalMinimum;
            field.physicalMaximum = global.physicalMaximum;
            field.unitExponent = global.unitExponent;
            field.unit = global.unit;
        }
    }

    private void reset() {
        rootCollection = new Collection(null, 0, 0);
        topCollection = rootCollection;
        globalStack.clear();
        delimiterDepth = 0;
        parseIndex = 0;
        descriptor = null;
        descriptorLength = 0;
        local = new Local();
        global = new Global();
        reports = new LinkedList<>();
    }

    /** entry point */
    public Collection parse(byte[] descriptor, int length) {
        reset();
        this.descriptor = descriptor;
        this.descriptorLength = length;
        try {
            while (Item.processNext(this));
        } catch (EORException e) {
logger.finer("end of report");
        }
        if (topCollection.getParent() != null)
            throw new IllegalStateException("unbalanced collection at end of report description");

        if (delimiterDepth > 0)
            throw new IllegalStateException("unbalanced delimiter at end of report description");

        return rootCollection;
    }

    public void dump() {
logger.finer("rootCollection: c:" + rootCollection.getChildren().size() + ", f:" + rootCollection.getFields().size());
        rootCollection.dump(out, "");

//logger.finer("reports:");
//        for (Report r : reports) {
//            r.dump(out, "");
//        }
    }
}
