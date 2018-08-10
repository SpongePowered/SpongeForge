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
package org.spongepowered.mod.mixin.core.item.inventory;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollection;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.mod.item.inventory.fabric.IItemHandlerFabric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(value = {InvWrapper.class, ItemStackHandler.class})
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class MixinIItemHandlerAdapter implements MinecraftInventoryAdapter, IMixinInventory {

    private Fabric fabric;
    private SlotLensCollection slots;
    @Nullable private SlotCollection slotCollection;
    private Lens lens;

    private List<SlotTransaction> capturedTransactions = new ArrayList<>();
    private boolean initalized = false;

    private void init() {
        if (!initalized) {
            initalized = true;
            this.fabric = new IItemHandlerFabric((IItemHandler) this);
            this.slots = new SlotLensCollection.Builder().add(this.fabric.getSize()).build();
            this.lens = new DefaultIndexedLens(0, this.fabric.getSize(), slots);
        }
    }

    @Override
    public Inventory parent() {
        return this;
    }

    @Override
    public SlotProvider getSlotProvider() {
        this.init();
        return this.slots;
    }

    @Override
    public List<Slot> slots() {
        this.init();
        if (this.slotCollection == null) {
            this.slotCollection = this.slots.getSlots(this);
        }
        return this.slotCollection.slots();
    }

    @Intrinsic
    public void inventory$clear() {
        this.getFabric().clear();
    }

    @Override
    public Lens getRootLens() {
        this.init();
        return this.lens;
    }

    @Override
    public Fabric getFabric() {
        this.init();
        return this.fabric;
    }

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedTransactions;
    }

}
