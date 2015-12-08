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
package org.spongepowered.mod.mixin.core.forge.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.event.SpongeForgeEventFactory;

import java.util.Optional;

@Mixin(value = ForgeEventFactory.class, remap = false)
public abstract class MixinForgeEventFactory {

    @Overwrite
    public static PlayerInteractEvent onPlayerInteract(EntityPlayer player, Action action, net.minecraft.world.World world, BlockPos pos, EnumFacing face) {
        if (world.isRemote) {
            PlayerInteractEvent event = new PlayerInteractEvent(player, action, pos, face, world);
            MinecraftForge.EVENT_BUS.post(event);
            return event;
        }

        InteractBlockEvent event = null;
        if (action == Action.LEFT_CLICK_BLOCK) {
            event = SpongeEventFactory.createInteractBlockEventPrimary(SpongeImpl.getGame(), Cause.of(NamedCause.source(player)), Optional.empty(),
                ((World) world).createSnapshot(VecHelper.toVector(pos)), face == null ? Direction.NONE
                : DirectionFacingProvider.getInstance().getKey(face).get());
        } else {
            event = SpongeEventFactory.createInteractBlockEventSecondary(SpongeImpl.getGame(), Cause.of(NamedCause.source(player)), Optional.empty(),
                ((World) world).createSnapshot(VecHelper.toVector(pos)), face == null ? Direction.NONE
                : DirectionFacingProvider.getInstance().getKey(face).get());
        }

        SpongeImpl.postEvent(event);

        return (PlayerInteractEvent) SpongeForgeEventFactory.lastForgeEvent;
    }
}
