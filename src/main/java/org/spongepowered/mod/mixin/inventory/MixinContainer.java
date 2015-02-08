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
package org.spongepowered.mod.mixin.inventory;

import com.google.common.collect.Lists;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@NonnullByDefault
@Mixin(Container.class)
public abstract class MixinContainer {

    @SuppressWarnings("rawtypes")
    @Shadow
    protected List crafters = Lists.newArrayList();

    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getInventory();

    @SuppressWarnings("unchecked")
    @Overwrite
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        Container container = (Container) (Object) this;
        if (this.crafters.contains(p_75132_1_)) {
            // Sponge start - As we do not create a new player object on respawn, we need to update the client with changes if listener already exists
            //throw new IllegalArgumentException("Listener already listening");
            p_75132_1_.sendContainerAndContentsToPlayer(container, this.getInventory());
            container.detectAndSendChanges();
            // Sponge end
        } else {
            this.crafters.add(p_75132_1_);
            p_75132_1_.sendContainerAndContentsToPlayer(container, this.getInventory());
            container.detectAndSendChanges();
        }
    }
}
