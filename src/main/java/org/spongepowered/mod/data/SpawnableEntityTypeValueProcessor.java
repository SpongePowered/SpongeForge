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
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueBuilder;

import java.util.Optional;

public class SpawnableEntityTypeValueProcessor extends AbstractSpongeValueProcessor<ItemStack, EntityType, Value<EntityType>> {

    public SpawnableEntityTypeValueProcessor() {
        super(ItemStack.class, Keys.SPAWNABLE_ENTITY_TYPE);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public Value<EntityType> constructValue(EntityType defaultValue) {
        return new SpongeValueBuilder().createValue(Keys.SPAWNABLE_ENTITY_TYPE, defaultValue, EntityTypes.CREEPER);
    }

    @Override
    public boolean set(ItemStack container, EntityType value) {
        final String name = (String) EntityList.classToStringMapping.get(value.getEntityClass());
        if (EntityList.stringToIDMapping.containsKey(name)) {
            final int id = (int) EntityList.stringToIDMapping.get(name);
            if (EntityList.entityEggs.containsKey(id)) {
                if (container.hasTagCompound() && container.getTagCompound().hasKey("entity_name")) {
                    container.getTagCompound().removeTag("entity_name");
                }
                container.setItemDamage(id);
                return true;
            }
        } else {
            if (!container.hasTagCompound()) {
                container.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = container.getTagCompound();
            tag.setString("entity_name", name);
            final EntityRegistry.EntityRegistration registration = EntityRegistry.instance()
                    .lookupModSpawn((Class<? extends Entity>) value.getEntityClass(), false);
            container.setItemDamage(registration.getModEntityId());
            return true;
        }
        return false;
    }

    @Override
    public Optional<EntityType> getVal(ItemStack container) {
        System.out.print("SpongeForge");
        final Class entity = (Class) EntityList.stringToClassMapping.get(ItemMonsterPlacer.getEntityName(container));
        for (EntityType type : SpongeImpl.getRegistry().getAllOf(EntityType.class)) {
            if (type.getEntityClass().equals(entity)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public ImmutableValue<EntityType> constructImmutableValue(EntityType value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            ItemStack item = (ItemStack) container;
            item.setItemDamage(0);
            if (item.hasTagCompound() && item.getTagCompound().hasKey("entity_name", 8)) {
                item.getTagCompound().removeTag("entity_name");
            }
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
}
