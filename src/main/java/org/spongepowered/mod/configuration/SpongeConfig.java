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
package org.spongepowered.mod.configuration;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SpongeConfig<T extends SpongeConfig.ConfigBase> {

    public enum Type {
        GLOBAL(GlobalConfig.class),
        DIMENSION(DimensionConfig.class),
        WORLD(WorldConfig.class);

        private final Class<? extends ConfigBase> type;

        Type(Class<? extends ConfigBase> type) {
            this.type = type;
        }
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

    private static final String HEADER = "1.0\n"
            + "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us at the IRC or drop by our forums and leave a post.\n"
            + "\n"
            + "# IRC: #sponge @ irc.esper.net ( http://webchat.esper.net/?channel=sponge )\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    private Type type;
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root(ConfigurationOptions.defaults()
            .setHeader(HEADER));
    private ObjectMapper<T>.BoundInstance configMapper;
    private T configBase;
    private String modId;
    private String configName;
    @SuppressWarnings("unused")
    private File file;

    @SuppressWarnings("unchecked")
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
            }

            this.loader = HoconConfigurationLoader.builder().setFile(file).build();
            if (type == Type.GLOBAL) {
                this.configName = "GLOBAL";
            } else {
                this.configName = file.getParentFile().getName().toUpperCase();
            }

            this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(this.type.type).bindToNew();

            reload();
            this.loader.save(this.root);
        } catch (Throwable t) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(t));
        }
    }

    public T getConfig() {
        return this.configBase;
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode(this.modId));
            this.loader.save(this.root);
        } catch (IOException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        } catch (ObjectMappingException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void reload() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults()
                    .setHeader(HEADER));
            this.configBase = this.configMapper.populate(this.root.getNode(this.modId));
        } catch (IOException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        } catch (ObjectMappingException e) {
            LogManager.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public CommentedConfigurationNode getRootNode() {
        return this.root.getNode(this.modId);
    }

    public CommentedConfigurationNode getSetting(String key) {
        if (key.equalsIgnoreCase(SpongeConfig.CONFIG_ENABLED)) {
            return getRootNode().getNode(key);
        } else if (!key.contains(".") || key.indexOf('.') == key.length() - 1) {
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

    public static class GlobalConfig extends ConfigBase {

        @Setting
        private SqlCategory sql = new SqlCategory();

        @Setting
        private CommandsCategory commands = new CommandsCategory();

        @Setting(value = "modules")
        private ModuleCategory mixins = new ModuleCategory();

        public SqlCategory getSql() {
            return this.sql;
        }

        public CommandsCategory getCommands() {
            return this.commands;
        }

        public ModuleCategory getModules() {
            return this.mixins;
        }
    }

    public static class DimensionConfig extends ConfigBase {

        public DimensionConfig() {
            this.configEnabled = false;
        }
    }

    public static class WorldConfig extends ConfigBase {

        public WorldConfig() {
            this.configEnabled = false;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class ConfigBase {

        @Setting(
                value = CONFIG_ENABLED,
                comment = "Controls whether or not this config is enabled.\n"
                        + "Note: If enabled, World configs override Dimension and Global, Dimension configs override Global.")
        protected boolean configEnabled = true;
        @Setting
        private DebugCategory debug = new DebugCategory();
        @Setting
        private EntityCategory entity = new EntityCategory();
        @Setting(value = MODULE_ENTITY_ACTIVATION_RANGE)
        private EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();
        @Setting
        private GeneralCategory general = new GeneralCategory();
        @Setting
        private LoggingCategory logging = new LoggingCategory();
        @Setting
        private WorldCategory world = new WorldCategory();

        public boolean isConfigEnabled() {
            return this.configEnabled;
        }

        public void setConfigEnabled(boolean configEnabled) {
            this.configEnabled = configEnabled;
        }

        public DebugCategory getDebug() {
            return this.debug;
        }

        public EntityCategory getEntity() {
            return this.entity;
        }

        public EntityActivationRangeCategory getEntityActivationRange() {
            return this.entityActivationRange;
        }

        public GeneralCategory getGeneral() {
            return this.general;
        }

        public LoggingCategory getLogging() {
            return this.logging;
        }

        public WorldCategory getWorld() {
            return this.world;
        }
    }

    @ConfigSerializable
    public static class SqlCategory extends Category {
        @Setting
        private Map<String, String> aliases = new HashMap<String, String>();

        public Map<String, String> getAliases() {
            return this.aliases;
        }
    }

    @ConfigSerializable
    public static class CommandsCategory extends Category {
        @Setting(comment = "A mapping from unqualified command alias to plugin id of the plugin that should handle a certain command")
        private Map<String, String> aliases = new HashMap<String, String>();

        public Map<String, String> getAliases() {
            return this.aliases;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @ConfigSerializable
    public static class DebugCategory extends Category {

        @Setting(value = DEBUG_THREAD_CONTENTION_MONITORING, comment = "Enable Java's thread contention monitoring for thread dumps")
        private boolean enableThreadContentionMonitoring = false;
        @Setting(value = DEBUG_DUMP_CHUNKS_ON_DEADLOCK, comment = "Dump chunks in the event of a deadlock")
        private boolean dumpChunksOnDeadlock = false;
        @Setting(value = DEBUG_DUMP_HEAP_ON_DEADLOCK, comment = "Dump the heap in the event of a deadlock")
        private boolean dumpHeapOnDeadlock = false;
        @Setting(value = DEBUG_DUMP_THREADS_ON_WARN, comment = "Dump the server thread on deadlock warning")
        private boolean dumpThreadsOnWarn = false;

        public boolean isEnableThreadContentionMonitoring() {
            return this.enableThreadContentionMonitoring;
        }

        public void setEnableThreadContentionMonitoring(boolean enableThreadContentionMonitoring) {
            this.enableThreadContentionMonitoring = enableThreadContentionMonitoring;
        }

        public boolean dumpChunksOnDeadlock() {
            return this.dumpChunksOnDeadlock;
        }

        public void setDumpChunksOnDeadlock(boolean dumpChunksOnDeadlock) {
            this.dumpChunksOnDeadlock = dumpChunksOnDeadlock;
        }

        public boolean dumpHeapOnDeadlock() {
            return this.dumpHeapOnDeadlock;
        }

        public void setDumpHeapOnDeadlock(boolean dumpHeapOnDeadlock) {
            this.dumpHeapOnDeadlock = dumpHeapOnDeadlock;
        }

        public boolean dumpThreadsOnWarn() {
            return this.dumpThreadsOnWarn;
        }

        public void setDumpThreadsOnWarn(boolean dumpThreadsOnWarn) {
            this.dumpThreadsOnWarn = dumpThreadsOnWarn;
        }
    }

    @ConfigSerializable
    public static class GeneralCategory extends Category {

        @Setting(value = GENERAL_DISABLE_WARNINGS, comment = "Disable warning messages to server admins")
        private boolean disableWarnings = false;
        @Setting(value = GENERAL_CHUNK_LOAD_OVERRIDE,
                comment = "Forces Chunk Loading on provide requests (speedup for mods that don't check if a chunk is loaded)")
        private boolean chunkLoadOverride = false;

        public boolean disableWarnings() {
            return this.disableWarnings;
        }

        public void setDisableWarnings(boolean disableWarnings) {
            this.disableWarnings = disableWarnings;
        }

        public boolean chunkLoadOverride() {
            return this.chunkLoadOverride;
        }

        public void setChunkLoadOverride(boolean chunkLoadOverride) {
            this.chunkLoadOverride = chunkLoadOverride;
        }
    }

    @ConfigSerializable
    public static class EntityCategory extends Category {

        @Setting(value = ENTITY_MAX_BOUNDING_BOX_SIZE, comment = "Max size of an entity's bounding box before removing it. Set to 0 to disable")
        private int maxBoundingBoxSize = 1000;
        @Setting(value = SpongeConfig.ENTITY_MAX_SPEED, comment = "Square of the max speed of an entity before removing it. Set to 0 to disable")
        private int maxSpeed = 100;
        @Setting(value = ENTITY_COLLISION_WARN_SIZE,
                comment = "Number of colliding entities in one spot before logging a warning. Set to 0 to disable")
        private int maxCollisionSize = 200;
        @Setting(value = ENTITY_COUNT_WARN_SIZE,
                comment = "Number of entities in one dimension before logging a warning. Set to 0 to disable")
        private int maxCountWarnSize = 0;
        @Setting(value = ENTITY_ITEM_DESPAWN_RATE, comment = "Controls the time in ticks for when an item despawns.")
        private int itemDespawnRate = 6000;

        public int getMaxBoundingBoxSize() {
            return this.maxBoundingBoxSize;
        }

        public void setMaxBoundingBoxSize(int maxBoundingBoxSize) {
            this.maxBoundingBoxSize = maxBoundingBoxSize;
        }

        public int getMaxSpeed() {
            return this.maxSpeed;
        }

        public void setMaxSpeed(int maxSpeed) {
            this.maxSpeed = maxSpeed;
        }

        public int getMaxCollisionSize() {
            return this.maxCollisionSize;
        }

        public void setMaxCollisionSize(int maxCollisionSize) {
            this.maxCollisionSize = maxCollisionSize;
        }

        public int getMaxCountWarnSize() {
            return this.maxCountWarnSize;
        }

        public void setMaxCountWarnSize(int maxCountWarnSize) {
            this.maxCountWarnSize = maxCountWarnSize;
        }

        public int getItemDespawnRate() {
            return this.itemDespawnRate;
        }

        public void setItemDespawnRate(int itemDespawnRate) {
            this.itemDespawnRate = itemDespawnRate;
        }
    }

    @ConfigSerializable
    public static class EntityActivationRangeCategory extends Category {

        @Setting(value = ENTITY_ACTIVATION_RANGE_CREATURE)
        private int creatureActivationRange = 32;
        @Setting(value = ENTITY_ACTIVATION_RANGE_MONSTER)
        private int monsterActivationRange = 32;
        @Setting(value = ENTITY_ACTIVATION_RANGE_AQUATIC)
        private int aquaticActivationRange = 32;
        @Setting(value = ENTITY_ACTIVATION_RANGE_AMBIENT)
        private int ambientActivationRange = 32;
        @Setting(value = ENTITY_ACTIVATION_RANGE_MISC)
        private int miscActivationRange = 16;

        public int getCreatureActivationRange() {
            return this.creatureActivationRange;
        }

        public void setCreatureActivationRange(int creatureActivationRange) {
            this.creatureActivationRange = creatureActivationRange;
        }

        public int getMonsterActivationRange() {
            return this.monsterActivationRange;
        }

        public void setMonsterActivationRange(int monsterActivationRange) {
            this.monsterActivationRange = monsterActivationRange;
        }

        public int getAquaticActivationRange() {
            return this.aquaticActivationRange;
        }

        public void setAquaticActivationRange(int aquaticActivationRange) {
            this.aquaticActivationRange = aquaticActivationRange;
        }

        public int getAmbientActivationRange() {
            return this.ambientActivationRange;
        }

        public void setAmbientActivationRange(int ambientActivationRange) {
            this.ambientActivationRange = ambientActivationRange;
        }

        public int getMiscActivationRange() {
            return this.miscActivationRange;
        }

        public void setMiscActivationRange(int miscActivationRange) {
            this.miscActivationRange = miscActivationRange;
        }
    }

    @ConfigSerializable
    public static class LoggingCategory extends Category {

        @Setting(value = LOGGING_CHUNK_LOAD, comment = "Log when chunks are loaded")
        private boolean chunkLoadLogging = false;
        @Setting(value = LOGGING_CHUNK_UNLOAD, comment = "Log when chunks are unloaded")
        private boolean chunkUnloadLogging = false;
        @Setting(value = LOGGING_ENTITY_SPAWN, comment = "Log when living entities are spawned")
        private boolean entitySpawnLogging = false;
        @Setting(value = LOGGING_ENTITY_DESPAWN, comment = "Log when living entities are despawned")
        private boolean entityDespawnLogging = false;
        @Setting(value = LOGGING_ENTITY_DEATH, comment = "Log when living entities are destroyed")
        private boolean entityDeathLogging = false;
        @Setting(value = LOGGING_STACKTRACES, comment = "Add stack traces to dev logging")
        private boolean logWithStackTraces = false;
        @Setting(value = LOGGING_ENTITY_COLLISION_CHECKS, comment = "Whether to log entity collision/count checks")
        private boolean logEntityCollisionChecks = false;
        @Setting(value = LOGGING_ENTITY_SPEED_REMOVAL, comment = "Whether to log entity removals due to speed")
        private boolean logEntitySpeedRemoval = false;

        public boolean chunkLoadLogging() {
            return this.chunkLoadLogging;
        }

        public void setChunkLoadLogging(boolean chunkLoadLogging) {
            this.chunkLoadLogging = chunkLoadLogging;
        }

        public boolean chunkUnloadLogging() {
            return this.chunkUnloadLogging;
        }

        public void setChunkUnloadLogging(boolean chunkUnloadLogging) {
            this.chunkUnloadLogging = chunkUnloadLogging;
        }

        public boolean entitySpawnLogging() {
            return this.entitySpawnLogging;
        }

        public void setEntitySpawnLogging(boolean entitySpawnLogging) {
            this.entitySpawnLogging = entitySpawnLogging;
        }

        public boolean entityDespawnLogging() {
            return this.entityDespawnLogging;
        }

        public void setEntityDespawnLogging(boolean entityDespawnLogging) {
            this.entityDespawnLogging = entityDespawnLogging;
        }

        public boolean entityDeathLogging() {
            return this.entityDeathLogging;
        }

        public void setEntityDeathLogging(boolean entityDeathLogging) {
            this.entityDeathLogging = entityDeathLogging;
        }

        public boolean logWithStackTraces() {
            return this.logWithStackTraces;
        }

        public void setLogWithStackTraces(boolean logWithStackTraces) {
            this.logWithStackTraces = logWithStackTraces;
        }

        public boolean logEntityCollisionChecks() {
            return this.logEntityCollisionChecks;
        }

        public void setLogEntityCollisionChecks(boolean logEntityCollisionChecks) {
            this.logEntityCollisionChecks = logEntityCollisionChecks;
        }

        public boolean logEntitySpeedRemoval() {
            return this.logEntitySpeedRemoval;
        }

        public void setLogEntitySpeedRemoval(boolean logEntitySpeedRemoval) {
            this.logEntitySpeedRemoval = logEntitySpeedRemoval;
        }
    }

    @ConfigSerializable
    public static class ModuleCategory extends Category {

        @Setting(value = MODULE_ENTITY_ACTIVATION_RANGE)
        private boolean pluginEntityActivation = true;

        public boolean usePluginEntityActivation() {
            return this.pluginEntityActivation;
        }

        public void setPluginEntityActivation(boolean pluginEntityActivation) {
            this.pluginEntityActivation = pluginEntityActivation;
        }
    }

    @ConfigSerializable
    public static class WorldCategory extends Category {

        @Setting(value = WORLD_INFINITE_WATER_SOURCE, comment = "Vanilla water source behavior - is infinite")
        private boolean infiniteWaterSource = false;
        @Setting(value = WORLD_FLOWING_LAVA_DECAY, comment = "Lava behaves like vanilla water when source block is removed")
        private boolean flowingLavaDecay = false;

        public boolean hasInfiniteWaterSource() {
            return this.infiniteWaterSource;
        }

        public void setInfiniteWaterSource(boolean infiniteWaterSource) {
            this.infiniteWaterSource = infiniteWaterSource;
        }

        public boolean hasFlowingLavaDecay() {
            return this.flowingLavaDecay;
        }

        public void setFlowingLavaDecay(boolean flowingLavaDecay) {
            this.flowingLavaDecay = flowingLavaDecay;
        }
    }

    @ConfigSerializable
    private static class Category {
    }
}
