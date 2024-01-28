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

package net.java.games.input.usb.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.java.games.input.usb.UsagePage;


/**
 * Represents a collection of {@link Field}.
 *
 * @see "https://github.com/nyholku/purejavahidapi"
 */
public final class Collection {

    private final Collection parent;
    private final LinkedList<Collection> children;
    private final LinkedList<Field> fields;
    private final int usagePair;
    private final int type;

    public Collection getParent() {
        return parent;
    }

    public LinkedList<Collection> getChildren() {
        return children;
    }

    public LinkedList<Field> getFields() {
        return fields;
    }

    public int getUsagePair() {
        return usagePair;
    }

    public int getUsagePage() {
        return (usagePair >> 16) & 0xffff;
    }

    public int getUsage() {
        return  usagePair & 0xffff;
    }

    public int getType() {
        return type;
    }

    public enum Type {
        Physical,
        Application,
        Logical,
        Report,
        NamedArray,
        UsageSwitch,
        UsageModifier,
        // 0x07 - 0x7F
        ReservedForFutureUse,
        // 0x80 - 0xFF
        VendorDefined;
        static Type valueOf(int type) {
            if (type < 0x07) {
                return values()[type];
            } else if (type <= 0x7F) {
                return ReservedForFutureUse;
            } else if (type <= 0xff) {
                return VendorDefined;
            } else {
                throw new IllegalArgumentException(String.valueOf(type));
            }
        }
    }

    Collection(Collection parent, int usagePair, int type) {
        this.parent = parent;
        this.usagePair = usagePair;
        this.type = type;
        children = new LinkedList<>();
        if (parent != null)
            parent.children.add(this);
        fields = new LinkedList<>();
    }

    void reset() {
        children.clear();
    }

    void add(Field field) {
        fields.add(field);
    }

    void dump(PrintStream out, String tab) {
        if (parent != null) {
            UsagePage usagePage_ = UsagePage.map(getUsagePage());
            out.printf(tab + "collection  type %s(%d)  usage 0x%04X:0x%04X %s:%s%n", Type.valueOf(type), type, getUsagePage(), getUsage(), usagePage_ == null ? "" : usagePage_, usagePage_ == null ? "" : usagePage_.mapUsage(getUsage()));
            tab += "   ";
        }
        for (Collection c : children) {
            c.dump(out, tab);
        }
        for (Field f : fields) {
            f.dump(out, tab);
        }
    }

    public List<Field> enumerateFields() {
        List<Field> result = new ArrayList<>();
        for (Collection c : children) {
            result.addAll(c.enumerateFields());
        }
        result.addAll(fields);
        return result;
    }

    @Override
    public String toString() {
        return "Collection{" +
                "parent=" + parent +
                ", children=" + children.size() +
                ", fields=" + fields.size() +
                ", usage=" + usagePair +
                ", type=" + Type.valueOf(type) +
                '}';
    }
}