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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTransaction;
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
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.util.VecHelper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@NonnullByDefault
@Mixin(value = BlockEvent.NeighborNotifyEvent.class, remap = false)
public abstract class MixinEventNotifyNeighborBlock extends MixinEventBlock implements NotifyNeighborBlockEvent.SourceBlock {

    private ImmutableMap<Direction, BlockSnapshot> originalRelatives;
    private Map<Direction, Location<World>> relatives;
    @Shadow private EnumSet<EnumFacing> notifiedSides;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, CallbackInfo ci) {
        createSpongeEventData();
    }

    @Override
    public ImmutableMap<Direction, BlockSnapshot> getOriginalRelatives() {
        return this.originalRelatives;
    }

    @Override
    public Map<Direction, Location<World>> getRelatives() {
        return this.relatives;
    }

    @Override
    public BlockSnapshot getSourceBlock() {
        return this.blockOriginal;
    }

    @Override
    public Location<World> getSourceLocation() {
        return this.blockOriginal.getLocation().get();
    }

    @Override
    public void filterDirections(Predicate<Direction> predicate) {
        Iterator<Direction> iterator = this.relatives.keySet().iterator();
        if (!predicate.apply(iterator.next())) {
            iterator.remove();
        }
    }

    @Override
    public void createSpongeEventData() {
        this.relatives = new HashMap<Direction, Location<World>>();
        if (this.notifiedSides != null) {
            for (EnumFacing notifiedSide : this.notifiedSides) {
                BlockPos offset = this.pos.offset(notifiedSide);
                Direction direction = SpongeGameRegistry.directionMap.inverse().get(notifiedSide);
                Location<World> location = new Location<World>((World) this.world, VecHelper.toVector(offset));
                this.relatives.put(direction, location);
            }
        }
        ImmutableMap.Builder<Direction, BlockSnapshot> builder = new ImmutableMap.Builder<Direction, BlockSnapshot>(); 
        for (Map.Entry<Direction, Location<World>> mapEntry : this.relatives.entrySet()) {
            BlockSnapshot blockSnapshot = mapEntry.getValue().createSnapshot();
            builder.put(mapEntry.getKey(), blockSnapshot);
        }
        this.originalRelatives = builder.build();
    }

    @Override
    public ImmutableList<BlockTransaction> getTransactions() {
        if (this.blockTransactions == null) {
            createSpongeEventData();
        }
        return this.blockTransactions;
    }

}
