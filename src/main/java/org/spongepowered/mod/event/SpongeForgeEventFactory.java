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
package org.spongepowered.mod.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.BreakBlockEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.HarvestBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.PlaceBlockEvent;
import org.spongepowered.api.event.entity.CreateEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.entity.item.TargetItemEvent;
import org.spongepowered.api.event.entity.living.TargetLivingEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.living.player.PlayerEvent;
import org.spongepowered.api.event.server.ServerLoadWorldEvent;
import org.spongepowered.api.event.server.ServerUnloadWorldEvent;
import org.spongepowered.api.event.world.WorldEvent;
import org.spongepowered.api.event.world.WorldExplosionEvent;
import org.spongepowered.api.event.world.WorldLoadChunkEvent;
import org.spongepowered.api.event.world.WorldUnloadChunkEvent;
import org.spongepowered.api.event.world.chunk.ChangeChunkEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class SpongeForgeEventFactory {

    public static net.minecraftforge.fml.common.eventhandler.Event findAndCreateForgeEvent(Event event,
            Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {

        // Block events
        if (net.minecraftforge.event.world.BlockEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent.class) {
                return createBlockNeighborNotifyEvent(event);
            }
            else if (clazz == net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent.class) {
                return createBlockHarvestEvent(event);
            } else if (clazz == net.minecraftforge.event.world.BlockEvent.BreakEvent.class) {
                return createBlockBreakEvent(event);
            } else if (clazz == net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent.class ||
                    clazz == net.minecraftforge.event.world.BlockEvent.PlaceEvent.class) {
                return createBlockPlaceEvent(event);
            } else {
                return createBlockEvent(event);
            }
        }

        // Player events
        else if (net.minecraftforge.event.entity.player.PlayerEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.entity.player.AchievementEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.AnvilRepairEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.ArrowLooseEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.ArrowNockEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.AttackEntityEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.BonemealEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.EntityInteractEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.EntityItemPickupEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.FillBucketEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.ItemTooltipEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerDropsEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerDestroyItemEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerFlyableFallEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerInteractEvent.class) {
                return createPlayerInteractEvent(event);
            } else if (clazz == net.minecraftforge.event.entity.player.PlayerOpenContainerEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerPickupXpEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerSleepInBedEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerUseItemEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerWakeUpEvent.class) {

            } else {
                return createPlayerEvent(event);
            }
        }

        // Living events
        else if (net.minecraftforge.event.entity.living.LivingEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.entity.living.LivingAttackEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingDeathEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingDropsEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingExperienceDropEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingFallEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingHealEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingHurtEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingPackSizeEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingSpawnEvent.class) {

            } else {
                return createLivingEvent(event);
            }
        }

        // Entity events
        else if (net.minecraftforge.event.entity.EntityEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.entity.EntityEvent.EntityConstructing.class) {
                return createEntityConstructingEvent(event);
            }
            else if (clazz == net.minecraftforge.event.entity.EntityMountEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.EntityStruckByLightningEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.EntityJoinWorldEvent.class) {
                return createEntityJoinWorldEvent(event);
            } else {
                return createEntityEvent(event);
            }
        }

        // Item events
        else if (net.minecraftforge.event.entity.item.ItemEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.entity.item.ItemExpireEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.item.ItemTossEvent.class) {

            } else {
                return createItemEvent(event);
            }
        }

        // World events
        else if (net.minecraftforge.event.world.WorldEvent.class.isAssignableFrom(clazz)) {
            if (net.minecraftforge.event.world.ChunkEvent.class.isAssignableFrom(clazz)) {
                if (clazz == net.minecraftforge.event.world.ChunkEvent.Load.class) {
                    return createChunkLoadEvent(event);
                } else if (clazz == net.minecraftforge.event.world.ChunkEvent.Unload.class) {
                    return createChunkUnloadEvent(event);
                } else if (clazz == net.minecraftforge.event.world.ChunkDataEvent.Load.class) {

                } else if (clazz == net.minecraftforge.event.world.ChunkDataEvent.Save.class) {

                } else if (clazz == net.minecraftforge.event.world.ChunkWatchEvent.UnWatch.class) {

                } else if (clazz == net.minecraftforge.event.world.ChunkWatchEvent.Watch.class) {

                }
                return createChunkEvent(event);
            } else if (clazz == net.minecraftforge.event.world.WorldEvent.Load.class) {
                return createWorldLoadEvent(event);
            } else if (clazz == net.minecraftforge.event.world.WorldEvent.Unload.class) {
                return createWorldUnloadEvent(event);
            } else if (clazz == net.minecraftforge.event.world.WorldEvent.Save.class) {

            } else {
                return createWorldEvent(event);
            }
        }

        // Explosion events
        else if (net.minecraftforge.event.world.ExplosionEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.world.ExplosionEvent.Start.class) {
                return createExplosionStartEvent(event);
            } else if (clazz == net.minecraftforge.event.world.ExplosionEvent.Detonate.class) {
                return createExplosionDetonateEvent(event);
            } else {
                return createExplosionEvent(event);
            }
        }

        // Server events
        else if (clazz == net.minecraftforge.event.ServerChatEvent.class) {
            return createServerChatEvent(event);
        }

        // return same event if not currently supported
        return (net.minecraftforge.fml.common.eventhandler.Event) event;
    }

    // Block events
    public static net.minecraftforge.event.world.BlockEvent createBlockEvent(Event event) {
        if (!(event instanceof ChangeBlockEvent.SourcePlayer)) {
            throw new IllegalArgumentException("Event is not a valid BlockEvent.");
        }

        ChangeBlockEvent spongeEvent = (ChangeBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraftforge.event.world.BlockEvent forgeEvent = new net.minecraftforge.event.world.BlockEvent(world, pos, world.getBlockState(pos));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.PlaceEvent createBlockPlaceEvent(Event event) {
        if (!(event instanceof PlaceBlockEvent.SourcePlayer)) {
            throw new IllegalArgumentException("Event is not a valid PlaceBlockEvent.");
        }

        PlaceBlockEvent.SourcePlayer spongeEvent = (PlaceBlockEvent.SourcePlayer) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockSnapshot replacementBlock = spongeEvent.getTransactions().get(0).getFinalReplacement();
        IBlockState state = (IBlockState) replacementBlock.getState();
        net.minecraftforge.common.util.BlockSnapshot forgeSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);

        net.minecraftforge.event.world.BlockEvent.PlaceEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.PlaceEvent(forgeSnapshot, world.getBlockState(pos),
                        (EntityPlayer) spongeEvent.getSourceEntity());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.BreakEvent createBlockBreakEvent(Event event) {
        if (!(event instanceof BreakBlockEvent.SourcePlayer)) {
            throw new IllegalArgumentException("Event is not a valid BreakBlockEvent.");
        }

        BreakBlockEvent.SourcePlayer spongeEvent = (BreakBlockEvent.SourcePlayer) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        net.minecraftforge.event.world.BlockEvent.BreakEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.BreakEvent(world, pos, world.getBlockState(pos),
                        (EntityPlayer) spongeEvent.getSourceEntity());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent createBlockHarvestEvent(Event event) {
        if (!(event instanceof HarvestBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid HarvestBlockEvent.");
        }

        HarvestBlockEvent.SourcePlayer spongeEvent = (HarvestBlockEvent.SourcePlayer) event;
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (ItemStack itemstack : spongeEvent.getItemStacks()) {
           // EntityItem entityItem = new EntityItem((net.minecraft.world.World) spongeEvent.getTargetLocation().getExtent(), spongeEvent.getTargetLocation().getBlockX(), spongeEvent.getTargetLocation().getBlockY(), spongeEvent.getTargetLocation().getBlockZ(), (net.minecraft.item.ItemStack) itemstack);
            droppedItems.add((net.minecraft.item.ItemStack) itemstack);
        }

        net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent((net.minecraft.world.World) spongeEvent.getTargetLocation()
                        .getExtent(), VecHelper.toBlockPos(spongeEvent
                        .getTargetLocation()
                        .getBlockPosition()), (net.minecraft.block.state.IBlockState) spongeEvent.getTargetLocation().getBlock(), 0,
                        spongeEvent.getDropChance(), droppedItems, (EntityPlayer) spongeEvent.getSourceEntity(), false);// spongeEvent.isSilkTouchHarvest());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent createBlockNeighborNotifyEvent(Event event) {
        if (!(event instanceof NotifyNeighborBlockEvent.SourceBlock)) {
            throw new IllegalArgumentException("Event " + event.getClass() + " is not a valid NotifyNeighborBlockEvent.");
        }

        NotifyNeighborBlockEvent.SourceBlock spongeEvent = (NotifyNeighborBlockEvent.SourceBlock) event;
        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getRelatives().entrySet()) {
            facings.add(SpongeGameRegistry.directionMap.get(mapEntry.getKey()));
        }

        IBlockState state = (IBlockState) spongeEvent.getSourceBlock().getState();
        BlockPos pos = VecHelper.toBlockPos(spongeEvent.getSourceLocation().getBlockPosition());
        net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getSourceLocation().getExtent();

        final NeighborNotifyEvent forgeEvent = new NeighborNotifyEvent(world, pos, state, facings);
        return forgeEvent;
    }

    // Entity events
    public static net.minecraftforge.event.entity.EntityEvent createEntityEvent(Event event) {
        if (!(event instanceof TargetEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetEntityEvent.");
        }

        TargetEntityEvent spongeEvent = (TargetEntityEvent) event;
        net.minecraftforge.event.entity.EntityEvent forgeEvent =
                new net.minecraftforge.event.entity.EntityEvent((net.minecraft.entity.Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.EntityEvent.EntityConstructing createEntityConstructingEvent(Event event) {
        if (!(event instanceof CreateEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valid CreateEntityEvent.");
        }

        CreateEntityEvent spongeEvent = (CreateEntityEvent) event;
        net.minecraftforge.event.entity.EntityEvent.EntityConstructing forgeEvent =
                new net.minecraftforge.event.entity.EntityEvent.EntityConstructing((net.minecraft.entity.Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.EntityJoinWorldEvent createEntityJoinWorldEvent(Event event) {
        if (!(event instanceof SpawnEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valid SpawnEntityEvent.");
        }

        SpawnEntityEvent spongeEvent = (SpawnEntityEvent) event;
        EntityJoinWorldEvent forgeEvent = new EntityJoinWorldEvent((net.minecraft.entity.Entity) spongeEvent.getTargetEntity(),
                (net.minecraft.world.World) spongeEvent.getTargetTransform().getLocation().getExtent());
        return forgeEvent;
    }

    // Living events
    public static net.minecraftforge.event.entity.living.LivingEvent createLivingEvent(Event event) {
        if (!(event instanceof TargetLivingEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetLivingEvent.");
        }

        TargetLivingEvent spongeEvent = (TargetLivingEvent) event;
        net.minecraftforge.event.entity.living.LivingEvent forgeEvent =
                new net.minecraftforge.event.entity.living.LivingEvent((EntityLivingBase) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    // Player events
    public static net.minecraftforge.event.entity.player.PlayerEvent createPlayerEvent(Event event) {
        if (!(event instanceof PlayerEvent)) {
            throw new IllegalArgumentException("Event is not a valid PlayerEvent.");
        }

        PlayerEvent spongeEvent = (PlayerEvent) event;
        net.minecraftforge.event.entity.player.PlayerEvent forgeEvent =
                new net.minecraftforge.event.entity.player.PlayerEvent((EntityPlayer) spongeEvent.getSourceEntity());
        return forgeEvent;
    }

    private static net.minecraftforge.event.entity.player.PlayerInteractEvent createPlayerInteractEvent(Event event) {
        if (!(event instanceof InteractBlockEvent.SourcePlayer)) {
            throw new IllegalArgumentException("Event is not a valid InteractBlockEvent.SourcePlayer.");
        }

        System.out.println("SpongeForgeEventFactory createPlayerInteractEvent " + event + " on thread " + Thread.currentThread().getName());
        InteractBlockEvent.SourcePlayer spongeEvent = (InteractBlockEvent.SourcePlayer) event;
        BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetLocation().getBlockPosition());
        EnumFacing face = SpongeGameRegistry.directionMap.get(spongeEvent.getTargetSide());
        EntityPlayer player = (EntityPlayer) spongeEvent.getSourceEntity();
        Action action = Action.RIGHT_CLICK_BLOCK;
        if (player.isUsingItem()) {
            action = Action.LEFT_CLICK_BLOCK;
        } else if (player.worldObj.isAirBlock(pos)) {
            action = Action.RIGHT_CLICK_AIR;
        }

        net.minecraftforge.event.entity.player.PlayerInteractEvent forgeEvent =
                new net.minecraftforge.event.entity.player.PlayerInteractEvent((EntityPlayer) spongeEvent.getSourceEntity(), action, pos, face,
                        (net.minecraft.world.World) spongeEvent.getSourceEntity().getWorld());
        return forgeEvent;
    }

    // Item events
    public static net.minecraftforge.event.entity.item.ItemEvent createItemEvent(Event event) {
        if (!(event instanceof TargetItemEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetItemEvent.");
        }

        TargetItemEvent spongeEvent = (TargetItemEvent) event;
        net.minecraftforge.event.entity.item.ItemEvent forgeEvent =
                new net.minecraftforge.event.entity.item.ItemEvent((EntityItem) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    // World events
    public static net.minecraftforge.event.world.WorldEvent createWorldEvent(Event event) {
        if (!(event instanceof WorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid WorldEvent.");
        }

        WorldEvent spongeEvent = (WorldEvent) event;
        net.minecraftforge.event.world.WorldEvent forgeEvent =
                new net.minecraftforge.event.world.WorldEvent((net.minecraft.world.World) spongeEvent.getSourceWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.WorldEvent.Load createWorldLoadEvent(Event event) {
        if (!(event instanceof ServerLoadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid ServerLoadWorldEvent.");
        }

        ServerLoadWorldEvent spongeEvent = (ServerLoadWorldEvent) event;
        net.minecraftforge.event.world.WorldEvent.Load forgeEvent =
                new net.minecraftforge.event.world.WorldEvent.Load((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.WorldEvent.Unload createWorldUnloadEvent(Event event) {
        if (!(event instanceof ServerUnloadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid ServerUnloadWorldEvent.");
        }

        ServerUnloadWorldEvent spongeEvent = (ServerUnloadWorldEvent) event;
        net.minecraftforge.event.world.WorldEvent.Unload forgeEvent =
                new net.minecraftforge.event.world.WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent createChunkEvent(Event event) {
        if (!(event instanceof ChangeChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid ChangeChunkEvent.");
        }

        ChangeChunkEvent spongeEvent = (ChangeChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent.Load createChunkLoadEvent(Event event) {
        if (!(event instanceof WorldLoadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid WorldLoadChunkEvent.");
        }

        WorldLoadChunkEvent spongeEvent = (WorldLoadChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent.Load forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent.Load(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent.Unload createChunkUnloadEvent(Event event) {
        if (!(event instanceof WorldUnloadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid WorldUnloadChunkEvent.");
        }

        WorldUnloadChunkEvent spongeEvent = (WorldUnloadChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent.Unload forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent.Unload(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    // Explosion events
    public static net.minecraftforge.event.world.ExplosionEvent createExplosionEvent(Event event) {
        if (!(event instanceof WorldExplosionEvent)) {
            throw new IllegalArgumentException("Event is not a valid WorldExplosionEvent.");
        }

        WorldExplosionEvent spongeEvent = (WorldExplosionEvent) event;
        net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getSourceWorld();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent forgeEvent = new net.minecraftforge.event.world.ExplosionEvent(world, explosion);
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ExplosionEvent.Start createExplosionStartEvent(Event event) {
        if (!(event instanceof WorldExplosionEvent.Pre)) {
            throw new IllegalArgumentException("Event is not a valid WorldExplosionEvent.Pre.");
        }

        WorldExplosionEvent.Pre spongeEvent = (WorldExplosionEvent.Pre) event;
        net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getSourceWorld();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Start(world, explosion);
        return forgeEvent;
    }

    @SuppressWarnings("unchecked")
    public static net.minecraftforge.event.world.ExplosionEvent.Detonate createExplosionDetonateEvent(Event event) {
        if (!(event instanceof WorldExplosionEvent.OnExplosion)) {
            throw new IllegalArgumentException("Event is not a valid WorldExplosionEvent.OnExplosion.");
        }

        WorldExplosionEvent.OnExplosion spongeEvent = (WorldExplosionEvent.OnExplosion) event;
        net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getSourceWorld();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Detonate(world, explosion,
                        (List<net.minecraft.entity.Entity>) spongeEvent.getEntities());
        return forgeEvent;
    }

    // Server events
    private static net.minecraftforge.event.ServerChatEvent createServerChatEvent(Event event) {
        if (!(event instanceof PlayerChatEvent)) {
            throw new IllegalArgumentException("Event is not a valid PlayerChatEvent.");
        }

        PlayerChatEvent spongeEvent = (PlayerChatEvent) event;
        IChatComponent component = SpongeTexts.toComponent(spongeEvent.getMessage(), spongeEvent.getSourceEntity().getLocale());
        if (!(component instanceof ChatComponentTranslation)) {
            component = new ChatComponentTranslation("%s", component);
        }

        // Using toPlain here is fine, since the raw message from the client can't have formatting.
        net.minecraftforge.event.ServerChatEvent forgeEvent =
                new net.minecraftforge.event.ServerChatEvent((EntityPlayerMP) spongeEvent.getSourceEntity(), Texts.toPlain(spongeEvent.getMessage()),
                        (ChatComponentTranslation) component);
        return forgeEvent;
    }
}
