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
package org.spongepowered.mod.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.SpongeVillagerRegistry;
import org.spongepowered.common.registry.type.entity.CareerRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.mod.interfaces.IMixinEntityVillagerForge;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public final class SpongeForgeVillagerRegistry {


    private static final BiMap<String, Profession> ALIASED_PROFESSION_BIMAP = HashBiMap.create();
    private static final BiMap<String, Career> ALIASED_CAREER_MAP = HashBiMap.create();

    static {
        ALIASED_PROFESSION_BIMAP.put("minecraft:smith", ProfessionRegistryModule.BLACKSMITH);
        ALIASED_CAREER_MAP.put("leather", CareerRegistryModule.getInstance().LEATHERWORKER);
        ALIASED_CAREER_MAP.put("armor", CareerRegistryModule.getInstance().ARMORER);
        ALIASED_CAREER_MAP.put("tool", CareerRegistryModule.getInstance().TOOL_SMITH);
        ALIASED_CAREER_MAP.put("weapon", CareerRegistryModule.getInstance().WEAPON_SMITH);
    }

    @Nonnull
    public static SpongeProfession fromNative(VillagerRegistry.VillagerProfession profession) {
        final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) profession;
        return mixinProfession.getSpongeProfession().orElseGet(() -> {
            final int id = ((ForgeRegistry<VillagerRegistry.VillagerProfession>) ForgeRegistries.VILLAGER_PROFESSIONS).getID(profession);
            final SpongeProfession newProfession = new SpongeProfession(id, mixinProfession.getId(), mixinProfession.getProfessionName());
            mixinProfession.setSpongeProfession(newProfession);
            ProfessionRegistryModule.getInstance().registerAdditionalCatalog(newProfession);
            return newProfession;
        });
    }

    public static SpongeCareer fromNative(VillagerRegistry.VillagerCareer career) {
        final IMixinVillagerCareer mixinCareer = (IMixinVillagerCareer) career;
        if (mixinCareer.isDelayed() && SpongeImplHooks.isMainThread()) {
            mixinCareer.performDelayedInit();
        }
        return mixinCareer.getSpongeCareer().orElseGet(() -> {
            final int careerId = mixinCareer.getId();
            final SpongeCareer suggestedCareer = new SpongeCareer(careerId, career.getName(), fromNative(mixinCareer.getProfession()), new SpongeTranslation("entity.Villager." + career.getName()));
            mixinCareer.setSpongeCareer(suggestedCareer);
            CareerRegistryModule.getInstance().registerCareer(suggestedCareer);
            ProfessionRegistryModule.getInstance().registerCareerForProfession(suggestedCareer);
            return suggestedCareer;
        });
    }

    @SuppressWarnings("unchecked")
    public static void spongePopupateList(IMixinEntityVillagerForge mixinEntityVillager, VillagerRegistry.VillagerProfession professionForge,
        int careerNumberId,
        int careerLevel, Random rand) {
        // Sponge Start - validate the found profession and career.
        // Sync up the profession with forge first before getting the sponge equivalent. Some mods
        // have custom villagers, so some of our injections don't end up getting called (Ice and Fire mod is an example)
        // so re-syncing the and setting the profession for sponge is the first thing we need to do
        if (!mixinEntityVillager.getProfession().isPresent()) {
            mixinEntityVillager.setProfession(SpongeForgeVillagerRegistry.fromNative(professionForge));
        }
        // Then we can get the profession from the villager
        final Profession profession = mixinEntityVillager.getProfession().get();
        // Get the career from forge
        final VillagerRegistry.VillagerCareer career = professionForge.getCareer(careerNumberId);

        // Sponge  - use our own registry stuffs to get the careers now that we've verified they are registered
        final List<Career> careers = ((SpongeProfession) profession).getUnderlyingCareers();
        // At this point the career should be validted and we can safely retrieve the career
        if (careers.size() <= careerNumberId) {
            // Mismatch of lists somehow.

            final List<Career> underlyingCareers = ((SpongeProfession) profession).getUnderlyingCareers();
            underlyingCareers.clear();
            // Try to re-add every career based on the profession's career
            final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) professionForge;
            for (VillagerRegistry.VillagerCareer villagerCareer : mixinProfession.getCareers()) {
                fromNative(villagerCareer); // attempts to re-set the career just in case.
            }
            if (careers.size() <= careerNumberId) {
                // at this point, there's something wrong and we need to print out once the issue:
                printMismatch(careerNumberId, profession, careers, mixinProfession);

                populateOffers(mixinEntityVillager, career, careerLevel, rand);
                return;
            }
        }
        final SpongeCareer spongeCareer = (SpongeCareer) careers.get(careerNumberId);

        SpongeVillagerRegistry.getInstance().populateOffers((Merchant) mixinEntityVillager, (List<TradeOffer>) (List<?>) mixinEntityVillager.getForgeTrades(), spongeCareer, careerLevel + 1, rand);
    }

    @SuppressWarnings("unchecked")
    public static void populateOffers(IMixinEntityVillagerForge mixinEntityVillager, VillagerRegistry.VillagerCareer career,
        int careerLevel, Random rand) {
        // Try sponge's career:
        final MerchantRecipeList villagerTrades = mixinEntityVillager.getForgeTrades();
        List<EntityVillager.ITradeList> trades = career.getTrades(careerLevel);
        if (trades != null && !trades.isEmpty()) {
            for (EntityVillager.ITradeList entityvillager$itradelist : trades) {
                entityvillager$itradelist.addMerchantRecipe((EntityVillager) mixinEntityVillager, villagerTrades, rand);
            }
        }
    }

    private static void printDebuggingVillagerInfo(IMixinEntityVillagerForge mixinEntityVillager, MerchantRecipeList temporary) {
        final PrettyPrinter printer = new PrettyPrinter(60).add("Printing Villager Information")
            .centre().hr()
            .add("Sponge is going to print out information on all the merchant recipes based on mods");
        printer.add("Printing added recipes to add");
        printer.add();
        printer.add("Adding");
        for (int i = 0; i < temporary.size(); i++) {
            final MerchantRecipe recipe = temporary.get(i);
            printer.add(" %d", i)
                .add(" %s + %s = %s", recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell());
        }

        printer.add();

        printer.add("Now printing existing:").add();

        final MerchantRecipeList existing = mixinEntityVillager.getForgeTrades();
        printer.add("Existing");
        for (int i = 0; i < existing.size(); i++) {
            final MerchantRecipe recipe = existing.get(i);
            printer.add(" %d", i)
                .add(" %s + %s = %s", recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell());
        }

        final List<MerchantRecipe> filtered = existing.stream()
                    .filter(recipe -> !temporary.contains(recipe))
                    .collect(Collectors.toList());
        final List<MerchantRecipe> forgeFiltered = temporary.stream()
                    .filter(recipe -> !filtered.contains(recipe))
                    .collect(Collectors.toList());
        existing.addAll(filtered);
        existing.addAll(forgeFiltered);

        printer.add().add("Finalized List");
        for (int i = 0; i < existing.size(); i++) {
            final MerchantRecipe recipe = existing.get(i);
            printer.add(" %d", i)
                .add(" %s + %s = %s", recipe.getItemToBuy(), recipe.getSecondItemToBuy(), recipe.getItemToSell());
        }

        printer.log(SpongeImpl.getLogger(), Level.ERROR);
    }

    private static void printMismatch(int careerNumberId, Profession profession, List<Career> careers, IMixinVillagerProfession mixinProfession) {
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
            printer.add("  %s %d : %s", "Career", i++, villagerCareer.getName());
        }
        printer.add();
        printer.add("%s : %s", "Sponge Profession", profession.getKey().toString());
        i = 0;
        for (Career spongeCareer : careers) {
            printer.add("  %s %d : %s", "Career", i++, spongeCareer.getKey().toString());
        }
        printer.add();
        printer.add("Villager career id attempted: " + careerNumberId);
        printer.log(SpongeImpl.getLogger(), Level.ERROR);
    }
}
