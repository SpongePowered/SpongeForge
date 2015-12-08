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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.mixin.core.fml.common.eventhandler.MixinEvent;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@NonnullByDefault
@Mixin(value = BlockEvent.class, remap = false)
public abstract class MixinEventBlock extends MixinEvent implements ChangeBlockEvent {

    public BlockSnapshot blockOriginal;
    protected BlockSnapshot blockReplacement;
    protected ImmutableList<Transaction<BlockSnapshot>> blockTransactions;

    @Shadow public BlockPos pos;
    @Shadow public net.minecraft.world.World world;
    @Shadow public IBlockState state;

    @Override
    public ImmutableList<Transaction<BlockSnapshot>> getTransactions() {
        if (this.blockTransactions == null) {
            this.blockTransactions = new ImmutableList.Builder<Transaction<BlockSnapshot>>().build();
        }
        return this.blockTransactions;
    }

    @Override
    public List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
        Iterator<Transaction<BlockSnapshot>> iterator = getTransactions().iterator();
        while (iterator.hasNext()) {
            Transaction<BlockSnapshot> transaction = iterator.next();
            if (transaction.getFinal().getLocation().isPresent()
                    && !predicate.test(transaction.getFinal().getLocation().get())) {
                transaction.setValid(false);
            }
        }
        return this.blockTransactions;
    }

    @Override
    public World getTargetWorld() {
        return (World) this.world;
    }

    @Override
    public Cause getCause() {
        if (this.blockOriginal == null) {
            this.blockOriginal = ((World) this.world).createSnapshot(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
        return Cause.of(NamedCause.source(this.blockOriginal));
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);
        if (spongeEvent instanceof ChangeBlockEvent) {
            ChangeBlockEvent event = (ChangeBlockEvent) spongeEvent;
            if (event.getTransactions() != null && event.getTransactions().size() > 0) {
                this.pos = VecHelper.toBlockPos(event.getTransactions().get(0).getFinal().getPosition());
                this.state = ((IBlockState) event.getTransactions().get(0).getFinal().getState());
            }
        }
    }
}
