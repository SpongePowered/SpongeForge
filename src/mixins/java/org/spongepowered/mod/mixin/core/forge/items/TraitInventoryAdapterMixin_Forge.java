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
package org.spongepowered.mod.mixin.core.forge.items;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.VanillaDoubleChestItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.EntityEquipmentInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.ReusableLensInventoryAdapaterBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.ReusableLensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import javax.annotation.Nullable;

/**
 * Mixin into all known forge {@link IItemHandler}s.
 * Implement {@link InventoryAdapter#bridge$getSlotProvider()}
 * and {@link InventoryAdapter#bridge$getRootLens()}
 * using a {@link ReusableLensProvider} or {@link LensProviderBridge}
 */
@Mixin(value = {
        CombinedInvWrapper.class,
        EntityEquipmentInvWrapper.class,
        InvWrapper.class,
        ItemStackHandler.class,
        RangedWrapper.class,
        SidedInvWrapper.class,
        VanillaDoubleChestItemHandler.class
}, priority = 999)
public abstract class TraitInventoryAdapterMixin_Forge implements ReusableLensInventoryAdapaterBridge, InventoryBridge {

    @Nullable private ReusableLens<?> impl$reusableLens = null;
    private int initializedSize;

    @Override
    public ReusableLens<?> bridge$getReusableLens() {
        if (this.impl$reusableLens == null && this.needsUpdate()) {
            this.impl$reusableLens = ReusableLens.getLens(this);
        }
        return this.impl$reusableLens;
    }

    @Nullable private SlotProvider impl$provider;
    @Nullable private Lens impl$lens;
    @Nullable private PluginContainer impl$PluginParent;

    private boolean needsUpdate() {
        boolean needsUpdate = this.bridge$getFabric().fabric$getSize() != this.initializedSize;
        if (needsUpdate) {
            this.impl$reusableLens = null;
            this.bridge$setSlotProvider(null);
            this.bridge$setLens(null);
        }
        return needsUpdate;
    }

    @Override
    public Fabric bridge$getFabric() {
        return (Fabric) this;
    }

    @Override
    public SlotProvider bridge$getSlotProvider() {
        if (this.impl$provider == null || this.needsUpdate()) {
            this.impl$provider = this.bridge$generateSlotProvider();
            this.initializedSize = this.bridge$getFabric().fabric$getSize();
            return this.impl$provider;
        }
        return this.impl$provider;
    }

    @Override
    public void bridge$setSlotProvider(final SlotProvider provider) {
        this.impl$provider = provider;
    }

    @Override
    public Lens bridge$getRootLens() {
        if (this.impl$lens == null || this.needsUpdate()) {
            this.impl$lens = this.bridge$generateLens(this.bridge$getSlotProvider());
        }
        return this.impl$lens;
    }

    @Override
    public void bridge$setLens(final Lens lens) {
        this.impl$lens = lens;
    }

    @Override
    public PluginContainer bridge$getPlugin() {
        if (this.impl$PluginParent == null) {
            this.impl$PluginParent = InventoryUtil.getPluginContainer(this);
        }
        return this.impl$PluginParent;
    }

    @Override
    public void bridge$setPlugin(final PluginContainer container) {
        this.impl$PluginParent = container;
    }
}
