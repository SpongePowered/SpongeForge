/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.keyboard;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.BitSet;

import javax.annotation.Nullable;

public class CustomClientKeyConflictContext implements IKeyConflictContext {

    private final int internalId;
    private final BitSet conflicts;

    public CustomClientKeyConflictContext(int internalId, BitSet conflicts) {
        this.internalId = internalId;
        this.conflicts = conflicts;
    }

    @Override
    public boolean isActive() {
        // Lets just assume this is always active on the client?
        return true;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        final int index = getConflictIndex(other);
        // Lets just assume any conflicts with all the mods
        return index == -1 || this.conflicts.get(index);
    }

    private static int getConflictIndex(IKeyConflictContext other) {
        // Some hardcoded numbers, well, move along
        if (other == KeyConflictContext.UNIVERSAL) {
            return 0;
        } else if (other == KeyConflictContext.IN_GAME) {
            return 1;
        } else if (other == KeyConflictContext.GUI) {
            return 2;
        } else if (other instanceof CustomClientKeyConflictContext) {
            return ((CustomClientKeyConflictContext) other).internalId;
        }
        // Unknown conflict context, probably modded
        return -1;
    }

    private static Int2ObjectMap<IKeyConflictContext> contexts = new Int2ObjectOpenHashMap<>();

    @Nullable
    public static IKeyConflictContext get(int internalId) {
        return contexts.get(internalId);
    }

    /**
     * Loads the {@link CustomClientKeyConflictContext}s from the conflict context data.
     *
     * @param conflictContextData The conflict context data
     */
    public static void load(Int2ObjectMap<BitSet> conflictContextData) {
        contexts.put(0, KeyConflictContext.UNIVERSAL);
        contexts.put(1, KeyConflictContext.IN_GAME);
        contexts.put(2, KeyConflictContext.GUI);
        for (Int2ObjectMap.Entry<BitSet> entry : conflictContextData.int2ObjectEntrySet()) {
            contexts.put(entry.getIntKey(), new CustomClientKeyConflictContext(entry.getIntKey(), entry.getValue()));
        }
    }

    /**
     * Removes all the {@link CustomClientKeyConflictContext}s.
     */
    public static void cleanup() {
        contexts.clear();
    }
}
