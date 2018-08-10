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
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeForgeVillagerRegistry;

@Mixin(value = VillagerRegistry.class, remap = false)
public class MixinVillagerRegistry {

    @Shadow RegistryNamespaced<ResourceLocation, VillagerRegistry.VillagerProfession> REGISTRY;

    /**
     * @author gabizou - July 1st, 2017
     * @reason Rewrite the register method to join Sponge's villager registrations with forge's.
     * For some auspicious reason, when trying to write a redirect, the redirect will fail...
     *
     * @param prof The profession being registered
     * @param id The id being registered
     */
    @Overwrite
    private void register(VillagerRegistry.VillagerProfession prof, int id) {
        this.REGISTRY.register(id, ((IMixinVillagerProfession) prof).getName(), prof);
        final int professionId = this.REGISTRY.getIDForObject(prof);
        final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) prof;
        final SpongeProfession spongeProfession = new SpongeProfession(professionId, mixinProfession.getId(), mixinProfession.getProfessionName());
        ((IMixinVillagerProfession) prof).setSpongeProfession(spongeProfession);
        ProfessionRegistryModule.getInstance().registerAdditionalCatalog(spongeProfession);
    }

}
