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
package org.spongepowered.mod.mixin.entityactivation;

import com.google.common.collect.ImmutableSetMultimap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.plugin.entityactivation.EntityActivationRange;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.ActivationCapability;

@NonnullByDefault
@Mixin(value = net.minecraft.world.World.class, priority = 999)
public abstract class WorldMixin_ForgeActivation {


    @Shadow public abstract void updateEntity(Entity ent);

    @Shadow public abstract void tick();

    @Inject(method = "updateEntityWithOptionalForce", at = @At("HEAD"), cancellable = true)
    private void forgeActivationImpl$checkIfCanUpdate(final Entity ticking, final boolean forceUpdate, final CallbackInfo ci) {
        // Sponge start - area is handled in ActivationRange
        //int i = MathHelper.floor_double(entityIn.posX);
        //int j = MathHelper.floor_double(entityIn.posZ);
        //boolean isForced = getPersistentChunks().containsKey(new net.minecraft.util.math.ChunkPos(i >> 4, j >> 4));
        //int k = isForced ? 0 : 32;
        //boolean canUpdate = !forceUpdate || this.isAreaLoaded(i - k, 0, j - k, i + k, 0, j + k, true);
        if (EntityActivationRange.checkIfActive(ticking)) {
            return;
        }
        // Allow forge mods to force an update
        if (net.minecraftforge.event.ForgeEventFactory.canEntityUpdate(ticking)) {
            return;
        }

        ticking.ticksExisted++;
        ((ActivationCapability) ticking).activation$inactiveTick();
        ci.cancel();
        // Sponge end
    }


    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;getPersistentChunks()Lcom/google/common/collect/ImmutableSetMultimap;", remap = false))
    private ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> forgeActivationImpl$returnEmptyMap(final World world) {
        return ImmutableSetMultimap.of();
    }

    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(IIIIIIZ)Z"))
    private boolean forgeActivationImpl$falseOutOfTheIf(final World world, final int xStart, final int yStart, final int zStart, final int xEnd,
        final int yEnd, final int zEnd, final boolean allowEmpty) {
        return true;
    }


}
