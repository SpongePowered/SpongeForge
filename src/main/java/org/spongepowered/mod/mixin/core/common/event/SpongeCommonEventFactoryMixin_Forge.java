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
package org.spongepowered.mod.mixin.core.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.Optional;

@Mixin(value = SpongeCommonEventFactory.class, remap = false)
public abstract class SpongeCommonEventFactoryMixin_Forge {

    @Inject(method = "callDestructEntityEventDeath", at = @At("HEAD"), cancellable = true)
    private static void onCallDestructEntityEventDeath(
        final EntityLivingBase entity, final DamageSource source, final boolean isMainThread, final CallbackInfoReturnable<Optional<DestructEntityEvent.Death>> cir) {
        if (net.minecraftforge.common.ForgeHooks.onLivingDeath(entity, source)) {
            cir.setReturnValue(Optional.empty());
        }
    }

    /**
     * @author Aaron1011 - June 7th, 2018
     * @reason This method is a stub in SpongeCommon, so there's no need to
     * inject into it.
     */
    @Overwrite
    public static void callPostPlayerRespawnEvent(final EntityPlayerMP playerMP, final boolean conqueredEnd) {
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(playerMP, conqueredEnd);
    }

}
