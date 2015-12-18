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

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeVillagerRegistry;

import java.util.ArrayList;

@Mixin(value = VillagerRegistry.class, remap = false)
public class MixinVillagerRegistry {

    @Inject(method = "register(Lnet/minecraftforge/fml/common/registry/VillagerRegistry$VillagerProfession;I)V", at = @At(value = "HEAD", args = "log=true"))
    private void registerForgeVillager(VillagerRegistry.VillagerProfession profession, int id, CallbackInfo ci) {
        if (id != -1) {
            Profession spongeProfession = new SpongeProfession(id, ((IMixinVillagerProfession) profession).getId());
            SpongeVillagerRegistry.registerProfession(profession, spongeProfession);
            ProfessionRegistryModule.getInstance().registerAdditionalCatalog(spongeProfession);
        }
    }

}
