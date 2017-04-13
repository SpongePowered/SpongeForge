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
package org.spongepowered.mod.inject;

import org.spongepowered.api.Platform;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.inject.SpongeImplementationModule;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.SpongeModGame;
import org.spongepowered.mod.SpongeModPlatform;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.network.SpongeModNetworkManager;
import org.spongepowered.mod.plugin.SpongeModPluginContainer;
import org.spongepowered.mod.plugin.SpongeModPluginManager;

public class SpongeForgeModule extends SpongeImplementationModule {

    @Override
    protected void configure() {
        super.configure();

        this.bind(SpongeMod.class).toInstance(SpongeMod.instance);
        this.bind(SpongeGame.class).to(SpongeModGame.class);
        this.bind(Platform.class).to(SpongeModPlatform.class);
        this.bind(PluginManager.class).to(SpongeModPluginManager.class);
        this.bind(EventManager.class).to(SpongeModEventManager.class);
        this.bind(ChannelRegistrar.class).to(SpongeModNetworkManager.class);

        this.requestInjection(SpongeMod.instance);
        this.requestStaticInjection(SpongeModPluginContainer.class);
    }
}
