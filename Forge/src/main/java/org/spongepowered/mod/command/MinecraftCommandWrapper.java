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
package org.spongepowered.mod.command;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.mod.SpongeMod;

import java.util.List;

/**
 * Wrapper around ICommands so they fit into the Sponge command system.
 */
public class MinecraftCommandWrapper implements CommandCallable {
    private static final String
                TRANSLATION_NO_PERMISSION = "commands.generic.permission";
    private final ModContainer activeMod;
    private final ICommand command;

    public MinecraftCommandWrapper(ModContainer activeMod, ICommand command) {
        this.activeMod = activeMod;
        this.command = command;
    }

    @Override
    public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException {
        CommandHandler handler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
        final ICommandSender mcSender = source instanceof ICommandSender ? (ICommandSender) source : new WrapperICommandSender(source);
        final String[] args = arguments.split(" ");
        int usernameIndex = handler.getUsernameIndex(this.command, args);
        int successCount = 0;

        // Below this is copied from CommandHandler.execute. This might need to be updated between versions.
        if (testPermission(source)) {
            net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(this.command, mcSender, args);
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
                if (event.exception != null) {
                    com.google.common.base.Throwables.propagateIfPossible(event.exception);
                }
                return false;
            }

            if (usernameIndex > -1) {
                @SuppressWarnings("unchecked")
                List<Entity> list = PlayerSelector.matchEntities(mcSender, args[usernameIndex], Entity.class);
                String previousNameVal = args[usernameIndex];
                mcSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

                for (Entity entity : list) {
                    args[usernameIndex] = entity.getUniqueID().toString();

                    if (handler.tryExecute(mcSender, args, this.command, arguments)) {
                        ++successCount;
                    }
                }
                args[usernameIndex] = previousNameVal;
            } else {
                mcSender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);

                if (handler.tryExecute(mcSender, args, this.command, arguments)) {
                    ++successCount;
                }
            }
        } else {
            source.sendMessage(Texts
                    .builder(SpongeMod.instance.getGame().getRegistry().getTranslationById(TRANSLATION_NO_PERMISSION).get(), new Object[0])
                    .color(TextColors.RED)
                    .build());
        }

        mcSender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, successCount);
        return successCount > 0;
    }

    public int getPermissionLevel() {
        return this.command instanceof CommandBase ? ((CommandBase) this.command).getRequiredPermissionLevel() : -1;
    }

    public String getCommandPermission() {
        return this.activeMod.getModId().toLowerCase() + ".command." + this.command.getCommandName();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        if (source instanceof ICommandSender) {
            return this.command.canCommandSenderUseCommand((ICommandSender) source);
        } else {
            return source.hasPermission(getCommandPermission());
        }
    }

    @Override
    public String getShortDescription(CommandSource source) {
        return null;
    }

    @Override
    public Text getHelp(CommandSource source) {
        return null;
    }

    @Override
    public String getUsage(CommandSource source) {
        final ICommandSender mcSender = source instanceof ICommandSender ? (ICommandSender) source : new WrapperICommandSender(source);
        return this.command.getCommandUsage(mcSender);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return this.command.addTabCompletionOptions((ICommandSender) source, arguments.split(" "), null);
    }

    public ModContainer getMod() {
        return this.activeMod;
    }

    @SuppressWarnings("unchecked")
    public List<String> getNames() {
        return ImmutableList.<String>builder().add(this.command.getCommandName()).addAll(this.command.getCommandAliases()).build();
    }
}
