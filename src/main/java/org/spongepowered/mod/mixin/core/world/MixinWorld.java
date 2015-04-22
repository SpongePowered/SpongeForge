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

import com.google.common.collect.ImmutableList;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.world.border.PlayerBorderListener;
import org.spongepowered.common.world.gen.CustomWorldChunkManager;
import org.spongepowered.common.world.gen.SpongeBiomeGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.world.gen.CustomChunkProviderGenerate;
import org.spongepowered.mod.world.gen.SpongeGeneratorPopulator;

import java.io.File;


@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private boolean keepSpawnLoaded;
    public SpongeConfig<SpongeConfig.WorldConfig> worldConfig;
    private volatile Context worldContext;
    private ImmutableList<Populator> populators;
    private ImmutableList<GeneratorPopulator> generatorPopulators;

    @Shadow public WorldProvider provider;
    @Shadow protected WorldInfo worldInfo;
    @Shadow private net.minecraft.world.border.WorldBorder worldBorder;

    @Shadow public abstract IChunkProvider getChunkProvider();
    @Shadow public abstract WorldChunkManager getWorldChunkManager();

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
    public int getHeight() {
        return this.provider.getActualHeight();
    }

    @Override
    public int getBuildHeight() {
        return this.provider.getHeight();
    }


}
