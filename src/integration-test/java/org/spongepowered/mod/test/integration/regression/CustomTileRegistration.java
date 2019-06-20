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
package org.spongepowered.mod.test.integration.regression;

import static org.junit.Assert.assertEquals;
import static org.spongepowered.common.regression.registry.TileEntityRegistrationTest.CORRECTLY_QUALIFIED_ID;
import static org.spongepowered.common.regression.registry.TileEntityRegistrationTest.MINECRAFT_PREFIXED_ID;
import static org.spongepowered.common.regression.registry.TileEntityRegistrationTest.UNQUALIFIED_TILE_ID;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.regression.registry.Test1Tile;
import org.spongepowered.common.regression.registry.Test2Tile;
import org.spongepowered.common.regression.registry.Test3Tile;
import org.spongepowered.common.test.RegressionTest;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.junit.TestUtils;

@RunWith(MinecraftRunner.class)
public class CustomTileRegistration extends BaseTest {

    public CustomTileRegistration(TestUtils testUtils) {
        super(testUtils);
    }

    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void registerStuff() {
        GameRegistry.registerTileEntity(Test1Tile.class, UNQUALIFIED_TILE_ID);
        GameRegistry.registerTileEntity(Test2Tile.class, CORRECTLY_QUALIFIED_ID);
        GameRegistry.registerTileEntity(Test3Tile.class, MINECRAFT_PREFIXED_ID);
    }

    /**
     * This verifies that our custom tile entity types are being registered, both as sponge mod prefixed, and
     * the rare case where a forge mod is being registered as "minecraft:" prefixed. These come into play for the
     * required mods section of schematics.
     */
    @RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeForge/issues/2785")
    @SuppressWarnings("ConstantConditions")
    @Test
    public void testGettingTileEntityTypes() {

        final TileEntityType test1type = TileEntityTypeRegistryModule.getInstance().getForClass(Test1Tile.class);
        final TileEntityType test2type = TileEntityTypeRegistryModule.getInstance().getForClass(Test2Tile.class);
        final TileEntityType test3type = TileEntityTypeRegistryModule.getInstance().getForClass(Test3Tile.class);

        @RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeForge/issues/2785",
            comment = "Specifically put, Forge has a weird case where they auto-prefix mod provided"
                      + "id's "
        )
        final String autoPrefixed = "sponge:" + UNQUALIFIED_TILE_ID;

        final TileEntityType test1StringType = TileEntityTypeRegistryModule.getInstance().getById(UNQUALIFIED_TILE_ID).get();
        final TileEntityType test2StringType = TileEntityTypeRegistryModule.getInstance().getById(CORRECTLY_QUALIFIED_ID).get();
        final TileEntityType test3StringType = TileEntityTypeRegistryModule.getInstance().getById(MINECRAFT_PREFIXED_ID).get();

        assertEquals(test1type, test1StringType);
        assertEquals(test2type, test2StringType);
        assertEquals(test3type, test3StringType);


        assertEquals(autoPrefixed, test1type.getId()); // Note that SpongeImplHooks by default will prefix based on package, so we're good.
        assertEquals("minecraft:" + UNQUALIFIED_TILE_ID, TileEntity.getKey(Test1Tile.class).toString());
        assertEquals(CORRECTLY_QUALIFIED_ID, test2type.getId());
        assertEquals(MINECRAFT_PREFIXED_ID, test3type.getId());

        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test1Tile()).getType(), test1type);
        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test2Tile()).getType(), test2type);
        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test3Tile()).getType(), test3type);
    }

}
