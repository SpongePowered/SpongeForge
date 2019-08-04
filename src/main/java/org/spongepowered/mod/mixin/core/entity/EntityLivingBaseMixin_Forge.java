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
package org.spongepowered.mod.mixin.core.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.LivingEntityBaseBridge;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 1001)
public abstract class EntityLivingBaseMixin_Forge extends EntityMixin_Forge implements LivingEntityBaseBridge {

    @Override
    public Optional<List<DamageFunction>> bridge$provideArmorModifiers(final EntityLivingBase entityLivingBase,
         final DamageSource source, final double damage) {
        return StaticMixinForgeHelper.createArmorModifiers((EntityLivingBase) (Object) this, source, damage);
    }

    @Override
    public float bridge$applyModDamage(final EntityLivingBase entityLivingBase, final DamageSource source, final float damage) {
        return ForgeHooks.onLivingHurt((EntityLivingBase) (Object) this, source, damage);
    }

    @Override
    public void bridge$applyArmorDamage(
        final EntityLivingBase entityLivingBase, final DamageSource source, final DamageEntityEvent entityEvent, final DamageModifier modifier) {
        final Optional<ArmorProperties> optional = modifier.getCause().getContext().get(StaticMixinForgeHelper.ARMOR_PROPERTY);
        if (optional.isPresent()) {
            StaticMixinForgeHelper.acceptArmorModifier((EntityLivingBase) (Object) this, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public float bridge$applyModDamagePost(final EntityLivingBase entityLivingBase, final DamageSource source, final float damage) {
        return ForgeHooks.onLivingDamage((EntityLivingBase) (Object) this, source, damage);
    }

    @Override
    public boolean bridge$hookModAttack(final EntityLivingBase entityLivingBase, final DamageSource source, final float amount) {
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
    private boolean forge$SkipForgeCheckOnServer(final ItemStack stack) {
        if (!this.world.isRemote) {
            return true; // This skips Forge's added if-block
        }
        return stack.isEmpty();
    }

    @Redirect(method = "setActiveHand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseStart(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)I",
            remap = false
        ))
    private int forge$OnlyThrowForgeEventOnClient(final EntityLivingBase this$0, final ItemStack stack, final int duration) {
        if (!this.world.isRemote) {
            return duration;
        }
        return ForgeEventFactory.onItemUseStart(this$0, stack, duration);
    }

    @Redirect(method = "onItemUseFinish",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            remap = false
        ))
    private ItemStack forge$OnlyThrowForgeEventOnClient(final EntityLivingBase entity, final ItemStack item, final int duration, final ItemStack result) {
        if (!this.world.isRemote) {
            return result;
        }
        return ForgeEventFactory.onItemUseFinish(entity, item, duration, result);
    }

    @Redirect(method = "stopActiveHand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onUseItemStop(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)Z",
            remap = false
        ))
    private boolean forge$OnlyThrowForgeStopEventOnClient(final EntityLivingBase entity, final ItemStack item, final int duration) {
        if (!this.world.isRemote) {
            return false;
        }
        return ForgeEventFactory.onUseItemStop(entity, item, duration);
    }


}
