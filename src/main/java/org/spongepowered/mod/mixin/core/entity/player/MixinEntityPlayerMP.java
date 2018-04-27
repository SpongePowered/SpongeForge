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
package org.spongepowered.mod.mixin.core.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.common.mixin.core.entity.player.MixinEntityPlayer;

import javax.annotation.Nullable;

@Mixin(value = EntityPlayerMP.class, priority = 1001)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer {
    @Shadow private NetHandlerPlayServer connection;

    // TODO - investigate whether this is used anywhere.
    public boolean usesCustomClient() {
        return this.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
    }

    /**
     * @author gabizou - April 7th, 2018
     * @reason reroute teleportation logic to common
     */
    @Overwrite(remap = false)
    @Nullable
    public Entity changeDimension(int dimensionId, ITeleporter teleporter) {
        return EntityUtil.teleportPlayerToDimension((EntityPlayerMP) (Object) this, dimensionId, (IMixinITeleporter) (Object) teleporter);
    }
}
