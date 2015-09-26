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

import com.google.common.base.Optional;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.BreakBlockEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.HarvestBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.PlaceBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.entity.item.TargetItemEvent;
import org.spongepowered.api.event.entity.living.TargetLivingEvent;
import org.spongepowered.api.event.inventory.UseItemStackEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
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

            } else if (clazz == net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start.class) {
                return createPlayerUseItemStartEvent(event);
            } else if (clazz == net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick.class) {
                return createPlayerUseItemTickEvent(event);
            } else if (clazz == net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop.class) {
                return createPlayerUseItemStopEvent(event);
            } else if (clazz == net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish.class) {
                return createPlayerUseItemFinishEvent(event);
            } else if (clazz == net.minecraftforge.event.entity.player.PlayerWakeUpEvent.class) {

            } else {
                return (net.minecraftforge.fml.common.eventhandler.Event) event;
            }
        }

        // Living events
        else if (net.minecraftforge.event.entity.living.LivingEvent.class.isAssignableFrom(clazz)) {
            if (clazz == net.minecraftforge.event.entity.living.LivingAttackEvent.class) {

            } else if (clazz == net.minecraftforge.event.entity.living.LivingDeathEvent.class) {
                return createLivingDeathEvent(event);
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
        if (!(event instanceof ChangeBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid ChangeBlockEvent.");
        }

        ChangeBlockEvent spongeEvent = (ChangeBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraftforge.event.world.BlockEvent forgeEvent = new net.minecraftforge.event.world.BlockEvent(world, pos, world.getBlockState(pos));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.PlaceEvent createBlockPlaceEvent(Event event) {
        if (!(event instanceof PlaceBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid PlaceBlockEvent.");
        }

        PlaceBlockEvent spongeEvent = (PlaceBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockSnapshot replacementBlock = spongeEvent.getTransactions().get(0).getFinalReplacement();
        IBlockState state = (IBlockState) replacementBlock.getState();
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraftforge.common.util.BlockSnapshot forgeSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
        net.minecraftforge.event.world.BlockEvent.PlaceEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.PlaceEvent(forgeSnapshot, world.getBlockState(pos),
                        (EntityPlayer) player.get());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.BreakEvent createBlockBreakEvent(Event event) {
        if (!(event instanceof BreakBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid BreakBlockEvent.");
        }

        BreakBlockEvent spongeEvent = (BreakBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraftforge.event.world.BlockEvent.BreakEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.BreakEvent(world, pos, world.getBlockState(pos),
                        (EntityPlayer) player.get());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent createBlockHarvestEvent(Event event) {
        if (!(event instanceof HarvestBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid HarvestBlockEvent.");
        }

        HarvestBlockEvent spongeEvent = (HarvestBlockEvent) event;
        List<net.minecraft.item.ItemStack> droppedItems = new ArrayList<net.minecraft.item.ItemStack>();
        for (ItemStack itemstack : spongeEvent.getItemStacks()) {
           // EntityItem entityItem = new EntityItem((net.minecraft.world.World) spongeEvent.getTargetLocation().getExtent(), spongeEvent.getTargetLocation().getBlockX(), spongeEvent.getTargetLocation().getBlockY(), spongeEvent.getTargetLocation().getBlockZ(), (net.minecraft.item.ItemStack) itemstack);
            droppedItems.add((net.minecraft.item.ItemStack) itemstack);
        }
        Optional<Player> player = spongeEvent.getCause().first(Player.class);

        Location<World> location = spongeEvent.getTargetBlock().getLocation().get();
        net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent forgeEvent =
                new net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent((net.minecraft.world.World) location.getExtent(), VecHelper.toBlockPos(location.getPosition()), (net.minecraft.block.state.IBlockState) location.getBlock(), 0,
                        spongeEvent.getDropChance(), droppedItems, player.isPresent() ? (EntityPlayer) player.get() : null, false);// spongeEvent.isSilkTouchHarvest());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent createBlockNeighborNotifyEvent(Event event) {
        if (!(event instanceof NotifyNeighborBlockEvent)) {
            throw new IllegalArgumentException("Event " + event.getClass() + " is not a valid NotifyNeighborBlockEvent.");
        }

        NotifyNeighborBlockEvent spongeEvent = (NotifyNeighborBlockEvent) event;
        Optional<BlockSnapshot> blockSnapshot = spongeEvent.getCause().first(BlockSnapshot.class);
        if (!blockSnapshot.isPresent() || !blockSnapshot.get().getLocation().isPresent()) {
            return null;
        }

        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Map.Entry<Direction, Location<World>> mapEntry : spongeEvent.getRelatives().entrySet()) {
            facings.add(SpongeGameRegistry.directionMap.get(mapEntry.getKey()));
        }

        IBlockState state = (IBlockState) blockSnapshot.get().getState();
        BlockPos pos = VecHelper.toBlockPos(blockSnapshot.get().getLocation().get().getBlockPosition());
        net.minecraft.world.World world = (net.minecraft.world.World) blockSnapshot.get().getLocation().get().getExtent();

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
        if (!(event instanceof ConstructEntityEvent.Post)) {
            throw new IllegalArgumentException("Event is not a valid ConstructEntityEvent.");
        }

        ConstructEntityEvent.Post spongeEvent = (ConstructEntityEvent.Post) event;
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
                (net.minecraft.world.World) spongeEvent.getTargetEntity().getLocation().getExtent());
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

    public static net.minecraftforge.event.entity.living.LivingDeathEvent createLivingDeathEvent(Event event) {
        if (!(event instanceof DestructEntityEvent.Death)) {
            throw new IllegalArgumentException("Event is not a valid DestructEntityEvent.Death event.");
        }

        DestructEntityEvent.Death spongeEvent = (DestructEntityEvent.Death) event;
        Optional<DamageSource> source = spongeEvent.getCause().first(DamageSource.class);
        if (!source.isPresent()) {
            return null;
        }

        net.minecraftforge.event.entity.living.LivingDeathEvent forgeEvent =
                new net.minecraftforge.event.entity.living.LivingDeathEvent((EntityLivingBase) spongeEvent.getTargetEntity(), (net.minecraft.util.DamageSource) source.get());
        return forgeEvent;
    }

    // Player events
    /*public static net.minecraftforge.event.entity.player.PlayerEvent createPlayerEvent(Event event) {
        if (!(event instanceof TargetPlayerEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetPlayerEvent.");
        }

        TargetPlayerEvent spongeEvent = (TargetPlayerEvent) event;
        net.minecraftforge.event.entity.player.PlayerEvent forgeEvent =
                new net.minecraftforge.event.entity.player.PlayerEvent((EntityPlayer) spongeEvent.getTargetEntity());
        return forgeEvent;
    }*/

    private static net.minecraftforge.event.entity.player.PlayerInteractEvent createPlayerInteractEvent(Event event) {
        if (!(event instanceof InteractBlockEvent)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid InteractBlockEvent.");
        }

        InteractBlockEvent spongeEvent = (InteractBlockEvent) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetBlock().getLocation().get().getPosition());
        EnumFacing face = SpongeGameRegistry.directionMap.get(spongeEvent.getTargetSide());
        EntityPlayer entityplayer = (EntityPlayer) player.get();
        Action action = Action.RIGHT_CLICK_BLOCK;
        if (entityplayer.isUsingItem()) {
            action = Action.LEFT_CLICK_BLOCK;
        } else if (entityplayer.worldObj.isAirBlock(pos)) {
            action = Action.RIGHT_CLICK_AIR;
        }

        net.minecraftforge.event.entity.player.PlayerInteractEvent forgeEvent =
                new net.minecraftforge.event.entity.player.PlayerInteractEvent((EntityPlayer) player.get(), action, pos, face,
                        (net.minecraft.world.World) player.get().getWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start createPlayerUseItemStartEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Start)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Start event.");
        }

        UseItemStackEvent.Start spongeEvent = (UseItemStackEvent.Start) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) spongeEvent.getItemStackInUse().getFinalSnapshot().createStack();
        net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start forgeEvent = new net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick createPlayerUseItemTickEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Tick)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Tick event.");
        }

        UseItemStackEvent.Tick spongeEvent = (UseItemStackEvent.Tick) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) spongeEvent.getItemStackInUse().getFinalSnapshot().createStack();
        net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick forgeEvent = new net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop createPlayerUseItemStopEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Stop)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Stop event.");
        }

        UseItemStackEvent.Stop spongeEvent = (UseItemStackEvent.Stop) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) spongeEvent.getItemStackInUse().getFinalSnapshot().createStack();
        net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop forgeEvent = new net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish createPlayerUseItemFinishEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Finish)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Finish event.");
        }

        UseItemStackEvent.Finish spongeEvent = (UseItemStackEvent.Finish) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) spongeEvent.getItemStackInUse().getFinalSnapshot().createStack();
        net.minecraft.item.ItemStack resultItemStack = (net.minecraft.item.ItemStack) spongeEvent.getItemStackResult().getFinalSnapshot().createStack();
        net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish forgeEvent = new net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration(), resultItemStack);
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
        if (!(event instanceof TargetWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetWorldEvent.");
        }

        TargetWorldEvent spongeEvent = (TargetWorldEvent) event;
        net.minecraftforge.event.world.WorldEvent forgeEvent =
                new net.minecraftforge.event.world.WorldEvent((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.WorldEvent.Load createWorldLoadEvent(Event event) {
        if (!(event instanceof LoadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid ServerLoadWorldEvent.");
        }

        LoadWorldEvent spongeEvent = (LoadWorldEvent) event;
        net.minecraftforge.event.world.WorldEvent.Load forgeEvent =
                new net.minecraftforge.event.world.WorldEvent.Load((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.WorldEvent.Unload createWorldUnloadEvent(Event event) {
        if (!(event instanceof UnloadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid ServerUnloadWorldEvent.");
        }

        UnloadWorldEvent spongeEvent = (UnloadWorldEvent) event;
        net.minecraftforge.event.world.WorldEvent.Unload forgeEvent =
                new net.minecraftforge.event.world.WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent createChunkEvent(Event event) {
        if (!(event instanceof TargetChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetChunkEvent.");
        }

        TargetChunkEvent spongeEvent = (TargetChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent.Load createChunkLoadEvent(Event event) {
        if (!(event instanceof LoadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid LoadChunkEvent.");
        }

        LoadChunkEvent spongeEvent = (LoadChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent.Load forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent.Load(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ChunkEvent.Unload createChunkUnloadEvent(Event event) {
        if (!(event instanceof UnloadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid UnloadChunkEvent.");
        }

        UnloadChunkEvent spongeEvent = (UnloadChunkEvent) event;
        net.minecraftforge.event.world.ChunkEvent.Unload forgeEvent =
                new net.minecraftforge.event.world.ChunkEvent.Unload(((net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    // Explosion events
    public static net.minecraftforge.event.world.ExplosionEvent createExplosionEvent(Event event) {
        if (!(event instanceof ExplosionEvent)) {
            throw new IllegalArgumentException("Event is not a valid ExplosionEvent.");
        }

        ExplosionEvent spongeEvent = (ExplosionEvent) event;
        Optional<World> world = spongeEvent.getCause().first(World.class);
        if (!world.isPresent()) {
            return null;
        }

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) world.get();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent forgeEvent = new net.minecraftforge.event.world.ExplosionEvent(forgeWorld, explosion);
        return forgeEvent;
    }

    public static net.minecraftforge.event.world.ExplosionEvent.Start createExplosionStartEvent(Event event) {
        if (!(event instanceof ExplosionEvent.Pre)) {
            throw new IllegalArgumentException("Event is not a valid ExplosionEvent.Pre.");
        }

        ExplosionEvent.Pre spongeEvent = (ExplosionEvent.Pre) event;
        Optional<World> world = spongeEvent.getCause().first(World.class);
        if (!world.isPresent()) {
            return null;
        }

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) world.get();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Start(forgeWorld, explosion);
        return forgeEvent;
    }

    @SuppressWarnings("unchecked")
    public static net.minecraftforge.event.world.ExplosionEvent.Detonate createExplosionDetonateEvent(Event event) {
        if (!(event instanceof ExplosionEvent.Detonate)) {
            throw new IllegalArgumentException("Event is not a valid ExplosionEvent.Detonate.");
        }

        ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) event;
        Optional<World> world = spongeEvent.getCause().first(World.class);
        if (!world.isPresent()) {
            return null;
        }

        net.minecraft.world.World forgeWorld = (net.minecraft.world.World) world.get();
        net.minecraft.world.Explosion explosion = (net.minecraft.world.Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Detonate(forgeWorld, explosion,
                        (List<net.minecraft.entity.Entity>) (Object) spongeEvent.getEntities());
        return forgeEvent;
    }

    // Server events
    private static net.minecraftforge.event.ServerChatEvent createServerChatEvent(Event event) {
        if (!(event instanceof MessageSinkEvent.Chat)) {
            throw new IllegalArgumentException("Event is not a valid MessageSinkEvent.");
        }

        MessageSinkEvent.Chat spongeEvent = (MessageSinkEvent.Chat) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        IChatComponent component = SpongeTexts.toComponent(spongeEvent.getOriginalMessage(), player.get().getLocale());
        if (!(component instanceof ChatComponentTranslation)) {
            component = new ChatComponentTranslation("%s", component);
        }

        // Using toPlain here is fine, since the raw message from the client can't have formatting.
        net.minecraftforge.event.ServerChatEvent forgeEvent =
                new net.minecraftforge.event.ServerChatEvent((EntityPlayerMP) player.get(), Texts.toPlain(spongeEvent.getOriginalMessage()),
                        (ChatComponentTranslation) component);
        return forgeEvent;
    }


    // Special handling before Forge events post
    @SuppressWarnings("unchecked")
    public static void onForgePost(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Detonate) {
            net.minecraftforge.event.world.ExplosionEvent.Detonate explosionEvent = (net.minecraftforge.event.world.ExplosionEvent.Detonate) forgeEvent;
            if (!explosionEvent.explosion.isSmoking) { // shouldBreakBlocks
                List<BlockPos> affectedBlocks = explosionEvent.explosion.func_180343_e();
                affectedBlocks.clear();
            }
        } else if (forgeEvent instanceof net.minecraftforge.event.entity.living.LivingDeathEvent) {
            MessageSinkEvent spongeEvent = (MessageSinkEvent) forgeEvent;
            Text returned = Texts.format(spongeEvent.getMessage());
            spongeEvent.getSink().sendMessage(returned);
        }
    }
}
