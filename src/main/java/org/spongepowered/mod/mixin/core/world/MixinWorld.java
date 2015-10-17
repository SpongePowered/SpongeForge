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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
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
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.inventory.DropItemEvent;
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
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
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
public abstract class MixinWorld implements World, IMixinWorld {

    private long weatherStartTime;
    public boolean processingCaptureCause = false;
    public boolean captureEntitySpawns = true;
    public boolean captureTerrainGen = false;
    public List<Entity> capturedEntities = new ArrayList<Entity>();
    public List<Entity> capturedEntityItems = new ArrayList<Entity>();
    public List<Entity> capturedOnBlockAddedEntities = new ArrayList<Entity>();
    public List<Entity> capturedOnBlockAddedItems = new ArrayList<Entity>();
    public BlockSnapshot currentTickBlock = null;
    public BlockSnapshot currentTickOnBlockAdded = null;
    public Entity currentTickEntity = null;
    public TileEntity currentTickTileEntity = null;
    public SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
    public List<BlockSnapshot> capturedSpongeBlockBreaks = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockPlaces = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockModifications = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockFluids = new ArrayList<BlockSnapshot>();
    public Map<PopulatorType, List<Transaction<BlockSnapshot>>> capturedSpongePopulators = Maps.newHashMap();
    public Map<CaptureType, List<BlockSnapshot>> captureBlockLists = Maps.newHashMap();
    @SuppressWarnings("unused")
    private boolean keepSpawnLoaded;
    private boolean worldSpawnerRunning;
    private boolean chunkSpawnerRunning;
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
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
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
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
            IBlockState currentState = chunk.getBlockState(pos);
            if (currentState == newState) {
                return false;
            }

            Block block = newState.getBlock();
            BlockSnapshot originalBlockSnapshot = null;
            BlockSnapshot newBlockSnapshot = null;
            Transaction<BlockSnapshot> transaction = null;

            if (!this.isRemote && !this.restoringBlockSnapshots) { // don't capture if we are restoring blocks
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
                originalBlockSnapshot = createSpongeBlockSnapshot(currentState, pos, flags);

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
                            this.capturedSpongePopulators.put(populatorType, new ArrayList<Transaction<BlockSnapshot>>());
                        }

                        transaction = new Transaction<BlockSnapshot>(originalBlockSnapshot, originalBlockSnapshot.withState((BlockState)newState));
                        this.capturedSpongePopulators.get(populatorType).add(transaction);
                    }
                } else if (block.getMaterial().isLiquid() || currentState.getBlock().getMaterial().isLiquid()) {
                    this.capturedSpongeBlockFluids.add(originalBlockSnapshot);
                } else if (block == Blocks.air) {
                    this.capturedSpongeBlockBreaks.add(originalBlockSnapshot);
                } else if (block != currentState.getBlock()) {
                    this.capturedSpongeBlockPlaces.add(originalBlockSnapshot);
                } else {
                    this.capturedSpongeBlockModifications.add(originalBlockSnapshot);
                }
            }

            int oldLight = currentState.getBlock().getLightValue((net.minecraft.world.World)(Object) this, pos);

            IBlockState iblockstate1 = ((IMixinChunk)chunk).setBlockState(pos, newState, currentState, newBlockSnapshot);

            if (iblockstate1 == null) {
                if (originalBlockSnapshot != null) {
                    this.capturedSpongeBlockBreaks.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockFluids.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockPlaces.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockModifications.remove(originalBlockSnapshot);
                }
                return false;
            } else {
                Block block1 = iblockstate1.getBlock();

                if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue((net.minecraft.world.World)(Object) this, pos) != oldLight) {
                    this.theProfiler.startSection("checkLight");
                    this.checkLight(pos);
                    this.theProfiler.endSection();
                }

                if (originalBlockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
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

        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0); // flag doesn't matter here
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onUpdateEntities(net.minecraft.entity.Entity entityIn) {
        if (this.isRemote || this.currentTickEntity != null) {
            entityIn.onUpdate();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickEntity = (Entity) entityIn;
        entityIn.onUpdate();
        handlePostTickCaptures(Cause.of(entityIn));
        this.currentTickEntity = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/gui/IUpdatePlayerListBox;update()V"))
    public void onUpdateTileEntities(IUpdatePlayerListBox tile) {
        if (this.isRemote || this.currentTickTileEntity != null) {
            tile.update();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickTileEntity = (TileEntity) tile;
        tile.update();
        handlePostTickCaptures(Cause.of(tile));
        this.currentTickTileEntity = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        if (this.isRemote || this.currentTickEntity != null || StaticMixinHelper.processingPlayer != null) {
            entity.onUpdate();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickEntity = (Entity) entity;
        entity.onUpdate();
        handlePostTickCaptures(Cause.of(entity));
        this.currentTickEntity = null;
        this.processingCaptureCause = false;
    }

    @Overwrite
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity) {
        return spawnEntity((Entity) entity, Cause.of(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean spawnEntity(Entity entity, Cause cause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(cause, "Cause cannot be null!");

        net.minecraft.entity.Entity entityIn = (net.minecraft.entity.Entity) entity;
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

            if (this.isRemote || flag) {
                if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (net.minecraft.world.World)(Object)this)) && !flag) return false;

                this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.loadedEntityList.add(entityIn);
                this.onEntityAdded(entityIn);
                return true;
            }

            if (!flag && this.processingCaptureCause) {
                if (entityIn instanceof EntityItem) {
                    if (this.currentTickOnBlockAdded != null) {
                        this.capturedOnBlockAddedItems.add((Item) entityIn);
                    } else {
                        this.capturedEntityItems.add((Item) entityIn);
                    }
                } else {
                    if (this.currentTickOnBlockAdded != null) {
                        this.capturedOnBlockAddedEntities.add((Entity) entityIn);
                    } else {
                        this.capturedEntities.add((Entity) entityIn);
                    }
                }
                return true;
            } else { // Custom

                if (entityIn instanceof EntityFishHook && ((EntityFishHook) entityIn).angler == null) {
                    // TODO MixinEntityFishHook.setShooter makes angler null sometimes,
                    // but that will cause NPE when ticking
                    return false;
                }

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
                    }
                }
                // Special case for TNT
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTNTPrimed) {
                    EntityTNTPrimed tntEntity = (EntityTNTPrimed) entityIn;
                    specialCause = tntEntity.getTntPlacedBy();

                    if (specialCause instanceof Player) {
                        Player player = (Player) specialCause;
                        setCreatorEntityNbt(entityIn.getEntityData(), player.getUniqueId());
                    }
                }
                // Special case for Tameables
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entityIn;
                    if (tameable.getOwnerEntity() != null) {
                        specialCause = tameable.getOwnerEntity();
                    }
                }

                if (specialCause != null) {
                    if (!cause.all().contains(specialCause)) {
                        cause = cause.with(specialCause);
                    }
                }

                org.spongepowered.api.event.Event event = null;
                ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
                entitySnapshotBuilder.add(((Entity) entityIn).createSnapshot());

                if (entityIn instanceof EntityItem) {
                    this.capturedEntityItems.add((Item) entityIn);
                    event = SpongeEventFactory.createDropItemEventCustom(Sponge.getGame(), cause, (List<Entity>)(List<?>)this.capturedEntityItems, entitySnapshotBuilder.build(), (World)(Object) this);
                } else {
                    event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getGame(), cause, this.capturedEntities, entitySnapshotBuilder.build(), (World)(Object) this);
                }
                Sponge.getGame().getEventManager().post(event);

                if (!((Cancellable)event).isCancelled()) {
                    if(entityIn instanceof net.minecraft.entity.effect.EntityWeatherEffect) {
                        return addWeatherEffect(entityIn);
                    }

                    this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                    this.loadedEntityList.add(entityIn);
                    this.onEntityAdded(entityIn);
                    if (entityIn instanceof EntityItem) {
                        this.capturedEntityItems.remove(entityIn);
                    } else {
                        this.capturedEntities.remove(entityIn);
                    }
                    return true;
                }

                return false;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handlePostTickCaptures(Cause cause) {
        if (this.isRemote || this.restoringBlockSnapshots || cause == null || cause.isEmpty()) {
            return;
        } else if (this.capturedEntities.size() == 0 && this.capturedEntityItems.size() == 0 && this.capturedSpongeBlockBreaks.size() == 0
                && this.capturedSpongeBlockModifications.size() == 0 && this.capturedSpongeBlockPlaces.size() == 0
                && this.capturedSpongeBlockFluids.size() == 0 && this.capturedSpongePopulators.size() == 0) {
            return; // nothing was captured, return
        }

        net.minecraft.world.World world = (net.minecraft.world.World)(Object) this;
        List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<Transaction<BlockSnapshot>>();
        boolean destructDrop = false;

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
    
                    Optional<User> user = spongeChunk.getBlockPosOwner(pos);
                    if (user.isPresent()) {
                        cause = cause.with(user.get());
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwnerEntity() != null) {
                        cause = cause.with(tameable.getOwnerEntity());
                    }
                } else {
                    if (((IMixinEntity) entity).getSpongeCreator().isPresent()) {
                       cause = cause.with(((IMixinEntity) entity).getSpongeCreator().get());
                    }
                }
            }
        }

        // Check root cause for additional information
        if (cause.root().get() instanceof EntityLivingBase) {
            EntityLivingBase rootEntity = (EntityLivingBase)cause.root().get();
            EntitySnapshot lastKilledEntity = ((IMixinEntityLivingBase)rootEntity).getLastKilledTarget();
            EntityLivingBase lastActiveTarget = ((IMixinEntityLivingBase)rootEntity).getLastActiveTarget();
            // Check for targeted entity
            if (lastKilledEntity != null) {
                // add the last targeted entity
                if (!cause.all().contains(lastKilledEntity)) {
                    Cause newCause = Cause.of(lastKilledEntity);
                    newCause = newCause.with(cause.all());
                    cause = newCause;
                    destructDrop = true;
                }
            }  else if (lastActiveTarget != null) {
                if (!cause.all().contains(lastActiveTarget)) {
                    if (lastActiveTarget.getHealth() <= 0) {
                        Cause newCause = Cause.of(((Entity)lastActiveTarget).createSnapshot());
                        newCause = newCause.with(cause.all());
                        cause = newCause;
                        destructDrop = true;
                    } else {
                        cause = cause.with(lastActiveTarget);
                    }
                }
            }
            if (rootEntity.getHealth() <= 0) {
                destructDrop = true;
            }
        } else {
            if (cause.root().get() instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity entity = (net.minecraft.entity.Entity) cause.root().get();
                if (entity.isDead) {
                    destructDrop = true;
                }
            }
        }

        // Handle Block captures
        for (Map.Entry<CaptureType, List<BlockSnapshot>> mapEntry : this.captureBlockLists.entrySet()) {
            CaptureType captureType = mapEntry.getKey();
            List<BlockSnapshot> capturedBlockList = mapEntry.getValue();

            if (capturedBlockList.size() > 0) {
                ImmutableList<Transaction<BlockSnapshot>> blockTransactions;
                ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<Transaction<BlockSnapshot>>();

                Iterator<BlockSnapshot> iterator = capturedBlockList.iterator();
                while (iterator.hasNext()) {
                    BlockSnapshot blockSnapshot = iterator.next();
                    BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
                    IBlockState currentState = getBlockState(pos);
                    builder.add(new Transaction<BlockSnapshot>(blockSnapshot, createSpongeBlockSnapshot(currentState, pos, 0)));
                    iterator.remove();
                }
                blockTransactions = builder.build();

                if (blockTransactions.size() > 0) {
                    ChangeBlockEvent event = null;
                    // Check for tracked user at pos of first transaction
                    if (!cause.first(User.class).isPresent()) {
                        Transaction<BlockSnapshot> blockTransaction = blockTransactions.get(0);
                        Optional<Chunk> chunk = this.getChunk(blockTransaction.getOriginal().getPosition());
                        if (chunk.isPresent()) {
                            IMixinChunk spongeChunk = (IMixinChunk) chunk.get();
                            BlockPos pos = VecHelper.toBlockPos(blockTransaction.getOriginal().getPosition());
                            Optional<User> user = spongeChunk.getBlockPosOwner(pos);
                            if (user.isPresent()) {
                                cause = cause.with(user);
                            }
                        }
                    }

                    if (captureType == CaptureType.BREAK) {
                        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C07PacketPlayerDigging) {
                            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) StaticMixinHelper.processingPacket;
                            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                                destructDrop = true;
                                Cause newCause = Cause.of(blockTransactions.get(0).getOriginal()); // make the first destroyed block root
                                newCause = newCause.with(cause.all());
                                cause = newCause;
                            }
                        }

                        event = SpongeEventFactory.createChangeBlockEventBreak(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.FLUID) {
                        event = SpongeEventFactory.createChangeBlockEventFluid(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.MODIFY) {
                        event = SpongeEventFactory.createChangeBlockEventModify(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.PLACE) {
                        event = SpongeEventFactory.createChangeBlockEventPlace(Sponge.getGame(), cause, (World) world, blockTransactions);
                    }

                    Sponge.getGame().getEventManager().post(event);

                    C08PacketPlayerBlockPlacement packet = null;

                    if (StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) { // player place
                        packet = (C08PacketPlayerBlockPlacement) StaticMixinHelper.processingPacket;
                    }

                    for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                        if (!transaction.isValid()) {
                            this.restoringBlockSnapshots = true;
                            transaction.getOriginal().restore(true, false);
                            this.restoringBlockSnapshots = false;
                            invalidTransactions.add(transaction);
                        }
                    }

                    if (event.isCancelled()) {
                        // Restore original blocks
                        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                            this.restoringBlockSnapshots = true;
                            transaction.getOriginal().restore(true, false);
                            this.restoringBlockSnapshots = false;
                        }

                        handlePostPlayerBlockEvent(captureType, StaticMixinHelper.processingPlayer, world, event.getTransactions());

                        // clear entity list and return to avoid spawning items
                        this.capturedEntities.clear();
                        this.capturedEntityItems.clear();
                        return;
                    } else {
                        if (invalidTransactions.size() > 0) {
                            handlePostPlayerBlockEvent(captureType, StaticMixinHelper.processingPlayer, world, invalidTransactions);
                        }

                        if (this.capturedEntityItems.size() > 0) {
                            handleDroppedItems(cause, (List<Entity>)(List<?>)this.capturedEntityItems, invalidTransactions, captureType == CaptureType.BREAK ? true : destructDrop);
                        }

                        markAndNotifyBlockPost(event.getTransactions(), captureType, cause);

                        if (captureType == CaptureType.PLACE && StaticMixinHelper.processingPlayer != null && packet != null && packet.getStack() != null) {
                            StaticMixinHelper.processingPlayer.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                        }
                    }
                }
            }
        }

        // Handle Populators
        boolean handlePopulators = false;

        for (List<Transaction<BlockSnapshot>> transactions : this.capturedSpongePopulators.values()) {
            if (transactions.size() > 0) {
                handlePopulators = true;
                break;
            }
        }

        if (handlePopulators && cause.first(Chunk.class).isPresent()) {
            Chunk targetChunk = cause.first(Chunk.class).get();
            PopulateChunkEvent.Post event = SpongeEventFactory.createPopulateChunkEventPost(Sponge.getGame(), cause, ImmutableMap.copyOf(this.capturedSpongePopulators), targetChunk);
            Sponge.getGame().getEventManager().post(event);

            for (List<Transaction<BlockSnapshot>> transactions : event.getPopulatedTransactions().values()) {
                markAndNotifyBlockPost(transactions, CaptureType.POPULATE, cause);
            }

            for (List<Transaction<BlockSnapshot>> transactions : this.capturedSpongePopulators.values()) {
                transactions.clear();
            }
        }

        // Handle Player Toss
        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) StaticMixinHelper.processingPacket;
            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
                destructDrop = false;
            }
        }

        // Handle Player kill commands
        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) StaticMixinHelper.processingPacket;
            if (chatPacket.getMessage().contains("kill")) {
                if (!cause.all().contains(StaticMixinHelper.processingPlayer)) {
                    cause = cause.with(StaticMixinHelper.processingPlayer);
                }
                destructDrop = true;
            }
        }

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            handleDroppedItems(cause, (List<Entity>)(List<?>)this.capturedEntityItems, invalidTransactions, destructDrop);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause, this.capturedEntities, invalidTransactions);
        }
    }

    private void handlePostPlayerBlockEvent(CaptureType captureType, EntityPlayerMP player, net.minecraft.world.World world, List<Transaction<BlockSnapshot>> transactions) {
        if (captureType == CaptureType.BREAK && player != null) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
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
        } else if (captureType == CaptureType.PLACE && player != null) {
            sendItemChangeToPlayer(player);
        }
    }

    private void sendItemChangeToPlayer(EntityPlayerMP player) {
        if (StaticMixinHelper.lastPlayerItem == null) {
            return;
        }

        // handle revert
        player.isChangingQuantityOnly = true;
        player.inventory.mainInventory[player.inventory.currentItem] = StaticMixinHelper.lastPlayerItem;
        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, StaticMixinHelper.lastPlayerItem));
    }

    private void handleDroppedItems(Cause cause, List<Entity> entities, List<Transaction<BlockSnapshot>> invalidTransactions, boolean destructItems) {
        Iterator<Entity> iter = entities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                setCreatorEntityNbt(((net.minecraft.entity.Entity)currentEntity).getEntityData(), user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getSpongeCreator();
                if (owner.isPresent()) {
                    if (!cause.all().contains(owner.get())) {
                        cause = cause.with(owner.get());
                    }
                    setCreatorEntityNbt(((net.minecraft.entity.Entity)currentEntity).getEntityData(), owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        DropItemEvent event = null;

        if (destructItems) {
            event = SpongeEventFactory.createDropItemEventDestruct(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build() , (World) this);
        } else {
            event = SpongeEventFactory.createDropItemEventDispense(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build() , (World) this);
        }

        if (!(Sponge.getGame().getEventManager().post(event))) {
            // Handle player deaths
            if (cause.first(Player.class).isPresent()) {
                EntityPlayerMP player = (EntityPlayerMP) cause.first(Player.class).get();
                if (player.getHealth() <= 0 && (player.theItemInWorldManager.getGameType() != GameType.CREATIVE) && !player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
                    player.inventory.clear();
                } else if (player.getHealth() <= 0) { // don't drop anything if creative or keepInventory
                    this.capturedEntityItems.clear();
                }
            }

            Iterator<Entity> iterator = event.getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                boolean invalidSpawn = false;
                if (invalidTransactions != null) {
                    for (Transaction<BlockSnapshot> transaction : invalidTransactions) {
                        if (transaction.getOriginal().getLocation().get().getBlockPosition().equals(entity.getLocation().getBlockPosition())) {
                            invalidSpawn = true;
                            break;
                        }
                    }
    
                    if (invalidSpawn) {
                        iterator.remove();
                        continue;
                    }
                }

                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                this.getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                this.loadedEntityList.add(nmsEntity);
                this.onEntityAdded(nmsEntity);
                SpongeHooks.logEntitySpawn(cause, nmsEntity);
                iterator.remove();
            }
        } else {
            if (StaticMixinHelper.processingPlayer != null) {
                sendItemChangeToPlayer(StaticMixinHelper.processingPlayer);
            }
            this.capturedEntityItems.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleEntitySpawns(Cause cause, List<Entity> entities, List<Transaction<BlockSnapshot>> invalidTransactions) {
        Iterator<Entity> iter = entities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                setCreatorEntityNbt(((net.minecraft.entity.Entity)currentEntity).getEntityData(), user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getSpongeCreator();
                if (owner.isPresent()) {
                    if (!cause.all().contains(owner.get())) {
                        cause = cause.with(owner.get());
                    }
                    setCreatorEntityNbt(((net.minecraft.entity.Entity)currentEntity).getEntityData(), owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        SpawnEntityEvent event = null;

        if (this.worldSpawnerRunning) {
            event = SpongeEventFactory.createSpawnEntityEventSpawner(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        } else if (this.chunkSpawnerRunning){
            event = SpongeEventFactory.createSpawnEntityEventChunkLoad(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        } else {
            event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        }

        if (!(Sponge.getGame().getEventManager().post(event))) {
            Iterator<Entity> iterator = event.getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                boolean invalidSpawn = false;
                if (invalidTransactions != null) {
                    for (Transaction<BlockSnapshot> transaction : invalidTransactions) {
                        if (transaction.getOriginal().getLocation().get().getBlockPosition().equals(entity.getLocation().getBlockPosition())) {
                            invalidSpawn = true;
                            break;
                        }
                    }
    
                    if (invalidSpawn) {
                        iterator.remove();
                        continue;
                    }
                }

                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                this.getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                this.loadedEntityList.add(nmsEntity);
                this.onEntityAdded(nmsEntity);
                SpongeHooks.logEntitySpawn(cause, nmsEntity);
                iterator.remove();
            }
        } else {
            this.capturedEntities.clear();
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

    @Override
    public boolean isWorldSpawnerRunning() {
        return this.worldSpawnerRunning;
    }

    @Override
    public void setWorldSpawnerRunning(boolean flag) {
        this.worldSpawnerRunning = flag;
    }

    @Override
    public boolean isChunkSpawnerRunning() {
        return this.chunkSpawnerRunning;
    }

    @Override
    public void setChunkSpawnerRunning(boolean flag) {
        this.chunkSpawnerRunning = flag;
    }

    @Override
    public boolean processingCaptureCause() {
        return this.processingCaptureCause;
    }

    @Override
    public void setProcessingCaptureCause(boolean flag) {
        this.processingCaptureCause = flag;
    }

    @Override
    public void setCurrentTickBlock(BlockSnapshot snapshot) {
        this.currentTickBlock = snapshot;
    }

    private void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, CaptureType type, Cause cause) {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            // Handle custom replacements
            if (transaction.isValid() && transaction.getCustom().isPresent()) {
                this.restoringBlockSnapshots = true;
                transaction.getFinal().restore(true, false);
                this.restoringBlockSnapshots = false;
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot)transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot)transaction.getFinal();
            SpongeHooks.logBlockAction(cause, (net.minecraft.world.World)(Object) this, type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState originalState = (IBlockState)oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState)newBlockSnapshot.getState();
            if (newState != null && !(newState.getBlock().hasTileEntity(newState))) { // Containers get placed automatically
                this.currentTickOnBlockAdded = this.createSpongeBlockSnapshot(newState, pos, updateFlag);
                newState.getBlock().onBlockAdded((net.minecraft.world.World)(Object)this, pos, newState);
                if (this.capturedOnBlockAddedItems.size() > 0) {
                    Cause blockCause = Cause.of(this.currentTickOnBlockAdded);
                    if (this.captureTerrainGen) {
                        net.minecraft.world.chunk.Chunk chunk = getChunkFromBlockCoords(pos);
                        if (chunk != null && ((IMixinChunk) chunk).getCurrentPopulateCause() != null) {
                            blockCause = blockCause.with(((IMixinChunk) chunk).getCurrentPopulateCause().all());
                        }
                    }
                    handleDroppedItems(blockCause, this.capturedOnBlockAddedItems, null, getBlockState(pos) != newState);
                }
                if (this.capturedOnBlockAddedEntities.size() > 0) {
                    Cause blockCause = Cause.of(this.currentTickOnBlockAdded);
                    if (this.captureTerrainGen) {
                        net.minecraft.world.chunk.Chunk chunk = getChunkFromBlockCoords(pos);
                        if (chunk != null && ((IMixinChunk) chunk).getCurrentPopulateCause() != null) {
                            blockCause = blockCause.with(((IMixinChunk) chunk).getCurrentPopulateCause().all());
                        }
                    }
                    handleEntitySpawns(blockCause, this.capturedOnBlockAddedEntities, null);
                }

                this.currentTickOnBlockAdded = null;
            }

            markAndNotifyBlock(pos, null, originalState, newState, updateFlag);
        }
    }

    @Override
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
