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
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.type.entity.CareerRegistryModule;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeVillagerRegistry;

@Mixin(value = VillagerRegistry.VillagerProfession.class, remap = false)
public abstract class MixinVillagerProfession implements IMixinVillagerProfession {

    private static final String REGISTER = "Lnet/minecraftforge/fml/common/registry/VillagerRegistry$"
                                           + "VillagerProfession;register(Lnet/minecraftforge/fml/common/registry/VillagerRegistry$VillagerCareer;)V";
    @Shadow private ResourceLocation name;

    @Override
    public String getId() {
        return this.name.getResourcePath();
    }

    @Inject(method = REGISTER, at = @At("RETURN"), remap = false)
    private void registerForgeCareer(VillagerRegistry.VillagerCareer career, CallbackInfo callbackInfo) {
        Profession profession = SpongeVillagerRegistry.getProfession(((IMixinVillagerCareer) career).getProfession()).get();
        Career career1 = new SpongeCareer(((IMixinVillagerCareer) career).getId(), ((IMixinVillagerCareer) career).getName(), profession);
        CareerRegistryModule.getInstance().registerCareer(career1);
    }

}
