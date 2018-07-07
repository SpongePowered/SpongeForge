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
package org.spongepowered.mod.item.inventory.fabric;

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

public class IItemHandlerFabric implements Fabric {
    private final IItemHandler inventory;

    public IItemHandlerFabric(IItemHandler inventory) {
        this.inventory = inventory;
    }

    @Override
    public Collection<?> allInventories() {
        return ImmutableSet.of(this.inventory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IItemHandler get(int index) {
        return this.inventory;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.inventory.getStackInSlot(index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        setIItemHandlerStack(this.inventory, index, stack);
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(0);
    }

    @Override
    public Translation getDisplayName() {
        return new FixedTranslation(getClass().getName());
    }

    @Override
    public int getSize() {
        return this.inventory.getSlots();
    }

    @Override
    public void clear() {
    }

    @Override
    public void markDirty() {
    }

    protected static void setIItemHandlerStack(IItemHandler handler, int index, ItemStack stack) {
        ItemStack prev = handler.getStackInSlot(index);
        if (prev != null) {
            int cnt = prev.getCount();
            // Extract all items
            while (cnt > 0) {
                ItemStack extracted = handler.extractItem(index, cnt, false);
                cnt -= extracted.getCount();
                if (extracted.getCount() == 0) {
                    break; // Do not keep looping if nothing was removed
                }
            }
        }

        prev = stack;
        // Insert all items
        while (!stack.isEmpty()) {
            stack = handler.insertItem(index, stack, false);
            if (prev == stack || prev.getCount() == stack.getCount()) {
                break; // Do not keep looping if nothing was inserted
            }
            prev = stack;
        }
    }

}
