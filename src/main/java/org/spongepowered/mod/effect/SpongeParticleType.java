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

import net.minecraft.util.EnumParticleTypes;

import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class SpongeParticleType implements ParticleType {

    private final EnumParticleTypes minecraftType;
    private final boolean hasMotion;

    public SpongeParticleType(EnumParticleTypes minecraftType, boolean hasMotion) {
        this.minecraftType = minecraftType;
        this.hasMotion = hasMotion;
    }

    @Override
    public String getName() {
        return this.minecraftType.func_179346_b();
    }

    @Override
    public boolean hasMotion() {
        return this.hasMotion;
    }

    public EnumParticleTypes getMinecraftType() {
        return this.minecraftType;
    }

    public static class Colorable extends SpongeParticleType implements ParticleType.Colorable {

        private final Color defaultColor;

        public Colorable(EnumParticleTypes minecraftType, boolean hasMotion, Color defaultColor) {
            super(minecraftType, hasMotion);
            this.defaultColor = defaultColor;
        }

        @Override
        public Color getDefaultColor() {
            return this.defaultColor;
        }

    }

    public static class Resizable extends SpongeParticleType implements ParticleType.Resizable {

        private final float defaultSize;

        public Resizable(EnumParticleTypes minecraftType, boolean hasMotion, float defaultSize) {
            super(minecraftType, hasMotion);
            this.defaultSize = defaultSize;
        }

        @Override
        public float getDefaultSize() {
            return this.defaultSize;
        }

    }

    public static class Note extends SpongeParticleType implements ParticleType.Note {

        private final float defaultNote;

        public Note(EnumParticleTypes minecraftType, boolean hasMotion, float defaultNote) {
            super(minecraftType, hasMotion);
            this.defaultNote = defaultNote;
        }

        @Override
        public float getDefaultNote() {
            return this.defaultNote;
        }

    }

    public static class Material extends SpongeParticleType implements ParticleType.Material {

        private final ItemStack defaultItem;

        public Material(EnumParticleTypes minecraftType, boolean hasMotion, ItemStack defaultItem) {
            super(minecraftType, hasMotion);
            this.defaultItem = defaultItem;
        }

        @Override
        public ItemStack getDefaultItem() {
            return this.defaultItem;
        }

    }
}
