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
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import net.minecraftforge.fml.common.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.config.ConfigFile;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.event.SpongeEventBus;
import org.spongepowered.mod.plugin.SpongePluginManager;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import javax.inject.Inject;
import java.io.File;
import java.lang.annotation.Annotation;

public class SpongeGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        PluginScope pluginScope = new PluginScope();

        DefaultConfig pluginConfig = new ConfigFileAnnotation(true);
        ConfigDir sharedDir = new ConfigDirAnnotation(true);
        ConfigDir pluginDir = new ConfigDirAnnotation(false);

        bindScope(PluginScoped.class, pluginScope);
        bind(PluginScope.class).toInstance(pluginScope);

        bind(Game.class).to(SpongeGame.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(SpongePluginManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(SpongeEventBus.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(SpongeGameRegistry.class).in(Scopes.SINGLETON);
        bind(File.class).annotatedWith(sharedDir).toProvider(GeneralConfigDirProvider.class).in(Scopes.SINGLETON);

        bind(PluginContainer.class).toProvider(PluginContainerProvider.class).in(PluginScoped.class);
        bind(Logger.class).toProvider(PluginLogProvider.class).in(PluginScoped.class);
        bind(File.class).annotatedWith(pluginConfig).toProvider(PluginConfigFileProvider.class).in(PluginScoped.class);
        bind(File.class).annotatedWith(pluginDir).toProvider(PluginConfigDirProvider.class).in(PluginScoped.class);
        bind(ConfigFile.class).annotatedWith(pluginConfig).toProvider(PluginHoconConfigProvider.class).in(PluginScoped.class);
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

    // This is strange, but required for Guice and annotations with values.
    private static class ConfigDirAnnotation implements ConfigDir {

        boolean shared;

        ConfigDirAnnotation(boolean isShared) {
            this.shared = isShared;
        }

        @Override
        public boolean sharedRoot() {
            return this.shared;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ConfigDir.class;
        }

        // See Javadocs for java.lang.annotation.Annotation for specification of equals, hashCode, toString
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof ConfigDir)) {
                return false;
            }

            ConfigDir that = (ConfigDir) o;

            return sharedRoot() == that.sharedRoot();
        }

        @Override
        public int hashCode() {
            return (127 * "sharedRoot".hashCode()) ^ Boolean.valueOf(sharedRoot()).hashCode();
        }

        @Override
        public String toString() {
            return "@org.spongepowered.api.service.config.ConfigDir(" +
                   "sharedRoot=" + this.shared +
                   ')';
        }
    }

    private static class PluginLogProvider implements Provider<Logger> {

        private final PluginScope scope;

        @Inject
        private PluginLogProvider(PluginScope sc) {
            this.scope = sc;
        }

        @Override
        public Logger get() {
            PluginContainer current = this.scope.getCurrentScope();
            return LoggerFactory.getLogger(current.getId());
        }
    }

    private static class GeneralConfigDirProvider implements Provider<File> {

        @Override
        public File get() {
            return Loader.instance().getConfigDir();
        }
    }

    private static class PluginConfigFileProvider implements Provider<File> {

        private final PluginScope scope;

        @Inject
        private PluginConfigFileProvider(PluginScope sc) {
            this.scope = sc;
        }

        @Override
        public File get() {
            PluginContainer current = this.scope.getCurrentScope();
            return new File(Loader.instance().getConfigDir(), current.getId() + ".conf");
        }
    }

    private static class PluginHoconConfigProvider implements Provider<ConfigFile> {

        private final PluginScope scope;

        @Inject
        private PluginHoconConfigProvider(PluginScope sc) {
            this.scope = sc;
        }

        @Override
        public ConfigFile get() {
            PluginContainer current = this.scope.getCurrentScope();
            return ConfigFile.parseFile(new File(Loader.instance().getConfigDir(), current.getId() + ".conf"));
        }
    }

    private static class PluginConfigDirProvider implements Provider<File> {

        private final PluginScope scope;

        @Inject
        private PluginConfigDirProvider(PluginScope sc) {
            this.scope = sc;
        }

        @Override
        public File get() {
            PluginContainer current = this.scope.getCurrentScope();
            return new File(Loader.instance().getConfigDir(), current.getId() + "/");
        }
    }

    private static class PluginContainerProvider implements Provider<PluginContainer> {

        private final PluginScope scope;

        @Inject
        private PluginContainerProvider(PluginScope sc) {
            this.scope = sc;
        }

        @Override
        public PluginContainer get() {
            return this.scope.getInstance(Key.get(PluginContainer.class));
        }
    }
}
