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
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerRespawnEvent;
import org.spongepowered.api.event.world.WorldLoadEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.event.SpongeImplEventFactory;
import org.spongepowered.mod.interfaces.IMixinPlayerRespawnEvent;

@Mixin(value = SpongeImplEventFactory.class, remap = false)
public abstract class MixinSpongeImplEventFactory {

    @Overwrite
    public static WorldLoadEvent createWorldLoad(Game game, org.spongepowered.api.world.World world) {
        return (WorldLoadEvent) new WorldEvent.Load((World) world);
    }

    @Overwrite
    public static PlayerJoinEvent createPlayerJoin(Game game, Player player, Location location, Text text, MessageSink sink) {
        final PlayerJoinEvent event = (PlayerJoinEvent) new PlayerEvent.PlayerLoggedInEvent((EntityPlayer) player);
        event.setLocation(location);
        event.setSink(sink);
        event.setNewMessage(text);
        return event;
    }

    @Overwrite
    public static PlayerRespawnEvent createPlayerRespawn(Game game, Player player, boolean isBedSpawn, Location respawnLocation) {
        final PlayerRespawnEvent event = (PlayerRespawnEvent) new PlayerEvent.PlayerRespawnEvent((EntityPlayer) player);
        ((IMixinPlayerRespawnEvent) event).setIsBedSpawn(isBedSpawn);
        event.setNewRespawnLocation(respawnLocation);
        return event;
    }
}
