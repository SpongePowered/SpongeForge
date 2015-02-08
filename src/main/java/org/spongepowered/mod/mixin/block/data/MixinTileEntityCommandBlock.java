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
package org.spongepowered.mod.mixin.block.data;

import com.google.common.base.Optional;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.data.CommandBlock;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.message.SpongeMessage;

@NonnullByDefault
@Implements(@Interface(iface = CommandBlock.class, prefix = "command$"))
@Mixin(net.minecraft.tileentity.TileEntityCommandBlock.class)
public abstract class MixinTileEntityCommandBlock extends TileEntity {

    @Shadow
    public abstract CommandBlockLogic getCommandBlockLogic();

    public String command$getStoredCommand() {
        return getCommandBlockLogic().commandStored;
    }

    public void command$setStoredCommand(String command) {
        getCommandBlockLogic().setCommand(command);
    }

    public int command$getSuccessCount() {
        return getCommandBlockLogic().getSuccessCount();
    }

    public void command$setSuccessCount(int count) {
        getCommandBlockLogic().successCount = count;
    }

    public boolean command$doesTrackOutput() {
        return getCommandBlockLogic().func_175571_m();
    }

    public void command$shouldTrackOutput(boolean track) {
        getCommandBlockLogic().func_175573_a(track);
    }

    public Optional<Message> command$getLastOutput() {
        return Optional.fromNullable(SpongeMessage.of(getCommandBlockLogic().getLastOutput()));
    }

    public void command$setLastOutput(Message message) {
        getCommandBlockLogic().func_145750_b(((SpongeMessage) message).getHandle());
    }

    void command$execute() {
        getCommandBlockLogic().trigger(getWorld());
    }

}
