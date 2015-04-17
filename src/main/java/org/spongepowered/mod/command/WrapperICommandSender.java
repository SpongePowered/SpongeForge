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
package org.spongepowered.mod.command;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.mod.text.SpongeChatComponent;

/**
 * Wrapper around a CommandSource that is not part of the base game to allow it to access MC commands.
 */
public class WrapperICommandSender implements ICommandSender {
    private final CommandSource source;

    public WrapperICommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public String getCommandSenderName() {
        return this.source.getName();
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText(this.source.getName());
    }

    @Override
    public void addChatMessage(IChatComponent component) {
        this.source.sendMessage(((SpongeChatComponent) component).toText());
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return this.source.hasPermission(commandName);
    }

    @Override
    public BlockPos getPosition() {
        return BlockPos.ORIGIN;
    }

    @Override
    public Vec3 getPositionVector() {
        return new Vec3(0, 0, 0);
    }

    @Override
    public World getEntityWorld() {
        return null;
    }

    @Override
    public Entity getCommandSenderEntity() {
        return null;
    }

    @Override
    public boolean sendCommandFeedback() {
        return true;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {

    }
}
