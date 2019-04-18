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
package org.spongepowered.mod.test;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

@Mod(modid = ICommandSenderExecuteTest.MOD_ID, name = "ICommandSender Execution Test", acceptableRemoteVersions = "*")
public class ICommandSenderExecuteTest {

    public static final String MOD_ID = "icommandsender-test";
    private static final Text HELLO_WORLD = Text.of("Hello World");
    private static ICommandSenderExecuteTest INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Sponge.getCommandManager().register(this, getTestCommand(), "icmdsendertest");
    }

    @Mod.EventHandler
    public void onStart(FMLServerStartingEvent event) {
        event.registerServerCommand(
                new ICommand() {
                    @Override
                    public String getName() {
                        return "helloworld";
                    }

                    @Override
                    public String getUsage(ICommandSender sender) {
                        return "";
                    }

                    @Override
                    public List<String> getAliases() {
                        return ImmutableList.of();
                    }

                    @Override
                    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                        Sponge.getServer().getConsole().sendMessage(HELLO_WORLD);
                    }

                    @Override
                    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
                        return true;
                    }

                    @Override
                    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                            @Nullable BlockPos targetPos) {
                        return ImmutableList.of();
                    }

                    @Override
                    public boolean isUsernameIndex(String[] args, int index) {
                        return false;
                    }

                    @Override
                    public int compareTo(ICommand o) {
                        return 0;
                    }
                }
        );
    }

    private static CommandSpec getTestCommand() {

        return CommandSpec.builder()
                .executor((source, arg) -> {
                    ICommandSender customSender = new ICommandSender() {
                        @Override
                        public String getName() {
                            return "Custom Element";
                        }

                        @Override
                        public boolean canUseCommand(int permLevel, String commandName) {
                            return true;
                        }

                        @Override
                        public World getEntityWorld() {
                            return (World) Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName())
                                    .orElseThrow(IllegalStateException::new);
                        }

                        @Nullable
                        @Override
                        public MinecraftServer getServer() {
                            return (MinecraftServer) Sponge.getServer();
                        }
                    };

                    ICommandSender sender = CommandSenderWrapper.create(customSender).withPermissionLevel(2);
                    ((MinecraftServer) Sponge.getServer()).getCommandManager()
                            .executeCommand(sender, "helloworld");
                    return CommandResult.success();
                })
                .build();
    }

}
