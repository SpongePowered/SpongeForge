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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
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

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.entity.player.PlayerInteractEvent.class, remap = false)
public abstract class MixinEventPlayerInteractBlock extends MixinEventPlayer implements InteractBlockEvent {

    private BlockSnapshot blockSnapshot;

    @Shadow public Action action;
    @Shadow public net.minecraft.world.World world;
    @Shadow public BlockPos pos;
    @Shadow public EnumFacing face;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityPlayer player, Action action, BlockPos pos, EnumFacing face, net.minecraft.world.World world, CallbackInfo ci) {
        if (pos != null) { // Forge fires this event on client side and passes a null pos and face
            this.blockSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        } else {
            this.blockSnapshot = BlockTypes.AIR.getDefaultState().snapshotFor(new Location<World>((World) world, Vector3i.ZERO));
        }
    }

    @Override
    public BlockSnapshot getTargetBlock() {
        return this.blockSnapshot;
    }

    @Override
    public Direction getTargetSide() {
        if (this.face != null) {
            return SpongeGameRegistry.directionMap.inverse().get(this.face);
        }
        return Direction.NONE;
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.entityPlayer);
    }

    @Override
    public void syncDataToForge() {
        super.syncDataToForge();
        this.pos = VecHelper.toBlockPos(getTargetBlock().getLocation().get().getPosition());
        this.face = SpongeGameRegistry.directionMap.get(getTargetSide());
        this.action = Action.RIGHT_CLICK_BLOCK;
        if (this.entityPlayer.isUsingItem()) {
            this.action = Action.LEFT_CLICK_BLOCK;
        } else if (this.entityPlayer.worldObj.isAirBlock(pos)) {
            this.action = Action.RIGHT_CLICK_AIR;
        }
    }
}
