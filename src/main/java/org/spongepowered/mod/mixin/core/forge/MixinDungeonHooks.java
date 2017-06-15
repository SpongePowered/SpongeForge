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
package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DungeonHooks;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.registry.type.world.gen.DungeonMobRegistryModule;

import java.util.Optional;

@Mixin(DungeonHooks.class)
public class MixinDungeonHooks {

    @Inject(method = "addDungeonMob(Lnet/minecraft/util/ResourceLocation;I)F", at = @At("RETURN"), remap = false)
    protected static void onAddDungeonMob(CallbackInfoReturnable<Float> ci, ResourceLocation name, int rarity) {
        Optional<EntityType> type = EntityUtil.fromLocationToType(name);
        if (!type.isPresent()) {
            SpongeImpl.getLogger().error("Mod tried to add a DungeonMob for a non existant mob " + name +" !");
            return;
        }

        Float ret = ci.getReturnValue();
        DungeonMobRegistryModule.getInstance().put(type.get(), ret.intValue());
    }

    @Inject(method = "removeDungeonMob(Lnet/minecraft/util/ResourceLocation;)I", at = @At("HEAD"), remap = false)
    protected static void onRemoveDungeonMob(CallbackInfoReturnable<Integer> ci, ResourceLocation name) {
        Optional<EntityType> type = EntityUtil.fromLocationToType(name);
        if (!type.isPresent()) {
            SpongeImpl.getLogger().error("Mod tried to remove a DungeonMob for a non existant mob " + name +" !");
            return;
        }

        DungeonMobRegistryModule.getInstance().remove(type.get());
    }

}
