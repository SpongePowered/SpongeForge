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
package org.spongepowered.mod.world.gen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link WorldGenerator}.
 *
 */
public final class SpongeWorldGenerator implements WorldGenerator {

    /**
     * Holds the populators. May be mutable or immutable, but must be changed to
     * be mutable before the first call to {@link #getPopulators()}.
     */
    private List<Populator> populators;
    /**
     * Holds the generator populators. May be mutable or immutable, but must be
     * changed to be mutable before the first call to
     * {@link #getGeneratorPopulators()}.
     */
    private List<GeneratorPopulator> generatorPopulators;
    private BiomeGenerator biomeGenerator;
    private GeneratorPopulator baseGenerator;

    private boolean biomeGeneratorChanged;
    private boolean baseGeneratorChanged;

    public SpongeWorldGenerator(BiomeGenerator biomeGenerator, GeneratorPopulator baseGenerator,
            List<GeneratorPopulator> generatorPopulators, List<Populator> populators) {
        this.biomeGenerator = Preconditions.checkNotNull(biomeGenerator, "biomeGenerator");
        this.baseGenerator = Preconditions.checkNotNull(baseGenerator, "baseGenerator");

        // Note that ImmutableList.copyOf returns actually the list itself if it
        // is already immutable
        this.populators = ImmutableList.copyOf(populators);
        this.generatorPopulators = ImmutableList.copyOf(generatorPopulators);
    }

    @Override
    public List<GeneratorPopulator> getGeneratorPopulators() {
        if (!(this.generatorPopulators instanceof ArrayList)) {
            // Need to make a copy to make the populators mutable
            this.generatorPopulators = new ArrayList<GeneratorPopulator>(this.generatorPopulators);
        }
        return this.generatorPopulators;
    }

    @Override
    public List<Populator> getPopulators() {
        if (!(this.populators instanceof ArrayList)) {
            // Need to make a copy to make the populators mutable
            this.populators = new ArrayList<Populator>(this.populators);
        }
        return this.populators;
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        Preconditions.checkState(!this.biomeGeneratorChanged,
                "Another plugin already set the biome generator to " + this.biomeGenerator.getClass().getName());

        this.biomeGenerator = Preconditions.checkNotNull(biomeGenerator);
        this.biomeGeneratorChanged = true;
    }

    @Override
    public GeneratorPopulator getBaseGeneratorPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGeneratorPopulator(GeneratorPopulator generator) {
        Preconditions.checkState(!this.baseGeneratorChanged,
                "Another plugin already set the base generator to " + this.biomeGenerator.getClass().getName());

        this.baseGenerator = Preconditions.checkNotNull(generator);
        this.baseGeneratorChanged = true;
    }

}
