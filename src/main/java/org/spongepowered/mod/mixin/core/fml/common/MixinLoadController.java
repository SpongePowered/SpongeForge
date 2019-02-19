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
package org.spongepowered.mod.mixin.core.fml.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.event.StateRegistry;
import org.spongepowered.mod.interfaces.IMixinLoadController;

import javax.annotation.Nullable;

@Mixin(value = LoadController.class, remap = false)
public abstract class MixinLoadController implements IMixinLoadController {
    @Shadow private ModContainer activeContainer;

    @Redirect(method = "distributeStateMessage", at = @At(value = "INVOKE", target = "Lcom/google/common/eventbus/EventBus;post(Ljava/lang/Object;)V", ordinal = 0, remap = false))
    public void onPost(EventBus eventBus, Object event, LoaderState state, Object[] eventData) {
        // 'distbuteStateMessage' is *sometimes* called before 'transition'.
        // To ensure that the state is properly set in SpongeGame, we need
        // to mixin here, before any event handlers have received the event

        SpongeImpl.getGame().setState(StateRegistry.getState(state));
        eventBus.post(event);
        if (state == LoaderState.CONSTRUCTING) {
            SpongeImpl.postEvent((Event) event, true);
        }
    }

    @Override
    public ModContainer getActiveModContainer() {
        return this.activeContainer;
    }

    @Override
    public void setActiveModContainer(@Nullable ModContainer container) {
        this.activeContainer = container;
    }

}
