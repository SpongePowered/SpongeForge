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
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge_AsyncLighting;
import org.spongepowered.common.world.WorldManager;

import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(value = MinecraftServer.class, priority = 1002)
public abstract class MinecraftServerMixin_Forge implements MinecraftServerBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Snooper usageSnooper;
    @Shadow(remap = false) public Hashtable<Integer, long[]> worldTickTimes;
    @Shadow private boolean serverIsRunning;

    @Shadow public abstract PlayerProfileCache getPlayerProfileCache();

    @Override
    public long[] bridge$getWorldTickTimes(final int dimensionId) {
        return this.worldTickTimes.get(dimensionId);
    }

    @Override
    public void bridge$putWorldTickTimes(final int dimensionId, final long[] tickTimes) {
        this.worldTickTimes.put(dimensionId, tickTimes);
    }

    @Override
    public void bridge$removeWorldTickTimes(final int dimensionId) {
        this.worldTickTimes.remove(dimensionId);
    }

    @Inject(method = "updateTimeLightAndEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;updateEntities()V",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void forgeImpl$UpdateChunkGC(final CallbackInfo ci, final Integer[] ids, final int x, final int id, final long i,
        final WorldServer worldServer) {
        final WorldServerBridge spongeWorld = (WorldServerBridge) worldServer;
        if (spongeWorld.bridge$getChunkGCTickInterval() > 0) {
            spongeWorld.bridge$doChunkGC();
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
    public WorldServer getWorld(final int dimensionId) {
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
        ((PlayerProfileCacheBridge) this.getPlayerProfileCache()).bridge$setCanSave(true);
        ((MinecraftServer) (Object) this).getPlayerProfileCache().save();
        ((PlayerProfileCacheBridge) this.getPlayerProfileCache()).bridge$setCanSave(false);

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

            for (final WorldServer worldserver : server.worlds)
            {
                if (worldserver != null)
                {
                    worldserver.disableLevelSaving = false;
                }
            }

            server.saveAllWorlds(false);

            for (final WorldServer worldserver1 : server.worlds)
            {
                if (worldserver1 != null)
                {
                    // Turn off Async Lighting
                    if (worldserver1 instanceof WorldServerBridge_AsyncLighting) {
                        final ExecutorService lightingExecutor = ((WorldServerBridge_AsyncLighting) worldserver1).asyncLightingBridge$getLightingExecutor();
                        lightingExecutor.shutdown();
                        try {
                            lightingExecutor.awaitTermination(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            lightingExecutor.shutdownNow();
                        }
                    }

                    // Direct to WorldManager for unload
                    WorldManager.unloadWorld(worldserver1, false, true);
                    // Sponge End
                    worldserver1.flush();
                }
            }
        }

        if (this.usageSnooper.isSnooperRunning())
        {
            this.usageSnooper.stopSnooper();
        }
    }
}
