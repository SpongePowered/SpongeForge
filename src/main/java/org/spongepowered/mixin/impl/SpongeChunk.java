package org.spongepowered.mixin.impl;

import org.spongepowered.api.block.Block;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.LivingEntity;
import org.spongepowered.api.math.Vector3i;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Voxel;
import org.spongepowered.mod.mixin.Ignore;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

import java.util.Collection;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class SpongeChunk implements Chunk {

    @Shadow int xPosition;
    @Shadow int zPosition;

    @Override
    public int getX() {
        return xPosition;
    }

    @Override
    public int getZ() {
        return zPosition;
    }

    @Override
    public Collection<Entity> getEntities() {
        return null;
    }

    @Override
    public Collection<LivingEntity> getLivingEntities() {
        return null;
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> entityClass) {
        return null;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<? extends Entity>... entityClasses) {
        return null;
    }

    @Ignore
    public abstract Block getBlock(int x, int y, int z);

    @Override
    public Voxel getVoxel(int x, int y, int z) {
        return null;
    }

    @Override
    public Block getBlock(Vector3i location) {
        return null;
    }

    @Override
    public Voxel getVoxel(Vector3i location) {
        return null;
    }
}
