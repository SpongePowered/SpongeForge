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
package org.spongepowered.mod.mixin.item.merchant;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.base.Optional;

@NonnullByDefault
@Mixin(net.minecraft.village.MerchantRecipe.class)
public abstract class MixinMerchantRecipe implements TradeOffer {

    @Shadow
    public abstract net.minecraft.item.ItemStack getItemToBuy();
    
    @Shadow
    public abstract boolean hasSecondItemToBuy();
    
    @Shadow
    public abstract net.minecraft.item.ItemStack getSecondItemToBuy();
    
    @Shadow
    public abstract net.minecraft.item.ItemStack getItemToSell();
    
    @Shadow
    public abstract int func_180321_e();
    
    @Shadow
    public abstract int func_180320_f();
    
    @Shadow
    public abstract boolean isRecipeDisabled();
    
    @Shadow
    public abstract boolean func_180322_j();
    
    @Override
    public ItemStack getFirstBuyingItem() {
        return (ItemStack) getItemToBuy();
    }

    @Override
    public boolean hasSecondItem() {
        return hasSecondItemToBuy();
    }

    @Override
    public Optional<ItemStack> getSecondBuyingItem() {
        return Optional.fromNullable((ItemStack) getSecondBuyingItem());
    }

    @Override
    public ItemStack getSellingItem() {
        return getSellingItem();
    }

    @Override
    public int getUses() {
        return func_180321_e();
    }

    @Override
    public int getMaxUses() {
        return func_180320_f();
    }

    @Override
    public boolean hasExpired() {
        return isRecipeDisabled();
    }

    @Override
    public boolean doesGrantExperience() {
        return func_180322_j();
    }

}
