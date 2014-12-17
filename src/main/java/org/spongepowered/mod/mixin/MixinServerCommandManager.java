package org.spongepowered.mod.mixin;

import java.util.ArrayList;

import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

@NonnullByDefault
@Mixin(ServerCommandManager.class)
public abstract class MixinServerCommandManager extends CommandHandler {

    @Override
    public int executeCommand(ICommandSender sender, String command) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        SimpleCommandService dispatcher = (SimpleCommandService)SpongeMod.instance.getGame().getCommandDispatcher();
        boolean result = true;

        try {
            if (sender instanceof MinecraftServer) {
                result = dispatcher.call((ConsoleSource)sender, command, new ArrayList<String>());
            } else {
                result = dispatcher.call((CommandSource)sender, command, new ArrayList<String>());
            }
        } catch (CommandException e) {
            // Ignore
        }

        if (!result) { // try vanilla
            return super.executeCommand(sender, command);
        }

        return 1;
    }

}
