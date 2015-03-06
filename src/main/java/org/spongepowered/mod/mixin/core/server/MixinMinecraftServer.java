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
package org.spongepowered.mod.mixin.core.server;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeText;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NonnullByDefault
@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = Server.class, prefix = "server$"))
public abstract class MixinMinecraftServer implements Server, ConsoleSource {

    @Shadow
    public abstract ServerConfigurationManager getConfigurationManager();

    @Shadow
    @SideOnly(Side.SERVER)
    public abstract String getServerHostname();

    @Shadow
    @SideOnly(Side.SERVER)
    public abstract int getPort();

    @Shadow
    private int tickCounter;

    @Shadow
    private ServerConfigurationManager serverConfigManager;

    @Shadow
    public abstract void addChatMessage(IChatComponent message);

    @Shadow
    public abstract boolean isServerInOnlineMode();

    @Shadow
    public abstract void initiateShutdown();

    @Override
    public Collection<World> getWorlds() {
        List<World> worlds = new ArrayList<World>();
        for (WorldServer worldServer : DimensionManager.getWorlds()) {
            worlds.add((World) worldServer);
        }
        return worlds;
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        // TODO: This needs to map to world id's somehow
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<World> getWorld(String worldName) {
        for (World world : getWorlds()) {
            if (world.getName().equals(worldName)) {
                return Optional.fromNullable(world);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean unloadWorld(World world) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public World createWorld(String worldName, WorldGenerator generator, long seed) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public World createWorld(String worldName, WorldGenerator generator) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public World createWorld(String worldName) {
        throw new UnsupportedOperationException(); // TODO
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void broadcastMessage(Text message) {
        getConfigurationManager().sendChatMsg(((SpongeText) message).toComponent());
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.fromNullable(new InetSocketAddress(getServerHostname(), getPort()));
    }

    @Override
    public boolean hasWhitelist() {
        return this.serverConfigManager.isWhiteListEnabled();
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        this.serverConfigManager.setWhiteListEnabled(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return isServerInOnlineMode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        return ImmutableList.copyOf((List<Player>) getConfigurationManager().playerEntityList);
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUsername(name));
    }

    @Override
    public Text getMotd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxPlayers() {
        return getConfigurationManager().getMaxPlayers();
    }

    @Override
    public int getRunningTimeTicks() {
        return this.tickCounter;
    }

    @Override
    public void sendMessage(Text... messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
        }
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        // TODO
        return true;
    }

    @Override
    public void shutdown(Text kickMessage) {
        /*for (Player player : getOnlinePlayers()) {
            ((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(kickMessage.toLegacy()); //TODO update with the new Text API
        }*/

        initiateShutdown();
    }
}
