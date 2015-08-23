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
package org.spongepowered.mod.event;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerJoinEvent;
import org.spongepowered.api.event.source.entity.living.player.PlayerQuitEvent;
import org.spongepowered.api.event.source.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.source.game.state.GameConstructionEvent;
import org.spongepowered.api.event.source.game.state.GameInitializationEvent;
import org.spongepowered.api.event.source.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.source.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.source.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.source.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.source.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.source.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.source.game.state.GameStoppingServerEvent;

public class EventRegistry {

    private static BiMap<Class<?>, Class<?>> eventMap = HashBiMap.create();

    private EventRegistry() {
    }

    static {
        // FML state events
        register(FMLConstructionEvent.class, GameConstructionEvent.class);
        register(FMLPreInitializationEvent.class, GamePreInitializationEvent.class);
        register(FMLInitializationEvent.class, GameInitializationEvent.class);
        register(FMLPostInitializationEvent.class, GamePostInitializationEvent.class);
        register(FMLLoadCompleteEvent.class, GameLoadCompleteEvent.class);

        register(FMLServerAboutToStartEvent.class, GameAboutToStartServerEvent.class);
        register(FMLServerStartingEvent.class, GameStartingServerEvent.class);
        register(FMLServerStartedEvent.class, GameStartedServerEvent.class);
        register(FMLServerStoppingEvent.class, GameStoppingServerEvent.class);
        register(FMLServerStoppedEvent.class, GameStoppedServerEvent.class);

        // FML game events
        register(PlayerEvent.PlayerLoggedInEvent.class, PlayerJoinEvent.class);
        register(PlayerEvent.PlayerLoggedOutEvent.class, PlayerQuitEvent.class);
    }

    private static void register(Class<?> otherEvent, Class<?> spongeEvent) {
        eventMap.put(otherEvent, spongeEvent);
    }

    public static Class<?> getImplementingClass(Class<?> apiClass) {
        return eventMap.inverse().get(apiClass);
    }

    public static Class<?> getApiClass(Class<?> implementingClass) {
        return eventMap.get(implementingClass);
    }

}
