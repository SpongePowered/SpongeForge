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
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.util.SpongeHooks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommandSponge extends CommandBase {

    @SuppressWarnings("unused")
    private static final List<String> FLAGS = ImmutableList.of("-g", "-d", "-w");
    private static final List<String> FLAG_COMMANDS = ImmutableList.of("save", "chunks", "conf", "reload");
    private static final List<String> COMMANDS = ImmutableList.of("chunks", "conf", "heap", "help", "reload", "save", "version");
    private static final List<String> ALIASES = ImmutableList.of("sp");

    private static final String USAGE_CONF =
            EnumChatFormatting.WHITE + "Usage:\n" + EnumChatFormatting.GREEN + "/sponge conf [-g] [-d dim] [-w world] key value";
    private static final String USAGE_RELOAD =
            EnumChatFormatting.WHITE + "Usage:\n" + EnumChatFormatting.GREEN + "/sponge reload [-g] [-d dim|*] [-w world|*]";
    private static final String USAGE_SAVE =
            EnumChatFormatting.WHITE + "Usage:\n" + EnumChatFormatting.GREEN + "/sponge save [-g] [-d dim|*] [-w world|*]";
    private static final String USAGE_CHUNKS =
            EnumChatFormatting.WHITE + "Usage:\n" + EnumChatFormatting.GREEN + "/sponge chunks [-g] [-d dim] [-w world]";

    @Override
    public String getCommandName() {
        return "sponge";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getCommandAliases() {
        return ALIASES;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/sponge <command> [args]";
    }

    private String getCommandUsage(String command) {
        if (command.equalsIgnoreCase("chunks")) {
            return USAGE_CHUNKS;
        } else if (command.equalsIgnoreCase("reload")) {
            return USAGE_RELOAD;
        } else if (command.equalsIgnoreCase("save")) {
            return USAGE_SAVE;
        }

        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            String command = args[0];
            SpongeConfig<?> config = CoreMixinPlugin.getGlobalConfig();

            if (COMMANDS.contains(command)) {
                if (FLAG_COMMANDS.contains(command)) {

                    String name = "";
                    WorldServer world = null;
                    DimensionType dimensionType = null;
                    if (sender instanceof EntityPlayer) {
                        world = (WorldServer) ((EntityPlayer) sender).worldObj;
                    }

                    if (command.equalsIgnoreCase("conf")) {
                        if (args.length < 3) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Improper conf syntax detected.\n"
                                    + USAGE_CONF));
                            return;
                        }

                        name = args[2];
                    } else {
                        if (args.length < 2) {
                            sender.addChatMessage(new ChatComponentText(
                                    EnumChatFormatting.RED + "Improper " + command + " syntax detected.\n" + getCommandUsage(command)));
                            return;
                        }

                        if (args.length > 2) {
                            name = args[2];
                        }
                    }

                    String flag = args[1];

                    if (flag.equalsIgnoreCase("-d")) {
                        if (name.equals("") || name.equals("*")) {
                            if (sender instanceof EntityPlayer) {
                                config = ((IMixinWorldProvider) ((EntityPlayer) sender).worldObj.provider).getDimensionConfig();
                            } else {
                                sender.addChatMessage(
                                        new ChatComponentText(EnumChatFormatting.RED + "Console requires a valid dimension name.\n"
                                                + USAGE_CONF));
                                return;
                            }
                        } else {
                            for (DimensionType dimType : ((SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry()).getDimensionTypes()) {
                                if (dimType.getName().equalsIgnoreCase(name)) {
                                    config = SpongeGameRegistry.dimensionConfigs.get(dimType.getDimensionClass());
                                    dimensionType = dimType;
                                    break;
                                }
                            }
                            if (config == CoreMixinPlugin.getGlobalConfig()) {
                                sender.addChatMessage(new ChatComponentText("Dimension '" + EnumChatFormatting.RED + name + EnumChatFormatting.WHITE
                                        + "' does not exist. Please enter a valid dimension."));
                                return;
                            }
                        }
                    } else if (flag.equalsIgnoreCase("-w")) {
                        if (name.equals("") || name.equals("*")) {
                            if (sender instanceof EntityPlayer) {
                                config = ((IMixinWorld) ((EntityPlayer) sender).worldObj).getWorldConfig();
                            } else {
                                sender.addChatMessage(
                                        new ChatComponentText(EnumChatFormatting.RED + "Console requires a valid world name.\n"
                                                + USAGE_CONF));
                                return;
                            }
                        } else {
                            for (WorldServer worldserver : DimensionManager.getWorlds()) {
                                if (worldserver.provider.getSaveFolder() == null && name.equalsIgnoreCase("DIM0")) {
                                    config = ((IMixinWorld) worldserver).getWorldConfig();
                                    world = worldserver;
                                    break;
                                }
                                if (worldserver.provider.getSaveFolder() != null && worldserver.provider.getSaveFolder().equalsIgnoreCase(name)) {
                                    config = ((IMixinWorld) worldserver).getWorldConfig();
                                    world = worldserver;
                                    break;
                                }
                            }
                            if (config == CoreMixinPlugin.getGlobalConfig()) {
                                sender.addChatMessage(new ChatComponentText(
                                        EnumChatFormatting.RED + "World " + EnumChatFormatting.AQUA + name + EnumChatFormatting.RED
                                                + " does not exist. Please enter a valid world."));
                                return;
                            }
                        }
                    } else if (flag.equalsIgnoreCase("-g")) {
                        config = CoreMixinPlugin.getGlobalConfig();
                    } else {
                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.AQUA + flag + EnumChatFormatting.RED + " is not recognized as a valid flag.\n"
                                        + USAGE_CONF));
                        return;
                    }

                    if (command.equalsIgnoreCase("save")) {
                        String configName = config.getConfigName();
                        if (name.equals("")) {
                            config.save();
                        } else if (name.equals("*") && config.getType() == SpongeConfig.Type.WORLD) {
                            for (WorldServer worldserver : DimensionManager.getWorlds()) {
                                SpongeConfig<SpongeConfig.WorldConfig> worldConfig = ((IMixinWorld) worldserver).getWorldConfig();
                                worldConfig.save();
                            }
                            configName = "ALL";
                        } else if (name.equals("*") && config.getType() == SpongeConfig.Type.DIMENSION) {
                            for (SpongeConfig<?> dimensionConfig : SpongeGameRegistry.dimensionConfigs.values()) {
                                dimensionConfig.save();
                            }
                            configName = "ALL";
                        }
                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.GREEN + "Saved " + EnumChatFormatting.GOLD + config.getType() + EnumChatFormatting.GREEN
                                        + " configuration: " + EnumChatFormatting.AQUA + configName));
                    } else if (command.equalsIgnoreCase("reload")) {
                        config.reload();
                        String configName = config.getConfigName();
                        if (name.equals("")) {
                            config.reload();
                        } else if (name.equals("*") && config.getType() == SpongeConfig.Type.WORLD) {
                            for (WorldServer worldserver : DimensionManager.getWorlds()) {
                                SpongeConfig<?> worldConfig = ((IMixinWorld) worldserver).getWorldConfig();
                                worldConfig.reload();
                            }
                            configName = "ALL";
                        } else if (name.equals("*") && config.getType() == SpongeConfig.Type.DIMENSION) {
                            for (SpongeConfig<?> dimensionConfig : SpongeGameRegistry.dimensionConfigs.values()) {
                                dimensionConfig.reload();
                            }
                            configName = "ALL";
                        }

                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.GREEN + "Reloaded " + EnumChatFormatting.GOLD + config.getType() + EnumChatFormatting.GREEN
                                        + " configuration: " + EnumChatFormatting.AQUA + configName));
                    } else if (command.equalsIgnoreCase("chunks")) {
                        processChunks(config.getType(), world, dimensionType, sender, args);
                    } else {
                        if (config.getSetting(args[args.length - 1]) != null) {
                            getToggle(config, sender, args[args.length - 1]);
                        } else {
                            setToggle(config, sender, args[args.length - 2], args[args.length - 1]);
                        }
                    }

                } else if ("version".equalsIgnoreCase(command)) {
                    sender.addChatMessage(new ChatComponentText(
                            "SpongeMod : " + EnumChatFormatting.GREEN + SpongeMod.instance.getGame().getImplementationVersion() + "\n"
                                    + "SpongeAPI : " + EnumChatFormatting.GREEN + SpongeMod.instance.getGame().getApiVersion()));
                } else if (command.equalsIgnoreCase("heap")) {
                    processHeap(sender, args);
                } else if (command.equalsIgnoreCase("help")) {
                    sender.addChatMessage(new ChatComponentText("commands:\n"
                            + "    " + EnumChatFormatting.GREEN + "chunks   " + EnumChatFormatting.WHITE + "     "
                            + "Prints chunk data for a specific dimension or world(s)\n"
                            + "    " + EnumChatFormatting.GREEN + "conf   " + EnumChatFormatting.WHITE + "     " + "Configure sponge settings\n"
                            + "    " + EnumChatFormatting.GREEN + "heap   " + EnumChatFormatting.WHITE + "     " + "Dump live JVM heap\n"
                            + "    " + EnumChatFormatting.GREEN + "reload   " + EnumChatFormatting.WHITE + "     "
                            + "Reloads a global, dimension, or world config\n"
                            + "    " + EnumChatFormatting.GREEN + "save   " + EnumChatFormatting.WHITE + "     "
                            + "Saves a global, dimension, or world config\n"
                            + "    " + EnumChatFormatting.GREEN + "version" + EnumChatFormatting.WHITE + "     " + "Prints current sponge version"));
                }
            } else { // invalid command
                sender.addChatMessage(new ChatComponentText("'" + EnumChatFormatting.RED + command + EnumChatFormatting.WHITE + "'"
                        + " is not recognized as a valid command.\nAvailable commands are: \n" + EnumChatFormatting.GREEN
                        + COMMANDS.toString()));
                return;
            }
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
        }
    }

    private void processHeap(ICommandSender sender, String[] args) {
        File file = new File(new File(new File("."), "dumps"),
                "heap-dump-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.bin");
        sender.addChatMessage(new ChatComponentText("Writing JVM heap data to: " + file));
        SpongeHooks.dumpHeap(file, true);
        sender.addChatMessage(new ChatComponentText("Heap dump complete"));
    }

    private void processChunks(SpongeConfig.Type type, WorldServer world, DimensionType dimensionType, ICommandSender sender, String[] args) {

        if (type == SpongeConfig.Type.GLOBAL) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Dimension stats: "));
            for (net.minecraft.world.WorldServer worldserver : DimensionManager.getWorlds()) {
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GOLD + "Dimension: " + EnumChatFormatting.AQUA + worldserver.provider.getDimensionId() + "\n"
                                + EnumChatFormatting.GOLD + " Loaded Chunks: " + EnumChatFormatting.GRAY + worldserver.theChunkProviderServer
                                        .getLoadedChunkCount() + "\n"
                                + EnumChatFormatting.GOLD + " Active Chunks: " + EnumChatFormatting.GRAY + worldserver.activeChunkSet.size() + "\n"
                                + EnumChatFormatting.GOLD + " Entities: " + EnumChatFormatting.GRAY + worldserver.loadedEntityList.size() + "\n"
                                + EnumChatFormatting.GOLD + " Tile Entities: " + EnumChatFormatting.GRAY + worldserver.loadedTileEntityList.size()
                        ));
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GOLD + " Removed Entities: " + EnumChatFormatting.GRAY + worldserver.unloadedEntityList.size() + "\n"
                                + EnumChatFormatting.GOLD + " Removed Tile Entities: " + EnumChatFormatting.GRAY + worldserver.tileEntitiesToBeRemoved
                                        .size()
                        ));
            }
        } else if (type == SpongeConfig.Type.WORLD) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "World stats: "));
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GOLD + "World: " + EnumChatFormatting.AQUA + (world.provider.getSaveFolder() == null ? "DIM0" :
                            world.provider.getSaveFolder()) + "\n"
                            + EnumChatFormatting.GOLD + " Loaded Chunks: " + EnumChatFormatting.GRAY + world.theChunkProviderServer
                                    .getLoadedChunkCount() + "\n"
                            + EnumChatFormatting.GOLD + " Active Chunks: " + EnumChatFormatting.GRAY + world.activeChunkSet.size() + "\n"
                            + EnumChatFormatting.GOLD + " Entities: " + EnumChatFormatting.GRAY + world.loadedEntityList.size() + "\n"
                            + EnumChatFormatting.GOLD + " Tile Entities: " + EnumChatFormatting.GRAY + world.loadedTileEntityList.size()
                    ));
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GOLD + " Removed Entities: " + EnumChatFormatting.GRAY + world.unloadedEntityList.size() + "\n"
                            + EnumChatFormatting.GOLD + " Removed Tile Entities: " + EnumChatFormatting.GRAY + world.tileEntitiesToBeRemoved.size()
                    ));
        } else if (type == SpongeConfig.Type.DIMENSION) {
            for (net.minecraft.world.WorldServer worldserver : DimensionManager.getWorlds()) {
                if (worldserver.provider.getClass() == dimensionType.getDimensionClass()) {
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.GOLD + "Dimension: " + EnumChatFormatting.AQUA + worldserver.provider.getDimensionId() + "\n"
                                    + EnumChatFormatting.GOLD + " Loaded Chunks: " + EnumChatFormatting.GRAY + worldserver.theChunkProviderServer
                                            .getLoadedChunkCount() + "\n"
                                    + EnumChatFormatting.GOLD + " Active Chunks: " + EnumChatFormatting.GRAY + worldserver.activeChunkSet.size()
                                    + "\n"
                                    + EnumChatFormatting.GOLD + " Entities: " + EnumChatFormatting.GRAY + worldserver.loadedEntityList.size() + "\n"
                                    + EnumChatFormatting.GOLD + " Tile Entities: " + EnumChatFormatting.GRAY + worldserver.loadedTileEntityList.size()
                            ));
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.GOLD + " Removed Entities: " + EnumChatFormatting.GRAY + worldserver.unloadedEntityList.size() + "\n"
                                    + EnumChatFormatting.GOLD + " Removed Tile Entities: " + EnumChatFormatting.GRAY
                                    + worldserver.tileEntitiesToBeRemoved.size()
                            ));
                }
            }
        }

        // TODO
        if ((args.length < 2) || !args[1].equalsIgnoreCase("dump")) {
            return;
        }
        boolean dumpAll = ((args.length > 2) && "all".equalsIgnoreCase(args[2]));

        File file = new File(new File(new File("."), "chunk-dumps"),
                "chunk-info-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
        sender.addChatMessage(new ChatComponentText("Writing chunk info to: " + file));
        SpongeHooks.writeChunks(file, dumpAll);
        sender.addChatMessage(new ChatComponentText("Chunk info complete"));
    }

    private boolean getToggle(SpongeConfig<?> config, ICommandSender sender, String key) {
        try {
            if (config.getSetting(key).isVirtual()) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not find option: " + key));
                return false;
            }

            CommentedConfigurationNode setting = config.getSetting(key);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + key + " " + EnumChatFormatting.GREEN + setting.getValue()));
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
            SpongeMod.instance.getLogger().error(ExceptionUtils.getStackTrace(e));
        }

        return true;
    }

    private boolean setToggle(SpongeConfig<?> config, ICommandSender sender, String key, String value) {
        try {
            if (config.getSetting(key).isVirtual()) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not find option: " + key));
                return false;
            }

            CommentedConfigurationNode setting = config.getSetting(key).setValue(value);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + key + " " + EnumChatFormatting.GREEN + setting.getValue()));

            config.save();
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
            SpongeMod.instance.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return true;
    }


}
