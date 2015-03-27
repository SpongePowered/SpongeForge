/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.granite;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.BlockBreakEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.granite.block.GraniteBlockSnapshot;
import org.spongepowered.granite.wrapper.BlockWrapper;

public class GraniteHooks {

    /**
     * Hook that prepares server logic for the firing of a {@link BlockBreakEvent}.
     * @param world The world
     * @param gameType The gametype
     * @param entityPlayer The player
     * @param pos The position
     * @return The called event
     */
    public static BlockBreakEvent onBlockBreakEvent(World world, WorldSettings.GameType gameType, EntityPlayerMP entityPlayer, BlockPos pos) {
        boolean preCancelEvent = false;
        if (gameType.isCreative() && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof ItemSword) {
            preCancelEvent = true;
        }
        if (gameType.isAdventure()) {
            if (gameType == WorldSettings.GameType.SPECTATOR) {
                preCancelEvent = true;
            }

            if (!entityPlayer.isAllowEdit()) {
                ItemStack itemstack = entityPlayer.getCurrentEquippedItem();
                if (itemstack == null || !itemstack.canDestroy(world.getBlockState(pos).getBlock())) {
                    preCancelEvent = true;
                }
            }
        }

        // Tell client the block is gone immediately then process events
        if (world.getTileEntity(pos) == null) {
            S23PacketBlockChange packet = new S23PacketBlockChange(world, pos);
            packet.blockState = Blocks.air.getDefaultState();
            entityPlayer.playerNetServerHandler.sendPacket(packet);
        }

        // Post the block break event
        // TODO Cause/Reason
        BlockBreakEvent event = SpongeEventFactory.createBlockBreak(Granite.instance.getGame(), new Cause(null, pos, null), new BlockWrapper((org
                .spongepowered.api.world.World) world, pos), new GraniteBlockSnapshot(world, pos), 0);
        event.setCancelled(preCancelEvent);
        Granite.instance.getGame().getEventManager().post(event);

        if (event.isCancelled()) {
            // Let the client know the block still exists
            entityPlayer.playerNetServerHandler.sendPacket(new S23PacketBlockChange(world, pos));

            // Update any tile entity data for this block
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity != null) {
                Packet packet = tileentity.getDescriptionPacket();
                if (packet != null) {
                    entityPlayer.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
        return event;
    }

}
