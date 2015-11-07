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
package org.spongepowered.mod.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.mod.network.SpongeModMessageHandler;

import java.util.Optional;

public class MessageTrackerDataRequest implements IMessage, IMessageHandler<MessageTrackerDataRequest, IMessage> {

    private int type;
    private int entityId;
    private int x;
    private int y;
    private int z;

    public MessageTrackerDataRequest() {
    }

    public MessageTrackerDataRequest(int type, int entityId, int x, int y, int z) {
        this.type = type;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.type);
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.type = buffer.readInt();
        this.entityId = buffer.readInt();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
    }

    @Override
    public IMessage onMessage(MessageTrackerDataRequest message, MessageContext ctx) {
        EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        if (!((Player) sender).hasPermission("sponge.debug.block-tracking")) {
            return null;
        }

        BlockPos pos = new BlockPos(message.x, message.y, message.z);
        if (!sender.worldObj.isBlockLoaded(pos)) {
            return null;
        }

        String ownerName = "";
        String notifierName = "";
        Optional<User> owner = Optional.empty();
        Optional<User> notifier = Optional.empty();

        if (message.type == 0) { // block
            IMixinChunk spongeChunk = (IMixinChunk) sender.worldObj.getChunkFromBlockCoords(pos);
            owner = spongeChunk.getBlockOwner(pos);
            notifier = spongeChunk.getBlockNotifier(pos);
        } else if (message.type == 1) { // entity
            Entity entity = sender.worldObj.getEntityByID(message.entityId);
            if (entity != null) {
                IMixinEntity spongeEntity = (IMixinEntity) entity;
                owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                notifier = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
            }
        }

        ownerName = owner.isPresent() ? owner.get().getName() : "";
        notifierName = notifier.isPresent() ? notifier.get().getName() : "";
        SpongeModMessageHandler.INSTANCE.sendTo(new MessageTrackerDataResponse(ownerName, notifierName), sender);
        return null;
    }

    @Override
    public String toString() {
        return String.format("MessageTrackerDataRequest - %d @%d, %d, %d", this.x, this.y, this.z);
    }
}
