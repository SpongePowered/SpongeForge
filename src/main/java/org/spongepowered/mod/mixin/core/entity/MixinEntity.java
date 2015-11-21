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
package org.spongepowered.mod.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.world.DimensionManager;


@NonnullByDefault
@Mixin(value = Entity.class, priority = 1001)
public abstract class MixinEntity implements IMixinEntity {

    @Shadow public net.minecraft.world.World worldObj;
    // @formatter:off
    @Shadow(remap = false)
    public abstract NBTTagCompound getEntityData();
    // @formatter:on

    @Override
    public final NBTTagCompound getSpongeData() {
        final NBTTagCompound data = this.getEntityData();
        if (!data.hasKey(NbtDataUtil.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            data.setTag(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        return data.getCompoundTag(NbtDataUtil.SPONGE_DATA);
    }

    @SuppressWarnings("unchecked")
    public boolean teleportEntity(Entity entity, Location<World> location, int currentDim, int targetDim, boolean forced) {
        MinecraftServer mcServer = MinecraftServer.getServer();
        final WorldServer fromWorld = mcServer.worldServerForDimension(currentDim);
        final WorldServer toWorld = mcServer.worldServerForDimension(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerManager().removePlayer((EntityPlayerMP) entity);
            mcServer.getConfigurationManager().playerEntityList.remove(entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removePlayerEntityDangerously(entity);
        entity.dimension = targetDim;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        if (forced) {
            while (!toWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
                entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
            }
        }

        toWorld.theChunkProviderServer.loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);

        if (entity instanceof EntityPlayer) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP) entity;

            // Support vanilla clients going into custom dimensions
            int clientDimension = DimensionManager.getClientDimensionToSend(toWorld.provider.getDimensionId(), toWorld, entityplayermp1);
            if (((IMixinEntityPlayerMP) entityplayermp1).usesCustomClient()) {
                DimensionManager.sendDimensionRegistration(toWorld, entityplayermp1, clientDimension);
            } else {
                // Send bogus dimension change for same worlds on Vanilla client
                if (currentDim != targetDim && (currentDim == clientDimension || targetDim == clientDimension)) {
                    entityplayermp1.playerNetServerHandler.sendPacket(
                            new S07PacketRespawn((byte) (clientDimension >= 0 ? -1 : 0), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                                    entityplayermp1.theItemInWorldManager.getGameType()));
                }
            }

            entityplayermp1.playerNetServerHandler.sendPacket(
                    new S07PacketRespawn(clientDimension, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityplayermp1.theItemInWorldManager.getGameType()));
            entity.setWorld(toWorld);
            entity.isDead = false;
            entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ,
                    entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
            entityplayermp1.setSneaking(false);
            mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(entityplayermp1, toWorld);
            toWorld.getPlayerManager().addPlayer(entityplayermp1);
            toWorld.spawnEntityInWorld(entityplayermp1);
            mcServer.getConfigurationManager().playerEntityList.add(entityplayermp1);
            entityplayermp1.theItemInWorldManager.setWorld(toWorld);
            entityplayermp1.addSelfToInternalCraftingInventory();
            entityplayermp1.setHealth(entityplayermp1.getHealth());
            for(Object effect : entityplayermp1.getActivePotionEffects()) {
                entityplayermp1.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(entityplayermp1.getEntityId(), (PotionEffect) effect));
            }

            FMLCommonHandler.instance().firePlayerChangedDimensionEvent(entityplayermp1, currentDim, targetDim);
        } else {
            toWorld.spawnEntityInWorld(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        return true;
    }

}
