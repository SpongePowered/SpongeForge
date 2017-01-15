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
package org.spongepowered.mod.mixin.core.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinPlayerList;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements IMixinPlayerList {

    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn);
    @Shadow public abstract void syncPlayerInventory(EntityPlayerMP playerIn);

    /**
     * @author Simon816
     * @author dualspiral
     *
     * Remove call to firePlayerLoggedOut under ordinary circumstances because
     * SpongeCommon's MixinNetHandlerPlayServer.onDisconnectHandler fires the
     * event already.
     *
     * There is a special case where this event is reinstated - if Sponge's
     * ClientConnectionEvent#Login event is cancelled - which we can detect
     * because the EntityPlayerMP's NetHandlerPlayServer has not been
     * reinstated at this stage. In that scenario, we fire Forge's event as a
     * matter of compatibility with mods that might have started their setup
     * with players.
     *
     * NOTE: ANY call to playerLoggedOut will need to fire the
     * PlayerLoggedOutEvent manually!
     */
    @Redirect(method = "playerLoggedOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;firePlayerLoggedOut(Lnet/minecraft/entity/player/EntityPlayer;)V",
            remap = false))
    public void onFirePlayerLoggedOutCall(FMLCommonHandler thisCtx, EntityPlayer playerIn) {
        if (playerIn instanceof EntityPlayerMP && ((EntityPlayerMP) playerIn).connection == null) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
        }
    }

}
