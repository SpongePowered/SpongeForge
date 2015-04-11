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
import net.minecraft.inventory.IInventory;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tile.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

import java.util.List;

public class SpongeLockableBuilder<T extends TileEntityCarrier> extends AbstractTileBuilder<T> {

    public SpongeLockableBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> build(DataView container) throws InvalidDataException {
        Optional<T> lockOptional = super.build(container);
        if (!lockOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a lockable tile entity!");
        }
        TileEntityCarrier lockable = lockOptional.get();
        if (!container.contains(new DataQuery("Contents"))) {
            throw new InvalidDataException("The provided container does not contain the data to make a lockable tile entity!");
        }
        SerializationService service = this.game.getServiceManager().provide(SerializationService.class).get();
        List<DataView> contents = container.getViewList(new DataQuery("Contents")).get();
        for (DataView content: contents) {
            net.minecraft.item.ItemStack stack =
                    (net.minecraft.item.ItemStack) content.getSerializable(new DataQuery("Item"), ItemStack.class, service).get();
            ((IInventory) lockable).setInventorySlotContents(content.getInt(new DataQuery("Slot")).get(), stack);
        }
        // TODO
//        if (container.contains(new DataQuery("Lock"))) {
//            LockableData lock = new SpongeLocableData
//            lockable.setLockableData(container.getString(new DataQuery("Lock")).get());
//        }
        return Optional.of((T) lockable);
    }
}
