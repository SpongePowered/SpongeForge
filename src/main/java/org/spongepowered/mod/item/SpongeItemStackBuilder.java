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
package org.spongepowered.mod.item;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.mod.item.ItemsHelper.validateData;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeItemStackBuilder implements ItemStackBuilder {
    @Nullable
    private Set<DataManipulator<?>> itemDataSet;
    private ItemType type;
    private int quantity;
    private int maxQuantity;

    public SpongeItemStackBuilder() {
        reset();
    }

    @Override
    public ItemStackBuilder itemType(ItemType itemType) {
        checkNotNull(itemType, "Item type cannot be null");
        this.type = itemType;
        return this;
    }

    @Override
    public ItemStackBuilder quantity(int quantity) throws IllegalArgumentException {
        checkArgument(quantity > 0, "Quantity must be greater than 0");
        this.quantity = quantity;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataManipulator<T>> ItemStackBuilder itemData(final T itemData) throws IllegalArgumentException {
        checkNotNull(itemData, "Must have a non-null item data!");
        checkNotNull(this.type, "Cannot set item data without having set a type first!");
        // Validation is required, we can't let devs set block data on a non-block item!
        DataTransactionResult result = validateData(this.type, itemData);
        if (result.getType() != DataTransactionResult.Type.SUCCESS) {
            throw new IllegalArgumentException("The item data is not compatible with the current item type!");
        } else {
            if (this.itemDataSet == null) {
                this.itemDataSet = Sets.newHashSet();
            }
            // Since item data objects are mutable, we don't want ot leak mutable objects!
            Optional<T> newOptional = ItemsHelper.getClone(itemData, (Class<T>) itemData.getClass());
            if (!newOptional.isPresent()) {
                throw new IllegalArgumentException("We could not property clone the data!");
            }
            // We have to sanitize the item data so that we don't have duplicates!
            for (Iterator<DataManipulator<?>> iter = this.itemDataSet.iterator(); iter.hasNext();) {
                DataManipulator<?> data = iter.next();
                if (data.getClass().equals(newOptional.get().getClass())) {
                    iter.remove();
                }
            }
            // Finally, add the item data
            this.itemDataSet.add(newOptional.get());
            return this;
        }
    }

    @Override
    public ItemStackBuilder fromItemStack(ItemStack itemStack) {
        checkNotNull(itemStack, "Item stack cannot be null");
        // Assumes the item stack's values don't need to be validated
        this.type = itemStack.getItem();
        this.quantity = itemStack.getQuantity();
        this.maxQuantity = itemStack.getMaxStackQuantity();
        return this;
    }

    @Override
    public ItemStackBuilder reset() {
        this.type = null;
        this.quantity = 1;
        this.maxQuantity = 64;
        this.itemDataSet = Sets.newHashSet();
        return this;
    }

    @Override
    public ItemStack build() throws IllegalStateException {
        checkState(this.type != null, "Item type has not been set");
        checkState(this.quantity <= this.maxQuantity, "Quantity cannot be greater than the max quantity (%s)", this.maxQuantity);
        // TODO How to enforce maxQuantity in the returned stack?
        final int damage;
        // Check if there's any additional data
        if (this.itemDataSet == null) {
            damage = 0;
        } else {
            // We need to actually get the damage value.
            Optional<Integer> damageOption = ItemsHelper.getDamageValue(this.type, this.itemDataSet);
            if (damageOption.isPresent()) {
                damage = damageOption.get();
            } else {
                // If there wasn't one set, well, default to 0
                damage = 0;
            }
        }
        ItemStack stack = (ItemStack) new net.minecraft.item.ItemStack((Item) this.type, this.quantity, damage);

        if (this.itemDataSet != null) {
            for (DataManipulator<?> data : this.itemDataSet) {
                ItemsHelper.setData(stack, data);
            }
        }
        return stack;
    }
}
