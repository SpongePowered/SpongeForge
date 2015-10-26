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
package org.spongepowered.mod.mixin.core.event.inventory;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.mixin.core.event.player.MixinEventPlayer;

@Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.class, remap = false)
public abstract class MixinEventUseItemStack extends MixinEventPlayer implements UseItemStackEvent {

    private int originalDuration;
    private ItemStackSnapshot itemSnapshot;
    private Transaction<ItemStackSnapshot> itemTransaction;
    @Shadow public net.minecraft.item.ItemStack item;
    @Shadow public int duration;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityPlayer player, net.minecraft.item.ItemStack item, int duration, CallbackInfo ci) {
        itemSnapshot = ((ItemStack) item).createSnapshot();
        this.itemTransaction = new Transaction<ItemStackSnapshot>(itemSnapshot, itemSnapshot.copy());
        this.originalDuration = duration;
    }

    @Override
    public int getOriginalRemainingDuration() {
        return this.originalDuration;
    }

    @Override
    public int getRemainingDuration() {
        return this.duration;
    }

    @Override
    public void setRemainingDuration(int duration) {
        this.duration = duration;
    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start.class, remap = false)
    static abstract class Start extends MixinEventUseItemStack implements UseItemStackEvent.Start {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick.class, remap = false)
    static abstract class Tick extends MixinEventUseItemStack implements UseItemStackEvent.Tick {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop.class, remap = false)
    static abstract class Stop extends MixinEventUseItemStack implements UseItemStackEvent.Stop {

    }

    @Mixin(value = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish.class, remap = false)
    static abstract class Finish extends MixinEventUseItemStack implements UseItemStackEvent.Finish {

        private ItemStackSnapshot itemResultSnapshot;
        private Transaction<ItemStackSnapshot> itemResultTransaction;

        @Inject(method = "<init>", at = @At("RETURN"))
        public void onConstructed(EntityPlayer player, net.minecraft.item.ItemStack item, int duration, net.minecraft.item.ItemStack result, CallbackInfo ci) {
            itemResultSnapshot = ((ItemStack) result).createSnapshot();
            this.itemResultTransaction = new Transaction<ItemStackSnapshot>(itemResultSnapshot, itemResultSnapshot.copy());
        }

        @Override
        public Transaction<ItemStackSnapshot> getItemStackResult() {
            return this.itemResultTransaction;
        }

    }

    @Override
    public Transaction<ItemStackSnapshot> getItemStackInUse() {
        return this.itemTransaction;
    }

    @Override
    public void syncDataToSponge(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        super.syncDataToSponge(forgeEvent);

        net.minecraftforge.event.entity.player.PlayerUseItemEvent event = (net.minecraftforge.event.entity.player.PlayerUseItemEvent) forgeEvent;
        this.itemTransaction.setCustom(((ItemStack) event.item).createSnapshot());
    }

    @Override
    public void syncDataToForge(org.spongepowered.api.event.Event spongeEvent) {
        super.syncDataToForge(spongeEvent);

       UseItemStackEvent event = (UseItemStackEvent) spongeEvent;
       this.item = (net.minecraft.item.ItemStack) event.getItemStackInUse().getFinal().createStack();
       this.duration = event.getRemainingDuration();
    }
}
