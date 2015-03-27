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
package org.spongepowered.common.effect.particle;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Objects;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.item.inventory.ItemStack;

import java.awt.Color;

public class SpongeParticleEffect implements ParticleEffect {

    private final SpongeParticleType type;

    private final Vector3d motion;
    private final Vector3d offset;

    private final int count;

    public SpongeParticleEffect(SpongeParticleType type, Vector3d motion, Vector3d offset, int count) {
        this.type = checkNotNull(type, "type");
        this.motion = checkNotNull(motion, "motion");
        this.offset = checkNotNull(offset, "offset");
        this.count = count;
    }

    @Override
    public SpongeParticleType getType() {
        return this.type;
    }

    @Override
    public Vector3d getMotion() {
        return this.motion;
    }

    @Override
    public Vector3d getOffset() {
        return this.offset;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("type", this.type)
                .add("motion", this.motion)
                .add("offset", this.offset)
                .add("count", this.count);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    public static class Colored extends SpongeParticleEffect implements ParticleEffect.Colorable {

        private final Color color;

        public Colored(SpongeParticleType type, Vector3d motion, Vector3d offset, Color color, int count) {
            super(type, motion, offset, count);
            this.color = color;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("color", this.color);
        }

    }

    public static class Resized extends SpongeParticleEffect implements ParticleEffect.Resizable {

        private final float size;

        public Resized(SpongeParticleType type, Vector3d motion, Vector3d offset, float size, int count) {
            super(type, motion, offset, count);
            this.size = size;
        }

        @Override
        public float getSize() {
            return this.size;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("size", this.size);
        }

    }

    public static class Note extends SpongeParticleEffect implements ParticleEffect.Note {

        private final float note;

        public Note(SpongeParticleType type, Vector3d motion, Vector3d offset, float note, int count) {
            super(type, motion, offset, count);
            this.note = note;
        }

        @Override
        public float getNote() {
            return this.note;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("note", this.note);
        }

    }

    public static class Materialized extends SpongeParticleEffect implements ParticleEffect.Material {

        private final ItemStack item;

        public Materialized(SpongeParticleType type, Vector3d motion, Vector3d offset, ItemStack item, int count) {
            super(type, motion, offset, count);
            this.item = item;
        }

        @Override
        public ItemStack getItem() {
            return this.item;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("item", this.item);
        }

    }

}
