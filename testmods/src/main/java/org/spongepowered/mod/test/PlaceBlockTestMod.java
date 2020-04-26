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
package org.spongepowered.mod.test;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = "placeblock", name = "PlaceBlockTest", acceptableRemoteVersions = "*")
public class PlaceBlockTestMod {
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler {
        @SubscribeEvent
        public void onPlace(final BlockEvent.PlaceEvent event) {
            if(!event.getPlayer().getHeldItem(event.getHand()).isEmpty()) {//This is what we reach in most cases.
                event.getPlayer().sendMessage(new TextComponentString("stack is not empty!"));
                PlaceBlockTestMod.logger.warn("stack is not empty!");
            } else {//This should not happen because it breaks mods that use the itemstack in this event. This is what Sponge currently causes if the itemstack only had one item in it.
                event.getPlayer().sendMessage(new TextComponentString("stack is empty (wrong behavior)!"));
                PlaceBlockTestMod.logger.error("stack is empty (wrong behavior)!");
            }
        }
    }
}