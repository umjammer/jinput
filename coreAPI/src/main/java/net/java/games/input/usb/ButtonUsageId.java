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

package net.java.games.input.usb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.java.games.input.Component;


/**
 * Button Usages
 *
 * @author elias
 * @version 1.0
 */
public final class ButtonUsageId implements UsageId {

    private final static Map<Integer, ButtonUsageId> map = new HashMap<>();

    private final int usageId;

    public static ButtonUsageId map(int buttonId) {
        ButtonUsageId existing = map.get(buttonId);
        if (existing != null)
            return existing;
        ButtonUsageId newButton = new ButtonUsageId(buttonId);
        map.put(buttonId, newButton);
        return newButton;
    }

    private ButtonUsageId(int usageId) {
        this.usageId = usageId;
    }

    @Override
    public Component.Identifier.Button getIdentifier() {
        return Arrays.stream(Component.Identifier.Button.values()).filter(e -> e.getName().equals(String.valueOf(usageId - 1))).findFirst().orElse(null);
    }

    @Override
    public int getId() {
        return usageId;
    }

    public String toString() {
        return "ButtonUsageId(" + usageId + ")";
    }
}
