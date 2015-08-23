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

import org.spongepowered.api.event.block.BlockUpdateNeighborBlockEvent;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinEvent;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = BlockEvent.NeighborNotifyEvent.class, remap = false)
public abstract class MixinEventBlockUpdate extends MixinEventBlock implements BlockUpdateNeighborBlockEvent {

    @Shadow private EnumSet<EnumFacing> notifiedSides;

    @Nullable private List<Location<World>> affectedLocations;

    @Override
    public List<Location<World>> getLocations() {
        if (this.affectedLocations == null) {
            this.affectedLocations = Lists.newArrayList();
            for (EnumFacing notifiedSide : this.notifiedSides) {
                BlockPos offset = this.pos.offset(notifiedSide);
                this.affectedLocations.add(((World) this.world).getLocation(offset.getX(), offset.getY(), offset.getZ()));
            }
        }
        return this.affectedLocations;
    }

    @SuppressWarnings("unused")
    private static NeighborNotifyEvent fromSpongeEvent(BlockUpdateNeighborBlockEvent blockUpdateEvent) {
        final Location<World> location = blockUpdateEvent.getTargetLocation();

        EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
        for (Direction direction : Direction.values()) {
            if ((direction.isCardinal() || direction == Direction.UP || direction == Direction.DOWN)
                    && blockUpdateEvent.getLocations().contains(location.getRelative(direction))) {
                facings.add(SpongeGameRegistry.directionMap.get(direction));
            }
        }

        final NeighborNotifyEvent event = new NeighborNotifyEvent((net.minecraft.world.World) blockUpdateEvent.getTargetLocation().getExtent(),
            VecHelper.toBlockPos(location.getBlockPosition()), (IBlockState) blockUpdateEvent.getTargetLocation().getBlock(), facings);
        ((IMixinEvent) event).setSpongeEvent(blockUpdateEvent);
        return event;
    }
}
