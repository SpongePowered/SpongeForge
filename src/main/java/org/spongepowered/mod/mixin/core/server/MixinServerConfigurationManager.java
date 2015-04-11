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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.data.manipulators.entities.RespawnLocationData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.mod.interfaces.IMixinServerConfigurationManager;
import org.spongepowered.mod.world.SpongeDimensionType;
import org.spongepowered.mod.world.border.PlayerBorderListener;

import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager implements IMixinServerConfigurationManager {

    @Shadow
    private static Logger logger;

    @Shadow
    private MinecraftServer mcServer;

    @Shadow
    private IPlayerFileData playerNBTManagerObj;

    @SuppressWarnings("rawtypes")
    @Shadow
    public List playerEntityList;

    @Shadow
    public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);

    @Shadow
    public abstract void setPlayerGameTypeBasedOnOther(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, net.minecraft.world.World worldIn);

    @Shadow
    protected abstract void func_96456_a(ServerScoreboard scoreboardIn, EntityPlayerMP playerIn);

    @Shadow
    public abstract MinecraftServer getServerInstance();

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract void sendChatMsg(IChatComponent component);

    @Shadow
    public abstract void playerLoggedIn(EntityPlayerMP playerIn);

    @SuppressWarnings("rawtypes")
    @Overwrite(aliases = "initializeConnectionToPlayer")
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, NetHandlerPlayServer nethandlerplayserver) {
        GameProfile gameprofile = playerIn.getGameProfile();
        PlayerProfileCache playerprofilecache = this.mcServer.getPlayerProfileCache();
        GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        playerIn.setWorld(this.mcServer.worldServerForDimension(playerIn.dimension));

        net.minecraft.world.World playerWorld = this.mcServer.worldServerForDimension(playerIn.dimension);
        if (playerWorld == null) {
            playerIn.dimension = 0;
            playerWorld = this.mcServer.worldServerForDimension(0);
            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            playerIn.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        playerIn.setWorld(playerWorld);
        playerIn.theItemInWorldManager.setWorld((WorldServer) playerIn.worldObj);
        String s1 = "local";

        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        logger.info(playerIn.getCommandSenderName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " at (" + playerIn.posX
                + ", " + playerIn.posY + ", " + playerIn.posZ + ")");
        WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
        WorldInfo worldinfo = worldserver.getWorldInfo();
        BlockPos blockpos = worldserver.getSpawnPoint();
        this.setPlayerGameTypeBasedOnOther(playerIn, (EntityPlayerMP) null, worldserver);
        playerIn.playerNetServerHandler = nethandlerplayserver;
        // Support vanilla clients logging into custom dimensions
        int dimension = worldserver.provider.getDimensionId();
        boolean fmlClient = playerIn.playerNetServerHandler.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
        if (!fmlClient) {
            if (((Dimension) worldserver.provider).getType().equals(DimensionTypes.NETHER)) {
                dimension = -1;
            } else if (((Dimension) worldserver.provider).getType().equals(DimensionTypes.END)) {
                dimension = 1;
            } else {
                dimension = 0;
            }
        } else {
            // register dimension on client-side
            FMLEmbeddedChannel serverChannel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(playerIn);
            serverChannel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(dimension,
                    ((SpongeDimensionType) ((Dimension) worldserver.provider).getType()).getDimensionTypeId()));
        }

        nethandlerplayserver.sendPacket(new S01PacketJoinGame(playerIn.getEntityId(), playerIn.theItemInWorldManager.getGameType(), worldinfo
                .isHardcoreModeEnabled(), dimension, worldserver.getDifficulty(), this.getMaxPlayers(), worldinfo
                .getTerrainType(), worldserver.getGameRules().getGameRuleBooleanValue("reducedDebugInfo")));
        nethandlerplayserver.sendPacket(new S3FPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this
                .getServerInstance().getServerModName())));
        nethandlerplayserver.sendPacket(new S41PacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        nethandlerplayserver.sendPacket(new S05PacketSpawnPosition(blockpos));
        nethandlerplayserver.sendPacket(new S39PacketPlayerAbilities(playerIn.capabilities));
        nethandlerplayserver.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
        playerIn.getStatFile().func_150877_d();
        playerIn.getStatFile().func_150884_b(playerIn);
        this.func_96456_a((ServerScoreboard) worldserver.getScoreboard(), playerIn);
        this.mcServer.refreshStatusNextTick();
        ChatComponentTranslation chatcomponenttranslation;

        if (!playerIn.getCommandSenderName().equalsIgnoreCase(s)) {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined.renamed", new Object[] {playerIn.getDisplayName(), s});
        } else {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", new Object[] {playerIn.getDisplayName()});
        }

        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        this.sendChatMsg(chatcomponenttranslation);
        this.playerLoggedIn(playerIn);
        nethandlerplayserver.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        this.updateTimeAndWeatherForPlayer(playerIn, worldserver);

        if (this.mcServer.getResourcePackUrl().length() > 0) {
            playerIn.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
        }

        Iterator iterator = playerIn.getActivePotionEffects().iterator();

        while (iterator.hasNext()) {
            PotionEffect potioneffect = (PotionEffect) iterator.next();
            nethandlerplayserver.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        playerIn.addSelfToInternalCraftingInventory();

        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedIn(playerIn);
        if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10)) {
            Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);

            if (entity != null) {
                entity.forceSpawn = true;
                worldserver.spawnEntityInWorld(entity);
                playerIn.mountEntity(entity);
                entity.forceSpawn = false;
            }
        }
    }

    @SuppressWarnings({"unused", "unchecked"})
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int targetDimension, boolean conqueredEnd) {
        // Phase 1 - check if the player is allowed to respawn in same dimension
        net.minecraft.world.World world = this.mcServer.worldServerForDimension(targetDimension);
        World fromWorld = (World) playerIn.worldObj;

        if (!world.provider.canRespawnHere()) {
            targetDimension = world.provider.getRespawnDimension(playerIn);
        }

        // Phase 2 - handle return from End
        if (conqueredEnd) {
            WorldServer exitWorld = this.mcServer.worldServerForDimension(targetDimension);
            Location enter = ((Player) playerIn).getLocation();
            Optional<Location> exit = Optional.absent();
            // use bed if available, otherwise default spawn
            if (((Player) playerIn).getData(RespawnLocationData.class).isPresent()) {
                exit = Optional.of(((Player) playerIn).getData(RespawnLocationData.class).get().getRespawnLocation());
            }
            if (!exit.isPresent() || ((net.minecraft.world.World) ((World) exit.get().getExtent())).provider.getDimensionId() != 0) {
                Vector3i pos = ((World) exitWorld).getProperties().getSpawnPosition();
                exit = Optional.of(new Location((World) exitWorld, new Vector3d(pos.getX(), pos.getY(), pos.getZ())));
            }
        }

        // Phase 3 - remove current player from current dimension
        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        // par1EntityPlayerMP.getServerForPlayer().getEntityTracker().removeEntityFromAllTrackingPlayers(par1EntityPlayerMP);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);

        // Phase 4 - handle bed spawn
        BlockPos bedSpawnChunkCoords = playerIn.getBedLocation(targetDimension);
        boolean spawnForced = playerIn.isSpawnForced(targetDimension);
        playerIn.dimension = targetDimension;
        EntityPlayerMP entityplayermp1 = playerIn;
        // make sure to update reference for bed spawn logic
        entityplayermp1.setWorld(this.mcServer.worldServerForDimension(playerIn.dimension));
        entityplayermp1.playerConqueredTheEnd = false;
        BlockPos bedSpawnLocation;
        boolean isBedSpawn = false;
        World toWorld = ((World) entityplayermp1.worldObj);
        Location location = null;

        if (bedSpawnChunkCoords != null) { // if player has a bed
            bedSpawnLocation =
                    EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), bedSpawnChunkCoords, spawnForced);

            if (bedSpawnLocation != null) {
                isBedSpawn = true;
                entityplayermp1.setLocationAndAngles(bedSpawnLocation.getX() + 0.5F,
                        bedSpawnLocation.getY() + 0.1F, bedSpawnLocation.getZ() + 0.5F, 0.0F, 0.0F);
                entityplayermp1.setSpawnPoint(bedSpawnChunkCoords, spawnForced);
                location =
                        new Location(toWorld, new Vector3d(bedSpawnChunkCoords.getX() + 0.5, bedSpawnChunkCoords.getY(),
                                bedSpawnChunkCoords.getZ() + 0.5));
            } else { // bed was not found (broken)
                entityplayermp1.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0));
                // use the spawnpoint as location
                location =
                        new Location(toWorld, new Vector3d(toWorld.getProperties().getSpawnPosition().getX(), toWorld.getProperties()
                                .getSpawnPosition().getY(), toWorld.getProperties().getSpawnPosition().getZ()));
            }
        }

        if (location == null) {
            // use the world spawnpoint as default location
            location =
                    new Location(toWorld, new Vector3d(toWorld.getProperties().getSpawnPosition().getX(), toWorld.getProperties().getSpawnPosition()
                            .getY(), toWorld.getProperties().getSpawnPosition().getZ()));
        }

        if (!conqueredEnd) { // don't reset player if returning from end
            // TODO - add respawn event
            ((IMixinEntityPlayerMP) playerIn).reset();
        }

        WorldServer targetWorld = (WorldServer) location.getExtent();
        entityplayermp1.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);//, location.getYaw(), location.getPitch());
        targetWorld.theChunkProviderServer.loadChunk((int) entityplayermp1.posX >> 4, (int) entityplayermp1.posZ >> 4);

        while (!targetWorld.getCollidingBoundingBoxes(entityplayermp1, entityplayermp1.getEntityBoundingBox()).isEmpty()) {
            entityplayermp1.setPosition(entityplayermp1.posX, entityplayermp1.posY + 1.0D, entityplayermp1.posZ);
        }

        // Phase 5 - Respawn player in new world
        int actualDimension = targetWorld.provider.getDimensionId();
        // inform client of custom dimensions
        FMLEmbeddedChannel serverChannel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityplayermp1);
        serverChannel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(actualDimension,
                ((SpongeDimensionType) ((Dimension) targetWorld.provider).getType()).getDimensionTypeId()));

        boolean fmlClient = entityplayermp1.playerNetServerHandler.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
        // Support vanilla clients teleporting to custom dimensions
        if (!fmlClient) {
            if (toWorld.getDimension().getType().equals(DimensionTypes.NETHER)) {
                actualDimension = -1;
            } else if (toWorld.getDimension().getType().equals(DimensionTypes.END)) {
                actualDimension = 1;
            } else {
                actualDimension = 0;
            }
        }

        entityplayermp1.playerNetServerHandler.sendPacket(new S07PacketRespawn(actualDimension, targetWorld.getDifficulty(), targetWorld
                .getWorldInfo().getTerrainType(), entityplayermp1.theItemInWorldManager.getGameType()));
        entityplayermp1.setWorld(targetWorld); // in case plugin changed it
        entityplayermp1.isDead = false;
        BlockPos blockpos1 = targetWorld.getSpawnPoint();
        entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ,
                entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
        entityplayermp1.setSneaking(false);
        BlockPos spawnLocation = targetWorld.getSpawnPoint();
        entityplayermp1.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(spawnLocation));
        entityplayermp1.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(entityplayermp1.experience, entityplayermp1.experienceTotal,
                entityplayermp1.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityplayermp1, targetWorld);
        targetWorld.getPlayerManager().addPlayer(entityplayermp1);
        targetWorld.spawnEntityInWorld(entityplayermp1);
        this.playerEntityList.add(entityplayermp1);
        entityplayermp1.addSelfToInternalCraftingInventory();
        entityplayermp1.setHealth(entityplayermp1.getHealth());

        FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp1);

        return entityplayermp1;
    }

    @Overwrite
    public void setPlayerManager(WorldServer[] worldServers) {
        if (this.playerNBTManagerObj != null) {
            return;
        }
        this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
        worldServers[0].getWorldBorder().addListener(new PlayerBorderListener());
    }

    @Overwrite
    public void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn) {
        net.minecraft.world.border.WorldBorder worldborder = worldIn.getWorldBorder();
        playerIn.playerNetServerHandler.sendPacket(new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.INITIALIZE));
        playerIn.playerNetServerHandler.sendPacket(new S03PacketTimeUpdate(worldIn.getTotalWorldTime(), worldIn.getWorldTime(), worldIn
                .getGameRules().getGameRuleBooleanValue("doDaylightCycle")));

        if (worldIn.isRaining()) {
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(1, 0.0F));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
        }
    }
}
