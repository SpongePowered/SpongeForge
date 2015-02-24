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
package org.spongepowered.mod.mixin.core.server;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;

@NonnullByDefault
@Mixin(ServerCommandManager.class)
public abstract class MixinServerCommandManager extends CommandHandler {

    @Override
    public int executeCommand(ICommandSender sender, String command) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        String name = command;
        String args = "";
        int pos = name.indexOf(' ');
        if (pos > -1) {
            name = command.substring(0, pos);
            args = command.substring(pos + 1);
        }

        Game game = SpongeMod.instance.getGame();
        if (game.getEventManager().post(SpongeEventFactory.createCommand(game, args, (CommandSource) sender, name))) {
            return 1;
        }

        return super.executeCommand(sender, command); // Try Vanilla instead
    }

}
