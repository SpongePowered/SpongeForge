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

import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.mod.service.world.SpongeChunkTicketManager;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

@Mixin(value = MinecraftServer.class, priority = 1001)
public abstract class MixinMinecraftServer implements Server, IMixinMinecraftServer {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Snooper usageSnooper;

    public ChunkTicketManager chunkTicketManager = new SpongeChunkTicketManager();

    @Shadow(remap = false) public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow private boolean serverIsRunning;

    @Override
    public long[] getWorldTickTimes(int dimensionId) {
        return this.worldTickTimes.get(dimensionId);
    }

    @Override
    public void putWorldTickTimes(int dimensionId, long[] tickTimes) {
        this.worldTickTimes.put(dimensionId, tickTimes);
    }

    @Override
    public void removeWorldTickTimes(int dimensionId) {
        this.worldTickTimes.remove(dimensionId);
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        return this.chunkTicketManager;
    }

    @Inject(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateEntities()V", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPostUpdateEntities(CallbackInfo ci, Integer ids[], int x, int id, long i, WorldServer worldServer) {
        IMixinWorldServer spongeWorld = (IMixinWorldServer) worldServer;
        if (spongeWorld.getChunkGCTickInterval() > 0) {
            spongeWorld.doChunkGC();
        }
    }

    /**
     * @author Zidane - May 11th, 2016
     * @reason Directs to {@link WorldManager} for multi world handling.
     *
     * @param dimensionId The requested dimension id
     * @return The world server, if available, or else the overworld
     */
    @Overwrite
    public WorldServer getWorld(int dimensionId) {
        WorldServer ret = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);
        if (ret == null) {
            DimensionManager.initDimension(dimensionId);
            ret = WorldManager.getWorldByDimensionId(dimensionId).orElse(null);
        }

        if (ret == null) {
            return WorldManager.getWorldByDimensionId(0).orElseThrow(() -> new RuntimeException("Attempt made to initialize "
                    + "dimension before overworld is loaded!"));
        }

        return ret;
    }

    /**
     * @author Zidane - Chris Sanders
     * @reason Overwrite to take control of the stopping process and direct to WorldManager
     */
    @Overwrite
    public void stopServer()
    {
        LOGGER.info("Stopping server");

        // Sponge Start - Force player profile cache save
        ((MinecraftServer) (Object) this).getPlayerProfileCache().save();

        final MinecraftServer server = (MinecraftServer) (Object) this;
        if (server.getNetworkSystem() != null)
        {
            server.getNetworkSystem().terminateEndpoints();
        }

        if (server.getPlayerList() != null)
        {
            LOGGER.info("Saving players");
            server.getPlayerList().saveAllPlayerData();
            server.getPlayerList().removeAllPlayers();
        }

        if (server.worlds != null)
        {
            LOGGER.info("Saving worlds");

            for (WorldServer worldserver : server.worlds)
            {
                if (worldserver != null)
                {
                    worldserver.disableLevelSaving = false;
                }
            }

            server.saveAllWorlds(false);

            for (WorldServer worldserver1 : server.worlds)
            {
                if (worldserver1 != null)
                {
                    // Turn off Async Lighting
                    if (SpongeImpl.getGlobalConfig().getConfig().getModules().useOptimizations() &&
                        SpongeImpl.getGlobalConfig().getConfig().getOptimizations().useAsyncLighting()) {
                        ((IMixinWorldServer) worldserver1).getLightingExecutor().shutdown();

                        try {
                            ((IMixinWorldServer) worldserver1).getLightingExecutor().awaitTermination(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            ((IMixinWorldServer) worldserver1).getLightingExecutor().shutdownNow();
                        }
                    }

                    // Direct to WorldManager for unload
                    WorldManager.unloadWorld(worldserver1, false);
                    // Sponge End
                    worldserver1.flush();
                }
            }
        }

        if (usageSnooper.isSnooperRunning())
        {
            this.usageSnooper.stopSnooper();
        }
    }
}
