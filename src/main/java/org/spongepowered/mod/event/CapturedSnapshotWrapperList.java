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
package org.spongepowered.mod.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Not really an array list, but we need to be because Forge adds
 * {@link World#capturedBlockSnapshots} which is declared as a type
 * {@link ArrayList}. This list is actually intended to serve as a
 * wrapper for mods to be able to peer into Sponge's captured blocks.
 */
public class CapturedSnapshotWrapperList extends ArrayList<BlockSnapshot> implements List<BlockSnapshot> {

    private static final long serialVersionUID = -8973153582753299054L;

    private static SpongeBlockSnapshot toSponge(BlockSnapshot blockSnapshot) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        final SpongeBlockSnapshot sponge = builder
            .worldId(((org.spongepowered.api.world.World) blockSnapshot.getWorld()).getUniqueId())
            .position(VecHelper.toVector3i(blockSnapshot.getPos()))
            .blockState(((BlockState) blockSnapshot.getReplacedBlock()))
            .flag(BlockChangeFlagRegistryModule.fromNativeInt(blockSnapshot.getFlag()))
            .unsafeNbt(blockSnapshot.getNbt())
            .build();
        return sponge;
    }

    private static BlockSnapshot toForge(SpongeBlockSnapshot spongeSnapshot) {
        final UUID worldUniqueId = spongeSnapshot.getWorldUniqueId();
        final org.spongepowered.api.world.World spongeWorld = Sponge.getServer().getWorld(worldUniqueId)
            .orElseThrow(() -> new IllegalStateException("World with uuid: " + worldUniqueId + " not registered for snapshot:" + spongeSnapshot));
        final World mcWorld = (World) spongeWorld;
        final BlockPos blockPos = VecHelper.toBlockPos(spongeSnapshot.getPosition());
        final IBlockState blockState = (IBlockState) spongeSnapshot.getState();
        final NBTTagCompound nbtTagCompound = spongeSnapshot.getCompound().orElse(null);
        return new BlockSnapshot(mcWorld, blockPos, blockState, nbtTagCompound);
    }

    private final World worldPointer;
    private List<SpongeBlockSnapshot> wrappedList = new ArrayList<>();

    @Nullable private List<BlockSnapshot> cachedSnapshots;

    public CapturedSnapshotWrapperList(World world) {
        this.worldPointer = world;
    }

    @SuppressWarnings("unchecked")
    private List<SpongeBlockSnapshot> getUnderlyingList() {
        if (SpongeImplHooks.isMainThread()) {
            final PhaseContext<?> data = PhaseTracker.getInstance().getCurrentContext();
            if (((IPhaseState) data.state).doesBulkBlockCapture(data)) {
                return data.getCapturedBlockSupplier().get();
            }
            return this.wrappedList;
        }
        return this.wrappedList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int size() {
        return getUnderlyingList().size();
    }

    @Override
    public boolean isEmpty() {
        return this.getUnderlyingList().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getCachedForgeList().contains(o);
    }

    private List<BlockSnapshot> getCachedForgeList() {
        populateCachedList();
        return this.cachedSnapshots;
    }

    private void populateCachedList() {
        final List<SpongeBlockSnapshot> underlying = getUnderlyingList();
        if (underlying.isEmpty()) {
            this.cachedSnapshots = new ArrayList<>();
        }
        this.cachedSnapshots = new ArrayList<>(underlying.size());
        for (SpongeBlockSnapshot spongeSnapshot : underlying) {
            final BlockSnapshot forgeSnapshot = toForge(spongeSnapshot);
            this.cachedSnapshots.add(forgeSnapshot);
        }
    }

    @Override
    public Iterator<BlockSnapshot> iterator() {
        return getCachedForgeList().iterator();
    }

    @Override
    public Object[] toArray() {
        return getCachedForgeList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getCachedForgeList().toArray(a);
    }

    @Override
    public boolean add(BlockSnapshot blockSnapshot) {
        final List<SpongeBlockSnapshot> underlyingList = getUnderlyingList();
        final List<BlockSnapshot> cachedForgeList = getCachedForgeList();
        cachedForgeList.add(blockSnapshot);
        final SpongeBlockSnapshot sponge = toSponge(blockSnapshot);
        return underlyingList.add(sponge);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean remove(Object o) {
        if (!(o instanceof BlockSnapshot)) {
            return false;
        }
        final BlockSnapshot forgeSnapshot = (BlockSnapshot) o;
        final SpongeBlockSnapshot spongeSnapshot = toSponge(forgeSnapshot);
        final List<SpongeBlockSnapshot> underlyingList = getUnderlyingList();
        final List<BlockSnapshot> cachedForgeList = getCachedForgeList();
        cachedForgeList.remove(forgeSnapshot);
        final PhaseContext<?> data = PhaseTracker.getInstance().getCurrentContext();
        if (((IPhaseState) data.state).doesBulkBlockCapture(data)) {
            data.getCapturedBlockSupplier().prune(spongeSnapshot);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getCachedForgeList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends BlockSnapshot> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends BlockSnapshot> c) {
        throw new UnsupportedOperationException("Sorry, but injecting snapshots into the wrapped capture list is unsupported by Sponge. If this is an expected operation, please report to SpongeForge's GitHub and explain the reasoning for needing to set at indexes.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getCachedForgeList().retainAll(c);
    }

    @Override
    public void clear() {
        if (this.cachedSnapshots != null) {
            this.cachedSnapshots.clear();
            this.cachedSnapshots = null;
        }
        if (this.wrappedList != null) {
            this.wrappedList.clear();
        }
    }

    @Override
    public BlockSnapshot get(int index) {
        return getCachedForgeList().get(index);
    }

    @Override
    public BlockSnapshot set(int index, BlockSnapshot element) {
        throw new UnsupportedOperationException("Sorry, but injecting snapshots into the wrapped capture list is unsupported by Sponge. If this is an expected operation, please report to SpongeForge's GitHub and explain the reasoning for needing to set at indexes.");
    }

    @Override
    public void add(int index, BlockSnapshot element) {
        final List<SpongeBlockSnapshot> underlyingList = getUnderlyingList();
        final List<BlockSnapshot> forgeList = getCachedForgeList();
        forgeList.add(index, element);
        final SpongeBlockSnapshot sponge = toSponge(element);
        underlyingList.add(index, sponge);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public BlockSnapshot remove(int index) {
        final List<BlockSnapshot> cachedForgeList = getCachedForgeList();
        final BlockSnapshot remove = cachedForgeList.remove(index);
        try {
            final PhaseContext<?> data = PhaseTracker.getInstance().getCurrentContext();
            if (((IPhaseState) data.state).doesBulkBlockCapture(data)) {
                final MultiBlockCaptureSupplier capturedBlockSupplier = data.getCapturedBlockSupplier();
                final SpongeBlockSnapshot snapshot = capturedBlockSupplier.get().get(index);
                if (snapshot != null) {
                    capturedBlockSupplier.prune(snapshot);
                }
            } else {
                this.wrappedList.remove(index);
            }
        } catch (IndexOutOfBoundsException e) {
            // something's fucky....
        }
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        return getCachedForgeList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getCachedForgeList().indexOf(o);
    }

    @Override
    public ListIterator<BlockSnapshot> listIterator() {
        return getCachedForgeList().listIterator();
    }

    @Override
    public ListIterator<BlockSnapshot> listIterator(int index) {
        return getCachedForgeList().listIterator(index);
    }

    @Override
    public List<BlockSnapshot> subList(int fromIndex, int toIndex) {
        return getCachedForgeList().subList(fromIndex, toIndex);
    }
}