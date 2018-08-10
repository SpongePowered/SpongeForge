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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongePlatform;

@Singleton
public final class SpongeModPlatform extends SpongePlatform {

    @Inject
    public SpongeModPlatform(PluginManager manager, MinecraftVersion minecraftVersion) {
        super(manager, (PluginContainer) SpongeMod.instance, minecraftVersion);
        this.platformMap.put("ForgeVersion", ForgeVersion.getVersion());
    }

    @Override
    public Type getType() {
        switch (FMLCommonHandler.instance().getSide()) {
            case CLIENT:
                return Type.CLIENT;
            case SERVER:
                return Type.SERVER;
            default:
                return Type.UNKNOWN;
        }
    }

    @Override
    public Type getExecutionType() {
        return staticGetExecutionType();
    }

    public static Type staticGetExecutionType() {
        String threadName = Thread.currentThread().getName();

        // Most common
        if (threadName.equals("Server thread")
                || threadName.startsWith("Netty Epoll Server IO #")
                || threadName.startsWith("Netty Server IO #")
                || threadName.startsWith("User Authenticator #")) {
            return Type.SERVER;
        } else if (threadName.equals("Client thread")
                || threadName.startsWith("Netty Client IO #")
                || threadName.startsWith("Netty Epoll Client IO #")
                || threadName.startsWith("Netty Local Client IO ")
                || threadName.startsWith("Netty Local Server IO #")) {
            return Type.CLIENT;
        }

        if (threadName.equals("Server Infinisleeper")
                || threadName.equals("Server console handler")
                || threadName.equals("Server Shutdown Thread")) {
            return Type.SERVER;
        } else if (threadName.startsWith("Server Pinger #")
                || threadName.startsWith("Chunk Batcher ")
                || threadName.equals("Client Shutdown Thread")
                || threadName.equals("Realms-connect-task")
                || threadName.startsWith("Texture Downloader #")
                || threadName.startsWith("Server Connector #")
                || threadName.equals("Timer hack thread")
                || threadName.equals("Twitch authenticator")
                || threadName.equals("Twitch shutdown hook")
                || threadName.equals("Sound Library Loader")) {
            return Type.CLIENT;
        } else {
            // "Downloader "
            // "File IO Thread"
            return Type.UNKNOWN;
        }
    }

}
