/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
import static org.spongepowered.api.service.persistence.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.block.tile.Banner;
import org.spongepowered.api.block.tile.data.BannerData;
import org.spongepowered.api.block.tile.data.BannerPatternShape;
import org.spongepowered.api.item.DyeColor;
import org.spongepowered.api.item.DyeColors;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;

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
    public List<PatternLayer> getPatternList() {
        return ImmutableList.copyOf(this.patterns);
    }

    @Override
    public void clearPattern() {
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
    public Optional<Banner> getTileEntity() {
        return Optional.absent();
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
}
