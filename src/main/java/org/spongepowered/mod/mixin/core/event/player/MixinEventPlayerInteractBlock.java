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
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
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
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

@NonnullByDefault
@Mixin(value = PlayerInteractEvent.class, remap = false)
public abstract class MixinEventPlayerInteractBlock extends MixinEventPlayer implements InteractBlockEvent {

    private BlockSnapshot blockSnapshot;

    @Shadow public Action action;
    @Shadow public net.minecraft.world.World world;
    @Shadow public BlockPos pos;
    @Shadow public EnumFacing face;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityPlayer player, Action action, BlockPos pos, EnumFacing face, net.minecraft.world.World world, CallbackInfo ci) {
        if (player instanceof EntityPlayerMP && !StaticMixinHelper.processingInternalForgeEvent) {
            if (pos != null) { // Forge fires this event on client side and passes a null pos and face
                this.blockSnapshot = ((World) world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            } else {
                this.blockSnapshot = BlockTypes.AIR.getDefaultState().snapshotFor(new Location<>((World) world, Vector3i.ZERO));
            }
        }
    }

    @Override
    public BlockSnapshot getTargetBlock() {
        return this.blockSnapshot;
    }

    @Override
    public Direction getTargetSide() {
        if (this.face != null) {
            return DirectionFacingProvider.getInstance().getKey(this.face).get();
        }
        return Direction.NONE;
    }

    @Override
    public Optional<Vector3d> getInteractionPoint() {
        return Optional.empty();
    }

    @Override
    public void syncDataToForge(Event spongeEvent) {
        super.syncDataToForge(spongeEvent);

        InteractBlockEvent event = (InteractBlockEvent) spongeEvent;
        this.pos = VecHelper.toBlockPos(event.getTargetBlock().getLocation().get().getPosition());
        final Optional<EnumFacing> facing = DirectionFacingProvider.getInstance().get(event.getTargetSide());
        if (!facing.isPresent()) {
            this.face = EnumFacing.DOWN;
        } else {
            this.face = facing.get();
        }
    }

    @Override
    public Event createSpongeEvent() {
        if (this.action == Action.LEFT_CLICK_BLOCK) {
            return SpongeEventFactory.createInteractBlockEventPrimary(getGame(), getCause(), getInteractionPoint(), getTargetBlock(), getTargetSide());
        } else {
            return SpongeEventFactory.createInteractBlockEventSecondary(getGame(), getCause(), getInteractionPoint(), getTargetBlock(), getTargetSide());
        }
    }
}
