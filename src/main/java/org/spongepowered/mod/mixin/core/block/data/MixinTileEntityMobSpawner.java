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
package org.spongepowered.mod.mixin.core.block.data;

import com.google.common.collect.Lists;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.block.data.MobSpawner;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.DataView;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;
import org.spongepowered.api.util.WeightedRandomEntity;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Implements(@Interface(iface = MobSpawner.class, prefix = "mobspawner$"))
@Mixin(net.minecraft.tileentity.TileEntityMobSpawner.class)
public abstract class MixinTileEntityMobSpawner extends MixinTileEntity {

    @Shadow
    public abstract MobSpawnerBaseLogic getSpawnerBaseLogic();

    public short mobspawner$getRemainingDelay() {
        return (short) getSpawnerBaseLogic().spawnDelay;
    }

    public void mobspawner$setRemainingDelay(short delay) {
        getSpawnerBaseLogic().spawnDelay = delay;
    }

    public short mobspawner$getMinimumSpawnDelay() {
        return (short) getSpawnerBaseLogic().minSpawnDelay;
    }

    public void mobspawner$setMinimumSpawnDelay(short delay) {
        getSpawnerBaseLogic().minSpawnDelay = delay;
    }

    public short mobspawner$getMaximumSpawnDelay() {
        return (short) getSpawnerBaseLogic().maxSpawnDelay;
    }

    public void mobspawner$setMaximumSpawnDelay(short delay) {
        getSpawnerBaseLogic().maxSpawnDelay = delay;
    }

    public short mobspawner$getSpawnCount() {
        return (short) getSpawnerBaseLogic().spawnCount;
    }

    public void mobspawner$setSpawnCount(short count) {
        getSpawnerBaseLogic().spawnCount = count;
    }

    public short mobspawner$getMaximumNearbyEntities() {
        return (short) getSpawnerBaseLogic().maxNearbyEntities;
    }

    public void mobspawner$setMaximumNearbyEntities(short count) {
        getSpawnerBaseLogic().maxNearbyEntities = count;
    }

    public short mobspawner$getRequiredPlayerRange() {
        return (short) getSpawnerBaseLogic().activatingRangeFromPlayer;
    }

    public void mobspawner$setRequiredPlayerRange(short range) {
        getSpawnerBaseLogic().activatingRangeFromPlayer = range;
    }

    public short mobspawner$getSpawnRange() {
        return (short) getSpawnerBaseLogic().spawnRange;
    }

    public void mobspawner$setSpawnRange(short range) {
        getSpawnerBaseLogic().spawnRange = range;
    }

    public void mobspawner$setNextEntityToSpawn(EntityType type, @Nullable DataContainer additionalProperties) {
        //TODO
    }

    public void mobspawner$setNextEntityToSpawn(WeightedRandomEntity entity) {
        mobspawner$setPossibleEntitiesToSpawn(new WeightedRandomEntity[] {entity});
    }

    public void mobspawner$setPossibleEntitiesToSpawn(WeightedRandomEntity... entities) {
        //TODO
    }

    public void mobspawner$setPossibleEntitiesToSpawn(Collection<WeightedRandomEntity> entities) {
        mobspawner$setPossibleEntitiesToSpawn(entities.toArray(new WeightedRandomEntity[entities.size()]));
    }

    public Collection<WeightedRandomEntity> mobspawner$getPossibleEntitiesToSpawn() {
        //TODO
        return null;
    }

    public void mobspawner$spawnEntityBatchImmediately(boolean force) {
        if (force) {
            short oldMaxNearby = (short) getSpawnerBaseLogic().maxNearbyEntities;
            getSpawnerBaseLogic().maxNearbyEntities = Short.MAX_VALUE;

            getSpawnerBaseLogic().spawnDelay = 0;
            getSpawnerBaseLogic().updateSpawner();

            getSpawnerBaseLogic().maxNearbyEntities = oldMaxNearby;
        } else {
            getSpawnerBaseLogic().spawnDelay = 0;
        }
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(new DataQuery("Delay"), this.mobspawner$getRemainingDelay());
        container.set(new DataQuery("MinimumDelay"), this.mobspawner$getMinimumSpawnDelay());
        container.set(new DataQuery("MaximumDelay"), this.mobspawner$getMaximumSpawnDelay());
        container.set(new DataQuery("SpawnCount"), this.mobspawner$getSpawnCount());
        container.set(new DataQuery("MaxNearbyEntities"), this.mobspawner$getMaximumNearbyEntities());
        container.set(new DataQuery("RequiredPlayerRange"), this.mobspawner$getRequiredPlayerRange());
        container.set(new DataQuery("SpawnRange"), this.mobspawner$getSpawnRange());
        List<DataView> views = Lists.newArrayList();
        for (WeightedRandomEntity entity : this.mobspawner$getPossibleEntitiesToSpawn()) {
            DataContainer entityContainer = new MemoryDataContainer();
            entityContainer.set(new DataQuery("EntityType"), entity.getEntityType().getId());
            entityContainer.set(new DataQuery("Weight"), entity.getWeight());
            entityContainer.set(new DataQuery("EntityData"), entity.getAdditionalProperties());
        }
        container.set(new DataQuery("WeightedEntities"), views);
        return container;
    }
}
