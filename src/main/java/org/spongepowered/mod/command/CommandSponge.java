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

import static org.spongepowered.api.util.command.args.GenericArguments.dimension;
import static org.spongepowered.api.util.command.args.GenericArguments.world;
import static org.spongepowered.api.util.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.util.command.args.GenericArguments.flags;
import static org.spongepowered.api.util.command.args.GenericArguments.literal;
import static org.spongepowered.api.util.command.args.GenericArguments.optional;
import static org.spongepowered.api.util.command.args.GenericArguments.seq;
import static org.spongepowered.api.util.command.args.GenericArguments.string;

import com.google.common.base.Optional;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;
import org.spongepowered.mod.util.SpongeHooks;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@NonnullByDefault
public class CommandSponge {
    private static final String INDENT = "    ";
    private static final String LONG_INDENT = INDENT + INDENT;

    private static final Text NEWLINE_TEXT = Texts.of("\n");

    /**
     * Create a new instance of the Sponge command structure.
     *
     * @param mod The mod to deal with
     * @return The newly created command
     */
    public static CommandSpec getCommand(SpongeMod mod) {
        final ChildCommandElementExecutor flagChildren = new ChildCommandElementExecutor(null);
        final ChildCommandElementExecutor nonFlagChildren = new ChildCommandElementExecutor(flagChildren);
        nonFlagChildren.register(getVersionCommand(mod), "version");
        nonFlagChildren.register(getAuditCommand(), "audit");
        nonFlagChildren.register(getHeapCommand(), "heap");
        flagChildren.register(getChunksCommand(mod), "chunks");
        flagChildren.register(getConfigCommand(), "config");
        flagChildren.register(getReloadCommand(), "reload"); // TODO: Should these two be subcommands of config, and what is now config be set?
        flagChildren.register(getSaveCommand(), "save");
        return CommandSpec.builder()
                .setDescription(Texts.of("Text description"))
                .setExtendedDescription(Texts.of("commands:\n", // TODO: Automatically generate from child executors (wait for help system on this)
                        INDENT, Texts.of(TextColors.GREEN, "chunks"), LONG_INDENT, "Prints chunk data for a specific dimension or world(s)\n",
                        INDENT, Texts.of(TextColors.GREEN, "conf"), LONG_INDENT, "Configure sponge settings\n",
                        INDENT, Texts.of(TextColors.GREEN, "heap"), LONG_INDENT, "Dump live JVM heap\n",
                        INDENT, Texts.of(TextColors.GREEN, "reload", LONG_INDENT, "Reloads a global, dimension, or world config\n"),
                        INDENT, Texts.of(TextColors.GREEN, "save"), LONG_INDENT, "Saves a global, dimension, or world config\n",
                        INDENT, Texts.of(TextColors.GREEN, "version"), LONG_INDENT, "Prints current Sponge version\n",
                        INDENT, Texts.of(TextColors.GREEN, "audit"), LONG_INDENT, "Audit mixin classes for implementation"))
                .setArguments(firstParsing(nonFlagChildren, flags()
                        .flag("-global", "g")
                        .valueFlag(world(Texts.of("world"), mod.getGame()), "-world", "w")
                        .valueFlag(dimension(Texts.of("dimension"), mod.getGame()), "-dimension", "d")
                        .buildWith(flagChildren)))
                .setExecutor(nonFlagChildren)
                .build();
    }

    // TODO: Have some sort of separator between outputs for each world/dimension/global/whatever (that are exactly one line?)
    private abstract static class ConfigUsingExecutor implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            int successes = 0;
            if (args.hasAny("global")) {
                src.sendMessage(Texts.of("Global: ", processGlobal(CoreMixinPlugin.getGlobalConfig(), src, args)));
                ++successes;
            }
            if (args.hasAny("dimension")) {
                for (DimensionType dimension : args.<DimensionType>getAll("dimension")) {
                    WorldProvider provider = DimensionManager.getProvider(((SpongeDimensionType) dimension).getDimensionTypeId());
                    src.sendMessage(Texts.of("Dimension ", dimension.getName(), ": ", processDimension(((IMixinWorldProvider) provider)
                                    .getDimensionConfig(), dimension, src, args)));
                    ++successes;
                }
            }
            if (args.hasAny("world")) {
                for (WorldProperties properties : args.<WorldProperties>getAll("world")) {
                    Optional<World> world = SpongeMod.instance.getGame().getServer().getWorld(properties.getUniqueId());
                    if (!world.isPresent()) {
                        throw new CommandException(Texts.of("World ", properties.getWorldName(), " is not loaded, cannot work with it"));
                    }
                    src.sendMessage(Texts.of("World ", properties.getWorldName(), ": ", processWorld(((IMixinWorld) world.get()).getWorldConfig(),
                            world.get(), src, args)));
                    ++successes;
                }
            }
            if (successes == 0) {
                throw new CommandException(Texts.of("At least one target flag must be specified"));
            }
            return CommandResult.builder().successCount(successes).build(); // TODO: How do we handle results?
        }

        protected Text processGlobal(SpongeConfig<SpongeConfig.GlobalConfig> config, CommandSource source, CommandContext args)
                throws CommandException {
            return process(config, source, args);
        }

        protected Text processDimension(SpongeConfig<SpongeConfig.DimensionConfig> config, DimensionType dim, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text processWorld(SpongeConfig<SpongeConfig.WorldConfig> config, World world, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
            return Texts.of("Unimplemented");
        }
    }

    // Flag children

    private static CommandSpec getChunksCommand(final SpongeMod mod) {
        return CommandSpec.builder()
                .setDescription(Texts.of("Print chunk information, optionally dump"))
                .setArguments(optional(seq(literal(Texts.of("dump"), "dump"), optional(literal(Texts.of("dump-all"), "all")))))
                .setPermission("sponge.command.chunks")
                .setExecutor(new ConfigUsingExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        CommandResult res = super.execute(src, args);
                        if (args.hasAny("dump")) {
                            File file = new File(new File(new File("."), "chunk-dumps"),
                                    "chunk-info-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
                            src.sendMessage(Texts.of("Writing chunk info to: ", file));
                            SpongeHooks.writeChunks(file, args.hasAny("dump-all"));
                            src.sendMessage(Texts.of("Chunk info complete"));
                        }
                        return res;
                    }

                    @Override
                    protected Text processGlobal(SpongeConfig<SpongeConfig.GlobalConfig> config, CommandSource source, CommandContext args)
                            throws CommandException {
                        for (World world : mod.getGame().getServer().getWorlds()) {
                            source.sendMessage(Texts.of("World ", Texts.of(TextStyles.BOLD, world.getName()),
                                    getChunksInfo(((WorldServer) world))));
                        }
                        return Texts.of("Printed chunk info for all worlds ");
                    }

                    @Override
                    protected Text processDimension(SpongeConfig<SpongeConfig.DimensionConfig> config, DimensionType dim, CommandSource source,
                            CommandContext args)
                            throws CommandException {
                        for (World world : mod.getGame().getServer().getWorlds()) {
                            if (world.getDimension().getType().equals(dim)) {
                                source.sendMessage(Texts.of("World ", Texts.of(TextStyles.BOLD, world.getName()),
                                        getChunksInfo(((WorldServer) world))));
                            }
                        }
                        return Texts.of("Printed chunk info for all worlds in dimension ", dim.getName());
                    }

                    @Override
                    protected Text processWorld(SpongeConfig<SpongeConfig.WorldConfig> config, World world, CommandSource source, CommandContext args)
                            throws CommandException {
                        return getChunksInfo((WorldServer) world);
                    }

                    protected Text key(Object text) {
                        return Texts.of(TextColors.GOLD, text);
                    }

                    protected Text value(Object text) {
                        return Texts.of(TextColors.GRAY, text);
                    }

                    protected Text getChunksInfo(WorldServer worldserver) {
                        return Texts.of(NEWLINE_TEXT, key("Dimension: "), value(worldserver.provider.getDimensionId()), NEWLINE_TEXT,
                                key("Loaded chunks: "), value(worldserver.theChunkProviderServer.getLoadedChunkCount()), NEWLINE_TEXT,
                                key("Active chunks: "), value(worldserver.activeChunkSet.size()), NEWLINE_TEXT,
                                key("Entities: "), value(worldserver.loadedEntityList.size()), NEWLINE_TEXT,
                                key("Tile Entities: "), value(worldserver.loadedTileEntityList.size()), NEWLINE_TEXT,
                                key("Removed Entities:"), value(worldserver.unloadedEntityList.size()), NEWLINE_TEXT,
                                key("Removed Tile Entities: "), value(worldserver.tileEntitiesToBeRemoved), NEWLINE_TEXT
                        );
                    }
                })
                .build();
    }

    private static CommandSpec getConfigCommand() {
        return CommandSpec.builder()
                .setDescription(Texts.of("Inspect the Sponge config"))
                .setArguments(seq(string(Texts.of("key")), optional(string(Texts.of("value")))))
                .setPermission("sponge.command.config")
                .setExecutor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        final Optional<String> key = args.getOne("key");
                        final Optional<String> value = args.getOne("value");
                        if (config.getSetting(key.get()).isVirtual()) {
                            throw new CommandException(Texts.of("Key ", Texts.builder(key.get()).color(TextColors.GREEN).build(), " is not "
                                    + "valid"));
                        }
                        CommentedConfigurationNode setting = config.getSetting(key.get());

                        if (value.isPresent()) { // Set
                            setting.setValue(value.get());
                            return Texts.builder().append(Texts.of(TextColors.GOLD, key), Texts.of(" set to "),
                                    Texts.of(TextColors.GREEN, setting.getValue())).build();
                        } else {
                            return Texts.builder().append(Texts.of(TextColors.GOLD, key), Texts.of(" is "),
                                    Texts.of(TextColors.GREEN, setting.getValue())).build();
                        }
                    }
                })
                .build();
    }

    private static CommandSpec getReloadCommand() {
        return CommandSpec.builder()
                .setDescription(Texts.of("Reload the Sponge configuration"))
                .setPermission("sponge.command.reload")
                .setExecutor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        config.reload();
                        return Texts.of("Reloaded configuration");
                    }
                })
                .build();
    }

    private static CommandSpec getSaveCommand() {
        return CommandSpec.builder()
                .setDescription(Texts.of("Save the configuration"))
                .setPermission("sponge.command.save")
                .setExecutor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        config.save();
                        return Texts.of("Saved");
                    }
                })
                .build();
    }

    // Non-flag children

    private static CommandSpec getHeapCommand() {
        return CommandSpec.builder()
                .setDescription(Texts.of("Generate a dump of the Sponge heap"))
                .setPermission("sponge.command.heap")
                .setExecutor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        File file = new File(new File(new File("."), "dumps"),
                                "heap-dump-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.bin");
                        src.sendMessage(Texts.of("Writing JVM heap data to: ", file));
                        SpongeHooks.dumpHeap(file, true);
                        src.sendMessage(Texts.of("Heap dump complete"));
                        return CommandResult.builder().successCount(1).build();
                    }
                })
                .build();

    }


    private static CommandSpec getVersionCommand(final SpongeMod mod) {
        return CommandSpec.builder()
                .setDescription(Texts.of("Display Sponge's current version"))
                .setPermission("sponge.command.version")
                .setExecutor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        src.sendMessage(Texts.of("SpongeMod: ", Texts.of(TextColors.GREEN, mod.getGame().getImplementationVersion()), "\n",
                                "SpongeAPI: ", Texts.of(TextColors.GREEN, mod.getGame().getApiVersion())));
                        return CommandResult.builder().successCount(1).build();
                    }
                })
                .build();
    }

    private static CommandSpec getAuditCommand() {
        return CommandSpec.builder()
                .setDescription(Texts.of("Audit Mixin classes for implementation"))
                .setPermission("sponge.command.audit")
                .setExecutor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        MixinEnvironment.getCurrentEnvironment().audit();
                        return CommandResult.empty();
                    }
                })
                .build();
    }
}
