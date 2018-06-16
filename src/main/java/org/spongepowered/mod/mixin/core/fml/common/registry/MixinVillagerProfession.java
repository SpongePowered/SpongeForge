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
package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = VillagerRegistry.VillagerProfession.class, remap = false)
public abstract class MixinVillagerProfession extends IForgeRegistryEntry.Impl<VillagerRegistry.VillagerProfession> implements IMixinVillagerProfession {

    @Shadow private ResourceLocation name;
    @Shadow private List<VillagerRegistry.VillagerCareer> careers;

    @Nullable private SpongeProfession spongeProfession;

    @Override
    public String getId() {
        return this.getRegistryName().toString();
    }

    @Override
    public List<VillagerRegistry.VillagerCareer> getCareers() {
        return this.careers;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public String getProfessionName() {
        return this.name.getResourcePath();
    }

    @Override
    public Optional<SpongeProfession> getSpongeProfession() {
        return Optional.ofNullable(this.spongeProfession);
    }

    @Override
    public void setSpongeProfession(@Nullable SpongeProfession profession) {
        this.spongeProfession = profession;
    }

    @Inject(method = "register(Lnet/minecraftforge/fml/common/registry/VillagerRegistry$VillagerCareer;)V",
            at = @At(value = "RETURN"), remap = false)
    private void registerForgeCareer(VillagerRegistry.VillagerCareer career, CallbackInfo callbackInfo) {
        if (this.spongeProfession == null) {
            this.spongeProfession = SpongeForgeVillagerRegistry.fromNative((VillagerRegistry.VillagerProfession) (Object) this);
        }
        final IMixinVillagerCareer mixinCareer = (IMixinVillagerCareer) career;
        final int careerId = mixinCareer.getId();
        final SpongeCareer suggestedCareer = new SpongeCareer(careerId, career.getName(), this.spongeProfession, new SpongeTranslation("entity.Villager." + career.getName()));
        mixinCareer.setSpongeCareer(suggestedCareer);
        this.spongeProfession.getUnderlyingCareers().add(suggestedCareer);
    }

}
