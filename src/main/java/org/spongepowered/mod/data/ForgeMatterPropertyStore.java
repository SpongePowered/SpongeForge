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
package org.spongepowered.mod.data;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.property.store.common.AbstractBlockPropertyStore;

import java.util.Optional;

import javax.annotation.Nullable;

public class ForgeMatterPropertyStore extends AbstractBlockPropertyStore<MatterProperty> {

    private static final MatterProperty SOLID = new MatterProperty(MatterProperty.Matter.SOLID);
    private static final MatterProperty LIQUID = new MatterProperty(MatterProperty.Matter.LIQUID);
    private static final MatterProperty GAS = new MatterProperty(MatterProperty.Matter.GAS);

    public ForgeMatterPropertyStore() {
        super(true);
    }

    @Override
    protected Optional<MatterProperty> getForBlock(@Nullable Location<?> location, IBlockState block) {
        if (block.getBlock() instanceof BlockLiquid || block.getBlock() instanceof BlockFluidBase || block.getBlock() instanceof IFluidBlock) {
            return Optional.of(LIQUID);
        } else if (block.getMaterial() == Material.AIR) {
            return Optional.of(GAS);
        } else {
            return Optional.of(SOLID);
        }
    }

    @Override
    public int getPriority() {
        return 101;
    }
}
