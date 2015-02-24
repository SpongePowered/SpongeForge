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

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

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
    public abstract NBTTagList getEnchantmentTagList();

    @Shadow
    public abstract boolean isItemEnchanted();

    @Shadow
    public abstract void addEnchantment(net.minecraft.enchantment.Enchantment ench, int level);

    @Override
    public ItemType getItem() {
        return (ItemType) shadow$getItem();
    }

    @Override
    public short getDamage() {
        return (short) getItemDamage();
    }

    @Override
    public void setDamage(short damage) {
        setItemDamage(damage);
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
    public Map<Enchantment, Integer> getEnchantments() {
        NBTTagList nbttaglist = getEnchantmentTagList();
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>(nbttaglist.tagCount());
        if (nbttaglist != null) {
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                short short1 = nbttaglist.getCompoundTagAt(i).getShort("id");
                short short2 = nbttaglist.getCompoundTagAt(i).getShort("lvl");
                enchantments.put((Enchantment) net.minecraft.enchantment.Enchantment.func_180306_c(short1), (int) short2);
            }
        }
        return enchantments;
    }

    @Override
    public boolean isEnchanted() {
        return isItemEnchanted();
    }

    @Override
    public void setEnchantment(Enchantment enchant, int level) {
        addEnchantment((net.minecraft.enchantment.Enchantment) enchant, level);
    }

    @Override
    public void removeEnchantment(Enchantment enchant) {
        NBTTagList nbttaglist = getEnchantmentTagList();
        if (nbttaglist != null) {
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                if (nbttaglist.getCompoundTagAt(i).getShort("id") == (short) ((net.minecraft.enchantment.Enchantment) enchant).effectId) {
                    nbttaglist.removeTag(i);
                }
            }
        }
    }

    @Override
    public int getEnchantment(Enchantment enchant) {
        return getEnchantments().get(enchant);
    }
}
