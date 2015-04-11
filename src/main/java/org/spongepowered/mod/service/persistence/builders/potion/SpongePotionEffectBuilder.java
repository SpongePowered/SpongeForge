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
package org.spongepowered.mod.service.persistence.builders.potion;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import net.minecraft.potion.Potion;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.mod.SpongeMod;

public class SpongePotionEffectBuilder implements DataBuilder<PotionEffect> {

    @Override
    public Optional<PotionEffect> build(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(new DataQuery("PotionType")) || !container.contains(new DataQuery("Duration"))
            || !container.contains(new DataQuery("Amplifier")) || !container.contains(new DataQuery("Ambiance"))
            || !container.contains(new DataQuery("ShowsParticles"))) {
            throw new InvalidDataException("The container does not have data pertaining to PotionEffect!");
        }
        String effectName = container.getString(new DataQuery("PotionType")).get();
        PotionEffectType potionType = null;
        for (Potion potion : Potion.potionTypes) {
            if (potion.getName().equalsIgnoreCase(effectName)) {
                potionType = (PotionEffectType) potion;
            }
        }
        if (potionType == null) {
            throw new InvalidDataException("The container has an invalid potion type name: " + effectName);
        }
        int duration = container.getInt(new DataQuery("Duration")).get();
        int amplifier = container.getInt(new DataQuery("Amplifier")).get();
        boolean ambience = container.getBoolean(new DataQuery("Ambience")).get();
        boolean particles = container.getBoolean(new DataQuery("ShowsParticles")).get();
        PotionEffectBuilder builder = SpongeMod.instance.getGame().getRegistry().getBuilderOf(PotionEffectBuilder.class).get();

        return Optional.of(builder.potionType(potionType)
                                  .particles(particles)
                                  .duration(duration)
                                  .amplifier(amplifier)
                                  .ambience(ambience)
                                  .build());
    }
}
