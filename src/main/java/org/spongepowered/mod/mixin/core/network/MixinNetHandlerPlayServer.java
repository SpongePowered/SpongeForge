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
package org.spongepowered.mod.mixin.core.network;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.text.SpongeTexts;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow public EntityPlayerMP playerEntity;
    @Shadow private int chatSpamThresholdCount;
    @Shadow private static Logger logger;
    @Shadow public NetworkManager netManager;
    @Shadow private MinecraftServer serverController;
    @Shadow private boolean hasMoved;

    @Shadow public abstract void sendPacket(final Packet packetIn);
    @Shadow public abstract void kickPlayerFromServer(String message);

    @Inject(method = "processChatMessage", at = @At(value = "INVOKE", target = "net.minecraftforge.common.ForgeHooks.onServerChatEvent"
            + "(Lnet/minecraft/network/NetHandlerPlayServer;Ljava/lang/String;Lnet/minecraft/util/ChatComponentTranslation;)"
            + "Lnet/minecraft/util/ChatComponentTranslation;", remap = false),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void injectChatEvent(C01PacketChatMessage packetIn, CallbackInfo ci, String s, ChatComponentTranslation component) {
        final ServerChatEvent event = new ServerChatEvent(this.playerEntity, s, component);
        final Text playerName = SpongeTexts.toText((IChatComponent) component.getFormatArgs()[0]);
        ((MessageSinkEvent.SourcePlayer) event).setUnformattedMessage(SpongeTexts.toText((IChatComponent) component.getFormatArgs()[1]));

        if (!MinecraftForge.EVENT_BUS.post(event)) {
            MessageSinkEvent.SourcePlayer spongeEvent = (MessageSinkEvent.SourcePlayer) event;
            Text returned = Texts.format(spongeEvent.getMessage(), ImmutableMap.of(MessageSinkEvent.SourcePlayer.PLACEHOLDER_NAME, playerName,
                    MessageSinkEvent.SourcePlayer.PLACEHOLDER_MESSAGE, spongeEvent.getUnformattedMessage()));
            spongeEvent.getSink().sendMessage(returned);

            // Chat spam suppression from MC
            this.chatSpamThresholdCount += 20;
            if (this.chatSpamThresholdCount > 200 && !MinecraftServer.getServer().getConfigurationManager()
                    .canSendCommands(this.playerEntity.getGameProfile())) {
                this.kickPlayerFromServer("disconnect.spam");
            }
        }

        ci.cancel();
    }

    @Redirect(method = "processPlayerBlockPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerInteract("
            + "Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$Action;"
            + "Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/util/EnumFacing;)"
            + "Lnet/minecraftforge/event/entity/player/PlayerInteractEvent;", remap = false))
    public PlayerInteractEvent onFirePlayerInteractEvent(EntityPlayer player, PlayerInteractEvent.Action action, net.minecraft.world.World world,
        BlockPos pos,
        EnumFacing face) {

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, pos, face, world);
        double reach = this.playerEntity.theItemInWorldManager.getGameType() == WorldSettings.GameType.CREATIVE ? 5 : 4.5;
        Optional<BlockRayHit<World>> attempt =
            BlockRay.from((Player)this.playerEntity)
            .filter(BlockRay.<World>maxDistanceFilter(((Player) this.playerEntity).getLocation().getPosition(), reach))
            .end();
        boolean missed;

        if (attempt.isPresent()) {
            BlockRayHit<World> hit = attempt.get();
            missed = hit.getExtent().getBlockType(hit.getBlockPosition()).equals(BlockTypes.AIR);
        } else {
            missed = true;
        }

        // If missed is false, then then the event will never actually be fired. However, it still needs to be returned
        // from the redirect. event.useItem is set to DEFAULT, to trigger the call to ItemInWorldManager#tryUseItem
        if (missed) {
            MinecraftForge.EVENT_BUS.post(event);
        } else {
            event.useItem = Event.Result.DEFAULT;
        }

        return event;
    }
}
