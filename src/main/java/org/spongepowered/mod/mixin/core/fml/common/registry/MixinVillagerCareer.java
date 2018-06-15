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
package org.spongepowered.mod.mixin.core.fml.common.registry;

import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.item.merchant.TradeOfferListMutator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.SpongeVillagerRegistry;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.List;
import java.util.Optional;

@Mixin(value = VillagerRegistry.VillagerCareer.class, remap = false)
public class MixinVillagerCareer implements IMixinVillagerCareer {

    @Shadow private int id;
    @Shadow private VillagerRegistry.VillagerProfession profession;
    @Shadow private List<List<EntityVillager.ITradeList>> trades;
    @Shadow private String name;

    private boolean delayed;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        this.delayed = !SpongeForgeVillagerRegistry.fromNative((VillagerRegistry.VillagerCareer) (Object) this).isPresent();
        // Still make sure that the forge profession and career profession are synchronized. This is kinda a last ditch effort to get
        // them registered with sponge.
        SpongeForgeVillagerRegistry.registerForgeCareer((VillagerRegistry.VillagerCareer) (Object) this);
    }

    @Override
    public VillagerRegistry.VillagerProfession getProfession() {
        return this.profession;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean isDelayed() {
        return this.delayed;
    }

    @Override
    public void performDelayedInit() {
        this.registerTrades();
    }

    @Override
    public void forceProfession(VillagerRegistry.VillagerProfession villagerProfession) {
        this.profession = villagerProfession;
    }

    /**
     * @author gabizou - June 6th, 2016
     * @reason Adds a {@link SpongeVillagerRegistry} handling for registering custom trade
     * lists.
     *
     * @param level The level
     * @param trades The trades
     * @return This career
     */
    @Overwrite
    public VillagerRegistry.VillagerCareer addTrade(int level, EntityVillager.ITradeList... trades) {
        if (level <= 0) {
            throw new IllegalArgumentException("Levels start at 1");
        }
        // Sponge start
        final Optional<SpongeCareer> spongeCareer = SpongeForgeVillagerRegistry.fromNative((VillagerRegistry.VillagerCareer) (Object) this);
        if (!spongeCareer.isPresent()) {
            SpongeImpl.getLogger().debug("Delaying trade registration for career {}", this.name);
        } else {
            for (EntityVillager.ITradeList trade : trades) {
                SpongeVillagerRegistry.getInstance().addMutator(spongeCareer.get(), level, (TradeOfferListMutator) trade);
            }
        }
        // Sponge end

        List<EntityVillager.ITradeList> levelTrades = level <= this.trades.size() ? this.trades.get(level - 1) : null;
        if (levelTrades == null) {
            while (this.trades.size() < level) {
                levelTrades = Lists.newArrayList();
                this.trades.add(levelTrades);
            }
        }
        if (levelTrades == null) //Not sure how this could happen, but screw it
        {
            levelTrades = Lists.newArrayList();
            this.trades.set(level - 1, levelTrades);
        }
        for (EntityVillager.ITradeList t : trades) {
            levelTrades.add(t);
        }
        return (VillagerRegistry.VillagerCareer) (Object) this;
    }

    private void registerTrades() {
        this.delayed = false;
        Career career = SpongeForgeVillagerRegistry.fromNative((VillagerRegistry.VillagerCareer) (Object) this).get();
        for (int i = 0; i < this.trades.size(); i++) {
            int level = i + 1;
            List<EntityVillager.ITradeList> trades = this.trades.get(i);
            for (EntityVillager.ITradeList trade: trades) {
                SpongeVillagerRegistry.getInstance().addMutator(career, level, (TradeOfferListMutator) trade);
            }
        }
    }
}
