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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.service.persistence.data.DataContainer;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private boolean keepSpawnLoaded;
    public SpongeConfig worldConfig;

    @Shadow public WorldProvider provider;

    @Shadow protected WorldInfo worldInfo;

    @Shadow public Random rand;

    @Shadow public List<net.minecraft.entity.Entity> loadedEntityList;

    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();

    @Shadow
    public abstract boolean spawnEntityInWorld(net.minecraft.entity.Entity p_72838_1_);

    @Shadow
    public abstract List<net.minecraft.entity.Entity> func_175644_a(Class<net.minecraft.entity.Entity> p_175644_1_,
            Predicate<net.minecraft.entity.Entity> p_175644_2_);

    @Shadow
    public abstract void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch);

    @Shadow
    public abstract long getSeed();

    @Shadow
    public abstract BiomeGenBase getBiomeGenForCoords(BlockPos pos);

    @Shadow
    public abstract net.minecraft.world.chunk.Chunk getChunkFromBlockCoords(BlockPos pos);

    @Shadow
    public abstract IChunkProvider getChunkProvider();

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
        return (BiomeType) this.getBiomeGenForCoords(VecHelper.toBlockPos(position));
    }

    @Override
    public void setBiome(Vector3i position, BiomeType biome) {
        int x = position.getX() >> 4;
        int z = position.getZ() >> 4;
        IChunkProvider chunkProvider = this.getChunkProvider();
        if (!chunkProvider.chunkExists(x, z)) {
            return;
        }
        net.minecraft.world.chunk.Chunk chunk = chunkProvider.provideChunk(x, z);
        byte[] biomeArray = chunk.getBiomeArray();
        // Taken from Chunk#getBiome
        int i = position.getX() & 15;
        int j = position.getZ() & 15;
        biomeArray[j << 4 | i] = (byte) (((BiomeGenBase) biome).biomeID & 255);
        chunk.setBiomeArray(biomeArray);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities() {
        return (Collection<Entity>) (Object) this.loadedEntityList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        return (Collection<Entity>) (Object) this.func_175644_a(net.minecraft.entity.Entity.class,
                (Predicate<net.minecraft.entity.Entity>) (Object) filter);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        try {
            net.minecraft.entity.Entity entity =
                    (net.minecraft.entity.Entity) type.getEntityClass().getConstructor(net.minecraft.world.World.class).newInstance(this);
            entity.setPosition(position.getX(), position.getY(), position.getZ());
            if (this.spawnEntityInWorld(entity)) {
                return Optional.of((Entity) entity);
            }
        } catch (Exception ignored) {
            // Entity constructor does not have single World parameter, too bad
        }
        return Optional.absent();
    }

    @Override
    public Optional<Entity> createEntity(EntitySnapshot snapshot, Vector3d position) {
        return this.createEntity(snapshot.getType(), position);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        // TODO once entity containers are implemented
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

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        this.playSound(sound, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        this.playSound(sound, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        this.playSoundEffect(position.getX(), position.getY(), position.getZ(), sound.getName(), (float) Math.max(minVolume, volume), (float) pitch);
    }

    @Override
    public Optional<Entity> getEntityFromUUID(UUID uuid) {
        if ((Object) this instanceof WorldServer) {
            // TODO Should this be done in an override in a WorldServer mixin?
            return Optional.fromNullable((Entity) ((WorldServer) (Object) this).getEntityFromUuid(uuid));
        }
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        if (this.worldInfo.getGameRulesInstance().hasRule(gameRule)) {
            return Optional.of(this.worldInfo.getGameRulesInstance().getGameRuleStringValue(gameRule));
        }
        return Optional.absent();
    }

    @Override
    public void setGameRule(String gameRule, String value) {
        this.worldInfo.getGameRulesInstance().setOrCreateGameRule(gameRule, value);
    }

    @Override
    public Map<String, String> getGameRules() {
        GameRules gameRules = this.worldInfo.getGameRulesInstance();
        Map<String, String> ruleMap = new HashMap<String, String>();
        for (String rule : gameRules.getRules()) {
            ruleMap.put(rule, gameRules.getGameRuleStringValue(rule));
        }
        return ruleMap;
    }

    @Override
    public long getWorldSeed() {
        return this.getSeed();
    }

    @Override
    public void setSeed(long seed) {
        this.worldInfo.randomSeed = seed;
        this.rand.setSeed(seed);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return ((ChunkProviderServer) this.getChunkProvider()).loadedChunks;
    }

    @Override
    public boolean deleteChunk(Chunk chunk) {
        ChunkProviderServer provider = (ChunkProviderServer) this.getChunkProvider();
        Vector3i chunkPos = chunk.getPosition();
        if (ForgeChunkManager.getPersistentChunksFor((net.minecraft.world.World) (Object) this).containsKey(
                new ChunkCoordIntPair(chunkPos.getX(), chunkPos.getZ()))) {
            return false;
        }
        // TODO This method is async, how to know if successful?
        provider.dropChunk(chunkPos.getX(), chunkPos.getZ());
        return true;
    }
}
