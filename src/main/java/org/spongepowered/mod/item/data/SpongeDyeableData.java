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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.DyeColor;
import org.spongepowered.api.item.ItemDataTransactionResult;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.data.DyeableItemData;
import org.spongepowered.api.item.data.ItemData;
import org.spongepowered.api.service.persistence.data.DataContainer;

import java.util.Collection;

public class SpongeDyeableData extends AbstractItemData implements DyeableItemData {

    private DyeColor color;

    public SpongeDyeableData(ItemType type, DyeColor color) {
        super(type);
        this.color = color;
    }

    @Override
    public DyeColor get() {
        return this.color;
    }

    @Override
    public void set(DyeColor value) {
        checkNotNull(value);
        this.color = value;
    }

    @Override
    public int compareTo(DyeableItemData dyeableItemData) {
        return this.color.getName().compareTo(dyeableItemData.get().getName());
    }

    @Override
    public DataContainer toContainer() {
        return this.color.toContainer();
    }

    @Override
    public ItemDataTransactionResult putData(ItemStack stack) {
        // Can't really use any other way since MC is scattered...
        if (stack.getItem().equals(Items.dye)) {
            EnumDyeColor newColor = EnumDyeColor.valueOf(this.color.getName().toUpperCase());
            if (newColor != null) {
                // do nothing

            }
            stack.setItemDamage(EnumDyeColor.valueOf(this.color.getName().toUpperCase()).getDyeDamage());
        } else if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockColored) {
            stack.setItemDamage(EnumDyeColor.valueOf(this.color.getName().toUpperCase()).getDyeDamage());
        } else if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
            // We shouldn't be doing this..... But......
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (nbttagcompound == null) {
                nbttagcompound = new NBTTagCompound();
                stack.setTagCompound(nbttagcompound);
            }

            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (!nbttagcompound.hasKey("display", 10)) {
                nbttagcompound.setTag("display", nbttagcompound1);
            }

            nbttagcompound1.setInteger("color", dyeToColor(this.color));
        }
        return rejectData(this.color);
    }

    private Optional<DyeColor> getPreviousColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item.equals(Items.dye)) {
            return Optional.of((DyeColor) (Object) EnumDyeColor.byMetadata(stack.getItemDamage()));
        } else if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() instanceof BlockColored) {
            return Optional.of((DyeColor) (Object) EnumDyeColor.byMetadata(stack.getItemDamage()));
        }
        return Optional.absent();// TODO actually try to convert leather color to dye...
    }

    private static int dyeToColor(DyeColor color) {
        // For the dye
        final float[] dyeRgbArray = EntitySheep.func_175513_a(EnumDyeColor.valueOf(color.getName().toUpperCase()));

        // Convert!
        final int trueRed = (int) (dyeRgbArray[0] * 255.0F);
        final int trueGreen = (int) (dyeRgbArray[1] * 255.0F);
        final int trueBlue = (int) (dyeRgbArray[2] * 255.0F);
        final int combinedRg = (trueRed << 8) + trueGreen;
        return (combinedRg << 8) + trueBlue;
    }

    private ItemDataTransactionResult rejectData(final DyeColor color) {
        return new ItemDataTransactionResult() {
            @Override
            public Type getType() {
                return Type.FAILURE;
            }

            @Override
            public Optional<Collection<ItemData<?>>> getRejectedData() {
                return Optional.<Collection<ItemData<?>>>of(
                        ImmutableSet.<ItemData<?>>of(new SpongeDyeableData(SpongeDyeableData.this.getOwner(), color)));
            }

            @Override
            public Optional<Collection<ItemData<?>>> getReplacedData() {
                return Optional.absent();
            }
        };
    }
}
