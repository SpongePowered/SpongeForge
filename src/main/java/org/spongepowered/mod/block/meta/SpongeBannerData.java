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
package org.spongepowered.mod.block.meta;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.BannerData;
import org.spongepowered.api.data.types.BannerPatternShape;
import org.spongepowered.api.data.types.DyeColor;
import org.spongepowered.api.data.types.DyeColors;

import java.util.List;

public class SpongeBannerData implements BannerData {

    private DyeColor base = DyeColors.WHITE;
    private List<PatternLayer> patterns = Lists.newArrayList();

    @Override
    public DyeColor getBaseColor() {
        return this.base;
    }

    @Override
    public void setBaseColor(DyeColor color) {
        this.base = checkNotNull(color);
    }

    @Override
    public List<PatternLayer> getPatternsList() {
        return ImmutableList.copyOf(this.patterns);
    }

    @Override
    public void clearPatterns() {
        this.patterns.clear();
    }

    @Override
    public void addPatternLayer(PatternLayer pattern) {
        this.patterns.add(checkNotNull(pattern));
    }

    @Override
    public void addPatternLayer(BannerPatternShape patternShape, DyeColor color) {
        this.patterns.add(new SpongePatternLayer(checkNotNull(patternShape), checkNotNull(color)));
    }

    @Override
    public int compareTo(BannerData o) {
        return this.base.getName().compareTo(o.getBaseColor().toString());
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("Base"), this.base);
        container.set(of("Patterns"), this.patterns);
        return container;
    }

    @Override
    public Optional<BannerData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<BannerData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<BannerData> from(DataContainer container) {
        return null;
    }
}
