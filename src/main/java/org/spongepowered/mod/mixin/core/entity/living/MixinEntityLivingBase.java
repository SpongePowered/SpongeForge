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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityLivingBase.class)
@Implements(@Interface(iface = Living.class, prefix = "living$"))
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public int deathTime;
    @Shadow public boolean potionsNeedUpdate;
    @Shadow public CombatTracker _combatTracker;
    @Shadow public EntityLivingBase entityLivingToAttack;
    @Shadow protected float lastDamage;
    @Shadow protected EntityPlayer attackingPlayer;
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potionEffect);
    @Shadow public abstract void removePotionEffect(int id);
    @Shadow public abstract void setCurrentItemOrArmor(int slotIn, net.minecraft.item.ItemStack stack);
    @Shadow public abstract void clearActivePotions();
    @Shadow public abstract void setLastAttacker(Entity entity);
    @Shadow public abstract boolean isPotionActive(Potion potion);
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract Collection getActivePotionEffects();
    @Shadow public abstract EntityLivingBase getLastAttacker();
    @Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);
    @Shadow public abstract net.minecraft.item.ItemStack getEquipmentInSlot(int slotIn);

    private int maxAir = 300;

    public void setLastAttacker(@Nullable Living lastAttacker) {
        setLastAttacker((EntityLivingBase) lastAttacker);
    }

    public void setMaxHealth(double maxHealth) {
        getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth);

        if (getHealth() > maxHealth) {
            setHealth((float) maxHealth);
        }
    }

    public void damageD(double amount) {
        Living thisEntity = (Living) this;
        DamageSource source = DamageSource.generic;
        if (thisEntity instanceof Human) {
            source = net.minecraft.util.DamageSource.causePlayerDamage((EntityPlayerMP) thisEntity);
        } else {
            source = net.minecraft.util.DamageSource.causeMobDamage((EntityLivingBase) thisEntity);
        }

        if (thisEntity instanceof net.minecraft.entity.boss.EntityDragon) {
            ((EntityDragon) thisEntity).attackEntityFrom(source, (float) amount);
        } else {
            attackEntityFrom(source, (float) amount);
        }
    }

    public double getHealthD() {
        return getHealth();
    }

    public void setHealthD(double health) {
        Living thisEntity = (Living) this;
        setHealth((float) health);

        if (thisEntity instanceof EntityPlayerMP && health == 0) {
            ((EntityPlayerMP) thisEntity).onDeath(DamageSource.generic);
        }
    }

    public double getMaxHealthD() {
        return getMaxHealth();
    }

    public void addPotionEffect(PotionEffect potionEffect, boolean force) {
        if (hasPotionEffect(potionEffect.getType())) {
            if (!force) {
                return;
            }
            removePotionEffect(potionEffect.getType());
        }

        addPotionEffect(new net.minecraft.potion.PotionEffect((net.minecraft.potion.PotionEffect) potionEffect));
    }

    public void addPotionEffects(Collection<PotionEffect> potionEffects, boolean force) {
        for (PotionEffect effect : potionEffects) {
            addPotionEffect(effect, force);
        }
    }

    public void removePotionEffect(PotionEffectType potionEffectType) {
        removePotionEffect(((Potion) potionEffectType).getId());
    }

    public boolean hasPotionEffect(PotionEffectType potionEffectType) {
        return isPotionActive((Potion) potionEffectType);
    }

    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
        for (Object obj : getActivePotionEffects()) {
            potionEffects.add((PotionEffect) obj);
        }
        return potionEffects;
    }

    public Optional<Living> getLastAttackerAPI() {
        return Optional.fromNullable((Living) getLastAttacker());
    }

    public double getEyeHeightD() {
        return getEyeHeight();
    }

    public Vector3d getEyeLocation() {
        return ((Living) this).getLocation().getPosition().add(0, getEyeHeight(), 0);
    }

    public int getRemainingAir() {
        return getAir();
    }

    public void setRemainingAir(int air) {
        setAir(air);
    }

    public int getMaxAir() {
        return this.maxAir;
    }

    public void setMaxAir(int air) {
        this.maxAir = air;
    }

    public double getLastDamage() {
        return this.lastDamage;
    }

    public void setLastDamage(double damage) {
        this.lastDamage = (float) damage;
    }

    public int getInvulnerabilityTicks() {
        return this.hurtResistantTime;
    }

    public void setInvulnerabilityTicks(int ticks) {
        this.hurtResistantTime = ticks;
    }

    public int getMaxInvulnerabilityTicks() {
        return this.maxHurtResistantTime;
    }

    public void setMaxInvulnerabilityTicks(int ticks) {
        this.maxHurtResistantTime = ticks;
    }

    public String getCustomName() {
        return getCustomNameTag();
    }

    public void setCustomName(String name) {
        if (name == null) {
            name = "";
        }

        if (name.length() > 64) {
            name = name.substring(0, 64);
        }

        setCustomNameTag(name);
    }

    public boolean isCustomNameVisible() {
        return getAlwaysRenderNameTag();
    }

    public void setCustomNameVisible(boolean visible) {
        setAlwaysRenderNameTag(visible);
    }

    public boolean isAPIInvisible() {
        return this.getFlag(5);
    }

    public void setAPIInvisible(boolean invisible) {
        this.setFlag(5, invisible);
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("maxAir")) {
            this.maxAir = compound.getInteger("maxAir");
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setInteger("maxAir", this.maxAir);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();

        return container;
    }

}
