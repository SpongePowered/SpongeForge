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
package org.spongepowered.mod.mixin.core.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.text.SpongeTexts;

import java.time.Instant;
import java.util.Optional;

@Mixin(value = PlayerAdvancements.class)
public class MixinPlayerAdvancements {

    private Text message;

    @Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void onSendAdvancementMessage(PlayerList list, ITextComponent component) {
        this.message = SpongeTexts.toText(component);
    }

    @Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onAdvancement(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/advancements/Advancement;)V"))
    private void onForgeHooks(EntityPlayerMP player, Advancement advancement) {
        Instant instant = Instant.now();

        MessageChannel channel;
        MessageEvent.MessageFormatter formatter;
        if (this.message != null) {
            channel = MessageChannel.TO_ALL;
            formatter = new MessageEvent.MessageFormatter(this.message);
        } else {
            channel = MessageChannel.TO_NONE;
            formatter = new MessageEvent.MessageFormatter();
            formatter.clear();
        }

        AdvancementEvent.Grant event = SpongeEventFactory.createAdvancementEventGrant(
                Sponge.getCauseStackManager().getCurrentCause(),
                channel,
                Optional.of(channel),
                (org.spongepowered.api.advancement.Advancement) advancement,
                formatter, (Player) player, instant, false

        );
        SpongeImpl.postEvent(event);
        if (!event.isMessageCancelled() && !event.getMessage().isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(player, event.getMessage()));
        }

        this.message = null;
    }

}
