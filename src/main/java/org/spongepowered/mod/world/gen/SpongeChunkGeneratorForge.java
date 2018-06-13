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

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockFalling;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.api.data.type.StoneTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.populator.BigMushroom;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.api.world.gen.populator.DesertWell;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Fossil;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.WaterLily;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.generation.PopulatorPhaseContext;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IFlaggedPopulator;
import org.spongepowered.common.interfaces.world.gen.IGenerationPopulator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.extent.SoftBufferExtentViewDownsize;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.SpongeGenerationPopulator;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.AnimalPopulator;
import org.spongepowered.common.world.gen.populators.PlainsGrassPopulator;
import org.spongepowered.common.world.gen.populators.SnowPopulator;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.rmi.activation.ActivationGroup_Stub;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Similar class to {@link SpongeChunkGenerator}, but instead gets its blocks
 * from a custom chunk generator.
 */
public final class SpongeChunkGeneratorForge extends SpongeChunkGenerator {

    public SpongeChunkGeneratorForge(World world, GenerationPopulator generationPopulator, BiomeGenerator biomeGenerator) {
        super(world, generationPopulator, biomeGenerator);

        String chunkGeneratorName = "";
        String modId = StaticMixinForgeHelper.getModIdFromClass(generationPopulator.getClass());
        if (modId.equalsIgnoreCase("unknown")) {
            if (generationPopulator instanceof SpongeGenerationPopulator) {
                chunkGeneratorName = "chunkGenerator (" + ((SpongeGenerationPopulator) generationPopulator).getHandle(world).getClass().getSimpleName() + ")";
            } else {
                chunkGeneratorName = "chunkGenerator (" + generationPopulator.getClass().getName() + ")";
            }
        } else {
            chunkGeneratorName = "chunkGenerator (" + modId + ":" + generationPopulator.getClass().getSimpleName().toLowerCase() + ")";
        }

        this.chunkGeneratorTiming = SpongeTimingsFactory.ofSafe(chunkGeneratorName, ((IMixinWorldServer) world).getTimingsHandler().chunkPopulate);
    }

    @Override
    public void replaceBiomeBlocks(World world, Random rand, int x, int z, ChunkPrimer chunk, ImmutableBiomeVolume biomes) {
        ChunkGeneratorEvent.ReplaceBiomeBlocks event = new ChunkGeneratorEvent.ReplaceBiomeBlocks(this, x, z, chunk, world);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY)
            return;
        super.replaceBiomeBlocks(world, rand, x, z, chunk, biomes);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void populate(int chunkX, int chunkZ) {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        this.chunkGeneratorTiming.startTimingIfSync();
        this.rand.setSeed(this.world.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ this.world.getSeed());
        BlockFalling.fallInstantly = true;

        // Have to regeneate the biomes so that any virtual biomes can be passed to the populator.
        this.cachedBiomes.reuse(new Vector3i(chunkX * 16, 0, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);
        ImmutableBiomeVolume biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();

        BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        BiomeType biome = (BiomeType) this.world.getBiome(blockpos.add(16, 0, 16));

        Chunk chunk = (Chunk) this.world.getChunkFromChunkCoords(chunkX, chunkZ);

        BiomeGenerationSettings settings = getBiomeSettings(biome);

        List<Populator> populators = new ArrayList<>(this.pop);

        Populator snowPopulator = null;
        Iterator<Populator> itr = populators.iterator();
        while (itr.hasNext()) {
            Populator populator = itr.next();
            if (populator instanceof SnowPopulator) {
                itr.remove();
                snowPopulator = populator;
                break;
            }
        }

        populators.addAll(settings.getPopulators());
        if (snowPopulator != null) {
            populators.add(snowPopulator);
        }

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPre(Sponge.getCauseStackManager().getCurrentCause(), populators, chunk));

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, this.world, this.rand, chunkX, chunkZ, false));
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(this.world, this.rand, blockpos));
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(this.world, this.rand, blockpos));
        List<String> flags = Lists.newArrayList();
        Vector3i min = new Vector3i(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
        org.spongepowered.api.world.World spongeWorld = (org.spongepowered.api.world.World) this.world;
        Extent volume = new SoftBufferExtentViewDownsize(chunk.getWorld(), min, min.add(15, 255, 15), min.sub(8, 0, 8), min.add(23, 255, 23));

        for (Populator populator : populators) {
            if (!(populator instanceof PlainsGrassPopulator)) {
                if (!this.checkForgeEvent(populator, this, chunkX, chunkZ, flags, chunk)) {
                    continue;
                }
            } else {
                final PlainsGrassPopulator grassPop = (PlainsGrassPopulator) populator;

                if (!this.checkForgeEvent(grassPop.getFlowers(), this, chunkX, chunkZ, flags, chunk)) {
                    grassPop.setPopulateFlowers(false);
                }

                if (!this.checkForgeEvent(grassPop.getGrass(), this, chunkX, chunkZ, flags, chunk)) {
                    grassPop.setPopulateGrass(false);
                }

                if (!this.checkForgeEvent(grassPop.getPlant(), this, chunkX, chunkZ, flags, chunk)) {
                    grassPop.setPopulateGrass(false);
                }

                if (!grassPop.isPopulateFlowers() && !grassPop.isPopulateGrass()) {
                    continue;
                }
            }

            final PopulatorType type = populator.getType();

            if (Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPopulate(Sponge.getCauseStackManager().getCurrentCause(), populator, chunk))) {
                continue;
            }

            try (PopulatorPhaseContext context = GenerationPhase.State.POPULATOR_RUNNING.createPhaseContext()
                    .world(this.world)
                    .populator(type)) {
                context.buildAndSwitch();
                Timing timing = null;
                if (Timings.isTimingsEnabled()) {
                    timing = this.populatorTimings.get(populator.getType().getId());
                    if (timing == null) {
                        timing = SpongeTimingsFactory.ofSafe(populator.getType().getId());
                        this.populatorTimings.put(populator.getType().getId(), timing);
                    }
                    timing.startTimingIfSync();
                }
                if (populator instanceof IFlaggedPopulator) {
                    ((IFlaggedPopulator) populator).populate(spongeWorld, volume, this.rand, biomeBuffer, flags);
                } else {
                    populator.populate(spongeWorld, volume, this.rand, biomeBuffer);
                }
                if (timing != null) {
                    timing.stopTimingIfSync();
                }
            }
        }

        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(this.world, this.rand, blockpos));
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(this.world, this.rand, blockpos));
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, this.world, this.rand, chunkX, chunkZ, false));

        // If we wrapped a custom chunk provider then we should call its
        // populate method so that its particular changes are used.
        if (this.baseGenerator instanceof SpongeGenerationPopulator) {
            Timing timing = null;
            IChunkGenerator chunkGenerator = ((SpongeGenerationPopulator) this.baseGenerator).getHandle(this.world);
            if (Timings.isTimingsEnabled()) {
                IGenerationPopulator spongePopulator = (IGenerationPopulator) this.baseGenerator;
                timing = spongePopulator.getTimingsHandler();
                timing.startTimingIfSync();
            }
            chunkGenerator.populate(chunkX, chunkZ);
            if (Timings.isTimingsEnabled()) {
                timing.stopTimingIfSync();
            }
        }

        org.spongepowered.api.event.world.chunk.PopulateChunkEvent.Post event =
                SpongeEventFactory.createPopulateChunkEventPost(Sponge.getCauseStackManager().getCurrentCause(), ImmutableList.copyOf(populators), chunk);
        SpongeImpl.postEvent(event);

        BlockFalling.fallInstantly = false;
        this.chunkGeneratorTiming.stopTimingIfSync();
        ((IMixinWorldServer) spongeWorld).getTimingsHandler().chunkPopulate.stopTimingIfSync();
    }

    @SuppressWarnings("deprecation")
    private boolean checkForgeEvent(Populator populator, IChunkGenerator chunkProvider, int chunkX, int chunkZ, List<String> flags, Chunk chunk) {
        boolean village_flag = flags.contains(WorldGenConstants.VILLAGE_FLAG);
        if (populator instanceof Ore && populator instanceof WorldGenerator) {
            BlockType type = ((Ore) populator).getOreBlock().getType();
            GenerateMinable.EventType otype = null;
            if (type.equals(BlockTypes.DIRT)) {
                otype = GenerateMinable.EventType.DIRT;
            } else if (type.equals(BlockTypes.GRAVEL)) {
                otype = GenerateMinable.EventType.GRAVEL;
            } else if (type.equals(BlockTypes.STONE)) {
                BlockState state = ((Ore) populator).getOreBlock();
                Optional<StoneType> stype;
                if ((stype = state.get(Keys.STONE_TYPE)).isPresent()) {
                    StoneType stoneType = stype.get();
                    if (stoneType.equals(StoneTypes.DIORITE)) {
                        otype = GenerateMinable.EventType.DIORITE;
                    } else if (stoneType.equals(StoneTypes.ANDESITE)) {
                        otype = GenerateMinable.EventType.ANDESITE;
                    } else if (stoneType.equals(StoneTypes.GRANITE)) {
                        otype = GenerateMinable.EventType.GRANITE;
                    } else {
                        return true;
                    }
                }
            } else if (type.equals(BlockTypes.COAL_ORE)) {
                otype = GenerateMinable.EventType.COAL;
            } else if (type.equals(BlockTypes.IRON_ORE)) {
                otype = GenerateMinable.EventType.IRON;
            } else if (type.equals(BlockTypes.GOLD_ORE)) {
                otype = GenerateMinable.EventType.GOLD;
            } else if (type.equals(BlockTypes.REDSTONE_ORE)) {
                otype = GenerateMinable.EventType.REDSTONE;
            } else if (type.equals(BlockTypes.DIAMOND_ORE)) {
                otype = GenerateMinable.EventType.DIAMOND;
            } else if (type.equals(BlockTypes.LAPIS_ORE)) {
                otype = GenerateMinable.EventType.LAPIS;
            } else if (type.equals(BlockTypes.QUARTZ_ORE)) {
                otype = GenerateMinable.EventType.QUARTZ;
            } else if (type.equals(BlockTypes.EMERALD_ORE)) {
                otype = GenerateMinable.EventType.EMERALD;
            } else if (type.equals(BlockTypes.MONSTER_EGG)) {
                otype = GenerateMinable.EventType.SILVERFISH;
            }
            return otype == null || TerrainGen
                    .generateOre((World) chunk.getWorld(), this.rand, (WorldGenerator) populator, VecHelper.toBlockPos(chunk.getBlockMin()), otype);
        }
        boolean populate = true;
        boolean decorate = true;

        Populate.EventType etype = this.getForgeEventTypeForPopulator(populator, chunk);
        if (etype != null) {
            populate = TerrainGen.populate(chunkProvider, (net.minecraft.world.World) chunk.getWorld(), this.rand, chunkX, chunkZ, village_flag,
                    etype);
        }

        Decorate.EventType detype = this.getForgeDecorateEventTypeForPopulator(populator, chunk);
        if (detype != null) {
            decorate = TerrainGen.decorate((World) chunk.getWorld(), this.rand, VecHelper.toBlockPos(chunk.getBlockMin()), detype);
        }

        // TODO May need to separate this..
        return populate && decorate;
    }

    private Populate.EventType getForgeEventTypeForPopulator(Populator populator, Chunk chunk) {
        if (populator instanceof Lake) {
            if (((Lake) populator).getLiquidType().getType().equals(BlockTypes.LAVA)
                    || ((Lake) populator).getLiquidType().getType().equals(BlockTypes.FLOWING_LAVA)) {
                return Populate.EventType.LAVA;
            }
            return Populate.EventType.LAKE;
        }
        if (populator instanceof Dungeon) {
            return Populate.EventType.DUNGEON;
        }
        if (populator instanceof AnimalPopulator) {
            return Populate.EventType.ANIMALS;
        }
        if (populator instanceof SnowPopulator) {
            return Populate.EventType.ICE;
        }
        if (populator instanceof Glowstone) {
            return Populate.EventType.GLOWSTONE;
        }
        if (populator instanceof RandomBlock) {
            BlockType type = ((RandomBlock) populator).getBlock().getType();
            if (type.equals(BlockTypes.FLOWING_LAVA) || type.equals(BlockTypes.LAVA)) {
                if (chunk.getWorld().getProperties().getGeneratorType().equals(GeneratorTypes.NETHER)) {
                    if (((RandomBlock) populator).getPlacementTarget().equals(WorldGenConstants.HELL_LAVA_ENCLOSED)) {
                        return Populate.EventType.NETHER_LAVA2;
                    }

                    return Populate.EventType.NETHER_LAVA;
                }
            } else if (type.equals(BlockTypes.FIRE)) {
                if (chunk.getWorld().getProperties().getGeneratorType().equals(GeneratorTypes.NETHER)) {
                    return Populate.EventType.FIRE;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private Decorate.EventType getForgeDecorateEventTypeForPopulator(Populator populator, Chunk chunk) {
        if (populator instanceof SeaFloor) {
            BlockType type = ((SeaFloor) populator).getBlock().getType();
            if (type.equals(BlockTypes.SAND)) {
                return Decorate.EventType.SAND;
            }
            if (type.equals(BlockTypes.CLAY)) {
                return Decorate.EventType.CLAY;
            }
            if (type.equals(BlockTypes.GRAVEL)) {
                return Decorate.EventType.SAND_PASS2;
            }
        }
        if (populator instanceof Forest) {
            return Decorate.EventType.TREE;
        }
        if (populator instanceof BigMushroom) {
            return Decorate.EventType.BIG_SHROOM;
        }
        if (populator instanceof DoublePlant) {
            for (final TableEntry<DoublePlantType> entry : ((DoublePlant) populator).getPossibleTypes()) {
                if (entry instanceof WeightedObject) {
                    if (((WeightedObject) entry).get() == DoublePlantTypes.GRASS) {
                        return Decorate.EventType.GRASS;
                    } else {
                        if (((WeightedObject) entry).get() == DoublePlantTypes.SUNFLOWER) {
                            return Decorate.EventType.FLOWERS;
                        }
                    }
                }
            }
        }
        if (populator instanceof Flower) {
            return Decorate.EventType.FLOWERS;
        }
        if (populator instanceof Shrub) {
            return Decorate.EventType.GRASS;
        }
        if (populator instanceof DeadBush) {
            return Decorate.EventType.DEAD_BUSH;
        }
        if (populator instanceof WaterLily) {
            return Decorate.EventType.LILYPAD;
        }
        if (populator instanceof Mushroom) {
            return Decorate.EventType.SHROOM;
        }
        if (populator instanceof Reed) {
            return Decorate.EventType.REED;
        }
        if (populator instanceof Pumpkin) {
            return Decorate.EventType.PUMPKIN;
        }
        if (populator instanceof Cactus) {
            return Decorate.EventType.CACTUS;
        }
        if (populator instanceof DesertWell) {
            return Decorate.EventType.DESERT_WELL;
        }
        if (populator instanceof BlockBlob) {
            final BlockType type = ((BlockBlob) populator).getBlock().getType();
            if (type.equals(BlockTypes.MOSSY_COBBLESTONE)) {
                return Decorate.EventType.ROCK;
            }
        }
        if (populator instanceof RandomBlock) {
            final BlockType type = ((RandomBlock) populator).getBlock().getType();

            if (type.equals(BlockTypes.FLOWING_WATER) || type.equals(BlockTypes.WATER)) {
                return Decorate.EventType.LAKE_WATER;
            } else if (type.equals(BlockTypes.FLOWING_LAVA) || type.equals(BlockTypes.LAVA)) {
                if (!chunk.getWorld().getProperties().getGeneratorType().equals(GeneratorTypes.NETHER)) {
                    return Decorate.EventType.LAKE_LAVA;
                }
            }
        }
        if (populator instanceof Fossil) {
            return Decorate.EventType.FOSSIL;
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("world", this.world)
                .toString();
    }
}
