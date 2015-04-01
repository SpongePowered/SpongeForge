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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.io.File;

@NonnullByDefault
@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider implements Dimension, IMixinWorldProvider {

    private boolean allowPlayerRespawns;
    private SpongeConfig<SpongeConfig.DimensionConfig> dimensionConfig;
    private volatile Context dimContext;

    @Shadow
    protected int dimensionId;
    @Shadow
    protected boolean isHellWorld;

    @Shadow
    public abstract String getDimensionName();

    @Shadow
    public abstract boolean canRespawnHere();

    @Shadow
    public abstract int getAverageGroundLevel();

    @Shadow
    public abstract boolean getHasNoSky();

    @Overwrite
    public static WorldProvider getProviderForDimension(int dimension) {
        WorldProvider provider = net.minecraftforge.common.DimensionManager.createProviderFor(dimension);
        if (!SpongeGameRegistry.dimensionConfigs.containsKey(provider.getClass())) {
            String providerName = provider.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
            SpongeConfig<SpongeConfig.DimensionConfig> dimConfig =
                    new SpongeConfig<SpongeConfig.DimensionConfig>(SpongeConfig.Type.DIMENSION, new File(SpongeMod.instance.getConfigDir()
                            + File.separator + providerName
                            + File.separator, "dimension.conf"), "sponge");
            SpongeGameRegistry.dimensionConfigs.put(provider.getClass(), dimConfig);
            ((IMixinWorldProvider) provider).setDimensionConfig(dimConfig);
        }

        Dimension dim = (Dimension) provider;
        dim.setAllowsPlayerRespawns(DimensionManager.shouldLoadSpawn(dimension));
        return provider;
    }

    @Override
    public int getDimensionId() {
        return this.dimensionId;
    }

    @Override
    public String getName() {
        return getDimensionName();
    }

    @Override
    public boolean allowsPlayerRespawns() {
        return this.allowPlayerRespawns;
    }

    @Override
    public void setAllowsPlayerRespawns(boolean allow) {
        this.allowPlayerRespawns = allow;
    }

    @Override
    public int getMinimumSpawnHeight() {
        return this.getAverageGroundLevel();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.isHellWorld;
    }

    @Override
    public void setWaterEvaporates(boolean evaporates) {
        this.isHellWorld = evaporates;
    }

    @Override
    public boolean hasSky() {
        return !getHasNoSky();
    }

    @Override
    public DimensionType getType() {
        return ((SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry()).dimensionClassMappings.get(this.getClass());
    }

    @Override
    public void setDimensionConfig(SpongeConfig<SpongeConfig.DimensionConfig> config) {
        this.dimensionConfig = config;
    }

    @Override
    public SpongeConfig<SpongeConfig.DimensionConfig> getDimensionConfig() {
        return this.dimensionConfig;
    }

    @Override
    public Context getContext() {
        if (this.dimContext == null) {
            this.dimContext = new Context(Context.DIMENSION_KEY, getName());
        }
        return this.dimContext;
    }
}
