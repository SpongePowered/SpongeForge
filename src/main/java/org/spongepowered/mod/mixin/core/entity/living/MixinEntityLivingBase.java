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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.phase.SpawningPhase;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 1001)
public abstract class MixinEntityLivingBase extends MixinEntity implements Living, IMixinEntityLivingBase {

    private EntityLivingBase nmsEntityLiving = (EntityLivingBase) (Object) this;

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", args = "log=true", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    public boolean onLivingDropSpawn(World world, Entity entityIn) {
        IMixinWorld spongeWorld = (IMixinWorld) world;
        CauseTracker causeTracker = spongeWorld.getCauseTracker();
        causeTracker.push(SpawningPhase.State.DEATH_DROPS_SPAWNING);
        boolean result = world.spawnEntityInWorld(entityIn);
        // todo
//        causeTracker.completeEntitySpawnPhase();
        return result;
    }

    @Override
    public Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> provideArmorModifiers(EntityLivingBase entityLivingBase,
         DamageSource source, double damage) {
        return StaticMixinForgeHelper.createArmorModifiers(this.nmsEntityLiving, source, damage);
    }

    @Override
    public float applyModDamage(EntityLivingBase entityLivingBase, DamageSource source, float damage) {
        return ForgeHooks.onLivingHurt(this.nmsEntityLiving, source, damage);
    }

    @Override
    public void applyArmorDamage(EntityLivingBase entityLivingBase, DamageSource source, DamageEntityEvent entityEvent, DamageModifier modifier) {
        Optional<ArmorProperties> optional = modifier.getCause().first(ArmorProperties.class);
        if (optional.isPresent()) {
            StaticMixinForgeHelper.acceptArmorModifier(this.nmsEntityLiving, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public boolean hookModAttack(EntityLivingBase entityLivingBase, DamageSource source, float amount) {
        return net.minecraftforge.common.ForgeHooks.onLivingAttack(this.nmsEntityLiving, source, amount);
    }
}
