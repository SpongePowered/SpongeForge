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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.UnwindingPhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.mixin.core.world.ExplosionAccessor;
import org.spongepowered.common.mixin.core.world.storage.WorldInfoAccessor;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.mod.bridge.world.DerivedWorldInfoBridge_Forge;
import org.spongepowered.mod.bridge.world.WorldBridge_Forge;
import org.spongepowered.mod.bridge.event.EventBusBridge_Forge;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//This class handles events initiated by Sponge plugins.
//It is primarily responsible for firing a corresponding Forge event to mods.
@SuppressWarnings("deprecation")
public class SpongeToForgeEventFactory {

    static final EventBusBridge_Forge forgeEventBus = ((EventBusBridge_Forge) MinecraftForge.EVENT_BUS);

    /**
     * Used by {@link SpongeModEventManager#extendedPost} to obtain
     * corresponding forge event class if available.
     * 
     * @param spongeEvent The sponge event to check against forge
     * @return The forge event class, if available
     */
    static Class<? extends net.minecraftforge.fml.common.eventhandler.Event> getForgeEventClass(final Event spongeEvent) {
        if (spongeEvent instanceof MessageChannelEvent.Chat) {
            return ServerChatEvent.class;
        } else if (spongeEvent instanceof ChangeInventoryEvent.Pickup.Pre) {
            if (spongeEvent.getCause().root() instanceof Player) {
                return EntityItemPickupEvent.class;
            }
        } else if (spongeEvent instanceof DestructEntityEvent.Death) {
            return LivingDeathEvent.class;
        } else if (spongeEvent instanceof InteractBlockEvent) {
            if (spongeEvent instanceof InteractBlockEvent.Primary) {
                return PlayerInteractEvent.LeftClickBlock.class;
            }
            if (spongeEvent instanceof  InteractBlockEvent.Secondary) {
                return PlayerInteractEvent.RightClickBlock.class;
            }
        } else if (spongeEvent instanceof InteractEntityEvent.Secondary) {
            final InteractEntityEvent event = (InteractEntityEvent) spongeEvent;
            if (event.getInteractionPoint().isPresent()) {
                return PlayerInteractEvent.EntityInteractSpecific.class;
            } else {
                return PlayerInteractEvent.EntityInteract.class;
            }
        } else if (spongeEvent instanceof InteractItemEvent.Secondary) {
            return PlayerInteractEvent.RightClickItem.class;
        } else if (spongeEvent instanceof NotifyNeighborBlockEvent) {
            return BlockEvent.NeighborNotifyEvent.class;
        } else if (spongeEvent instanceof ChangeBlockEvent.Place) {
            if (((ChangeBlockEvent) spongeEvent).getTransactions().size() > 1) {
                return BlockEvent.MultiPlaceEvent.class;
            }
            return BlockEvent.PlaceEvent.class;
        } else if (spongeEvent instanceof ExplosionEvent.Pre) {
            return net.minecraftforge.event.world.ExplosionEvent.Start.class;
        } else if (spongeEvent instanceof ExplosionEvent.Detonate) {
            return net.minecraftforge.event.world.ExplosionEvent.Detonate.class;
        } else if (spongeEvent instanceof DropItemEvent) {
            final Object source = spongeEvent.getSource();
            if (spongeEvent instanceof DropItemEvent.Destruct && (source instanceof Living || source instanceof DamageSource)) {
                return LivingDropsEvent.class;
            }
            if ((spongeEvent instanceof DropItemEvent.Dispense || spongeEvent instanceof DropItemEvent.Custom) && source instanceof Player) {
                if (((EventBusBridge_Forge) MinecraftForge.EVENT_BUS).forgeBridge$getEventListenerClassList().contains(ItemTossEvent.class)) {
                    return ItemTossEvent.class;
                }
                if (((EventBusBridge_Forge) MinecraftForge.EVENT_BUS).forgeBridge$getEventListenerClassList().contains(EntityJoinWorldEvent.class)) {
                    return EntityJoinWorldEvent.class;
                }
                return null;
            }
        } else if (spongeEvent instanceof ClientConnectionEvent) {
            if (spongeEvent instanceof ClientConnectionEvent.Join) {
                return PlayerEvent.PlayerLoggedInEvent.class;
            }
            if (spongeEvent instanceof ClientConnectionEvent.Disconnect) {
                return PlayerEvent.PlayerLoggedOutEvent.class;
            }
        } else if (spongeEvent instanceof MoveEntityEvent.Teleport) {
            return EntityTravelToDimensionEvent.class;
        } else if (spongeEvent instanceof SpawnEntityEvent) {
            return EntityJoinWorldEvent.class;
        } else if (spongeEvent instanceof LoadWorldEvent) {
            return WorldEvent.Load.class;
        } else if (spongeEvent instanceof UnloadWorldEvent) {
            return WorldEvent.Unload.class;
        } else if (spongeEvent instanceof SaveWorldEvent.Post) {
            return WorldEvent.Save.class;
        } else if (spongeEvent instanceof LoadChunkEvent) {
            return ChunkEvent.Load.class;
        } else if (spongeEvent instanceof UnloadChunkEvent) {
            return ChunkEvent.Unload.class;
        } else if (spongeEvent instanceof FishingEvent.Stop) {
            return ItemFishedEvent.class;
        } else if (spongeEvent instanceof UseItemStackEvent) {
            if (spongeEvent instanceof UseItemStackEvent.Start) {
                return LivingEntityUseItemEvent.Start.class;
            }
            if (spongeEvent instanceof UseItemStackEvent.Tick) {
                return LivingEntityUseItemEvent.Tick.class;
            }
            if (spongeEvent instanceof UseItemStackEvent.Stop) {
                return LivingEntityUseItemEvent.Stop.class;
            }
            if (spongeEvent instanceof UseItemStackEvent.Replace) {
                return LivingEntityUseItemEvent.Finish.class;
            }
        } else if (spongeEvent instanceof AdvancementEvent.Grant) {
            return net.minecraftforge.event.entity.player.AdvancementEvent.class;
        } else if (spongeEvent instanceof RideEntityEvent) {
            return net.minecraftforge.event.entity.EntityMountEvent.class;
        }
        return null;
    }

    // Used for firing Forge events after a Sponge event has been triggered
    static boolean createAndPostForgeEvent(final SpongeToForgeEventData spongeEventData) {
        final Class<? extends net.minecraftforge.fml.common.eventhandler.Event> clazz = spongeEventData.getForgeClass();
        final Event spongeEvent = spongeEventData.getSpongeEvent();
        if (spongeEvent instanceof MessageChannelEvent.Chat) {
            return createAndPostServerChatEvent(spongeEventData);
        } else if (spongeEvent instanceof ChangeInventoryEvent.Pickup.Pre) {
            return createAndPostEntityItemPickupEvent(spongeEventData);
        } else if (spongeEvent instanceof InteractEntityEvent.Secondary) {
            return createAndPostEntityInteractEvent(spongeEventData);
        } else if (spongeEvent instanceof NotifyNeighborBlockEvent) {
            return createAndPostNeighborNotifyEvent(spongeEventData);
        } else if (spongeEvent instanceof ChangeBlockEvent.Place) {
            return createAndPostBlockPlaceEvent(spongeEventData);
        } else if (spongeEvent instanceof ChangeBlockEvent.Pre) {
            return createAndPostBlockBreakEvent(spongeEventData);
        } else if (PlayerInteractEvent.class.isAssignableFrom(clazz)) {
            if (spongeEvent instanceof InteractBlockEvent) {
                return createAndPostPlayerInteractBlockEvent(spongeEventData);
            }
            if (spongeEvent instanceof InteractItemEvent.Secondary) {
                return createAndPostPlayerInteractItemEvent(spongeEventData);
            }
        } else if (LivingDropsEvent.class.isAssignableFrom(clazz)) {
            if (spongeEvent instanceof DropItemEvent.Destruct) {
                final Object root = spongeEventData.getSpongeEvent().getSource();
                if (root instanceof Player) {
                    return createAndPostItemTossEvent(spongeEventData);
                }
                return createAndPostLivingDropsEventEvent(spongeEventData);
            }
        } else if (ItemTossEvent.class.isAssignableFrom(clazz)) {
            if (spongeEvent instanceof DropItemEvent.Dispense) {
                final Object root = spongeEvent.getSource();
                if (root instanceof Player) {
                    return createAndPostItemTossEvent(spongeEventData);
                }
            }
        } else if (spongeEvent instanceof ClientConnectionEvent.Join) {
            return createAndPostPlayerLoggedInEvent(spongeEventData);
        } else if (spongeEvent instanceof ClientConnectionEvent.Disconnect) {
            return createAndPostPlayerLoggedOutEvent(spongeEventData);
        } else if (EntityJoinWorldEvent.class.isAssignableFrom(clazz)) {
            return createAndPostEntityJoinWorldEvent(spongeEventData);
        } else if (spongeEvent instanceof UnloadWorldEvent) {
            return createAndPostWorldUnloadEvent(spongeEventData);
        } else if (spongeEvent instanceof LoadWorldEvent) {
            return createAndPostWorldLoadEvent(spongeEventData);
        } else if (spongeEvent instanceof SaveWorldEvent) {
            return createAndPostWorldSaveEvent(spongeEventData);
        } else if (spongeEvent instanceof LoadChunkEvent) {
            return createAndPostChunkLoadEvent(spongeEventData);
        } else if (spongeEvent instanceof UnloadChunkEvent) {
            return createAndPostChunkUnloadEvent(spongeEventData);
        } else if (spongeEvent instanceof ExplosionEvent.Pre) {
            return createAndPostExplosionEventPre(spongeEventData);
        } else if (spongeEvent instanceof ExplosionEvent.Detonate) {
            return createAndPostExplosionEventDetonate(spongeEventData);
        } else if (spongeEvent instanceof FishingEvent.Stop) {
            return createAndPostItemFishedEvent(spongeEventData);
        } else if (spongeEvent instanceof UseItemStackEvent) {
            return createAndPostLivingUseItemEvent(spongeEventData);
        } else if (spongeEvent instanceof AdvancementEvent.Grant) {
            return createAndPostAdvancementGrantEvent(spongeEventData);
        } else if (spongeEvent instanceof RideEntityEvent) {
            return createAndPostRideEntityEvent(spongeEventData);
        }
        return false;
    }

    private static boolean createAndPostRideEntityEvent(final SpongeToForgeEventData eventData) {
        final RideEntityEvent spongeEvent = (RideEntityEvent) eventData.getSpongeEvent();
        EntityMountEvent forgeEvent = (EntityMountEvent) eventData.getForgeEvent();

        final Entity rider = spongeEvent.getCause().first(Entity.class).orElse(null);
        if (rider == null) {
            return false;
        }

        if (forgeEvent == null) {
            forgeEvent = new EntityMountEvent(rider, (Entity)spongeEvent.getTargetEntity(), (net.minecraft.world.World)spongeEvent.getTargetEntity().getWorld(), spongeEvent instanceof RideEntityEvent.Mount);
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        if (forgeEvent.isCanceled()) {
            spongeEvent.setCancelled(true);
        }

        return true;
    }

    private static boolean createAndPostServerChatEvent(final SpongeToForgeEventData eventData) {
        final MessageChannelEvent.Chat spongeEvent = (MessageChannelEvent.Chat) eventData.getSpongeEvent();
        ServerChatEvent forgeEvent = (ServerChatEvent) eventData.getForgeEvent();

        // We store this to check if the component in the forge event changes - we don't want to sync
        // the forge component back if no changes have been made (to keep our header/footer formatting).
        //
        // This will remain null if no forge event is to be fired.
        ITextComponent originalComponent = null;
        if (forgeEvent == null && spongeEvent.getSource() instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP) spongeEvent.getSource();
            originalComponent = SpongeTexts.toComponent(spongeEvent.getMessage());
            forgeEvent = new ServerChatEvent(player, spongeEvent.getRawMessage().toPlain(), originalComponent.createCopy());
            eventData.setForgeEvent(forgeEvent);
        } else if (forgeEvent != null) {
            // Get what the sponge event is saying, because that's what will potentially have to change.
            originalComponent = SpongeTexts.toComponent(spongeEvent.getMessage());
        }

        if (forgeEvent != null) {
            forgeEvent.setCanceled(spongeEvent.isCancelled());
            forgeEventBus.forgeBridge$post(eventData);
            if (!originalComponent.equals(forgeEvent.getComponent())) {
                // The message has changed, all we can do is set the body and leave the
                // header/footer blank.
                spongeEvent.setMessage(SpongeTexts.toText(forgeEvent.getComponent()));
            }
        }
        return true;
    }

    private static boolean createAndPostLivingUseItemEvent(final SpongeToForgeEventData eventData) {
        final UseItemStackEvent spongeEvent = (UseItemStackEvent) eventData.getSpongeEvent();
        LivingEntityUseItemEvent forgeEvent = (LivingEntityUseItemEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final EntityLivingBase entity = spongeEvent.getCause().first(EntityLivingBase.class).orElse(null);
            if (entity == null) {
                return false;
            }

            final ItemStack stack = ItemStackUtil.toNative(spongeEvent.getItemStackInUse().createStack());

            if (spongeEvent instanceof UseItemStackEvent.Start) {
                forgeEvent = new LivingEntityUseItemEvent.Start(entity, stack, spongeEvent.getRemainingDuration());
                forgeEvent.setCanceled(((UseItemStackEvent.Start) spongeEvent).isCancelled());
            } else if (spongeEvent instanceof UseItemStackEvent.Tick) {
                forgeEvent = new LivingEntityUseItemEvent.Tick(entity, stack, spongeEvent.getRemainingDuration());
                forgeEvent.setCanceled(((UseItemStackEvent.Tick) spongeEvent).isCancelled());
            } else if (spongeEvent instanceof UseItemStackEvent.Stop) {
                forgeEvent = new LivingEntityUseItemEvent.Stop(entity, stack, spongeEvent.getRemainingDuration());
                forgeEvent.setCanceled(((UseItemStackEvent.Stop) spongeEvent).isCancelled());
            } else if (spongeEvent instanceof UseItemStackEvent.Replace) {
                forgeEvent = new LivingEntityUseItemEvent.Finish(entity, stack, spongeEvent.getRemainingDuration(), ItemStackUtil.toNative((
                        (UseItemStackEvent.Replace) spongeEvent).getItemStackResult().getFinal().createStack()));
                if (forgeEvent.isCancelable()) {
                    forgeEvent.setCanceled(((UseItemStackEvent.Replace) spongeEvent).isCancelled());
                }
            }

            if (forgeEvent == null) {
                return false;
            }

            eventData.setForgeEvent(forgeEvent);
        } else {
            // sync Sponge to Forge
            forgeEvent.setDuration(spongeEvent.getRemainingDuration());
        }

        forgeEventBus.forgeBridge$post(eventData);
        spongeEvent.setRemainingDuration(forgeEvent.getDuration());

        // Cancelling the Forge tick event stops all usage of the item, whereas
        // cancelling the Sponge tick event merely skips the logic for the current tick.
        if (forgeEvent.isCanceled()) {
            if (forgeEvent instanceof LivingEntityUseItemEvent.Tick) {
                spongeEvent.setRemainingDuration(-1);
            } else {
                ((Cancellable) spongeEvent).setCancelled(true);
            }
        }

        if (forgeEvent instanceof LivingEntityUseItemEvent.Finish) {
            ((UseItemStackEvent.Replace) spongeEvent).getItemStackResult().setCustom(ItemStackUtil.snapshotOf(((LivingEntityUseItemEvent.Finish) forgeEvent).getResultStack()));
        }

        return true;
    }

    private static boolean createAndPostAdvancementGrantEvent(final SpongeToForgeEventData eventData) {
        final AdvancementEvent.Grant spongeEvent = (AdvancementEvent.Grant) eventData.getSpongeEvent();
        net.minecraftforge.event.entity.player.AdvancementEvent forgeEvent = (net.minecraftforge.event.entity.player.AdvancementEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new net.minecraftforge.event.entity.player.AdvancementEvent((EntityPlayer) spongeEvent.getTargetEntity(), (net.minecraft.advancements.Advancement) spongeEvent.getAdvancement());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(forgeEvent, true);
        return true;
    }


    // Bulk Event Handling
    private static boolean createAndPostItemTossEvent(final SpongeToForgeEventData eventData) {
        final SpawnEntityEvent spongeEvent = (SpawnEntityEvent) eventData.getSpongeEvent();
        final ItemTossEvent forgeEvent = (ItemTossEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final Cause cause = spongeEvent.getCause();
            final SpawnType spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
            final EntityPlayerMP serverPlayer = (EntityPlayerMP) spongeEvent.getCause().root();
            if (spawnType == null || spawnType != SpawnTypes.DROPPED_ITEM || spongeEvent.getEntities().isEmpty()) {
                return false;
            }

            spongeEvent.filterEntities(e -> (e instanceof EntityItem) && !forgeEventBus.forgeBridge$post(new ItemTossEvent(
                    (EntityItem) e, serverPlayer), true));

            createAndPostEntityJoinWorldEvent(eventData);
            handleCustomStack(spongeEvent);
            return true;
        } else {
            // Sync Sponge to Forge
            if (spongeEvent.isCancelled() || spongeEvent.getEntities().isEmpty()) {
                return false;
            }
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerInteractBlockEvent(final SpongeToForgeEventData eventData) {
        final InteractBlockEvent spongeEvent = (InteractBlockEvent) eventData.getSpongeEvent();
        PlayerInteractEvent forgeEvent = (PlayerInteractEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final Player player = spongeEvent.getCause().first(Player.class).orElse(null);
            // Forge doesn't support left-click AIR
            if (player == null || (spongeEvent instanceof InteractBlockEvent.Primary && spongeEvent.getTargetBlock() == BlockSnapshot.NONE)) {
                return false;
            }

            final BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTargetBlock().getPosition());
            final EnumFacing face = DirectionFacingProvider.getInstance().get(spongeEvent.getTargetSide()).orElse(null);
            Vec3d hitVec = null;
            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            if (spongeEvent.getInteractionPoint().isPresent()) {
                hitVec = VecHelper.toVec3d(spongeEvent.getInteractionPoint().get());
            }
            if (spongeEvent instanceof InteractBlockEvent.Primary) {
                forgeEvent = new PlayerInteractEvent.LeftClickBlock(entityPlayerMP, pos, face, hitVec);
            } else if (spongeEvent instanceof InteractBlockEvent.Secondary) {
                final EnumHand hand = spongeEvent instanceof InteractBlockEvent.Secondary.MainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                forgeEvent = new PlayerInteractEvent.RightClickBlock(entityPlayerMP, hand, pos, face, hitVec);
            }

            if (forgeEvent == null) {
                return false;
            }
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        if (forgeEvent instanceof PlayerInteractEvent.RightClickBlock) {
            final PlayerInteractEvent.RightClickBlock event = (PlayerInteractEvent.RightClickBlock) forgeEvent;
            // Mods have higher priority
            if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseItemResult(getTristateFromResult(event.getUseItem()));
            }
            if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT) {
                ((InteractBlockEvent.Secondary) spongeEvent).setUseBlockResult(getTristateFromResult(event.getUseBlock()));
            }
        }

        return true;
    }

    private static boolean createAndPostPlayerInteractItemEvent(final SpongeToForgeEventData eventData) {
        final InteractItemEvent.Secondary spongeEvent = (InteractItemEvent.Secondary) eventData.getSpongeEvent();
        PlayerInteractEvent.RightClickItem forgeEvent = (PlayerInteractEvent.RightClickItem) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final Player player = spongeEvent.getCause().first(Player.class).orElse(null);
            if (player == null) {
                return false;
            }

            final EnumHand hand = spongeEvent instanceof InteractItemEvent.Secondary.MainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            forgeEvent = new PlayerInteractEvent.RightClickItem((EntityPlayerMP) player, hand);
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostEntityItemPickupEvent(final SpongeToForgeEventData eventData) {
        final ChangeInventoryEvent.Pickup.Pre spongeEvent = (ChangeInventoryEvent.Pickup.Pre) eventData.getSpongeEvent();
        EntityItemPickupEvent forgeEvent = (EntityItemPickupEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            // Sponge -> Forge
            if (spongeEvent.getTargetEntity() instanceof EntityItem) {
                final EntityItem entityItem = (EntityItem) spongeEvent.getTargetEntity();
                final Player player = spongeEvent.getCause().first(Player.class).orElse(null);
                if (player != null) {
                    forgeEvent = new EntityItemPickupEvent((EntityPlayer) player, entityItem);
                    eventData.setForgeEvent(forgeEvent);
                }
            }
            if (forgeEvent == null) {
                return false;
            }
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostLivingDropsEventEvent(final SpongeToForgeEventData eventData) {
        final AffectEntityEvent spongeEvent = (AffectEntityEvent) eventData.getSpongeEvent();
        LivingDropsEvent forgeEvent = (LivingDropsEvent) eventData.getForgeEvent();
        final Cause cause = spongeEvent.getCause();
        //final SpawnType spawnType = cause.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
        final EntityLivingBase living = cause.first(EntityLivingBase.class).orElse(null);

        if (living != null && spongeEvent instanceof DropItemEvent.Destruct) {
            final DropItemEvent.Destruct destruct = (DropItemEvent.Destruct) spongeEvent;

            final net.minecraft.util.DamageSource damageSource = (net.minecraft.util.DamageSource) cause.first(DamageSource.class).orElse(null);

            if (damageSource != null) {

                final List<EntityItem> items = destruct.getEntities()
                  .stream()
                  .filter(e -> e instanceof EntityItem)
                  .map(e -> (EntityItem) e)
                  .collect(Collectors.toList());

                if (forgeEvent == null) {
                    if (living instanceof EntityPlayerMP) {
                        final EntityPlayerMP serverPlayer = (EntityPlayerMP) living;

                        forgeEvent = new PlayerDropsEvent(serverPlayer, damageSource, new ArrayList<>(items), ((EntityLivingBaseAccessor)
                          serverPlayer).accessor$getRecentlyHitValue() > 0);
                    } else {
                        forgeEvent = new LivingDropsEvent(living, damageSource, new ArrayList<>(items), net.minecraftforge.common.ForgeHooks
                          .getLootingLevel(living, damageSource.getTrueSource(), damageSource),
                            ((EntityLivingBaseAccessor) living).accessor$getRecentlyHitValue() > 0);
                    }
                    eventData.setForgeEvent(forgeEvent);
                } else {
                    // Sync Sponge to Forge
                    if (spongeEvent.getEntities().size() != forgeEvent.getDrops().size()) {
                        forgeEvent.getDrops().clear();
                        for (final org.spongepowered.api.entity.Entity entity : spongeEvent.getEntities()) {
                            if (entity instanceof EntityItem) {
                                forgeEvent.getDrops().add((EntityItem) entity);
                            }
                        }
                    }
                }

                forgeEventBus.forgeBridge$post(eventData);
                if (forgeEvent.isCanceled()) {
                    spongeEvent.setCancelled(true);
                    return true;
                } else {
                    // Re-sync entity list from forge to sponge
                    spongeEvent.getEntities().removeAll(items);
                    spongeEvent.getEntities().addAll(forgeEvent.getDrops().stream().map(entity -> (org.spongepowered.api.entity.Entity) entity).collect(Collectors.toList()));
                }
            }
        }

        return createAndPostEntityJoinWorldEvent(eventData);
    }

    private static boolean createAndPostEntityJoinWorldEvent(final SpongeToForgeEventData eventData) {
        final SpawnEntityEvent spongeEvent = (SpawnEntityEvent) eventData.getSpongeEvent();
        final ListIterator<org.spongepowered.api.entity.Entity> iterator = spongeEvent.getEntities().listIterator();
        if (spongeEvent.getEntities().isEmpty()) {
            if (eventData.getForgeEvent() != null) {
                return true;
            }
            return false;
        }

        // used to avoid player item restores when set to dead
        boolean canCancelEvent = true;

        while (iterator.hasNext()) {
            final org.spongepowered.api.entity.Entity entity = iterator.next();
            final EntityJoinWorldEvent forgeEvent = new EntityJoinWorldEvent((Entity) entity,
                    (net.minecraft.world.World) entity.getLocation().getExtent());

            eventData.setForgeEvent(forgeEvent);
            // Avoid calling post with eventData as that propagates cancel
            ((EventBusBridge_Forge) MinecraftForge.EVENT_BUS).forgeBridge$post(forgeEvent, true);
            final Entity mcEntity = (Entity) entity;
            if (mcEntity.isDead) {
                // Don't restore packet item if a mod wants it dead
                // Mods such as Flux-Networks kills the entity item to spawn a custom one
                canCancelEvent = false;
            }
            if (forgeEvent.isCanceled()) {
                iterator.remove();
            }
        }
        if (spongeEvent.getEntities().isEmpty() && canCancelEvent) {
            spongeEvent.setCancelled(true);
        }
        return true;
    }

    static void handlePrefireLogic(final Event event) {
        if (event instanceof SpawnEntityEvent) {
            handleCustomStack((SpawnEntityEvent) event);
        }
    }

    // Copied from ForgeInternalHandler.onEntityJoinWorld, but with modifications
    @SuppressWarnings("deprecation")
    private static void handleCustomStack(final SpawnEntityEvent event) {
        // Sponge start - iterate over entities
        final ListIterator<org.spongepowered.api.entity.Entity> it = event.getEntities().listIterator();
        while (it.hasNext()) {
            final Entity entity = (Entity) it.next(); //Sponge - use entity from event
            if (entity instanceof EntityItem) {
                handleCustomEntityFromIterator(it, entity);
            }
        }
    }

    private static void handleCustomEntityFromIterator(final ListIterator<org.spongepowered.api.entity.Entity> it, final Entity entity) {
        final ItemStack stack = entity instanceof EntityItem ? ((EntityItem) entity).getItem() : ItemStack.EMPTY;
        final Item item = stack.getItem();

        if (item.hasCustomEntity(stack)) {
            final Entity newEntity = item.createEntity(entity.getEntityWorld(), entity, stack); // Sponge - use world from entity
            if (newEntity != null) {
                entity.setDead();

                final EntityJoinWorldEvent cancelledEvent = new EntityJoinWorldEvent(entity, entity.getEntityWorld());
                cancelledEvent.setCanceled(true);
                forgeEventBus.forgeBridge$post(cancelledEvent, true);

                if (!cancelledEvent.isCanceled()) {
                    SpongeImpl.getLogger()
                            .error("A mod has un-cancelled the EntityJoinWorld event for the original EntityItem (from before Item#createEntity is called). This is almost certainly a terrible idea!");
                }

                it.set((org.spongepowered.api.entity.Entity) newEntity);
                // Sponge end
            }
        }
    }

    private static boolean createAndPostNeighborNotifyEvent(final SpongeToForgeEventData eventData) {
        final NotifyNeighborBlockEvent spongeEvent = (NotifyNeighborBlockEvent) eventData.getSpongeEvent();
        BlockEvent.NeighborNotifyEvent forgeEvent = (BlockEvent.NeighborNotifyEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final LocatableBlock locatableBlock = spongeEvent.getCause().first(LocatableBlock.class).orElse(null);
            final TileEntity tileEntitySource = spongeEvent.getCause().first(TileEntity.class).orElse(null);
            final Location<World> sourceLocation;
            final IBlockState state;
            if (locatableBlock != null) {
                sourceLocation = locatableBlock.getLocation();
                state = (IBlockState) locatableBlock.getBlockState();
            } else if (tileEntitySource != null) {
                sourceLocation = tileEntitySource.getLocation();
                state = (IBlockState) sourceLocation.getBlock();
            } else { // should never happen but just in case it does
                return false;
            }

            final EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
            for (final Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getNeighbors().entrySet()) {
                if (mapEntry.getKey() != Direction.NONE) {
                    facings.add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
                }
            }

            if (facings.isEmpty()) {
                return false;
            }

            final BlockPos pos = VecHelper.toBlockPos(sourceLocation);
            final net.minecraft.world.World world = (net.minecraft.world.World) sourceLocation.getExtent();
            // TODO - the boolean forced redstone bit needs to be set properly
            forgeEvent = new BlockEvent.NeighborNotifyEvent(world, pos, state, facings, false);
            eventData.setForgeEvent(forgeEvent);
        } else {
            // sync Sponge -> Forge
            if (forgeEvent.getNotifiedSides().size() != spongeEvent.getNeighbors().size()) {
                forgeEvent.getNotifiedSides().clear();
                for (final Map.Entry<Direction, BlockState> mapEntry : spongeEvent.getNeighbors().entrySet()) {
                    forgeEvent.getNotifiedSides().add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
                }
            }
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostBlockBreakEvent(final SpongeToForgeEventData eventData) {
        final ChangeBlockEvent.Pre spongeEvent = (ChangeBlockEvent.Pre) eventData.getSpongeEvent();
        BlockEvent.BreakEvent forgeEvent = (BlockEvent.BreakEvent) eventData.getForgeEvent();
        if (!(spongeEvent.getCause().root() instanceof Player) || spongeEvent.getLocations().size() != 1) {
            return false;
        }

        if (forgeEvent == null) {
            final EntityPlayer player = (EntityPlayer) spongeEvent.getCause().root();
            final net.minecraft.world.World world = player.world;
            final Location<World> location = spongeEvent.getLocations().get(0);
            final BlockPos pos = VecHelper.toBlockPos(location.getBlockPosition());
            final IBlockState state = (IBlockState) location.getBlock();
            forgeEvent = new BlockEvent.BreakEvent(world, pos, state, player);
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    @SuppressWarnings("deprecation")
    private static boolean createAndPostBlockPlaceEvent(final SpongeToForgeEventData eventData) {
        final ChangeBlockEvent.Place spongeEvent = (ChangeBlockEvent.Place) eventData.getSpongeEvent();
        BlockEvent.PlaceEvent forgeEvent = (BlockEvent.PlaceEvent) eventData.getForgeEvent();
        if (!(spongeEvent.getCause().root() instanceof Player)) {
            return false;
        }

        if (forgeEvent == null) {
            final EntityPlayer player = (EntityPlayer) spongeEvent.getCause().root();
            final net.minecraft.world.World world = player.world;
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final PhaseContext<?> currentContext = phaseTracker.getCurrentContext();
            PhaseContext<?> target = currentContext;
            if (currentContext instanceof UnwindingPhaseContext) {
                target = ((UnwindingPhaseContext) currentContext).getUnwindingContext();
            }
            final PacketContext<?> context = target instanceof PacketContext<?> ? (PacketContext<?>) target : null;
            final Packet<?> contextPacket = context != null ? context.getPacket() : null;
            if (contextPacket == null) {
                return false;
            }

            if (spongeEvent.getTransactions().size() == 1) {
                final BlockPos pos = VecHelper.toBlockPos(spongeEvent.getTransactions().get(0).getOriginal().getPosition());
                final IBlockState state = (IBlockState) spongeEvent.getTransactions().get(0).getOriginal().getState();
                final net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                IBlockState placedAgainst = Blocks.AIR.getDefaultState();
                EnumHand hand = EnumHand.MAIN_HAND;
                if (contextPacket instanceof CPacketPlayerTryUseItemOnBlock) {
                    final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) contextPacket;
                    final EnumFacing facing = packet.getDirection();
                    placedAgainst = blockSnapshot.getWorld().getBlockState(blockSnapshot.getPos().offset(facing.getOpposite()));
                    hand = packet.getHand();
                }

                forgeEvent = new BlockEvent.PlaceEvent(blockSnapshot, placedAgainst, player, hand);
                eventData.setForgeEvent(forgeEvent);
            } else { // multi
                final Iterator<Transaction<BlockSnapshot>> iterator = spongeEvent.getTransactions().iterator();
                final List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots = new ArrayList<>();

                while (iterator.hasNext()) {
                    final Transaction<BlockSnapshot> transaction = iterator.next();
                    final Location<World> location = transaction.getOriginal().getLocation().get();
                    final IBlockState state = (IBlockState) transaction.getOriginal().getState();
                    final BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    final net.minecraftforge.common.util.BlockSnapshot blockSnapshot = new net.minecraftforge.common.util.BlockSnapshot(world, pos, state);
                    blockSnapshots.add(blockSnapshot);
                }

                IBlockState placedAgainst = Blocks.AIR.getDefaultState();
                EnumHand hand = EnumHand.MAIN_HAND;
                if (contextPacket instanceof CPacketPlayerTryUseItemOnBlock) {
                    final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) contextPacket;
                    final EnumFacing facing = packet.getDirection();
                    placedAgainst = blockSnapshots.get(0).getWorld().getBlockState(blockSnapshots.get(0).getPos().offset(facing.getOpposite()));
                    hand = packet.getHand();
                }

                forgeEvent = new BlockEvent.MultiPlaceEvent(blockSnapshots, placedAgainst, player, hand);
                eventData.setForgeEvent(forgeEvent);
            }
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostExplosionEventPre(final SpongeToForgeEventData eventData) {
        final ExplosionEvent.Pre spongeEvent = (ExplosionEvent.Pre) eventData.getSpongeEvent();
        net.minecraftforge.event.world.ExplosionEvent.Start forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Start) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Start(
                    ((net.minecraft.world.World) spongeEvent.getTargetWorld()), ((Explosion) spongeEvent.getExplosion()));
            eventData.setForgeEvent(forgeEvent);
        }
        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean createAndPostExplosionEventDetonate(final SpongeToForgeEventData eventData) {
        final ExplosionEvent.Detonate spongeEvent = (ExplosionEvent.Detonate) eventData.getSpongeEvent();
        net.minecraftforge.event.world.ExplosionEvent.Detonate forgeEvent = (net.minecraftforge.event.world.ExplosionEvent.Detonate) eventData.getForgeEvent();

        final Explosion explosion = (Explosion) spongeEvent.getExplosion();
        if (explosion == null) {
            return false;
        }

        if (forgeEvent == null) {
            forgeEvent = new net.minecraftforge.event.world.ExplosionEvent.Detonate(
                    (net.minecraft.world.World) spongeEvent.getTargetWorld(), explosion,
                    (List<Entity>) (List<?>) spongeEvent.getEntities());
            explosion.getAffectedBlockPositions().clear();
            for (final Location<World> x : spongeEvent.getAffectedLocations()) {
                explosion.getAffectedBlockPositions().add(VecHelper.toBlockPos(x.getPosition()));
            }
            eventData.setForgeEvent(forgeEvent);
        }

        if (!((ExplosionAccessor) forgeEvent.getExplosion()).accessor$getDamagesTerrain()) {
            final List<BlockPos> affectedBlocks = forgeEvent.getExplosion().getAffectedBlockPositions();
            affectedBlocks.clear();
        }
        if (spongeEvent.getAffectedLocations().size() != forgeEvent.getAffectedBlocks().size()) {
            forgeEvent.getAffectedBlocks().clear();
            for (final Location<World> x : spongeEvent.getAffectedLocations()) {
                forgeEvent.getAffectedBlocks().add(VecHelper.toBlockPos(x.getPosition()));
            }
        }
        forgeEventBus.forgeBridge$post(eventData);
        if (spongeEvent.getAffectedLocations().size() != forgeEvent.getAffectedBlocks().size()) {
            spongeEvent.getAffectedLocations().clear();
            for (final BlockPos pos : forgeEvent.getAffectedBlocks()) {
                spongeEvent.getAffectedLocations().add(new Location<>(spongeEvent.getTargetWorld(), VecHelper.toVector3i(pos)));
            }
        }
        return true;
    }

    private static boolean createAndPostEntityInteractEvent(final SpongeToForgeEventData eventData) {
        final InteractEntityEvent.Secondary spongeEvent = (InteractEntityEvent.Secondary) eventData.getSpongeEvent();
        PlayerInteractEvent forgeEvent = (PlayerInteractEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final Optional<Player> player = spongeEvent.getCause().first(Player.class);
            if (!player.isPresent()) {
                return false;
            }
            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player.get();
            final EnumHand hand = entityPlayerMP.getActiveHand();

            final EntityPlayer entityPlayer = (EntityPlayer) player.get();
            final Entity entity = (Entity) spongeEvent.getTargetEntity();
            final Vector3d hitVec = spongeEvent.getInteractionPoint().orElse(null);

            if (hitVec != null) {
                forgeEvent = new PlayerInteractEvent.EntityInteractSpecific(entityPlayer, hand, entity, VecHelper.toVec3d(hitVec));
            } else {
                forgeEvent = new PlayerInteractEvent.EntityInteract(entityPlayer, hand, entity);
            }
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEvent.setCanceled(spongeEvent.isCancelled());
        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerLoggedInEvent(final SpongeToForgeEventData eventData) {
        final ClientConnectionEvent.Join spongeEvent = (ClientConnectionEvent.Join) eventData.getSpongeEvent();
        PlayerEvent.PlayerLoggedInEvent forgeEvent = (PlayerEvent.PlayerLoggedInEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new PlayerEvent.PlayerLoggedInEvent((EntityPlayer) spongeEvent.getTargetEntity());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostPlayerLoggedOutEvent(final SpongeToForgeEventData eventData) {
        final ClientConnectionEvent.Disconnect spongeEvent = (ClientConnectionEvent.Disconnect) eventData.getSpongeEvent();
        PlayerEvent.PlayerLoggedOutEvent forgeEvent = (PlayerEvent.PlayerLoggedOutEvent) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new PlayerEvent.PlayerLoggedOutEvent((EntityPlayer) spongeEvent.getTargetEntity());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    private static boolean createAndPostWorldSaveEvent(final SpongeToForgeEventData eventData) {
        final SaveWorldEvent spongeEvent = (SaveWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Save forgeEvent = (WorldEvent.Save) eventData.getForgeEvent();
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        ((WorldBridge_Forge) spongeEvent.getTargetWorld()).forgeBridge$setRedirectedWorldInfo(WorldManager.getWorldByDimensionId(0).get().getWorldInfo());
        if (forgeEvent == null) {
            forgeEvent = new WorldEvent.Save((net.minecraft.world.World) spongeEvent.getTargetWorld());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(forgeEvent, true);
        ((WorldBridge_Forge) spongeEvent.getTargetWorld()).forgeBridge$setRedirectedWorldInfo(null);
        return true;
    }

    private static boolean createAndPostWorldLoadEvent(final SpongeToForgeEventData eventData) {
        final LoadWorldEvent spongeEvent = (LoadWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Load forgeEvent = (WorldEvent.Load) eventData.getForgeEvent();
        // Since Forge only uses a single save handler, we need to make sure to pass the overworld's handler.
        // This makes sure that mods dont attempt to save/read their data from the wrong location.
        final net.minecraft.world.World minecraftWorld = (net.minecraft.world.World) spongeEvent.getTargetWorld();
        if (minecraftWorld.provider.getDimension() != 0) {
            final DerivedWorldInfo info = new DerivedWorldInfo(WorldManager.getWorldByDimensionId(0).get().getWorldInfo());
            ((DerivedWorldInfoBridge_Forge) info).forgeBridge$setOverrideLevelName(minecraftWorld.getWorldInfo().getWorldName());
            ((WorldBridge_Forge) spongeEvent.getTargetWorld()).forgeBridge$setRedirectedWorldInfo(info);
        }
        ((ChunkProviderServerBridge) minecraftWorld.getChunkProvider()).bridge$setForceChunkRequests(true);
        try {
            if (forgeEvent == null) {
                forgeEvent = new WorldEvent.Load(minecraftWorld);
                eventData.setForgeEvent(forgeEvent);
            }

            forgeEventBus.forgeBridge$post(forgeEvent, true);
            return true;
        } finally {
            ((ChunkProviderServerBridge) minecraftWorld.getChunkProvider()).bridge$setForceChunkRequests(false);
            ((WorldBridge_Forge) minecraftWorld).forgeBridge$setRedirectedWorldInfo(null);
        }
    }

    private static boolean createAndPostWorldUnloadEvent(final SpongeToForgeEventData eventData) {
        final UnloadWorldEvent spongeEvent = (UnloadWorldEvent) eventData.getSpongeEvent();
        WorldEvent.Unload forgeEvent = (WorldEvent.Unload) eventData.getForgeEvent();
        if (forgeEvent == null) {
            forgeEvent = new WorldEvent.Unload((net.minecraft.world.World) spongeEvent.getTargetWorld());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostChunkLoadEvent(final SpongeToForgeEventData eventData) {
        final LoadChunkEvent spongeEvent = (LoadChunkEvent) eventData.getSpongeEvent();
        ChunkEvent.Load forgeEvent = (ChunkEvent.Load) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
            forgeEvent = new ChunkEvent.Load(chunk);
            eventData.setForgeEvent(forgeEvent);
        }
        forgeEventBus.forgeBridge$post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostChunkUnloadEvent(final SpongeToForgeEventData eventData) {
        final UnloadChunkEvent spongeEvent = (UnloadChunkEvent) eventData.getSpongeEvent();
        ChunkEvent.Unload forgeEvent = (ChunkEvent.Unload) eventData.getForgeEvent();
        if (forgeEvent == null) {
            final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeEvent.getTargetChunk();
            forgeEvent = new ChunkEvent.Unload(chunk);
            eventData.setForgeEvent(forgeEvent);
        }
        forgeEventBus.forgeBridge$post(forgeEvent, true);
        return true;
    }

    private static boolean createAndPostItemFishedEvent(final SpongeToForgeEventData eventData) {
        final FishingEvent.Stop spongeEvent = (FishingEvent.Stop) eventData.getSpongeEvent();
        ItemFishedEvent forgeEvent = (ItemFishedEvent) eventData.getForgeEvent();
        final List<Transaction<ItemStackSnapshot>> potentialFishies = spongeEvent.getTransactions();
        if (potentialFishies.isEmpty()) {
            return false;
        }

        if (forgeEvent == null) {
            final List<ItemStack> itemstacks = spongeEvent.getTransactions()
              .stream()
              .filter(Transaction::isValid)
              .map(transaction -> transaction.getFinal().createStack())
              .map(ItemStackUtil::toNative)
              .collect(Collectors.toList());

            if (itemstacks.isEmpty()) {
                return false;
            }

            forgeEvent = new ItemFishedEvent(itemstacks, 0, (EntityFishHook) spongeEvent.getFishHook());
            eventData.setForgeEvent(forgeEvent);
        }

        forgeEventBus.forgeBridge$post(eventData);
        return true;
    }

    // Special handler to process any events after ALL events have been posted to both Forge and Sponge
    public static void onPostEnd(final SpongeToForgeEventData eventData) {

    }

    private static Tristate getTristateFromResult(final net.minecraftforge.fml.common.eventhandler.Event.Result result) {
        if (result == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
            return Tristate.TRUE;
        } else if (result == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
            return Tristate.FALSE;
        }

        return Tristate.UNDEFINED;
    }
}
