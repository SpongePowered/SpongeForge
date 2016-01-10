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

import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldType;
import org.spongepowered.common.interfaces.world.gen.IPopulatorProvider;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.mod.world.gen.SpongeChunkProviderForge;

@Mixin(value = net.minecraft.world.World.class, priority = 1001)
public abstract class MixinWorld implements org.spongepowered.api.world.World, IMixinWorld {

    @Shadow public WorldInfo worldInfo;
    @Shadow public abstract IChunkProvider getChunkProvider();

    @Inject(method = "updateWeatherBody()V", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThundering(Z)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRaining(Z)V")
    }, require = 2)
    private void onUpdateWeatherBody(CallbackInfo ci) {
        this.setWeatherStartTime(this.worldInfo.getWorldTotalTime());
    }

    @Override
    public void updateWorldGenerator() {

        IMixinWorldType worldType = (IMixinWorldType) this.getProperties().getGeneratorType();
        // Get the default generator for the world type
        DataContainer generatorSettings = this.getProperties().getGeneratorSettings();

        SpongeWorldGenerator newGenerator = worldType.createGenerator(this, generatorSettings);
        // If the base generator is an IChunkProvider which implements
        // IPopulatorProvider we request that it add its populators not covered
        // by the base generation populator
        if (newGenerator.getBaseGenerationPopulator() instanceof IPopulatorProvider) {
            ((IPopulatorProvider) newGenerator.getBaseGenerationPopulator()).addPopulators(newGenerator);
        }

        // Re-apply all world generator modifiers
        WorldCreationSettings creationSettings = this.getCreationSettings();

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(creationSettings, generatorSettings, newGenerator);
        }

        SpongeChunkProviderForge spongegen = new SpongeChunkProviderForge((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
        spongegen.setGenerationPopulators(newGenerator.getGenerationPopulators());
        spongegen.setPopulators(newGenerator.getPopulators());
        spongegen.setBiomeOverrides(newGenerator.getBiomeSettings());
        setSpongeGenerator(spongegen);

        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) getChunkProvider();
        chunkProviderServer.serverChunkGenerator = spongegen;
    }
}
