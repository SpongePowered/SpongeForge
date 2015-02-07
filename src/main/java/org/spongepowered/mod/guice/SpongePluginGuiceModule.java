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
package org.spongepowered.mod.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import net.minecraftforge.fml.common.Loader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;
import java.lang.annotation.Annotation;

import javax.inject.Inject;

/**
 * Guice module that contains injections for a single plugin
 */
public class SpongePluginGuiceModule extends AbstractModule {

    private final PluginContainer container;

    public SpongePluginGuiceModule(PluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        DefaultConfig pluginConfig = new ConfigFileAnnotation(true);
        ConfigDir pluginDir = new ConfigDirAnnotation(false);

        bind(PluginContainer.class).toInstance(this.container);
        bind(Logger.class).toInstance(LoggerFactory.getLogger(this.container.getId()));
        bind(File.class).annotatedWith(pluginConfig).toProvider(PluginConfigFileProvider.class).in(Scopes.SINGLETON);
        bind(File.class).annotatedWith(pluginDir).toProvider(PluginConfigDirProvider.class).in(Scopes.SINGLETON);
        bind(ConfigurationLoader.class).annotatedWith(pluginConfig).toProvider(PluginHoconConfigProvider.class);

    }

    // This is strange, but required for Guice and annotations with values.
    private static class ConfigFileAnnotation implements DefaultConfig {

        boolean shared;

        ConfigFileAnnotation(boolean isShared) {
            this.shared = isShared;
        }

        @Override
        public boolean sharedRoot() {
            return this.shared;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return DefaultConfig.class;
        }

        // See Javadocs for java.lang.annotation.Annotation for specification of equals, hashCode, toString
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof DefaultConfig)) {
                return false;
            }

            DefaultConfig that = (DefaultConfig) o;

            return sharedRoot() == that.sharedRoot();
        }

        @Override
        public int hashCode() {
            return (127 * "sharedRoot".hashCode()) ^ Boolean.valueOf(sharedRoot()).hashCode();
        }

        @Override
        public String toString() {
            return "@org.spongepowered.api.service.config.Config(" +
                   "sharedRoot=" + this.shared +
                   ')';
        }
    }

    private static class PluginConfigFileProvider implements Provider<File> {

        private final PluginContainer container;

        @Inject
        private PluginConfigFileProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public File get() {
            return new File(Loader.instance().getConfigDir(), this.container.getId() + ".conf");
        }
    }

    private static class PluginHoconConfigProvider implements Provider<ConfigurationLoader> {

        private final PluginContainer container;

        @Inject
        private PluginHoconConfigProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public ConfigurationLoader get() {
            return HoconConfigurationLoader.builder().setFile(new File(Loader.instance().getConfigDir(), this.container.getId() + ".conf")).build();
        }
    }

    private static class PluginConfigDirProvider implements Provider<File> {

        private final PluginContainer container;

        @Inject
        private PluginConfigDirProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public File get() {
            return new File(Loader.instance().getConfigDir(), this.container.getId() + "/");
        }
    }
}
