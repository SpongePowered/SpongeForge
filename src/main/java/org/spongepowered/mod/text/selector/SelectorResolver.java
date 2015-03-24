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
package org.spongepowered.mod.text.selector;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.mod.util.VecHelper;

public class SelectorResolver implements ICommandSender {

    private final World world;
    private final Vec3 position;

    private SelectorResolver(Extent extent, Vector3d position) {
        this.world = (World) extent;
        this.position = position != null ? VecHelper.toVector(position) : null;
    }

    public SelectorResolver(Extent extent) {
        this(extent, null);
    }

    public SelectorResolver(Location location) {
        this(location.getExtent(), location.getPosition());
    }

    @Override
    public String getCommandSenderName() {
        return "SelectorResolver";
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText(getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent component) {
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return false;
    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(this.position);
    }

    @Override
    public Vec3 getPositionVector() {
        return this.position;
    }

    @Override
    public World getEntityWorld() {
        return this.world;
    }

    @Override
    public Entity getCommandSenderEntity() {
        return null;
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {

    }

}
