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
package org.spongepowered.mod.mixin.core.entity.projectile;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.projectile.source.UnknownProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.player.fishing.PlayerHookedEntityEvent;
import org.spongepowered.api.event.entity.player.fishing.PlayerRetractFishingLineEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.entity.DamageHandler;
import org.spongepowered.mod.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.mod.interfaces.IMixinEntityFishHook;
import org.spongepowered.mod.mixin.core.entity.MixinEntity;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook extends MixinEntity implements FishHook, IMixinEntityFishHook {

    @Shadow private boolean inGround;
    @Shadow private EntityPlayer angler;
    @Shadow public Entity caughtEntity;
    @Shadow private int ticksCatchable;
    @Shadow public abstract ItemStack getFishingResult();

    @Nullable
    public ProjectileSource projectileSource;
    private double damageAmount;
    private ItemStack fishingRod;

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource != null) {
            return this.projectileSource;
        } else if (this.angler != null && this.angler instanceof ProjectileSource) {
            return (ProjectileSource) this.angler;
        }
        return new UnknownProjectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof EntityPlayer) {
            // This allows things like Vanilla kill attribution to take place
            this.angler = (EntityPlayer) shooter;
        } else {
            this.angler = null;
        }
        this.projectileSource = shooter;
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getHookedEntity() {
        return Optional.fromNullable((org.spongepowered.api.entity.Entity) this.caughtEntity);
    }

    @Override
    public void setHookedEntity(@Nullable org.spongepowered.api.entity.Entity entity) {
        this.caughtEntity = (Entity) entity;
    }

    @Redirect(method = "onUpdate()V", at =
            @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z")
        )
    public boolean onAttackEntityFrom(Entity this$0, DamageSource damageSource, float damage) {
        PlayerHookedEntityEvent event = SpongeEventFactory.createPlayerHookedEntityEvent(SpongeMod.instance.getGame(), (Player) this.angler, this,
                (org.spongepowered.api.entity.Entity) this$0);
        if (!SpongeMod.instance.getGame().getEventManager().post(event)) {
            if (this.getShooter() instanceof Entity) {
                damageSource = DamageHandler.damage(this, (Entity) this.getShooter());
            }
            return this$0.attackEntityFrom(damageSource, (float) this.getDamage());
        }
        return false;
    }

    public double getDamage() {
        return this.damageAmount;
    }

    public void setDamage(double damage) {
        this.damageAmount = damage;
    }

    // I hate to use @Overwrite here, but we need to be able handle both an entity being caught
    // and a fish being caught. There's no good way to do this with an injection.
    @Overwrite
    public int handleHookRetraction() {
        ItemStack itemStack = null;
        int exp = 0;
        if (this.ticksCatchable > 0) {
            itemStack = this.getFishingResult();
            exp = this.rand.nextInt(6) + 1;
        }

        PlayerRetractFishingLineEvent event = SpongeEventFactory
                .createPlayerRetractFishingLineEvent(SpongeMod.instance.getGame(), (Player) this.angler, this,
                        (org.spongepowered.api.item.inventory.ItemStack) itemStack, (org.spongepowered.api.entity.Entity) this.caughtEntity, exp);
        byte b0 = 0;
        if (!SpongeMod.instance.getGame().getEventManager().post(event)) {
            exp = event.getExp();
            if (event.getCaughtEntity().isPresent()) {
                this.caughtEntity = (Entity) event.getCaughtEntity().get();

                double entityitem = this.angler.posX - this.posX;
                double d2 = this.angler.posY - this.posY;
                double d4 = this.angler.posZ - this.posZ;
                double d6 = MathHelper.sqrt_double(entityitem * entityitem + d2 * d2 + d4 * d4);
                double d8 = 0.1D;
                this.caughtEntity.motionX += entityitem * d8;
                this.caughtEntity.motionY += d2 * d8 + MathHelper.sqrt_double(d6) * 0.08D;
                this.caughtEntity.motionZ += d4 * d8;
                b0 = 3;
            }

            if (event.getCaughtItem().isPresent()) {
                EntityItem entityitem1 = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, (ItemStack) event.getCaughtItem().get());
                double d1 = this.angler.posX - this.posX;
                double d3 = this.angler.posY - this.posY;
                double d5 = this.angler.posZ - this.posZ;
                double d7 = MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
                double d9 = 0.1D;
                entityitem1.motionX = d1 * d9;
                entityitem1.motionY = d3 * d9 + MathHelper.sqrt_double(d7) * 0.08D;
                entityitem1.motionZ = d5 * d9;
                this.worldObj.spawnEntityInWorld(entityitem1);
                this.angler.worldObj.spawnEntityInWorld(
                        new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D,
                                this.rand.nextInt(6) + 1));
                b0 = 1;
            }

            if (this.inGround) {
                b0 = 2;
            }

            this.setDead();
            this.angler.fishEntity = null;

            if (this.fishingRod != null) {
                this.fishingRod.damageItem(b0, this.angler);
                this.angler.swingItem();
                this.fishingRod = null;
            }
        }
        return b0;
    }

    @Override
    public void setFishingRodItemStack(ItemStack fishingRod) {
        this.fishingRod = fishingRod;
    }

    @Override
    @SoftOverride
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("damageAmount")) {
            this.damageAmount = compound.getDouble("damageAmount");
        }
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    @SoftOverride
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setDouble("damageAmount", this.damageAmount);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, this.angler);
    }
}
