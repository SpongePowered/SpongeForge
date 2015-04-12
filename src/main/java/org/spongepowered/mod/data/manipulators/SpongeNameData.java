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
package org.spongepowered.mod.data.manipulators;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.NameData;

public class SpongeNameData extends AbstractDataManipulator<NameData> implements NameData {

    private String name;
    private boolean visible;

    @Override
    public String getCustomName() {
        return this.name;
    }

    @Override
    public void setCustomName(String name) {
        this.name = checkNotNull(name);
    }

    @Override
    public boolean isCustomNameVisible() {
        return this.visible;
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String getValue() {
        return this.name;
    }

    @Override
    public void setValue(String value) {
        setCustomName(value);
    }

    @Override
    public Optional<NameData> fill(DataHolder dataHolder) {
        return fill(dataHolder, DataPriority.DATA_HOLDER);
    }

    @Override
    public Optional<NameData> fill(DataHolder dataHolder, DataPriority overlap) {
        if (dataHolder instanceof Entity) {
            if (overlap == DataPriority.DATA_HOLDER) {
                String entityName = ((Entity) dataHolder).getCustomNameTag();
                if (entityName == null) {
                    return Optional.absent();
                } else {
                    setCustomName(entityName);
                    setCustomNameVisible(((Entity) dataHolder).getAlwaysRenderNameTag());
                    return Optional.<NameData>of(this);
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<NameData> from(DataContainer container) {
        checkNotNull(container, "null container");
        if (container.contains(of("CustomName"))) {
            setCustomName(container.getString(of("CustomName")).get());
            setCustomNameVisible(container.getBoolean(of("CustomNameVisible")).get());
            return Optional.<NameData>of(this);
        }
        return Optional.absent();
    }

    @Override
    public int compareTo(NameData o) {
        return this.name.compareTo(o.getCustomName());
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("CustomName"), this.name);
        container.set(of("CustomNameVisible"), this.visible);
        return container;
    }
}
