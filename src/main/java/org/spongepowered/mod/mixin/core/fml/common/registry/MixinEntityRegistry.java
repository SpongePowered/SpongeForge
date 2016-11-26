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

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

@NonnullByDefault
@Mixin(value = EntityRegistry.class, remap = false)
public abstract class MixinEntityRegistry {

    @Inject(method = "doModEntityRegistration", at = @At(value = "RETURN", ordinal = 1))
    private void onModEntityRegistration(ResourceLocation registryName, Class<? extends Entity> entityClass, String entityName,
            int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, CallbackInfo ci) {
        registerCustomEntity(entityClass, entityName, id, FMLCommonHandler.instance().findContainerFor(mod));
    }

    private static void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, ModContainer modContainer) {
        // fix bad entity name registrations from mods
        if (entityName.contains(".")) {
            if ((entityName.indexOf(".") + 1) < entityName.length()) {
                entityName = entityName.substring(entityName.indexOf(".") + 1, entityName.length());
            }
        }

        entityName = entityName.replace("entity", "");
        if (entityName.startsWith("ent")) {
            entityName = entityName.replace("ent", "");
        }

        entityName = entityName.replaceAll("[^A-Za-z0-9]", "");
        String modId = "unknown";
        if (modContainer != null) {
            modId = modContainer.getModId();
        }

        SpongeEntityType entityType = new SpongeEntityType(id, entityName, modId, entityClass, null);
        EntityTypeRegistryModule.getInstance().registerAdditionalCatalog(entityType);
    }

}
