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
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.mod.item.inventory.fabric.IItemHandlerFabric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
@Mixin(ItemStackHandler.class)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class ItemstackHandlerMixin_Forge implements MinecraftInventoryAdapter, TrackedInventoryBridge {

    @Nullable protected Inventory parent;
    protected SlotCollection slots;
    protected List<Inventory> children = new ArrayList<Inventory>();
    @Nullable protected Iterable<Slot> slotIterator;
    private Fabric fabric;
    @Nullable protected Lens lens = null;

    private List<SlotTransaction> capturedTransactions = new ArrayList<>();
    private boolean initalized = false;

    private void init() {
        if (!this.initalized) {
            this.initalized = true;
            this.fabric = new IItemHandlerFabric(((ItemStackHandler)(Object) this));
            this.slots = new SlotCollection.Builder().add(this.fabric.getSize()).build();
            this.lens = new OrderedInventoryLensImpl(0, this.fabric.getSize(), 1, this.slots);
        }
    }

    @Override
    public Inventory parent() {
        return this.parent == null ? this : this.parent;
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        this.init();
        return this.slots;
    }

    @Override
    public Inventory bridge$getChild(int index) {
        if (index < 0 || index >= this.bridge$getRootLens().getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.children.size()) {
            this.children.add(null);
        }
        Inventory child = this.children.get(index);
        if (child == null) {
            child = this.bridge$getRootLens().getChildren().get(index).getAdapter(this.bridge$getFabric(), this);
            this.children.set(index, child);
        }
        return child;
    }

    // TODO bridge$getChild with lens not implemented

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        this.init();
        if (this.slotIterator == null) {
            this.slotIterator = this.slots.getIterator(this);
        }
        return (Iterable<T>) this.slotIterator;
    }

    @Intrinsic
    public void inventory$clear() {
        this.bridge$getFabric().clear();
    }

    @Override
    public Lens bridge$getRootLens() {
        this.init();
        return this.lens;
    }

    @Override
    public Fabric bridge$getFabric() {
        this.init();
        return this.fabric;
    }

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.capturedTransactions;
    }


}
