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

import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Map.Entry;

@NonnullByDefault
@Mixin(net.minecraft.enchantment.Enchantment.class)
public abstract class MixinEnchantment implements Enchantment {

    @Shadow private static Map<ResourceLocation, net.minecraft.enchantment.Enchantment> field_180307_E;

    @Shadow
    public abstract int getMinLevel();

    @Shadow
    public abstract int getMaxLevel();

    @Shadow
    public abstract int getMinEnchantability(int level);

    @Shadow
    public abstract int getMaxEnchantability(int level);

    @Shadow
    public abstract boolean canApplyTogether(net.minecraft.enchantment.Enchantment ench);

    @Shadow
    public abstract boolean canApply(net.minecraft.item.ItemStack stack);

    @Shadow(remap = false) // forge method
    public abstract boolean isAllowedOnBooks();

    @Override
    public String getId() {
        for (Entry<ResourceLocation, net.minecraft.enchantment.Enchantment> entry : field_180307_E.entrySet()) {
            if (entry.getValue().equals(this)) {
                return entry.getKey().toString();
            }
        }
        return ""; //Should never be reached
    }

    @Override
    public int getMinimumLevel() {
        return getMinLevel();
    }

    @Override
    public int getMaximumLevel() {
        return getMaxLevel();
    }

    @Override
    public int getMinimumEnchantabilityForLevel(int level) {
        return getMinEnchantability(level);
    }

    @Override
    public int getMaximumEnchantabilityForLevel(int level) {
        return getMaxEnchantability(level);
    }

    @Override
    public boolean canBeAppliedToStack(ItemStack stack) {
        return (stack.getItem() == ItemTypes.BOOK) ? isAllowedOnBooks() : canApply((net.minecraft.item.ItemStack) stack);
    }

    @Override
    public boolean canBeAppliedByTable(ItemStack stack) {
        return canBeAppliedToStack(stack);
    }

    @Override
    public boolean isCompatibleWith(Enchantment ench) {
        return canApplyTogether((net.minecraft.enchantment.Enchantment) ench);
    }
}
