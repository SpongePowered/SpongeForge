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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandPermissionException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.InvocationCommandException;
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

    public MinecraftCommandWrapper(final ModContainer activeMod, final ICommand command) {
        this.activeMod = activeMod;
        this.command = command;
    }

    private static ICommandSender sourceToSender(CommandSource source) {
        return source instanceof ICommandSender ? (ICommandSender) source : new WrapperICommandSender(source);
    }

    private String[] splitArgs(String arguments) {
        return arguments.isEmpty() ? new String[0] : arguments.split(" +");
    }

    @Override
    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException {

        if (!testPermission(source)) {
            throw new CommandPermissionException(Texts.of(SpongeMod.instance.getGame().getRegistry()
                    .getTranslationById(TRANSLATION_NO_PERMISSION).get()));
        }

        CommandHandler handler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
        final ICommandSender mcSender = sourceToSender(source);
        final String[] splitArgs = splitArgs(arguments);
        int usernameIndex = handler.getUsernameIndex(this.command, splitArgs);
        int successCount = 0;


        // Below this is copied from CommandHandler.execute. This might need to be updated between versions.
        net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(this.command, mcSender, splitArgs);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
            if (event.exception != null) {
                throw new InvocationCommandException(Texts.of("Error while firing Forge event"), event.exception);
            }
            return Optional.of(CommandResult.empty());
        }
        int affectedEntities = 1;
        if (usernameIndex > -1) {
            @SuppressWarnings("unchecked")
            List<Entity> list = PlayerSelector.matchEntities(mcSender, splitArgs[usernameIndex], Entity.class);
            String previousNameVal = splitArgs[usernameIndex];
            affectedEntities = list.size();

            for (Entity entity : list) {
                splitArgs[usernameIndex] = entity.getUniqueID().toString();

                if (handler.tryExecute(mcSender, splitArgs, this.command, arguments)) {
                    ++successCount;
                }
            }
            splitArgs[usernameIndex] = previousNameVal;
        } else {
            if (handler.tryExecute(mcSender, splitArgs, this.command, arguments)) {
                ++successCount;
            }
        }

        return Optional.of(CommandResult.builder()
                .affectedEntities(affectedEntities)
                .successCount(successCount)
                .build());
    }

    public int getPermissionLevel() {
        return this.command instanceof CommandBase ? ((CommandBase) this.command).getRequiredPermissionLevel() : -1;
    }

    public String getCommandPermission() {
        return this.activeMod.getModId().toLowerCase() + ".command." + this.command.getCommandName();
    }

    public ModContainer getMod() {
        return this.activeMod;
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
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.absent();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.absent();
    }

    @Override
    public Text getUsage(CommandSource source) {
        final ICommandSender mcSender =
                source instanceof ICommandSender ? (ICommandSender) source : new WrapperICommandSender(source);
        String usage = this.command.getCommandUsage(mcSender);
        if (usage.startsWith("/") && usage.contains(" ")) {
            usage = usage.substring(usage.indexOf(" "));
        }
        return Texts.of(SpongeMod.instance.getGame().getRegistry().getTranslationById(usage).get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return this.command.addTabCompletionOptions((ICommandSender) source, splitArgs(arguments), null);
    }
    @SuppressWarnings("unchecked")
    public List<String> getNames() {
        return ImmutableList.<String>builder().add(this.command.getCommandName()).addAll(this.command.getCommandAliases()).build();
    }
}
