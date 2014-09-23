/**
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
package org.spongepowered.mod.event;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.event.world.BlockEvent;

import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.voxel.VoxelEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class EventRegistry {
    
    private static BiMap<Class<?>, Class<?>> eventMap = HashBiMap.create();
    
    private EventRegistry() {
    }
    
    static {
        // FML events
        register(FMLInitializationEvent.class, InitializationEvent.class);
        register(FMLServerStartingEvent.class, ServerStartingEvent.class);
        register(FMLPreInitializationEvent.class, PreInitializationEvent.class);
        // Forge events
        register(BlockEvent.BreakEvent.class, VoxelEvent.class);
    }
    
    private static void register(Class<?> otherEvent, Class<?> spongeEvent) {
        eventMap.put(otherEvent, spongeEvent);
    }
    
    public static Class<?> getImplementingClass(Class<?> apiClass) {
        return eventMap.inverse().get(apiClass);
    }
    
    public static Class<?> getAPIClass(Class<?> implementingClass) {
        return eventMap.get(implementingClass);
    }

}
