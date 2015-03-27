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
package org.spongepowered.granite.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.granite.Granite;
import org.spongepowered.granite.GraniteGame;
import org.spongepowered.granite.GraniteGameRegistry;
import org.spongepowered.granite.event.GraniteEventManager;
import org.spongepowered.granite.plugin.GranitePluginManager;

import java.io.File;

public class GraniteGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Granite.class).toInstance(Granite.instance);
        bind(Game.class).to(GraniteGame.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(GranitePluginManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(GraniteEventManager.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(GraniteGameRegistry.class).in(Scopes.SINGLETON);
        bind(ServiceManager.class).to(SimpleServiceManager.class).in(Scopes.SINGLETON);

        ConfigDirAnnotation sharedRoot = new ConfigDirAnnotation(true);
        bind(File.class).annotatedWith(sharedRoot).toInstance(Granite.instance.getConfigDirectory());
    }

}
