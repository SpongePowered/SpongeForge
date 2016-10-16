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
package org.spongepowered.mod.mixin.core.entity.living;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.manipulator.mutable.entity.ZombieData;
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.type.ZombieTypes;
import org.spongepowered.api.entity.living.monster.Zombie;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeZombieData;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = EntityZombie.class, priority = 1001)
public abstract class MixinEntityZombie implements Zombie {

    @Shadow(remap = false) @Nullable public abstract VillagerRegistry.VillagerProfession getVillagerTypeForge();
    @Shadow @Nullable public abstract net.minecraft.entity.monster.ZombieType getZombieType();

    // Changes method on Common mixin, to use Forge professions
    @Override
    public ZombieData getZombieData() {
        VillagerRegistry.VillagerProfession forgeProf = getVillagerTypeForge();
        if (forgeProf == null) {
            ZombieType type = EntityUtil.typeFromNative(getZombieType());
            return new SpongeZombieData(type, Optional.empty());
        }
        return new SpongeZombieData(ZombieTypes.VILLAGER,
                SpongeForgeVillagerRegistry.getProfession(getVillagerTypeForge()));
    }

}
