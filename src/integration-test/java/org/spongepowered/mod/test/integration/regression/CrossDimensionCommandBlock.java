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

import static org.hamcrest.MatcherAssert.assertThat;

import com.flowpowered.math.vector.Vector3d;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.junit.TestUtils;
import org.spongepowered.mod.test.integration.RegressionTest;

import java.util.Optional;

@RunWith(MinecraftRunner.class)
@RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeCommon/issues/1718")
public class CrossDimensionCommandBlock extends BaseTest  {

    public CrossDimensionCommandBlock(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void testUseCommandBlock() throws Throwable {

        Location<World> targetPosition = this.testUtils.runOnMainThread(() -> {

            WorldProperties worldProperties = Sponge.getServer().createWorldProperties("the_command_block_world",
                    WorldArchetype.builder().commandsAllowed(true).difficulty(Difficulties.PEACEFUL)
                            .dimension(DimensionTypes.OVERWORLD)
                            .enabled(true)
                            .gameMode(GameModes.CREATIVE)
                            .generator(GeneratorTypes.FLAT)
                            .build("mctester:command_block_world", "Command block World"));

            World world = Sponge.getServer().loadWorld(worldProperties).get();

            this.testUtils.getThePlayer().setLocation(world.getSpawnLocation());
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.COMMAND_BLOCK, 1));

            return this.testUtils.getThePlayer().getLocation().add(0, 1, 1);
        });
        this.testUtils.waitForWorldChunks();
        this.testUtils.waitForInventoryPropagation();

        this.client.lookAt(targetPosition.getPosition());

        if (!this.testUtils.runOnMainThread(targetPosition::getBlockType).equals(BlockTypes.AIR)) {
            this.client.leftClick();
        }

        // Right click once to place the command block, then right click again to open it
        this.client.rightClick();
        this.client.rightClick();

        Optional<String> guiClass = this.client.getOpenGuiClass();

        assertThat("Failed to open command block gui!", guiClass, Matchers.equalTo(Optional.of("net.minecraft.client.gui.GuiCommandBlock")));
    }

}
