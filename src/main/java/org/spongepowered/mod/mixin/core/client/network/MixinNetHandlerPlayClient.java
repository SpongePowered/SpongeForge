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

import com.google.common.collect.Sets;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.network.ServerConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.mod.client.interfaces.IMixinNetworkPlayerInfo;
import org.spongepowered.mod.interfaces.IMixinNetPlayHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements ServerConnection, TabList, IMixinNetPlayHandler {


    @Shadow @Final private NetworkManager netManager;

    @Shadow public abstract Collection<NetworkPlayerInfo> getPlayerInfoMap();

    @Shadow public abstract NetworkPlayerInfo getPlayerInfo(UUID uniqueId);

    private ITextComponent header;
    private ITextComponent footer;

    private final Set<String> registeredChannels = Sets.newHashSet();

    @Override
    public InetSocketAddress getAddress() {
        return ((IMixinNetworkManager) this.netManager).getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return ((IMixinNetworkManager) this.netManager).getVirtualHost();
    }

    @Override
    public Set<String> getRegisteredChannels() {
        return this.registeredChannels;
    }

    @Override
    public TabList getTabList() {
        return this;
    }

    @Override
    public Optional<Text> getHeader() {
        return Optional.ofNullable(this.header).map(SpongeTexts::toText);
    }

    @Override
    public Optional<Text> getFooter() {
        return Optional.ofNullable(this.footer).map(SpongeTexts::toText);
    }

    @Override
    public Collection<TabListEntry> getEntries() {
        List<TabListEntry> list = new ArrayList<>();
        for (NetworkPlayerInfo npi : getPlayerInfoMap()) {
            list.add((TabListEntry) npi);
        }
        return list;
    }

    @Override
    public Optional<TabListEntry> getEntry(UUID uniqueId) {
        return Optional.ofNullable((TabListEntry) this.getPlayerInfo(uniqueId));
    }

    /**
     * Saves the header and footer for later
     *
     * @param packetIn The packet
     * @param ci The callback
     * @author killjoy1221
     */
    @Inject(method = "handlePlayerListHeaderFooter", at = @At("RETURN"))
    public void onHandleListHeaderFooter(SPacketPlayerListHeaderFooter packetIn, CallbackInfo ci) {
        this.header = packetIn.getHeader();
        this.footer = packetIn.getFooter();
    }

    /**
     * Sets the parent tab list for the tab list entries when they are created.
     *
     * @param packetIn The packet
     * @param ci The callback
     * @author killjoy1221
     */
    @Inject(method = "handlePlayerListItem", at = @At(value = "RETURN"))
    public void onEntryAdded(SPacketPlayerListItem packetIn, CallbackInfo ci) {
        // go through all the player info and set the tab list
        // just in case one was added
        for (NetworkPlayerInfo npi : this.getPlayerInfoMap()) {
            ((IMixinNetworkPlayerInfo) npi).setTabList(this);
        }
    }
}
