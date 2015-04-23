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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.InvocationCommandException;
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
        net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(this.command, sender, args);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
            if (event.exception != null) {
                throw new InvocationCommandException(Texts.of("Error while firing Forge event"), event.exception);
            }
            return false;
        }
        return super.throwEvent(sender, args);
    }
}
