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
package org.spongepowered.mod.mixin.core.block;

import net.minecraft.block.BlockRailBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Optional;

@Mixin(value = BlockRailBase.class, remap = false)
public class MixinBlockRailBase {

    // Used to transfer tracking information from minecarts to block positions
    @Inject(method = "onMinecartPass", at = @At(value = "HEAD"))
    public void onMinecartRailPass(World world, net.minecraft.entity.item.EntityMinecart cart, BlockPos pos, CallbackInfo ci) {
        IMixinEntity spongeEntity = (IMixinEntity) cart;
        Optional<User> notifier = spongeEntity.getNotifierUser();
        Optional<User> owner = spongeEntity.getCreatorUser();
        if (owner.isPresent() || notifier.isPresent()) {
            IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
            if (notifier.isPresent()) {
                spongeChunk.addTrackedBlockPosition(world.getBlockState(pos).getBlock(), pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
            } else {
                spongeChunk.addTrackedBlockPosition(world.getBlockState(pos).getBlock(), pos, owner.get(), PlayerTracker.Type.NOTIFIER);
            }
        }
    }
}
