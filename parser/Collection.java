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
import java.util.LinkedList;


/**
 * @see "https://github.com/nyholku/purejavahidapi"
 */
public final class Collection {

    Collection parent;
    LinkedList<Collection> children;
    LinkedList<Field> fields;
    int usage;
    int type;

    Collection(Collection parent, int usage, int type) {
        this.parent = parent;
        this.usage = usage;
        this.type = type;
        children = new LinkedList<>();
        if (parent != null)
            parent.children.add(this);
        fields = new LinkedList<>();
    }

    void add(Field field) {
        fields.add(field);
    }

    public void dump(PrintStream out, String tab) {
        if (parent != null) {
            out.printf(tab + "collection  type %d  usage 0x%04X:0x%04X\n", type, (usage >> 16) & 0xFFFF, usage & 0xFFFF);
            tab += "   ";
        }
        for (Collection c : children)
            c.dump(out, tab);
        for (Field f : fields)
            f.dump(out, tab);
    }
}