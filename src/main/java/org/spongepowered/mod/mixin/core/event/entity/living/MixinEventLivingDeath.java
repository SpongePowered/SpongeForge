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
package org.spongepowered.mod.mixin.core.event.entity.living;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.sink.MessageSinks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.text.SpongeTexts;

@Mixin(value = LivingDeathEvent.class, remap = false)
public abstract class MixinEventLivingDeath extends MixinEventLiving implements DestructEntityEvent.Death {

    private MessageSink sink;
    private MessageSink originalSink;
    private Text originalMessage;
    private Text message;

    @Shadow public DamageSource source;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(EntityLivingBase entity, DamageSource source, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP) {
            Player player = (Player) entity;
            this.originalSink = player.getMessageSink();
            this.sink = player.getMessageSink();
        } else {
            this.originalSink = MessageSinks.toNone();
            this.sink = MessageSinks.toNone();
        }

        this.originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        this.message = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
    }

    @Override
    public MessageSink getOriginalSink() {
        return this.originalSink;
    }

    @Override
    public MessageSink getSink() {
        return this.sink;
    }

    @Override
    public void setSink(MessageSink sink) {
        this.sink = sink;
    }

    @Override
    public Text getOriginalMessage() {
        return this.originalMessage;
    }

    @Override
    public Text getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(Text message) {
        this.message = message;
    }

    @Override
    public Cause getCause() {
        return Cause.of(this.source);
    }

}
