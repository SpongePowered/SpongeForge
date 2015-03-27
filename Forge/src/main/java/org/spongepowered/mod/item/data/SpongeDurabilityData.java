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
package org.spongepowered.mod.item.data;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.service.persistence.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import org.spongepowered.api.item.ItemDataTransactionResult;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.data.DurabilityData;
import org.spongepowered.api.item.data.ItemData;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;

import java.util.Collection;

public class SpongeDurabilityData extends AbstractItemData implements DurabilityData {

    private int durability;
    private int maxDurability;
    private boolean breakable = true;

    public SpongeDurabilityData(ItemType type, int currentDurability) {
        super(type);
        this.durability = currentDurability;
        this.maxDurability = ((Item) type).getMaxDamage();
    }

    public SpongeDurabilityData(ItemStack stack) {
        super((ItemType) stack.getItem());
        this.durability = stack.getItemDamage();
        this.maxDurability = stack.getItem().getMaxDamage();
        this.breakable = stack.isItemStackDamageable();
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public void setDurability(int durability) {
        checkArgument(durability >= 0);
        this.durability = durability;
    }

    @Override
    public boolean isBreakable() {
        return this.breakable;
    }

    @Override
    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

    @Override
    public int compareTo(DurabilityData o) {
        return this.breakable ? o.isBreakable() ? 0 : -1 : o.isBreakable() ? 1 : 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Unbreakable"), this.breakable);
        return container;
    }

    @Override
    public ItemDataTransactionResult putData(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword) {
            ItemDataTransactionResult result = success(stack);
            stack.setItemDamage(this.durability);
            stack.getTagCompound().setBoolean("Unbreakable", this.breakable);
            return result;
        }
        return rejected(this);
    }

    private ItemDataTransactionResult success(final ItemStack stack) {
        return new ItemDataTransactionResult() {
            @Override
            public Type getType() {
                return Type.SUCCESS;
            }

            @Override
            public Optional<Collection<ItemData<?>>> getRejectedData() {
                return Optional.absent();
            }

            @Override
            public Optional<Collection<ItemData<?>>> getReplacedData() {
                return Optional.<Collection<ItemData<?>>>of(ImmutableSet.<ItemData<?>>of(new SpongeDurabilityData(stack)));
            }
        };
    }

    private ItemDataTransactionResult rejected(final SpongeDurabilityData data) {
        return new ItemDataTransactionResult() {
            @Override
            public Type getType() {
                return Type.FAILURE;
            }

            @Override
            public Optional<Collection<ItemData<?>>> getRejectedData() {
                return Optional.<Collection<ItemData<?>>>of(ImmutableSet.<ItemData<?>>of(data));
            }

            @Override
            public Optional<Collection<ItemData<?>>> getReplacedData() {
                return Optional.absent();
            }
        };
    }

}
