/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
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

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.event.SpongeEventManager;
import org.spongepowered.mod.plugin.SpongePluginManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SpongeGame implements Game {
    private static final String apiVersion = Game.class.getPackage().getImplementationVersion();
    private static final String implementationVersion = SpongeGame.class.getPackage().getImplementationVersion();
    private final Logger logger = LogManager.getLogger("sponge");
    private final List<World> worlds = Lists.newArrayList();
    private final SpongePluginManager pluginManager;
    private final SpongeEventManager eventManager;

    public SpongeGame() {
        this.pluginManager = new SpongePluginManager();
        this.eventManager = new SpongeEventManager();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Platform getPlatform() {
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT:
                return Platform.CLIENT;
            default:
                return Platform.SERVER;
        }
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public Collection<World> getWorlds() {
        return worlds;
    }

    @Override
    public World getWorld(UUID uniqueId) {
        for(World world : worlds) {
            if(world.getUniqueID().equals(uniqueId)) {
                return world;
            }
        }

        return null;
    }

    @Override
    public World getWorld(String worldName) {
        for(World world : worlds) {
            if(world.getName().equals(worldName)) {
                return world;
            }
        }

        return null;
    }

    @Override
    public void broadcastMessage(String message) {

    }

    @Subscribe
    public void onWorldLoad(WorldEvent.Load event) {
        //Make sure that we are taling about the same world...
        if(!(event.world instanceof WorldServer)) {
            return;
        }

        SpongeWorld spongeWorld = new SpongeWorld((WorldServer) event.world);
        worlds.add(spongeWorld);
    }

    @Subscribe
    public void onWorldUnload(WorldEvent.Unload event) {
        //Make sure that we are taling about the same world...
        if(!(event.world instanceof WorldServer)) {
            return;
        }

        String name = event.world.getWorldInfo().getWorldName();
        Iterator<World> it = worlds.iterator();
        while(it.hasNext()) {
            //Check if one the world matched the world being unloaded.
            if(it.next().getName().equals(name)) {
                //Remove world from list
                it.remove();
            }
        }
    }

    public String getAPIVersion() {
        return apiVersion != null ? apiVersion : "UNKNOWN";
    }

    @Override
    public String getImplementationVersion() {
        return implementationVersion != null ? implementationVersion : "UNKNOWN";
    }
}
