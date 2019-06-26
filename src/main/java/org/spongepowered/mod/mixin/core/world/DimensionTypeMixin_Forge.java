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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;

@Mixin(value = DimensionType.class, priority = 1002, remap = false)
public abstract class DimensionTypeMixin_Forge implements DimensionTypeBridge {

    @Shadow private boolean shouldLoadSpawn;

    /**
     * @author blood - September 10th, 2016
     * @author gabizou - January 25th, 2019
     *
     * @reason - Uses forge's var for determining whether a dimension type should generate spawn.
     * @update - Forge's variable is no longer to determine whether spawn is generated on load, it's
     * to determine whether the spawn needs to remain loaded to the chunk ticket manager, or whether
     * the world can be unloaded.
     */
    @Override
    public boolean bridge$shouldKeepSpawnLoaded() {
        return this.shouldLoadSpawn;
    }
}
