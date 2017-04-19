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
package org.spongepowered.mod.client;

import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Matthew on 4/18/2017.
 */
public class ClientTabList implements TabList {

    private final NetHandlerPlayClient netHandler;

    private Text header;
    private Text footer;

    public ClientTabList(NetHandlerPlayClient netHandler) {
        this.netHandler = netHandler;
    }

    @Override
    public Optional<Text> getHeader() {
        return Optional.ofNullable(this.header);
    }

    public void setHeader(Text header) {
        this.header = header;
    }

    @Override
    public Optional<Text> getFooter() {
        return Optional.ofNullable(this.footer);
    }

    public void setFooter(Text footer) {
        this.footer = footer;
    }

    @Override
    public Collection<TabListEntry> getEntries() {
        return this.netHandler.getPlayerInfoMap().stream()
                .map(TabListEntry.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<TabListEntry> getEntry(UUID uniqueId) {
        return Optional.ofNullable((TabListEntry) this.netHandler.getPlayerInfo(uniqueId));
    }
}
