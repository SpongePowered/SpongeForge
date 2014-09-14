/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
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

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.mod.SpongeGame;

import java.util.HashMap;
import java.util.Map;

public class SpongeCommandManager implements CommandManager {
    private Map<String, Command> commands = new HashMap<String, Command>();
    private SpongeGame game;
    private MinecraftServer server;

    public SpongeCommandManager(SpongeGame game, MinecraftServer server){
        this.game = game;
        this.server = server;
    }

    @Override
    public void registerCommand(Command command) {
        if (commands.containsKey(command.getCommandName()) || server.getCommandManager().getCommands().containsKey(command.getCommandName()))
            throw new CommandException("Command is already used!");
        commands.put(command.getCommandName(), command);
        CommandHandler ch = (CommandHandler) server.getCommandManager();
        ch.registerCommand(new SpongeCommand(command));
    }

    @Override
    public Map<String, Command> getCommands() {
        return commands;
    }
}
