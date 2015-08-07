package org.spongepowered.mod.mixin.core.event.player;

import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.event.entity.player.PlayerItemConsumeEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish.class)
public abstract class MixinEventPlayerItemConsume extends MixinEventPlayer implements PlayerItemConsumeEvent {

    @Shadow public net.minecraft.item.ItemStack result;

    @Override
    public ItemStack getConsumedItem() {
        return (ItemStack) this.result;
    }

    @Override
    public void setItem(ItemStack item) {
        this.result = (net.minecraft.item.ItemStack) item;
    }

}
