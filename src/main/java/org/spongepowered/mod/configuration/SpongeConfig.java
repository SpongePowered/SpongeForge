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

import java.io.File;
import java.util.Map;

import org.spongepowered.mod.SpongeMod;

import com.google.common.collect.Maps;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

public class SpongeConfig {

    public enum Type {
        GLOBAL,
        DIMENSION,
        WORLD
    }

    private final String HEADER = "1.0\n"
            + "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us at the IRC or drop by our forums and leave a post.\n"
            + "\n"
            + "# IRC: #sponge @ irc.esper.net ( http://webchat.esper.net/?channel=sponge )\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    private Configuration config;
    private String configName;
    private Type type;

    public Map<String, ConfigSetting> settings = Maps.newHashMap();

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_ENTITY_SETTINGS = "entity-settings";
    public static final String CATEGORY_CHUNK_SETTINGS = "chunk-settings";
    public static final String CATEGORY_DEBUG = "debug";
    public static final String CATEGORY_FAKE_PLAYERS = "fake-players";
    public static final String CATEGORY_LOGGING = "logging";
    public static final String CATEGORY_WORLD_SETTINGS = "world-settings";

    public ConfigSetting configEnabled;
    public ConfigSetting disableWarnings;
    public ConfigSetting chunkLoadOverride;
    public ConfigSetting chunkLoadLogging;
    public ConfigSetting chunkUnloadLogging;
    public ConfigSetting entitySpawnLogging;
    public ConfigSetting entityDespawnLogging;
    public ConfigSetting entityDeathLogging;
    public ConfigSetting logWithStackTraces;
    public ConfigSetting dumpChunksOnDeadlock;
    public ConfigSetting dumpHeapOnDeadlock;
    public ConfigSetting dumpThreadsOnWarn;
    public ConfigSetting logEntityCollisionChecks;
    public ConfigSetting logEntitySpeedRemoval;
    public ConfigSetting largeCollisionLogSize;
    public ConfigSetting largeEntityCountLogSize;

    public ConfigSetting checkEntityBoundingBoxes;
    public ConfigSetting checkEntityMaxSpeeds;
    public ConfigSetting largeBoundingBoxLogSize;
    public ConfigSetting entityMaxSpeed;

    public ConfigSetting enableThreadContentionMonitoring;

    public ConfigSetting infiniteWaterSource;
    public ConfigSetting flowingLavaDecay;


    public SpongeConfig() {};

    public SpongeConfig(Type type, String path) {
        this.type = type;
        this.config = new Configuration(new File(SpongeMod.instance.getSuggestedConfigFile().getParent(), "sponge" + File.separator + path), this.HEADER);
        this.configName = this.config.getConfigFile().getParentFile().getName().toUpperCase();

        init();
    }

    public void init() {
        // General
        this.configEnabled = new ConfigSetting(CATEGORY_GENERAL, this.config.get(CATEGORY_GENERAL, "config-enabled", this.type == Type.GLOBAL ? true : false, "Controls whether or not this config is enabled. Note: If enabled, World configs override Dimension and Global, Dimension configs override Global."));

        // Logging
        this.disableWarnings = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "disabled-warnings", false, "Disable warning messages to server admins"));
        this.chunkLoadLogging = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "chunk-load", false, "Log when chunks are loaded"));
        this.chunkUnloadLogging = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "chunk-unload", false, "Log when chunks are unloaded"));
        this.entitySpawnLogging = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-spawn", false, "Log when living entities are spawned"));
        this.entityDespawnLogging = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-despawn", false, "Log when living entities are despawned"));
        this.entityDeathLogging = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-death", false, "Log when an entity is destroyed"));
        this.logWithStackTraces = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "detailed-logging", false, "Add stack traces to dev logging"));
        this.logEntityCollisionChecks = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-collision-checks", false, "Whether to log entity collision/count checks"));
        this.logEntitySpeedRemoval = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-speed-removal", false, "Whether to log entity removals due to speed"));
        this.largeCollisionLogSize = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "collision-warn-size", 200, "Number of colliding entities in one spot before logging a warning. Set to 0 to disable", 100, 2000));
        this.largeEntityCountLogSize = new ConfigSetting(CATEGORY_LOGGING, this.config.get(CATEGORY_LOGGING, "entity-count-warn-size", 0, "Number of entities in one dimension logging a warning. Set to 0 to disable", 0, 999999));

        // Chunk Settings
        this.chunkLoadOverride = new ConfigSetting(CATEGORY_CHUNK_SETTINGS, this.config.get(CATEGORY_CHUNK_SETTINGS, "chunk-load-override", false, "Forces Chunk Loading on provide requests (speedup for mods that don't check if a chunk is loaded)"));

        // Entity Settings
        this.checkEntityBoundingBoxes = new ConfigSetting(CATEGORY_ENTITY_SETTINGS, this.config.get(CATEGORY_ENTITY_SETTINGS, "check-entity-bounding-boxes", true, "Removes a living entity that exceeds the max bounding box size."));
        this.checkEntityMaxSpeeds = new ConfigSetting(CATEGORY_ENTITY_SETTINGS, this.config.get(CATEGORY_ENTITY_SETTINGS, "check-entity-max-speeds", false, "Removes any entity that exceeds max speed."));
        this.largeBoundingBoxLogSize = new ConfigSetting(CATEGORY_ENTITY_SETTINGS, this.config.get(CATEGORY_ENTITY_SETTINGS, "entity-bounding-box-max-size", 1000, "Max size of an entity's bounding box before removing it (either being too large or bugged and 'moving' too fast)"));
        this.entityMaxSpeed = new ConfigSetting(CATEGORY_ENTITY_SETTINGS, this.config.get(CATEGORY_ENTITY_SETTINGS, "entity-max-speed", 100, "Square of the max speed of an entity before removing it"));

        // Debug Settings
        this.enableThreadContentionMonitoring = new ConfigSetting(CATEGORY_DEBUG, this.config.get(CATEGORY_DEBUG, "thread-contention-monitoring", false, "Set true to enable Java's thread contention monitoring for thread dumps"));
        this.dumpChunksOnDeadlock = new ConfigSetting(CATEGORY_DEBUG, this.config.get(CATEGORY_DEBUG, "dump-chunks-on-deadlock", false, "Dump chunks in the event of a deadlock"));
        this.dumpHeapOnDeadlock = new ConfigSetting(CATEGORY_DEBUG, this.config.get(CATEGORY_DEBUG, "dump-heap-on-deadlock", false, "Dump the heap in the event of a deadlock"));
        this.dumpThreadsOnWarn = new ConfigSetting(CATEGORY_DEBUG, this.config.get(CATEGORY_DEBUG, "dump-threads-on-warn", false, "Dump the the server thread on deadlock warning"));

        // World Settings
        this.infiniteWaterSource = new ConfigSetting(CATEGORY_WORLD_SETTINGS, this.config.get(CATEGORY_WORLD_SETTINGS, "infinite-water-source", true, "Vanilla water source behavior - is infinite"));
        this.flowingLavaDecay = new ConfigSetting(CATEGORY_WORLD_SETTINGS, this.config.get(CATEGORY_WORLD_SETTINGS, "flowing-lava-decay", false, "Lava behaves like vanilla water when source block is removed"));

        this.settings.clear();
        this.settings.put("general.config-enabled", this.configEnabled);
        this.settings.put("chunk-settings.chunk-load-override", this.chunkLoadOverride);
        this.settings.put("debug.dump-chunks-on-deadlock", this.dumpChunksOnDeadlock);
        this.settings.put("debug.dump-heap-on-deadlock", this.dumpHeapOnDeadlock);
        this.settings.put("debug.dump-threads-on-warn", this.dumpThreadsOnWarn);
        this.settings.put("debug.thread-contention-monitoring", this.enableThreadContentionMonitoring);
        this.settings.put("entity-settings.check-entity-bounding-boxes", this.checkEntityBoundingBoxes);
        this.settings.put("entity-settings.check-max-speed", this.checkEntityMaxSpeeds);
        this.settings.put("entity-settings.max-bounding-box-size", this.largeBoundingBoxLogSize);
        this.settings.put("entity-settings.maxspeed", this.entityMaxSpeed);
        this.settings.put("logging.disabled-warnings", this.disableWarnings);
        this.settings.put("logging.chunk-load", this.chunkLoadLogging);
        this.settings.put("logging.chunk-unload", this.chunkUnloadLogging);
        this.settings.put("logging.entity-spawn", this.entitySpawnLogging);
        this.settings.put("logging.entity-despawn", this.entityDespawnLogging);
        this.settings.put("logging.entity-death", this.entityDeathLogging);
        this.settings.put("logging.detailed-logging", this.logWithStackTraces);
        this.settings.put("logging.entity-collision-checks", this.logEntityCollisionChecks);
        this.settings.put("logging.entity-speed-removal", this.logEntitySpeedRemoval);
        this.settings.put("logging.collision-warn-size", this.largeCollisionLogSize);
        this.settings.put("logging.entity-count-warn-size", this.largeEntityCountLogSize);
        this.settings.put("world-settings.infinite-water-source", this.infiniteWaterSource);
        this.settings.put("world-settings.flowing-lava-decay", this.flowingLavaDecay);
        this.load();
    }

    public void load() {
        try {
            this.config.load();

            for (ConfigSetting toggle : this.settings.values()) {
                toggle.setValue(this.config.get(toggle.getCategory(), toggle.getProperty().getName(), toggle.getProperty().getDefault(), toggle.getDescription()).getString());
            }

            this.save();
        } catch (Exception ex) {
            MinecraftServer.getServer().logSevere("Could not load " + this.config);
            ex.printStackTrace();
        }
    }

    public void save() {
        this.config.save();
    }

    public Type getType() {
        return this.type;
    }

    public String getConfigName() {
        return this.configName;
    }
}
