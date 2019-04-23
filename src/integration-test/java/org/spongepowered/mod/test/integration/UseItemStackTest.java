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
package org.spongepowered.mod.test.integration;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.junit.TestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(MinecraftRunner.class)
public class UseItemStackTest extends BaseTest {

    public UseItemStackTest(TestUtils testUtils) {
        super(testUtils);
    }

    private ItemStack setupItemStack() throws Throwable {
        ItemStack stack = ItemStack.of(ItemTypes.GOLDEN_APPLE, 1);

        this.testUtils.runOnMainThread(() -> {
            this.testUtils.getThePlayer().offer(Keys.GAME_MODE, GameModes.SURVIVAL);
            this.testUtils.getThePlayer().setItemInHand(HandTypes.MAIN_HAND, stack);
        });

        this.testUtils.waitForInventoryPropagation();
        return stack;
    }

    @Test
    public void testNormalCycle() throws Throwable {
        ItemStack stack = this.setupItemStack();

        final UseItemStateHolder useItemStateHolder = new UseItemStateHolder();
        final int[] firstDuration = new int[1];

        this.testUtils.listen(new StandaloneEventListener<>(UseItemStackEvent.class, event -> {
            useItemStateHolder.transition(event);
            if (event instanceof UseItemStackEvent.Start) {
                firstDuration[0] = event.getOriginalRemainingDuration();
            }
        }));

        this.client.holdRightClick(true);
        this.testUtils.sleepTicks(40);
        this.client.holdRightClick(false);

        int expectedDuration = ItemStackUtil.toNative(stack).getItem().getMaxItemUseDuration(ItemStackUtil.toNative(stack));
        assertThat("Unexpected item use duration!", firstDuration[0], is(expectedDuration));

        List<UseItemState> expectedStates = Lists.newArrayList(UseItemState.START);
        for (int i = 0; i < firstDuration[0]; i++) {
            expectedStates.add(UseItemState.TICK);
        }
        Collections.addAll(expectedStates, UseItemState.FINISH, UseItemState.REPLACE, UseItemState.RESET);

        List<UseItemState> actualStates = useItemStateHolder.getHistory().stream().map(s -> s.state).collect(Collectors.toList());
        List<UseItemStackEvent> actualEvents = useItemStateHolder.getHistory().stream().map(s -> s.event).collect(Collectors.toList());

        List<ItemStackSnapshot> items = actualEvents.stream().map(UseItemStackEvent::getItemStackInUse).collect(Collectors.toList());
        ItemStackSnapshot expectedSnapshot = stack.createSnapshot();

        assertThat("Unexpected sequence of events!", actualStates, equalTo(expectedStates));
        assertThat("Unexpected item in use!", items, everyItem(equalTo(expectedSnapshot)));

    }

    @Test
    @Ignore("This test is too flaky to leave enabled - the inherent raciness of cancelling the usage leads to spurious failures")
    public void testClientStopsUsing() throws Throwable {
        //Thread.sleep(5000);
        ItemStack stack = this.setupItemStack();

        final UseItemStateHolder useItemStateHolder = new UseItemStateHolder();
        final int[] firstDuration = new int[1];

        CompletableFuture<Void> readyToCancel = new CompletableFuture<>();
        CompletableFuture<Void> stopped = new CompletableFuture<>();

        int stopAt = 30;

        this.testUtils.listen(new StandaloneEventListener<>(UseItemStackEvent.class, new EventListener<UseItemStackEvent>() {

            @Override
            public void handle(UseItemStackEvent event) throws Exception {
                useItemStateHolder.transition(event);

                System.err.println("Got event: " + event);

                if (event instanceof UseItemStackEvent.Start) {
                    firstDuration[0] = event.getOriginalRemainingDuration();
                    return;
                }

                // We wake up the tester thread, which tells the client
                // to stop right clicking
                if (event.getRemainingDuration() == stopAt) {
                    readyToCancel.complete(null);
                    return;
                }

                // Once the client stops right clicking,
                // we should receive a Stop event, then a Reset event

                if (event instanceof UseItemStackEvent.Reset) {
                    stopped.complete(null);
                    return;
                }
            }
        }));

        this.client.holdRightClick(true);

        //Thread.sleep(999999);

        readyToCancel.get(9999999, TimeUnit.SECONDS);

        // This is inherently racy - once the client
        // starts eating an item, the server will continue to tick
        // down the item usage unless it receives a cancellation from the client.
        // If the cancellation packet from the client isn't processed in time,
        // the item will be consumed, even though it should have cancelled
        // from the client's perspective.
        //
        // This is how Vanilla works, so we have to live with it.
        // We cancel the item usage with one second remaining - since
        // McTester uses a singleplayer world, this should be more than
        // enough time to ensure that the server consistently receives the
        // cancellation in time.

        System.err.println("Stopping right click!");
        this.client.holdRightClick(false);

        stopped.get(5, TimeUnit.SECONDS);

        System.err.println("Right click stopped!");

        List<UseItemState> actualStates = useItemStateHolder.getHistory().stream().map(s -> s.state).collect(Collectors.toList());

        assertThat("Unexpected start state!", actualStates.get(0), equalTo(UseItemState.START));
        int stopIndex = actualStates.indexOf(UseItemState.STOP);
        int numTicks = stopIndex - 1;

        System.err.println("Stopped at " + numTicks);

        // stopIndex should be the second-to-last item in the list, followed by RESET
        assertThat(actualStates, hasSize(stopIndex + 2));

        // Due to the raciness mentioned above, we can't know exactly how many ticks will occur.
        // The item should have been ticking at least until we told the client to stop right clicking,
        // and possibly for several more ticks after that (up to the maximum number allowed)
        assertThat(numTicks, is(both(greaterThanOrEqualTo(firstDuration[0] - stopAt)).and(lessThanOrEqualTo(firstDuration[0]))));

        assertThat("Unexpected final state!", actualStates.get(actualStates.size() - 1), equalTo(UseItemState.RESET));

    }

    class UseItemStateHolder {
        private UseItemState state = UseItemState.init();
        private List<UseItemStateSnapshot> history = Lists.newArrayList();

        public void transition(UseItemStackEvent event) {
            this.state = this.state.transition(event);
            this.history.add(new UseItemStateSnapshot(this.state, event));
        }

        public List<UseItemStateSnapshot> getHistory() {
            return ImmutableList.copyOf(this.history);
        }
    }

    class UseItemStateSnapshot {
        public final UseItemState state;
        public final UseItemStackEvent event;

        UseItemStateSnapshot(UseItemState state, UseItemStackEvent event) {
            this.state = state;
            this.event = event;
        }
    }

    enum UseItemState {
        INIT,
        START,
        TICK,
        STOP,
        FINISH,
        REPLACE,
        RESET;

        static {
            INIT.nextStates = Sets.newHashSet(UseItemState.START);
            START.nextStates = Sets.newHashSet(UseItemState.TICK);
            TICK.nextStates = Sets.newHashSet(UseItemState.TICK, UseItemState.STOP, UseItemState.FINISH, UseItemState.RESET);
            STOP.nextStates = Sets.newHashSet(UseItemState.RESET);
            FINISH.nextStates = Sets.newHashSet(UseItemState.REPLACE);
            REPLACE.nextStates = Sets.newHashSet(UseItemState.RESET);
            RESET.nextStates = Sets.newHashSet();
        }

        private Set<UseItemState> nextStates;

        public static UseItemState init() {
            return UseItemState.INIT;
        }

        public UseItemState transition(UseItemStackEvent event) {
            UseItemState nextState;
            if (event instanceof UseItemStackEvent.Start) {
                nextState = START;
            } else if (event instanceof UseItemStackEvent.Tick) {
                nextState = TICK;
            } else if (event instanceof UseItemStackEvent.Stop) {
                nextState = STOP;
            } else if (event instanceof UseItemStackEvent.Finish) {
                nextState = FINISH;
            } else if (event instanceof UseItemStackEvent.Replace) {
                nextState = REPLACE;
            } else if (event instanceof UseItemStackEvent.Reset) {
                nextState = RESET;
            } else {
                throw new IllegalArgumentException("Unknown event " + event);
            }

            if (!this.nextStates.contains(nextState)) {
                throw new IllegalStateException(String.format("Attempted to transition to state %s from %s", this, nextState));
            }
            return nextState;
        }
    }
}
