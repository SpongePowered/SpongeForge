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
package org.spongepowered.mod.mixin.core.world.chunk.storage;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.mod.interfaces.IMixinChunk;

import java.util.Map;

@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {

    @Inject(method = "writeChunkToNBT", at = @At(value = "RETURN"))
    public void onWriteChunkToNBT(net.minecraft.world.chunk.Chunk chunkIn, World worldIn, NBTTagCompound compound, CallbackInfo ci) {
        IMixinChunk chunk = (IMixinChunk) chunkIn;

        // Add tracked block positions
        if (chunk.getTrackedShortPlayerPositions().size() > 0 || chunk.getTrackedIntPlayerPositions().size() > 0) {
            NBTTagCompound trackedNbt = new NBTTagCompound();
            NBTTagList positions = new NBTTagList();
            trackedNbt.setTag(NbtDataUtil.SPONGE_BLOCK_POS_TABLE, positions);
            compound.setTag(NbtDataUtil.SPONGE_DATA, trackedNbt);

            for (Map.Entry<Short, Integer> mapEntry : chunk.getTrackedShortPlayerPositions().entrySet()) {
                Short pos = mapEntry.getKey();
                Integer uuidIndex = mapEntry.getValue();
                NBTTagCompound valueNbt = new NBTTagCompound();
                valueNbt.setInteger("uuid", uuidIndex);
                valueNbt.setShort("pos", pos);
                positions.appendTag(valueNbt);
            }

            for (Map.Entry<Integer, Integer> mapEntry : chunk.getTrackedIntPlayerPositions().entrySet()) {
                Integer pos = mapEntry.getKey();
                Integer uuidIndex = mapEntry.getValue();
                NBTTagCompound valueNbt = new NBTTagCompound();
                valueNbt.setInteger("uuid", uuidIndex);
                valueNbt.setInteger("ipos", pos);
                positions.appendTag(valueNbt);
            }
        }
    }

    @Inject(method = "readChunkFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getIntArray(Ljava/lang/String;)[I", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onReadChunkFromNBT(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<net.minecraft.world.chunk.Chunk> ci, int chunkX, int chunkZ, net.minecraft.world.chunk.Chunk chunkIn) {
        if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
            Map<Integer, Integer> trackedIntPlayerPositions = Maps.newHashMap();
            Map<Short, Integer> trackedShortPlayerPositions = Maps.newHashMap();
            NBTTagList positions = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA).getTagList(NbtDataUtil.SPONGE_BLOCK_POS_TABLE, 10);
            IMixinChunk chunk = (IMixinChunk) chunkIn;
            for (int i = 0; i < positions.tagCount(); i++) {
                NBTTagCompound valueNbt = positions.getCompoundTagAt(i);
                if (valueNbt.hasKey("pos")) {
                    trackedShortPlayerPositions.put(valueNbt.getShort("pos"), valueNbt.getInteger("uuid"));
                } else {
                    trackedIntPlayerPositions.put(valueNbt.getInteger("ipos"), valueNbt.getInteger("uuid"));
                }
            }
            chunk.setTrackedIntPlayerPositions(trackedIntPlayerPositions);
            chunk.setTrackedShortPlayerPositions(trackedShortPlayerPositions);
        }
    }
}
