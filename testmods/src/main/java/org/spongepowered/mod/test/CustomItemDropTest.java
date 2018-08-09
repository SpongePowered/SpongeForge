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
package org.spongepowered.mod.test;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mod(modid = CustomItemDropTest.MOD_ID, name = "Custom Item Drop Test", acceptableRemoteVersions = "*")
public class CustomItemDropTest {

    public static final String MOD_ID = "customitemdroptest";
    public static final String ITEM_ID = "dropitem";
    private ItemType EGG_TOSS;
    private boolean areListenersEnabled = false;
    private static CustomItemDropTest INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, this);
        Sponge.getCommandManager().register(this, getCommand(), "customItemDropTest");
    }


    private static CommandCallable getCommand() {
        return CommandSpec.builder()
            .description(Text.of(TextColors.BLUE, "Toggles whether the event manipulations and entity deaths are enabled or not."))
            .executor((src, args) -> {
                INSTANCE.areListenersEnabled = !INSTANCE.areListenersEnabled;
                return CommandResult.success();
            })
            .build();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        EggItem item = new EggItem();
        item.setCreativeTab(CreativeTabs.FOOD);
        item.setRegistryName(new ResourceLocation(MOD_ID, ITEM_ID));
        event.getRegistry().register(item);
        final Optional<ItemType> type = Sponge.getRegistry().getType(ItemType.class, CatalogKey.of(MOD_ID, ITEM_ID));
        type.ifPresent(itemType -> this.EGG_TOSS = itemType);
    }

    @Listener(beforeModifications = false)
    @Exclude(DropItemEvent.Pre.class)
    public void onDropItem(DropItemEvent event) {
        if (this.areListenersEnabled) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onDeath(DestructEntityEvent.Death entityEvent) {
        if (this.areListenersEnabled) {
            entityEvent.setCancelled(true);
            final Living targetEntity = entityEvent.getTargetEntity();
            targetEntity.offer(Keys.HEALTH, targetEntity.require(Keys.MAX_HEALTH));
        }
    }

    @Listener(beforeModifications = true)
    public void onDropItem(DropItemEvent.Dispense event, @Root Player player) {
        if (this.areListenersEnabled) {
            final List<org.spongepowered.api.entity.Item> collections = event.getEntities()
                .stream()
                .filter(entity -> entity instanceof org.spongepowered.api.entity.Item)
                .map(entity -> (org.spongepowered.api.entity.Item) entity)
                .filter(itemEntity -> itemEntity.getItemType().equals(this.EGG_TOSS))
                .collect(Collectors.toList());
            if (!collections.isEmpty()) {
                final org.spongepowered.api.entity.Entity entity = player.getWorld().createEntity(EntityTypes.CREEPER, player.getPosition());
                event.getEntities().add(entity);
            }
        }
    }

    public static class EggItem extends ItemEgg {

        @Override
        @Nullable
        public Entity createEntity(World world, Entity location, ItemStack itemstack) {
            EntityChicken entity = new EntityChicken(world);
            entity.copyLocationAndAnglesFrom(location);
            return entity;
        }

        @Override
        public boolean hasCustomEntity(ItemStack stack) {
            return true;
        }
    }

}
