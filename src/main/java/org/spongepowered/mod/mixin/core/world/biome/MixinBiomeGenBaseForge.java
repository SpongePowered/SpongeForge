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
package org.spongepowered.mod.mixin.core.world.biome;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.DeferredBiomeDecorator;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.biome.IBiomeGenBase;
import org.spongepowered.common.mixin.core.world.biome.MixinBiomeGenBase;

@Mixin(value = BiomeGenBase.class, priority = 1001)
@Implements(value = @Interface(iface = IBiomeGenBase.class, prefix = "super$") )
public class MixinBiomeGenBaseForge extends MixinBiomeGenBase {

    @Shadow public BiomeDecorator theBiomeDecorator;

    @Intrinsic(displace = true)
    public BiomeGenerationSettings super$initPopulators(World world) {
        if (this.theBiomeDecorator instanceof DeferredBiomeDecorator) {
            ((DeferredBiomeDecorator) this.theBiomeDecorator).fireCreateEventAndReplace((BiomeGenBase) (Object) this);
        }
        return initPopulators(world);
    }
}
