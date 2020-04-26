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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.mod.bridge.world.WorldBridge_Forge;
import org.spongepowered.mod.event.CapturedSnapshotWrapperList;

import javax.annotation.Nullable;

// Use lower priority so it is applied before the changes in SpongeCommon
@Mixin(value = World.class, priority = 999)
public abstract class WorldMixin_Forge implements WorldBridge_Forge {

    private WorldInfo forgeImpl$redirectWorldInfo;

    @Shadow(remap = false) public java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot> capturedBlockSnapshots;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public boolean isRemote;
    @Shadow protected MapStorage mapStorage;
    @Shadow public abstract boolean canSeeSky(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract int getLightFor(EnumSkyBlock type, BlockPos pos);
    @Shadow public abstract void updateComparatorOutputLevel(BlockPos pos, Block blockIn);
    @Shadow public boolean isBlockModifiable(final EntityPlayer player, final BlockPos pos) { return false; } // Shadow

    @Shadow protected WorldInfo worldInfo;

    @Shadow public abstract long getTotalWorldTime();

    /**
     * @author gabizou - July 25th, 2016
     * @reason Optimizes several blockstate lookups for getting raw light.
     *
     * @param pos The position to get the light for
     * @param lightType The light type
     * @return The raw light
     */
    @Overwrite
    private int getRawLight(final BlockPos pos, final EnumSkyBlock lightType) {
        if (lightType == EnumSkyBlock.SKY && this.canSeeSky(pos)) {
            return 15;
        } else {
            // Sponge Start - Optimize block light checks
            final IBlockState blockState = this.getBlockState(pos);
            final int blockLight = SpongeImplHooks.getChunkPosLight(blockState, (net.minecraft.world.World) (Object) this, pos);
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
                for (final EnumFacing enumfacing : EnumFacing.values()) {
                    final BlockPos blockpos = pos.offset(enumfacing);
                    final int k = this.getLightFor(lightType, blockpos) - j;

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

    @Redirect(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            // Forge adds the method change from isBlockLoaded(BlockPos) to isBlockLoaded(BlockPos,boolean)....
            target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;Z)Z"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;isInvalid()Z", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z")
        )
    )
    private boolean forgeImpl$useTileActiveChunk(final World world, final BlockPos pos, final boolean allowEmpty) {
        return true; // If we got to here, we already have the method `bridge$shouldTick()` passing
    }

    /**
     * @author gabizou - March 1st, 2019 - 1.12.2
     * @reason Forge adds the comparator output update to notify neighboring
     * blocks, and when Sponge is performing block restores, this needs to be
     * ignored when Sponge performs the restore. To be overridden in the mod
     * equivalent to MixinWorldServer.
     *
     * @param world This world
     * @param pos The position of the tile being removed
     * @param blockIn The block type of the tile entity being removed
     */
    @Redirect(method = "removeTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateComparatorOutputLevel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    protected void forgeImpl$UseComparatorOutputLevel(final World world, final BlockPos pos, final Block blockIn, final BlockPos samePos) {
        this.updateComparatorOutputLevel(pos, blockIn);
    }

    @Inject(method = "getWorldInfo", at = @At("HEAD"), cancellable = true)
    private void forgeImpl$getRedirectedWorldInfoIfAvailable(final CallbackInfoReturnable<WorldInfo> cir) {
        if (this.provider.getDimension() != 0 && this.forgeImpl$redirectWorldInfo != null) {
            cir.setReturnValue(this.forgeImpl$redirectWorldInfo);
        }
    }

    @Inject(method = "getMapStorage", at = @At("HEAD"), cancellable = true)
    private void forgeImpl$getOverworldMapStorageInsteadOfMultiDimension(final CallbackInfoReturnable<MapStorage> cir)
    {
        // Forge only uses a single save handler so we need to always pass overworld's mapstorage here
        if (!this.isRemote && (this.mapStorage == null || this.provider.getDimension() != 0)) {
            final WorldServer overworld = DimensionManager.getWorld(0);
            if (overworld != null) {
                cir.setReturnValue(overworld.getMapStorage());
            }
        }
    }

    @Override
    public void forgeBridge$setRedirectedWorldInfo(@Nullable final WorldInfo info) {
        this.forgeImpl$redirectWorldInfo = info;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onIniitToSetForgeList(final ISaveHandler saveHandlerIn, final WorldInfo info, final WorldProvider providerIn,
        final Profiler profilerIn, final boolean client, final CallbackInfo ci) {
        if (!((WorldBridge) this).bridge$isFake()) {
            this.capturedBlockSnapshots = new CapturedSnapshotWrapperList((World) (Object) this);
        }
    }


    @ModifyArg(method = "updateWeatherBody", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V"))
    int vanillaImpl$updateRainTimeStart(final int newRainTime) {
        return newRainTime;
    }

    @ModifyArg(method = "updateWeatherBody", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V"))
    int vanillaImpl$updateThunderTimeStart(final int newThunderTime) {
        return newThunderTime;
    }

}
