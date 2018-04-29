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
import com.flowpowered.math.vector.Vector3i;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.junit.TestUtils;

import java.util.Optional;
import java.util.UUID;

@RunWith(MinecraftRunner.class)
public class TntExplosionTest extends BaseTest {

    public TntExplosionTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void testTnt() throws Throwable {
        Vector3d targetPos = this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().offer(Keys.GAME_MODE, GameModes.CREATIVE);
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.TNT, 1));

            return this.testUtils.getThePlayer().getLocation().add(0, -1, 1).getPosition();
        });

        this.testUtils.waitForInventoryPropagation();

        Vector3i firstBlock = targetPos.toInt();
        Vector3i secondBlock = firstBlock.add(1, 0, 0);

        this.client.lookAt(firstBlock.toDouble());
        this.client.rightClick();

        this.client.lookAt(secondBlock.toDouble());
        this.client.rightClick();

        UUID playerId = this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.FLINT_AND_STEEL, 1));
            return this.testUtils.getThePlayer().getUniqueId();
        });

        this.testUtils.waitForInventoryPropagation();

        this.testUtils.listen(new StandaloneEventListener<>(ExplosionEvent.Detonate.class, (event) -> {
            if (!event.getExplosion().getSourceExplosive().map(e -> e instanceof PrimedTNT).orElse(false)) {
                return;
            }

            Assert.assertThat("Unexpected explosion owner UUID", event.getCause().getContext().get(EventContextKeys.OWNER).map(Identifiable::getUniqueId), Matchers.equalTo(Optional
                    .of(playerId))) ;


        }));

        this.client.rightClick();
    }
}
