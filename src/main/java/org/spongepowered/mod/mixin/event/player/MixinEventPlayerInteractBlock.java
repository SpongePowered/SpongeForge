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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.wrapper.BlockWrapper;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.player.PlayerInteractEvent.class, remap = false)
public abstract class MixinEventPlayerInteractBlock extends PlayerEvent implements PlayerInteractBlockEvent {

    @Shadow public Action action;
    @Shadow public World world;
    @Shadow public BlockPos pos;

    public MixinEventPlayerInteractBlock(EntityPlayer player, Action action, BlockPos pos, EnumFacing face, World world) {
        super(player);
    }

    @Override
    public BlockLoc getBlock() {
        return (BlockLoc) new BlockWrapper((org.spongepowered.api.world.World) this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public EntityInteractionType getInteractionType() {
        if (this.action == Action.LEFT_CLICK_BLOCK) {
            return EntityInteractionType.LEFT_CLICK;
        } else if (this.action == Action.RIGHT_CLICK_AIR || this.action == Action.RIGHT_CLICK_BLOCK) {
            return EntityInteractionType.RIGHT_CLICK;
        } else {
            return EntityInteractionType.MIDDLE_CLICK;
        }
    }

    @Override
    public Optional<Cause> getCause() {
        return Optional.fromNullable(new Cause(null, (Player) this.entityPlayer, null));
    }

    /*&@Override
    public boolean isCancelled() {
        return this.isCanceled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.setCanceled(cancelled);
    }*/
}
