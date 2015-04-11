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
package org.spongepowered.mod.item.merchant;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.village.MerchantRecipe;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;

public class SpongeTradeOfferBuilder implements TradeOfferBuilder {

    private ItemStack firstItem;
    private ItemStack secondItem;
    private ItemStack sellingItem;
    private int useCount;
    private int maxUses;
    private boolean allowsExperience;

    public SpongeTradeOfferBuilder() {
        reset();
    }

    @Override
    public TradeOfferBuilder firstBuyingItem(ItemStack item) {
        checkNotNull(item, "Buying item cannot be null");
        this.firstItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder secondBuyingItem(ItemStack item) {
        this.secondItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder sellingItem(ItemStack item) {
        checkNotNull(item, "Selling item cannot be null");
        this.sellingItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder uses(int uses) {
        checkArgument(uses >= 0, "Usage count cannot be negative");
        this.useCount = uses;
        return this;
    }

    @Override
    public TradeOfferBuilder maxUses(int maxUses) {
        checkArgument(maxUses > 0, "Max usage count must be greater than 0");
        this.maxUses = maxUses;
        return this;
    }

    @Override
    public TradeOfferBuilder setCanGrantExperience(boolean experience) {
        this.allowsExperience = experience;
        return this;
    }

    @Override
    public TradeOffer build() throws IllegalStateException {
        checkState(this.firstItem != null, "Trading item has not been set");
        checkState(this.sellingItem != null, "Selling item has not been set");
        checkState(this.useCount <= this.maxUses, "Usage count cannot be greater than the max usage count (%s)", this.maxUses);
        MerchantRecipe recipe =
                new MerchantRecipe((net.minecraft.item.ItemStack) this.firstItem, (net.minecraft.item.ItemStack) this.secondItem,
                        (net.minecraft.item.ItemStack) this.sellingItem, this.useCount, this.maxUses);
        recipe.rewardsExp = this.allowsExperience;
        return (TradeOffer) recipe;
    }

    @Override
    public TradeOfferBuilder from(TradeOffer offer) {
        checkNotNull(offer, "Trade offer cannot be null");
        // Assumes the offer's values don't need to be validated
        this.firstItem = offer.getFirstBuyingItem();
        this.secondItem = offer.getSecondBuyingItem().orNull();
        this.sellingItem = offer.getSellingItem();
        this.useCount = offer.getUses();
        this.maxUses = offer.getMaxUses();
        this.allowsExperience = offer.doesGrantExperience();
        return this;
    }

    @Override
    public TradeOfferBuilder reset() {
        this.firstItem = null;
        this.secondItem = null;
        this.sellingItem = null;
        this.useCount = 0;
        this.maxUses = 7;
        this.allowsExperience = true;
        return this;
    }
}
