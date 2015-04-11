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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.mod.util.gen.ByteArrayMutableBiomeArea;
import org.spongepowered.mod.util.gen.ChunkPrimerBuffer;

import java.util.List;
import java.util.Random;

/**
 * Similar class to {@link ChunkProviderGenerate}, but instead gets its blocks
 * from a custom chunk generator.
 */
public final class CustomChunkProviderGenerate implements IChunkProvider {

    private static final Vector2i CHUNK_AREA = new Vector2i(16, 16);

    final GeneratorPopulator generatorPopulator;
    final BiomeGenerator biomeGenerator;
    private final World world;
    private final ByteArrayMutableBiomeArea cachedBiomes;

    /**
     * Gets the chunk generator from the given generator populator and biome
     * generator.
     *
     * @param world The world to bind the chunk provider to.
     * @param generatorPopulator The generator populator.
     * @param biomeGenerator Biome generator used to generate chunks.
     * @return The chunk generator.
     * @throws IllegalArgumentException If the generator populator cannot be
     *         bound to the given world.
     */
    public static IChunkProvider of(World world, GeneratorPopulator generatorPopulator, BiomeGenerator biomeGenerator) {
        if (generatorPopulator instanceof SpongeGeneratorPopulator) {
            // Unwrap instead of wrap
            return ((SpongeGeneratorPopulator) generatorPopulator).getHandle(world);
        }
        // Wrap a custom GeneratorPopulator implementation
        return new CustomChunkProviderGenerate(world, generatorPopulator, biomeGenerator);
    }

    private CustomChunkProviderGenerate(World world, GeneratorPopulator generatorPopulator, BiomeGenerator biomeGenerator) {
        this.world = Preconditions.checkNotNull(world);
        this.generatorPopulator = Preconditions.checkNotNull(generatorPopulator);
        this.biomeGenerator = Preconditions.checkNotNull(biomeGenerator);

        // Make initially empty biome cache
        this.cachedBiomes = new ByteArrayMutableBiomeArea(Vector2i.ZERO, CHUNK_AREA);
        this.cachedBiomes.detach();
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        Random random = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        BlockFalling.fallInstantly = true;

        // Calling the events makes the Sponge-added populators fire
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, this.world, random, chunkX, chunkZ, false));
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, this.world, random, chunkX, chunkZ, false));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int chunkX, int chunkZ) {
        // This method normally places ocean monuments in existing chunks
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        BiomeGenBase biome = this.world.getBiomeGenForCoords(pos);
        @SuppressWarnings("unchecked")
        List<SpawnListEntry> creatures = biome.getSpawnableList(creatureType);
        return creatures;
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        // Maybe allow to register stronghold locations?
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        // No structure support
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {

        // Generate biomes
        this.cachedBiomes.reuse(new Vector2i(chunkX * 16, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);

        // Generate blocks
        ChunkPrimer chunkprimer = new ChunkPrimer();
        ChunkPrimerBuffer buffer = new ChunkPrimerBuffer(chunkprimer, chunkZ, chunkZ);
        this.generatorPopulator.populate((org.spongepowered.api.world.World) this.world, buffer, this.cachedBiomes.getImmutableClone());

        // Assemble chunk
        Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        System.arraycopy(this.cachedBiomes.detach(), 0, biomeArray, 0, biomeArray.length);
        chunk.generateSkylightMap();

        return chunk;
    }

    // Methods below are simply mirrors of the methods in ChunkProviderGenerate

    @Override
    public Chunk provideChunk(BlockPos blockPosIn) {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public boolean saveChunks(boolean bool, IProgressUpdate progressUpdate) {
        return true;
    }

    @Override
    public void saveExtraData() {

    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

}
