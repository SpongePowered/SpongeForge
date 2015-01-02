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
package org.spongepowered.mod.item.merchant;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.village.MerchantRecipe;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;

import java.lang.reflect.Field;

public class SpongeTradeOfferBuilder implements TradeOfferBuilder {

    private ItemStack firstItem;
    private ItemStack secondItem;
    private ItemStack sellingItem;
    private int useCount;
    private int maxUses;
    private boolean allowsExperience;
    private static final Field field_180323_f;

    static {
        try {
            field_180323_f = MerchantRecipe.class.getDeclaredField("field_180323_f");
            field_180323_f.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SpongeTradeOfferBuilder() {
        reset();
    }

    @Override
    public TradeOfferBuilder withFirstBuyingItem(ItemStack item) {
        checkNotNull(item, "Buying item cannot be null");
        this.firstItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder withSeconBuyingItem(ItemStack item) {
        this.secondItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder withSellingItem(ItemStack item) {
        checkNotNull(item, "Selling item cannot be null");
        this.sellingItem = item;
        return this;
    }

    @Override
    public TradeOfferBuilder withUses(int uses) {
        checkArgument(uses >= 0, "Usage count cannot be negative");
        this.useCount = uses;
        return this;
    }

    @Override
    public TradeOfferBuilder withMaxUses(int maxUses) {
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
        checkState(firstItem != null, "Trading item has not been set");
        checkState(sellingItem != null, "Selling item has not been set");
        checkState(useCount <= maxUses, "Usage count cannot be greater than the max usage count (%s)", maxUses);
        MerchantRecipe recipe =
                new MerchantRecipe((net.minecraft.item.ItemStack) firstItem, (net.minecraft.item.ItemStack) secondItem,
                        (net.minecraft.item.ItemStack) sellingItem, useCount, maxUses);
        try {
            field_180323_f.setBoolean(recipe, allowsExperience);
        } catch (Exception ignored) {
            // No can do
        }
        return (TradeOffer) recipe;
    }

    @Override
    public TradeOfferBuilder fromTradeOffer(TradeOffer offer) {
        checkNotNull(offer, "Trade offer cannot be null");
        // Assumes the offer's values don't need to be validated
        firstItem = offer.getFirstBuyingItem();
        secondItem = offer.getSecondBuyingItem().orNull();
        sellingItem = offer.getSellingItem();
        useCount = offer.getUses();
        maxUses = offer.getMaxUses();
        allowsExperience = offer.doesGrantExperience();
        return this;
    }

    @Override
    public TradeOfferBuilder reset() {
        firstItem = null;
        secondItem = null;
        sellingItem = null;
        useCount = 0;
        maxUses = 7;
        allowsExperience = true;
        return this;
    }
}
