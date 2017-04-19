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
package org.spongepowered.mod.mixin.core.client.network;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.mod.client.interfaces.IMixinNetworkPlayerInfo;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(NetworkPlayerInfo.class)
public class MixinNetworkPlayerInfo implements TabListEntry, IMixinNetworkPlayerInfo {

    @Shadow @Final private com.mojang.authlib.GameProfile gameProfile;

    @Shadow @Nullable private ITextComponent displayName;
    @Shadow private int responseTime;
    @Shadow private GameType gameType;

    private TabList tabList;

    @Override
    public void setTabList(TabList tabList) {
        this.tabList = tabList;
    }

    @Override
    public TabList getList() {
        return this.tabList;
    }

    @Override
    public GameProfile getProfile() {
        return (GameProfile) this.gameProfile;
    }

    @Override
    public Optional<Text> getDisplayName() {
        if (this.displayName == null) {
            return Optional.empty();
        }
        return Optional.of(SpongeTexts.toText(this.displayName));
    }

    @Override
    public TabListEntry setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName == null ? null : SpongeTexts.toComponent(displayName);
        return this;
    }

    @Override
    public int getLatency() {
        return this.responseTime;
    }

    @Override
    public TabListEntry setLatency(int latency) {
        this.responseTime = latency;
        return this;
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.gameType;
    }

    @Override
    public TabListEntry setGameMode(GameMode gameMode) {
        this.gameType = (GameType) (Object) gameMode;
        return this;
    }
}
