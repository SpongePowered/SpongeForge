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
package org.spongepowered.mod.mixin.core.entity.living.complex;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityEnderCrystal;
import org.spongepowered.api.entity.EnderCrystal;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.complex.EnderDragonPart;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.mixin.core.entity.living.MixinEntityLiving;

import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityDragon.class)
@Implements(@Interface(iface = EnderDragon.class, prefix = "dragon$"))
public abstract class MixinEntityDragon extends MixinEntityLiving {

    @Shadow public EntityDragonPart[] dragonPartArray;
    @Shadow public EntityEnderCrystal healingEnderCrystal;

    public Set<EnderDragonPart> dragon$getParts() {
        Builder<EnderDragonPart> builder = ImmutableSet.builder();

        for (EntityDragonPart part : this.dragonPartArray) {
            builder.add((EnderDragonPart) part);
        }

        return builder.build();
    }

    public Optional<EnderCrystal> getHealingCrystal() {
        return Optional.fromNullable((EnderCrystal) this.healingEnderCrystal);
    }

    public void setHealingCrystal(@Nullable EnderCrystal crystal) {
        this.healingEnderCrystal = (EntityEnderCrystal) crystal;
    }

}
