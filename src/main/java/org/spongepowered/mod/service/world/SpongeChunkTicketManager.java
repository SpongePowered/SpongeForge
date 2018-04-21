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
package org.spongepowered.mod.service.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.mixin.core.forge.common.MixinForgeChunkManager$Ticket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeChunkTicketManager implements ChunkTicketManager {

    @Override
    public void registerCallback(Object plugin, Callback callback) {
        ForgeChunkManager.setForcedChunkLoadingCallback(plugin, new SpongeLoadingCallback(callback));
    }

    @Override
    public Optional<LoadingTicket> createTicket(Object plugin, World world) {
        Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongeLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<EntityLoadingTicket> createEntityTicket(Object plugin, World world) {
        Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongeEntityLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerLoadingTicket> createPlayerTicket(Object plugin, World world, UUID player) {
        Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.empty();
        }

        Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongePlayerLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerEntityLoadingTicket> createPlayerEntityTicket(Object plugin, World world, UUID player) {
        Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.empty();
        }

        Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongePlayerEntityLoadingTicket(forgeTicket));
    }

    @Override
    public int getMaxTickets(Object plugin) {
        return ForgeChunkManager.getMaxTicketLengthFor(((PluginContainer) plugin).getId());
    }

    @Override
    public int getAvailableTickets(Object plugin, World world) {
        return ForgeChunkManager.ticketCountAvailableFor(plugin, (net.minecraft.world.World) world);
    }

    @Override
    public int getAvailableTickets(UUID player) {
        Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        return spongePlayer.map(player1 -> ForgeChunkManager.ticketCountAvailableFor(player1.getName())).orElse(0);

    }

    @Override
    public ImmutableSetMultimap<Vector3i, LoadingTicket> getForcedChunks(World world) {
        ImmutableSetMultimap<ChunkPos, Ticket> forgeForcedChunks =
                ForgeChunkManager.getPersistentChunksFor((net.minecraft.world.World) world);
        ImmutableSetMultimap.Builder<Vector3i, LoadingTicket> spongeForcedChunks = ImmutableSetMultimap.builder();
        for (Map.Entry<ChunkPos, Ticket> ticketPair : forgeForcedChunks.entries()) {
            spongeForcedChunks.put(new Vector3i(ticketPair.getKey().x, 0, ticketPair.getKey().z),
                    new SpongeLoadingTicket(ticketPair.getValue()));
        }

        return spongeForcedChunks.build();
    }

    private class SpongeLoadingTicket implements ChunkTicketManager.LoadingTicket {

        ForgeChunkManager.Ticket forgeTicket;
        private final PluginContainer plugin;
        private final String pluginId;
        @Nullable private ImmutableSet<Vector3i> chunkList;
        private final World world;

        SpongeLoadingTicket(Ticket ticket) {
            this.forgeTicket = ticket;
            this.plugin = SpongeImpl.getGame().getPluginManager().getPlugin(ticket.getModId()).get();
            this.pluginId = this.plugin.getId();
            this.world = (World) ticket.world;
        }

        @Override
        public boolean setNumChunks(int numChunks) {
            if (numChunks > this.getMaxNumChunks() || (numChunks <= 0 && this.getMaxNumChunks() > 0)) {
                return false;
            }

            this.forgeTicket.setChunkListDepth(numChunks);
            return true;
        }

        @Override
        public int getNumChunks() {
            return this.forgeTicket.getChunkListDepth();
        }

        @Override
        public int getMaxNumChunks() {
            return this.forgeTicket.getMaxChunkListDepth();
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public DataContainer getCompanionData() {
            return NbtTranslator.getInstance()
                    .translate(this.forgeTicket.getModData());
        }

        @SuppressWarnings("MixinClassReference")
        @Override
        public void setCompanionData(DataContainer container) {
            ((MixinForgeChunkManager$Ticket) this.forgeTicket)
                    .setModData(NbtTranslator.getInstance()
                            .translate(container));
        }

        @Override
        public String getPlugin() {
            return this.pluginId;
        }

        @Override
        public ImmutableSet<Vector3i> getChunkList() {
            if (this.chunkList != null) {
                return this.chunkList;
            }

            Set<Vector3i> forgeChunkList = new HashSet<>();
            for (ChunkPos chunkCoord : this.forgeTicket.getChunkList()) {
                forgeChunkList.add(new Vector3i(chunkCoord.x, 0, chunkCoord.z));
            }

            this.chunkList = new ImmutableSet.Builder<Vector3i>().addAll(forgeChunkList).build();
            return this.chunkList;
        }

        @Override
        public void forceChunk(Vector3i chunk) {
            ForgeChunkManager.forceChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void unforceChunk(Vector3i chunk) {
            ForgeChunkManager.unforceChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void prioritizeChunk(Vector3i chunk) {
            ForgeChunkManager.reorderChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void release() {
            ForgeChunkManager.releaseTicket(this.forgeTicket);
        }

    }

    private class SpongeEntityLoadingTicket extends SpongeLoadingTicket implements EntityLoadingTicket {

        SpongeEntityLoadingTicket(Ticket ticket) {
            super(ticket);
        }

        @Override
        public void bindToEntity(Entity entity) {
            this.forgeTicket.bindEntity((net.minecraft.entity.Entity) entity);
        }

        @Override
        public Entity getBoundEntity() {
            return (Entity) this.forgeTicket.getEntity();
        }

    }

    private class SpongePlayerLoadingTicket extends SpongeLoadingTicket implements PlayerLoadingTicket {

        SpongePlayerLoadingTicket(Ticket ticket) {
            super(ticket);
        }

        @Override
        public UUID getPlayerUniqueId() {
            return SpongeImpl.getGame().getServer().getPlayer(this.forgeTicket.getPlayerName()).get().getUniqueId();
        }

    }

    private class SpongePlayerEntityLoadingTicket extends SpongePlayerLoadingTicket implements PlayerEntityLoadingTicket {

        SpongePlayerEntityLoadingTicket(Ticket ticket) {
            super(ticket);
        }

        @Override
        public void bindToEntity(Entity entity) {
            this.forgeTicket.bindEntity((net.minecraft.entity.Entity) entity);
        }

        @Override
        public Entity getBoundEntity() {
            return (Entity) this.forgeTicket.getEntity();
        }

    }

    private class SpongeLoadingCallback implements ForgeChunkManager.LoadingCallback {

        Callback spongeLoadingCallback;

        SpongeLoadingCallback(ChunkTicketManager.Callback callback) {
            this.spongeLoadingCallback = callback;
        }

        @Override
        public void ticketsLoaded(List<Ticket> tickets, net.minecraft.world.World world) {
            List<LoadingTicket> loadingTickets = new ArrayList<>();

            for (Ticket ticket : tickets) {
                loadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            this.spongeLoadingCallback.onLoaded(new ImmutableList.Builder<LoadingTicket>().addAll(loadingTickets).build(),
                    (World) world);
        }

    }

    @SuppressWarnings("unused")
    private class SpongeOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.OrderedLoadingCallback {

        public SpongeOrderedCallback(Object plugin, Callback callback) {
            super(callback);
        }

        @Override
        public List<Ticket> ticketsLoaded(List<Ticket> tickets, net.minecraft.world.World world, int maxTicketCount) {
            List<LoadingTicket> spongeLoadingTickets = new ArrayList<>();
            for (Ticket ticket : tickets) {
                spongeLoadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            OrderedCallback spongeOrderedCallback = (OrderedCallback) this.spongeLoadingCallback;
            List<LoadingTicket> spongeKeptTickets =
                    spongeOrderedCallback.onLoaded(ImmutableList.copyOf(spongeLoadingTickets), (World) world, maxTicketCount);
            List<Ticket> forgeTickets = new ArrayList<>();

            for (LoadingTicket ticket : spongeKeptTickets) {
                forgeTickets.add(((SpongeLoadingTicket) ticket).forgeTicket);
            }
            return forgeTickets;
        }

    }

    @SuppressWarnings("unused")
    private class SpongePlayerOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.PlayerOrderedLoadingCallback {

        public SpongePlayerOrderedCallback(Object plugin, Callback callback) {
            super(callback);
        }

        @Override
        public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, net.minecraft.world.World world) {
            ListMultimap<UUID, LoadingTicket> spongeLoadingTickets = ArrayListMultimap.create();
            for (Map.Entry<String, Ticket> mapEntry : tickets.entries()) {
                Optional<Player> player = SpongeImpl.getGame().getServer().getPlayer(mapEntry.getKey());
                player.ifPresent(player1 -> spongeLoadingTickets.put(player1.getUniqueId(), new SpongePlayerLoadingTicket(mapEntry.getValue())));
            }

            ListMultimap<UUID, LoadingTicket> spongeKeptTickets =
                    ((PlayerOrderedCallback) this.spongeLoadingCallback).onPlayerLoaded(ImmutableListMultimap.copyOf(spongeLoadingTickets),
                            (World) world);
            ListMultimap<String, Ticket> forgeTickets = ArrayListMultimap.create();

            for (Map.Entry<UUID, LoadingTicket> mapEntry : spongeKeptTickets.entries()) {
                Optional<Player> player = SpongeImpl.getGame().getServer().getPlayer(mapEntry.getKey());
                player.ifPresent(player1 -> forgeTickets.put(player1.getName(), ((SpongeLoadingTicket) mapEntry.getValue()).forgeTicket));
            }

            return forgeTickets;
        }

    }

}
