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
package org.spongepowered.mod;

import com.google.inject.Inject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.annotation.Nonnull;

@Plugin(id = "item_handler_test")
public class ItemHandlerTest {

    @Inject private Logger logger;

    @Listener
    public void onInit(GameInitializationEvent event) {
        this.logger.info("Enabled the IItemHandler test plugin.");
        final MyItemHandlerModifiable itemHandler = new MyItemHandlerModifiable();

        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
        itemHandler.getSlots();
        itemHandler.getStackInSlot(0);
        itemHandler.insertItem(0, ItemStack.EMPTY, false);
        itemHandler.extractItem(0, 1, false);
        itemHandler.getSlotLimit(0);
    }

    private class MyItemHandlerModifiable implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            logger.info("Hello: setStackInSlot");
        }

        @Override
        public int getSlots() {
            logger.info("Hello: getSlots");
            return 0;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            logger.info("Hello: getStackInSlot");
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            logger.info("Hello: insertItem");
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            logger.info("Hello: extractItem");
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            logger.info("Hello: getSlotLimit");
            return 0;
        }
    }
}
