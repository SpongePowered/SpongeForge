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
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(modid = ICommandPermissionTest.MOD_ID, name = "ICommand Permission Test", acceptableRemoteVersions = "*")
public class ICommandPermissionTest {

    public static final String MOD_ID = "icommand-test";

    @Mod.EventHandler
    public void onStart(FMLServerStartingEvent event) {
        event.registerServerCommand(
                new ICommand() {
                    @Override
                    public String getName() {
                        return "perms-true-test";
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
                    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                        sender.sendMessage(new TextComponentString("No op level test"));
                    }

                    @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
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

        event.registerServerCommand(
                new ICommand() {
                    @Override
                    public String getName() {
                        return "perms-0-test";
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
                    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                        sender.sendMessage(new TextComponentString("OP level 0 test"));
                    }

                    @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
                        return sender.canUseCommand(0, getName());
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

        event.registerServerCommand(
                new ICommand() {
                    @Override
                    public String getName() {
                        return "perms-1-test";
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
                    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                        sender.sendMessage(new TextComponentString("OP level 1 test"));
                    }

                    @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
                        return sender.canUseCommand(1, getName());
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
}
