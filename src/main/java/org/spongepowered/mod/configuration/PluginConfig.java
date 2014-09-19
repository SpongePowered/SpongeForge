/**
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.configuration.ConfigArray;
import org.spongepowered.api.configuration.ConfigElement;
import org.spongepowered.api.configuration.ConfigObject;
import org.spongepowered.api.configuration.ConfigPrimitive;
import org.spongepowered.api.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PluginConfig extends ConfigurationObject implements Configuration {

    protected static final String EXT = ".cfg";
    private String name;
    private File file;

    public PluginConfig(File dir, String name) {
        this(makeFile(dir, name));
        this.name = name;
    }

    public PluginConfig(File file) {
        super(null);
        this.file = file;
        try {
            this.file.mkdirs();
            this.file.createNewFile();
            if (exists()) {
                this.load();
            }
        } catch (IOException ex) {
            LogManager.getLogger().warn("IOException while trying to load " 
                    + this.file.getPath(), ex);
        }
    }

    private static File makeFile(File dir, String name) {
        if (dir.isFile()) {
            throw new RuntimeException(dir.getPath() + "is a file.");
        }
        return new File(dir, name + EXT);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void save() throws IOException {
        Config config = Serializer.deserialize(this);
        FileUtils.write(getFile(), config.root().render());
    }

    @Override
    public void load() throws IOException {
        Config config = ConfigFactory.parseFile(getFile());
        Serializer.serialize(config.root(), this);
    }

    @Override
    public void clear() {
        this.delete();
    }

    public static class Serializer {

        public static void serialize(com.typesafe.config.ConfigObject origin, Configuration target) {
            Map<String, Object> map = origin.unwrapped();
            mapToObject(map, target);
        }

        @SuppressWarnings("unchecked")
        private static void mapToObject(Map<String, Object> origin, ConfigObject target) {
            for (Entry<String, Object> entry : origin.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    // object
                    mapToObject((Map<String, Object>) entry.getValue(), target.getObject(entry.getKey()));
                } else if (entry.getValue() instanceof List) {
                    // array
                    listToArray((List<Object>)entry.getValue(), target.getArray(entry.getKey(), Object.class));
                } else {
                    // primitive
                    target.getPrimitive(entry.getKey(), entry.getValue());
                }
            }
        }

        private static void listToArray(List<Object> origin, ConfigArray<Object> target) {
            for (Object obj : origin) {
                target.add(obj);
            }
        }

        public static Config deserialize(Configuration origin) {
            Map<String, Object> object = Maps.newHashMap();
            objectToMap(origin, object);
            return ConfigValueFactory.fromMap(object, origin.getComment()).toConfig();
        }

        @SuppressWarnings("unchecked")
        private static void objectToMap(ConfigObject origin, Map<String, Object> target) {
            for (Entry<String, ConfigElement<?>> elem : origin) {
                if (elem.getValue() instanceof ConfigPrimitive) {
                    target.put(elem.getKey(), elem.getValue());
                } else if (elem.getValue() instanceof ConfigArray) {
                    List<Object> list = Lists.newArrayList();
                    arrayToList((ConfigArray<Object>)elem.getValue(), list);
                    target.put(elem.getKey(), list);
                } else if (elem.getValue() instanceof ConfigObject) {
                    Map<String, Object> map = Maps.newHashMap();
                    objectToMap((ConfigObject) elem.getValue(), map);
                    target.put(elem.getKey(), map);
                }
            }
        }

        private static void arrayToList(ConfigArray<Object> origin, List<Object> target) {
            for (Object obj : origin) {
                target.add(obj);
            }
        }
    }
}
