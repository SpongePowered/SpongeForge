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

import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ForgeEventFactory.class, remap = false)
public abstract class MixinForgeEventFactory {

//    @Overwrite
//    public static PlayerInteractEvent onPlayerInteract(EntityPlayer player, Action action, net.minecraft.world.World world, BlockPos pos,
//            EnumFacing face, Vec3d localPos) {
//        if (world.isRemote) {
//            PlayerInteractEvent event = new PlayerInteractEvent(player, action, pos, face, world, localPos);
//            MinecraftForge.EVENT_BUS.post(event);
//            return event;
//        }
//
//        PlayerInteractEvent forgeEvent = new PlayerInteractEvent(player, action, pos, face, world, localPos);
//        Event spongeEvent = ((IMixinEvent) forgeEvent).createSpongeEvent();
//
//        // Bypass ForgeEventFactory so we maintain the same event reference.
//        if (((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post(spongeEvent, forgeEvent,
//                forgeEvent.getListenerList().getListeners(((IMixinEventBus) MinecraftForge.EVENT_BUS).getBusID()))) {
//            forgeEvent.setCanceled(true);
//        }
//
//        return forgeEvent;
//    }
}
