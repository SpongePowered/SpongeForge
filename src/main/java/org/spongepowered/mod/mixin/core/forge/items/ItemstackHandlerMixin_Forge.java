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
package org.spongepowered.mod.mixin.core.forge.items;

import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.mod.item.inventory.fabric.IItemHandlerFabric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(ItemStackHandler.class)
public abstract class ItemstackHandlerMixin_Forge implements InventoryAdapter, TrackedInventoryBridge, InventoryAdapterBridge {

    @Nullable private SlotCollection forgeImpl$slots;
    @Nullable private Fabric forgeImpl$fabric;
    @Nullable private Lens forgeImpl$lens;
    private List<Inventory> forgeImpl$children = new ArrayList<Inventory>();
    private List<SlotTransaction> forgeImpl$capturedTransactions = new ArrayList<>();

    @Override
    public SlotProvider bridge$getSlotProvider() {
        if (this.forgeImpl$slots == null) {
            this.forgeImpl$slots = new SlotCollection.Builder().add(this.bridge$getFabric().getSize()).build();
        }
        return this.forgeImpl$slots;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Inventory bridge$getChild(final int index) {
        if (index < 0 || index >= this.bridge$getRootLens().getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.forgeImpl$children.size()) {
            this.forgeImpl$children.add(null);
        }
        Inventory child = this.forgeImpl$children.get(index);
        if (child == null) {
            child = (Inventory) this.bridge$getRootLens().getChildren().get(index).getAdapter(this.bridge$getFabric(), (Inventory) this);
            this.forgeImpl$children.set(index, child);
        }
        return child;
    }

    @Override
    public Lens bridge$getRootLens() {
        if (this.forgeImpl$lens == null) {
            this.forgeImpl$lens = new OrderedInventoryLensImpl(0, this.bridge$getFabric().getSize(), 1, this.bridge$getSlotProvider());
        }
        return this.forgeImpl$lens;
    }

    @Override
    public Fabric bridge$getFabric() {
        if (this.forgeImpl$fabric == null) {
            this.forgeImpl$fabric = new IItemHandlerFabric(((ItemStackHandler)(Object) this));
        }
        return this.forgeImpl$fabric;
    }

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.forgeImpl$capturedTransactions;
    }


}
