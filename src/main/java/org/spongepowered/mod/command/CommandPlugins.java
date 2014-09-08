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

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collection;

public class CommandPlugins extends CommandBase {

    private Game game;

    public CommandPlugins(Game game){
        this.game = game;
    }

    @Override
    public String getCommandName() {
        return "plugins";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Shows a list of all the installed plugins";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        StringBuilder pluginlist = new StringBuilder();
        Collection<PluginContainer> plugins = game.getPluginManager().getPlugins();

        pluginlist.append("(" + plugins.size() + ")");
        for (PluginContainer plugin : plugins){
            if (plugins.size() != 0)
                pluginlist.append(",");
            pluginlist.append(plugin.getName() + " " + plugin.getVersion());
        }
        sender.addChatMessage(new ChatComponentText(pluginlist.toString()));
    }
}
