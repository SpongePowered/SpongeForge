package org.spongepowered.mod.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.interfaces.entity.IMixinVillager;
import org.spongepowered.common.mixin.core.entity.MixinEntityAgeable;
import org.spongepowered.common.registry.SpongeVillagerRegistry;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(value = EntityVillager.class, priority = 1100)
public abstract class MixinEntityVillager extends MixinEntityAgeable implements Villager, IMixinVillager {

    @Shadow private int careerId;
    @Shadow private int careerLevel;

    @Shadow public abstract VillagerRegistry.VillagerProfession getProfessionForge();

    @Shadow @Nullable public MerchantRecipeList buyingList;

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

        int i = this.careerId - 1;
        int j = this.careerLevel - 1;
        // Sponge Start - validate the found profession and career.
        final Profession profession = this.getProfession();
        // Set the profession back to the villager to re-sync sponge's career system
        this.setProfession(SpongeForgeVillagerRegistry.validateProfession(professionForge, (SpongeProfession) profession));
        final VillagerRegistry.VillagerCareer career = professionForge.getCareer(i);
        // Validate the profession's career is registered with sponge
        SpongeForgeVillagerRegistry.registerForgeCareer(career);

        // Sponge  - use our own registry stuffs to get the careers now that we've verified they are registered
        final List<Career> careers = (List<Career>) profession.getCareers();
        // At this point the career should be validted and we can safely retrieve the career
        final Career careerLevel = careers.get(this.careerId - 1);
        SpongeForgeVillagerRegistry.validateCareer(career, careerLevel);

        try {
            SpongeVillagerRegistry.getInstance()
                .populateOffers(this, (List<TradeOffer>) (List<?>) this.buyingList, careerLevel, this.careerLevel, this.rand);
        } catch (Exception e) {
            // If all else fails, fall back to forge's careers and get the trades.
            List<EntityVillager.ITradeList> trades = career.getTrades(j);
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
                .filter(trade -> temp.contains(trade))
                .collect(Collectors.toList());
            final List<MerchantRecipe> forgeFiltered = temp.stream()
                .filter(filtered::contains)
                .collect(Collectors.toList());
            this.buyingList = new MerchantRecipeList();
            this.buyingList.addAll(filtered);
            this.buyingList.addAll(forgeFiltered);

        }
    }
}
