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
package org.spongepowered.mod.guice;

import static com.google.inject.name.Names.named;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.guice.ConfigDirAnnotation;
import org.spongepowered.common.plugin.SpongeApiContainer;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.world.SpongeTeleportHelper;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.SpongeModGame;
import org.spongepowered.mod.SpongeModPlatform;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.network.SpongeModNetworkManager;
import org.spongepowered.mod.plugin.SpongePluginManager;

import java.io.File;
import java.nio.file.Path;

public class SpongeGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SpongeMod.class).toInstance(SpongeMod.instance);
        bind(Logger.class).toInstance(LogManager.getLogger(SpongeImpl.ECOSYSTEM_NAME));

        bind(PluginContainer.class).annotatedWith(named(SpongeImpl.ECOSYSTEM_ID)).toInstance((PluginContainer) SpongeMod.instance);
        bind(PluginContainer.class).annotatedWith(named(SpongeImpl.API_ID)).to(SpongeApiContainer.class).in(Scopes.SINGLETON);
        bind(PluginContainer.class).annotatedWith(named(SpongeImpl.GAME_ID)).toInstance((PluginContainer) Loader.instance().getMinecraftModContainer());

        bind(Game.class).to(SpongeModGame.class).in(Scopes.SINGLETON);
        bind(MinecraftVersion.class).toInstance(SpongeImpl.MINECRAFT_VERSION);
        bind(Platform.class).to(SpongeModPlatform.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(SpongePluginManager.class).in(Scopes.SINGLETON);
        bind(ServiceManager.class).to(SimpleServiceManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(SpongeModEventManager.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(SpongeGameRegistry.class).in(Scopes.SINGLETON);
        bind(TeleportHelper.class).to(SpongeTeleportHelper.class).in(Scopes.SINGLETON);
        bind(ChannelRegistrar.class).to(SpongeModNetworkManager.class).in(Scopes.SINGLETON);

        ConfigDirAnnotation sharedRoot = new ConfigDirAnnotation(true);
        bind(Path.class).annotatedWith(sharedRoot).toInstance(SpongeImpl.getConfigDir());
        bind(File.class).annotatedWith(sharedRoot).toInstance(SpongeImpl.getConfigDir().toFile());
    }
}
