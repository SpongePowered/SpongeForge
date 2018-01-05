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
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.event.StateRegistry;
import org.spongepowered.mod.interfaces.IMixinLoadController;

import javax.annotation.Nullable;

@Mixin(value = LoadController.class, remap = false)
public abstract class MixinLoadController implements IMixinLoadController {

    @Shadow private ModContainer activeContainer;
    private ThreadLocal<ModContainer> threadLocalActiveContainer = ThreadLocal.withInitial(() -> null);

    @Shadow protected abstract ModContainer findActiveContainerFromStack();

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
        return this.threadLocalActiveContainer.get();
    }

    @Override
    public void setActiveModContainer(ModContainer container) {
        if (container == null) {
            this.threadLocalActiveContainer.remove();
        } else {
            this.threadLocalActiveContainer.set(container);
        }
    }

    /**
     * @author dualspiral - 16th June 2017
     *
     * This overwites {@link LoadController#activeContainer()}
     *
     * Sponge can post events from threads that are not on the server thread,
     * and this can cause confusion for the majority of events that are
     * firing on the server thread. As a result, we redirect calls to get
     * the active container to a thread local version, so this confusion
     * does not occur.
     */
    @Nullable
    public ModContainer activeContainer() {
        ModContainer modContainer = this.threadLocalActiveContainer.get();
        if (modContainer == null) {
            return findActiveContainerFromStack();
        }

        return modContainer;
    }

    /**
     * @author dualspiral - 16th June 2017
     *
     * See above for rationale. This sets the active container in the thread
     * local variable.
     *
     * @param container The container
     */
    @Inject(method = "forceActiveContainer", at = @At("RETURN"))
    private void onForceActiveContainerReturn(@Nullable ModContainer container, CallbackInfo ci) {
        if (container == null) {
            this.threadLocalActiveContainer.remove();
        } else {
            this.threadLocalActiveContainer.set(container);
        }
    }

    /**
     * @author dualspiral - 16th June 2017
     *
     * Called during state changes, we mirror what is in the active container variable.
     *
     * @param ci callbackinfo
     */
    @Inject(method = "sendEventToModContainer", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
            target = "Lnet/minecraftforge/fml/common/LoadController;activeContainer:Lnet/minecraftforge/fml/common/ModContainer;"))
    private void onSetApplyModContainer(CallbackInfo ci) {
        if (this.activeContainer != null) {
            this.threadLocalActiveContainer.set(this.activeContainer);
        } else {
            this.threadLocalActiveContainer.remove();
        }
    }

}
