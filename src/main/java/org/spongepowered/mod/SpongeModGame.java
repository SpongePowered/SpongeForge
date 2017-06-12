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

import com.google.inject.Singleton;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.Server;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.mod.registry.SpongeForgeGameDictionary;

import java.nio.file.Path;
import java.util.Optional;

@NonnullByDefault
@Singleton
public final class SpongeModGame extends SpongeGame {

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
    public Optional<GameDictionary> getGameDictionary() {
        return Optional.of(SpongeForgeGameDictionary.instance);
    }

}
