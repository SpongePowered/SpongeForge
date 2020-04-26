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
package org.spongepowered.mod.mixin.core.client;

import static net.minecraft.client.Minecraft.getSystemTime;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.server.integrated.IntegratedServerBridge;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;
import org.spongepowered.mod.bridge.client.MinecraftBridge_Forge;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Forge implements MinecraftBridge_Forge {

    @Shadow private LanguageManager languageManager;
    @Shadow private IntegratedServer integratedServer;

    @Shadow private long debugUpdateTime;

    private GuiOverlayDebug debugGui;
    private Text kickMessage;
    private boolean isNewSave;

    @Inject(method = "launchIntegratedServer", at = @At(value = "NEW", args = {"class=net/minecraft/world/storage/WorldInfo"}, ordinal = 0))
    private void forgeImpl$setNewSave(final CallbackInfo ci) {
        this.isNewSave = true;
    }

    @Redirect(method = "launchIntegratedServer",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Minecraft;integratedServer:Lnet/minecraft/server/integrated/IntegratedServer;",
            ordinal = 0))
    private void forgeImpl$MarkForNewSaves(final Minecraft minecraft, final IntegratedServer server) {
        this.integratedServer = server;
        if (this.isNewSave) {
            ((IntegratedServerBridge) this.integratedServer).bridge$markNewSave();
        }
        this.isNewSave = false;
    }

    @Override
    public void forgeBridge$setDebugGui(final GuiOverlayDebug debugGui) {
        this.debugGui = debugGui;
    }

    @Override
    public GuiOverlayDebug forgeBridge$getDebugGui() {
        return this.debugGui;
    }

    @Override
    public Text forgeBridge$getSinglePlayerKickMessage() {
        return this.kickMessage;
    }

    @Override
    public void forgeBridge$setSinglePlayerKickMessage(final Text text) {
        this.kickMessage = text;
    }

    @Dynamic // Forge's TerminalTransformer performs a rewrite of the System.exit(1), and we need to just inject before
    @Inject(method = "shutdownMinecraftApplet",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/asm/transformers/TerminalTransformer$ExitVisitor;systemExitCalled(I)V",
            remap = false))
    private void onShutdownDelegate(final CallbackInfo ci) {
        SpongeImpl.postShutdownEvents();
    }

    /**
     * This will inject at the moment before the single player instance is
     * spawning the EntityPlayerSP client into the world. This checks if
     * it is single player, and if so, attempts to retrieve if the player
     * has joined this world before, if not, it thinks that this is the
     * first time joining and creates the necessary data.
     *
     * @param client The client
     * @param name The world name
     * @param callbackInfo The necessary callback info
     */
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityPlayerSP;preparePlayerToSpawn()V",
            shift = At.Shift.AFTER))
    private void onSpawn(final WorldClient client, final String name, final CallbackInfo callbackInfo) {
        try {
            if (Sponge.isServerAvailable() && SpongeImpl.getServer().isSinglePlayer()) {
                final EntityPlayer player = Minecraft.getMinecraft().player;
                final UUID uuid = player.getUniqueID();
                final Optional<Instant> joined = SpongePlayerDataHandler.getFirstJoined(uuid);
                if (!joined.isPresent()) {
                    final Instant newJoined = Instant.now();
                    SpongePlayerDataHandler.setPlayerInfo(uuid, newJoined, newJoined);
                }
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Could not retrieve the player instance or single player instance to get the join data.", e);
        }

    }

    @SuppressWarnings("deprecation")
    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
        at = @At(value="INVOKE", target="Lnet/minecraft/client/LoadingScreenRenderer;displayLoadingString(Ljava/lang/String;)V", ordinal = 0))
    private void forgeImpl$updateLoadMessage(final LoadingScreenRenderer loadingScreen, final String message) {
        // TODO Minecrell should review this...
        if (this.kickMessage == null) {
            loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal"));
        } else {
            final String loadingString;
            if (this.kickMessage instanceof TranslatableText) {
                loadingString = ((TranslatableText) this.kickMessage).getTranslation().get(Locale.forLanguageTag(this.languageManager.getCurrentLanguage()
                        .getLanguageCode()));
            } else {
                loadingString = TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.kickMessage);
            }
            loadingScreen.displayLoadingString(loadingString);
            this.kickMessage = null;
        }
    }

    // There's absolutely no reason to loop here. We force the loop
    // to always exit after one interation by setting debugUpdateTime to getSystemTime()
    @Redirect(method = "runGameLoop",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugUpdateTime:J", opcode = Opcodes.PUTFIELD))
    private void forgeImpl$updateDebugTime(final Minecraft this$0, final long value) {
        this.debugUpdateTime = getSystemTime();
    }
}
