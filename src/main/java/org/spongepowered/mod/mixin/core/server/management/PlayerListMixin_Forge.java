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
package org.spongepowered.mod.mixin.core.server.management;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.NetworkUtil;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(PlayerList.class)
public abstract class PlayerListMixin_Forge {

    @Shadow @Final private MinecraftServer server;

    /**
     * @author Simon816
     * @author dualspiral
     *
     * Remove call to firePlayerLoggedOut under ordinary circumstances because
     * SpongeCommon's NetHandlerPlayServerMixin.onDisconnectHandler fires the
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
    private void forge$ValidateFirePlayerLoggedOutWithValidConnection(FMLCommonHandler thisCtx, EntityPlayer playerIn) {
        if (playerIn instanceof EntityPlayerMP && ((EntityPlayerMP) playerIn).connection == null) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
        }
    }

    /**
     * @author gabizou - June 18th, 2019 - 1.12.2
     * @reason Use the common hook to initialize the connection to the player
     * with the changed signature. Vanilla doesn't have the handler added on
     * by default.
     */
    @Overwrite(remap = false)
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        NetworkUtil.initializeConnectionToPlayer((PlayerList) (Object) this, netManager, playerIn, handler);
    }

    /**
     * @author Zidane - June 2019 - 1.12.2
     * @reason Re-route to the common hook
     */
    @Overwrite(remap = false)
    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, ITeleporter teleporter) {
        EntityUtil.transferEntityToWorld(entityIn, null, toWorldIn, (ForgeITeleporterBridge) teleporter, false);
    }

    /**
     * @author Zidane - June 2019 - 1.12.2
     * @reason Re-route to the common hook
     */
    @Overwrite(remap = false)
    public void transferPlayerToDimension(EntityPlayerMP player, int dimensionIn, ITeleporter teleporter) {
        EntityUtil.transferPlayerToWorld(player, null, this.server.getWorld(dimensionIn), (ForgeITeleporterBridge) teleporter);
    }
}
