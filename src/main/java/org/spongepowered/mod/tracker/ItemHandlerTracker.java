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
package org.spongepowered.mod.tracker;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.common.launch.transformer.tracker.TrackerMethod;

import javax.annotation.Nonnull;

/**
 * All method calls to {@link IItemHandler} or {@link IItemHandlerModifiable}
 * will be delegated through this {@link ItemHandlerTracker}.
 */
public final class ItemHandlerTracker {

    // DO NOT MODIFY THE SIGNATURES OF THE FOLLOWING METHODS!
    ///////////////////////// START /////////////////////////

    @TrackerMethod
    public static int getSlots(IItemHandler itemHandler) {
        return itemHandler.getSlots();
    }

    @TrackerMethod
    public static ItemStack getStackInSlot(IItemHandler itemHandler, int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @TrackerMethod
    public static ItemStack insertItem(IItemHandler itemHandler, int slot, @Nonnull ItemStack stack, boolean simulate) {
        return itemHandler.insertItem(slot, stack, simulate);
    }

    @TrackerMethod
    public static ItemStack extractItem(IItemHandler itemHandler, int slot, int amount, boolean simulate) {
        return itemHandler.extractItem(slot, amount, simulate);
    }

    @TrackerMethod
    public static int getSlotLimit(IItemHandler itemHandler, int slot) {
        return itemHandler.getSlotLimit(slot);
    }

    @TrackerMethod
    public static void setStackInSlot(IItemHandlerModifiable itemHandler, int slot, @Nonnull ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    ////////////////////////// END //////////////////////////
    // Put whatever you like under here.

    private ItemHandlerTracker() {
    }
}
