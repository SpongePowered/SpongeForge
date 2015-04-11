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

import static org.spongepowered.mod.service.persistence.NbtTranslator.getInstance;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulators.SingleValueData;
import org.spongepowered.api.data.manipulators.items.BlockItemData;
import org.spongepowered.api.data.manipulators.items.DurabilityData;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.mod.item.data.AbstractItemData;

import java.util.Collection;
import java.util.Set;


public final class ItemsHelper {

    public static final DataTransactionResult SUCCESS_NO_REPLACEMENTS = new DataTransactionResult() {
        @Override
        public Type getType() {
            return Type.SUCCESS;
        }

        @Override
        public Optional<Collection<DataManipulator<?>>> getRejectedData() {
            return Optional.absent();
        }

        @Override
        public Optional<Collection<DataManipulator<?>>> getReplacedData() {
            return Optional.absent();
        }
    };

    private ItemsHelper() { // No subclassing for you!
    }

    public static <T extends DataManipulator<T>> Optional<T> getClone(T DataManipulator, Class<T> clazz) {

        return Optional.absent();
    }

    public static Optional<Integer> getDamageValue(final ItemType type, final Set<DataManipulator<?>> DataManipulatorSet) {
        if (type instanceof ItemBlock) {
            // If it's a block, well, we definitely should have some block state information we can use
            for (DataManipulator<?> data : DataManipulatorSet) {
                if (data instanceof BlockItemData) {
                    BlockItemData blockData = (BlockItemData) data;
                    return Optional.of(Block.getBlockFromItem((Item) type).damageDropped((BlockState.StateImplementation) blockData.getState()));
                }
            }
        } else if (((Item) type).getHasSubtypes()) {
            // TODO we need a better way to represent identifiable damage values

        } else {
            for (DataManipulator<?> data : DataManipulatorSet) {
                // Otherwise, it's a durability number
                if (data instanceof DurabilityData) {
                    return Optional.of(((DurabilityData) data).getDurability());
                } else if (data instanceof SingleValueData<?, ?>) {
                    // We really need to figure this one out.
                }
            }
        }
        return Optional.absent();
    }

    public static DataTransactionResult validateData(ItemType type, DataManipulator<?> data) {
        return SUCCESS_NO_REPLACEMENTS; // TODO actually implement
    }

    public static DataTransactionResult setData(ItemStack stack, DataManipulator<?> data) {
        if (data instanceof AbstractItemData) {
            return ((AbstractItemData) data).putData((net.minecraft.item.ItemStack) stack);
        } else {
            NBTTagCompound compound = ((net.minecraft.item.ItemStack) stack).getTagCompound();
            getInstance().translateContainerToData(compound, data.toContainer());
            return SUCCESS_NO_REPLACEMENTS;
        }
    }
}
