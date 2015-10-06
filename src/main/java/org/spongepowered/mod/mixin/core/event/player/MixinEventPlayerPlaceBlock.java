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
package org.spongepowered.mod.mixin.core.event.player;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinBlockSnapshot;
import org.spongepowered.mod.mixin.core.event.block.MixinEventBlock;

@NonnullByDefault
@Mixin(value = BlockEvent.PlaceEvent.class, remap = false)
public abstract class MixinEventPlayerPlaceBlock extends MixinEventBlock implements ChangeBlockEvent.Place {

    @Shadow public EntityPlayer player;
    @Shadow public ItemStack itemInHand;
    @Shadow public BlockSnapshot blockSnapshot;
    @Shadow public IBlockState placedBlock;
    @Shadow public IBlockState placedAgainst;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(BlockSnapshot blockSnapshot, IBlockState placedAgainst, EntityPlayer player, CallbackInfo ci) {
        this.blockOriginal = ((IMixinBlockSnapshot) blockSnapshot).createSpongeBlockSnapshot();
        this.blockReplacement = ((IMixinBlockSnapshot) BlockSnapshot.getBlockSnapshot(blockSnapshot.world, blockSnapshot.pos)).createSpongeBlockSnapshot();
        this.blockTransactions = new ImmutableList.Builder<BlockTransaction>().add(new BlockTransaction(this.blockOriginal, this.blockReplacement)).build();
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.player);
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);

        ChangeBlockEvent.Place event = (ChangeBlockEvent.Place) spongeEvent;
        this.placedBlock = (IBlockState) event.getTransactions().get(0).getFinalReplacement().getState();
    }
}
