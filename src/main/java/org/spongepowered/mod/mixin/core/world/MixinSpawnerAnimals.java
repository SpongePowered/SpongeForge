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

import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.IMixinWorld;

import java.util.Random;

@Mixin(SpawnerAnimals.class)
public abstract class MixinSpawnerAnimals {

    @Inject(method = "findChunksForSpawning", at = @At(value = "HEAD"))
    public void onFindChunksForSpawningHead(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnedOnSetTickRate, CallbackInfoReturnable<Integer> ci) {
        ((IMixinWorld) worldServer).setWorldSpawnerRunning(true);
        ((IMixinWorld) worldServer).setProcessingCaptureCause(true);
    }

    @Inject(method = "findChunksForSpawning", at = @At(value = "RETURN"))
    public void onFindChunksForSpawningReturn(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnedOnSetTickRate, CallbackInfoReturnable<Integer> ci) {
        ((IMixinWorld) worldServer).handlePostTickCaptures(Cause.of(worldServer));
        ((IMixinWorld) worldServer).setWorldSpawnerRunning(false);
        ((IMixinWorld) worldServer).setProcessingCaptureCause(false);
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "HEAD"))
    private static void onPerformWorldGenSpawningHead(World worldServer, BiomeGenBase biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        ((IMixinWorld) worldServer).setChunkSpawnerRunning(true);
        ((IMixinWorld) worldServer).setProcessingCaptureCause(true);
    }

    @Inject(method = "performWorldGenSpawning", at = @At(value = "RETURN"))
    private static void onPerformWorldGenSpawningReturn(World worldServer, BiomeGenBase biome, int j, int k, int l, int m, Random rand, CallbackInfo ci) {
        ((IMixinWorld) worldServer).handlePostTickCaptures(Cause.of(worldServer, biome));
        ((IMixinWorld) worldServer).setChunkSpawnerRunning(false);
        ((IMixinWorld) worldServer).setProcessingCaptureCause(true);
    }
}
