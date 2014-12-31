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
package org.spongepowered.mod.text.selector;

import javax.annotation.Nullable;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.api.world.Location;

import com.flowpowered.math.vector.Vector3d;

class FakeCommandSender implements ICommandSender {

    private final Location location;
    private final Vec3 positionVec3;

    public FakeCommandSender(@Nullable Location location) {
        this.location = location;
        if (location != null) {
            Vector3d pos = location.getPosition();
            this.positionVec3 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        } else {
            this.positionVec3 = null;
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public IChatComponent getDisplayName() {
        return null;
    }

    @Override
    public void addChatMessage(IChatComponent message) {
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        // TODO Permissions hook?
        return true;
    }

    @Override
    public BlockPos getPosition() {
        return this.location == null ? null : new BlockPos(this.positionVec3);
    }

    @Override
    public Vec3 getPositionVector() {
        return this.positionVec3;
    }

    @Override
    public World getEntityWorld() {
        return this.location == null ? null : (World) this.location.getExtent();
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
    public void func_174794_a(Type p_174794_1_, int p_174794_2_) {
    }

}
