/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.mod.mixin.event.player;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.player.PlayerPlaceBlockEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(value = BlockEvent.PlaceEvent.class, remap = false)
public abstract class MixinEventPlayerPlaceBlock extends BlockEvent implements PlayerPlaceBlockEvent {

    @Shadow public EntityPlayer player;
    @Shadow public ItemStack itemInHand;
    @Shadow public net.minecraftforge.common.util.BlockSnapshot blockSnapshot;
    @Shadow public IBlockState placedBlock;
    @Shadow public IBlockState placedAgainst;

    public MixinEventPlayerPlaceBlock(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
    }

    @Override
    public Player getPlayer() {
        return (Player) this.player;
    }

    @Override
    public BlockSnapshot getReplacementBlock() {
        return (BlockSnapshot) this.blockSnapshot;
    }

    @Override
    public Optional<Cause> getCause() {
        return Optional.fromNullable(new Cause(null, (Player) this.player, null));
    }

    @Override
    public boolean isCancelled() {
        return this.isCanceled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.setCanceled(cancelled);
    }
}
