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
package org.spongepowered.mod.mixin.core.item.inventory;

import static org.spongepowered.api.service.persistence.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.ItemDataTransactionResult;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.data.ItemData;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.properties.ItemProperty;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.item.ItemsHelper;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
@NonnullByDefault
@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack implements ItemStack {

    @Shadow
    public int stackSize;

    @Shadow(prefix = "shadow$")
    public abstract Item shadow$getItem();

    @Shadow
    public abstract int getItemDamage();

    @Shadow
    public abstract void setItemDamage(int meta);

    @Shadow
    public abstract int getMaxStackSize();

    @Shadow
    public abstract NBTTagCompound getTagCompound();

    @Override
    public ItemType getItem() {
        return (ItemType) shadow$getItem();
    }

    @Override
    public int getQuantity() {
        return this.stackSize;
    }

    @Override
    public void setQuantity(int quantity) throws IllegalArgumentException {
        if (quantity > this.getMaxStackQuantity()) {
            throw new IllegalArgumentException("Quantity (" + quantity + ") exceeded the maximum stack size (" + this.getMaxStackQuantity() + ")");
        } else {
            this.stackSize = quantity;
        }
    }

    @Override
    public int getMaxStackQuantity() {
        return getMaxStackSize();
    }

    @Override
    public void setMaxStackQuantity(int quantity) {
        shadow$getItem().setMaxStackSize(quantity);
    }

    @Override
    public <T extends ItemData<T>> ItemDataTransactionResult setItemData(T itemData) {
        // TODO actually implement this....
        ItemDataTransactionResult result = ItemsHelper.validateData(this.getItem(), itemData);
        if (result.getType() != ItemDataTransactionResult.Type.SUCCESS) {
            return result;
        } else {
            result = ItemsHelper.setData(this, itemData);
            return result;
        }
    }

    @Override
    public <T extends ItemData<T>> Optional<T> getOrCreateItemData(Class<T> dataClass) {
        return null;
    }

    @Override
    public Collection<ItemProperty<?, ?>> getProperties() {
        return null;
    }

    @Override
    public Collection<ItemData<?>> getItemData() {
        return null;
    }

    public <T extends ItemProperty<?, ?>> Optional<T> getProperty(Class<T> property) {

        return Optional.absent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getData(Class<T> dataClass) {
        if (ItemData.class.isAssignableFrom(dataClass)) {
            return (Optional<T>) getOrCreateItemData((Class) dataClass);
        } else if (ItemProperty.class.isAssignableFrom(dataClass)) {
            return (Optional<T>) getProperty((Class) dataClass);
        } else {
            return Optional.absent(); // Because fuck you, that's why
        }
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("ItemType"), this.getItem().getId());
        container.set(of("Quantity"), this.getQuantity());
        List<DataContainer> containerList = Lists.newArrayList();
        for (ItemData<?> itemData : getItemData()) {
            containerList.add(itemData.toContainer());
        }


        return container;
    }

}
