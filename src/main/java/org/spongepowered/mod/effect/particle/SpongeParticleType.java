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
package org.spongepowered.mod.effect.particle;

import java.awt.Color;

import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStack;

import net.minecraft.util.EnumParticleTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class SpongeParticleType implements ParticleType {
    
    protected String name;
    protected boolean hasMotion;
    
    public SpongeParticleType(String name, boolean hasMotion) {
        Preconditions.checkNotNull(name, "Particle name cannot be null");
        this.name = name;
        this.hasMotion = hasMotion;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean hasMotion() {
        return hasMotion;
    }
    
    /**
     * Gets corresponding vanilla particle enum for this particle, if possible.
     * @return Vanilla particle enum if available
     */
    public Optional<EnumParticleTypes> getVanillaParticle() {
        try {
            return Optional.of(EnumParticleTypes.valueOf(name));
        } catch (IllegalArgumentException e) {
            // Given name doesn't refer to any vanilla particle type
        }
        return Optional.absent();
    }
    
    public class Colorable extends SpongeParticleType implements ParticleType.Colorable {
        
        protected Color defColor = Color.RED;
        
        public Colorable(String name, boolean hasMotion, Color defColor) {
            super(name, hasMotion);
            this.defColor = defColor;
        }

        @Override
        public Color getDefaultColor() {
            return defColor;
        }
        
    }

    public class Resizable extends SpongeParticleType implements ParticleType.Resizable {
        
        protected float defSize = 4.0F;
        
        public Resizable(String name, boolean hasMotion, float defSize) {
            super(name, hasMotion);
            this.defSize = defSize;
        }

        @Override
        public float getDefaultSize() {
            return defSize;
        }
        
    }
    
    public class Note extends SpongeParticleType implements ParticleType.Note {

        protected float defNote;
        
        public Note(String name, boolean hasMotion, float defNote) {
            super(name, hasMotion);
            this.defNote = defNote;
        }

        @Override
        public float getDefaultNote() {
            return defNote;
        }
        
    }
    
    public class Material extends SpongeParticleType implements ParticleType.Material {
        
        protected ItemStack defMaterial;
        
        public Material(String name, boolean hasMotion, ItemStack defMaterial) {
            super(name, hasMotion);
            this.defMaterial = defMaterial;
        }

        @Override
        public ItemStack getDefaultItem() {
            return defMaterial;
        }
        
    }
    
}
