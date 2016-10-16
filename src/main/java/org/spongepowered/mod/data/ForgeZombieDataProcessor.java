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
package org.spongepowered.mod.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.type.ZombieTypes;
import org.spongepowered.common.data.processor.data.entity.ZombieDataProcessor;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.Map;
import java.util.Optional;

public class ForgeZombieDataProcessor extends ZombieDataProcessor {

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    protected boolean set(EntityZombie dataHolder, Map<Key<?>, Object> keyValues) {
        ZombieType type = (ZombieType) keyValues.get(Keys.ZOMBIE_TYPE);
        Profession prof = (Profession) keyValues.get(Keys.VILLAGER_ZOMBIE_PROFESSION);
        // If It's a native type, just set it directly
        if(EntityUtil.isNative(type, prof)) {
            dataHolder.setZombieType(EntityUtil.toNative(type, prof));
            return true;
        }

        Optional<VillagerRegistry.VillagerProfession> forgeProf = SpongeForgeVillagerRegistry.getProfession(prof);
        if (forgeProf.isPresent()) {
            dataHolder.setVillagerType(forgeProf.get());
            return true;
        }

        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityZombie dataHolder) {
        VillagerRegistry.VillagerProfession forgeProf = dataHolder.getVillagerTypeForge();
        if (forgeProf == null) {
            ZombieType type = EntityUtil.typeFromNative(dataHolder.getZombieType());
            return ImmutableMap.of(Keys.ZOMBIE_TYPE, type, Keys.VILLAGER_ZOMBIE_PROFESSION, Optional.empty());
        }

        return ImmutableMap.of(Keys.ZOMBIE_TYPE, ZombieTypes.VILLAGER,
                Keys.VILLAGER_ZOMBIE_PROFESSION, SpongeForgeVillagerRegistry.getProfession(dataHolder.getVillagerTypeForge()));
    }
}
