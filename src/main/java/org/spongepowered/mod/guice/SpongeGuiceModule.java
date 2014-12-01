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

package org.spongepowered.mod.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import net.minecraftforge.fml.common.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.event.SpongeEventManager;
import org.spongepowered.mod.plugin.SpongePluginManager;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.io.File;

import javax.inject.Inject;

public class SpongeGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        PluginScope pluginScope = new PluginScope();

        bindScope(PluginScoped.class, pluginScope);
        bind(PluginScope.class).toInstance(pluginScope);

        bind(Game.class).to(SpongeGame.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(SpongePluginManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(SpongeEventManager.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(SpongeGameRegistry.class).in(Scopes.SINGLETON);
        bind(File.class).annotatedWith(Names.named("GeneralConfigDir")).toProvider(GeneralConfigDirProvider.class).in(Scopes.SINGLETON);

        bind(PluginContainer.class).toProvider(PluginContainerProvider.class).in(PluginScoped.class);
        bind(Logger.class).toProvider(PluginLogProvider.class).in(PluginScoped.class);
        bind(File.class).annotatedWith(Names.named("PluginConfigFile")).toProvider(PluginConfigFileProvider.class).in(PluginScoped.class);
        bind(File.class).annotatedWith(Names.named("PluginConfigDir")).toProvider(PluginConfigDirProvider.class).in(PluginScoped.class);
    }

    private static class PluginLogProvider implements Provider<Logger> {

        private final PluginScope scope;

        @Inject
        private PluginLogProvider(PluginScope sc) {
            scope = sc;
        }

        @Override
        public Logger get() {
            PluginContainer current = scope.getCurrentScope();
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
            scope = sc;
        }

        @Override
        public File get() {
            PluginContainer current = scope.getCurrentScope();
            return new File(Loader.instance().getConfigDir(), current.getId() + ".conf");
        }
    }

    private static class PluginConfigDirProvider implements Provider<File> {

        private final PluginScope scope;

        @Inject
        private PluginConfigDirProvider(PluginScope sc) {
            scope = sc;
        }

        @Override
        public File get() {
            PluginContainer current = scope.getCurrentScope();
            return new File(Loader.instance().getConfigDir(), current.getId() + "/");
        }
    }

    private static class PluginContainerProvider implements Provider<PluginContainer> {

        private final PluginScope scope;

        @Inject
        private PluginContainerProvider(PluginScope sc) {
            scope = sc;
        }

        @Override
        public PluginContainer get() {
            return scope.getInstance(Key.get(PluginContainer.class));
        }
    }
}
