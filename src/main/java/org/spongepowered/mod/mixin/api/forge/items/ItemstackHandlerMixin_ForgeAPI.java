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
package org.spongepowered.mod.mixin.api.forge.items;

import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.item.inventory.adapter.impl.DefaultImplementedInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
@Mixin(ItemStackHandler.class)
@Implements(@Interface(iface = Inventory.class, prefix = "inventory$"))
public abstract class ItemstackHandlerMixin_ForgeAPI implements Inventory, DefaultImplementedInventoryAdapter {

    @Nullable private Iterable<Slot> forgeAPI$slotIterator;

    @Override
    public Inventory parent() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.forgeAPI$slotIterator == null) {
            // the bridge$getSlotProvider will lazily initialize the slot collection for us
            this.forgeAPI$slotIterator = ((SlotCollection) this.bridge$getSlotProvider()).getIterator(this);
        }
        return (Iterable<T>) this.forgeAPI$slotIterator;
    }

    @Intrinsic
    public void inventory$clear() {
        this.bridge$getFabric().clear();
    }



}
