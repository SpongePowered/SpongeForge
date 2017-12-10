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
package org.spongepowered.mod.mixin.core.world.biome;

import net.minecraft.world.biome.BiomeDecorator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BiomeDecorator.class)
public class MixinBiomeDecorator {

    // TODO - gabizou - evaluate whether this is still needed and whether we should be preventing chunk loads
//    @Inject(method = "decorate", at = @At("HEAD"))
//    protected void onBiomeDecorateHead(World worldIn, Random random, Biome biome, BlockPos pos, CallbackInfo ci) {
//        if (!worldIn.isRemote) {
//            WorldServer world = (WorldServer) worldIn;
//            // don't allow chunks to load while decorating
//            world.getChunkProvider().chunkLoadOverride = false;
//        }
//    }
//
//    @Inject(method = "decorate", at = @At("RETURN"))
//    protected void onBiomeDecorateReturn(World worldIn, Random random, Biome biome, BlockPos pos, CallbackInfo ci) {
//        if (!worldIn.isRemote) {
//            WorldServer world = (WorldServer) worldIn;
//            // decorate is finished, allow chunks to load
//            world.theChunkProviderServer.chunkLoadOverride = true;
//        }
//    }
}
