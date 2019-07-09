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
package org.spongepowered.mod.mixin.api.minecraft.world.chunk;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

@Mixin(Chunk.class)
public abstract class ChunkMixin_ForgeAPI implements org.spongepowered.api.world.Chunk {

    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;

    @Override
    public boolean unloadChunk() {
        if (((ChunkBridge) this).bridge$isPersistedChunk()) {
            return false;
        }

        // TODO 1.9 Update - Zidane's thing
        if (this.world.provider.canRespawnHere()) {//&& DimensionManager.shouldLoadSpawn(this.world.provider.getDimension())) {
            if (this.world.isSpawnChunk(this.x, this.z)) {
                return false;
            }
        }
        ((WorldServer) this.world).getChunkProvider().queueUnload((Chunk) (Object) this);
        return true;
    }


}
