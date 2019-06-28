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
import org.spongepowered.mod.bridge.registry.VillagerCareerBridge_Forge;
import org.spongepowered.mod.bridge.registry.VillagerProfessionBridge_Forge;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = VillagerRegistry.VillagerProfession.class, remap = false)
public abstract class VillagerRegistry$VillagerProfessionMixin_Forge extends IForgeRegistryEntry.Impl<VillagerRegistry.VillagerProfession> implements
    VillagerProfessionBridge_Forge {

    @Shadow private ResourceLocation name;
    @Shadow private List<VillagerRegistry.VillagerCareer> careers;

    @Nullable private SpongeProfession spongeProfession;

    @Override
    public String forgeBridge$getId() {
        return this.getRegistryName().toString();
    }

    @Override
    public List<VillagerRegistry.VillagerCareer> forgeBridge$getCareers() {
        return this.careers;
    }

    @Override
    public ResourceLocation forgeBridge$getName() {
        return this.name;
    }

    @Override
    public String forgeBridge$getProfessionName() {
        return this.name.getPath();
    }

    @Override
    public Optional<SpongeProfession> forgeBridge$getSpongeProfession() {
        return Optional.ofNullable(this.spongeProfession);
    }

    @Override
    public void forgeBridge$setSpongeProfession(@Nullable final SpongeProfession profession) {
        this.spongeProfession = profession;
    }

    @Inject(method = "register(Lnet/minecraftforge/fml/common/registry/VillagerRegistry$VillagerCareer;)V",
            at = @At(value = "RETURN"), remap = false)
    private void registerForgeCareer(final VillagerRegistry.VillagerCareer career, final CallbackInfo callbackInfo) {
        if (this.spongeProfession == null) {
            this.spongeProfession = SpongeForgeVillagerRegistry.fromNative((VillagerRegistry.VillagerProfession) (Object) this);
        }
        final VillagerCareerBridge_Forge mixinCareer = (VillagerCareerBridge_Forge) career;
        final int careerId = mixinCareer.forgeBridge$getId();
        final SpongeCareer suggestedCareer = new SpongeCareer(careerId, career.getName(), this.spongeProfession, new SpongeTranslation("entity.Villager." + career.getName()));
        mixinCareer.forgeBridge$setSpongeCareer(suggestedCareer);
        this.spongeProfession.getUnderlyingCareers().add(suggestedCareer);
    }

}
