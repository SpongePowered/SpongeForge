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

import com.google.common.base.Objects;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.awt.Color;

public class SpongeParticleType implements ParticleType {

    private final EnumParticleTypes type;
    private final boolean motion;

    public SpongeParticleType(EnumParticleTypes type, boolean motion) {
        this.motion = checkNotNull(motion, "motion");
        this.type = checkNotNull(type, "type");
    }

    public EnumParticleTypes getInternalType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.type.getParticleName();
    }

    @Override
    public boolean hasMotion() {
        return this.motion;
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("type", this.type)
                .add("motion", this.motion);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).toString();
    }

    public static class Colorable extends SpongeParticleType implements ParticleType.Colorable {

        private final Color color;

        public Colorable(EnumParticleTypes type, Color color) {
            super(type, false);
            this.color = color;
        }

        @Override
        public Color getDefaultColor() {
            return this.color;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("color", this.color);
        }

    }

    public static class Resizable extends SpongeParticleType implements ParticleType.Resizable {

        private final float size;

        public Resizable(EnumParticleTypes type, float size) {
            super(type, false);
            this.size = size;
        }

        @Override
        public float getDefaultSize() {
            return this.size;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("size", this.size);
        }

    }

    public static class Note extends SpongeParticleType implements ParticleType.Note {

        private final float note;

        public Note(EnumParticleTypes type, float note) {
            super(type, false);
            this.note = note;
        }

        @Override
        public float getDefaultNote() {
            return this.note;
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("note", this.note);
        }

    }

    public static class Material extends SpongeParticleType implements ParticleType.Material {

        // TODO: This should change to the sponge item stack type if a clone method available is
        private net.minecraft.item.ItemStack item;

        public Material(EnumParticleTypes type, net.minecraft.item.ItemStack item, boolean motion) {
            super(type, motion);
            this.item = item;
        }

        @Override
        public ItemStack getDefaultItem() {
            return (ItemStack) this.item.copy();
        }

        @Override
        protected Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("item", this.item);
        }

    }

}
