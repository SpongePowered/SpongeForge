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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingPackSizeEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.entity.item.TargetItemEvent;
import org.spongepowered.api.event.entity.living.TargetLivingEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.IMixinInitCause;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinEventBus;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeForgeEventFactory {

    public static net.minecraftforge.fml.common.eventhandler.Event findAndCreateForgeEvent(Event event,
            Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {

        // Block events
        if (BlockEvent.class.isAssignableFrom(clazz)) {
            if (clazz == BlockEvent.NeighborNotifyEvent.class) {
                return createBlockNeighborNotifyEvent(event);
            } else if (clazz == BlockEvent.HarvestDropsEvent.class) {
                // return createBlockHarvestEvent(event);
            } else if (clazz == BlockEvent.MultiPlaceEvent.class ||
                    clazz == BlockEvent.PlaceEvent.class) {
                return createBlockPlaceEvent(event);
            } else {
                return createBlockEvent(event);
            }
        }

        // Player events
        else if (PlayerEvent.class.isAssignableFrom(clazz)) {
            if (clazz == AchievementEvent.class) {

            } else if (clazz == AnvilRepairEvent.class) {

            } else if (clazz == ArrowLooseEvent.class) {

            } else if (clazz == ArrowNockEvent.class) {

            } else if (clazz == AttackEntityEvent.class) {
                return createAttackEntityEvent(event);
            } else if (clazz == BonemealEvent.class) {

            } else if (clazz == EntityInteractEvent.class) {
                return createEntityInteractEvent(event);
            } else if (clazz == EntityItemPickupEvent.class) {

            } else if (clazz == FillBucketEvent.class) {

            } else if (clazz == ItemTooltipEvent.class) {

            } else if (clazz == PlayerDropsEvent.class) {

            } else if (clazz == PlayerDestroyItemEvent.class) {

            } else if (clazz == PlayerFlyableFallEvent.class) {

            } else if (clazz == PlayerInteractEvent.class) {
                return createPlayerInteractEvent(event);
            } else if (clazz == PlayerOpenContainerEvent.class) {

            } else if (clazz == PlayerPickupXpEvent.class) {

            } else if (clazz == PlayerSleepInBedEvent.class) {
                return createPlayerSleepInBedEvent(event);
            } else if (clazz == PlayerUseItemEvent.Start.class) {
                return createPlayerUseItemStartEvent(event);
            } else if (clazz == PlayerUseItemEvent.Tick.class) {
                return createPlayerUseItemTickEvent(event);
            } else if (clazz == PlayerUseItemEvent.Stop.class) {
                return createPlayerUseItemStopEvent(event);
            } else if (clazz == PlayerUseItemEvent.Finish.class) {
                return createPlayerUseItemFinishEvent(event);
            } else {
                return (net.minecraftforge.fml.common.eventhandler.Event) event;
            }
        }

        // Living events
        else if (LivingEvent.class.isAssignableFrom(clazz)) {
            if (clazz == LivingAttackEvent.class) {

            } else if (clazz == LivingDeathEvent.class) {
                return createLivingDeathEvent(event);
            } else if (clazz == LivingDropsEvent.class) {
                return createLivingDropsEvent(event);
            } else if (clazz == LivingExperienceDropEvent.class) {

            } else if (clazz == LivingFallEvent.class) {

            } else if (clazz == LivingHealEvent.class) {

            } else if (clazz == LivingHurtEvent.class) {

            } else if (clazz == LivingPackSizeEvent.class) {

            } else if (clazz == LivingSetAttackTargetEvent.class) {

            } else if (clazz == LivingSpawnEvent.class) {

            } else {
                return createLivingEvent(event);
            }
        }

        // Item events - need to come before EntityEvent filtering because of
        // class hierarchies.
        else if (ItemEvent.class.isAssignableFrom(clazz)) {
            if (clazz == ItemExpireEvent.class) {

            } else if (clazz == ItemTossEvent.class) {
                return createItemTossEvent(event);
            } else {
                return createItemEvent(event);
            }
        }

        // Entity events
        else if (EntityEvent.class.isAssignableFrom(clazz)) {
            if (clazz == EntityEvent.EntityConstructing.class) {
                return createEntityConstructingEvent(event);
            } else if (clazz == EntityMountEvent.class) {

            } else if (clazz == EntityStruckByLightningEvent.class) {

            } else {
                return createEntityEvent(event);
            }
        }

        // World events
        else if (WorldEvent.class.isAssignableFrom(clazz)) {
            if (ChunkEvent.class.isAssignableFrom(clazz)) {
                if (clazz == ChunkEvent.Load.class) {
                    return createChunkLoadEvent(event);
                } else if (clazz == ChunkEvent.Unload.class) {
                    return createChunkUnloadEvent(event);
                } else if (clazz == ChunkDataEvent.Load.class) {

                } else if (clazz == ChunkDataEvent.Save.class) {

                } else if (clazz == ChunkWatchEvent.UnWatch.class) {

                } else if (clazz == ChunkWatchEvent.Watch.class) {

                }
                return createChunkEvent(event);
            } else if (clazz == WorldEvent.Load.class) {
                return createWorldLoadEvent(event);
            } else if (clazz == WorldEvent.Unload.class) {
                return createWorldUnloadEvent(event);
            } else if (clazz == WorldEvent.Save.class) {
                return createWorldSaveEvent(event);
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
        else if (clazz == ServerChatEvent.class) {
            return createServerChatEvent(event);
        }

        // return same event if not currently supported
        return (net.minecraftforge.fml.common.eventhandler.Event) event;
    }

    // Used for firing single events to Forge from sponge bulk events
    public static Event callForgeEvent(Event spongeEvent, Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz) {
        if (EntityItemPickupEvent.class.isAssignableFrom(clazz)) {
            return callEntityItemPickupEvent(spongeEvent);
        } else if (EntityJoinWorldEvent.class.isAssignableFrom(clazz)) {
            return callEntityJoinWorldEvent(spongeEvent);
        } else if (BlockEvent.BreakEvent.class.isAssignableFrom(clazz)) {
            return callBlockBreakEvent(spongeEvent);
        } else if (BlockEvent.PlaceEvent.class.isAssignableFrom(clazz)) {
            return callBlockPlaceEvent(spongeEvent);
        }
        return spongeEvent;
    }

    // Block events
    public static BlockEvent createBlockEvent(Event event) {
        if (!(event instanceof ChangeBlockEvent)) {
            throw new IllegalArgumentException("Event is not a valid ChangeBlockEvent.");
        }

        ChangeBlockEvent spongeEvent = (ChangeBlockEvent) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockEvent forgeEvent = new BlockEvent(world, pos, world.getBlockState(pos));
        return forgeEvent;
    }

    public static BlockEvent.PlaceEvent createBlockPlaceEvent(Event event) {
        if (!(event instanceof ChangeBlockEvent.Place)) {
            throw new IllegalArgumentException("Event is not a valid ChangeBlockEvent.Place event.");
        }

        ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) event;
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockSnapshot replacementBlock = spongeEvent.getTransactions().get(0).getFinal();
        IBlockState state = (IBlockState) replacementBlock.getState();
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraftforge.common.util.BlockSnapshot forgeSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
        BlockEvent.PlaceEvent forgeEvent =
                new BlockEvent.PlaceEvent(forgeSnapshot, world.getBlockState(pos),
                        (EntityPlayer) player.get());
        return forgeEvent;
    }

    public static BlockEvent.NeighborNotifyEvent createBlockNeighborNotifyEvent(Event event) {
        if (!(event instanceof NotifyNeighborBlockEvent)) {
            throw new IllegalArgumentException("Event " + event.getClass() + " is not a valid NotifyNeighborBlockEvent.");
        }

        NotifyNeighborBlockEvent spongeEvent = (NotifyNeighborBlockEvent) event;
        Optional<BlockSnapshot> blockSnapshot = spongeEvent.getCause().first(BlockSnapshot.class);
        if (!blockSnapshot.isPresent() || !blockSnapshot.get().getLocation().isPresent()) {
            return null;
        }

        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getNeighbors().entrySet()) {
            facings.add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
        }

        IBlockState state = (IBlockState) blockSnapshot.get().getState();
        BlockPos pos = VecHelper.toBlockPos(blockSnapshot.get().getLocation().get());
        net.minecraft.world.World world = (net.minecraft.world.World) blockSnapshot.get().getLocation().get().getExtent();

        final NeighborNotifyEvent forgeEvent = new NeighborNotifyEvent(world, pos, state, facings);
        return forgeEvent;
    }

    // Entity events
    public static EntityEvent createEntityEvent(Event event) {
        if (!(event instanceof TargetEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetEntityEvent.");
        }

        TargetEntityEvent spongeEvent = (TargetEntityEvent) event;
        EntityEvent forgeEvent =
                new EntityEvent((Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static EntityEvent.EntityConstructing createEntityConstructingEvent(Event event) {
        if (!(event instanceof ConstructEntityEvent.Post)) {
            throw new IllegalArgumentException("Event is not a valid ConstructEntityEvent.");
        }

        ConstructEntityEvent.Post spongeEvent = (ConstructEntityEvent.Post) event;
        EntityEvent.EntityConstructing forgeEvent =
                new EntityEvent.EntityConstructing((Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static AttackEntityEvent createAttackEntityEvent(Event event) {
        if (!(event instanceof InteractEntityEvent.Primary)) {
            throw new IllegalArgumentException("Event is not a valid InteractEntityEvent.Primary event.");
        }

        InteractEntityEvent.Primary spongeEvent = (InteractEntityEvent.Primary) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        AttackEntityEvent forgeEvent = new AttackEntityEvent((EntityPlayer) player.get(), (net.minecraft.entity.Entity) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    // Living events
    public static LivingEvent createLivingEvent(Event event) {
        if (!(event instanceof TargetLivingEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetLivingEvent.");
        }

        TargetLivingEvent spongeEvent = (TargetLivingEvent) event;
        LivingEvent forgeEvent =
                new LivingEvent((EntityLivingBase) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static LivingDeathEvent createLivingDeathEvent(Event event) {
        if (!(event instanceof DestructEntityEvent.Death)) {
            throw new IllegalArgumentException("Event is not a valid DestructEntityEvent.Death event.");
        }

        DestructEntityEvent.Death spongeEvent = (DestructEntityEvent.Death) event;
        Optional<DamageSource> source = spongeEvent.getCause().first(DamageSource.class);
        if (!source.isPresent()) {
            return null;
        }

        LivingDeathEvent forgeEvent =
                new LivingDeathEvent((EntityLivingBase) spongeEvent.getTargetEntity(), (net.minecraft.util.DamageSource) source.get());
        return forgeEvent;
    }

    public static LivingDropsEvent createLivingDropsEvent(Event event) {
        if (!(event instanceof DropItemEvent.Destruct)) {
            throw new IllegalArgumentException("Event is not a valid DropItemEvent.Destruct.");
        }

        DropItemEvent.Destruct spongeEvent = (DropItemEvent.Destruct) event;
        Object source = spongeEvent.getCause().root();
        Optional<DamageSource> damageSource = spongeEvent.getCause().first(DamageSource.class);
        if (!(source instanceof Living) || !damageSource.isPresent() || spongeEvent.getEntities().size() <= 0) {
            return null;
        }

        ArrayList<EntityItem> itemDrops = new ArrayList<>();
        for (org.spongepowered.api.entity.Entity entity : spongeEvent.getEntities()) {
            itemDrops.add((EntityItem) entity);
        }

        int lootingLevel = 0;

        if (source instanceof EntityPlayer) {
            lootingLevel = EnchantmentHelper.getLootingModifier((EntityLivingBase)source);
        }

        LivingDropsEvent forgeEvent =
                new LivingDropsEvent((EntityLivingBase) source, (net.minecraft.util.DamageSource) damageSource.get(), itemDrops, lootingLevel,
                        ((IMixinEntityLivingBase) source).getRecentlyHit() > 0);
        return forgeEvent;
    }

    // Player events

    private static EntityInteractEvent createEntityInteractEvent(Event event) {
        if (!(event instanceof InteractEntityEvent.Secondary)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid InteractEntityEvent.");
        }

        InteractEntityEvent.Secondary spongeEvent = (InteractEntityEvent.Secondary) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        final EntityPlayer entityPlayer = (EntityPlayer) player.get();
        final Entity entity = (Entity) spongeEvent.getTargetEntity();

        EntityInteractEvent forgeEvent = new EntityInteractEvent(entityPlayer, entity);
        return forgeEvent;
    }

    private static PlayerInteractEvent createPlayerInteractEvent(Event event) {
        if (!(event instanceof InteractBlockEvent)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid InteractBlockEvent.");
        }

        InteractBlockEvent spongeEvent = (InteractBlockEvent) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetBlock().getLocation().get().getPosition());
        Optional<EnumFacing> face = DirectionFacingProvider.getInstance().get(spongeEvent.getTargetSide());
        Action action = null;
        if (spongeEvent instanceof InteractBlockEvent.Primary) {
            action = Action.LEFT_CLICK_BLOCK;
        } else if (spongeEvent instanceof InteractBlockEvent.Secondary) {
            if (spongeEvent.getTargetBlock().getState().getType() == BlockTypes.AIR) {
                action = Action.RIGHT_CLICK_AIR;
            } else {
                action = Action.RIGHT_CLICK_BLOCK;
            }
        } else { // attempt to determine action
            EntityPlayer entityplayer = (EntityPlayer) player.get();
            action = Action.RIGHT_CLICK_BLOCK;
            if (entityplayer.isUsingItem()) {
                action = Action.LEFT_CLICK_BLOCK;
            } else if (entityplayer.worldObj.isAirBlock(pos)) {
                action = Action.RIGHT_CLICK_AIR;
            }
        }

        PlayerInteractEvent forgeEvent =
                new PlayerInteractEvent((EntityPlayer) player.get(), action, pos, face.isPresent() ? face.get() : null,
                        (net.minecraft.world.World) player.get().getWorld());
        return forgeEvent;
    }

    public static PlayerSleepInBedEvent createPlayerSleepInBedEvent(Event event) {
        if (!(event instanceof SleepingEvent.Pre)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid SleepingEvent.Pre event.");
        }

        SleepingEvent.Pre spongeEvent = (SleepingEvent.Pre) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }
        Location<World> location = spongeEvent.getBed().getLocation().get();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return new PlayerSleepInBedEvent((EntityPlayer) player.get(), pos);
    }

    public static PlayerUseItemEvent.Start createPlayerUseItemStartEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Start)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Start event.");
        }

        UseItemStackEvent.Start spongeEvent = (UseItemStackEvent.Start) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().getFinal().createStack();
        PlayerUseItemEvent.Start forgeEvent =
                new PlayerUseItemEvent.Start((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static PlayerUseItemEvent.Tick createPlayerUseItemTickEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Tick)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Tick event.");
        }

        UseItemStackEvent.Tick spongeEvent = (UseItemStackEvent.Tick) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().getFinal().createStack();
        PlayerUseItemEvent.Tick forgeEvent = new PlayerUseItemEvent.Tick((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static PlayerUseItemEvent.Stop createPlayerUseItemStopEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Stop)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Stop event.");
        }

        UseItemStackEvent.Stop spongeEvent = (UseItemStackEvent.Stop) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().getFinal().createStack();
        PlayerUseItemEvent.Stop forgeEvent = new PlayerUseItemEvent.Stop((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration());
        return forgeEvent;
    }

    public static PlayerUseItemEvent.Finish createPlayerUseItemFinishEvent(Event event) {
        if (!(event instanceof UseItemStackEvent.Finish)) {
            throw new IllegalArgumentException("Event " + event + " is not a valid UseItemStackEvent.Finish event.");
        }

        UseItemStackEvent.Finish spongeEvent = (UseItemStackEvent.Finish) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        net.minecraft.item.ItemStack itemstack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackInUse().getFinal().createStack();
        net.minecraft.item.ItemStack resultItemStack = (net.minecraft.item.ItemStack) (Object) spongeEvent.getItemStackResult().getFinal().createStack();
        PlayerUseItemEvent.Finish forgeEvent =
                new PlayerUseItemEvent.Finish((EntityPlayer) player.get(), itemstack, spongeEvent.getRemainingDuration(), resultItemStack);
        return forgeEvent;
    }

    // Item events
    public static ItemEvent createItemEvent(Event event) {
        if (!(event instanceof TargetItemEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetItemEvent.");
        }

        TargetItemEvent spongeEvent = (TargetItemEvent) event;
        ItemEvent forgeEvent =
                new ItemEvent((EntityItem) spongeEvent.getTargetEntity());
        return forgeEvent;
    }

    public static ItemTossEvent createItemTossEvent(Event event) {
        if (!(event instanceof DropItemEvent.Dispense)) {
            throw new IllegalArgumentException("Event is not a valid DropItemEvent.Dispense event.");
        }

        if (!(event.getCause().root() instanceof Player)) {
            return null;
        }

        DropItemEvent.Dispense spongeEvent = (DropItemEvent.Dispense) event;
        ItemTossEvent forgeEvent = new ItemTossEvent((EntityItem) spongeEvent.getEntities().get(0), (EntityPlayerMP) event.getCause().root());
        return forgeEvent;
    }

    // World events
    public static WorldEvent createWorldEvent(Event event) {
        if (!(event instanceof TargetWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetWorldEvent.");
        }

        TargetWorldEvent spongeEvent = (TargetWorldEvent) event;
        WorldEvent forgeEvent =
                new WorldEvent((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static WorldEvent.Load createWorldLoadEvent(Event event) {
        if (!(event instanceof LoadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid LoadWorldEvent.");
        }

        LoadWorldEvent spongeEvent = (LoadWorldEvent) event;
        WorldEvent.Load forgeEvent =
                new WorldEvent.Load((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static WorldEvent.Save createWorldSaveEvent(Event event) {
        if (!(event instanceof SaveWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid SaveWorldEvent.");
        }

        SaveWorldEvent spongeEvent = (SaveWorldEvent) event;
        WorldEvent.Save forgeEvent =
                new WorldEvent.Save((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static WorldEvent.Unload createWorldUnloadEvent(Event event) {
        if (!(event instanceof UnloadWorldEvent)) {
            throw new IllegalArgumentException("Event is not a valid UnloadWorldEvent.");
        }

        UnloadWorldEvent spongeEvent = (UnloadWorldEvent) event;
        WorldEvent.Unload forgeEvent =
                new WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld());
        return forgeEvent;
    }

    public static ChunkEvent createChunkEvent(Event event) {
        if (!(event instanceof TargetChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid TargetChunkEvent.");
        }

        TargetChunkEvent spongeEvent = (TargetChunkEvent) event;
        ChunkEvent forgeEvent =
                new ChunkEvent(((Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static ChunkEvent.Load createChunkLoadEvent(Event event) {
        if (!(event instanceof LoadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid LoadChunkEvent.");
        }

        LoadChunkEvent spongeEvent = (LoadChunkEvent) event;
        ChunkEvent.Load forgeEvent =
                new ChunkEvent.Load(((Chunk) spongeEvent.getTargetChunk()));
        return forgeEvent;
    }

    public static ChunkEvent.Unload createChunkUnloadEvent(Event event) {
        if (!(event instanceof UnloadChunkEvent)) {
            throw new IllegalArgumentException("Event is not a valid UnloadChunkEvent.");
        }

        UnloadChunkEvent spongeEvent = (UnloadChunkEvent) event;
        ChunkEvent.Unload forgeEvent =
                new ChunkEvent.Unload(((Chunk) spongeEvent.getTargetChunk()));
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
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
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
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Start(forgeWorld, explosion);
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
        Explosion explosion = (Explosion) spongeEvent.getExplosion();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent =
                new net.minecraftforge.event.world.ExplosionEvent.Detonate(forgeWorld, explosion,
                        (List<Entity>) (Object) spongeEvent.getEntities());
        return forgeEvent;
    }

    // Server events
    private static ServerChatEvent createServerChatEvent(Event event) {
        if (!(event instanceof MessageChannelEvent.Chat)) {
            throw new IllegalArgumentException("Event is not a valid MessageChannelEvent.Chat.");
        }

        MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) event;
        Optional<Player> player = spongeEvent.getCause().first(Player.class);
        if (!player.isPresent()) {
            return null;
        }

        Text spongeText = spongeEvent.getOriginalMessage();
        IChatComponent component = SpongeTexts.toComponent(spongeText);
        if (!(component instanceof ChatComponentTranslation)) {
            component = new ChatComponentTranslation("%s", component);
        }

        // Using toPlain here is fine, since the raw message from the client
        // can't have formatting.
        ServerChatEvent forgeEvent =
                new ServerChatEvent((EntityPlayerMP) player.get(), spongeEvent.getOriginalMessage().toPlain(),
                        (ChatComponentTranslation) component);
        ((IMixinInitCause) forgeEvent).initCause(spongeEvent.getCause());

        return forgeEvent;
    }

    // Special handling before Forge events post
    @SuppressWarnings("unchecked")
    public static void onForgePost(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        if (forgeEvent instanceof net.minecraftforge.event.world.ExplosionEvent.Detonate) {
            net.minecraftforge.event.world.ExplosionEvent.Detonate explosionEvent =
                    (net.minecraftforge.event.world.ExplosionEvent.Detonate) forgeEvent;
            if (!explosionEvent.explosion.isSmoking) { // shouldBreakBlocks
                List<BlockPos> affectedBlocks = explosionEvent.explosion.getAffectedBlockPositions();
                affectedBlocks.clear();
            }
        } else if (forgeEvent instanceof LivingDeathEvent) {
            MessageChannelEvent spongeEvent = (MessageChannelEvent) forgeEvent;
            Text message = spongeEvent.getMessage();
            if (!spongeEvent.isMessageCancelled() && !message.isEmpty()) {
                spongeEvent.getChannel().ifPresent(channel -> channel.send(((LivingDeathEvent) forgeEvent).entityLiving, spongeEvent.getMessage()));
            }
        }
    }

    // Bulk Event Handling
    public static CollideEntityEvent callEntityItemPickupEvent(Event event) {
        if (!(event instanceof CollideEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valid CollideEntityEvent.");
        }

        CollideEntityEvent spongeEvent = (CollideEntityEvent) event;
        if (spongeEvent.getCause().first(Player.class).isPresent()) {
            Iterator<org.spongepowered.api.entity.Entity> iterator = spongeEvent.getEntities().iterator();
            while (iterator.hasNext()) {
                org.spongepowered.api.entity.Entity entity = iterator.next();
                if (entity instanceof org.spongepowered.api.entity.Item) {
                    EntityItem entityItem = (EntityItem) entity;
                    EntityItemPickupEvent forgeEvent =
                            new EntityItemPickupEvent((EntityPlayer) spongeEvent.getCause().first(Player.class).get(), entityItem);
                    ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                    if (forgeEvent.isCanceled()) {
                        iterator.remove();
                    }
                }
            }
        }
        return spongeEvent;
    }

    public static SpawnEntityEvent callEntityJoinWorldEvent(Event event) {
        if (!(event instanceof SpawnEntityEvent)) {
            throw new IllegalArgumentException("Event is not a valiud SpawnEntityEvent.");
        }

        SpawnEntityEvent spongeEvent = (SpawnEntityEvent) event;
        Iterator<org.spongepowered.api.entity.Entity> iterator = spongeEvent.getEntities().iterator();
        while (iterator.hasNext()) {
            org.spongepowered.api.entity.Entity entity = iterator.next();
            EntityJoinWorldEvent forgeEvent = new EntityJoinWorldEvent((net.minecraft.entity.Entity) entity,
                    (net.minecraft.world.World) entity.getLocation().getExtent());

            ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
            if (forgeEvent.isCanceled()) {
                iterator.remove();
            }
        }
        return spongeEvent;
    }

    public static ChangeBlockEvent.Break callBlockBreakEvent(Event event) {
        if (!(event instanceof ChangeBlockEvent.Break)) {
            throw new IllegalArgumentException("Event is not a valid ChangeBlockEventBreak");
        }

        ChangeBlockEvent.Break spongeEvent = (ChangeBlockEvent.Break) event;

        if (spongeEvent.getCause().first(Player.class).isPresent()) {
            Player player = spongeEvent.getCause().first(Player.class).get();
            Iterator<Transaction<BlockSnapshot>> iterator = spongeEvent.getTransactions().iterator();
            while (iterator.hasNext()) {
                Transaction<BlockSnapshot> transaction = iterator.next();
                Location<World> location = transaction.getOriginal().getLocation().get();
                net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
                BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

                StaticMixinHelper.breakEventExtendedState = (IBlockState) transaction.getOriginal().getExtendedState();
                BlockEvent.BreakEvent forgeEvent =
                        new BlockEvent.BreakEvent(world, pos, (IBlockState) transaction.getOriginal().getState(),
                                (EntityPlayer) player);
                StaticMixinHelper.breakEventExtendedState = null;

                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    transaction.setValid(false);
                }
            }
        }
        return spongeEvent;
    }

    public static ChangeBlockEvent.Place callBlockPlaceEvent(Event event) {
        if (!(event instanceof ChangeBlockEvent.Place)) {
            throw new IllegalArgumentException("Event is not a valid ChangeBlockEventPlace");
        }

        ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) event;

        if (spongeEvent.getCause().first(Player.class).isPresent()) {
            EntityPlayer player = (EntityPlayer) spongeEvent.getCause().first(Player.class).get();
            net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getTargetWorld();

            if (spongeEvent.getTransactions().size() == 1) {
                BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTransactions().get(0).getOriginal().getPosition());
                IBlockState state = (IBlockState) spongeEvent.getTransactions().get(0).getOriginal().getState();
                net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                IBlockState placedAgainst = Blocks.air.getDefaultState();
                if (StaticMixinHelper.packetPlayer != null && StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) StaticMixinHelper.processingPacket;
                    EnumFacing facing = EnumFacing.getFront(packet.getPlacedBlockDirection());
                    placedAgainst = blockSnapshot.world.getBlockState(blockSnapshot.pos.offset(facing.getOpposite()));
                }

                BlockEvent.PlaceEvent forgeEvent = new BlockEvent.PlaceEvent(blockSnapshot, placedAgainst, player);
                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    spongeEvent.getTransactions().get(0).setValid(false);
                }
            } else { // multi
                Iterator<Transaction<BlockSnapshot>> iterator = spongeEvent.getTransactions().iterator();
                List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots = new ArrayList<>();

                while (iterator.hasNext()) {
                    Transaction<BlockSnapshot> transaction = iterator.next();
                    Location<World> location = transaction.getOriginal().getLocation().get();
                    IBlockState state = (IBlockState) transaction.getOriginal().getState();
                    BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                    blockSnapshots.add(blockSnapshot);
                }

                IBlockState placedAgainst = Blocks.air.getDefaultState();
                if (StaticMixinHelper.packetPlayer != null && StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) StaticMixinHelper.processingPacket;
                    EnumFacing facing = EnumFacing.getFront(packet.getPlacedBlockDirection());
                    placedAgainst = blockSnapshots.get(0).world.getBlockState(blockSnapshots.get(0).pos.offset(facing.getOpposite()));
                }

                BlockEvent.MultiPlaceEvent forgeEvent = new BlockEvent.MultiPlaceEvent(blockSnapshots, placedAgainst, player);
                ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(forgeEvent, true);
                if (forgeEvent.isCanceled()) {
                    while (iterator.hasNext()) {
                        iterator.next().setValid(false);
                    }
                }
            }
        }
        return spongeEvent;
    }
}
