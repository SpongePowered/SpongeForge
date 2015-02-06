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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.effect.particle.SpongeParticleEffect;
import org.spongepowered.mod.effect.particle.SpongeParticleHelper;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.util.VecHelper;
import org.spongepowered.mod.wrapper.BlockWrapper;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private boolean keepSpawnLoaded;
    public SpongeConfig worldConfig;

    @Shadow
    public WorldProvider provider;

    @Shadow
    protected WorldInfo worldInfo;

    @Shadow
    public Random rand;

    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client, CallbackInfo ci) {
        if (!client) {
            this.worldConfig = new SpongeConfig(SpongeConfig.Type.WORLD, providerIn.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "") + File.separator + (providerIn.getDimensionId() == 0 ? "dim0" : providerIn.getSaveFolder().toLowerCase()) + File.separator + "world.cfg");
        }
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"))
    public void onGetCollidingBoundingBoxes(net.minecraft.entity.Entity entity, net.minecraft.util.AxisAlignedBB axis, CallbackInfoReturnable<List> cir) {
        if (!entity.worldObj.isRemote && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            cir.setReturnValue(new ArrayList());// Removing misbehaved living entities
        }
    }

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
        WorldServer worldserver = (WorldServer)(Object)this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(position.getX(), position.getZ())) {
            chunk = worldserver.theChunkProviderServer.provideChunk(position.getX(), position.getZ());
        }
        return Optional.fromNullable((Chunk)chunk);
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i position, boolean shouldGenerate) {
        WorldServer worldserver = (WorldServer)(Object)this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(position.getX(), position.getZ()) || shouldGenerate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(position.getX(), position.getZ());
        }
        return Optional.fromNullable((Chunk)chunk);
    }

    @Override
    public BlockLoc getBlock(Vector3d position) {
        return new BlockWrapper(this, VecHelper.toBlockPos(position));
    }

    @Override
    public BlockLoc getBlock(int x, int y, int z) {
        return new BlockWrapper(this, x, y, z);
    }

    @Override
    public BiomeType getBiome(Vector3i position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBiome(Vector3i position, BiomeType biome) {
        // TODO
    }

    @Override
    public Collection<Entity> getEntities() {
        return new ArrayList<Entity>();
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        return Optional.absent();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return (WorldBorder) shadow$getWorldBorder();
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet packet : packets) {
                manager.sendToAllNear(x, y, z, (double) radius, this.provider.getDimensionId(), packet);
            }
        }
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

    @Override
    public SpongeConfig getWorldConfig() {
        return this.worldConfig;
    }
}
