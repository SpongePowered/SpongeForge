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
package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinContainer;

@Mixin(value = NetworkRegistry.class, remap = false)
public abstract class MixinNetworkRegistry {

    @Redirect(method = "getRemoteGuiContainer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/network/IGuiHandler;"
            + "getServerGuiElement(ILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;III)Ljava/lang/Object;"))
    private Object setRemoteContainerPlugin(IGuiHandler factory, int id, EntityPlayer player, World world, int x, int y, int z, ModContainer mc,
            EntityPlayerMP player1, int id1, World world1, int x1, int y1, int z1) {
        Object value = factory.getServerGuiElement(id, player, world, x, y, z);
        if (value instanceof Container) {
            ((IMixinContainer) value).setPlugin((PluginContainer) mc);
        }
        return value;
    }
}
