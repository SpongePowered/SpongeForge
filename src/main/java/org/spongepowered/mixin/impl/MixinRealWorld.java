/**
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
package org.spongepowered.mixin.impl;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.Block;
import org.spongepowered.api.world.World;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;
import org.spongepowered.wrapper.BlockWrapper;

@Mixin(net.minecraft.world.World.class)
public abstract class MixinRealWorld implements World {
    @Shadow protected WorldInfo worldInfo;
    @Shadow public WorldProvider provider;

    @Shadow(prefix = "shadow$") public abstract net.minecraft.block.Block shadow$getBlock(int x, int y, int z);

    @Override
    public String getName() {
        return worldInfo.getWorldName() + "_" + provider.getDimensionName().toLowerCase().replace(' ', '_');
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return new BlockWrapper(this, x, y, z);
    }
}
