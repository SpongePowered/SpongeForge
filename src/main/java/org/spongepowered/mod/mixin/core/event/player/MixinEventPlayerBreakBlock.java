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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.source.entity.living.player.PlayerBreakBlockEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.interfaces.IMixinEvent;
import org.spongepowered.mod.mixin.core.event.block.MixinEventBlock;

@NonnullByDefault
@Mixin(value = BlockEvent.BreakEvent.class, remap = false)
public abstract class MixinEventPlayerBreakBlock extends MixinEventBlock implements PlayerBreakBlockEvent {

    @Shadow private int exp;
    @Shadow private EntityPlayer player;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, BlockPos pos, IBlockState state, EntityPlayer player, CallbackInfo ci) {
        this.experience = this.exp;
    }

    @Inject(method = "setExpToDrop", at = @At("RETURN"))
    public void onSetExpToDrop(int exp, CallbackInfo ci) {
        this.experience = exp;
    }

    @Override
    public Player getEntity() {
        return (Player) this.player;
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.player);
    }

    @SuppressWarnings("unused")
    private static BlockEvent.BreakEvent fromSpongeEvent(PlayerBreakBlockEvent spongeEvent) {
        Location<World> location = spongeEvent.getTransactions().get(0).getOriginal().getLocation().get();
        net.minecraft.world.World world = (net.minecraft.world.World) location.getExtent();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), (EntityPlayer) spongeEvent.getEntity());
        ((IMixinEvent) event).setSpongeEvent(spongeEvent);
        return event;
    }
}
