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
package org.spongepowered.mod.service.persistence.builders.block.tile;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tile.carrier.Hopper;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

public class SpongeHopperBuilder extends SpongeLockableBuilder<Hopper> {

    public SpongeHopperBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Hopper> build(DataView container) throws InvalidDataException {
        Optional<Hopper> hopperOptional = super.build(container);
        if (!hopperOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a Hopper tile entity!");
        }
        Hopper hopper = hopperOptional.get();
        if (container.contains(new DataQuery("CustomName"))) {
            ((TileEntityHopper) hopper).setCustomName(container.getString(new DataQuery("CustomName")).get());
        }
        if (!container.contains(new DataQuery("TransferCooldown"))) {
            throw new InvalidDataException("The provided container does not contain the data to make a Hopper!");
        }
        // TODO
//        hopper.setTransferCooldown(container.getInt(new DataQuery("TransferCooldown")).get());
        ((TileEntityHopper) hopper).validate();
        return Optional.of(hopper);
    }
}
