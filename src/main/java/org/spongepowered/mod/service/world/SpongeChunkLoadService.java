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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ImmutableList;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.world.ChunkLoadService;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpongeChunkLoadService implements ChunkLoadService {

    @Override
    public void registerCallback(Object plugin, Callback callback) {
        ForgeChunkManager.setForcedChunkLoadingCallback(plugin, new SpongeLoadingCallback(plugin, callback));
    }

    @Override
    public Optional<LoadingTicket> createTicket(Object plugin, World world) {
        Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.absent();
        }

        return Optional.of((LoadingTicket) new SpongeLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<EntityLoadingTicket> createEntityTicket(Object plugin, World world) {
        Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.absent();
        }

        return Optional.of((EntityLoadingTicket) new SpongeEntityLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerLoadingTicket> createPlayerTicket(Object plugin, World world, UUID player) {
        Optional<Player> spongePlayer = Sponge.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.absent();
        }

        Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.absent();
        }

        return Optional.of((PlayerLoadingTicket) new SpongePlayerLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerEntityLoadingTicket> createPlayerEntityTicket(Object plugin, World world, UUID player) {
        Optional<Player> spongePlayer = Sponge.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.absent();
        }

        Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.absent();
        }

        return Optional.of((PlayerEntityLoadingTicket) new SpongePlayerEntityLoadingTicket(forgeTicket));
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
        Optional<Player> spongePlayer = Sponge.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return 0;
        }

        return ForgeChunkManager.ticketCountAvailableFor(spongePlayer.get().getName());
    }

    @Override
    public ImmutableSetMultimap<Vector3i, LoadingTicket> getForcedChunks(World world) {
        ImmutableSetMultimap<ChunkCoordIntPair, Ticket> forgeForcedChunks =
                ForgeChunkManager.getPersistentChunksFor((net.minecraft.world.World) world);
        ImmutableSetMultimap.Builder<Vector3i, LoadingTicket> spongeForcedChunks = ImmutableSetMultimap.builder();
        for (Map.Entry<ChunkCoordIntPair, Ticket> ticketPair : forgeForcedChunks.entries()) {
            spongeForcedChunks.put(new Vector3i(ticketPair.getKey().chunkXPos, 0, ticketPair.getKey().chunkZPos),
                    new SpongeLoadingTicket(ticketPair.getValue()));
        }

        return spongeForcedChunks.build();
    }

    private class SpongeLoadingTicket implements ChunkLoadService.LoadingTicket {

        protected ForgeChunkManager.Ticket forgeTicket;
        private PluginContainer plugin;
        private String pluginId;
        private ImmutableSet<Vector3i> chunkList;

        private SpongeLoadingTicket(Ticket ticket) {
            this.forgeTicket = ticket;
            this.plugin = Sponge.getGame().getPluginManager().getPlugin(ticket.getModId()).get();
            this.pluginId = this.plugin.getId();
        }

        @Override
        public boolean setNumChunks(int numChunks) {
            if (numChunks > getMaxTickets(this.plugin) || (numChunks <= 0 && getMaxTickets(this.plugin) > 0)) {
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
        public String getPlugin() {
            return this.pluginId;
        }

        @Override
        public ImmutableSet<Vector3i> getChunkList() {
            if (this.chunkList != null) {
                return this.chunkList;
            }

            Set<Vector3i> forgeChunkList = new HashSet<Vector3i>();
            for (ChunkCoordIntPair chunkCoord : this.forgeTicket.getChunkList()) {
                forgeChunkList.add(new Vector3i(chunkCoord.chunkXPos, 0, chunkCoord.chunkZPos));
            }

            this.chunkList = new ImmutableSet.Builder<Vector3i>().addAll(forgeChunkList).build();
            return this.chunkList;
        }

        @Override
        public void forceChunk(Vector3i chunk) {
            ForgeChunkManager.forceChunk(this.forgeTicket, VecHelper.toChunkCoordIntPair(chunk));
        }

        @Override
        public void unforceChunk(Vector3i chunk) {
            ForgeChunkManager.unforceChunk(this.forgeTicket, VecHelper.toChunkCoordIntPair(chunk));
        }

        @Override
        public void prioritizeChunk(Vector3i chunk) {
            ForgeChunkManager.reorderChunk(this.forgeTicket, VecHelper.toChunkCoordIntPair(chunk));
        }

        @Override
        public void release() {
            ForgeChunkManager.releaseTicket(this.forgeTicket);
        }

    }

    private class SpongeEntityLoadingTicket extends SpongeLoadingTicket implements EntityLoadingTicket {

        private SpongeEntityLoadingTicket(Ticket ticket) {
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

        private SpongePlayerLoadingTicket(Ticket ticket) {
            super(ticket);
        }

        @Override
        public UUID getPlayerUniqueId() {
            return Sponge.getGame().getServer().getPlayer(this.forgeTicket.getPlayerName()).get().getUniqueId();
        }

    }

    private class SpongePlayerEntityLoadingTicket extends SpongePlayerLoadingTicket implements PlayerEntityLoadingTicket {

        private SpongePlayerEntityLoadingTicket(Ticket ticket) {
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

        protected Callback spongeLoadingCallback;

        public SpongeLoadingCallback(Object plugin, ChunkLoadService.Callback callback) {
            this.spongeLoadingCallback = callback;
        }

        @Override
        public void ticketsLoaded(List<Ticket> tickets, net.minecraft.world.World world) {
            List<LoadingTicket> loadingTickets = new ArrayList<LoadingTicket>();

            for (Ticket ticket : tickets) {
                loadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            this.spongeLoadingCallback.onLoaded(new ImmutableList.Builder<LoadingTicket>().addAll(loadingTickets).build(),
                    (org.spongepowered.api.world.World) world);
        }

    }

    @SuppressWarnings("unused")
    private class SpongeOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.OrderedLoadingCallback {

        public SpongeOrderedCallback(Object plugin, Callback callback) {
            super(plugin, callback);
        }

        @Override
        public List<Ticket> ticketsLoaded(List<Ticket> tickets, net.minecraft.world.World world, int maxTicketCount) {
            List<LoadingTicket> spongeLoadingTickets = new ArrayList<LoadingTicket>();
            for (Ticket ticket : tickets) {
                spongeLoadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            OrderedCallback spongeOrderedCallback = (OrderedCallback) this.spongeLoadingCallback;
            List<LoadingTicket> spongeKeptTickets =
                    spongeOrderedCallback.onLoaded(ImmutableList.copyOf(spongeLoadingTickets), (org.spongepowered.api.world.World) world, maxTicketCount);
            List<Ticket> forgeTickets = new ArrayList<Ticket>();

            for (LoadingTicket ticket : spongeKeptTickets) {
                forgeTickets.add(((SpongeLoadingTicket) ticket).forgeTicket);
            }
            return forgeTickets;
        }

    }

    @SuppressWarnings("unused")
    private class SpongePlayerOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.PlayerOrderedLoadingCallback {

        public SpongePlayerOrderedCallback(Object plugin, Callback callback) {
            super(plugin, callback);
        }

        @Override
        public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, net.minecraft.world.World world) {
            ListMultimap<UUID, LoadingTicket> spongeLoadingTickets = ArrayListMultimap.create();
            for (Map.Entry<String, Ticket> mapEntry : tickets.entries()) {
                Optional<Player> player = Sponge.getGame().getServer().getPlayer(mapEntry.getKey());
                if (player.isPresent()) {
                    spongeLoadingTickets.put(player.get().getUniqueId(), new SpongePlayerLoadingTicket(mapEntry.getValue()));
                }
            }

            ListMultimap<UUID, LoadingTicket> spongeKeptTickets =
                    ((PlayerOrderedCallback) this.spongeLoadingCallback).onPlayerLoaded(ImmutableListMultimap.copyOf(spongeLoadingTickets),
                            (org.spongepowered.api.world.World) world);
            ListMultimap<String, Ticket> forgeTickets = ArrayListMultimap.create();

            for (Map.Entry<UUID, LoadingTicket> mapEntry : spongeKeptTickets.entries()) {
                Optional<Player> player = Sponge.getGame().getServer().getPlayer(mapEntry.getKey());
                if (player.isPresent()) {
                    forgeTickets.put(player.get().getName(), ((SpongeLoadingTicket) mapEntry.getValue()).forgeTicket);
                }
            }

            return forgeTickets;
        }

    }

}
