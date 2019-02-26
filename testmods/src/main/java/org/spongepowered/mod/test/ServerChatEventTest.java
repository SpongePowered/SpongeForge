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
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
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

@Mod(modid = ServerChatEventTest.MOD_ID, name = "Server Chat Event Test", acceptableRemoteVersions = "*")
public class ServerChatEventTest {

    public static final String MOD_ID = "serverchateventtest";
    private static final Text PARAMETER = Text.of("state");
    private boolean areListenersEnabled = false;
    private boolean altering = false;
    private static ServerChatEventTest INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Sponge.getCommandManager().register(this, getCommand(), "forgeserverchatevent");
    }

    private static CommandCallable getCommand() {
        return CommandSpec.builder()
                .child(getListenerCommand(), "active")
                .child(getAlterCommand(), "alter")
                .build();
    }

    private static CommandCallable getListenerCommand() {
        return CommandSpec.builder()
            .description(Text.of(TextColors.BLUE, "Toggles whether the ServerChatEvent is being listened to."))
            .arguments(GenericArguments.bool(PARAMETER))
            .executor((src, args) -> {
                boolean newState = args.requireOne(PARAMETER);
                if (INSTANCE.areListenersEnabled != newState) {
                    if (newState) {
                        MinecraftForge.EVENT_BUS.register(INSTANCE);
                    } else {
                        MinecraftForge.EVENT_BUS.unregister(INSTANCE);
                    }

                    INSTANCE.areListenersEnabled = newState;
                }

                src.sendMessage(Text.of("ServerChatEvent listener active: ", INSTANCE.areListenersEnabled));
                return CommandResult.success();
            })
            .build();
    }

    private static CommandCallable getAlterCommand() {
        return CommandSpec.builder()
                .description(Text.of(TextColors.BLUE, "Toggles whether the ServerChatEvent manipulates the chat."))
                .arguments(GenericArguments.bool(PARAMETER))
                .executor((src, args) -> {
                    INSTANCE.altering = args.requireOne(PARAMETER);
                    src.sendMessage(Text.of("ServerChatEvent listener altering chat: ", INSTANCE.altering));
                    return CommandResult.success();
                })
                .build();
    }

    @SubscribeEvent
    public void registerItems(ServerChatEvent event) {
        if (this.altering) {
            event.setComponent(event.getComponent().appendText(" - Forge"));
        }
    }

}
