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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.world.FakePlayer;
import org.spongepowered.mod.registry.SpongeForgeGameDictionary;

import java.nio.file.Path;

@NonnullByDefault
@Singleton
public final class SpongeModGame extends SpongeGame {

    @Inject
    public SpongeModGame(Platform platform, PluginManager pluginManager, EventManager eventManager,
            AssetManager assetManager, ServiceManager serviceManager, TeleportHelper teleportHelper, ChannelRegistrar channelRegistrar,
            Logger logger, SpongeGameRegistry gameRegistry) {
        super(platform, pluginManager, eventManager, assetManager, serviceManager, teleportHelper, channelRegistrar, logger, gameRegistry);
    }

    @Override
    public Path getSavesDirectory() {
        return FMLCommonHandler.instance().getSavesDirectory().toPath();
    }

    @Override
    public boolean isServerAvailable() {
        return FMLCommonHandler.instance().getSidedDelegate().getServer() != null;
    }

    @Override
    public Server getServer() {
        final MinecraftServer server = FMLCommonHandler.instance().getSidedDelegate().getServer();
        checkState(server != null, "Server has not been initialized yet!");
        return (Server) server;
    }

    @Override
    public GameDictionary getGameDictionary() {
        return SpongeForgeGameDictionary.instance;
    }

    @Override
    public FakePlayer.Factory createFakePlayerFactory() {
        return new FakePlayer.Factory() {

            @Override
            public EntityPlayerMP getPlayer(WorldServer world, GameProfile profile) {
                return FakePlayerFactory.get(world, profile);
            }
        };
    }

}
