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
package org.spongepowered.mod;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.mod.network.SpongeModNetworkManager;
import org.spongepowered.mod.registry.SpongeGameDictionary;

import java.nio.file.Path;

import javax.inject.Inject;

@NonnullByDefault
@Singleton
public final class SpongeModGame extends SpongeGame {

    private GameState state = GameState.CONSTRUCTION;

    private final SpongeModNetworkManager networkManager = new SpongeModNetworkManager();

    @Inject
    public SpongeModGame(Platform platform, PluginManager pluginManager, EventManager eventManager, SpongeGameRegistry gameRegistry,
            ServiceManager serviceManager, TeleportHelper teleportHelper) {
        super(platform, pluginManager, eventManager, gameRegistry, serviceManager, teleportHelper);
    }

    @Override
    public Path getSavesDirectory() {
        return FMLCommonHandler.instance().getSavesDirectory().toPath();
    }

    @Override
    public GameState getState() {
        return this.state;
    }

    public void setState(GameState state) {
        this.state = checkNotNull(state);
    }

    @Override
    public GameDictionary getGameDictionary() {
        return SpongeGameDictionary.instance;
    }

    @Override
    public SpongeNetworkManager getChannelRegistrar() {
        return this.networkManager;
    }

}
