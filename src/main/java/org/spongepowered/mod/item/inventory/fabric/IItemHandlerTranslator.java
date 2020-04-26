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
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.fabric.InventoryTranslator;

import java.util.Collection;

public class IItemHandlerTranslator implements InventoryTranslator<IItemHandler> {

    @Override
    public Collection<InventoryBridge> allInventories(IItemHandler inventory) {
        return ImmutableSet.of((InventoryBridge) inventory);
    }

    @Override
    public InventoryBridge get(IItemHandler inventory, int index) {
        return (InventoryBridge) inventory;
    }

    @Override
    public ItemStack getStack(IItemHandler inventory, int index) {
        return inventory.getStackInSlot(index);
    }

    @Override
    public void setStack(IItemHandler inventory, int index, ItemStack stack) {
        IItemHandlerFabricUtil.setIItemHandlerStack(inventory, index, stack);
    }

    @Override
    public int getMaxStackSize(IItemHandler inventory) {
        return inventory.getSlotLimit(0);
    }

    @Override
    public Translation getDisplayName(IItemHandler inventory) {
        return new FixedTranslation(inventory.getClass().getName());
    }

    @Override
    public int getSize(IItemHandler inventory) {
        return inventory.getSlots();
    }

    @Override
    public void clear(IItemHandler inventory) {
        if (inventory instanceof IItemHandlerModifiable) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ((IItemHandlerModifiable) inventory).setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void markDirty(IItemHandler inventory) {
    }

}
