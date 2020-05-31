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

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.Maps;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

@NonnullByDefault
@Mixin(value = GameRegistry.class, remap = false)
public class GameRegistryMixin_Forge {

    private static final Map<Class<?>, Timing> forgeImpl$worldGeneratorTimings = new IdentityHashMap<>();

    @Redirect(method = "generateWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/IWorldGenerator;generate(Ljava/util/Random;IILnet/minecraft/world/World;Lnet/minecraft/world/gen/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V",
            remap = false))
    private static void forgeImpl$startTimingOnGenerate(final IWorldGenerator worldGenerator, final Random random, final int chunkX, final int chunkZ,
        final World world, final IChunkGenerator chunkGenerator, final IChunkProvider chunkProvider) {
        Timing timing = null;
        if (Timings.isTimingsEnabled()) {
            timing = forgeImpl$worldGeneratorTimings.get(worldGenerator.getClass());
            if (timing == null) {
                final String modId = StaticMixinForgeHelper.getModIdFromClass(worldGenerator.getClass());
                timing = SpongeTimingsFactory.ofSafe("worldGenerator (" + modId + ":" + worldGenerator.getClass().getName() + ")");
                forgeImpl$worldGeneratorTimings.put(worldGenerator.getClass(), timing);
            }
        }
        try (final Timing start = timing) {
            if (start != null) {
                start.startTimingIfSync();
            }
            worldGenerator.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    @Inject(method = "generateWorld", at = @At("HEAD"))
    private static void forgeImpl$startTiming(final int chunkX, final int chunkZ, final World world, final IChunkGenerator chunkGenerator,
        final IChunkProvider chunkProvider, final CallbackInfo ci) {
        if (Timings.isTimingsEnabled()) {
            ((WorldServerBridge) world).bridge$getTimingsHandler().chunkPopulate.startTimingIfSync();
        }
    }

    @Inject(method = "generateWorld", at = @At(value = "RETURN"))
    private static void forgeImpl$StopTiming(final int chunkX, final int chunkZ, final World world, final IChunkGenerator chunkGenerator,
        final IChunkProvider chunkProvider, final CallbackInfo ci) {
        if (Timings.isTimingsEnabled()) {
            ((WorldServerBridge) world).bridge$getTimingsHandler().chunkPopulate.stopTimingIfSync();
        }
    }

}
