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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.chunk.PopulateChunkEvent;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.border.PlayerBorderListener;
import org.spongepowered.common.world.gen.SpongePopulatorType;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.IMixinChunk;
import org.spongepowered.mod.util.StaticMixinHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mixin(value = net.minecraft.world.World.class, priority = 1001)
public abstract class MixinWorld implements org.spongepowered.api.world.World, IMixinWorld {

    private long weatherStartTime;
    public boolean captureEntitySpawns = true;
    public boolean captureTerrainGen = false;
    public List<net.minecraft.entity.Entity> capturedEntities = new ArrayList<net.minecraft.entity.Entity>();
    public BlockSnapshot currentTickBlock = null;
    public Entity currentTickEntity = null;
    public TileEntity currentTickTileEntity = null;
    public SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
    public List<BlockSnapshot> capturedSpongeBlockBreaks = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockPlaces = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockModifications = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockFluids = new ArrayList<BlockSnapshot>();
    public Map<PopulatorType, List<BlockTransaction>> capturedSpongePopulators = Maps.newHashMap();
    public Map<CaptureType, List<BlockSnapshot>> captureBlockLists = Maps.newHashMap();
    @SuppressWarnings("unused")
    private boolean keepSpawnLoaded;
    public SpongeConfig<SpongeConfig.WorldConfig> worldConfig;

    @SuppressWarnings("rawtypes")
    @Shadow public List loadedEntityList;
    @Shadow public WorldInfo worldInfo;
    @Shadow public Profiler theProfiler;
    @Shadow public boolean isRemote;
    @Shadow public final Random rand = new Random();
    @Shadow protected boolean scheduledUpdatesAreImmediate;
    @Shadow private net.minecraft.world.border.WorldBorder worldBorder;
    @Shadow(remap = false) public boolean restoringBlockSnapshots;
    @Shadow(remap = false) public boolean captureBlockSnapshots;
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);
    @Shadow public abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow(remap = false) public abstract ImmutableSetMultimap<ChunkCoordIntPair, Ticket> getPersistentChunks();
    @Shadow public abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);
    @Shadow public abstract void updateEntity(net.minecraft.entity.Entity ent);
    @Shadow public abstract boolean isValid(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);
    @Shadow public abstract boolean checkLight(BlockPos pos);
    @Shadow(remap = false) public abstract void markAndNotifyBlock(BlockPos pos, net.minecraft.world.chunk.Chunk chunk, IBlockState old, IBlockState new_, int flags);

    @Inject(method = "updateWeatherBody()V", remap = false, at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThundering(Z)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRaining(Z)V")
    })
    private void onUpdateWeatherBody(CallbackInfo ci) {
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {
        if (!client) {
            String providerName = providerIn.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
            this.worldConfig =
                    new SpongeConfig<SpongeConfig.WorldConfig>(SpongeConfig.Type.WORLD,
                            new File(Sponge.getModConfigDirectory() + File.separator + "worlds" + File.separator
                                    + providerName + File.separator
                                    + (providerIn.getDimensionId() == 0 ? "DIM0" :
                                            Sponge.getSpongeRegistry().getWorldFolder(providerIn.getDimensionId()))
                                    , "world.conf"), Sponge.ECOSYSTEM_NAME.toLowerCase());
        }

        if (Sponge.getGame().getPlatform().getType() == Platform.Type.SERVER) {
            this.worldBorder.addListener(new PlayerBorderListener());
        }
        this.keepSpawnLoaded = ((WorldProperties) info).doesKeepSpawnLoaded();
        // Turn on capturing
        this.captureBlockSnapshots = true;
        this.captureEntitySpawns = true;
        this.captureBlockLists.put(CaptureType.BREAK, this.capturedSpongeBlockBreaks);
        this.captureBlockLists.put(CaptureType.FLUID, this.capturedSpongeBlockFluids);
        this.captureBlockLists.put(CaptureType.MODIFY, this.capturedSpongeBlockModifications);
        this.captureBlockLists.put(CaptureType.PLACE, this.capturedSpongeBlockPlaces);
    }

    @SuppressWarnings("rawtypes")
    @Overwrite
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
            return false;
        } else if (getBlockState(pos) == newState) { // nothing to change
            return false;
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
            Block block = newState.getBlock();
            BlockSnapshot blockSnapshot = null;
            BlockTransaction transaction = null;
            IBlockState currentState = getBlockState(pos);

            if (!this.isRemote) {
                if (StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;
                    if (block != currentState.getBlock()) { // Place
                        spongeChunk.addTrackedBlockPosition(block, pos, StaticMixinHelper.processingPlayer);
                    }
                }
                // remove block position if broken
                if (block == Blocks.air) {
                    ((IMixinChunk) chunk).removeTrackedPlayerPosition(pos);
                }
                blockSnapshot = createSpongeBlockSnapshot(currentState, pos, flags);

                // black magic to track populators
                Class clazz = StaticMixinHelper.getCallerClass(3);

                if (net.minecraft.world.gen.feature.WorldGenerator.class.isAssignableFrom(clazz)) {
                    SpongePopulatorType populatorType = null;
                    populatorType = StaticMixinHelper.populator;

                    if (clazz == net.minecraft.world.gen.feature.WorldGenerator.class) {
                        if (StaticMixinHelper.lastPopulatorClass != null) {
                            clazz = StaticMixinHelper.lastPopulatorClass;
                        } else {
                            int level = 3;
                            // locate correct generator class
                            while (clazz == net.minecraft.world.gen.feature.WorldGenerator.class || clazz == net.minecraft.world.gen.feature.WorldGenHugeTrees.class) {
                                clazz = StaticMixinHelper.getCallerClass(level);
                                level++;
                            }
                        }
                    }

                    if (populatorType == null) {
                        populatorType = (SpongePopulatorType) SpongeMod.instance.getSpongeRegistry().populatorClassToTypeMappings.get(clazz);
                    }

                    if (populatorType != null) {
                        if (this.capturedSpongePopulators.get(populatorType) == null) {
                            this.capturedSpongePopulators.put(populatorType, new ArrayList<BlockTransaction>());
                        }

                        transaction = new BlockTransaction(blockSnapshot, blockSnapshot.withState((BlockState)newState));
                        this.capturedSpongePopulators.get(populatorType).add(transaction);
                    }
                } else if (block.getMaterial().isLiquid() || currentState.getBlock().getMaterial().isLiquid()) {
                    this.capturedSpongeBlockFluids.add(blockSnapshot);
                } else if (block == Blocks.air) {
                    this.capturedSpongeBlockBreaks.add(blockSnapshot);
                } else if (block != currentState.getBlock()) {
                    this.capturedSpongeBlockPlaces.add(blockSnapshot);
                } else {
                    this.capturedSpongeBlockModifications.add(blockSnapshot);
                }
            }

            int oldLight = getBlockState(pos).getBlock().getLightValue((net.minecraft.world.World)(Object) this, pos);

            IBlockState iblockstate1 = chunk.setBlockState(pos, newState);

            if (iblockstate1 == null) {
                if (blockSnapshot != null) {
                    this.capturedSpongeBlockBreaks.remove(blockSnapshot);
                    this.capturedSpongeBlockFluids.remove(blockSnapshot);
                    this.capturedSpongeBlockPlaces.remove(blockSnapshot);
                    this.capturedSpongeBlockModifications.remove(blockSnapshot);
                }
                return false;
            } else {
                Block block1 = iblockstate1.getBlock();

                if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue((net.minecraft.world.World)(Object) this, pos) != oldLight) {
                    this.theProfiler.startSection("checkLight");
                    this.checkLight(pos);
                    this.theProfiler.endSection();
                }

                if (blockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
                    this.markAndNotifyBlock(pos, chunk, iblockstate1, newState, flags); // Modularize client and physic updates
                }

                return true;
            }
        }
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Redirect(method = "forceBlockUpdateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onForceBlockUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null || ((IMixinWorld) worldIn).capturingTerrainGen()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0); // flag doesn't matter here
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onUpdateEntities(net.minecraft.entity.Entity entityIn) {
        if (this.isRemote || this.currentTickEntity != null) {
            entityIn.onUpdate();
            return;
        }

        this.currentTickEntity = (Entity) entityIn;
        entityIn.onUpdate();
        handlePostTickCaptures(Cause.of(entityIn));
        this.currentTickEntity = null;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/gui/IUpdatePlayerListBox;update()V"))
    public void onUpdateTileEntities(IUpdatePlayerListBox tile) {
        if (this.isRemote || this.currentTickTileEntity != null) {
            tile.update();
            return;
        }

        this.currentTickTileEntity = (TileEntity) tile;
        tile.update();
        handlePostTickCaptures(Cause.of(tile));
        this.currentTickTileEntity = null;
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        if (this.isRemote || this.currentTickEntity != null || StaticMixinHelper.processingPlayer != null) {
            entity.onUpdate();
            return;
        }

        this.currentTickEntity = (Entity) entity;
        entity.onUpdate();
        handlePostTickCaptures(Cause.of(entity));
        this.currentTickEntity = null;
    }

    @SuppressWarnings("unchecked")
    @Overwrite
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entityIn) {
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.isRemote && (entityIn == null || (entityIn instanceof net.minecraft.entity.item.EntityItem && this.restoringBlockSnapshots))) return false;

        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer) {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, true)) {
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                net.minecraft.world.World world = (net.minecraft.world.World)(Object) this;
                world.playerEntities.add(entityplayer);
                world.updateAllPlayersSleepingFlag();
            }

            if (this.isRemote) {
                if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (net.minecraft.world.World)(Object)this)) && !flag) return false;

                this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.loadedEntityList.add(entityIn);
                this.onEntityAdded(entityIn);
                return true;
            }

            SpawnEntityEvent specialEvent = null;
            EntityLivingBase specialCause = null;

            // Special case for throwables
            if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityThrowable) {
                EntityThrowable throwable = (EntityThrowable) entityIn;
                specialCause = throwable.getThrower();

                if (specialCause != null) {
                    if (specialCause instanceof Player) {
                        Player player = (Player) specialCause;
                        setCreatorEntityNbt(entityIn.getEntityData(), player.getUniqueId());
                    }
                    specialEvent = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), Cause.of(specialCause), (Entity) entityIn);
                    Sponge.getGame().getEventManager().post(specialEvent);
                }
            }
            // Special case for TNT
            else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTNTPrimed) {
                EntityTNTPrimed entity = (EntityTNTPrimed) entityIn;
                specialCause = entity.getTntPlacedBy();

                if (specialCause instanceof Player) {
                    Player player = (Player) specialCause;
                    setCreatorEntityNbt(entityIn.getEntityData(), player.getUniqueId());
                }
                if (specialCause != null) {
                    specialEvent = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), Cause.of(specialCause), (Entity) entityIn);
                    Sponge.getGame().getEventManager().post(specialEvent);
                }
            }
            // Special case for Tameables
            else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entityIn;
                if (tameable.getOwnerEntity() != null) {
                    specialCause = tameable.getOwnerEntity();
                    specialEvent = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), Cause.of(specialCause), (Entity) entityIn);
                    Sponge.getGame().getEventManager().post(specialEvent);
                }
            }

            if (specialEvent != null) {
                if (!specialEvent.isCancelled()) {
                    this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                    this.loadedEntityList.add(entityIn);
                    this.onEntityAdded(entityIn);
                    return true;
                } else {
                    return false;
                }
            } else if (!flag) {
                this.capturedEntities.add(entityIn);
                return true;
            } else {
                if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (net.minecraft.world.World)(Object)this)) && !flag) return false;

                this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.loadedEntityList.add(entityIn);
                this.onEntityAdded(entityIn);
                return true;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handlePostTickCaptures(Cause cause) {
        if (this.isRemote) {
            return;
        }

        net.minecraft.world.World world = (net.minecraft.world.World)(Object) this;

        // Attempt to find a Player cause if we do not have one
        if (!cause.first(User.class).isPresent()) {
            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
                // Check for player at pos of first transaction
                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
                Optional<TileEntity> te = cause.first(TileEntity.class);
                BlockPos pos = null;
                if (snapshot.isPresent()) {
                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
                } else {
                    pos = ((net.minecraft.tileentity.TileEntity)te.get()).getPos();
                }
                net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
                if (chunk != null) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;
    
                    if (spongeChunk.getBlockPosOwner(pos).isPresent()) {
                        List<Object> causes = new ArrayList<Object>();
                        causes.addAll(cause.all());
                        causes.add(spongeChunk.getBlockPosOwner(pos).get());
                        cause = Cause.of(causes.toArray());
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwnerEntity() != null) {
                        List<Object> causes = new ArrayList<Object>();
                        causes.addAll(cause.all());
                        causes.add(tameable.getOwnerEntity());
                        cause = Cause.of(causes.toArray());
                    }
                } else {
                    if (((IMixinEntity) entity).getSpongeCreator().isPresent()) {
                        List<Object> causes = new ArrayList<Object>();
                        causes.addAll(cause.all());
                        causes.add(((IMixinEntity) entity).getSpongeCreator().get());
                        cause = Cause.of(causes.toArray());
                    }
                }
            }
        }

        // Handle Block captures
        for (Map.Entry<CaptureType, List<BlockSnapshot>> mapEntry : this.captureBlockLists.entrySet()) {
            CaptureType captureType = mapEntry.getKey();
            List<BlockSnapshot> capturedBlockList = mapEntry.getValue();

            if (capturedBlockList.size() > 0) {
                ImmutableList<BlockTransaction> blockTransactions;
                ImmutableList.Builder<BlockTransaction> builder = new ImmutableList.Builder<BlockTransaction>();

                Iterator<BlockSnapshot> iterator = capturedBlockList.iterator();
                while (iterator.hasNext()) {
                    BlockSnapshot blockSnapshot = iterator.next();
                    BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
                    IBlockState currentState = getBlockState(pos);
                    builder.add(new BlockTransaction(blockSnapshot, createSpongeBlockSnapshot(currentState, pos, 0)));
                    iterator.remove();
                }
                blockTransactions = builder.build();

                if (blockTransactions.size() > 0) {
                    ChangeBlockEvent event = null;
                    // Check for tracked user at pos of first transaction
                    if (!cause.first(User.class).isPresent()) {
                        BlockTransaction blockTransaction = blockTransactions.get(0);
                        Optional<Chunk> chunk = this.getChunk(blockTransaction.getOriginal().getPosition());
                        if (chunk.isPresent()) {
                            IMixinChunk spongeChunk = (IMixinChunk) chunk.get();
                            BlockPos pos = VecHelper.toBlockPos(blockTransaction.getOriginal().getPosition());
                            if (spongeChunk.getBlockPosOwner(pos).isPresent()) {
                                List<Object> causes = new ArrayList<Object>();
                                causes.addAll(cause.all());
                                causes.add(spongeChunk.getBlockPosOwner(pos).get());
                                cause = Cause.of(causes);
                            }
                        }
                    }

                    if (captureType == CaptureType.BREAK) {
                        event = SpongeEventFactory.createChangeBlockEventBreak(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.FLUID) {
                        event = SpongeEventFactory.createChangeBlockEventFluid(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.MODIFY) {
                        event = SpongeEventFactory.createChangeBlockEventModify(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.PLACE) {
                        event = SpongeEventFactory.createChangeBlockEventPlace(Sponge.getGame(), cause, (World) world, blockTransactions);
                    }

                    Sponge.getGame().getEventManager().post(event);

                    EntityPlayerMP player = null;
                    C08PacketPlayerBlockPlacement packet = null;

                    if (StaticMixinHelper.processingPlayer != null) {
                        player = (EntityPlayerMP) StaticMixinHelper.processingPlayer;
                    }

                    if (StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) { // player place
                        packet = (C08PacketPlayerBlockPlacement) StaticMixinHelper.processingPacket;
                    }

                    if (event.isCancelled()) {

                        // Restore original blocks
                        for (BlockTransaction transaction : event.getTransactions()) {
                            this.restoringBlockSnapshots = true;
                            transaction.getOriginal().restore(true, false);
                            this.restoringBlockSnapshots = false;
                        }

                        if (captureType == CaptureType.BREAK && player != null) {
                            // Let the client know the blocks still exist
                            for (BlockTransaction transaction : event.getTransactions()) {
                                BlockSnapshot snapshot = transaction.getOriginal();
                                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
                                player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(world, pos));

                                // Update any tile entity data for this block
                                net.minecraft.tileentity.TileEntity tileentity = world.getTileEntity(pos);
                                if (tileentity != null)  {
                                    Packet pkt = tileentity.getDescriptionPacket();
                                    if (pkt != null) {
                                        player.playerNetServerHandler.sendPacket(pkt);
                                    }
                                }
                            }
                        } else if (captureType == CaptureType.PLACE && player != null && packet != null) {
                            // handle revert
                            player.isChangingQuantityOnly = true;
                            player.inventory.mainInventory[player.inventory.currentItem] = ItemStack.copyItemStack(packet.getStack());
                            Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                            player.openContainer.detectAndSendChanges();
                            player.isChangingQuantityOnly = false;
                            // force client itemstack update if place event was cancelled
                            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, packet.getStack()));
                        }

                        // clear entity list and return to avoid spawning items
                        this.capturedEntities.clear();
                        return;
                    } else {
                        markAndNotifyBlockPost(event.getTransactions(), captureType, cause);

                        if (captureType == CaptureType.PLACE && player != null && packet != null && packet.getStack() != null) {
                            player.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                        }
                    }
                }
            }
        }

        // Handle Populators
        boolean handlePopulators = false;

        for (List<BlockTransaction> transactions : this.capturedSpongePopulators.values()) {
            if (transactions.size() > 0) {
                handlePopulators = true;
                break;
            }
        }

        if (handlePopulators && cause.first(Chunk.class).isPresent()) {
            Chunk targetChunk = cause.first(Chunk.class).get();
            PopulateChunkEvent.Post event = SpongeEventFactory.createPopulateChunkEventPost(Sponge.getGame(), cause, ImmutableMap.copyOf(this.capturedSpongePopulators), targetChunk);
            Sponge.getGame().getEventManager().post(event);

            for (List<BlockTransaction> transactions : event.getPopulatedTransactions().values()) {
                markAndNotifyBlockPost(transactions, CaptureType.POPULATE, cause);
            }

            for (List<BlockTransaction> transactions : this.capturedSpongePopulators.values()) {
                transactions.clear();
            }
        }

        // Handle Entity captures
        Iterator<net.minecraft.entity.Entity> iterator = this.capturedEntities.iterator();
        while(iterator.hasNext()) {
            net.minecraft.entity.Entity capturedEntity = iterator.next();

            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                setCreatorEntityNbt(capturedEntity.getEntityData(), user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getSpongeCreator();
                if (owner.isPresent()) {
                    List<Object> causes = new ArrayList<Object>();
                    causes.addAll(cause.all());
                    causes.add(owner.get());
                    cause = Cause.of(causes.toArray());
                    setCreatorEntityNbt(capturedEntity.getEntityData(), owner.get().getUniqueId());
                }
            }
            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), cause, (Entity) capturedEntity);
            if (!(Sponge.getGame().getEventManager().post(event))) {
                int x = MathHelper.floor_double(capturedEntity.posX / 16.0D);
                int z = MathHelper.floor_double(capturedEntity.posZ / 16.0D);
                this.getChunkFromChunkCoords(x, z).addEntity(capturedEntity);
                this.loadedEntityList.add(capturedEntity);
                this.onEntityAdded(capturedEntity);
                SpongeHooks.logEntitySpawn(cause, capturedEntity);
            }
            iterator.remove();
        }
    }

    private void setCreatorEntityNbt(NBTTagCompound nbt, UUID uuid) {
        if (!nbt.hasKey(NbtDataUtil.SPONGE_DATA)) {
            NBTTagCompound spongeNbt = new NBTTagCompound();
            NBTTagCompound creatorNbt = new NBTTagCompound();
            creatorNbt.setLong("uuid_least", uuid.getLeastSignificantBits());
            creatorNbt.setLong("uuid_most", uuid.getMostSignificantBits());
            spongeNbt.setTag(NbtDataUtil.SPONGE_ENTITY_CREATOR, creatorNbt);
            nbt.setTag(NbtDataUtil.SPONGE_DATA, spongeNbt);
        } else if (!nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA).hasKey(NbtDataUtil.SPONGE_ENTITY_CREATOR)){
            NBTTagCompound spongeNbt = nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA);
            NBTTagCompound creatorNbt = new NBTTagCompound();
            creatorNbt.setLong("uuid_least", uuid.getLeastSignificantBits());
            creatorNbt.setLong("uuid_most", uuid.getMostSignificantBits());
            spongeNbt.setTag(NbtDataUtil.SPONGE_ENTITY_CREATOR, creatorNbt);
        } else {
            nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA).getCompoundTag(NbtDataUtil.SPONGE_ENTITY_CREATOR).setLong("uuid_least", uuid.getLeastSignificantBits());
            nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA).getCompoundTag(NbtDataUtil.SPONGE_ENTITY_CREATOR).setLong("uuid_most", uuid.getMostSignificantBits());
        }
    }

    @Override
    public Optional<BlockSnapshot> getCurrentTickBlock() {
        return Optional.ofNullable(this.currentTickBlock);
    }

    @Override
    public Optional<Entity> getCurrentTickEntity() {
        return Optional.ofNullable(this.currentTickEntity);
    }

    @Override
    public Optional<TileEntity> getCurrentTickTileEntity() {
        return Optional.ofNullable(this.currentTickTileEntity);
    }

    @Override
    public boolean capturingTerrainGen() {
        return this.captureTerrainGen;
    }

    @Override
    public void setCapturingTerrainGen(boolean flag) {
        this.captureTerrainGen = flag;
    }

    @Override
    public void setCapturingEntitySpawns(boolean flag) {
        this.captureEntitySpawns = flag;
    }

    @Override
    public List<BlockSnapshot> getBlockBreakList() {
        return this.capturedSpongeBlockBreaks;
    }

    private void markAndNotifyBlockPost(List<BlockTransaction> transactions, CaptureType type, Cause cause) {
        for (BlockTransaction transaction : transactions) {
            // Handle custom replacements
            if (transaction.isValid() && transaction.getCustomReplacement().isPresent()) {
                this.restoringBlockSnapshots = true;
                transaction.getFinalReplacement().restore(true, false);
                this.restoringBlockSnapshots = false;
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot)transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot)transaction.getFinalReplacement();
            SpongeHooks.logBlockAction(cause, (net.minecraft.world.World)(Object) this, type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState oldBlock = (IBlockState)oldBlockSnapshot.getState();
            IBlockState newBlock = (IBlockState)newBlockSnapshot.getState();
            if (newBlock != null && !(newBlock.getBlock().hasTileEntity(newBlock))) { // Containers get placed automatically
                newBlock.getBlock().onBlockAdded((net.minecraft.world.World)(Object)this, pos, newBlock);
            }

            markAndNotifyBlock(pos, null, oldBlock, newBlock, updateFlag);
        }
    }

    public SpongeBlockSnapshot createSpongeBlockSnapshot(IBlockState state, BlockPos pos, int updateFlag) {
        builder.reset();
        Location<World> location = new Location<World>((World) this, VecHelper.toVector(pos));
        builder.blockState((BlockState) state)
            .worldId(location.getExtent().getUniqueId())
            .position(location.getBlockPosition());
        net.minecraft.tileentity.TileEntity te = getTileEntity(pos);
        NBTTagCompound nbt = null;
        if (te != null) {
            nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
        }
        if (nbt != null) {
            builder.unsafeNbt(nbt);
        }

        return new SpongeBlockSnapshot(builder, updateFlag);
    }
}
