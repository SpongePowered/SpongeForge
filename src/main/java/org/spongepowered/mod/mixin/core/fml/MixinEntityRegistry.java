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

import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.entity.SpongeEntityRegistry;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import java.util.Map;

@NonnullByDefault
@Mixin(value = EntityRegistry.class, remap = false)
public abstract class MixinEntityRegistry implements SpongeEntityRegistry {

    private SpongeGameRegistry gameRegistry = (SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry();

    @Shadow
    private ListMultimap<ModContainer, EntityRegistration> entityRegistrations;

    @Shadow
    private Map<String, ModContainer> entityNames;

    @Shadow
    private BiMap<Class<? extends Entity>, EntityRegistration> entityClassRegistrations;

    @SuppressWarnings({"unchecked", "unused"})
    private void doModEntityRegistration(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange,
            int updateFrequency, boolean sendsVelocityUpdates) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        EntityRegistration er = EntityRegistry.instance().new EntityRegistration(mc, entityClass, entityName, id, trackingRange, updateFrequency,
                sendsVelocityUpdates);
        try {
            this.entityClassRegistrations.put(entityClass, er);
            this.entityNames.put(entityName, mc);
            if (!EntityList.classToStringMapping.containsKey(entityClass)) {
                String entityModName = String.format("%s.%s", mc.getModId(), entityName);
                EntityList.classToStringMapping.put(entityClass, entityModName);
                EntityList.stringToClassMapping.put(entityModName, entityClass);
                FMLLog.finer("Automatically registered mod %s entity %s as %s", mc.getModId(), entityName, entityModName);
            } else {
                FMLLog.fine("Skipping automatic mod %s entity registration for already registered class %s", mc.getModId(), entityClass.getName());
            }
        } catch (IllegalArgumentException e) {
            FMLLog.log(Level.WARN, e, "The mod %s tried to register the entity (name,class) (%s,%s) one or both of which are already registered",
                    mc.getModId(), entityName, entityClass.getName());
            return;
        }
        this.entityRegistrations.put(mc, er);
        registerCustomEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    @Override
    public void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange,
            int updateFrequency, boolean sendsVelocityUpdates) {
        final ModContainer activeModContainer = Loader.instance().activeModContainer();
        String modId = "unknown";
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
        if (activeModContainer != null) {
            modId = activeModContainer.getModId();
        }
        //entityName = modId + "-" + entityName;
        SpongeEntityType entityType = new SpongeEntityType(id, entityName, modId, entityClass);
        this.gameRegistry.entityClassToTypeMappings.put(entityClass, entityType);
        this.gameRegistry.entityIdToTypeMappings.put(entityType.getId(), entityType);
    }

}
