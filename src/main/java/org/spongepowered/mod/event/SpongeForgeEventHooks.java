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

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

public final class SpongeForgeEventHooks {

    @Nullable
    public static PhaseContext<?> preEventPhaseCheck(IEventListener listener, Event event) {
        if (event instanceof TickEvent.WorldTickEvent) {
            final TickEvent.WorldTickEvent worldTickEvent = (TickEvent.WorldTickEvent) event;
            if (!(worldTickEvent.world instanceof IMixinWorldServer)) {
                return null;
            }
            if (worldTickEvent.phase == TickEvent.Phase.START) {
                return PluginPhase.Listener.PRE_WORLD_TICK_LISTENER
                    .createPhaseContext()
                    .source(listener)
                    .event(event);
            } else if (worldTickEvent.phase == TickEvent.Phase.END) {
                return PluginPhase.Listener.POST_WORLD_TICK_LISTENER
                    .createPhaseContext()
                    .source(listener)
                    .event(event);
            }
        }
        // Basically some forge mods also listen to the server tick event and perform world changes as well...........
        if (event instanceof TickEvent.ServerTickEvent) {
            final TickEvent.ServerTickEvent serverTickEvent = (TickEvent.ServerTickEvent) event;
            if (serverTickEvent.phase == TickEvent.Phase.START) {
                // Need to prepare all worlds many mods do this
                return PluginPhase.Listener.PRE_SERVER_TICK_LISTENER.createPhaseContext()
                        .source(listener)
                        .event(event);
            } else if (serverTickEvent.phase == TickEvent.Phase.END) {
                // Need to prepare all worlds many mods do this
                return PluginPhase.Listener.POST_SERVER_TICK_LISTENER.createPhaseContext()
                    .source(listener)
                    .event(event);

            }
        }
        return null;
    }

}
