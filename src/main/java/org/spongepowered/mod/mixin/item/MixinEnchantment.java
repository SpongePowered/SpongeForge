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
package org.spongepowered.mod.mixin.item;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

@NonnullByDefault
@Mixin(net.minecraft.enchantment.Enchantment.class)
public abstract class MixinEnchantment implements Enchantment {

    @Shadow(prefix = "shadow$") private static final Map<ResourceLocation, Enchantment> shadow$field_180307_E = Maps.newHashMap();

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getMinLevel();

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getMaxLevel();

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getMinEnchantability(int level);

    @Shadow(prefix = "shadow$")
    public abstract int shadow$getMaxEnchantability(int level);

    @Shadow(prefix = "shadow$")
    public abstract boolean shadow$canApplyAtEnchantingTable(net.minecraft.item.ItemStack stack);

    @Shadow(prefix = "shadow$")
    public abstract boolean shadow$canApplyTogether(net.minecraft.enchantment.Enchantment ench);

    @Shadow(prefix = "shadow$")
    public abstract boolean shadow$canApply(net.minecraft.item.ItemStack stack);

    @Shadow(prefix = "shadow$")
    public abstract boolean shadow$isAllowedOnBooks();

    private static final BiMap<Enchantment, ResourceLocation> ids = HashBiMap.create(shadow$field_180307_E).inverse();

    @Override
    public String getId() {
        return ids.get(this).toString();
    }

    @Override
    public int getMinimumLevel() {
        return shadow$getMinLevel();
    }

    @Override
    public int getMaximumLevel() {
        return shadow$getMaxLevel();
    }

    @Override
    public int getMinimumEnchantabilityForLevel(int level) {
        return shadow$getMinEnchantability(level);
    }

    @Override
    public int getMaximumEnchantabilityForLevel(int level) {
        return shadow$getMaxEnchantability(level);
    }

    @Override
    public boolean canBeAppliedToStack(ItemStack stack) {
        return (stack.getItem() == ItemTypes.BOOK) ? shadow$isAllowedOnBooks() : shadow$canApply((net.minecraft.item.ItemStack) stack);
    }

    @Override
    public boolean canBeAppliedByTable(ItemStack stack) {
        return shadow$canApplyAtEnchantingTable((net.minecraft.item.ItemStack) stack);
    }

    @Override
    public boolean isCompatibleWith(Enchantment ench) {
        return shadow$canApplyTogether((net.minecraft.enchantment.Enchantment) ench);
    }
}
