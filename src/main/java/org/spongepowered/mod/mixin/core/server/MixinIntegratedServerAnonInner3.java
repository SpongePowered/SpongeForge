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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Optional;

@SideOnly(Side.CLIENT)
@Mixin(targets = "net/minecraft/server/integrated/IntegratedServer$3")
public class MixinIntegratedServerAnonInner3 {

    /**
     * @author Simon816
     *
     * PlayerQuitEvent must be fired manually just before playerLoggedOut.
     *
     * @see MixinServerConfigurationManager#onFirePlayerLoggedOutCall
     */
    @ModifyArg(method = "run()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    public EntityPlayerMP beforeFirePlayerLoggedOut(EntityPlayerMP playerIn) {
        Player player = (Player) playerIn;
        MessageChannel originalChannel = player.getMessageChannel();
        ClientConnectionEvent.Disconnect event = SpongeImplHooks.createClientConnectionEventDisconnect(
                Cause.of(NamedCause.source(player)), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), player, true
        );
        SpongeImpl.postEvent(event);
        // Doesn't make sense to send the event's message because all players
        // are quitting anyway
        return playerIn;
    }
}
