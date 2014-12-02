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
package org.spongepowered.mixin.impl;

import com.google.common.base.Optional;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.math.Vector2i;
import org.spongepowered.api.math.Vector3d;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;
import org.spongepowered.wrapper.BlockWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World {

    @Shadow
    public WorldProvider provider;

    @Shadow
    protected WorldInfo worldInfo;

    @Override
    public UUID getUniqueID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return worldInfo.getWorldName() + "_" + provider.getDimensionName().toLowerCase().replace(' ', '_');
    }

    @Override
    public Optional<Chunk> getChunk(Vector2i position) {
        return Optional.absent();
    }

    @Override
    public Optional<Chunk> loadChunk(Vector2i position, boolean shouldGenerate) {
        return Optional.absent();
    }

    @Override
    public Chunk loadChunk(Vector2i position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockLoc getBlock(Vector3d position) {
        // TODO: MC's BlockPos does some sort of special rounding on double positions -- do we want to do that too?
        return new BlockWrapper(this, (int) position.getX(), (int) position.getY(), (int) position.getZ());
    }

    @Override
    public BlockLoc getBlock(int x, int y, int z) {
        return new BlockWrapper(this, x, y, z);
    }

    @Override
    public Biome getBiome(Vector3d position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Entity> getEntities() {
        return new ArrayList<Entity>();
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        return Optional.absent();
    }
}
