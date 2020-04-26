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

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.fml.common.CertificateHelper;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.mod.bridge.fml.LoadControllerBridge_Forge;
import org.spongepowered.mod.event.StateRegistry;
import org.spongepowered.mod.plugin.SpongeModPluginContainer;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = LoadController.class, remap = false)
public abstract class LoadControllerMixin_Forge implements LoadControllerBridge_Forge {
    @Shadow private ModContainer activeContainer;
    @Shadow private Loader loader;
    @Shadow private Multimap<String, LoaderState.ModState> modStates;

    @Redirect(method = "distributeStateMessage(Lnet/minecraftforge/fml/common/LoaderState;[Ljava/lang/Object;)V",
        at = @At(value = "INVOKE", target = "Lcom/google/common/eventbus/EventBus;post(Ljava/lang/Object;)V", ordinal = 0, remap = false))
    private void forgeImpl$PostEvent(final EventBus eventBus, final Object event, final LoaderState state, final Object[] eventData) {
        // 'distbuteStateMessage' is *sometimes* called before 'transition'.
        // To ensure that the state is properly set in SpongeGame, we need
        // to mixin here, before any event handlers have received the event

        SpongeImpl.getGame().setState(StateRegistry.getState(state));
        eventBus.post(event);
        if (state == LoaderState.CONSTRUCTING) {
            SpongeImpl.postEvent((Event) event, true);
        }
    }

    @Inject(method = "printModStates", at = @At(value = "NEW", target = "net/minecraftforge/common/util/TextTable"))
    private void printModsTableHeader(final StringBuilder ret, final CallbackInfo ci) {
        ret.append("\n");
        ret.append("\n\t");
        ret.append("Mods:");
    }

    @Redirect(method = "printModStates", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/Loader;getModList()Ljava/util/List;"))
    private List<ModContainer> forgeImpl$separateModsForTable(final Loader loader) {
        return loader.getModList().stream().filter(modContainer -> !(modContainer instanceof SpongeModPluginContainer)).collect(Collectors.toList());
    }

    @Inject(method = "printModStates", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void forgeImpl$addPluginsTable(final StringBuilder ret, final CallbackInfo ci, final TextTable table) {
        table.clear();

        ret.append("\n");
        ret.append("\n\t");
        ret.append("Plugins:");

        for (final ModContainer mc : this.loader.getModList().stream()
            .filter(modContainer -> modContainer instanceof SpongeModPluginContainer)
            .collect(Collectors.toList())) {

            table.add(
                this.modStates.get(mc.getModId()).stream().map(LoaderState.ModState::getMarker).reduce("", (a, b) -> a + b),
                mc.getModId(),
                mc.getVersion(),
                mc.getSource().getName(),
                mc.getSigningCertificate() != null ? CertificateHelper.getFingerprint(mc.getSigningCertificate()) : "None"
            );
        }

        ret.append("\n");
        ret.append("\n\t");
        table.append(ret, "\n\t");
        ret.append("\n");
    }

    @Override
    public ModContainer forgeBridge$getActiveModContainer() {
        return this.activeContainer;
    }

    @Override
    public void forgeBridge$setActiveModContainer(@Nullable final ModContainer container) {
        this.activeContainer = container;
    }

}
