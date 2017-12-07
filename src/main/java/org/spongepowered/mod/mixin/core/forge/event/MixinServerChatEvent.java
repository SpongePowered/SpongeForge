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
package org.spongepowered.mod.mixin.core.forge.event;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.ServerChatEvent;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.mod.interfaces.IMixinEvent;

@Mixin(value = ServerChatEvent.class, remap = false)
public abstract class MixinServerChatEvent extends net.minecraftforge.fml.common.eventhandler.Event implements IMixinEvent {

    // Flag to indicate that the component has been changed, for sync purposes.
    private boolean hasChanged = false;
    @Shadow private ITextComponent component;

    @Inject(method = "setComponent", at = @At("RETURN"))
    private void onSetComponent(ITextComponent e, CallbackInfo ci) {
        this.hasChanged = true;
    }

    @Override
    public void syncDataToForge(Event event) {
        if (event instanceof MessageChannelEvent.Chat) {
            this.component = SpongeTexts.toComponent(((MessageChannelEvent.Chat) event).getMessage());
            this.hasChanged = false;
        }
    }

    @Override
    public void syncDataToSponge(Event event) {
        if (this.hasChanged && event instanceof MessageChannelEvent.Chat) {
            // We have no way to determine what the head or the body is - so
            // we just put it all into the body.
            ((MessageChannelEvent.Chat) event).setMessage(((IMixinTextComponent) this.component).toText());
            this.hasChanged = false;
        }
    }
}
