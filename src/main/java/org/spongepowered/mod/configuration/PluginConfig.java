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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.configuration.Configuration;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;

public class PluginConfig extends ConfigurationObject implements Configuration {

    protected static final String EXT = ".cfg";
    private String name;
    private File file;

    public PluginConfig(PluginContainer plugin, String name) {
        this(makeFile(plugin, name));
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
            LogManager.getLogger().warn("IOException while trying to load " + this.file.getPath(), ex);
        }
    }

    private static File makeFile(PluginContainer plugin, String name) {
        return new File(plugin.getResourceFolder(true), name + EXT);
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
        Config config = ConfigFactory.empty();
        readConfig(config, this);
        FileUtils.write(file, config.root().render()); // maybe?
    }

    @Override
    public void load() throws IOException {
        Config config = ConfigFactory.parseFile(file);
        writeConfig(this, config);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private void writeConfig(Configuration config, Config target) {
        throw new UnsupportedOperationException();
    }

    private void readConfig(Config config, Configuration target) {
        throw new UnsupportedOperationException();
    }

}
