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
package org.spongepowered.mod.mixin.core.event.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

@NonnullByDefault
@Mixin(value = BlockEvent.NeighborNotifyEvent.class, remap = false)
public abstract class MixinEventNotifyNeighborBlock extends MixinEventBlock implements NotifyNeighborBlockEvent {

    private ImmutableMap<Direction, BlockState> originalNeighbors;
    private Map<Direction, BlockState> neighbors;
    @Shadow private EnumSet<EnumFacing> notifiedSides;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, CallbackInfo ci) {
        if (!world.isRemote && !StaticMixinHelper.processingInternalForgeEvent) {
            createSpongeEventData();
        }
    }

    @Override
    public ImmutableMap<Direction, BlockState> getOriginalNeighbors() {
        return this.originalNeighbors;
    }

    @Override
    public Map<Direction, BlockState> getNeighbors() {
        return this.neighbors;
    }

    @Override
    public void filterDirections(Predicate<Direction> predicate) {
        Iterator<Direction> iterator = this.neighbors.keySet().iterator();
        if (!predicate.test(iterator.next())) {
            iterator.remove();
        }
    }

    public void createSpongeEventData() {
        this.neighbors = new HashMap<>();
        if (this.notifiedSides != null) {
            for (EnumFacing notifiedSide : this.notifiedSides) {
                BlockPos offset = this.pos.offset(notifiedSide);
                Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
                Location<World> location = new Location<>((World) this.world, VecHelper.toVector(offset));
                if (location.getBlockY() >=0 && location.getBlockY() <= 255) {
                    this.neighbors.put(direction, location.getBlock());
                }
            }
        }
        this.originalNeighbors = ImmutableMap.copyOf(this.neighbors);
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);

        NotifyNeighborBlockEvent event = (NotifyNeighborBlockEvent) spongeEvent;
        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Map.Entry<Direction, BlockState> mapEntry : event.getNeighbors().entrySet()) {
            facings.add(DirectionFacingProvider.getInstance().get(mapEntry.getKey()).get());
        }

        this.notifiedSides = facings;
    }
}
