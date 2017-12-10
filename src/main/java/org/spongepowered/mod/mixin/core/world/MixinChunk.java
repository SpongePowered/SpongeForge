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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.IMixinChunk;

@NonnullByDefault
@Mixin(value = net.minecraft.world.chunk.Chunk.class, priority = 1001)
public abstract class MixinChunk implements Chunk, IMixinChunk {

    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow public boolean unloadQueued;

    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(int x, int y, int z);
    @Shadow public abstract int getTopFilledSegment();

    @Redirect(method = "onLoad", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", remap = false))
    public boolean onLoadForgeEvent(net.minecraftforge.fml.common.eventhandler.EventBus eventBus, net.minecraftforge.fml.common.eventhandler.Event event) {
        // This event is handled in SpongeForgeEventFactory
        return false;
    }

    @Redirect(method = "onUnload", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", remap = false))
    public boolean onUnloadForgeEvent(net.minecraftforge.fml.common.eventhandler.EventBus eventBus, net.minecraftforge.fml.common.eventhandler.Event event) {
        // This event is handled in SpongeForgeEventFactory
        return false;
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    public void onLoadInject(CallbackInfo ci) {
        if (!this.world.isRemote) {
            for (ChunkPos forced : this.world.getPersistentChunks().keySet()) {
                if (forced.x == this.x && forced.z == this.z) {
                    this.setPersistedChunk(true);
                    return;
                }
            }
            this.setPersistedChunk(false);
        }
    }

    @Inject(method = "onUnload", at = @At("RETURN"))
    public void onUnloadInject(CallbackInfo ci) {
        // Moved from ChunkProviderServer
        net.minecraftforge.common.ForgeChunkManager.putDormantChunk(ChunkPos.asLong(this.x, this.z), (net.minecraft.world.chunk.Chunk) (Object) this);
    }

    @Override
    public boolean unloadChunk() {
        if (this.isPersistedChunk()) {
            return false;
        }

        // TODO 1.9 Update - Zidane's thing
        if (this.world.provider.canRespawnHere()) {//&& DimensionManager.shouldLoadSpawn(this.world.provider.getDimension())) {
            if (this.world.isSpawnChunk(this.x, this.z)) {
                return false;
            }
        }
        ((WorldServer) this.world).getChunkProvider().queueUnload((net.minecraft.world.chunk.Chunk) (Object) this);
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "markLoaded", at = @At("RETURN"))
    public void onSetChunkLoaded(boolean loaded, CallbackInfo ci) {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction direction : directions) {
            Vector3i neighborPosition = this.getPosition().add(direction.asBlockOffset());
            net.minecraft.world.chunk.Chunk neighbor = this.world.getChunkProvider().getLoadedChunk
                    (neighborPosition.getX(), neighborPosition.getZ());
            if (neighbor != null) {
                this.setNeighbor(direction, (Chunk) neighbor);
                ((IMixinChunk) neighbor).setNeighbor(direction.getOpposite(), this);
            }
        }
    }


    /**
     * @author gabizou - July 25th, 2016
     * @reason - Adds ignorance to blocks who do not perform any
     * location checks for their light values based on location
     *
     * @param x The x position
     * @param z The y position
     * @return whatever vanilla said
     */
    @Overwrite
    private boolean checkLight(int x, int z) {
        int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockLightOpacity(blockpos$mutableblockpos);

            if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel()) {
                flag1 = true;
            }

            if (!flag && k > 0) {
                flag = true;
            } else if (flag && k == 0 && !this.world.checkLight(blockpos$mutableblockpos)) {
                return false;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > 0; --l) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            // Sponge Start - Use SpongeImplHooks for forge optimization
            // if (this.getBlockState(blockpos$mutableblockpos).getLightValue() > 0) // Vanilla
            // if (this.getBlockState(blockpos$mutableblockpos).getLightValue(this.worldObj, blockpos$mutableblockpos) > 0) // Forge
            if (SpongeImplHooks.getChunkPosLight(this.getBlockState(blockpos$mutableblockpos), this.world, blockpos$mutableblockpos) > 0) {
                // Sponge End
                this.world.checkLight(blockpos$mutableblockpos);
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private static int getChunkBlockLightOpacity(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos pos) {
        return blockState.getLightOpacity();
    }

    @SuppressWarnings("deprecation")
    private static int getChunkBlockLightOpacity(IBlockState state, net.minecraft.world.World worldObj, int x, int y, int z) {
        return state.getLightOpacity();
    }

    /**
     * @author gabizou - July 25th, 2016
     * @reason - Adds ignorance to blocks who do not perform any
     * location checks for their light values based on location
     *
     * @param pos The block position
     * @return whatever vanilla said
     */
    @Overwrite
    public int getBlockLightOpacity(BlockPos pos) {
        // Sponge Start - Rewrite to use SpongeImplHooks
        // return this.getBlockState(pos).getLightOpacity(); // Vanilla
        // return this.getBlockState(pos).getLightOpacity(this.worldObj, pos); // Forge
        return getChunkBlockLightOpacity(this.getBlockState(pos), this.world, pos);
    }

    /**
     * @author gabizou - July 25th, 2016
     * @reason - Adds ignorance to blocks who do not perform any
     * location checks for their light values based on location
     *
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return whatever vanilla said
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    private int getBlockLightOpacity(int x, int y, int z) {
        IBlockState state = this.getBlockState(x, y, z); //Forge: Can sometimes be called before we are added to the global world list. So use the less accurate one during that. It'll be recalculated later
        // Sponge Start - Rewrite to use SpongeImplHooks because, again, unecessary block state retrieval.
        // return this.getBlockState(x, y, z).getLightOpacity(); // Vanilla
        // return this.unloaded ? state.getLightOpacity() : state.getLightOpacity(this.worldObj, new BlockPos(x, y, z)); // Forge
        return this.unloadQueued ? state.getLightOpacity() : getChunkBlockLightOpacity(state, this.world, x, y, z);
        // Sponge End
    }

}
