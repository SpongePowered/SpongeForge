package org.spongepowered.mixin.impl;

import java.util.Map;

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
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.entity.SpongeEntityRegistry;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Overwrite;
import org.spongepowered.mod.mixin.Shadow;
import org.spongepowered.mod.registry.SpongeGameRegistry;

import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;

@NonnullByDefault
@Mixin(EntityRegistry.class)
public abstract class MixinEntityRegistry implements SpongeEntityRegistry {

    private SpongeGameRegistry gameRegistry = (SpongeGameRegistry)SpongeMod.instance.getGame().getRegistry();

    @Shadow
    private ListMultimap<ModContainer, EntityRegistration> entityRegistrations;
    @Shadow
    private Map<String,ModContainer> entityNames;
    @Shadow
    private BiMap<Class<? extends Entity>, EntityRegistration> entityClassRegistrations;

    @SuppressWarnings("unchecked")
    @Overwrite
    private void doModEntityRegistration(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        EntityRegistration er = EntityRegistry.instance().new EntityRegistration(mc, entityClass, entityName, id, trackingRange, updateFrequency, sendsVelocityUpdates);
        try
        {
            entityClassRegistrations.put(entityClass, er);
            entityNames.put(entityName, mc);
            if (!EntityList.classToStringMapping.containsKey(entityClass))
            {
                String entityModName = String.format("%s.%s", mc.getModId(), entityName);
                EntityList.classToStringMapping.put(entityClass, entityModName);
                EntityList.stringToClassMapping.put(entityModName, entityClass);
                FMLLog.finer("Automatically registered mod %s entity %s as %s", mc.getModId(), entityName, entityModName);
            }
            else
            {
                FMLLog.fine("Skipping automatic mod %s entity registration for already registered class %s", mc.getModId(), entityClass.getName());
            }
        }
        catch (IllegalArgumentException e)
        {
            FMLLog.log(Level.WARN, e, "The mod %s tried to register the entity (name,class) (%s,%s) one or both of which are already registered", mc.getModId(), entityName, entityClass.getName());
            return;
        }
        entityRegistrations.put(mc, er);
        registerCustomEntity(entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    @Override
    public void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        ModContainer activeModContainer = Loader.instance().activeModContainer();
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
        gameRegistry.entityClassToTypeMappings.put(entityClass, entityType);
        gameRegistry.entityIdToTypeMappings.put(entityType.getId(), entityType);
    }

}
