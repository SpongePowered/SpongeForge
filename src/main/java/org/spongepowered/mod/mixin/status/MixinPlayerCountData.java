/*
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
package org.spongepowered.mod.mixin.status;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ServerStatusResponse;
import org.spongepowered.api.event.server.StatusPingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;

@Mixin(ServerStatusResponse.PlayerCountData.class)
public abstract class MixinPlayerCountData implements StatusPingEvent.Response.Players {

    private List<GameProfile> profiles;

    @Shadow
    private int onlinePlayerCount;

    @Shadow
    private int maxPlayers;

    @Override
    public int getOnline() {
        return this.onlinePlayerCount;
    }

    @Override
    public void setOnline(int online) {
        this.onlinePlayerCount = online;
    }

    @Override
    public int getMax() {
        return this.maxPlayers;
    }

    @Override
    public void setMax(int max) {
        this.maxPlayers = max;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<org.spongepowered.api.GameProfile> getProfiles() {
        return (List) this.profiles; // This cast should be always save
    }

    @Overwrite
    public GameProfile[] getPlayers() {
        if (this.profiles == null) {
            this.profiles = Lists.newArrayList();
        }

        // TODO: When serializing, Minecraft calls this method frequently (it doesn't store the result).
        // Maybe we should cache this until the list is modified or patch the serialization?
        return this.profiles.toArray(new GameProfile[this.profiles.size()]);
    }

    @Overwrite
    public void setPlayers(GameProfile[] playersIn) {
        if (this.profiles == null) {
            this.profiles = Lists.newArrayList(playersIn);
        } else {
            this.profiles.clear();
            Collections.addAll(this.profiles, playersIn);
        }
    }
}
