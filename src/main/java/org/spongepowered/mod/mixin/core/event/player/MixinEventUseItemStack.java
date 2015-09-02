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
package org.spongepowered.mod.mixin.core.event.player;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.ItemStackTransaction;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.class, remap = false)
public abstract class MixinEventUseItemStack extends MixinEventPlayer implements UseItemStackEvent {

    private ItemStackSnapshot itemSnapshot;
    @Shadow public net.minecraft.item.ItemStack item;
    @Shadow public int duration;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityPlayer player, net.minecraft.item.ItemStack item, int duration, CallbackInfo ci) {
        // TODO itemSnapshot = ((ItemStack) item).createSnapshot();
    }

    @Override
    public Transform<World> getSourceTransform() {
        return ((Player) this.entityPlayer).getTransform();
    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start.class, remap = false)
    static abstract class Start extends MixinEventUseItemStack implements UseItemStackEvent.SourcePlayer.Start {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick.class, remap = false)
    static abstract class Tick extends MixinEventUseItemStack implements UseItemStackEvent.SourcePlayer.Tick {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop.class, remap = false)
    static abstract class Stop extends MixinEventUseItemStack implements UseItemStackEvent.SourcePlayer.Stop {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish.class, remap = false)
    static abstract class Finish extends MixinEventUseItemStack implements UseItemStackEvent.SourcePlayer.Finish {

    }

    @Override
    public ItemStackTransaction getItemStackInUse() {
        // TODO
        return null;
    }

    @Override
    public void setItemStackInUse(ItemStackSnapshot item) {
        // TODO
    }

}
