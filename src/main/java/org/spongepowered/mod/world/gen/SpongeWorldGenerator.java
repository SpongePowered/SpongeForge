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

package org.spongepowered.mod.world.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class SpongeWorldGenerator implements WorldGenerator {

    /**
     * May be mutable or immutable, but must be changed to be mutable before the
     * first call to {@link #getPopulators()}.
     */
    private List<Populator> populators;
    private final WorldServer world;
    private long seed;
    @Nullable
    private BiomeGenerator biomeGenerator;
    @Nullable
    private GeneratorPopulator generatorPopulator;

    public SpongeWorldGenerator(WorldServer world, ImmutableList<Populator> populators) {
        this.world = Preconditions.checkNotNull(world, "world");
        this.populators = Preconditions.checkNotNull(populators, "populators");
        this.seed = world.getSeed();
    }

    @Override
    public boolean areMapFeaturesEnabled() {
        return this.world.getWorldInfo().isMapFeaturesEnabled();
    }

    @Override
    public List<Populator> getPopulators() {
        if (!(this.populators instanceof ArrayList)) {
            // Need to make a copy to make the populators mutable
            // Normally, we don't copy the populators in the constructor,
            // that would be a waste of memory if there are a huge number of
            // populators registered
            this.populators = new ArrayList<Populator>(this.populators);
        }
        return this.populators;
    }

    /**
     * Gets the list of populators. The list is not guaranteed to be mutable.
     * Use with care.
     *
     * @return The list.
     */
    public List<Populator> accessPopulators() {
        return this.populators;
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        if (this.biomeGenerator == null) {
            return SpongeBiomeGenerator.of(this.world.getWorldChunkManager());
        }
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = Preconditions.checkNotNull(biomeGenerator);
    }

    @Override
    public GeneratorPopulator getGeneratorPopulator() {
        if (this.generatorPopulator == null) {
            ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.world.getChunkProvider();
            return SpongeGeneratorPopulator.of(this.world, chunkProviderServer.serverChunkGenerator);
        }
        return this.generatorPopulator;
    }

    @Override
    public void setGeneratorPopulator(GeneratorPopulator generator) {
        this.generatorPopulator = Preconditions.checkNotNull(generator);
    }

    public WorldServer getWorld() {
        return this.world;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

}
