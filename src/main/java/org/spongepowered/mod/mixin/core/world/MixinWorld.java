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
package org.spongepowered.mod.mixin.core.world;

import org.spongepowered.mod.interfaces.IMixinWorldSettings;
import net.minecraft.world.WorldSettings;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tile.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.effect.particle.SpongeParticleEffect;
import org.spongepowered.mod.effect.particle.SpongeParticleHelper;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldType;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.world.border.PlayerBorderListener;
import org.spongepowered.mod.world.gen.CustomChunkProviderGenerate;
import org.spongepowered.mod.world.gen.CustomWorldChunkManager;
import org.spongepowered.mod.world.gen.SpongeBiomeGenerator;
import org.spongepowered.mod.world.gen.SpongeGeneratorPopulator;
import org.spongepowered.mod.world.gen.SpongeWorldGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private boolean keepSpawnLoaded;
    public SpongeConfig<SpongeConfig.WorldConfig> worldConfig;
    private volatile Context worldContext;
    private ImmutableList<Populator> populators;
    private ImmutableList<GeneratorPopulator> generatorPopulators;

    @Shadow
    public WorldProvider provider;

    @Shadow
    protected WorldInfo worldInfo;

    @Shadow
    protected ISaveHandler saveHandler;

    @Shadow
    public Random rand;

    @Shadow
    public List<net.minecraft.entity.Entity> loadedEntityList;

    @Shadow
    private net.minecraft.world.border.WorldBorder worldBorder;

    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();

    @Shadow
    public abstract boolean spawnEntityInWorld(net.minecraft.entity.Entity entityIn);

    @Shadow
    public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            Predicate<net.minecraft.entity.Entity> filter);

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

    @Shadow
    public abstract WorldChunkManager getWorldChunkManager();

    @Shadow
    public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {
        if (!client) {
            String providerName = providerIn.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
            this.worldConfig =
                    new SpongeConfig<SpongeConfig.WorldConfig>(SpongeConfig.Type.WORLD, new File(SpongeMod.instance.getConfigDir()
                            + File.separator + providerName
                            + File.separator + (providerIn.getDimensionId() == 0 ? "dim0" : providerIn.getSaveFolder().toLowerCase()), "world.conf"),
                            "sponge");
        }

        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            this.worldBorder.addListener(new PlayerBorderListener());
        }
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"))
    public void onGetCollidingBoundingBoxes(net.minecraft.entity.Entity entity, net.minecraft.util.AxisAlignedBB axis,
            CallbackInfoReturnable<List> cir) {
        if (!entity.worldObj.isRemote && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            cir.setReturnValue(new ArrayList());// Removing misbehaved living entities
        }
    }

    @Override
    public UUID getUniqueId() {
        return ((WorldProperties) this.worldInfo).getUniqueId();
    }

    @Override
    public String getName() {
        return this.worldInfo.getWorldName();
    }

    @Override
    public Optional<Chunk> getChunk(Vector3i position) {
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(position.getX(), position.getZ())) {
            chunk = worldserver.theChunkProviderServer.provideChunk(position.getX(), position.getZ());
        }
        return Optional.fromNullable((Chunk) chunk);
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i position, boolean shouldGenerate) {
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(position.getX(), position.getZ()) || shouldGenerate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(position.getX(), position.getZ());
        }
        return Optional.fromNullable((Chunk) chunk);
    }

    @Override
    public BlockState getBlock(Vector3i position) {
        return getBlock(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return (BlockState) ((net.minecraft.world.World) (Object) this).getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public BlockType getBlockType(Vector3i position) {
        return getBlock(position).getType();
    }

    @Override
    public void setBlock(Vector3i position, BlockState block) {
        SpongeHooks.setBlockState(((net.minecraft.world.World) (Object) this), position, block);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        SpongeHooks.setBlockState(((net.minecraft.world.World) (Object) this), x, y, z, block);
    }

    @Override
    public Location getFullBlock(Vector3i position) {
        return new Location(this, position.toDouble());
    }

    @Override
    public Location getFullBlock(int x, int y, int z) {
        return new Location(this, new Vector3d(x, y, z));
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return getBiome(position.getX(), position.getY());
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        return (BiomeType) this.getBiomeGenForCoords(new BlockPos(x, 0, z));
    }

    @Override
    public void setBiome(Vector2i position, BiomeType biome) {
        setBiome(position.getX(), position.getY(), biome);
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        int cx = x >> 4;
        int cz = z >> 4;
        IChunkProvider chunkProvider = this.getChunkProvider();
        if (!chunkProvider.chunkExists(cx, cz)) {
            return;
        }
        net.minecraft.world.chunk.Chunk chunk = chunkProvider.provideChunk(cx, cz);
        byte[] biomeArray = chunk.getBiomeArray();
        // Taken from Chunk#getBiome
        int i = x & 15;
        int j = z & 15;
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
        return (Collection<Entity>) (Object) this.getEntities(net.minecraft.entity.Entity.class,
                (Predicate<net.minecraft.entity.Entity>) (Object) filter);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");

        Entity entity = null;

        try {
            entity = ConstructorUtils.invokeConstructor(type.getEntityClass(), this);
            entity.setLocation(entity.getLocation().setPosition(position));
        } catch (Exception e) {
            SpongeMod.instance.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return Optional.fromNullable(entity);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3i position) {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");
        Entity entity = null;

        try {
            entity = ConstructorUtils.invokeConstructor(type.getEntityClass(), this);
            entity.setLocation(entity.getLocation().setPosition(position.toDouble()));
        } catch (Exception e) {
            SpongeMod.instance.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return Optional.fromNullable(entity);
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        // TODO once entity containers are implemented
        return Optional.absent();
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");
        return spawnEntityInWorld(((net.minecraft.entity.Entity) entity));
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
                manager.sendToAllNear(x, y, z, radius, this.provider.getDimensionId(), packet);
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
            if (this.worldInfo.getCleanWeatherTime() > 0) {
                return this.worldInfo.getCleanWeatherTime();
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
            this.worldInfo.setCleanWeatherTime((int) duration);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.RAIN)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    @Override
    public Dimension getDimension() {
        return (Dimension) this.provider;
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
    public SpongeConfig<SpongeConfig.WorldConfig> getWorldConfig() {
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
    public Optional<Entity> getEntity(UUID uuid) {
        World spongeWorld = this;
        if (spongeWorld instanceof WorldServer) {
            return Optional.fromNullable((Entity) ((WorldServer) (Object) this).getEntityFromUuid(uuid));
        }
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return ((ChunkProviderServer) this.getChunkProvider()).loadedChunks;
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        if (chunk == null) {
            return false;
        }
        return chunk.unloadChunk();
    }

    @Override
    public WorldCreationSettings getCreationSettings() {
        WorldProperties properties = this.getProperties();

        // Create based on WorldProperties
        WorldSettings settings = new WorldSettings(this.worldInfo);
        IMixinWorldSettings mixin = (IMixinWorldSettings) (Object) settings;
        mixin.setDimensionType(properties.getDimensionType());
        mixin.setGeneratorSettings(properties.getGeneratorSettings());
        mixin.setGeneratorModifiers(properties.getGeneratorModifiers());
        mixin.setEnabled(true);
        mixin.setKeepSpawnLoaded(this.keepSpawnLoaded);
        mixin.setLoadOnStartup(properties.loadOnStartup());

        return (WorldCreationSettings) (Object) settings;
    }

    @Override
    public void updateWorldGenerator() {
        IMixinWorldType worldType = (IMixinWorldType) this.getProperties().getGeneratorType();

        // Get the default generator for the world type
        DataContainer generatorSettings = this.getProperties().getGeneratorSettings();
        if (generatorSettings.contains(IMixinWorldType.STRING_VALUE)) {
            String options = generatorSettings.getString(IMixinWorldType.STRING_VALUE).get();
            if (options.equals("")) {
                return;
            }
        }
        SpongeWorldGenerator newGenerator = worldType.createGenerator(this, generatorSettings);

        // Re-apply all world generator modifiers
        WorldCreationSettings creationSettings = this.getCreationSettings();

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(creationSettings, generatorSettings, newGenerator);
        }

        // Set this world generator
        this.setWorldGenerator(newGenerator);
    }

    @Override
    public void setWorldGenerator(WorldGenerator generator) {
        // Replace biome generator with possible modified one
        BiomeGenerator biomeGenerator = generator.getBiomeGenerator();
        WorldServer thisWorld = (WorldServer) (Object) this;
        thisWorld.provider.worldChunkMgr = CustomWorldChunkManager.of(biomeGenerator);

        // Replace generator populator with possibly modified one
        GeneratorPopulator generatorPopulator = generator.getBaseGeneratorPopulator();
        replaceChunkGenerator(CustomChunkProviderGenerate.of(thisWorld, generatorPopulator, biomeGenerator));

        // Replace populators with possibly modified list
        this.populators = ImmutableList.copyOf(generator.getPopulators());
        this.generatorPopulators = ImmutableList.copyOf(generator.getGeneratorPopulators());
    }

    @Override
    public ImmutableList<Populator> getPopulators() {
        if (this.populators == null) {
            this.populators = ImmutableList.of();
        }
        return this.populators;
    }

    @Override
    public ImmutableList<GeneratorPopulator> getGeneratorPopulators() {
        if (this.generatorPopulators == null) {
            this.generatorPopulators = ImmutableList.of();
        }
        return this.generatorPopulators;
    }

    private void replaceChunkGenerator(IChunkProvider provider) {
        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.getChunkProvider();
        chunkProviderServer.serverChunkGenerator = provider;
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        // We have to create a new instance every time to satisfy the contract
        // of this method, namely that changing the state of the returned
        // instance does not affect the world without setWorldGenerator being
        // called
        ChunkProviderServer serverChunkProvider = (ChunkProviderServer) this.getChunkProvider();
        WorldServer world = (WorldServer) (Object) this;
        return new SpongeWorldGenerator(
                SpongeBiomeGenerator.of(getWorldChunkManager()), 
                SpongeGeneratorPopulator.of(world, serverChunkProvider.serverChunkGenerator), 
                this.generatorPopulators, 
                this.populators);
    }

    @Override
    public void setWorldInfo(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public int getHeight() {
        return this.provider.getActualHeight();
    }

    @Override
    public int getBuildHeight() {
        return this.provider.getHeight();
    }

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.worldInfo;
    }

    @Override
    public Location getSpawnLocation() {
        return new Location(this, this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
    }

    @Override
    public Context getContext() {
        if (this.worldContext == null) {
            this.worldContext = new Context(Context.WORLD_KEY, getName());
        }
        return this.worldContext;
    }

    @Override
    public Optional<TileEntity> getTileEntity(Vector3i position) {
        return getTileEntity(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = getTileEntity(new BlockPos(x,y,z));
        if(tileEntity == null) {
            return Optional.absent();
        } else {
            return Optional.of((TileEntity)tileEntity);
        }
    }

    @Override
    public Optional<TileEntity> getTileEntity(Location blockLoc) {
        return getTileEntity(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
    }

}
