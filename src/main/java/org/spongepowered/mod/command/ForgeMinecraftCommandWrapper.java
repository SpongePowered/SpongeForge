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

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.command.MinecraftCommandWrapper;

/**
 * Command wrapper throwing forge events.
 */
public class ForgeMinecraftCommandWrapper extends MinecraftCommandWrapper {

    public ForgeMinecraftCommandWrapper(PluginContainer owner, ICommand command) {
        super(owner, command);
    }

    @Override
    protected boolean throwEvent(ICommandSender sender, String[] args) throws InvocationCommandException {
        CommandEvent event = new CommandEvent(this.command, sender, args);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            if (event.getException() != null) {
                throw new InvocationCommandException(Text.of("Error while firing Forge event"), event.getException());
            }
            return false;
        }
        return super.throwEvent(sender, args);
    }

    @Override
    public boolean suppressDuplicateAlias(String alias) {
        SpongeImpl.getLogger().warn("The mod {} has registered multiple commands for the alias '{}'. This is probably indicative of a bug.", this.getOwner().getId(), alias);
        return true;
    }
}
