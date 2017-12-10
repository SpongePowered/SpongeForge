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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.world.IMixinWorld;

// Use lower priority so it is applied before the changes in SpongeCommon
@Mixin(value = World.class, priority = 999)
public abstract class MixinWorld implements IMixinWorld {


    private boolean callingWorldEvent = false;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public boolean isRemote;
    @Shadow protected MapStorage mapStorage;

    @Shadow public abstract IChunkProvider getChunkProvider();
    @Shadow public abstract boolean canSeeSky(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract int getLightFor(EnumSkyBlock type, BlockPos pos);

    /**
     * @author gabizou - July 25th, 2016
     * @reason Optimizes several blockstate lookups for getting raw light.
     *
     * @param pos The position to get the light for
     * @param lightType The light type
     * @return The raw light
     */
    @Overwrite
    private int getRawLight(BlockPos pos, EnumSkyBlock lightType) {
        if (lightType == EnumSkyBlock.SKY && this.canSeeSky(pos)) {
            return 15;
        } else {
            // Sponge Start - Optimize block light checks
            IBlockState blockState = this.getBlockState(pos);
            int blockLight = SpongeImplHooks.getChunkPosLight(blockState, (net.minecraft.world.World) (Object) this, pos);
            int i = lightType == EnumSkyBlock.SKY ? 0 : blockLight; // Changed by forge to use the local variable
            int j = SpongeImplHooks.getBlockLightOpacity(blockState, (net.minecraft.world.World) (Object) this, pos);
            // Sponge End

            if (j >= 15 && blockLight > 0) {
                j = 1;
            }

            if (j < 1) {
                j = 1;
            }

            if (j >= 15) {
                return 0;
            } else if (i >= 14) {
                return i;
            } else {
                for (EnumFacing enumfacing : EnumFacing.values()) {
                    BlockPos blockpos = pos.offset(enumfacing);
                    int k = this.getLightFor(lightType, blockpos) - j;

                    if (k > i) {
                        i = k;
                    }

                    if (i >= 14) {
                        return i;
                    }
                }

                return i;
            }
        }
    }

    @Inject(method = "getWorldInfo", at = @At("HEAD"), cancellable = true)
    public void onGetWorldInfo(CallbackInfoReturnable<WorldInfo> cir) {
        if (this.provider.getDimension() != 0 && this.callingWorldEvent) {
            cir.setReturnValue(DimensionManager.getWorld(0).getWorldInfo());
        }
    }

    @Inject(method = "getMapStorage", at = @At("HEAD"), cancellable = true)
    public void onGetMapStorage(CallbackInfoReturnable<MapStorage> cir)
    {
        // Forge only uses a single save handler so we need to always pass overworld's mapstorage here
        if (!this.isRemote && (this.mapStorage == null || this.provider.getDimension() != 0)) {
            WorldServer overworld = DimensionManager.getWorld(0);
            if (overworld != null) {
                cir.setReturnValue(overworld.getMapStorage());
            }
        }
    }

    @Override
    public void setCallingWorldEvent(boolean flag) {
        this.callingWorldEvent = flag;
    }
}
