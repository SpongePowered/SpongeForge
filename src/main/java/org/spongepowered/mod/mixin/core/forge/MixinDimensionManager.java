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
package org.spongepowered.mod.mixin.core.forge;

import com.google.common.base.Optional;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.SpongeWorldBuilder;

import java.util.ArrayList;
import java.util.Hashtable;

@NonnullByDefault
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Shadow private static Hashtable<Integer, Class<? extends WorldProvider>> providers;
    @Shadow private static Hashtable<Integer, Boolean> spawnSettings;
    @Shadow private static ArrayList<Integer> unloadQueue;

    @Overwrite
    public static boolean registerProviderType(int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
        if (providers.containsKey(id)) {
            return false;
        }

        String worldType;
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
        ((SpongeGameRegistry) Sponge.getGame().getRegistry())
                .registerDimensionType(new SpongeDimensionType(worldType, keepLoaded, provider, id));
        providers.put(id, provider);
        if (id == 1) {
            // TODO - make this configurable
            keepLoaded = true; // keep end loaded for plugins
        }
        spawnSettings.put(id, keepLoaded);
        return true;
    }

    @Overwrite
    public static void unloadWorld(int id) {
        WorldServer world = DimensionManager.getWorld(id);
        if (world == null || (world.playerEntities.isEmpty() && !((org.spongepowered.api.world.World) world).doesKeepSpawnLoaded())) {
            // Purposely let a null world through so Forge can issue a warning
            unloadQueue.add(id);
        }
    }

    @Overwrite
    public static void initDimension(int dim) {
        if (dim == 0) {
            return;
        }

        int providerId = 0;
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null) {
            throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
        }
        try {
            providerId = DimensionManager.getProviderType(dim);
        } catch (Exception e) {
            System.err.println("Cannot Hotload Dim: " + e.getMessage());
            return; // If a provider hasn't been registered then we can't hotload the dim
        }
        WorldBuilder builder = Sponge.getSpongeRegistry().createWorldBuilder();
        builder = builder.dimensionType(Sponge.getSpongeRegistry().getDimensionTypeFromProvider(providerId))
                .keepsSpawnLoaded(spawnSettings.get(providerId)).generator(GeneratorTypes.DEFAULT); 
        Optional<org.spongepowered.api.world.World> world = ((SpongeWorldBuilder) builder).dimensionId(dim).build();
        // load world
        if (world.isPresent()) {
            Sponge.getGame().getServer().loadWorld(world.get().getProperties());
        }
    }

}
