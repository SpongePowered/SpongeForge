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
package org.spongepowered.mod.mixin.core.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.mod.service.world.SpongeChunkTicketManager;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Optional;

@Mixin(value = MinecraftServer.class, priority = 1001)
public abstract class MixinMinecraftServer implements Server, IMixinMinecraftServer {

    public ChunkTicketManager chunkTicketManager = new SpongeChunkTicketManager();

    @Shadow public WorldServer[] worldServers;
    @Shadow(remap = false) public Hashtable<Integer, long[]> worldTickTimes;

    @Override
    public Hashtable<Integer, long[]> getWorldTickTimes() {
        return this.worldTickTimes;
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        return this.chunkTicketManager;
    }

    /**
     * @author Zidane - May 11th, 2016
     * @reason Directs to {@link WorldManager} for multi world handling.
     *
     * @param dimensionId The requested dimension id
     * @return The world server, if available, or else the overworld
     */
    @Overwrite
    public WorldServer worldServerForDimension(int dimensionId) {
        WorldServer ret = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);
        if (ret != null) {
            return ret;
        }

        // They passed us an unknown dimension id, fail fast to overworld
        if (!WorldManager.isDimensionRegistered(dimensionId)) {
            System.err.println("Dimension not registered.");
            return this.worldServers[0];
        }

        final DimensionType dimensionType = WorldManager.getDimensionType(dimensionId).orElse(null);
        final Optional<Path> registeredWorldPath = WorldManager.getDimensionPath(dimensionId);
        String worldFolderName;

        if (registeredWorldPath.isPresent()) {
            worldFolderName = registeredWorldPath.get().getFileName().toString();
        } else {
            // Only not present if this dimension instance has never been loaded
            final WorldProvider provider = dimensionType.createDimension();
            provider.setDimension(dimensionId);
            worldFolderName = provider.getSaveFolder();
        }

        final SpongeConfig<?> config = SpongeHooks.getActiveConfig(((IMixinDimensionType) (Object) dimensionType).getConfigPath(), worldFolderName);
        if (config.getConfig().isConfigEnabled() && !config.getConfig().getWorld().isWorldEnabled()) {
            System.err.println("Dimension disabled.");
            return this.worldServers[0];
        }

        DimensionManager.initDimension(dimensionId);
        ret = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);

        if (ret == null) {
            System.err.println("Dimension failed to initialize.");
            ret = this.worldServers[0];
        }

        return ret;
    }
}
