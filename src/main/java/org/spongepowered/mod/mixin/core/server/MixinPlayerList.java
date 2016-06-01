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
package org.spongepowered.mod.mixin.core.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.world.WorldManager;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements IMixinPlayerList {

    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn);
    @Shadow public abstract void syncPlayerInventory(EntityPlayerMP playerIn);

    /**
     * @author Simon816
     *
     * Remove call to firePlayerLoggedOut because SpongeCommon's
     * MixinNetHandlerPlayServer.onDisconnectPlayer fires the event already.
     *
     * NOTE: ANY call to playerLoggedOut will need to fire the
     * PlayerLoggedOutEvent manually!
     */
    @Redirect(method = "playerLoggedOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;firePlayerLoggedOut(Lnet/minecraft/entity/player/EntityPlayer;)V",
            remap = false))
    public void onFirePlayerLoggedOutCall(FMLCommonHandler thisCtx, EntityPlayer playerIn) {
        // net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
    }


    /**
     * @author blood - May 21st, 2016
     *
     * @reason - adjusted to support {@link MoveEntityEvent.Position.Teleport.Portal}
     *
     * @param playerIn The player teleporting to another dimension
     * @param targetDimensionId The id of target dimension.
     * @param teleporter The teleporter used to transport and create the portal
     */
    @Overwrite
    public void transferPlayerToDimension(EntityPlayerMP playerIn, int targetDimensionId, net.minecraft.world.Teleporter teleporter) {
        MoveEntityEvent.Position.Teleport.Portal event = SpongeCommonEventFactory.handleDisplaceEntityPortalEvent(playerIn, targetDimensionId, teleporter);
        if (event != null || event.isCancelled()) {
            return;
        }

        WorldServer fromWorld = (WorldServer) event.getFromTransform().getExtent();
        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();
        playerIn.dimension = toWorld.provider.getDimensionType().getId();
        // Support vanilla clients teleporting to custom dimensions
        final DimensionType dimensionType = WorldManager.getClientDimensionType(toWorld.provider.getDimensionType());
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            WorldManager.sendDimensionRegistration(playerIn, dimensionType);
        }
        playerIn.connection.sendPacket(new SPacketRespawn(playerIn.dimension, fromWorld.getDifficulty(), fromWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
        fromWorld.removeEntityDangerously(playerIn);
        playerIn.isDead = false;
        // we do not need to call transferEntityToWorld as we already have the correct transform and created the portal in handleDisplaceEntityPortalEvent
        ((IMixinEntity) playerIn).setLocationAndAngles(event.getToTransform());
        toWorld.spawnEntityInWorld(playerIn);
        toWorld.updateEntityWithOptionalForce(playerIn, false);
        playerIn.setWorld(toWorld);
        this.preparePlayer(playerIn, fromWorld);
        playerIn.connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.interactionManager.setWorld(toWorld);
        this.updateTimeAndWeatherForPlayer(playerIn, toWorld);
        this.syncPlayerInventory(playerIn);

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.connection.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }
        ((IMixinEntityPlayerMP) playerIn).refreshXpHealthAndFood();
        // Forge needs to know when a player changes to new a dimension
        // This cannot be mapped to DisplaceEntityEvent.Teleport as this event must be called AFTER transfer.
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(playerIn, fromWorld.provider.getDimension(), toWorld.provider.getDimension());
    }
}
