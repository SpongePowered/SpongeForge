package org.spongepowered.mixin.impl;

import net.minecraft.potion.Potion;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

@NonnullByDefault
@Mixin(net.minecraft.potion.PotionEffect.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potionEffect$"))
public abstract class MixinPotionEffect implements PotionEffect {

    @Shadow 
    public abstract int getPotionId();

    @Shadow 
    private int duration;

    @Shadow
    private int amplifier;

    @Shadow
    private boolean isAmbient;

    @Shadow
    private boolean showParticles;

    @Override
    public PotionEffectType getType() {
        return (PotionEffectType) Potion.potionTypes[getPotionId()];
    }

    @Override
    public void apply(Living ent) {
        // TODO
    }

    public int potionEffect$getDuration() {
        return this.duration;
    }

    public void potionEffect$setDuration(int duration) {
        this.duration = duration;
    }

    public int potionEffect$getAmplifier() {
        return this.amplifier;
    }

    @Override
    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    @Override
    public boolean isAmbient() {
        return this.isAmbient;
    }

    @Override
    public void setAmbient(boolean ambient) {
        this.isAmbient = ambient;
    }

    @Override
    public boolean getShowParticles() {
        return showParticles;
    }

    @Override
    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }
}
