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
package org.spongepowered.mod.mixin.core.fml.common.gameevent;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.api.event.entity.player.PlayerRespawnEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.interfaces.IMixinPlayerRespawnEvent;

@Mixin(PlayerEvent.PlayerRespawnEvent.class)
public abstract class MixinPlayerRespawnEvent extends MixinPlayerEvent implements PlayerRespawnEvent, IMixinPlayerRespawnEvent {

    private Location<World> originalLocation;
    private Location<World> newLocation;
    private boolean isBedSpawn;

    @Override
    public void setIsBedSpawn(boolean isBedSpawn) {
        this.isBedSpawn = isBedSpawn;
    }

    @Override
    public Location<World> getRespawnLocation() {
        return this.originalLocation;
    }

    @Override
    public Location<World> getNewRespawnLocation() {
        return this.newLocation;
    }

    @Override
    public boolean isBedSpawn() {
        return this.isBedSpawn;
    }

    @Override
    public void setNewRespawnLocation(Location<World> respawnLocation) {
        if (this.originalLocation == null) {
            this.originalLocation = respawnLocation;
        }
        this.newLocation = respawnLocation;
    }
}
