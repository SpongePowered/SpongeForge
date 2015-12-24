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

import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.SpongeWorldCreationSettingsBuilder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Optional;

@NonnullByDefault
@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {

    @Shadow private static Hashtable<Integer, Class<? extends WorldProvider>> providers;
    @Shadow private static Hashtable<Integer, Boolean> spawnSettings;
    @Shadow private static ArrayList<Integer> unloadQueue;
    @Shadow private static Hashtable<Integer, WorldServer> worlds;

    @Overwrite
    public static boolean registerProviderType(int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
        if (providers.containsKey(id)) {
            return false;
        }

        String worldType;
        switch (id) {
            case -1:
                worldType = "nether";
                break;
            case 0:
                worldType = "overworld";
                break;
            case 1:
                worldType = "the_end";
                break;
            default: // modded
                worldType = provider.getSimpleName().toLowerCase();
                worldType = worldType.replace("worldprovider", "");
                worldType = worldType.replace("provider", "");
        }

        // Grab provider name if available
        try {
            WorldProvider worldProvider = provider.newInstance();
            worldType = worldProvider.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
        } catch (Exception e) {
            // ignore
        }

        // register dimension type
        DimensionRegistryModule.getInstance().registerAdditionalCatalog(new SpongeDimensionType(worldType, keepLoaded, provider, id));
        providers.put(id, provider);
        if (id == 1) {
            // TODO - make this configurable
            keepLoaded = true; // keep end loaded for plugins
        }
        spawnSettings.put(id, keepLoaded);
        return true;
    }

    @Inject(method = "unregisterProviderType(I)[I", at = @At(value = "RETURN", ordinal = 1))
    private static void onUnregisterProvider(int id, CallbackInfoReturnable<int[]> cir) {
        DimensionRegistryModule.getInstance().unregisterProvider(id);
    }

    @Overwrite
    public static boolean shouldLoadSpawn(int dim) {
        final WorldServer worldServer = DimensionManager.getWorld(dim);
        final SpongeConfig<SpongeConfig.WorldConfig> worldConfig = ((IMixinWorld) worldServer).getWorldConfig();
        if (worldConfig.getConfig().isConfigEnabled()) {
            return worldConfig.getConfig().getWorld().getKeepSpawnLoaded();
        }
        // Don't use configs at this point, use spawn settings in the provider type
        int id = DimensionManager.getProviderType(dim);
        return spawnSettings.containsKey(id) && spawnSettings.get(id);
    }

    @Overwrite
    public static void unloadWorld(int id) {
        WorldServer world = DimensionManager.getWorld(id);
        if (world == null || (world.playerEntities.isEmpty() && !((World) world).doesKeepSpawnLoaded())) {
            // Purposely let a null world through so Forge can issue a warning
            unloadQueue.add(id);
        }
    }

    @Overwrite
    public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
        for (int id : unloadQueue) {
            WorldServer w = worlds.get(id);
            if (w != null) {
                Sponge.getGame().getServer().unloadWorld((World) w);
            } else {
                FMLLog.warning("Unexpected world unload - world %d is already unloaded", id);
            }
        }
        unloadQueue.clear();
    }

    @Overwrite
    public static void initDimension(int dim) {
        if (dim == 0) {
            return;
        }

        int providerId = 0;
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null) {
            throw new RuntimeException("Error during initDimension. Cannot Hotload Dim: " + dim + ", Overworld is not Loaded!");
        }
        try {
            providerId = DimensionManager.getProviderType(dim);
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Error during initDimension. Cannot Hotload Dim: " + e.getMessage());
            return; // If a provider hasn't been registered then we can't hotload the dim
        }

        final WorldProvider provider = WorldProvider.getProviderForDimension(dim);
        final WorldCreationSettings.Builder builder = SpongeImpl.getRegistry().createBuilder(WorldCreationSettings.Builder.class)
                .dimension(((Dimension) provider).getType())
                .name(provider.getDimensionName())
                .keepsSpawnLoaded(spawnSettings.get(providerId));
        ((SpongeWorldCreationSettingsBuilder) builder).dimensionId(dim).isMod(true);
        final WorldCreationSettings settings = builder.build();
        final Optional<WorldProperties> optWorldProperties = Sponge.getGame().getServer().createWorldProperties(settings);
        if (!optWorldProperties.isPresent()) {
            SpongeImpl.getLogger().error("Could not initialize world [" + settings.getWorldName() + "]. Failed to create properties.");
            return;
        }
        final Optional<World> optWorld = Sponge.getGame().getServer().loadWorld(optWorldProperties.get());
        if (!optWorld.isPresent()) {
            SpongeImpl.getLogger().error("Error during initDimension. Cannot Hotload Dim: " + dim + " for provider " + provider.getClass().getName());
        }
    }
}
