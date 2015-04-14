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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinWorldType;
import org.spongepowered.mod.service.persistence.NbtTranslator;
import org.spongepowered.mod.world.gen.SpongeBiomeGenerator;
import org.spongepowered.mod.world.gen.SpongeGeneratorPopulator;
import org.spongepowered.mod.world.gen.SpongeWorldGenerator;

@NonnullByDefault
@Mixin(WorldType.class)
public abstract class MixinWorldType implements GeneratorType, IMixinWorldType {

    @Shadow private String worldType;
    @Shadow private int worldTypeId;

    @Override
    public String getId() {
        return this.worldType;
    }

    @Shadow
    public abstract WorldChunkManager getChunkManager(net.minecraft.world.World world);

    @Shadow
    public abstract IChunkProvider getChunkGenerator(net.minecraft.world.World world, String options);

    @Override
    public String getName() {
        return this.worldType;
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft stores the generator settings as a string. For the flat
        // world, they use a custom format, for WorldType.CUSTOMIZED they use
        // a serialized JSON string
        if ((Object) this == WorldType.FLAT) {
            String defaultSettings = FlatGeneratorInfo.getDefaultFlatGenerator().toString();
            MemoryDataContainer container = new MemoryDataContainer();
            container.set(STRING_VALUE, defaultSettings);
            return container;
        }
        if ((Object) this == WorldType.CUSTOMIZED) {
            // They easiest way to go from ChunkProviderSettings to
            // DataContainer
            // is via json and NBT
            try {
                String jsonString = ChunkProviderSettings.Factory.func_177865_a("").toString();
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(jsonString);
                return NbtTranslator.getInstance().translateFrom(nbt);
            } catch (NBTException e) {
                AssertionError error = new AssertionError("Failed to parse default settings of CUSTOMIZED world type");
                error.initCause(e);
                throw error;
            }
        }
        return new MemoryDataContainer();
    }

    @Override
    public WorldGenerator createGenerator(World world) {
        return this.createGeneratorFromString(world, "");
    }

    @Override
    public SpongeWorldGenerator createGenerator(World world, DataContainer settings) {
        // Minecraft uses a string for world generator settings
        // This string can be a JSON string, or be a string of a custom format

        // Try to convert to custom format
        Optional<String> asString = settings.getString(STRING_VALUE);
        if (asString.isPresent()) {
            return this.createGeneratorFromString(world, asString.get());
        }

        // Convert to JSON
        String json = ""; // TODO how to convert datacontainer to JSON?
        return this.createGeneratorFromString(world, json);
    }

    private SpongeWorldGenerator createGeneratorFromString(World world, String settings) {
        net.minecraft.world.World mcWorld = (net.minecraft.world.World) world;
        IChunkProvider chunkProvider = this.getChunkGenerator(mcWorld, settings);
        WorldChunkManager chunkManager = this.getChunkManager(mcWorld);

        return new SpongeWorldGenerator(
                SpongeBiomeGenerator.of(chunkManager),
                SpongeGeneratorPopulator.of((WorldServer) world, chunkProvider),
                ImmutableList.<GeneratorPopulator> of(),
                ImmutableList.<Populator> of());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getName().hashCode();
        result = prime * result + worldTypeId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldType)) {
            return false;
        }

        WorldType other = (WorldType) obj;
        if (!this.getName().equals(other.getWorldTypeName())) {
            return false;
        }

        if (this.worldTypeId != other.getWorldTypeID()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
