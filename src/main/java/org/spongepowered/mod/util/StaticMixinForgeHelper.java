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
package org.spongepowered.mod.util;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityEntry;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.mod.SpongeMod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nullable;

public final class StaticMixinForgeHelper {

    static final Map<String, EventContextKey<ItemStackSnapshot>> ARMOR_KEYS = new ConcurrentHashMap<>();
    public static final EventContextKey<ISpecialArmor.ArmorProperties> ARMOR_PROPERTY = new EventContextKey<ISpecialArmor.ArmorProperties>() {
        @Override
        public Class<ISpecialArmor.ArmorProperties> getAllowedType() {
            return ISpecialArmor.ArmorProperties.class;
        }

        @Override
        public String getId() {
            return "forge:ArmorProperty";
        }

        @Override
        public String getName() {
            return "ArmorProperty";
        }
    };
    private static final EventContextKey<DamageEventObject> DAMAGE_MODIFIER_OBJECT = new EventContextKey<DamageEventObject>() {
        @Override
        public Class<DamageEventObject> getAllowedType() {
            return DamageEventObject.class;
        }

        @Override
        public String getId() {
            return "sponge:damage_event_object";
        }

        @Override
        public String getName() {
            return "0xDEADBEEF";
        }
    };

    public static Optional<List<DamageFunction>> createArmorModifiers(
        EntityLivingBase entityLivingBase, DamageSource damageSource, double damage) {
        Iterable<ItemStack> inventory = entityLivingBase.getArmorInventoryList();
        final List<ItemStack> itemStacks = Lists.newArrayList(inventory);
        damage *= 25;
        // Beware all ye who enter here, for there's nothing but black magic here.
        ArrayList<ISpecialArmor.ArmorProperties> dmgVals = new ArrayList<ISpecialArmor.ArmorProperties>();
        for (int x = 0; x < itemStacks.size(); x++) {
            ISpecialArmor.ArmorProperties properties = getProperties(entityLivingBase, itemStacks.get(x), damageSource, damage, x);
            if (properties != null) {
                dmgVals.add(properties);
            }
        }
        return createArmorModifiers(dmgVals, itemStacks, damage);
    }

    public static void acceptArmorModifier(EntityLivingBase entity, DamageSource damageSource, DamageModifier modifier, double damage) {
        Optional<ISpecialArmor.ArmorProperties> property = modifier.getCause().getContext().get(ARMOR_PROPERTY);
        final NonNullList<ItemStack> inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory.armorInventory : entity.armorArray;
        if (property.isPresent()) {
            ItemStack stack = inventory.get(property.get().Slot);

            damage = Math.abs(damage) * 25;
            int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            if (stack.getItem() instanceof ISpecialArmor) {
                ((ISpecialArmor) stack.getItem()).damageArmor(entity, stack, damageSource, itemDamage, property.get().Slot);
            } else {
                stack.damageItem(itemDamage, entity);
            }
            if (stack.isEmpty()) {
                inventory.set(property.get().Slot, ItemStack.EMPTY); // Totally unsure whether this is right....
            }
        }
    }

    private static double damageToHandle;

    private static Optional<List<DamageFunction>> createArmorModifiers(List<ISpecialArmor.ArmorProperties> dmgVals, List<ItemStack> inventory, double damage) {
        if (dmgVals.size() > 0) {
            final List<DamageFunction> list = new ArrayList<>();
            ISpecialArmor.ArmorProperties[] props = dmgVals.toArray(new ISpecialArmor.ArmorProperties[dmgVals.size()]);
            sortProperties(props, damage);
            boolean first = true;
            int level = props[0].Priority;
            double ratio = 0;
            for (ISpecialArmor.ArmorProperties prop : props) {
                EquipmentType type = DamageEventHandler.resolveEquipment(prop.Slot);

                final DamageEventObject object = new DamageEventObject();
                object.previousLevel = prop.Priority;
                object.previousRatio = ratio;
                if (first) {
                    object.previousDamage = damage;
                    object.augment = true;
                }
                DoubleUnaryOperator function = incomingDamage -> {
                    incomingDamage *= 25;
                    if (object.augment) {
                        damageToHandle = incomingDamage;
                    }
                    double functionDamage = damageToHandle;
                    object.previousDamage = functionDamage;
                    object.level = prop.Priority;
                    object.ratio = prop.AbsorbRatio;
                    if (object.previousLevel != prop.Priority) {
                        functionDamage -= (functionDamage * object.previousRatio);
                        damageToHandle = functionDamage;
                        object.ratio = 0;
                        object.level = prop.Priority;
                    }
                    object.ratio += prop.AbsorbRatio;
                    return - ((functionDamage * prop.AbsorbRatio) / 25);
                };
                // We still need to "simulate" the original function so that the ratios are handled
                if (level != prop.Priority) {
                    damage -= (damage * ratio);
                    ratio = 0;
                    level = prop.Priority;
                }
                ratio += prop.AbsorbRatio;

                EventContextKey<ItemStackSnapshot> contextKey = ARMOR_KEYS.get("armor:" + type.getId());
                if (contextKey == null) {
                    contextKey = new EventContextKey<ItemStackSnapshot>() {
                        @Override
                        public Class<ItemStackSnapshot> getAllowedType() {
                            return ItemStackSnapshot.class;
                        }

                        @Override
                        public String getId() {
                            return "armor:" + type.getId();
                        }

                        @Override
                        public String getName() {
                            return type.getName();
                        }
                    };
                    ARMOR_KEYS.put("armor: " + type.getId(), contextKey);
                }

                final ItemStack itemStack = inventory.get(prop.Slot);
                DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(
                        EventContext.builder()
                            .add(contextKey, ItemStackUtil.snapshotOf(itemStack))
                            .add(ARMOR_PROPERTY, prop)
                            .add(DAMAGE_MODIFIER_OBJECT, object)
                        .build(),
                        itemStack
                    ))
                    .type(DamageModifierTypes.ARMOR)
                    .build();
                list.add(DamageFunction.of(modifier, function));
                first = false;
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    private static ISpecialArmor.ArmorProperties getProperties(EntityLivingBase base, ItemStack armorStack, DamageSource damageSource, double damage, int index) {
        if (armorStack.isEmpty()) {
            return null;
        }
        ISpecialArmor.ArmorProperties prop = null;
        if (armorStack.getItem() instanceof ISpecialArmor) {
            ISpecialArmor armor = (ISpecialArmor) armorStack.getItem();
            prop = armor.getProperties(base, armorStack, damageSource, damage / 25D, index).copy();
        } else if (armorStack.getItem() instanceof ItemArmor && !damageSource.isUnblockable()) {
            ItemArmor armor = (ItemArmor) armorStack.getItem();
            prop = new ISpecialArmor.ArmorProperties(0, armor.damageReduceAmount / 25D, Integer.MAX_VALUE);
        }
        if (prop != null) {
            prop.Slot = index;
            return prop;
        }
        return null;
    }

    private static void sortProperties(ISpecialArmor.ArmorProperties[] armor, double damage) {
        Arrays.sort(armor);

        int start = 0;
        double total = 0;
        int priority = armor[0].Priority;
        int pStart = 0;
        boolean pChange = false;
        boolean pFinished = false;

        for (int x = 0; x < armor.length; x++) {
            total += armor[x].AbsorbRatio;
            if (x == armor.length - 1 || armor[x].Priority != priority) {
                if (armor[x].Priority != priority) {
                    total -= armor[x].AbsorbRatio;
                    x--;
                    pChange = true;
                }
                if (total > 1) {
                    for (int y = start; y <= x; y++) {
                        double newRatio = armor[y].AbsorbRatio / total;
                        if (newRatio * damage > armor[y].AbsorbMax) {
                            armor[y].AbsorbRatio = armor[y].AbsorbMax / damage;
                            total = 0;
                            for (int z = pStart; z <= y; z++) {
                                total += armor[z].AbsorbRatio;
                            }
                            start = y + 1;
                            x = y;
                            break;
                        }
                        armor[y].AbsorbRatio = newRatio;
                        pFinished = true;
                    }
                    if (pChange && pFinished) {
                        damage -= (damage * total);
                        total = 0;
                        start = x + 1;
                        priority = armor[start].Priority;
                        pStart = start;
                        pChange = false;
                        pFinished = false;
                        if (damage <= 0) {
                            for (int y = x + 1; y < armor.length; y++) {
                                armor[y].AbsorbRatio = 0;
                            }
                            break;
                        }
                    }
                } else {
                    for (int y = start; y <= x; y++) {
                        total -= armor[y].AbsorbRatio;
                        if (damage * armor[y].AbsorbRatio > armor[y].AbsorbMax) {
                            armor[y].AbsorbRatio = armor[y].AbsorbMax / damage;
                        }
                        total += armor[y].AbsorbRatio;
                    }
                    damage -= (damage * total);
                    total = 0;
                    if (x != armor.length - 1) {
                        start = x + 1;
                        priority = armor[start].Priority;
                        pStart = start;
                        pChange = false;
                        if (damage <= 0) {
                            for (int y = x + 1; y < armor.length; y++) {
                                armor[y].AbsorbRatio = 0;
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static String getModIdFromClass(Class clazz) {
        final String className = clazz.getName();
        String modId = className.contains("net.minecraft.") ? "minecraft" : className.contains("org.spongepowered.") ? "sponge" : "unknown";
        String modPackage = className.replace("." + clazz.getSimpleName(), "");
        for (ModContainer mc : Loader.instance().getActiveModList()) {
            if (mc.getOwnedPackages().contains(modPackage)) {
                modId = mc.getModId();
                break;
            }
        }

        return modId;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public static ModContainer getModContainerFromClass(Class clazz) {
        final String className = clazz.getName();
        String modPackage = className.replace("." + clazz.getSimpleName(), "");
        for (ModContainer mc : Loader.instance().getActiveModList()) {
            if (mc.getOwnedPackages().contains(modPackage)) {
                return mc;
            }
        }

        return null;
    }

    public static void registerCustomEntity(EntityEntry entityEntry) {
        if (EntityTypeRegistryModule.getInstance().entityClassToTypeMappings.get(entityEntry.getEntityClass()) != null) {
            return;
        }

        final ModContainer modContainer = getModContainerFromClass(entityEntry.getEntityClass());
        if (modContainer == null) {
            return;
        }

        registerCustomEntity(entityEntry.getEntityClass(), entityEntry.getName(), EntityList.getID(entityEntry.getEntityClass()), modContainer);
    }

    public static void registerCustomEntity(Class<? extends Entity> entityClass, String entityName, int id, ModContainer modContainer) {
        // fix bad entity name registrations from mods
        final String[] parts = entityName.split(":");
        if (parts.length > 1) {
            entityName = parts[1];
        }
        if (entityName.contains(".")) {
            if ((entityName.indexOf(".") + 1) < entityName.length()) {
                entityName = entityName.substring(entityName.indexOf(".") + 1, entityName.length());
            }
        }

        entityName = entityName.replace("entity", "");
        entityName = entityName.replaceAll("[^A-Za-z0-9]", "");
        String modId = "unknown";
        if (modContainer != null) {
            modId = modContainer.getModId();
        }

        if (!modContainer.equals(SpongeMod.instance)) {
            SpongeEntityType entityType = new SpongeEntityType(id, entityName, modId, entityClass, null);
            EntityTypeRegistryModule.getInstance().registerAdditionalCatalog(entityType);
        }
    }

    public static boolean shouldTakeOverModNetworking(ModContainer mod) {
        if (mod == null) {
            return false;
        }
        return SpongeImpl.getGlobalConfig().getConfig().getBrokenMods().getBrokenNetworkHandlerMods().contains(mod.getModId());
    }
}
