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
package org.spongepowered.common.block.meta;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.spongepowered.api.block.tile.carrier.Beacon;
import org.spongepowered.api.block.tile.data.BeaconData;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;

public class SpongeBeaconData implements BeaconData {

    private Optional<PotionEffectType> primary;
    private Optional<PotionEffectType> secondary;

    @Override
    public Optional<PotionEffectType> getPrimaryEffect() {
        return this.primary;
    }

    @Override
    public void setPrimaryEffect(PotionEffectType effect) {
        this.primary = Optional.of(effect);
    }

    @Override
    public Optional<PotionEffectType> getSecondaryEffect() {
        return this.secondary;
    }

    @Override
    public void setSecondaryEffect(PotionEffectType effect) {
        this.secondary = Optional.of(effect);
    }

    @Override
    public void clearEffects() {
        this.primary = Optional.absent();
        this.secondary = Optional.absent();
    }

    @Override
    public Optional<Beacon> getTileEntity() {
        return Optional.absent();
    }

    @Override
    public int compareTo(BeaconData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("primary", this.primary)
                .add("secondary", this.secondary)
                .toString();
    }

}
