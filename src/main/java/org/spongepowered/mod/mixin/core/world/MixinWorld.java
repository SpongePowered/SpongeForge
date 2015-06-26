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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;

@Mixin(value = World.class, priority = 1001)
public abstract class MixinWorld implements org.spongepowered.api.world.World {

    @Shadow public WorldInfo worldInfo;
    private long weatherStartTime;

    @Inject(method = "updateWeatherBody()V", remap = false, at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThundering(Z)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRaining(Z)V")
    })
    private void onUpdateWeatherBody(CallbackInfo ci) {
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Override
    public BlockSnapshot getBlockSnapshot(Vector3i position) {
        return (BlockSnapshot) net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot((World) (Object) this, VecHelper.toBlockPos(position));
    }

    @Override
    public BlockSnapshot getBlockSnapshot(int x, int y, int z) {
        return (BlockSnapshot) net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot((World) (Object) this, new BlockPos(x, y, z));
    }

    @Override
    public void setBlockSnapshot(Vector3i position, BlockSnapshot snapshot) {
        net.minecraftforge.common.util.BlockSnapshot block = (net.minecraftforge.common.util.BlockSnapshot) snapshot;
        ((World) (Object) this).setBlockState(VecHelper.toBlockPos(position), block.getReplacedBlock(), block.flag);
    }

    @Override
    public void setBlockSnapshot(int x, int y, int z, BlockSnapshot snapshot) {
        net.minecraftforge.common.util.BlockSnapshot block = (net.minecraftforge.common.util.BlockSnapshot) snapshot;
        ((World) (Object) this).setBlockState(new BlockPos(x, y, z), block.getReplacedBlock(), block.flag);
    }

}
