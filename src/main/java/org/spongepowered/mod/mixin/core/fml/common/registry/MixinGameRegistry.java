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
package org.spongepowered.mod.mixin.core.fml.common.registry;

import co.aikar.timings.Timings;
import com.flowpowered.math.vector.Vector2i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Random;

@NonnullByDefault
@Mixin(value = GameRegistry.class, remap = false)
public class MixinGameRegistry {

    @Redirect(method = "generateWorld", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/IWorldGenerator;generate(Ljava/util/Random;IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V"))
    private static void onWorldgeneratorGenerate(IWorldGenerator worldGenerator, Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (world instanceof WorldServer) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) world;
            if (Timings.isTimingsEnabled()) {
                mixinWorldServer.getTimingsHandler().chunkPopulate.startTimingIfSync();
            }
            mixinWorldServer.getCauseTracker().switchToPhase(TrackingPhases.WORLD, WorldPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .add(NamedCause.source(generator))
                    .add(NamedCause.of(InternalNamedCauses.WorldGeneration.CHUNK_PROVIDER, provider))
                    .add(NamedCause.of("ChunkPos", new Vector2i(chunkX, chunkZ)))
                    .addCaptures()
                    .complete());
        }
        worldGenerator.generate(random, chunkX, chunkZ, world, generator, provider);
        if (world instanceof WorldServer) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) world;
            mixinWorldServer.getCauseTracker().completePhase();
            if (Timings.isTimingsEnabled()) {
                mixinWorldServer.getTimingsHandler().chunkPopulate.stopTimingIfSync();
            }
        }
    }

}