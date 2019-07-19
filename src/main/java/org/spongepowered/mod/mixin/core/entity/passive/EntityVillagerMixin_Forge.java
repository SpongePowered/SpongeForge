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
package org.spongepowered.mod.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.mixin.core.entity.EntityAgeableMixin;
import org.spongepowered.mod.bridge.entity.passive.EntityVillagerBridge_Forge;
import org.spongepowered.mod.bridge.registry.VillagerCareerBridge_Forge;
import org.spongepowered.mod.bridge.registry.VillagerProfessionBridge_Forge;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import javax.annotation.Nullable;

@Mixin(value = EntityVillager.class, priority = 1100)
public abstract class EntityVillagerMixin_Forge extends EntityAgeableMixin implements EntityVillagerBridge_Forge {

    @Shadow private int careerId;
    @Shadow private int careerLevel;
    @Shadow @Nullable private MerchantRecipeList buyingList;

    @Shadow(remap = false) public abstract VillagerRegistry.VillagerProfession getProfessionForge();

    /**
     * @author gabizou - April 8th, 2018
     * @reason This overwrites the forge handling to cater to Sponge's villager handling.
     * There have been too many bugs with the re-assignment of VillagerProfessions and
     * mods not registering them normally, which ends up causing sync issues between the
     * forge professions and sponge professions. This aims to have failsafes for handling
     * with Sponge's system.
     */
    @Overwrite
    public void populateBuyingList() {
        final VillagerRegistry.VillagerProfession professionForge = this.getProfessionForge();
        if (this.careerId != 0 && this.careerLevel != 0) {
            ++this.careerLevel;
        } else {
            this.careerId = professionForge.getRandomCareer(this.rand) + 1;
            this.careerLevel = 1;
        }

        // Sanity purposes, occasionally NBT doesn't get validated that the career id is higher than the profession's list.
        if (this.careerId - 1 >= ((VillagerProfessionBridge_Forge) professionForge).forgeBridge$getCareers().size()) {
            this.careerId = professionForge.getRandomCareer(this.rand) + 1;
        }

        if (this.buyingList == null) {
            this.buyingList = new MerchantRecipeList();
        }

        final int careerNumberId = this.careerId - 1;
        final int careerLevel = this.careerLevel - 1;
        // Sponge - only get the profession once
        final VillagerRegistry.VillagerCareer career = professionForge.getCareer(careerNumberId);
        final VillagerCareerBridge_Forge mixinCareer = (VillagerCareerBridge_Forge) career;
        if (mixinCareer.forgeBridge$isDelayed() && SpongeImplHooks.isMainThread()) {
            mixinCareer.forgeBridge$performDelayedInit();
        }
        if (mixinCareer.forgeBridge$isModded()) {
            // we have to allow forge mods to do their own forge things.
            SpongeForgeVillagerRegistry.populateOffers(this, career, careerLevel, this.rand);
            return;
        }
        // Otherwise, if we are able to control the offers, then go ahead and modify them.
        SpongeForgeVillagerRegistry.spongePopupateList(this, professionForge, careerNumberId, careerLevel, this.rand);

    }

    @Override
    public MerchantRecipeList forgeBridge$getForgeTrades() {
        return this.buyingList;
    }

}
