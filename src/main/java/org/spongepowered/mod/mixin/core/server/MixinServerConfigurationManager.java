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
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.Teleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NonnullByDefault
@Mixin(value = ServerConfigurationManager.class, priority = 1001)
public abstract class MixinServerConfigurationManager {

    private int preTravelDimension;
    /**
     * @author Simon816
     *
     * Remove call to firePlayerLoggedOut because SpongeCommon's
     * MixinNetHandlerPlayServer.onDisconnectPlayer fires the event already.
     *
     * NOTE: ANY call to playerLoggedOut will need to fire the
     * PlayerLoggedOutEvent manually!
     */
    @Redirect(method = "playerLoggedOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;firePlayerLoggedOut(Lnet/minecraft/entity/player/EntityPlayer;)V",
            remap = false))
    public void onFirePlayerLoggedOutCall(FMLCommonHandler thisCtx, EntityPlayer playerIn) {
        // net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
    }

    // Forge needs to know when a player changes to new a dimension
    // This cannot be mapped to DisplaceEntityEvent.Teleport as this event is called BEFORE transfer.
    @Inject(method = "transferPlayerToDimension", at = @At(value = "HEAD", ordinal = 1), remap = false)
    public void onTransferPlayerToDimensionHead(EntityPlayerMP playerIn, int targetDimensionId, Teleporter teleporter, CallbackInfo ci) {
        this.preTravelDimension = playerIn.dimension;
    }

    @Inject(method = "transferPlayerToDimension", at = @At(value = "RETURN", ordinal = 1), remap = false)
    public void onTransferPlayerToDimensionReturn(EntityPlayerMP playerIn, int targetDimensionId, Teleporter teleporter, CallbackInfo ci) {
        if (this.preTravelDimension != playerIn.dimension) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(playerIn, this.preTravelDimension, playerIn.dimension);
        }
    }
}
