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
package org.spongepowered.mod.itemhandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.launch.transformer.tracker.TrackedMethod;

import javax.annotation.Nonnull;

/**
 * All method calls to {@link IItemHandler} or {@link IItemHandlerModifiable}
 * will be delegated through this {@link ItemHandlerTracker}.
 */
public final class ItemHandlerTracker {

    // DO NOT MODIFY THE SIGNATURES OF THE FOLLOWING METHODS!
    ///////////////////////// START /////////////////////////

    @TrackedMethod
    public static int getSlots(IItemHandler itemHandler) {
        // SpongeImpl.getLogger().info("BEFORE: getSlots");
        // Do things
        try {
            return itemHandler.getSlots();
        } finally {
            // SpongeImpl.getLogger().info("AFTER: getSlots");
        }
    }

    @TrackedMethod
    public static ItemStack getStackInSlot(IItemHandler itemHandler, int slot) {
        SpongeImpl.getLogger().info("BEFORE: getStackInSlot");
        // Do things
        try {
            return itemHandler.getStackInSlot(slot);
        } finally {
            SpongeImpl.getLogger().info("AFTER: getStackInSlot");
        }
    }

    @TrackedMethod
    public static ItemStack insertItem(IItemHandler itemHandler, int slot, @Nonnull ItemStack stack, boolean simulate) {
        SpongeImpl.getLogger().info("BEFORE: insertItem");
        // Do things
        try {
            return itemHandler.insertItem(slot, stack, simulate);
        } finally {
            SpongeImpl.getLogger().info("AFTER: insertItem");
        }
    }

    @TrackedMethod
    public static ItemStack extractItem(IItemHandler itemHandler, int slot, int amount, boolean simulate) {
        SpongeImpl.getLogger().info("BEFORE: extractItem");
        // Do things
        try {
            return itemHandler.extractItem(slot, amount, simulate);
        } finally {
            SpongeImpl.getLogger().info("AFTER: extractItem");
        }
    }

    @TrackedMethod
    public static int getSlotLimit(IItemHandler itemHandler, int slot) {
        SpongeImpl.getLogger().info("BEFORE: setStackInSlot");
        // Do things
        try {
            return itemHandler.getSlotLimit(slot);
        } finally {
            SpongeImpl.getLogger().info("AFTER: setStackInSlot");
        }
    }

    @TrackedMethod
    public static void setStackInSlot(IItemHandlerModifiable itemHandler, int slot, @Nonnull ItemStack stack) {
        SpongeImpl.getLogger().info("BEFORE: setStackInSlot");
        // Do things
        itemHandler.setStackInSlot(slot, stack);
        SpongeImpl.getLogger().info("AFTER: setStackInSlot");
    }

    ////////////////////////// END //////////////////////////
    // Put whatever you like under here.

    private ItemHandlerTracker() {
    }
}
