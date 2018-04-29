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
package org.spongepowered.mod

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityType
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.explosive.FusedExplosive
import org.spongepowered.api.entity.living.monster.Creeper
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.entity.living.player.gamemode.GameMode
import org.spongepowered.api.entity.living.player.gamemode.GameModes
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.cause.EventContextKeys
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.entity.SpawnEntityEvent
import org.spongepowered.api.event.entity.explosive.PrimeExplosiveEvent
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.entity.Hotbar
import org.spongepowered.api.item.inventory.query.QueryOperationTypes
import org.spongepowered.mctester.internal.BaseTest
import org.spongepowered.mctester.internal.coroutine.CoroutineTestUtils
import org.spongepowered.mctester.internal.event.StandaloneEventListener
import org.spongepowered.mctester.junit.CoroutineTest
import org.spongepowered.mctester.api.junit.MinecraftRunner
import org.spongepowered.mctester.junit.TestUtils
import java.util.*

@RunWith(MinecraftRunner::class)
class CreeperTestKotlin(testUtils: TestUtils): BaseTest(testUtils) {

    @Test
    @CoroutineTest
    suspend fun explodeCreeperKotlin() {
        System.err.println("Hello from kotlin!")

        val player = this.testUtils.thePlayer
        player.offer<GameMode>(Keys.GAME_MODE, GameModes.CREATIVE)

        (player.inventory.query<Inventory>(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar::class.java)) as Hotbar).selectedSlotIndex = 0
        player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.builder().itemType(ItemTypes.SPAWN_EGG).quantity(1).add<EntityType>(Keys.SPAWNABLE_ENTITY_TYPE, EntityTypes.CREEPER).build())

        CoroutineTestUtils.waitForInventoryPropagation();

        val targetPos = this.testUtils.thePlayer.getLocation().getPosition().add(0f, -1f, 2f)
        this.client.lookAtSuspend(targetPos);

        val creeper = arrayOfNulls<Creeper>(1)

        this.testUtils.listen<SpawnEntityEvent>(StandaloneEventListener<SpawnEntityEvent>(SpawnEntityEvent::class.java,
                EventListener<SpawnEntityEvent> { event:SpawnEntityEvent->
                    if (event.getEntities().stream().noneMatch({ e -> e.getType() == EntityTypes.CREEPER }))
                        return@EventListener

                    assertThat<List<Entity>>(event.getEntities(), hasSize<Entity>(1))
                    creeper[0] = event.getEntities().get(0) as Creeper

                    assertTrue("Cause doesn't contain player: " + event.getCause(), event.getCause().contains(this.testUtils.thePlayer))
                    assertTrue("Cause doesn't contain correct item: " + event.getCause(),
                            event.getCause().getContext().get(EventContextKeys.USED_ITEM).map({ i -> i.getType() == ItemTypes.SPAWN_EGG }).orElse(false)
                    )

                    Sponge.getEventManager().unregisterListeners(this)

                }))

        client.rightClickSuspend();

        assertThat<Creeper>("Creeper did not spawn!", creeper[0], instanceOf<Creeper>(Creeper::class.java))

        this.testUtils.listen<MoveEntityEvent>(StandaloneEventListener<MoveEntityEvent>(MoveEntityEvent::class.java, EventListener { event:MoveEntityEvent->

            if (event.getTargetEntity().getUniqueId() === creeper[0]!!.getUniqueId())
            {
                event.setCancelled(true)
            }
        }))


        this.testUtils.thePlayer.getInventory().query<Hotbar>(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar::class.java)).selectedSlotIndex = 1
        this.testUtils.thePlayer.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.FLINT_AND_STEEL, 1))

        CoroutineTestUtils.waitForInventoryPropagation();
        this.client.lookAtSuspend(creeper[0]!!)

        var fuseDuration: Int? = null

        this.testUtils.listenOneShotSuspend({
            try {
                this.client.rightClickSuspend()
            } catch (e: Throwable) {
                throw RuntimeException(e)
            }
        }, StandaloneEventListener<PrimeExplosiveEvent.Pre>(PrimeExplosiveEvent.Pre::class.java) { primeEvent: PrimeExplosiveEvent.Pre ->
            assertThat<FusedExplosive>(primeEvent.targetEntity, equalTo<FusedExplosive>(creeper[0]))
            fuseDuration = primeEvent.targetEntity.get(Keys.FUSE_DURATION).get()


        })

        // We should expect blokcs to break once the duration is up.
        try {
            this.testUtils.listenTimeOutSuspend<ChangeBlockEvent.Break>({ this.client.rightClickSuspend() },
                    StandaloneEventListener<ChangeBlockEvent.Break>(ChangeBlockEvent.Break::class.java) { event: ChangeBlockEvent.Break ->

                        assertThat(event.cause.context.get(EventContextKeys.OWNER).get().uniqueId,
                                equalTo(this.testUtils.thePlayer.getUniqueId()))

                        for (transaction in event.transactions) {
                            assertThat(transaction.final.state.type, equalTo(BlockTypes.AIR))
                        }
                    }, fuseDuration!!)

        } catch (e: Throwable) {
            throw RuntimeException(e)
        }

    }

    @Test(expected = IllegalStateException::class)
    @CoroutineTest
    suspend fun expectedFailure() {
        throw IllegalStateException("This should be caught!")
    }

}
