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
package org.spongepowered.mod.mixin.forge;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.registry.SpongeGameRegistry;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.util.Hashtable;

@NonnullByDefault
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Shadow
    private static Hashtable<Integer, Class<? extends WorldProvider>> providers;
    @Shadow
    private static Hashtable<Integer, Boolean> spawnSettings;

    @Overwrite
    public static boolean registerProviderType(int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
        if (providers.containsKey(id)) {
            return false;
        }

        String worldType = "";
        switch (id) {
            case -1:
                worldType = "NETHER";
                break;
            case 0:
                worldType = "OVERWORLD";
                break;
            case 1:
                worldType = "END";
                break;
            default: // modded
                worldType = provider.getSimpleName().toLowerCase();
                worldType = worldType.replace("worldprovider", "");
                worldType = worldType.replace("provider", "");
        }
        // register dimension type
        ((SpongeGameRegistry) SpongeMod.instance.getGame().getRegistry())
                .registerEnvironment(new SpongeDimensionType(worldType, keepLoaded, provider));
        providers.put(id, provider);
        spawnSettings.put(id, keepLoaded);
        return true;
    }

}
