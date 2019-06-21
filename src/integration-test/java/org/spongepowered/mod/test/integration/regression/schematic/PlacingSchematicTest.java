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
package org.spongepowered.mod.test.integration.regression.schematic;

import static org.junit.Assert.assertEquals;

import com.flowpowered.math.vector.Vector3i;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.regression.registry.Test1Tile;
import org.spongepowered.common.regression.registry.Test2Tile;
import org.spongepowered.common.regression.registry.Test3Tile;
import org.spongepowered.common.regression.registry.TileEntityRegistrationTest;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.junit.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 */
@RunWith(MinecraftRunner.class)
public class PlacingSchematicTest extends BaseTest {

    public PlacingSchematicTest(TestUtils testUtils) {
        super(testUtils);
    }

    @BeforeClass
    @SuppressWarnings("deprecation")
    public static void setupSchematic() {
        // Note that these are tested against in CustomTileRegistration test
        GameRegistry.registerTileEntity(Test1Tile.class, TileEntityRegistrationTest.UNQUALIFIED_TILE_ID);
        GameRegistry.registerTileEntity(Test2Tile.class, TileEntityRegistrationTest.CORRECTLY_QUALIFIED_ID);
        GameRegistry.registerTileEntity(Test3Tile.class, TileEntityRegistrationTest.MINECRAFT_PREFIXED_ID);
    }

    @Test
    public void createSchematic() throws Throwable {
        this.testUtils.runOnMainThread(() -> {
            final WorldArchetype schematics = WorldArchetype.builder().from(WorldArchetypes.THE_VOID)
                .gameMode(GameModes.CREATIVE)
                .build(SpongeImpl.ECOSYSTEM_ID + ":schematica", "Schematica");
            final WorldProperties properties = Sponge.getServer().createWorldProperties("schematica", schematics);
            final World schematica = Sponge.getServer().loadWorld(properties).get();
            final Player player = this.testUtils.getThePlayer();
            player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
            player.offer(Keys.IS_FLYING, true);
            player.setLocation(new Location<>(schematica, 0, 64, 0));
            final DataContainer container;
            try (final GZIPInputStream stream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("placement.schematic"))) {
                container = DataFormats.NBT.readFrom(stream);
            } catch (Exception e) {
                throw new AssertionError("Failed to load schematic file", e);
            }
            final Schematic toPlace = DataTranslators.SCHEMATIC.translate(container);
            final Location<World> location = player.getLocation();
            toPlace.apply(location, BlockChangeFlags.NONE);

            final Vector3i blockMin = toPlace.getBlockMin();
            final Vector3i blockMax = toPlace.getBlockMax();

            final Vector3i origin = location.getBlockPosition();

            // A note about this:
            /*
                The recreated schematic should always equal the deserialized
                schematic since the Schematic will verify that it's backing
                volume is that of an ArchetypeVolume. The reason for this is
                that if it were the case it retains the backed ArchetypeVolume,
                duplication of maps and lists are retained, so the object
                memory footprint doubles for the amount of TileEntities and Entities
                that are stored within.
             */
            final ArchetypeVolume archetypeVolume = schematica.createArchetypeVolume(origin.add(blockMin), origin.add(blockMax), origin);
            final Schematic recreated = Schematic.builder()
                .volume(archetypeVolume)
                .metaValue(Schematic.METADATA_AUTHOR, "gabizou")
                .metaValue(Schematic.METADATA_NAME, "placement")
                .blockPaletteType(PaletteTypes.LOCAL_BLOCKS)
                .build();
            assertEquals("The recreated schematic from world should equal the de-serialized schematic", toPlace, recreated);
            final Schematic deserialized = DataTranslators.SCHEMATIC.translate(DataTranslators.SCHEMATIC.translate(recreated));
            /*
                This assertion proves the following:
                - Deserializing a schematic and pasting it into the world works
                - Creating a new schematic with the *SAME* information as the original schematic (metadata) at the location
                  and then "translating" and "re-translating back" into a Schematic will yeild the an equal Schematic.
                - The full circle of creating, serializing, deserializing, and translating produces the same
                  schematic as if you loaded a new schematic from file.
                - Schematics that contain extra data attached to tile entities are retained, but the engine
                  may not respect those when a new schematic is created from the placed original schematic
             */
            assertEquals("The de-serialized recreated schematic should equal the original deserialized schematic", toPlace, deserialized);
            return schematica;
        });
        this.testUtils.waitForWorldChunks();
        this.testUtils.listen(new StandaloneEventListener<>(ChangeBlockEvent.Post.class, (event) -> {

        }));
        this.testUtils.runOnMainThread(() -> {
            final Player thePlayer = this.testUtils.getThePlayer();
        });

        File inputFile = new File(this.getClass().getClassLoader().getResource("placement.schematic").getFile());
        DataContainer container = DataFormats.NBT.readFrom(new GZIPInputStream(new FileInputStream(inputFile)));

        // Up next: test loading and saving v1 schematics.
    }
}

