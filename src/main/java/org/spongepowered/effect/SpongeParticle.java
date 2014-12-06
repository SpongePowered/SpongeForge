/**
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
package org.spongepowered.effect;

import net.minecraft.util.EnumParticleTypes;

import org.spongepowered.api.effect.Particle;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class SpongeParticle implements Particle {
    protected String name;
    
    public SpongeParticle(String name) {
        Preconditions.checkNotNull(name, "Particle name cannot be null");
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Gets corresponding vanilla particle enum for this particle, if possible.
     * @return Vanilla particle enum if available
     */
    public Optional<EnumParticleTypes> getVanillaParticleType() {
        try {
            return Optional.of(EnumParticleTypes.valueOf(name));
        } catch (IllegalArgumentException e) {
            // Given name doesn't refer to any vanilla particle type
        }
        
        return Optional.absent();
    }
}
