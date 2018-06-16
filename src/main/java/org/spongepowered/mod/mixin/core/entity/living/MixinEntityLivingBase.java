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
package org.spongepowered.mod.mixin.core.entity.living;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 1001)
public abstract class MixinEntityLivingBase extends MixinEntity implements Living, IMixinEntityLivingBase {

    @Shadow public abstract void stopActiveHand();

    @Override
    public Optional<List<DamageFunction>> provideArmorModifiers(EntityLivingBase entityLivingBase,
         DamageSource source, double damage) {
        return StaticMixinForgeHelper.createArmorModifiers((EntityLivingBase) (Object) this, source, damage);
    }

    @Override
    public float applyModDamage(EntityLivingBase entityLivingBase, DamageSource source, float damage) {
        return ForgeHooks.onLivingHurt((EntityLivingBase) (Object) this, source, damage);
    }

    @Override
    public void applyArmorDamage(EntityLivingBase entityLivingBase, DamageSource source, DamageEntityEvent entityEvent, DamageModifier modifier) {
        Optional<ArmorProperties> optional = modifier.getCause().getContext().get(StaticMixinForgeHelper.ARMOR_PROPERTY);
        if (optional.isPresent()) {
            StaticMixinForgeHelper.acceptArmorModifier((EntityLivingBase) (Object) this, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public boolean hookModAttack(EntityLivingBase entityLivingBase, DamageSource source, float amount) {
        return net.minecraftforge.common.ForgeHooks.onLivingAttack((EntityLivingBase) (Object) this, source, amount);
    }

    // Stub out all the places where Forge fires LivingEntityUseItemEvent
    // Our UseItemStackEvent is different enough that we completely take over
    // the handling of this event, and explicitly fire the forge event ourselves
    // from SpongeForgeEventFactory
    // However, we only want to do this stubbing on the server
    // If ew're on a client entity, we make sure to restore the original behavior,
    // since Sponge events only fire on the server

    @Redirect(method = "updateActiveHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    public boolean onIsEmpty(ItemStack stack) {
        if (!this.world.isRemote) {
            return true; // This skips Forge's added if-block
        }
        return stack.isEmpty();
    }

    @Redirect(method = "setActiveHand", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseStart(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)I"))
    public int onItemUseStart(EntityLivingBase this$0, ItemStack stack, int duration) {
        if (!this.world.isRemote) {
            return duration;
        }
        return ForgeEventFactory.onItemUseStart(this$0, stack, duration);
    }

    @Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack onItemUseFinish(EntityLivingBase entity, ItemStack item, int duration, ItemStack result) {
        if (!this.world.isRemote) {
            return result;
        }
        return ForgeEventFactory.onItemUseFinish(entity, item, duration, result);
    }

    @Redirect(method = "stopActiveHand", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onUseItemStop(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)Z"))
    public boolean onUseItemStop(EntityLivingBase entity, ItemStack item, int duration) {
        if (!this.world.isRemote) {
            return false;
        }
        return ForgeEventFactory.onUseItemStop(entity, item, duration);
    }


}
