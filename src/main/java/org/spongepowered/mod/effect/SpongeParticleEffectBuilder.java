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
package org.spongepowered.mod.effect;

import java.awt.Color;

import net.minecraft.item.Item;

import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Preconditions;

@NonnullByDefault
public class SpongeParticleEffectBuilder implements ParticleEffectBuilder {

    protected final ParticleType particle;
    protected Vector3f motion = Vector3f.ZERO;
    protected Vector3f offset = Vector3f.ZERO;
    protected int count = 1;

    public SpongeParticleEffectBuilder(ParticleType particle) {
        this.particle = particle;
    }

    @Override
    public ParticleEffectBuilder motion(Vector3f motion) {
        this.motion = motion;
        return this;
    }

    @Override
    public ParticleEffectBuilder offset(Vector3f offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public ParticleEffectBuilder count(int count) throws IllegalArgumentException {
        Preconditions.checkArgument(count > 0);
        this.count = count;
        return this;
    }

    @Override
    public ParticleEffect build() {
        return new SpongeParticleEffect(this.particle, this.motion, this.offset, this.count);
    }

    // Everything below is currently unusable
    public class Colorable extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Colorable {

        private Color color;

        public Colorable(ParticleType.Colorable particle) {
            super(particle);
            this.color = particle.getDefaultColor();
        }

        @Override
        public ParticleEffectBuilder.Colorable color(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public ParticleEffectBuilder.Colorable motion(Vector3f motion) {
            super.motion(motion);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Colorable offset(Vector3f offset) {
            super.offset(offset);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Colorable count(int count) {
            super.count(count);
            return this;
        }

        @Override
        public ParticleEffect.Colorable build() {
            return new SpongeParticleEffect.Colorable((ParticleType.Colorable) this.particle, this.motion, this.offset, this.count, this.color);
        }

    }

    public class Resizable extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Resizable {

        private float size;

        public Resizable(ParticleType.Resizable particle) {
            super(particle);
            this.size = particle.getDefaultSize();
        }

        @Override
        public ParticleEffectBuilder.Resizable size(float size) {
            this.size = size;
            return this;
        }

        @Override
        public ParticleEffectBuilder.Resizable motion(Vector3f motion) {
            super.motion(motion);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Resizable offset(Vector3f offset) {
            super.offset(offset);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Resizable count(int count) {
            super.count(count);
            return this;
        }

        @Override
        public ParticleEffect.Resizable build() {
            return new SpongeParticleEffect.Resizable((ParticleType.Resizable) this.particle, this.motion, this.offset, this.count, this.size);
        }

    }

    public class Note extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Note {

        private float note;

        public Note(ParticleType.Note particle) {
            super(particle);
            this.note = particle.getDefaultNote();
        }

        @Override
        public ParticleEffectBuilder.Note note(float note) {
            this.note = note;
            return this;
        }

        @Override
        public ParticleEffectBuilder.Note motion(Vector3f motion) {
            super.motion(motion);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Note offset(Vector3f offset) {
            super.offset(offset);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Note count(int count) {
            super.count(count);
            return this;
        }

        @Override
        public ParticleEffect.Note build() {
            return new SpongeParticleEffect.Note((ParticleType.Note) this.particle, this.motion, this.offset, this.count, this.note);
        }

    }

    public class Material extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Material {

        private ItemStack item;

        public Material(ParticleType.Material particle) {
            super(particle);
            this.item = particle.getDefaultItem();
        }

        @Override
        public ParticleEffectBuilder.Material item(ItemStack item) {
            this.item = item;
            return this;
        }

        @Override
        public ParticleEffectBuilder.Material itemType(ItemType item) {
            this.item = (ItemStack) new net.minecraft.item.ItemStack((Item) item);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Material motion(Vector3f motion) {
            super.motion(motion);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Material offset(Vector3f offset) {
            super.offset(offset);
            return this;
        }

        @Override
        public ParticleEffectBuilder.Material count(int count) {
            super.count(count);
            return this;
        }

        @Override
        public ParticleEffect.Material build() {
            return new SpongeParticleEffect.Material((ParticleType.Material) this.particle, this.motion, this.offset, this.count, this.item);
        }

    }
}
