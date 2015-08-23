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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.source.block.BlockEvent;
import org.spongepowered.api.event.target.block.ChangeBlockEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.mixin.core.fml.common.eventhandler.MixinEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.world.BlockEvent.class, remap = false)
public abstract class MixinEventBlock extends MixinEvent implements ChangeBlockEvent {

    private BlockSnapshot blockSnapshot;
    private BlockState replacementBlock;
    private List<Location<World>> blockLocations;
    private ImmutableList<BlockSnapshot> blockSnapshots;

    @Shadow public BlockPos pos;
    @Shadow public net.minecraft.world.World world;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
        this.blockSnapshot = (BlockSnapshot) net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(world, pos);
        this.blockSnapshots = ImmutableList.of(this.blockSnapshot);
        this.blockLocations = new ArrayList<Location<World>>();
        this.blockLocations.add(new Location<World>((World) world, VecHelper.toVector(pos)));
        this.replacementBlock = BlockTypes.AIR.getDefaultState();
    }

    @Override
    public Location<World> getTargetLocation() {
        return new Location<World>((World) this.world, VecHelper.toVector(this.pos).toDouble());
    }

    @Override
    public BlockSnapshot getSnapshot() {
        return this.blockSnapshot;
    }

    @Override
    public ImmutableList<BlockSnapshot> getSnapshots() {
        return this.blockSnapshots;
    }

    @Override
    public BlockState getReplacementBlock() {
        return BlockTypes.AIR.getDefaultState();
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.blockSnapshot);
    }

    @Override
    public List<Location<World>> getLocations() {
        return this.blockLocations;
    }

    @Override
    public BlockState getOriginalReplacementBlock() {
        return BlockTypes.AIR.getDefaultState();
    }

    @Override
    public void setReplacementBlock(BlockState block) {
        this.replacementBlock = block;
    }

    @Override
    public List<Location<World>> filterBlockLocations(Predicate<Location<World>> predicate) {
        Iterator<Location<World>> iterator = this.getLocations().iterator();
        while (iterator.hasNext()) {
            if (!predicate.apply(iterator.next())) {
                iterator.remove();
            }
        }
        return this.getLocations();
    }

    @SuppressWarnings("unused")
    private static net.minecraftforge.event.world.BlockEvent fromSpongeEvent(BlockEvent spongeEvent) {
        net.minecraft.world.World world = (net.minecraft.world.World) spongeEvent.getLocation().getExtent();

        Location<World> location = spongeEvent.getLocation();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        net.minecraftforge.event.world.BlockEvent event = new net.minecraftforge.event.world.BlockEvent(world, pos, world.getBlockState(pos));
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }

}
