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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.event.SpongeModEventManager;
import org.spongepowered.mod.event.StateRegistry;
import org.spongepowered.mod.interfaces.IMixinLoadController;

@Mixin(value = LoadController.class, remap = false)
public abstract class MixinLoadController implements IMixinLoadController {
    @Shadow private ModContainer activeContainer;

    @Shadow protected abstract ModContainer findActiveContainerFromStack();

    @Redirect(method = "distributeStateMessage", at = @At(value = "INVOKE", target = "Lcom/google/common/eventbus/EventBus;post(Ljava/lang/Object;)V", ordinal = 0, remap = false))
    public void onPost(EventBus eventBus, Object event, LoaderState state, Object[] eventData) {
        // 'distbuteStateMessage' is *sometimes* called before 'transition'.
        // To ensure that the state is properly set in SpongeGame, we need
        // to mixin here, before any event handlers have received the event

        SpongeImpl.getGame().setState(StateRegistry.getState(state));
        eventBus.post(event);
        if (state == LoaderState.CONSTRUCTING) {
            ((SpongeModEventManager) SpongeImpl.getGame().getEventManager()).post((Event) event, true);
        }
    }

    @Override
    public ModContainer getActiveModContainer() {
        return this.activeContainer;
    }

    @Override
    public void setActiveModContainer(ModContainer container) {
        this.activeContainer = container;
    }

    /**
     * @author dualspiral - 4th June 2017
     *
     * Sponge can post events from threads that are not on the server thread,
     * and this can cause confusion for the majority of events that are
     * firing on the server thread. In these cases, we just get the
     * mod from the stack.
     *
     * @param cir
     */
    @Inject(method = "activeContainer", at = @At("HEAD"), cancellable = true)
    private void onActiveContainerHead(CallbackInfoReturnable<ModContainer> cir) {
        if (Sponge.isServerAvailable() && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            cir.setReturnValue(findActiveContainerFromStack());
        }
    }
}
