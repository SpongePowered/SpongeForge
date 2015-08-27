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
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockTypes;
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
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.mixin.core.fml.common.eventhandler.MixinEvent;

import java.util.Iterator;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.world.BlockEvent.class, remap = false)
public abstract class MixinEventBlock extends MixinEvent implements ChangeBlockEvent {

    private BlockSnapshot blockOriginal;
    private BlockSnapshot blockReplacement;
    protected int experience; // Need to do this here until Forge moves experience to Harvest
    protected ImmutableList<BlockTransaction> blockTransactions;

    @Shadow public BlockPos pos;
    @Shadow public net.minecraft.world.World world;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
        this.blockOriginal = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        this.blockReplacement = this.blockOriginal.withState(BlockTypes.AIR.getDefaultState());
    }

    public ImmutableList<BlockTransaction> generateTransactions() {
        this.blockTransactions =
                new ImmutableList.Builder<BlockTransaction>().add(new BlockTransaction(this.blockOriginal, this.blockReplacement)).build();
        return this.blockTransactions;
    }

    @Override
    public ImmutableList<BlockTransaction> getTransactions() {
        if (this.blockTransactions == null) {
            generateTransactions();
        }
        return this.blockTransactions;
    }

    @Override
    public void filter(Predicate<Location<World>> predicate) {
        Iterator<BlockTransaction> iterator = getTransactions().iterator();
        while (iterator.hasNext()) {
            BlockTransaction transaction = iterator.next();
            if (transaction.getFinalReplacement().getLocation().isPresent()
                    && !predicate.apply(transaction.getFinalReplacement().getLocation().get())) {
                transaction.setIsValid(false);
            }
        }
    }

    @SuppressWarnings("unused")
    private static net.minecraftforge.event.world.BlockEvent fromSpongeEvent(ChangeBlockEvent spongeEvent) {
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        net.minecraftforge.event.world.BlockEvent event = new net.minecraftforge.event.world.BlockEvent(world, pos, world.getBlockState(pos));
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }

}
