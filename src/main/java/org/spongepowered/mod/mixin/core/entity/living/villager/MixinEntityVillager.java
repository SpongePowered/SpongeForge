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
package org.spongepowered.mod.mixin.core.entity.living.villager;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.types.Career;
import org.spongepowered.api.data.types.Profession;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.entity.SpongeEntityMeta;
import org.spongepowered.mod.mixin.core.entity.living.MixinEntityAgeable;

import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.entity.passive.EntityVillager.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.living.Villager.class, prefix = "villager$"))
public abstract class MixinEntityVillager extends MixinEntityAgeable {

    @Shadow private boolean isPlaying;
    @Shadow private EntityPlayer buyingPlayer;
    @Shadow private int careerId;
    @Shadow private MerchantRecipeList buyingList;
    @Shadow public abstract int getProfession();
    @Shadow public abstract void setProfession(int professionId);
    @Shadow public abstract void setCustomer(EntityPlayer player);
    @Shadow(prefix = "shadow$")
    public abstract EntityPlayer shadow$getCustomer();
    @Shadow public abstract MerchantRecipeList getRecipes(EntityPlayer player);

    private Profession profession;

    @SuppressWarnings("unchecked")
    @Inject(method = "setProfession(I)V", at = @At("RETURN"))
    public void onSetProfession(int professionId, CallbackInfo ci) {
        // TODO: Fix GameRegistry for API changes
        this.profession = ((List<? extends Profession>) SpongeMod.instance.getGame().getRegistry().getAllOf(Profession.class)).get(professionId);
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    @Overwrite
    public boolean isTrading() {
        return this.buyingPlayer != null;
    }

    public Career getCareer() {
        // TODO: Fix GameRegistry for API changes
        return ((List<Career>) SpongeMod.instance.getGame().getRegistry().getCareers(this.profession)).get(this.careerId);
    }

    public void setCareer(Career career) {
        setProfession(((SpongeEntityMeta) career.getProfession()).type);
        this.careerId = ((SpongeEntityMeta) career).type;
    }

    public Optional<Human> getCustomer() {
        return Optional.fromNullable((Human) this.shadow$getCustomer());
    }

    public void setCustomer(@Nullable Human human) {
        this.setCustomer((EntityPlayer) human);
    }

    @SuppressWarnings("unchecked")
    public List<TradeOffer> getOffers() {
        return getRecipes(null);
    }

    public void setOffers(List<TradeOffer> offers) {
        this.buyingList = (MerchantRecipeList) offers;
    }

    @SuppressWarnings("unchecked")
    public void addOffer(TradeOffer offer) {
        this.buyingList.add(offer);
    }

}
