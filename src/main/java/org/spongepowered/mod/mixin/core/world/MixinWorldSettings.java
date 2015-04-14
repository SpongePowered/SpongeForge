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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.entity.player.gamemode.SpongeGameMode;
import org.spongepowered.mod.interfaces.IMixinWorldSettings;
import org.spongepowered.mod.world.gen.WorldGeneratorRegistry;

import java.util.Collection;

@NonnullByDefault
@Mixin(WorldSettings.class)
public class MixinWorldSettings implements WorldCreationSettings, IMixinWorldSettings {

    private DimensionType dimensionType;
    private DataContainer generatorSettings;
    private boolean worldEnabled;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private ImmutableCollection<WorldGeneratorModifier> generatorModifiers;

    @Shadow
    private long seed;

    @Shadow
    private WorldSettings.GameType theGameType;

    @Shadow
    private boolean mapFeaturesEnabled;

    @Shadow
    private boolean hardcoreEnabled;

    @Shadow
    private WorldType terrainType;

    @Shadow
    private boolean commandsAllowed;

    @Shadow
    private boolean bonusChestEnabled;

    @Shadow
    private String worldName;

    @Override
    public String getWorldName() {
        return this.worldName;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public GameMode getGameMode() {
        return new SpongeGameMode(this.theGameType.getName());
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeaturesEnabled;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcoreEnabled;
    }

    @Override
    public boolean commandsAllowed() {
        return this.commandsAllowed;
    }

    @Override
    public boolean bonusChestEnabled() {
        return this.bonusChestEnabled;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public void setDimensionType(DimensionType type) {
        this.dimensionType = type;
    }

    @Override
    public DataContainer getGeneratorSettings() {
        return this.generatorSettings;
    }

    @Override
    public void setGeneratorSettings(DataContainer settings) {
        this.generatorSettings = settings;
    }

    @Override
    public boolean isEnabled() {
        return this.worldEnabled;
    }

    @Override
    public void setEnabled(boolean isWorldEnabled) {
        this.worldEnabled = isWorldEnabled;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepSpawnLoaded) {
        this.keepSpawnLoaded = keepSpawnLoaded;
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        ImmutableList<WorldGeneratorModifier> defensiveCopy = ImmutableList.copyOf(modifiers);
        WorldGeneratorRegistry.getInstance().checkAllRegistered(defensiveCopy);
        this.generatorModifiers = defensiveCopy;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        if (this.generatorModifiers == null) {
            return ImmutableList.of();
        }
        return this.generatorModifiers;
    }
}
