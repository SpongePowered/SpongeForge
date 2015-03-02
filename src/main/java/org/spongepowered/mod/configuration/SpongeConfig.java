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
package org.spongepowered.mod.configuration;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;

public class SpongeConfig {

    public enum Type {
        GLOBAL,
        DIMENSION,
        WORLD
    }

    public static final String CONFIG_ENABLED = "config-enabled";

    // DEBUG
    public static final String DEBUG_THREAD_CONTENTION_MONITORING = "thread-contention-monitoring";
    public static final String DEBUG_DUMP_CHUNKS_ON_DEADLOCK = "dump-chunks-on-deadlock";
    public static final String DEBUG_DUMP_HEAP_ON_DEADLOCK = "dump-heap-on-deadlock";
    public static final String DEBUG_DUMP_THREADS_ON_WARN = "dump-threads-on-warn";

    // ENTITY
    public static final String ENTITY_MAX_BOUNDING_BOX_SIZE = "max-bounding-box-size";
    public static final String ENTITY_MAX_SPEED = "max-speed";
    public static final String ENTITY_COLLISION_WARN_SIZE = "collision-warn-size";
    public static final String ENTITY_COUNT_WARN_SIZE = "count-warn-size";
    public static final String ENTITY_ITEM_DESPAWN_RATE = "item-despawn-rate";
    public static final String ENTITY_ACTIVATION_RANGE_CREATURE = "creature-activation-range";
    public static final String ENTITY_ACTIVATION_RANGE_MONSTER = "monster-activation-range";
    public static final String ENTITY_ACTIVATION_RANGE_AQUATIC = "aquatic-activation-range";
    public static final String ENTITY_ACTIVATION_RANGE_AMBIENT = "ambient-activation-range";
    public static final String ENTITY_ACTIVATION_RANGE_MISC = "misc-activation-range";

    // GENERAL
    public static final String GENERAL_DISABLE_WARNINGS = "disable-warnings";
    public static final String GENERAL_CHUNK_LOAD_OVERRIDE = "chunk-load-override";

    // LOGGING
    public static final String LOGGING_CHUNK_LOAD = "chunk-load";
    public static final String LOGGING_CHUNK_UNLOAD = "chunk-unload";
    public static final String LOGGING_ENTITY_DEATH = "entity-death";
    public static final String LOGGING_ENTITY_DESPAWN = "entity-despawn";
    public static final String LOGGING_ENTITY_COLLISION_CHECKS = "entity-collision-checks";
    public static final String LOGGING_ENTITY_SPAWN = "entity-spawn";
    public static final String LOGGING_ENTITY_SPEED_REMOVAL = "entity-speed-removal";
    public static final String LOGGING_STACKTRACES = "log-stacktraces";

    // MODULES
    public static final String MODULE_ENTITY_ACTIVATION_RANGE = "entity-activation-range";

    // WORLD
    public static final String WORLD_INFINITE_WATER_SOURCE = "infinite-water-source";
    public static final String WORLD_FLOWING_LAVA_DECAY = "flowing-lava-decay";

    private final String HEADER = "1.0\n"
            + "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us at the IRC or drop by our forums and leave a post.\n"
            + "\n"
            + "# IRC: #sponge @ irc.esper.net ( http://webchat.esper.net/?channel=sponge )\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    private Type type;
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root();
    private ObjectMapper<ConfigBase>.BoundInstance configMapper;
    private ConfigBase configBase;
    private String modId;
    private String configName;
    @SuppressWarnings("unused")
    private File file;

    public SpongeConfig(Type type, File file, String modId) {

        this.type = type;
        this.file = file;
        this.modId = modId;

        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();

                this.loader = HoconConfigurationLoader.builder().setFile(file).build();
                if (type == Type.GLOBAL) {
                    this.configBase = new GlobalConfig();
                    this.configName = "GLOBAL";
                } else if (type == Type.DIMENSION) {
                    this.configBase = new DimensionConfig();
                    this.configName = file.getParentFile().getName().toUpperCase();
                } else {
                    this.configBase = new WorldConfig();
                    this.configName = file.getParentFile().getName().toUpperCase();
                }
                this.configMapper = ObjectMapper.forObject(this.configBase);
                this.configMapper.serialize(this.root.getNode(this.modId));
                this.loader.save(this.root);
            } else {
                this.loader = HoconConfigurationLoader.builder().setFile(file).build();
                this.root = this.loader.load();
            }
        } catch (Throwable t) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(t));
        }
    }

    public void save() {
        try {
            this.loader.save(this.root);
        } catch (IOException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void reload() {
        try {
            this.root = this.loader.load();
        } catch (IOException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public CommentedConfigurationNode getRootNode() {
        return this.root.getNode(this.modId);
    }

    public CommentedConfigurationNode getSetting(String key) {
        if (key.equalsIgnoreCase(SpongeConfig.CONFIG_ENABLED)) {
            return getRootNode().getNode(key);
        }
        else if (!key.contains(".") || key.indexOf('.') == key.length() - 1) {
            return null;
        } else {
            String category = key.substring(0, key.indexOf('.'));
            String prop = key.substring(key.indexOf('.') + 1);
            return getRootNode().getNode(category).getNode(prop);
        }
    }

    public String getConfigName() {
        return this.configName;
    }

    public Type getType() {
        return this.type;
    }

    private class GlobalConfig extends ConfigBase {

        @Setting(value = "modules")
        public ModuleCategory mixins = new ModuleCategory();
    }

    private class DimensionConfig extends ConfigBase {

        public DimensionConfig() {
            this.configEnabled = false;
        }
    }

    private class WorldConfig extends ConfigBase {

        public WorldConfig() {
            this.configEnabled = false;
        }
    }

    private class ConfigBase {

        @Setting(
                value = CONFIG_ENABLED,
                comment = "Controls whether or not this config is enabled.\nNote: If enabled, World configs override Dimension and Global, Dimension configs override Global.")
        public boolean configEnabled = true;
        @Setting
        public DebugCategory debug = new DebugCategory();
        @Setting
        public EntityCategory entity = new EntityCategory();
        @Setting(value = MODULE_ENTITY_ACTIVATION_RANGE)
        public EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();
        @Setting
        public GeneralCategory general = new GeneralCategory();
        @Setting
        public LoggingCategory logging = new LoggingCategory();
        @Setting
        public WorldCategory world = new WorldCategory();

        @ConfigSerializable
        private class DebugCategory extends Category {

            @Setting(value = DEBUG_THREAD_CONTENTION_MONITORING, comment = "Enable Java's thread contention monitoring for thread dumps")
            public boolean enableThreadContentionMonitoring = false;
            @Setting(value = DEBUG_DUMP_CHUNKS_ON_DEADLOCK, comment = "Dump chunks in the event of a deadlock")
            public boolean dumpChunksOnDeadlock = false;
            @Setting(value = DEBUG_DUMP_HEAP_ON_DEADLOCK, comment = "Dump the heap in the event of a deadlock")
            public boolean dumpHeapOnDeadlock = false;
            @Setting(value = DEBUG_DUMP_THREADS_ON_WARN, comment = "Dump the server thread on deadlock warning")
            public boolean dumpThreadsOnWarn = false;
        }

        @ConfigSerializable
        private class GeneralCategory extends Category {

            @Setting(value = GENERAL_DISABLE_WARNINGS, comment = "Disable warning messages to server admins")
            public boolean disableWarnings = false;
            @Setting(value = GENERAL_CHUNK_LOAD_OVERRIDE,
                    comment = "Forces Chunk Loading on provide requests (speedup for mods that don't check if a chunk is loaded)")
            public boolean chunkLoadOverride = false;
        }

        @ConfigSerializable
        private class EntityCategory extends Category {

            @Setting(value = ENTITY_MAX_BOUNDING_BOX_SIZE, comment = "Max size of an entity's bounding box before removing it. Set to 0 to disable")
            public int maxBoundingBoxSize = 1000;
            @Setting(value = SpongeConfig.ENTITY_MAX_SPEED, comment = "Square of the max speed of an entity before removing it. Set to 0 to disable")
            public int maxSpeed = 100;
            @Setting(value = ENTITY_COLLISION_WARN_SIZE,
                    comment = "Number of colliding entities in one spot before logging a warning. Set to 0 to disable")
            public int maxCollisionSize = 200;
            @Setting(value = ENTITY_COUNT_WARN_SIZE,
                    comment = "Number of entities in one dimension before logging a warning. Set to 0 to disable")
            public int maxCountWarnSize = 0;
            @Setting(value = ENTITY_ITEM_DESPAWN_RATE, comment = "Controls the time in ticks for when an item despawns.")
            public int itemDespawnRate = 6000;
        }

        @ConfigSerializable
        private class EntityActivationRangeCategory extends Category {

            @Setting(value = ENTITY_ACTIVATION_RANGE_CREATURE)
            public int creatureActivationRange = 32;
            @Setting(value = ENTITY_ACTIVATION_RANGE_MONSTER)
            public int monsterActivationRange = 32;
            @Setting(value = ENTITY_ACTIVATION_RANGE_AQUATIC)
            public int aquaticActivationRange = 32;
            @Setting(value = ENTITY_ACTIVATION_RANGE_AMBIENT)
            public int ambientActivationRange = 32;
            @Setting(value = ENTITY_ACTIVATION_RANGE_MISC)
            public int miscActivationRange = 16;
        }

        @ConfigSerializable
        private class LoggingCategory extends Category {

            @Setting(value = LOGGING_CHUNK_LOAD, comment = "Log when chunks are loaded")
            public boolean chunkLoadLogging = false;
            @Setting(value = LOGGING_CHUNK_UNLOAD, comment = "Log when chunks are unloaded")
            public boolean chunkUnloadLogging = false;
            @Setting(value = LOGGING_ENTITY_SPAWN, comment = "Log when living entities are spawned")
            public boolean entitySpawnLogging = false;
            @Setting(value = LOGGING_ENTITY_DESPAWN, comment = "Log when living entities are despawned")
            public boolean entityDespawnLogging = false;
            @Setting(value = LOGGING_ENTITY_DEATH, comment = "Log when living entities are destroyed")
            public boolean entityDeathLogging = false;
            @Setting(value = LOGGING_STACKTRACES, comment = "Add stack traces to dev logging")
            public boolean logWithStackTraces = false;
            @Setting(value = LOGGING_ENTITY_COLLISION_CHECKS, comment = "Whether to log entity collision/count checks")
            public boolean logEntityCollisionChecks = false;
            @Setting(value = LOGGING_ENTITY_SPEED_REMOVAL, comment = "Whether to log entity removals due to speed")
            public boolean logEntitySpeedRemoval = false;
        }

        @ConfigSerializable
        protected class ModuleCategory extends Category {

            @Setting(value = MODULE_ENTITY_ACTIVATION_RANGE)
            public boolean pluginEntityActivation = true;
        }

        @ConfigSerializable
        private class WorldCategory extends Category {

            @Setting(value = WORLD_INFINITE_WATER_SOURCE, comment = "Vanilla water source behavior - is infinite")
            public boolean infiniteWaterSource = false;
            @Setting(value = WORLD_FLOWING_LAVA_DECAY, comment = "Lava behaves like vanilla water when source block is removed")
            public boolean flowingLavaDecay = false;
        }

        @ConfigSerializable
        private class Category {
        }
    }
}
