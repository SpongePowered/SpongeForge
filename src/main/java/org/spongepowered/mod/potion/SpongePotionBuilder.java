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
package org.spongepowered.mod.potion;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;

public class SpongePotionBuilder implements PotionEffectBuilder {

    private PotionEffectType potionType;
    private int duration;
    private int amplifier;
    private boolean isAmbient;
    private boolean showParticles;

    public SpongePotionBuilder() {
        reset();
    }

    @Override
    public PotionEffectBuilder potionType(PotionEffectType potionEffectType) {
        checkNotNull(potionEffectType, "Potion effect type cannot be null");
        this.potionType = potionEffectType;
        return this;
    }

    @Override
    public PotionEffectBuilder duration(int duration) {
        checkArgument(duration > 0, "Duration must be greater than 0");
        this.duration = duration;
        return this;
    }

    @Override
    public PotionEffectBuilder amplifier(int amplifier) throws IllegalArgumentException {
        checkArgument(amplifier >= 0, "Amplifier must not be negative");
        this.amplifier = amplifier;
        return this;
    }

    @Override
    public PotionEffectBuilder ambience(boolean ambience) {
        this.isAmbient = ambience;
        return this;
    }

    @Override
    public PotionEffectBuilder particles(boolean showsParticles) {
        this.showParticles = showsParticles;
        return this;
    }

    @Override
    public PotionEffectBuilder reset() {
        this.potionType = null;
        this.duration = 0;
        this.amplifier = 0;
        this.isAmbient = false;
        this.showParticles = true;
        return this;
    }

    @Override
    public PotionEffect build() throws IllegalStateException {
        checkState(this.potionType != null, "Potion type has not been set");
        checkState(this.duration > 0, "Duration has not been set");
        return (PotionEffect) new net.minecraft.potion.PotionEffect(((net.minecraft.potion.Potion) this.potionType).id, this.duration,
                this.amplifier,
                this.isAmbient,
                this.showParticles);
    }
}
