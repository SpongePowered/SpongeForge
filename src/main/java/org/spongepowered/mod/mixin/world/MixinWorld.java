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
package org.spongepowered.mod.mixin.world;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.wrapper.BlockWrapper;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World {

    private boolean keepSpawnLoaded;

    @Shadow
    public WorldProvider provider;

    @Shadow
    protected WorldInfo worldInfo;

    @Shadow
    public Random rand;

    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();

    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getLoadedEntityList();
    
    @Shadow
    public abstract net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);
    
    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getEntitiesWithinAABB(Class clazz,AxisAlignedBB aabb);
    
    @Shadow
    public abstract BiomeGenBase getBiomeGenForCoords(final BlockPos pos);
    
    @Shadow
    public abstract IChunkProvider getChunkProvider();
    
    @Shadow
    public abstract net.minecraft.world.World init();
    
    @Override
    public UUID getUniqueID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return this.worldInfo.getWorldName() + "_" + this.provider.getDimensionName().toLowerCase().replace(' ', '_');
    }

    @Override
    public Optional<Chunk> getChunk(Vector3i position) {
        return Optional.fromNullable((Chunk)getChunkFromChunkCoords(position.getX(),position.getZ()));
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i position, boolean shouldGenerate) {
        if(!this.getChunkProvider().chunkExists(position.getX(), position.getY())){
            return Optional.fromNullable((Chunk)this.getChunkProvider().provideChunk(position.getX(), position.getZ()));
        }
        return Optional.absent();
    }

    @Override
    public BlockLoc getBlock(Vector3d position) {
        // TODO: MC's BlockPos does some sort of special rounding on double
        // positions -- do we want to do that too?
        return new BlockWrapper(this, (int) position.getX(), (int) position.getY(), (int) position.getZ());
    }

    @Override
    public BlockLoc getBlock(int x, int y, int z) {
        return new BlockWrapper(this, x, y, z);
    }

    public BiomeType getBiome(Vector3d position) {
        return (BiomeType) this.provider.getBiomeGenForCoords(new BlockPos(position.getX(),position.getY(),position.getZ()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities() {
        return this.getLoadedEntityList();
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        this.getChunkFromChunkCoords((int)position.getX(), (int)position.getZ())
            .addEntity(new EntityOcelot(this.init()));
        for(Field f:EntityTypes.class.getFields()){
            if(type.getId().toLowerCase().contains(f.getName().toLowerCase())){
                return Optional.fromNullable((Entity)EntityList.createEntityByName(f.getName().toLowerCase(), this.init()));
            }
        }
        return Optional.absent();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return (WorldBorder) shadow$getWorldBorder();
    }

    @Override
    public Weather getWeather() {
        if (this.worldInfo.isThundering()) {
            return Weathers.THUNDER_STORM;
        } else if (this.worldInfo.isRaining()) {
            return Weathers.RAIN;
        } else {
            return Weathers.CLEAR;
        }
    }

    @Override
    public long getRemainingDuration() {
        Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.func_176133_A() > 0) {
                return this.worldInfo.func_176133_A();
            } else {
                return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
            }
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            return this.worldInfo.getThunderTime();
        } else if (weather.equals(Weathers.RAIN)) {
            return this.worldInfo.getRainTime();
        }
        return 0;
    }

    long weatherStartTime;

    @Inject(method = "updateWeatherBody()V", remap = false, at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThundering(Z)V"),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRaining(Z)V")
    })
    private void onUpdateWeatherBody(CallbackInfo ci) {
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Override
    public void forecast(Weather weather) {
        this.forecast(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void forecast(Weather weather, long duration) {
        if (weather.equals(Weathers.CLEAR)) {
            this.worldInfo.func_176142_i((int) duration);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.RAIN)) {
            this.worldInfo.func_176142_i(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            this.worldInfo.func_176142_i(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    @Override
    public Dimension getDimension() {
        return (Dimension)this.provider;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepLoaded) {
        this.keepSpawnLoaded = keepLoaded;
    }
}
