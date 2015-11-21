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
package org.spongepowered.mod.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSpawnableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class SpawnableDataProcessor extends AbstractItemSingleDataProcessor<EntityType, Value<EntityType>, SpawnableData, ImmutableSpawnableData> {

    public SpawnableDataProcessor() {
        super(input -> input.getItem().equals(Items.spawn_egg), Keys.SPAWNABLE_ENTITY_TYPE);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public boolean set(ItemStack itemStack, EntityType value) {
        final String name = (String) EntityList.classToStringMapping.get(value.getEntityClass());
        if (EntityList.stringToIDMapping.containsKey(name)) {
            final int id = (int) EntityList.stringToIDMapping.get(name);
            if (EntityList.entityEggs.containsKey(id)) {
                itemStack.setItemDamage(id);
                return true;
            }
        } else {
            if (!itemStack.hasTagCompound()) {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = itemStack.getTagCompound();
            tag.setString("entity_name", name);
            final EntityRegistry.EntityRegistration registration = EntityRegistry.instance()
                    .lookupModSpawn((Class<? extends Entity>) value.getEntityClass(), false);
            itemStack.setItemDamage(registration.getModEntityId());
            return true;
        }
        return false;
    }

    @Override
    public Optional<EntityType> getVal(ItemStack itemStack) {
        final Class entity = (Class) EntityList.stringToClassMapping.get(ItemMonsterPlacer.getEntityName(itemStack));
        for (EntityType type : SpongeImpl.getRegistry().getAllOf(EntityType.class)) {
            if (type.getEntityClass().equals(entity)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public ImmutableValue<EntityType> constructImmutableValue(EntityType value) {
        return ImmutableSpongeValue.cachedOf(Keys.SPAWNABLE_ENTITY_TYPE, EntityTypes.CREEPER, value);
    }

    @Override
    public SpawnableData createManipulator() {
        return new SpongeSpawnableData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            ItemStack item = (ItemStack) dataHolder;
            item.setItemDamage(0);
            if (item.hasTagCompound() && item.getTagCompound().hasKey("entity_name", 8)) {
                item.getTagCompound().removeTag("entity_name");
            }
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
}
