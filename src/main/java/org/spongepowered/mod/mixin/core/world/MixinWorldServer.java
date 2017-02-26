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

import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.mixin.core.world.MixinWorld;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.mod.world.gen.SpongeChunkGeneratorForge;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(value = WorldServer.class, priority = 1001)
public abstract class MixinWorldServer extends MixinWorld implements World, IMixinWorldServer {

    @Shadow public abstract ChunkProviderServer getChunkProvider();

    @Override
    public Integer getDimensionId() {
        return this.provider.getDimension();
    }

    @Override
    public SpongeChunkGenerator createChunkGenerator(SpongeWorldGenerator newGenerator) {
        return new SpongeChunkGeneratorForge((net.minecraft.world.World) (Object) this, newGenerator.getBaseGenerationPopulator(),
                newGenerator.getBiomeGenerator());
    }

    @Override
    public CompletableFuture<Optional<Chunk>> loadChunkAsync(int cx, int cy, int cz, boolean shouldGenerate) {
        // Currently, we can only load asynchronously if the chunk should not be generated
        if (shouldGenerate) {
            return org.spongepowered.api.world.World.super.loadChunkAsync(cx, cy, cz, true);
        }

        if (!SpongeChunkLayout.instance.isValidChunk(cx, cy, cz)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        CompletableFuture<Optional<Chunk>> future = new CompletableFuture<>();
        getChunkProvider().loadChunk(cx, cz, () -> future.complete(Optional.ofNullable((Chunk) getChunkProvider().getLoadedChunk(cx, cz))));
        return future;
    }

}
