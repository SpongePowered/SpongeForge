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

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.PluginPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldManager;

public final class SpongeForgeEventHooks {


    public static void preEventPhaseCheck(IEventListener listener, Event event) {
        if (!CauseTracker.ENABLED) {
            return;
        }
        if (event instanceof TickEvent.WorldTickEvent) {
            final TickEvent.WorldTickEvent worldTickEvent = (TickEvent.WorldTickEvent) event;
            if (!(worldTickEvent.world instanceof IMixinWorldServer)) {
                return;
            }
            if (worldTickEvent.phase == TickEvent.Phase.START) {
                final CauseTracker causeTracker = ((IMixinWorldServer) worldTickEvent.world).getCauseTracker();
                causeTracker.switchToPhase(PluginPhase.Listener.PRE_SERVER_TICK_LISTENER, PhaseContext.start()
                        .add(NamedCause.source(listener))
                        .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                        .addCaptures()
                        .player()
                        .complete()
                    );
                // Need to prepare all worlds
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    if (worldServer == worldTickEvent.world) {
                        continue;
                    }
                    final CauseTracker otherCauseTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
                    otherCauseTracker.switchToPhase(PluginPhase.Listener.PRE_SERVER_TICK_LISTENER, PhaseContext.start()
                            .add(NamedCause.source(listener))
                            .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                            .addCaptures()
                            .player()
                            .complete()
                    );

                }
            } else if (worldTickEvent.phase == TickEvent.Phase.END) {
                final CauseTracker causeTracker = ((IMixinWorldServer) worldTickEvent.world).getCauseTracker();
                causeTracker.switchToPhase(PluginPhase.Listener.POST_WORLD_TICK_LISTENER, PhaseContext.start()
                        .add(NamedCause.source(listener))
                        .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                        .addCaptures()
                        .player()
                        .complete()
                );
                // Need to prepare all worlds
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    if (worldServer == worldTickEvent.world) {
                        continue;
                    }
                    final CauseTracker otherCauseTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
                    otherCauseTracker.switchToPhase(PluginPhase.Listener.POST_WORLD_TICK_LISTENER, PhaseContext.start()
                            .add(NamedCause.source(listener))
                            .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                            .addCaptures()
                            .player()
                            .complete()
                    );
                }
            }
        }
        // Basically some forge mods also listen to the server tick event and perform world changes as well...........
        if (event instanceof TickEvent.ServerTickEvent) {
            final TickEvent.ServerTickEvent serverTickEvent = (TickEvent.ServerTickEvent) event;
            if (serverTickEvent.phase == TickEvent.Phase.START) {
                // Need to prepare all worlds many mods do this
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    final CauseTracker otherCauseTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
                    otherCauseTracker.switchToPhase(PluginPhase.Listener.PRE_SERVER_TICK_LISTENER, PhaseContext.start()
                            .add(NamedCause.source(listener))
                            .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                            .addCaptures()
                            .player()
                            .complete()
                    );
                }
            } else if (serverTickEvent.phase == TickEvent.Phase.END) {
                // Need to prepare all worlds many mods use this
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    final CauseTracker otherCauseTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
                    otherCauseTracker.switchToPhase(PluginPhase.Listener.POST_SERVER_TICK_LISTENER, PhaseContext.start()
                            .add(NamedCause.source(listener))
                            .add(NamedCause.of(InternalNamedCauses.Tracker.TICK_EVENT, event))
                            .addCaptures()
                            .player()
                            .complete()
                    );
                }
            }
        }
    }

    public static void postEventPhaseCheck(IEventListener listener, Event event) {
        if (!CauseTracker.ENABLED) {
            return;
        }
        if (event instanceof TickEvent.WorldTickEvent) {
            final TickEvent.WorldTickEvent worldTickEvent = (TickEvent.WorldTickEvent) event;
            if (!(worldTickEvent.world instanceof IMixinWorldServer)) {
                return;
            }
            if (worldTickEvent.phase == TickEvent.Phase.START) {
                final CauseTracker causeTracker = ((IMixinWorldServer) worldTickEvent.world).getCauseTracker();
                causeTracker.completePhase();
                // Need to complete all worlds
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    if (worldServer == worldTickEvent.world) {
                        continue;
                    }
                    ((IMixinWorldServer) worldServer).getCauseTracker().completePhase();
                }
            } else if (worldTickEvent.phase == TickEvent.Phase.END) {
                final CauseTracker causeTracker = ((IMixinWorldServer) worldTickEvent.world).getCauseTracker();
                causeTracker.completePhase();
                // Need to complete all worlds
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    if (worldServer == worldTickEvent.world) {
                        continue;
                    }
                    ((IMixinWorldServer) worldServer).getCauseTracker().completePhase();
                }
            }
        }
        // Basically some forge mods also listen to the server tick event and perform world changes as well...........
        if (event instanceof TickEvent.ServerTickEvent) {
            final TickEvent.ServerTickEvent serverTickEvent = (TickEvent.ServerTickEvent) event;
            if (serverTickEvent.phase == TickEvent.Phase.START) {

                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    ((IMixinWorldServer) worldServer).getCauseTracker().completePhase();
                }
            } else if (serverTickEvent.phase == TickEvent.Phase.END) {
                for (WorldServer worldServer : WorldManager.getWorlds()) {
                    ((IMixinWorldServer) worldServer).getCauseTracker().completePhase();
                }
            }
        }
    }
}
