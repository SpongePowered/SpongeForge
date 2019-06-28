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
import org.spongepowered.mod.mixin.core.forge.common.ForgeChunkManager$TicketAccessor;

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
    public void registerCallback(final Object plugin, final Callback callback) {
        ForgeChunkManager.setForcedChunkLoadingCallback(plugin, new SpongeLoadingCallback(callback));
    }

    @Override
    public Optional<LoadingTicket> createTicket(final Object plugin, final World world) {
        final Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongeLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<EntityLoadingTicket> createEntityTicket(final Object plugin, final World world) {
        final Ticket forgeTicket = ForgeChunkManager.requestTicket(plugin, (net.minecraft.world.World) world, ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongeEntityLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerLoadingTicket> createPlayerTicket(final Object plugin, final World world, final UUID player) {
        final Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.empty();
        }

        final Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.NORMAL);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongePlayerLoadingTicket(forgeTicket));
    }

    @Override
    public Optional<PlayerEntityLoadingTicket> createPlayerEntityTicket(final Object plugin, final World world, final UUID player) {
        final Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        if (!spongePlayer.isPresent()) {
            return Optional.empty();
        }

        final Ticket forgeTicket =
                ForgeChunkManager.requestPlayerTicket(plugin, spongePlayer.get().getName(), (net.minecraft.world.World) world,
                        ForgeChunkManager.Type.ENTITY);
        if (forgeTicket == null) {
            return Optional.empty();
        }

        return Optional.of(new SpongePlayerEntityLoadingTicket(forgeTicket));
    }

    @Override
    public int getMaxTickets(final Object plugin) {
        return ForgeChunkManager.getMaxTicketLengthFor(((PluginContainer) plugin).getId());
    }

    @Override
    public int getAvailableTickets(final Object plugin, final World world) {
        return ForgeChunkManager.ticketCountAvailableFor(plugin, (net.minecraft.world.World) world);
    }

    @Override
    public int getAvailableTickets(final UUID player) {
        final Optional<Player> spongePlayer = SpongeImpl.getGame().getServer().getPlayer(player);
        return spongePlayer.map(player1 -> ForgeChunkManager.ticketCountAvailableFor(player1.getName())).orElse(0);

    }

    @Override
    public ImmutableSetMultimap<Vector3i, LoadingTicket> getForcedChunks(final World world) {
        final ImmutableSetMultimap<ChunkPos, Ticket> forgeForcedChunks =
                ForgeChunkManager.getPersistentChunksFor((net.minecraft.world.World) world);
        final ImmutableSetMultimap.Builder<Vector3i, LoadingTicket> spongeForcedChunks = ImmutableSetMultimap.builder();
        for (final Map.Entry<ChunkPos, Ticket> ticketPair : forgeForcedChunks.entries()) {
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

        SpongeLoadingTicket(final Ticket ticket) {
            this.forgeTicket = ticket;
            this.plugin = SpongeImpl.getGame().getPluginManager().getPlugin(ticket.getModId()).get();
            this.pluginId = this.plugin.getId();
            this.world = (World) ticket.world;
        }

        @Override
        public boolean setNumChunks(final int numChunks) {
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
        public void setCompanionData(final DataContainer container) {
            ((ForgeChunkManager$TicketAccessor) this.forgeTicket)
                    .forgeAccessor$setModData(NbtTranslator.getInstance()
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

            final Set<Vector3i> forgeChunkList = new HashSet<>();
            for (final ChunkPos chunkCoord : this.forgeTicket.getChunkList()) {
                forgeChunkList.add(new Vector3i(chunkCoord.x, 0, chunkCoord.z));
            }

            this.chunkList = new ImmutableSet.Builder<Vector3i>().addAll(forgeChunkList).build();
            return this.chunkList;
        }

        @Override
        public void forceChunk(final Vector3i chunk) {
            ForgeChunkManager.forceChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void unforceChunk(final Vector3i chunk) {
            ForgeChunkManager.unforceChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void prioritizeChunk(final Vector3i chunk) {
            ForgeChunkManager.reorderChunk(this.forgeTicket, VecHelper.toChunkPos(chunk));
        }

        @Override
        public void release() {
            ForgeChunkManager.releaseTicket(this.forgeTicket);
        }

    }

    private class SpongeEntityLoadingTicket extends SpongeLoadingTicket implements EntityLoadingTicket {

        SpongeEntityLoadingTicket(final Ticket ticket) {
            super(ticket);
        }

        @Override
        public void bindToEntity(final Entity entity) {
            this.forgeTicket.bindEntity((net.minecraft.entity.Entity) entity);
        }

        @Override
        public Entity getBoundEntity() {
            return (Entity) this.forgeTicket.getEntity();
        }

    }

    private class SpongePlayerLoadingTicket extends SpongeLoadingTicket implements PlayerLoadingTicket {

        SpongePlayerLoadingTicket(final Ticket ticket) {
            super(ticket);
        }

        @Override
        public UUID getPlayerUniqueId() {
            return SpongeImpl.getGame().getServer().getPlayer(this.forgeTicket.getPlayerName()).get().getUniqueId();
        }

    }

    private class SpongePlayerEntityLoadingTicket extends SpongePlayerLoadingTicket implements PlayerEntityLoadingTicket {

        SpongePlayerEntityLoadingTicket(final Ticket ticket) {
            super(ticket);
        }

        @Override
        public void bindToEntity(final Entity entity) {
            this.forgeTicket.bindEntity((net.minecraft.entity.Entity) entity);
        }

        @Override
        public Entity getBoundEntity() {
            return (Entity) this.forgeTicket.getEntity();
        }

    }

    private class SpongeLoadingCallback implements ForgeChunkManager.LoadingCallback {

        Callback spongeLoadingCallback;

        SpongeLoadingCallback(final ChunkTicketManager.Callback callback) {
            this.spongeLoadingCallback = callback;
        }

        @Override
        public void ticketsLoaded(final List<Ticket> tickets, final net.minecraft.world.World world) {
            final List<LoadingTicket> loadingTickets = new ArrayList<>();

            for (final Ticket ticket : tickets) {
                loadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            this.spongeLoadingCallback.onLoaded(new ImmutableList.Builder<LoadingTicket>().addAll(loadingTickets).build(),
                    (World) world);
        }

    }

    @SuppressWarnings("unused")
    private class SpongeOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.OrderedLoadingCallback {

        public SpongeOrderedCallback(final Object plugin, final Callback callback) {
            super(callback);
        }

        @Override
        public List<Ticket> ticketsLoaded(final List<Ticket> tickets, final net.minecraft.world.World world, final int maxTicketCount) {
            final List<LoadingTicket> spongeLoadingTickets = new ArrayList<>();
            for (final Ticket ticket : tickets) {
                spongeLoadingTickets.add(new SpongeLoadingTicket(ticket));
            }

            final OrderedCallback spongeOrderedCallback = (OrderedCallback) this.spongeLoadingCallback;
            final List<LoadingTicket> spongeKeptTickets =
                    spongeOrderedCallback.onLoaded(ImmutableList.copyOf(spongeLoadingTickets), (World) world, maxTicketCount);
            final List<Ticket> forgeTickets = new ArrayList<>();

            for (final LoadingTicket ticket : spongeKeptTickets) {
                forgeTickets.add(((SpongeLoadingTicket) ticket).forgeTicket);
            }
            return forgeTickets;
        }

    }

    @SuppressWarnings("unused")
    private class SpongePlayerOrderedCallback extends SpongeLoadingCallback implements ForgeChunkManager.PlayerOrderedLoadingCallback {

        public SpongePlayerOrderedCallback(final Object plugin, final Callback callback) {
            super(callback);
        }

        @Override
        public ListMultimap<String, Ticket> playerTicketsLoaded(final ListMultimap<String, Ticket> tickets, final net.minecraft.world.World world) {
            final ListMultimap<UUID, LoadingTicket> spongeLoadingTickets = ArrayListMultimap.create();
            for (final Map.Entry<String, Ticket> mapEntry : tickets.entries()) {
                final Optional<Player> player = SpongeImpl.getGame().getServer().getPlayer(mapEntry.getKey());
                player.ifPresent(player1 -> spongeLoadingTickets.put(player1.getUniqueId(), new SpongePlayerLoadingTicket(mapEntry.getValue())));
            }

            final ListMultimap<UUID, LoadingTicket> spongeKeptTickets =
                    ((PlayerOrderedCallback) this.spongeLoadingCallback).onPlayerLoaded(ImmutableListMultimap.copyOf(spongeLoadingTickets),
                            (World) world);
            final ListMultimap<String, Ticket> forgeTickets = ArrayListMultimap.create();

            for (final Map.Entry<UUID, LoadingTicket> mapEntry : spongeKeptTickets.entries()) {
                final Optional<Player> player = SpongeImpl.getGame().getServer().getPlayer(mapEntry.getKey());
                player.ifPresent(player1 -> forgeTickets.put(player1.getName(), ((SpongeLoadingTicket) mapEntry.getValue()).forgeTicket));
            }

            return forgeTickets;
        }

    }

}
