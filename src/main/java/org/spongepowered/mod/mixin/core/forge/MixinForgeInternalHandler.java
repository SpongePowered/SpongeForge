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
package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.mod.event.SpongeToForgeEventFactory;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

@Mixin(ForgeInternalHandler.class)
public abstract class MixinForgeInternalHandler {

    /**
     * @author gabizou - May 8th, 2018
     * @reason Sponge handles this in {@link SpongeToForgeEventFactory#handleCustomStack(SpawnEntityEvent)} for
     * all related item drops. No need to actually listen to an event to have this handled correctly.
     *
     * @param event The event
     */
    @Overwrite(remap = false)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // Sponge Start
        /* - This is already handled in SpongeForgeEventFactory.
        if (!event.getWorld().isRemote)
        {
            ForgeChunkManager.loadEntity(event.getEntity());
        }

        Entity entity = event.getEntity();
        if (entity.getClass().equals(EntityItem.class))
        {
            ItemStack stack = ((EntityItem)entity).getItem();
            Item item = stack.getItem();
            if (item.hasCustomEntity(stack))
            {
                Entity newEntity = item.createEntity(event.getWorld(), entity, stack);
                if (newEntity != null)
                {
                    entity.setDead();
                    event.setCanceled(true);
                    event.getWorld().spawnEntity(newEntity);
                }
            }
        }
        */
        // Sponge End
    }
}
