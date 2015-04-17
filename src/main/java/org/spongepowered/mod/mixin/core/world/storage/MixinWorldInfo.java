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
package org.spongepowered.mod.mixin.core.world.storage;

import org.spongepowered.api.data.MemoryDataContainer;

import static com.google.common.base.Preconditions.checkNotNull;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.entity.player.gamemode.SpongeGameMode;
import org.spongepowered.mod.interfaces.IMixinWorldInfo;
import org.spongepowered.mod.interfaces.IMixinWorldType;
import org.spongepowered.mod.service.persistence.NbtTranslator;
import org.spongepowered.mod.world.gen.WorldGeneratorRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@NonnullByDefault
@Mixin(WorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties, IMixinWorldInfo {

    private UUID uuid;
    private boolean worldEnabled;
    private DimensionType dimensionType;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private ImmutableCollection<String> generatorModifiers;
    private NBTTagCompound spongeRootLevelNbt;
    private NBTTagCompound spongeNbt;

    @Shadow
    public long randomSeed;

    @Shadow
    private net.minecraft.world.WorldType terrainType;

    @Shadow
    private String generatorOptions;

    @Shadow
    private int spawnX;

    @Shadow
    private int spawnY;

    @Shadow
    private int spawnZ;

    @Shadow
    private long totalTime;

    @Shadow
    private long worldTime;

    @Shadow
    private long lastTimePlayed;

    @Shadow
    private long sizeOnDisk;

    @Shadow
    private NBTTagCompound playerTag;

    @Shadow
    private int dimension;

    @Shadow
    private String levelName;

    @Shadow
    private int saveVersion;

    @Shadow
    private int cleanWeatherTime;

    @Shadow
    private boolean raining;

    @Shadow
    private int rainTime;

    @Shadow
    private boolean thundering;

    @Shadow
    private int thunderTime;

    @Shadow
    private WorldSettings.GameType theGameType;

    @Shadow
    private boolean mapFeaturesEnabled;

    @Shadow
    private boolean hardcore;

    @Shadow
    private boolean allowCommands;

    @Shadow
    private boolean initialized;

    @Shadow
    private EnumDifficulty difficulty;

    @Shadow
    private boolean difficultyLocked;

    @Shadow
    private double borderCenterX;

    @Shadow
    private double borderCenterZ;

    @Shadow
    private double borderSize;

    @Shadow
    private long borderSizeLerpTime;

    @Shadow
    private double borderSizeLerpTarget;

    @Shadow
    private double borderSafeZone;

    @Shadow
    private double borderDamagePerBlock;

    @Shadow
    private int borderWarningDistance;

    @Shadow
    private int borderWarningTime;

    @Shadow
    private GameRules theGameRules;

    @Shadow
    public abstract NBTTagCompound getNBTTagCompound();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(WorldSettings settings, String name, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);

        WorldCreationSettings creationSettings = (WorldCreationSettings) (Object) settings;
        this.dimensionType = creationSettings.getDimensionType();
        this.generatorModifiers = WorldGeneratorRegistry.getInstance().toIds(creationSettings.getGeneratorModifiers());
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(NBTTagCompound nbt, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
    }

    private void updateSpongeNbt() {
        if (this.levelName != null) {
            this.spongeNbt.setString("LevelName", this.levelName); // for reference
        }
        this.spongeNbt.setInteger("dimensionId", this.dimension);
        if (this.dimensionType != null) {
            this.spongeNbt.setString("dimensionType", this.dimensionType.getDimensionClass().getName());
        }
        if (this.uuid != null) {
            this.spongeNbt.setLong("uuid_most", this.uuid.getMostSignificantBits());
            this.spongeNbt.setLong("uuid_least", this.uuid.getLeastSignificantBits());
        }
        this.spongeNbt.setBoolean("enabled", this.worldEnabled);
        this.spongeNbt.setBoolean("keepSpawnLoaded", this.keepSpawnLoaded);
        this.spongeNbt.setBoolean("loadOnStartup", this.loadOnStartup);

        if (this.generatorModifiers != null) {
            NBTTagList generatorModifierNbt = new NBTTagList();
            for (String generatorModifierId : this.generatorModifiers) {
                generatorModifierNbt.appendTag(new NBTTagString(generatorModifierId));
            }
            this.spongeNbt.setTag("generatorModifiers", generatorModifierNbt);
        }
    }

    @Override
    public void readSpongeNbt(NBTTagCompound nbt) {
        this.dimension = nbt.getInteger("dimensionId");
        long uuid_most = nbt.getLong("uuid_most");
        long uuid_least = nbt.getLong("uuid_least");
        this.uuid = new UUID(uuid_most, uuid_least);
        this.worldEnabled = nbt.getBoolean("enabled");
        this.keepSpawnLoaded = nbt.getBoolean("keepSpawnLoaded");
        this.loadOnStartup = nbt.getBoolean("loadOnStartup");
        for (DimensionType type : SpongeMod.instance.getSpongeRegistry().dimensionClassMappings.values()) {
            if (type.getDimensionClass().getCanonicalName().equalsIgnoreCase(nbt.getString("dimensionType"))) {
                this.dimensionType = type;
            }
        }

        // Read generator modifiers
        NBTTagList generatorModifiersNbt = nbt.getTagList("generatorModifiers", 8);
        ImmutableList.Builder<String> ids = ImmutableList.builder();
        for (int i = 0; i < generatorModifiersNbt.tagCount(); i++) {
            ids.add(generatorModifiersNbt.getStringTagAt(i));
        }
        this.generatorModifiers = ids.build();
    }

    @Override
    public Vector3i getSpawnPosition() {
        return new Vector3i(this.spawnX, this.spawnY, this.spawnZ);
    }

    @Override
    public void setSpawnPosition(Vector3i position) {
        checkNotNull(position);
        this.spawnX = position.getX();
        this.spawnY = position.getY();
        this.spawnZ = position.getZ();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public void setGeneratorType(GeneratorType type) {
        this.terrainType = (WorldType) type;
    }

    public long worldproperties$getSeed() {
        return this.randomSeed;
    }

    @Override
    public void setSeed(long seed) {
        this.randomSeed = seed;
    }

    @Override
    public long getTotalTime() {
        return this.totalTime;
    }

    public long worldproperties$getWorldTime() {
        return this.worldTime;
    }

    @Override
    public void setWorldTime(long time) {
        this.worldTime = time;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public void setDimensionType(DimensionType type) {
        this.dimensionType = type;
    }

    public String worldproperties$getWorldName() {
        return this.levelName;
    }

    @Override
    public void setWorldName(String name) {
        this.levelName = name;
    }

    public boolean worldproperties$isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean state) {
        this.raining = state;
    }

    public int worldproperties$getRainTime() {
        return this.rainTime;
    }

    public void worldproperties$setRainTime(int time) {
        this.rainTime = time;
    }

    public boolean worldproperties$isThundering() {
        return this.thundering;
    }

    public void worldproperties$setThundering(boolean state) {
        this.thundering = state;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int time) {
        this.thunderTime = time;
    }

    @Override
    public GameMode getGameMode() {
        return new SpongeGameMode(this.theGameType.getName());
    }

    @Override
    public void setGameMode(GameMode gamemode) {
        this.theGameType = SpongeMod.instance.getSpongeRegistry().getGameType(gamemode);
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeaturesEnabled;
    }

    @Override
    public void setMapFeaturesEnabled(boolean state) {
        this.mapFeaturesEnabled = state;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }

    @Override
    public void setHardcore(boolean state) {
        this.hardcore = state;
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.allowCommands;
    }

    @Override
    public void setCommandsAllowed(boolean state) {
        this.allowCommands = state;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public Difficulty getDifficulty() {
        return (Difficulty) (Object) this.difficulty;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = (EnumDifficulty) (Object) difficulty;
    }

    @Override
    public WorldBorder getWorldBorder() {
        net.minecraft.world.border.WorldBorder border = new net.minecraft.world.border.WorldBorder();
        border.setCenter(this.borderCenterX, this.borderCenterZ);
        border.setDamageAmount(this.borderDamagePerBlock);
        border.setDamageBuffer(this.borderSafeZone);
        border.setWarningDistance(this.borderWarningDistance);
        border.setWarningTime(this.borderWarningTime);
        return (WorldBorder) border;
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        return Optional.fromNullable(this.theGameRules.getGameRuleStringValue(gameRule));
    }

    @Override
    public Map<String, String> getGameRules() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setGameRule(String gameRule, String value) {
        this.theGameRules.setOrCreateGameRule(gameRule, value);
    }

    @Override
    public void setDimensionId(int id) {
        this.dimension = id;
    }

    @Override
    public int getDimensionId() {
        return this.dimension;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public DataContainer getAdditionalProperties() {
        NBTTagCompound additionalProperties = (NBTTagCompound) this.spongeRootLevelNbt.copy();
        additionalProperties.removeTag(SpongeMod.instance.getModId());
        return NbtTranslator.getInstance().translateFrom(additionalProperties);
    }

    @Override
    public DataContainer toContainer() {
        return NbtTranslator.getInstance().translateFrom(getNBTTagCompound());
    }

    @Override
    public boolean isEnabled() {
        return this.worldEnabled;
    }

    @Override
    public void setEnabled(boolean state) {
        this.worldEnabled = state;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        this.loadOnStartup = state;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean state) {
        this.keepSpawnLoaded = state;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        if (this.generatorModifiers == null) {
            return ImmutableList.of();
        }
        return WorldGeneratorRegistry.getInstance().toModifiers(this.generatorModifiers);
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        Preconditions.checkNotNull(modifiers, "modifiers");

        this.generatorModifiers = WorldGeneratorRegistry.getInstance().toIds(modifiers);
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft uses a String, we want to return a fancy DataContainer

        // Parse the world generator settings as JSON
        try {
            NBTTagCompound nbt = JsonToNBT.getTagFromJson(this.generatorOptions);
            return NbtTranslator.getInstance().translateFrom(nbt);
        } catch (NBTException e) {
        }

        // Else return container with one single value
        MemoryDataContainer container = new MemoryDataContainer();
        container.set(IMixinWorldType.STRING_VALUE, this.generatorOptions);
        return container;
    }

    @Override
    public Optional<DataView> getPropertySection(DataQuery path) {
        if (this.spongeRootLevelNbt.hasKey(path.toString())) {
            return Optional.<DataView> of(NbtTranslator.getInstance().translateFrom(this.spongeRootLevelNbt.getCompoundTag(path.toString())));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void setPropertySection(DataQuery path, DataView data) {
        NBTTagCompound nbt = NbtTranslator.getInstance().translateData(data);
        this.spongeRootLevelNbt.setTag(path.toString(), nbt);
    }

    @Override
    public NBTTagCompound getSpongeRootLevelNbt() {
        updateSpongeNbt();
        return this.spongeRootLevelNbt;
    }

    @Override
    public NBTTagCompound getSpongeNbt() {
        updateSpongeNbt();
        return this.spongeNbt;
    }

    @Override
    public void setSpongeRootLevelNBT(NBTTagCompound nbt) {
        this.spongeRootLevelNbt = nbt;
        if (nbt.hasKey(SpongeMod.instance.getModId())) {
            this.spongeNbt = nbt.getCompoundTag(SpongeMod.instance.getModId());
        } else {
            this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
        }
    }
}
