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
package org.spongepowered.mod.mixin.core.server;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MinecraftDummyContainer;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.command.MinecraftCommandWrapper;
import org.spongepowered.mod.service.permission.SpongePermissionService;

import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(ServerCommandManager.class)
public abstract class MixinServerCommandManager extends CommandHandler implements org.spongepowered.mod.interfaces.IMixinServerCommandManager {
    private static final String
                TRANSLATION_COMMAND_NOT_FOUND = "commands.generic.notFound";

    private List<MinecraftCommandWrapper> lowPriorityCommands;
    private List<MinecraftCommandWrapper> earlyRegisterCommands;

    private void updateLists() {
        this.lowPriorityCommands = Lists.newArrayList();
        this.earlyRegisterCommands = Lists.newArrayList();
    }

    private void updateStat(ICommandSender sender, CommandResultStats.Type type, Optional<Integer> count) {
        if (count.isPresent()) {
            sender.setCommandStat(type, count.get());
        }
    }

    @Override
    public int executeCommand(ICommandSender sender, String command) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        CommandSource source = ((CommandSource) sender);
        Game game = SpongeMod.instance.getGame();
        Optional<CommandResult> resultOpt;
        if ((resultOpt = game.getCommandDispatcher().process(source, command)).isPresent()) {
            CommandResult result = resultOpt.get();
            updateStat(sender, CommandResultStats.Type.AFFECTED_BLOCKS, result.getAffectedBlocks());
            updateStat(sender, CommandResultStats.Type.AFFECTED_ENTITIES, result.getAffectedEntities());
            updateStat(sender, CommandResultStats.Type.AFFECTED_ITEMS, result.getAffectedItems());
            updateStat(sender, CommandResultStats.Type.QUERY_RESULT, result.getQueryResult());
            updateStat(sender, CommandResultStats.Type.SUCCESS_COUNT, result.getSuccessCount());
            return resultOpt.get().getSuccessCount().or(1);
        } else {
            source.sendMessage(Texts.builder(game.getRegistry().getTranslationById(TRANSLATION_COMMAND_NOT_FOUND).get())
                    .color(TextColors.RED).build());
            return 0;
        }

        //return super.executeCommand(sender, command); // Try Vanilla instead
    }

    @Override
    public ICommand registerCommand(ICommand command) {
        if (this.lowPriorityCommands == null) { // TODO: Not have to work around the lack of ability to inject field initializers?
            updateLists();
        }
        ModContainer activeContainer = Loader.instance().activeModContainer();
        if (activeContainer == null) {
            activeContainer = Loader.instance().getMinecraftModContainer();
        }


        MinecraftCommandWrapper cmd = new MinecraftCommandWrapper(activeContainer, command);
        if (activeContainer instanceof MinecraftDummyContainer) {
            this.lowPriorityCommands.add(cmd);
        } else if (SpongeMod.instance == null) {
            this.earlyRegisterCommands.add(cmd);
        } else {
            SpongeMod.instance.getGame().getCommandDispatcher().register(cmd.getMod(), cmd, cmd.getNames());
            registerDefaultPermissions(SpongeMod.instance.getGame(), cmd);
        }
        return super.registerCommand(command);
    }

    private void registerDefaultPermissions(Game game, MinecraftCommandWrapper cmd) {
        Optional<PermissionService> perms = game.getServiceManager().provide(PermissionService.class);
        if (perms.isPresent()) {
            PermissionService service = perms.get();
            int opLevel = cmd.getPermissionLevel();
            if (opLevel == -1) {
                return;
            }
            if (service instanceof SpongePermissionService) {
                ((SpongePermissionService) service).getGroupForOpLevel(opLevel).getTransientData().setPermission(SubjectData.GLOBAL_CONTEXT,
                        cmd.getCommandPermission(), Tristate.TRUE);
            } else if (opLevel == 0) {
                service.getDefaultData().setPermission(SubjectData.GLOBAL_CONTEXT, cmd.getCommandPermission(),
                        Tristate.TRUE);
            }
        }
    }

    private void registerCommandsList(List<MinecraftCommandWrapper> cmds, Game game) {
        for (Iterator<MinecraftCommandWrapper> it = cmds.iterator(); it.hasNext();) {
            MinecraftCommandWrapper cmd = it.next();
            it.remove();
            game.getCommandDispatcher().register(cmd.getMod(), cmd, cmd.getNames());
            registerDefaultPermissions(game, cmd);
        }
    }

    @Override
    public void registerLowPriorityCommands(Game game) {
        registerCommandsList(this.lowPriorityCommands, game);
    }

    @Override
    public void registerEarlyCommands(Game game) {
        registerCommandsList(this.earlyRegisterCommands, game);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getTabCompletionOptions(ICommandSender sender, String input, BlockPos pos) {
        CommandService service = SpongeMod.instance.getGame().getCommandDispatcher();
        CommandSource source = (CommandSource) sender;
        return service.getSuggestions(source, input);
    }
}
