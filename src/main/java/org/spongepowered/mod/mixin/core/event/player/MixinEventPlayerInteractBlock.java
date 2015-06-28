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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.interfaces.IMixinEventPlayerInteractBlock;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.player.PlayerInteractEvent.class, remap = false)
public abstract class MixinEventPlayerInteractBlock extends PlayerEvent implements PlayerInteractBlockEvent, IMixinEventPlayerInteractBlock {

    @Shadow
    public Action action;

    @Shadow
    public World world;

    @Shadow
    public BlockPos pos;

    @Shadow public EnumFacing face;

    public Optional<Vector3d> clickedPosition = Optional.absent();

    public MixinEventPlayerInteractBlock(EntityPlayer player, Action action, BlockPos pos, EnumFacing face, World world) {
        super(player);
    }

    @Override
    public Location getBlock() {
        return new Location((org.spongepowered.api.world.World) this.world, VecHelper.toVector(this.pos).toDouble());
    }

    @Override
    public EntityInteractionType getInteractionType() {
        if (this.action == Action.LEFT_CLICK_BLOCK) {
            return EntityInteractionTypes.ATTACK;
        } else if (this.action == Action.RIGHT_CLICK_AIR || this.action == Action.RIGHT_CLICK_BLOCK) {
            return EntityInteractionTypes.USE;
        } else {
            return EntityInteractionTypes.PICK_BLOCK;
        }
    }

    @Override
    public Direction getSide() {
        if (this.face != null) {
            return ((SpongeGameRegistry) Sponge.getGame().getRegistry()).directionMap.inverse().get(this.face);
        }
        return Direction.NONE;
    }

    @Override
    public Optional<Cause> getCause() {
        return Optional.fromNullable(new Cause(null, this.entityPlayer, null));
    }

    @Override
    public void setClickedPosition(Vector3d clickedPosition) {
        this.clickedPosition = Optional.of(VecHelper.toVector(this.pos).toDouble().add(clickedPosition));
    }

    @Override
    public Optional<Vector3d> getClickedPosition() {
        return clickedPosition;
    }
}
