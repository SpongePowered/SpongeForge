package org.spongepowered.mixin.impl;

import org.spongepowered.api.block.Block;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.LivingEntity;
import org.spongepowered.api.math.Vector3i;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Voxel;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.mixin.Ignore;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

import java.util.Collection;
import java.util.UUID;

@Mixin(net.minecraft.world.World.class)
public abstract class SpongeWorld implements World {

    @Override
    public UUID getUniqueID() {
        return null;
    }

    public String getName(){
        return "World";
    }

    @Ignore
    abstract public Chunk getChunk(int cx, int cz);

    @Override
    public Chunk loadChunk(int cx, int cz, boolean shouldGenerate) {
        return null;
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
