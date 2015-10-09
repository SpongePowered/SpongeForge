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

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.user.UserStorage;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.mod.interfaces.IMixinChunk;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk, IMixinChunk {

    private ChunkCoordIntPair chunkCoordIntPair;
    public Map<Integer, Integer> trackedIntBlockPositions = Maps.newHashMap();
    public Map<Short, Integer> trackedShortBlockPositions = Maps.newHashMap();
    private Cause populateCause;

    private final int NUM_XZ_BITS = 4;
    private final int NUM_SHORT_Y_BITS = 8;
    private final int NUM_INT_Y_BITS = 24;
    private final int Y_SHIFT = NUM_XZ_BITS;
    private final int Z_SHORT_SHIFT = Y_SHIFT + NUM_SHORT_Y_BITS;
    private final int Z_INT_SHIFT = Y_SHIFT + NUM_INT_Y_BITS;
    private final short XZ_MASK = 0xF;
    private final short Y_SHORT_MASK = 0xFF;
    private final int Y_INT_MASK = 0xFFFFFF;

    @Shadow private net.minecraft.world.World worldObj;
    @Shadow public int xPosition;
    @Shadow public int zPosition;
    @Shadow private boolean isModified;
    @Shadow private ExtendedBlockStorage[] storageArrays;
    @Shadow private int[] precipitationHeightMap;
    @Shadow private int[] heightMap;

    @Shadow
    public abstract IBlockState getBlockState(final BlockPos pos);

    @Shadow
    public abstract TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_);

    @Shadow
    public abstract void generateSkylightMap();

    @Shadow
    protected abstract void relightBlock(int x, int y, int z);

    @Shadow
    public abstract int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos);

    @Shadow
    protected abstract void propagateSkylightOcclusion(int x, int z);

    @Override
    public boolean unloadChunk() {
        if (ForgeChunkManager.getPersistentChunksFor(this.worldObj).containsKey(this.chunkCoordIntPair)) {
            return false;
        }

        if (this.worldObj.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(this.worldObj.provider.getDimensionId())) {
            if (this.worldObj.isSpawnChunk(this.xPosition, this.zPosition)) {
                return false;
            }
        }
        ((WorldServer) this.worldObj).theChunkProviderServer.dropChunk(this.xPosition, this.zPosition);
        return true;
    }

    @Override
    public Cause getCurrentPopulateCause() {
        return this.populateCause;
    }

    @Redirect(method = "populateChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;populate(Lnet/minecraft/world/chunk/IChunkProvider;II)V") )
    public void onPopulateChunkPre(IChunkProvider chunkProviderServer, IChunkProvider chunkProvider, int x, int z) {
        IMixinWorld world = (IMixinWorld) this.worldObj;
        world.setProcessingCaptureCause(true);
        world.setCapturingTerrainGen(true);
        this.populateCause = Cause.of(this, chunkProvider);
        chunkProviderServer.populate(chunkProvider, x, z);
        world.handlePostTickCaptures(this.populateCause);
        world.setCapturingTerrainGen(false);
        world.setProcessingCaptureCause(false);
        this.populateCause = null;
    }

    @Overwrite
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        IBlockState iblockstate1 = this.getBlockState(pos);

        if (iblockstate1 == state) {
            return null;
        }
        // Sponge - reroute to new method that accepts snapshot to prevent a second snapshot from being created.
        return setBlockState(pos, state, iblockstate1, null);
    }

    @Override
    public IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, BlockSnapshot newBlockSnapshot) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }

        int i1 = this.heightMap[l];

        // Sponge - remove blockstate check as we handle it in world.setBlockState
        Block block = newState.getBlock();
        Block block1 = currentState.getBlock();
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
        boolean flag = false;

        if (extendedblockstorage == null) {
            if (block == Blocks.air) {
                return null;
            }

            extendedblockstorage = this.storageArrays[j >> 4] = new ExtendedBlockStorage(j >> 4 << 4, !this.worldObj.provider.getHasNoSky());
            flag = j >= i1;
        }

        int j1 = block.getLightOpacity(this.worldObj, pos);

        extendedblockstorage.set(i, j & 15, k, newState);

        // if (block1 != block)
        {
            if (!this.worldObj.isRemote) {
                // Only fire block breaks when the block changes.
                if (currentState.getBlock() != newState.getBlock())
                    block1.breakBlock(this.worldObj, pos, currentState);
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.worldObj, pos, currentState, newState))
                    this.worldObj.removeTileEntity(pos);
            } else if (block1.hasTileEntity(currentState)) {
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && te.shouldRefresh(this.worldObj, pos, currentState, newState))
                    this.worldObj.removeTileEntity(pos);
            }
        }

        if (extendedblockstorage.getBlockByExtId(i, j & 15, k) != block) {
            return null;
        } else {
            if (flag) {
                this.generateSkylightMap();
            } else {
                int k1 = block.getLightOpacity(this.worldObj, pos);

                if (j1 > 0) {
                    if (j >= i1) {
                        this.relightBlock(i, j + 1, k);
                    }
                } else if (j == i1 - 1) {
                    this.relightBlock(i, j, k);
                }

                if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                    this.propagateSkylightOcclusion(i, k);
                }
            }

            TileEntity tileentity;

            if (!this.worldObj.isRemote && block1 != block) {
                // Sponge start - Ignore block activations during block placement captures unless it's
                // a BlockContainer. Prevents blocks such as TNT from activating when
                // cancelled.
                if (this.worldObj.captureBlockSnapshots != true || block instanceof BlockContainer) {
                    if (newBlockSnapshot == null) {
                        block.onBlockAdded(this.worldObj, pos, newState);
                    }
                }
                // Sponge end
            }

            if (block.hasTileEntity(newState)) {
                tileentity = this.getTileEntity(pos, EnumCreateEntityType.CHECK);

                if (tileentity == null) {
                    tileentity = block.createTileEntity(this.worldObj, newState);
                    this.worldObj.setTileEntity(pos, tileentity);
                }

                if (tileentity != null) {
                    tileentity.updateContainingBlockInfo();
                }
            }

            this.isModified = true;
            return currentState;
        }
    }

    @Override
    public void addTrackedBlockPosition(Block block, BlockPos pos, EntityPlayer player) {
        if (!SpongeHooks.getActiveConfig(this.worldObj).getConfig().getBlockTracking().getBlockBlacklist().contains(((BlockType)block).getId())) {
            SpongeHooks.logBlockTrack(this.worldObj, block, pos, player, true);
            if (pos.getY() <= 255) {
                this.trackedShortBlockPositions.put(blockPosToShort(pos), ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(player.getPersistentID()));
            } else {
                this.trackedIntBlockPositions.put(blockPosToInt(pos), ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(player.getPersistentID()));
            }
        } else {
            SpongeHooks.logBlockTrack(this.worldObj, block, pos, player, false);
        }
    }

    @Override
    public Map<Integer, Integer> getTrackedIntPlayerPositions() {
        return this.trackedIntBlockPositions;
    }

    @Override
    public Map<Short, Integer> getTrackedShortPlayerPositions() {
        return this.trackedShortBlockPositions;
    }

    @Override
    public Optional<User> getBlockPosOwner(BlockPos pos) {
        if (this.trackedIntBlockPositions.get(blockPosToInt(pos)) != null) {
            int index = this.trackedIntBlockPositions.get(blockPosToInt(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(index));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return Sponge.getGame().getServiceManager().provide(UserStorage.class).get().get(uuid.get());
            }
        } else if (this.trackedShortBlockPositions.get(blockPosToShort(pos)) != null) {
            int index = this.trackedShortBlockPositions.get(blockPosToShort(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(index));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return Sponge.getGame().getServiceManager().provide(UserStorage.class).get().get(uuid.get());
            }
        }

        return Optional.empty();
    }

    @Override
    public void setTrackedIntPlayerPositions(Map<Integer, Integer> trackedPositions) {
        this.trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void setTrackedShortPlayerPositions(Map<Short, Integer> trackedPositions) {
        this.trackedShortBlockPositions = trackedPositions;
    }

    @Override
    public void removeTrackedPlayerPosition(BlockPos pos) {
        if (pos.getY() <= 255) {
            short shortPos = blockPosToShort(pos);
            this.trackedShortBlockPositions.remove(shortPos);
        } else {
            int intPos = blockPosToInt(pos);
            this.trackedIntBlockPositions.remove(intPos);
        }
    }

    /**
    * Modifies bits in an integer.
    * 
    * @param num Integer to modify
    * @param data Bits of data to add
    * @param which Index of nibble to start at
    * @param bitsToReplace The number of bits to replace starting from nibble index
    * @return The modified integer
    */
    public int setNibble(int num, int data, int which, int bitsToReplace) {
        return (num & ~(bitsToReplace << (which * 4)) | (data << (which * 4)));
    }

    /**
     * Serialize this BlockPos into a short value
     */
    public short blockPosToShort(BlockPos pos) {
        short serialized = (short) setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = (short) setNibble(serialized, pos.getY() & Y_SHORT_MASK, 1, NUM_SHORT_Y_BITS);
        serialized = (short) setNibble(serialized, pos.getZ() & XZ_MASK, 3, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    public BlockPos blockPosFromShort(short serialized) {
        int x = this.xPosition * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_SHORT_MASK;
        int z = this.zPosition * 16 + ((serialized >> Z_SHORT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }

    /**
     * Serialize this BlockPos into an int value
     */
    public int blockPosToInt(BlockPos pos) {
        int serialized = setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = setNibble(serialized, pos.getY() & Y_INT_MASK, 1, NUM_INT_Y_BITS);
        serialized = setNibble(serialized, pos.getZ() & XZ_MASK, 7, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    public BlockPos blockPosFromInt(int serialized) {
        int x = this.xPosition * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_INT_MASK;
        int z = this.zPosition * 16 + ((serialized >> Z_INT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }
}
