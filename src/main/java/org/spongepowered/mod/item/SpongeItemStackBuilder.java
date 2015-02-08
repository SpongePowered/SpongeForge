/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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

import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;

public class SpongeItemStackBuilder implements ItemStackBuilder {

    private ItemType type;
    private int damage;
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
    public ItemStackBuilder damage(int damage) {
        checkArgument(damage >= 0, "Damage cannot be negative");
        this.damage = damage;
        return this;
    }

    @Override
    public ItemStackBuilder quantity(int quantity) throws IllegalArgumentException {
        checkArgument(quantity > 0, "Quantity must be greater than 0");
        this.quantity = quantity;
        return this;
    }

    @Override
    public ItemStackBuilder maxQuantity(int maxQuantity) {
        checkArgument(maxQuantity > 0, "Max quantity must be greater than 0");
        this.maxQuantity = maxQuantity;
        return this;
    }

    @Override
    public ItemStackBuilder fromItemStack(ItemStack itemStack) {
        checkNotNull(itemStack, "Item stack cannot be null");
        // Assumes the item stack's values don't need to be validated
        this.type = itemStack.getItem();
        this.damage = itemStack.getDamage();
        this.quantity = itemStack.getQuantity();
        this.maxQuantity = itemStack.getMaxStackQuantity();
        return this;
    }

    @Override
    public ItemStackBuilder reset() {
        this.type = null;
        this.damage = 0;
        this.quantity = 1;
        this.maxQuantity = 64;
        return this;
    }

    @Override
    public ItemStack build() throws IllegalStateException {
        checkState(this.type != null, "Item type has not been set");
        checkState(this.quantity <= this.maxQuantity, "Quantity cannot be greater than the max quantity (%s)", this.maxQuantity);
        // TODO How to enforce maxQuantity in the returned stack?
        return (ItemStack) new net.minecraft.item.ItemStack((Item) this.type, this.quantity, this.damage);
    }
}
