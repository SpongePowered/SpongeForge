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

import static org.junit.Assert.assertTrue;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.test.RegressionTest;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.junit.TestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(MinecraftRunner.class)
@RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeCommon/issues/2144")
public class FlowerPotDropTest extends BaseTest {

    public FlowerPotDropTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void testFlowerPotPlacement() throws Throwable {
        ItemStack stack = ItemStack.of(ItemTypes.IRON_INGOT, 1);

        Vector3d position = this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, stack);
            return this.testUtils.getThePlayer().getPosition().add(0, -1, 1);
        });
        this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().getWorld().setBlock(position.toInt(), BlockTypes.FLOWER_POT.getDefaultState());
        });
        this.testUtils.waitForInventoryPropagation();

        // Look at the ground in front of us
        this.client.lookAt(position);

        // There might be unrelated block changes events in the world,
        // so we explicitly check that our handler was called with the right one
        // at some point
        final AtomicBoolean gotPlayerEvent = new AtomicBoolean(false);

        this.testUtils.listenOneShot(() -> this.client.leftClick(), new StandaloneEventListener<>(ConstructEntityEvent.Pre.class, (event) -> {
            final Cause cause = event.getCause();
            final LocatableBlock blockBeingDropped = cause.first(LocatableBlock.class)
                .orElseThrow(() -> new AssertionError("LocatableBlock not part of Cause for Flowerpot"));
            final Optional<BlockSnapshot> blockSnapshot = cause.getContext().get(EventContextKeys.BLOCK_HIT);
            if (!blockSnapshot.isPresent()) {
                return;
            }
            final BlockSnapshot hitBlock = blockSnapshot.get();
            final Location<World> hitLocation = hitBlock.getLocation().get();
            final Vector3i droppedPosition = blockBeingDropped.getPosition();
            final boolean isDroppedPotentiallyOrigin = droppedPosition.getX() == 0 || droppedPosition.getZ() == 0;
            final boolean isHitSameAsDropped = (hitLocation.getBlockX() == droppedPosition.getX() || hitLocation.getBlockZ() == droppedPosition.getZ());
            final Vector3i targetPosition = event.getTransform().getPosition().toInt();
            final boolean isTargetEqualToDropped = targetPosition.getX() != droppedPosition.getX() || targetPosition.getZ() != droppedPosition.getZ();
            assertTrue("Positions do not match", isDroppedPotentiallyOrigin && isHitSameAsDropped && !isTargetEqualToDropped);
            gotPlayerEvent.set(true);
        }));
        assertTrue("ChangeBlockEvent.Break was not fired!", gotPlayerEvent.get());
    }
}
