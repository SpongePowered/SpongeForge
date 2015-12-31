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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.DamageEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class StaticMixinForgeHelper {

    public static DamageSource exchangeDamageSource(DamageSource damageSource) {

        return damageSource;
    }

    public static Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> createArmorModifiers(
        EntityLivingBase entityLivingBase, DamageSource damageSource, double damage) {
        ItemStack[] inventory = (entityLivingBase instanceof EntityPlayer) ? ((EntityPlayer) entityLivingBase).inventory.armorInventory : entityLivingBase.getInventory();
        damage *= 25;
        // Beware all ye who enter here, for there's nothing but black magic here.
        ArrayList<ISpecialArmor.ArmorProperties> dmgVals = new ArrayList<ISpecialArmor.ArmorProperties>();
        for (int x = 0; x < inventory.length; x++) {
            ISpecialArmor.ArmorProperties properties = getProperties(entityLivingBase, inventory[x], damageSource, damage, x);
            if (properties != null) {
                dmgVals.add(properties);
            }
        }
        return createArmorModifiers(dmgVals, inventory, damage);
    }

    public static void acceptArmorModifier(EntityLivingBase entity, DamageSource damageSource, DamageModifier modifier, double damage) {
        Optional<ISpecialArmor.ArmorProperties> property = modifier.getCause().first(ISpecialArmor.ArmorProperties.class);
        final ItemStack[] inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory.armorInventory : entity.getInventory();
        if (property.isPresent()) {
            damage = Math.abs(damage) * 25;
            ItemStack stack = inventory[property.get().Slot];
            int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            if (stack.getItem() instanceof ISpecialArmor) {
                ((ISpecialArmor) stack.getItem()).damageArmor(entity, stack, damageSource, itemDamage, property.get().Slot);
            } else {
                stack.damageItem(itemDamage, entity);
            }
            if (stack.stackSize <= 0) {
                inventory[property.get().Slot] = null;
            }
        }
    }

    private static double damageToHandle;

    private static Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> createArmorModifiers(List<ISpecialArmor.ArmorProperties> dmgVals, ItemStack[] inventory, double damage) {
        if (dmgVals.size() > 0) {
            final List<Tuple<DamageModifier, Function<? super Double, Double>>> list = new ArrayList<>();
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
                Function<? super Double, Double> function = incomingDamage -> {
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

                DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(NamedCause.of(DamageEntityEvent.GENERAL_ARMOR + ":" + type.getId(),
                                                  ((org.spongepowered.api.item.inventory.ItemStack) inventory[prop.Slot]).createSnapshot()),
                                    NamedCause.of("ArmorProperty", prop),
                                    NamedCause.of("0xDEADBEEF", object)))
                    .type(DamageModifierTypes.ARMOR)
                    .build();
                list.add(new Tuple<>(modifier, function));
                first = false;
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    private static ISpecialArmor.ArmorProperties getProperties(EntityLivingBase base, ItemStack armorStack, DamageSource damageSource, double damage, int index) {
        if (armorStack == null) {
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
                            armor[y].AbsorbRatio = (double) armor[y].AbsorbMax / damage;
                            total = 0;
                            for (int z = pStart; z <= y; z++) {
                                total += armor[z].AbsorbRatio;
                            }
                            start = y + 1;
                            x = y;
                            break;
                        } else {
                            armor[y].AbsorbRatio = newRatio;
                            pFinished = true;
                        }
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
                            armor[y].AbsorbRatio = (double) armor[y].AbsorbMax / damage;
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

}
