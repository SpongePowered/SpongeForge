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
package org.spongepowered.mod.mixin.core.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
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
import org.spongepowered.common.SpongeImplFactory;
import org.spongepowered.mod.interfaces.IMixinPlayerRespawnEvent;

@Mixin(value = SpongeImplFactory.class, remap = false)
public abstract class MixinSpongeImplFactory {

    @Overwrite
    public static LoadWorldEvent createLoadWorldEvent(Game game, World world) {
        return (LoadWorldEvent) new WorldEvent.Load((net.minecraft.world.World) world);
    }

    @Overwrite
    public static ClientConnectionEvent.Join createClientConnectionEventJoin(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Player targetEntity) {
        final ClientConnectionEvent.Join event = (ClientConnectionEvent.Join) new PlayerEvent.PlayerLoggedInEvent((EntityPlayer) targetEntity);
        event.setSink(sink);
        event.setMessage(message);
        return event;
    }

    @Overwrite
    public static RespawnPlayerEvent createRespawnPlayerEvent(Game game, Cause cause, Transform<World> fromTransform, Transform<World> toTransform, Player targetEntity, boolean bedSpawn) {
        final RespawnPlayerEvent event = (RespawnPlayerEvent) new PlayerEvent.PlayerRespawnEvent((EntityPlayer) targetEntity);
        ((IMixinPlayerRespawnEvent) event).setIsBedSpawn(bedSpawn);
        event.setToTransform(toTransform);
        return event;
    }

    @Overwrite
    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Player targetEntity) {
        final ClientConnectionEvent.Disconnect event = (ClientConnectionEvent.Disconnect) new PlayerEvent.PlayerLoggedOutEvent((EntityPlayer) targetEntity);
        event.setMessage(message);
        event.setSink(sink);
        return event;
    }

    @Overwrite
    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block.hasTileEntity(state);
    }

    @Overwrite
    public static int getBlockLightValue(Block block, BlockPos pos, IBlockAccess world) {
        return block.getLightValue(world, pos);
    }

    @Overwrite
    public static int getBlockLightOpacity(Block block, IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity(world, pos);
    }

    @Overwrite
    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return tile.shouldRefresh(world, pos, oldState, newState);
    }
}
