package org.spongepowered.mixin.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@NonnullByDefault
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server {

    @Shadow 
    public abstract ServerConfigurationManager getConfigurationManager();

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
    public void broadcastMessage(Message<?> message) {
        // TODO: Revisit this when text API is actually implemented.
        getConfigurationManager().sendChatMsg(new ChatComponentText((String) message.getContent()));
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        return ImmutableList.copyOf((List<Player>)getConfigurationManager().playerEntityList);
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.fromNullable((Player) getConfigurationManager().func_177451_a(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUsername(name));
    }

    @Override
    public Message.Text getMOTD() {
        throw new UnsupportedOperationException();
    }
}
