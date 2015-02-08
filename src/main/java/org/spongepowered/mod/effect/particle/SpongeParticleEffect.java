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

import com.flowpowered.math.vector.Vector3f;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.item.inventory.ItemStack;

import java.awt.Color;

public class SpongeParticleEffect implements ParticleEffect {

    private SpongeParticleType type;

    private Vector3f motion;
    private Vector3f offset;

    private int count;

    public SpongeParticleEffect(SpongeParticleType type, Vector3f motion, Vector3f offset, int count) {
        this.motion = motion;
        this.offset = offset;
        this.count = count;
        this.type = type;
    }

    @Override
    public SpongeParticleType getType() {
        return this.type;
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

    public static class Colored extends SpongeParticleEffect implements ParticleEffect.Colorable {

        private Color color;

        public Colored(SpongeParticleType type, Vector3f motion, Vector3f offset, Color color, int count) {
            super(type, motion, offset, count);
            this.color = color;
        }

        @Override
        public Color getColor() {
            return this.color;
        }

    }

    public static class Resized extends SpongeParticleEffect implements ParticleEffect.Resizable {

        private float size;

        public Resized(SpongeParticleType type, Vector3f motion, Vector3f offset, float size, int count) {
            super(type, motion, offset, count);
            this.size = size;
        }

        @Override
        public float getSize() {
            return this.size;
        }

    }

    public static class Note extends SpongeParticleEffect implements ParticleEffect.Note {

        private float note;

        public Note(SpongeParticleType type, Vector3f motion, Vector3f offset, float note, int count) {
            super(type, motion, offset, count);
            this.note = note;
        }

        @Override
        public float getNote() {
            return this.note;
        }

    }

    public static class Materialized extends SpongeParticleEffect implements ParticleEffect.Material {

        private ItemStack item;

        public Materialized(SpongeParticleType type, Vector3f motion, Vector3f offset, ItemStack item, int count) {
            super(type, motion, offset, count);
            this.item = item;
        }

        @Override
        public ItemStack getItem() {
            return this.item;
        }

    }

}
