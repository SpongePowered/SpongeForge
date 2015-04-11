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
package org.spongepowered.mod.mixin.core.event.player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeChatComponent;

@NonnullByDefault
@Mixin(value = net.minecraftforge.event.ServerChatEvent.class, remap = false)
public abstract class MixinEventPlayerChat extends Event implements PlayerChatEvent {

    @Shadow
    public String message;

    @Shadow
    public String username;

    @Shadow
    public EntityPlayerMP player;

    @Shadow
    public ChatComponentTranslation component;

    @Override
    public CommandSource getSource() {
        return (CommandSource) this.player;
    }

    @Override
    public Text getMessage() {
        return ((SpongeChatComponent) this.component).toText();
    }

    @Override
    public Player getPlayer() {
        return (Player) this.player;
    }

    @Override
    public Player getHuman() {
        return (Player) this.player;
    }

    @Override
    public Player getLiving() {
        return (Player) this.player;
    }

    @Override
    public Player getEntity() {
        return (Player) this.player;
    }

}
