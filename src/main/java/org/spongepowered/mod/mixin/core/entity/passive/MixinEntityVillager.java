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
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.interfaces.entity.IMixinVillager;
import org.spongepowered.common.mixin.core.entity.MixinEntityAgeable;
import org.spongepowered.common.registry.SpongeVillagerRegistry;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(value = EntityVillager.class, priority = 1100)
public abstract class MixinEntityVillager extends MixinEntityAgeable implements Villager, IMixinVillager {

    @Shadow private int careerId;
    @Shadow private int careerLevel;
    @Shadow @Nullable public MerchantRecipeList buyingList;

    @Shadow(remap = false) public abstract VillagerRegistry.VillagerProfession getProfessionForge();

    /**
     * @author gabizou - April 8th, 2018
     * @reason This overwrites the forge handling to cater to Sponge's villager handling.
     * There have been too many bugs with the re-assignment of VillagerProfessions and
     * mods not registering them normally, which ends up causing sync issues between the
     * forge professions and sponge professions. This aims to have failsafes for handling
     * with Sponge's system.
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public void populateBuyingList() {
        // Sponge - only get the profession once
        final VillagerRegistry.VillagerProfession professionForge = this.getProfessionForge();
        if (this.careerId != 0 && this.careerLevel != 0) {
            ++this.careerLevel;
        } else {
            this.careerId = professionForge.getRandomCareer(this.rand) + 1;
            this.careerLevel = 1;
        }

        if (this.buyingList == null) {
            this.buyingList = new MerchantRecipeList();
        }

        int careerNumberId = this.careerId - 1;
        int careerLevel = this.careerLevel - 1;
        // Sponge Start - validate the found profession and career.
        final Profession profession = this.getProfession();
        // Set the profession back to the villager to re-sync sponge's career system
        this.setProfession(SpongeForgeVillagerRegistry.syncProfession(professionForge, (SpongeProfession) profession));
        final VillagerRegistry.VillagerCareer career = professionForge.getCareer(careerNumberId);

        // Sponge  - use our own registry stuffs to get the careers now that we've verified they are registered
        final List<Career> careers = (List<Career>) profession.getCareers();
        // At this point the career should be validted and we can safely retrieve the career
        if (careers.size() <= careerNumberId) {
            // Mismatch of lists somehow.

            final List<Career> underlyingCareers = ((SpongeProfession) profession).getUnderlyingCareers();
            underlyingCareers.clear();
            // Try to re-add every career based on the profession's career
            final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) professionForge;
            for (VillagerRegistry.VillagerCareer villagerCareer : mixinProfession.getCareers()) {
                SpongeForgeVillagerRegistry.registerForgeCareer(mixinProfession, villagerCareer);
            }
            if (careers.size() <= careerNumberId) {
                // at this point, there's something wrong and we need to print out once the issue:
                final PrettyPrinter printer = new PrettyPrinter(60).add("Sponge Forge Career Mismatch!").centre().hr()
                    .addWrapped(
                        "Sponge is attemping to recover from a mismatch from a Forge mod provided VillagerCareer and Sponge's Career implementation.")
                    .add()
                    .addWrapped(
                        "Due to the issue, Sponge is printing out all this information to assist sponge resolving the issue. Please open an issue on github for SpongeForge")
                    .add();
                printer.add("%s : %s", "Forge Profession", mixinProfession.getProfessionName());
                int i = 0;
                for (VillagerRegistry.VillagerCareer villagerCareer : mixinProfession.getCareers()) {
                    printer.add("  %s %n : %s", "Career", i++, villagerCareer.getName());
                }
                printer.add();
                printer.add("%s : %s", "Sponge Profession", profession.getId());
                i = 0;
                for (Career spongeCareer : careers) {
                    printer.add("  %s %n : %s", "Career", i++, spongeCareer.getId());
                }
                printer.add();
                printer.add("Villager career id attempted: " + careerNumberId);
                printer.log(SpongeImpl.getLogger(), Level.ERROR);

                performMerchantFillFromForge(careerNumberId, career);
                return;
            }
        }
        final SpongeCareer spongeCareer = (SpongeCareer) careers.get(careerNumberId);
        SpongeForgeVillagerRegistry.syncCareer(career, spongeCareer);

        try {
            SpongeVillagerRegistry.getInstance()
                .populateOffers(this, (List<TradeOffer>) (List<?>) this.buyingList, spongeCareer, this.careerLevel, this.rand);
        } catch (Exception e) {
            performMerchantFillFromForge(careerLevel, career);
        }
    }

    private void performMerchantFillFromForge(int careerLevel, VillagerRegistry.VillagerCareer career) {
        // If all else fails, fall back to forge's careers and get the trades.
        List<EntityVillager.ITradeList> trades = career.getTrades(careerLevel);
        final MerchantRecipeList temp = new MerchantRecipeList();

        if (trades != null) {
            for (EntityVillager.ITradeList tradeList : trades) {
                tradeList.addMerchantRecipe((EntityVillager) (Object) this, temp, this.rand);
            }
        }

        // Then sync them back
        for (final Iterator<MerchantRecipe> iterator = this.buyingList.iterator(); iterator.hasNext(); ) {
            final MerchantRecipe merchantRecipe = iterator.next();
            boolean exists = false;
            for (MerchantRecipe recipe : temp) {
                if (merchantRecipe.equals(recipe)) {
                    exists = true;
                }
            }
            if (!exists) {
                iterator.remove();
            }
        }
        final List<MerchantRecipe> filtered = this.buyingList.stream()
            .filter(temp::contains)
            .collect(Collectors.toList());
        final List<MerchantRecipe> forgeFiltered = temp.stream()
            .filter(filtered::contains)
            .collect(Collectors.toList());
        this.buyingList = new MerchantRecipeList();
        this.buyingList.addAll(filtered);
        this.buyingList.addAll(forgeFiltered);
    }
}
