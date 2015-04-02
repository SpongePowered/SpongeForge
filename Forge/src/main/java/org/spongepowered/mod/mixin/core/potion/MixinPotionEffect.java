/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.mod.mixin.core.potion;

import static org.spongepowered.api.service.persistence.data.DataQuery.of;

import net.minecraft.potion.Potion;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(net.minecraft.potion.PotionEffect.class)
@Implements(@Interface(iface = PotionEffect.class, prefix = "potionEffect$"))
public abstract class MixinPotionEffect implements PotionEffect {

    @Shadow
    public abstract int getPotionID();

    @Shadow
    private int duration;

    @Shadow
    private int amplifier;

    @Shadow
    private boolean isAmbient;

    @Shadow
    private boolean showParticles;

    @Override
    public PotionEffectType getType() {
        return (PotionEffectType) Potion.potionTypes[getPotionID()];
    }

    public int potionEffect$getDuration() {
        return this.duration;
    }

    public int potionEffect$getAmplifier() {
        return this.amplifier;
    }

    @Override
    public boolean isAmbient() {
        return this.isAmbient;
    }

    @Override
    public boolean getShowParticles() {
        return this.showParticles;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("PotionType"), Potion.potionTypes[getPotionID()].getName());
        container.set(of("Duration"), this.duration);
        container.set(of("Amplifier"), this.amplifier);
        container.set(of("Ambience"), this.isAmbient);
        container.set(of("ShowsParticles"), this.showParticles);
        return container;
    }
}
