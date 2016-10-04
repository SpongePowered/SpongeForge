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

import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.api.data.type.ZombieType;
import org.spongepowered.api.data.type.ZombieTypes;
import org.spongepowered.common.data.processor.value.entity.ZombieTypeValueProcessor;
import org.spongepowered.common.entity.EntityUtil;

import java.util.Optional;

public class ForgeZombieTypeValueProcessor extends ZombieTypeValueProcessor {

    @Override
    public int getPriority() {
        return 200;
    }

    // Forge sets custom villagers == NORMAL, we need to account for that
    @Override
    protected Optional<ZombieType> getVal(EntityZombie container) {
        net.minecraft.entity.monster.ZombieType nativeType = container.getZombieType();
        if (nativeType == net.minecraft.entity.monster.ZombieType.NORMAL
                && container.getVillagerTypeForge() != null) {
            // Forge zombie, it's a villager
            return Optional.of(ZombieTypes.VILLAGER);
        }

        return Optional.of(EntityUtil.typeFromNative(nativeType));
    }
}
