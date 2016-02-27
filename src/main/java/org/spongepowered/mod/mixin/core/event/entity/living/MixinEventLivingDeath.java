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
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = LivingDeathEvent.class, remap = false)
public abstract class MixinEventLivingDeath extends MixinEventLiving implements DestructEntityEvent.Death {

    private final MessageFormatter formatter = new MessageFormatter();
    private boolean messageCancelled;
    private Text originalMessage;
    @Nullable private MessageChannel channel;
    private MessageChannel originalChannel;
    private Cause cause;

    @Shadow @Final public DamageSource source;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(EntityLivingBase entity, DamageSource source, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP) {
            Player player = (Player) entity;
            this.originalChannel = player.getMessageChannel();
            this.channel = player.getMessageChannel();
        } else {
            this.originalChannel = MessageChannel.TO_NONE;
            this.channel = MessageChannel.TO_NONE;
        }

        this.originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        getFormatter().getBody().add(new DefaultBodyApplier(this.originalMessage));
        Optional<User> sourceCreator = Optional.empty();

        if (this.source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) this.source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            sourceCreator = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        }
        if (sourceCreator.isPresent()) {
            this.cause = Cause.of(NamedCause.source(this.source), NamedCause.of("Victim", this.entityLiving),
                NamedCause.owner(sourceCreator.get()));
        } else {
            this.cause = Cause.of(NamedCause.source(this.source), NamedCause.of("Victim", this.entityLiving));
        }
        // Store cause for drop event which is called after this event
        if (sourceCreator.isPresent()) {
            StaticMixinHelper.dropCause = Cause.of(NamedCause.source(this.entityLiving), NamedCause.of("Attacker", this.source),
                NamedCause.owner(sourceCreator.get()));
        } else {
            StaticMixinHelper.dropCause = Cause.of(NamedCause.source(this.entityLiving), NamedCause.of("Attacker", this.source));
        }
    }

    @Override
    public boolean isMessageCancelled() {
        return this.messageCancelled;
    }

    @Override
    public void setMessageCancelled(boolean cancelled) {
        this.messageCancelled = cancelled;
    }

    public MessageFormatter getFormatter() {
        return this.formatter;
    }

    @Override
    public MessageChannel getOriginalChannel() {
        return this.originalChannel;
    }

    @Override
    public Optional<MessageChannel> getChannel() {
        return Optional.ofNullable(this.channel);
    }

    @Override
    public void setChannel(@Nullable MessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public Text getOriginalMessage() {
        return this.originalMessage;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

}
