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
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.type.entity.CareerRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;

import java.util.List;
import java.util.Optional;

public final class SpongeForgeVillagerRegistry {

    private static final BiMap<VillagerRegistry.VillagerProfession, Profession> professionMap = HashBiMap.create();
    private static final BiMap<VillagerRegistry.VillagerCareer, Career> careerMap = HashBiMap.create();

    private static final BiMap<String, Profession> forgeToSpongeProfessionMap = HashBiMap.create();
    private static final BiMap<String, Career> forgeToSpongeCareerMap = HashBiMap.create();

    static {
        forgeToSpongeProfessionMap.put("minecraft:smith", ProfessionRegistryModule.BLACKSMITH);
        forgeToSpongeCareerMap.put("leather", CareerRegistryModule.getInstance().LEATHERWORKER);
        forgeToSpongeCareerMap.put("armor", CareerRegistryModule.getInstance().ARMORER);
        forgeToSpongeCareerMap.put("tool", CareerRegistryModule.getInstance().TOOL_SMITH);
        forgeToSpongeCareerMap.put("weapon", CareerRegistryModule.getInstance().WEAPON_SMITH);
    }

    public static SpongeProfession syncProfession(VillagerRegistry.VillagerProfession villagerProfession, SpongeProfession profession) {
        final SpongeProfession spongeProfession = (SpongeProfession) forgeToSpongeProfessionMap.get(villagerProfession.getRegistryName().toString());
        if (spongeProfession != null) {
            professionMap.forcePut(villagerProfession, spongeProfession);
        } else {
            professionMap.forcePut(villagerProfession, profession);
        }
        final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) villagerProfession;
        final List<VillagerRegistry.VillagerCareer> careers = mixinProfession.getCareers();
        for (VillagerRegistry.VillagerCareer career : careers) {
            registerForgeCareer(mixinProfession, career); // This should not be recurisve since the profession is being set
        }
        return spongeProfession != null ? spongeProfession : profession;
    }

    public static SpongeCareer syncCareer(VillagerRegistry.VillagerCareer villagerCareer, Career career) {
        final Career spongeCareer = forgeToSpongeCareerMap.get(villagerCareer.getName());
        if (spongeCareer != null) {
            careerMap.forcePut(villagerCareer, spongeCareer);
        } else {
            careerMap.forcePut(villagerCareer, career);
            if (((IMixinVillagerCareer) villagerCareer).isDelayed()) {
                ((IMixinVillagerCareer) villagerCareer).performDelayedInit();
            }
        }
        return  spongeCareer == null ? (SpongeCareer) career : (SpongeCareer) spongeCareer;
    }


    private static Optional<Profession> getProfession(VillagerRegistry.VillagerProfession profession) {
        Profession forgeProfession = professionMap.get(profession);
        if (forgeProfession == null) {

        }
        return Optional.ofNullable(forgeProfession);
    }

    public static Optional<VillagerRegistry.VillagerProfession> getProfession(Profession profession) {
        return Optional.ofNullable(professionMap.inverse().get(profession));
    }

    public static Optional<SpongeCareer> fromNative(VillagerRegistry.VillagerCareer career) {
        return Optional.ofNullable((SpongeCareer) careerMap.get(career));
    }

    public static Optional<SpongeProfession> fromNative(VillagerRegistry.VillagerProfession profession) {
        return Optional.ofNullable((SpongeProfession) professionMap.get(profession));
    }

    public static void registerForgeCareer(IMixinVillagerProfession mixinVillagerProfession,
        VillagerRegistry.VillagerCareer career) {
        final VillagerRegistry.VillagerProfession villagerProfession = ((IMixinVillagerCareer) career).getProfession();
        final Optional<Profession> spongeProfession = getProfession(villagerProfession);
        if (!spongeProfession.isPresent()) {
            if (mixinVillagerProfession != villagerProfession) {
                final PrettyPrinter printer = new PrettyPrinter(60);
                printer.add("Incorrect Villager Profession retrieved for provided Career!").centre().hr()
                    .add("Sponge is attempting to bridge compatibility with mod provided villager careers and professions, but occasionally a career is missed for some reason.")
                    .add()
                    .add("Retrieved profession:" + mixinVillagerProfession)
                    .add("Career provided profession:" + villagerProfession)
                    .add()
                    .add("Sponge will attempt to proceed and correct this, but usually needs to have some sort of correction in mod related code")
                    .log(SpongeImpl.getLogger(), Level.WARN);
                ((IMixinVillagerCareer) career).forceProfession(villagerProfession);
            }


        }
        spongeProfession.ifPresent(profession -> {
            final SpongeCareer suggestedCareer = new SpongeCareer(((IMixinVillagerCareer) career).getId(), career.getName(), profession, new
              SpongeTranslation("entity.Villager." + career.getName()));
            final SpongeCareer registeredCareer = syncCareer(career, suggestedCareer);
            CareerRegistryModule.getInstance().registerCareer(registeredCareer);
        });
        if (!spongeProfession.isPresent()) {
            SpongeImpl.getLogger().debug("Delaying registration of career {} until its profession {} is registered with Forge.", career.getName(), villagerProfession.getRegistryName());
        }
    }
}
