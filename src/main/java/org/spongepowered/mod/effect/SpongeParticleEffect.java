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

import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import com.flowpowered.math.vector.Vector3f;

@NonnullByDefault
public class SpongeParticleEffect implements ParticleEffect {

    private final ParticleType particle;
    private final Vector3f motion;
    private final Vector3f offset;
    private final int count;

    public SpongeParticleEffect(ParticleType particle, Vector3f motion, Vector3f offset, int count) {
        this.particle = particle;
        this.motion = motion;
        this.offset = offset;
        this.count = count;
    }

    @Override
    public ParticleType getType() {
        return this.particle;
    }

    @Override
    public Vector3f getMotion() {
        return this.motion;
    }

    @Override
    public Vector3f getOffset() {
        return this.offset;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    public static class Colorable extends SpongeParticleEffect implements ParticleEffect.Colorable {

        private final Color color;

        public Colorable(ParticleType.Colorable particle, Vector3f motion, Vector3f offset, int count, Color color) {
            super(particle, motion, offset, count);
            this.color = color;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

    }

    public static class Resizable extends SpongeParticleEffect implements ParticleEffect.Resizable {

        private final float size;

        public Resizable(ParticleType.Resizable particle, Vector3f motion, Vector3f offset, int count, float size) {
            super(particle, motion, offset, count);
            this.size = size;
        }

        @Override
        public float getSize() {
            return this.size;
        }

    }

    public static class Note extends SpongeParticleEffect implements ParticleEffect.Note {

        private final float note;

        public Note(ParticleType.Note particle, Vector3f motion, Vector3f offset, int count, float note) {
            super(particle, motion, offset, count);
            this.note = note;
        }

        @Override
        public float getNote() {
            return this.note;
        }

    }

    public static class Material extends SpongeParticleEffect implements ParticleEffect.Material {

        private final ItemStack item;

        public Material(ParticleType.Material particle, Vector3f motion, Vector3f offset, int count, ItemStack item) {
            super(particle, motion, offset, count);
            this.item = item;
        }

        @Override
        public ItemStack getItem() {
            return this.item;
        }

    }

}
