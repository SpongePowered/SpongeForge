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

import com.flowpowered.math.vector.Vector3d;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.junit.TestUtils;
import org.spongepowered.mod.test.integration.RegressionTest;

import java.util.Optional;

@RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeCommon/issues/1945")
@RunWith(MinecraftRunner.class)
public class BreakBlockUsedItem extends BaseTest {

    public BreakBlockUsedItem(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void testCauseHasUsedItem() throws Throwable {
        ItemStack stack = ItemStack.of(ItemTypes.IRON_INGOT, 1);

        Vector3d position = this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, stack);
            return this.testUtils.getThePlayer().getPosition().add(0, -1, 1);
        });
        this.testUtils.waitForInventoryPropagation();

        // Look at the ground in front of us
        this.client.lookAt(position);

        // There might be unrelated block changes events in the world,
        // so we explicitly check that our handler was called with the right one
        // at some point
        final boolean[] gotPlayerEvent = new boolean[1];

        this.testUtils.listenOneShot(() -> this.client.leftClick(), new StandaloneEventListener<>(ChangeBlockEvent.Break.class, (event) -> {

            if (event.getTransactions().stream().map(t -> t.getOriginal().getPosition()).noneMatch(p -> p.equals(position.toInt()))) {
                return;
            }

            gotPlayerEvent[0] = true;
            Assert.assertThat("Cause doesn't contain EventConextKeys.USED_ITEM", event.getCause().getContext().get(EventContextKeys.USED_ITEM),
                    Matchers.equalTo(Optional.of(stack.createSnapshot())));

        }));
        Assert.assertTrue("ChangeBlockEvent.Break was not fired!", gotPlayerEvent[0]);
    }
}
