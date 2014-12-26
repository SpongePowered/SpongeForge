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
package org.spongepowered.mod.effect.particle;

import java.awt.Color;

import net.minecraft.item.Item;

import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Preconditions;

public class SpongeParticleEffectBuilder implements ParticleEffectBuilder {
    protected final SpongeParticleType type;

    protected Vector3f motion = Vector3f.ZERO;
    protected Vector3f offset = Vector3f.ZERO;

    protected int count = 1;

    public SpongeParticleEffectBuilder(SpongeParticleType type) {
        this.type = type;
    }

    @Override
    public SpongeParticleEffectBuilder withMotion(Vector3f motion) {
        Preconditions.checkNotNull(motion, "The motion vector cannot be null! Use Vector3f.ZERO instead!");
        this.motion = motion;
        return this;
    }

    @Override
    public SpongeParticleEffectBuilder withOffset(Vector3f offset) {
        Preconditions.checkNotNull(offset, "The offset vector cannot be null! Use Vector3f.ZERO instead!");
        this.offset = offset;
        return this;
    }

    @Override
    public SpongeParticleEffectBuilder withCount(int count) {
        Preconditions.checkState(count > 0, "The count has to be greater then zero!");
        this.count = count;
        return this;
    }

    @Override
    public SpongeParticleEffect build() throws IllegalStateException {
        return new SpongeParticleEffect(this.type, this.motion, this.offset, this.count);
    }

    public static class BuilderColorable extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Colorable {
        private Color color;

        public BuilderColorable(SpongeParticleType.Colorable type) {
            super(type);
            this.color = type.getDefaultColor();
        }

        @Override
        public BuilderColorable withColor(Color color) {
            Preconditions.checkNotNull(color, "The color cannot be null!");
            this.color = color;
            return this;
        }

        @Override
        public BuilderColorable withMotion(Vector3f motion) {
            return (BuilderColorable) super.withMotion(motion);
        }

        @Override
        public BuilderColorable withOffset(Vector3f motion) {
            return (BuilderColorable) super.withOffset(motion);
        }

        @Override
        public BuilderColorable withCount(int count) {
            return (BuilderColorable) super.withCount(count);
        }

        @Override
        public SpongeParticleEffect.Colored build() throws IllegalStateException {
            return new SpongeParticleEffect.Colored(this.type, this.motion, this.offset, this.color, this.count);
        }

    }

    public static class BuilderResizable extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Resizable {
        private float size;

        public BuilderResizable(SpongeParticleType.Resizable type) {
            super(type);
            this.size = type.getDefaultSize();
        }

        @Override
        public BuilderResizable withSize(float size) {
            Preconditions.checkState(size >= 0f, "The size has to be greater or equal to zero!");
            this.size = size;
            return this;
        }

        @Override
        public BuilderResizable withMotion(Vector3f motion) {
            return (BuilderResizable) super.withMotion(motion);
        }

        @Override
        public BuilderResizable withOffset(Vector3f motion) {
            return (BuilderResizable) super.withOffset(motion);
        }

        @Override
        public BuilderResizable withCount(int count) {
            return (BuilderResizable) super.withCount(count);
        }

        @Override
        public SpongeParticleEffect.Resized build() throws IllegalStateException {
            return new SpongeParticleEffect.Resized(this.type, this.motion, this.offset, this.size, this.count);
        }

    }

    public static class BuilderNote extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Note {
        private float note;

        public BuilderNote(SpongeParticleType.Note type) {
            super(type);
            this.note = type.getDefaultNote();
        }

        @Override
        public BuilderNote withNote(float note) {
            Preconditions.checkState(note >= 0f && note <= 24f, "The note has to scale between 0 and 24!");
            this.note = note;
            return this;
        }

        @Override
        public BuilderNote withMotion(Vector3f motion) {
            return (BuilderNote) super.withMotion(motion);
        }

        @Override
        public BuilderNote withOffset(Vector3f motion) {
            return (BuilderNote) super.withOffset(motion);
        }

        @Override
        public BuilderNote withCount(int count) {
            return (BuilderNote) super.withCount(count);
        }

        @Override
        public SpongeParticleEffect.Note build() throws IllegalStateException {
            return new SpongeParticleEffect.Note(this.type, this.motion, this.offset, this.note, this.count);
        }

    }

    public static class BuilderMaterial extends SpongeParticleEffectBuilder implements ParticleEffectBuilder.Material {
        private ItemStack item;

        public BuilderMaterial(SpongeParticleType.Material type) {
            super(type);
            this.item = type.getDefaultItem();
        }

        @Override
        public BuilderMaterial withItem(ItemStack item) {
            Preconditions.checkNotNull(item, "The item stack cannot be null!");
            this.item = item;
            return this;
        }

        @Override
        public Material withItemType(ItemType item) {
            Preconditions.checkNotNull(item, "The item type cannot be null!");
            this.item = ItemStack.class.cast(new net.minecraft.item.ItemStack((Item) item));
            return null;
        }

        @Override
        public BuilderMaterial withMotion(Vector3f motion) {
            return (BuilderMaterial) super.withMotion(motion);
        }

        @Override
        public BuilderMaterial withOffset(Vector3f motion) {
            return (BuilderMaterial) super.withOffset(motion);
        }

        @Override
        public BuilderMaterial withCount(int count) {
            return (BuilderMaterial) super.withCount(count);
        }

        @Override
        public SpongeParticleEffect.Materialized build() throws IllegalStateException {
            return new SpongeParticleEffect.Materialized(this.type, this.motion, this.offset, this.item, this.count);
        }

    }

}
