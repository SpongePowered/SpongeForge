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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.FuseData;

public class SpongeFuseData extends AbstractDataManipulator<FuseData> implements FuseData {

    private int fuse = 0;

    public SpongeFuseData() {
    }

    @Override
    public int getFuseDuration() {
        return this.fuse;
    }

    @Override
    public void setFuseDuration(int fuseTicks) {
        checkArgument(fuseTicks >= this.getMinValue(), "Must be greater than the min value!");
        checkArgument(fuseTicks <= this.getMaxValue(), "Must be less than the max value!");
        this.fuse = fuseTicks;
    }

    @Override
    public Integer getMinValue() {
        return 0;
    }

    @Override
    public Integer getMaxValue() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer getValue() {
        return this.fuse;
    }

    @Override
    public void setValue(Integer value) {
        this.setFuseDuration(checkNotNull(value, "Can not accept null values!"));
    }

    @Override
    public Optional<FuseData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<FuseData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<FuseData> from(DataContainer container) {
        return null;
    }

    @Override
    public int compareTo(FuseData o) {
        return o.getFuseDuration() - this.fuse;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(DataQuery.of("Fuse"), this.fuse);
        return container;
    }
}
