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
package org.spongepowered.mod.mixin.core.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.event.SpongeImplEventFactory;
import org.spongepowered.mod.interfaces.IMixinPlayerRespawnEvent;

@Mixin(value = SpongeImplEventFactory.class, remap = false)
public abstract class MixinSpongeImplEventFactory {

    @Overwrite
    public static LoadWorldEvent createLoadWorldEvent(Game game, World world) {
        return (LoadWorldEvent) new WorldEvent.Load((net.minecraft.world.World) world);
    }

    @Overwrite
    public static ClientConnectionEvent.Join createClientConnectionEventJoin(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Transform<World> fromTransform, Transform<World> toTransform, Player targetEntity) {
        final ClientConnectionEvent.Join event = (ClientConnectionEvent.Join) new PlayerEvent.PlayerLoggedInEvent((EntityPlayer) targetEntity);
        event.getTargetEntity().setLocation(toTransform.getLocation());
        event.setSink(sink);
        event.setMessage(message);
        return event;
    }

    @Overwrite
    public static RespawnPlayerEvent createRespawnPlayerEvent(Game game, Cause cause, Transform<World> fromTransform, Transform<World> toTransform, Player targetEntity, boolean bedSpawn) {
        final RespawnPlayerEvent event = (RespawnPlayerEvent) new PlayerEvent.PlayerRespawnEvent((EntityPlayer) targetEntity);
        ((IMixinPlayerRespawnEvent) event).setIsBedSpawn(bedSpawn);
        event.getFromTransform().setLocation(toTransform.getLocation());
        return event;
    }

    @Overwrite
    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Player targetEntity) {
        final ClientConnectionEvent.Disconnect event = (ClientConnectionEvent.Disconnect) new PlayerEvent.PlayerLoggedOutEvent((EntityPlayer) targetEntity);
        event.setMessage(message);
        event.setSink(sink);
        return event;
    }
}
