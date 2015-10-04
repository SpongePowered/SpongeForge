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
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.mixin.core.fml.common.eventhandler.MixinEvent;

import java.util.Iterator;
import java.util.List;

@NonnullByDefault
@Mixin(value = BlockEvent.class, remap = false)
public abstract class MixinEventBlock extends MixinEvent implements ChangeBlockEvent {

    public BlockSnapshot blockOriginal;
    protected BlockSnapshot blockReplacement;
    protected ImmutableList<BlockTransaction> blockTransactions;

    @Shadow public BlockPos pos;
    @Shadow public net.minecraft.world.World world;
    @Shadow public IBlockState state;

    @Override
    public ImmutableList<BlockTransaction> getTransactions() {
        if (this.blockTransactions == null) {
            this.blockTransactions = new ImmutableList.Builder<BlockTransaction>().build();
        }
        return this.blockTransactions;
    }

    @Override
    public List<BlockTransaction> filter(Predicate<Location<World>> predicate) {
        Iterator<BlockTransaction> iterator = getTransactions().iterator();
        while (iterator.hasNext()) {
            BlockTransaction transaction = iterator.next();
            if (transaction.getFinalReplacement().getLocation().isPresent()
                    && !predicate.apply(transaction.getFinalReplacement().getLocation().get())) {
                transaction.setIsValid(false);
            }
        }
        return this.blockTransactions;
    }

    @Override
    public Cause getCause() {
        if (this.blockOriginal == null) {
            this.blockOriginal = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        }
        return Cause.of(this.blockOriginal);
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);
        ChangeBlockEvent event = (ChangeBlockEvent) spongeEvent;
        if (event.getTransactions() != null && event.getTransactions().size() > 0) {
            this.pos = VecHelper.toBlockPos(event.getTransactions().get(0).getFinalReplacement().getPosition());
            this.state = ((IBlockState) event.getTransactions().get(0).getFinalReplacement().getState());
        }
    }
}
