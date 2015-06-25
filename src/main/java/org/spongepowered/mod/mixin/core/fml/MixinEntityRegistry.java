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
package org.spongepowered.mod.mixin.core.fml;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeEntityRegistry;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.registry.SpongeModGameRegistry;

@NonnullByDefault
@Mixin(value = EntityRegistry.class, remap = false)
public abstract class MixinEntityRegistry implements SpongeEntityRegistry {

    private SpongeModGameRegistry gameRegistry = (SpongeModGameRegistry) SpongeMod.instance.getGame().getRegistry();

    @Inject(method = "doModEntityRegistration", at = @At(value = "RETURN", ordinal = 1))
    private void onModEntityRegistration(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange,
            int updateFrequency, boolean sendsVelocityUpdates, CallbackInfo ci) {
        registerCustomEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    @Override
    public void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange,
            int updateFrequency, boolean sendsVelocityUpdates) {
        // fixup bad entity names from mods
        if (entityName.contains(".")) {
            if ((entityName.indexOf(".") + 1) < entityName.length()) {
                entityName = entityName.substring(entityName.indexOf(".") + 1, entityName.length());
            }
        }
        entityName.replace("entity", "");
        if (entityName.startsWith("ent")) {
            entityName.replace("ent", "");
        }
        entityName = entityName.replaceAll("[^A-Za-z0-9]", ""); // remove all non-digits/alphanumeric
        ModContainer activeModContainer = Loader.instance().activeModContainer();
        String modId = "unknown";
        if (activeModContainer != null) {
            modId = activeModContainer.getModId();
        }
        //entityName = modId + "-" + entityName;
        SpongeEntityType entityType = new SpongeEntityType(id, entityName, modId, entityClass);
        this.gameRegistry.entityClassToTypeMappings.put(entityClass, entityType);
        this.gameRegistry.entityIdToTypeMappings.put(entityType.getId(), entityType);
    }

}
