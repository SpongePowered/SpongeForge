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
package org.spongepowered.mod.entity;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;

public class SpongeEntityMeta implements CatalogType {

    public final int type;
    public final String name;

    public SpongeEntityMeta(int type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getId() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SpongeEntityMeta other = (SpongeEntityMeta) obj;
        if (this.type != other.type) {
            return false;
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("id"), this.type);
        container.set(of("name"), this.name);
        return container;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("name", this.name)
                .toString();
    }
}
