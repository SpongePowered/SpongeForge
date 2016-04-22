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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.processor.data.item.SpawnableDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class ForgeSpawnableDataProcessor extends SpawnableDataProcessor {

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            ItemStack item = (ItemStack) (Object) container;
            item.setItemDamage(0);
            if (item.hasTagCompound() && item.getTagCompound().hasKey(NbtDataUtil.FORGE_ENTITY_TYPE, NbtDataUtil.TAG_STRING)) {
                item.getTagCompound().removeTag(NbtDataUtil.FORGE_ENTITY_TYPE);
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    @Override
    public boolean set(ItemStack itemStack, EntityType value) {
        final String name = EntityList.classToStringMapping.get(value.getEntityClass());
        if (EntityList.stringToIDMapping.containsKey(name)) {
            final int id = EntityList.stringToIDMapping.get(name);
            if (EntityList.entityEggs.containsKey(id)) {
                itemStack.setItemDamage(id);
                return true;
            }
        } else {
            if (!itemStack.hasTagCompound()) {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = itemStack.getTagCompound();
            tag.setString(NbtDataUtil.FORGE_ENTITY_TYPE, name);
            final EntityRegistry.EntityRegistration registration = EntityRegistry.instance()
                    .lookupModSpawn((Class<? extends Entity>) value.getEntityClass(), false);
            itemStack.setItemDamage(registration.getModEntityId());
            return true;
        }
        return false;
    }

    @Override
    public Optional<EntityType> getVal(ItemStack itemStack) {
        final Class<? extends Entity> entity = EntityList.stringToClassMapping.get(ItemMonsterPlacer.getEntityName(itemStack));
        for (EntityType type : SpongeImpl.getRegistry().getAllOf(EntityType.class)) {
            if (type.getEntityClass().equals(entity)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
