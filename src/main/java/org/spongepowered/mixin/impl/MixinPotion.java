package org.spongepowered.mixin.impl;

import net.minecraft.potion.Potion;

import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.mixin.Implements;
import org.spongepowered.mod.mixin.Interface;
import org.spongepowered.mod.mixin.Mixin;

@NonnullByDefault
@Mixin(Potion.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potion$"))
public abstract class MixinPotion implements PotionEffectType {

    public boolean potion$isInstant() {
        return false;
    }

}
