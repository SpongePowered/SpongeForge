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

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.api.Client;
import org.spongepowered.api.LocalServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.ClientPlayer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinIntegratedServer;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements Client, IMixinMinecraft {

    private static final String LOAD_WORLD = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V";
    private static final String ENTITY_PLAYER_PREPARE_TO_SPAWN = "Lnet/minecraft/client/entity/EntityPlayerSP;preparePlayerToSpawn()V";
    private static final String SAVE_HANDLER_SAVE_WORLD_INFO =
            "Lnet/minecraft/world/storage/ISaveHandler;saveWorldInfo(Lnet/minecraft/world/storage/WorldInfo;)V";
    private static final String FORGE_TRANSFORMER_EXIT_VISITOR =
            "Lnet/minecraftforge/fml/common/asm/transformers/TerminalTransformer$ExitVisitor;systemExitCalled(I)V";

    @Shadow private LanguageManager mcLanguageManager;
    @Shadow private IntegratedServer theIntegratedServer;

    @Shadow public WorldClient world;
    @Shadow public EntityPlayerSP player;

    @Shadow @Final private File fileAssets;
    @Shadow @Final private File fileResourcepacks;
    @Shadow @Nullable public GuiScreen currentScreen;

    @Shadow public abstract void launchIntegratedServer(String folderName, String worldName, @Nullable WorldSettings worldSettingsIn);

    @Shadow public abstract boolean isCallingFromMinecraftThread();

    private GuiOverlayDebug debugGui;
    private Text kickMessage;
    private boolean isNewSave;

    @Inject(method = "launchIntegratedServer", at = @At(value = "NEW", args = {"class=net/minecraft/world/storage/WorldInfo"}, ordinal = 0))
    public void onCreateWorldInfo(CallbackInfo ci) {
        this.isNewSave = true;
    }

    @Redirect(method = "launchIntegratedServer", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Minecraft;theIntegratedServer:Lnet/minecraft/server/integrated/IntegratedServer;", ordinal = 0))
    public void onSetIntegratedServerField(Minecraft minecraft, IntegratedServer server) {
        this.theIntegratedServer = server;
        if (this.isNewSave) {
            ((IMixinIntegratedServer) this.theIntegratedServer).markNewSave();
        }
        this.isNewSave = false;
    }

    @Override
    public void setDebugGui(GuiOverlayDebug debugGui) {
        this.debugGui = debugGui;
    }

    @Override
    public GuiOverlayDebug getDebugGui() {
        return this.debugGui;
    }

    @Override
    public Text getSinglePlayerKickMessage() {
        return this.kickMessage;
    }

    @Override
    public void setSinglePlayerKickMessage(Text text) {
        this.kickMessage = text;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "shutdownMinecraftApplet", at = @At(value = "INVOKE", target = FORGE_TRANSFORMER_EXIT_VISITOR, remap = false))
    public void onShutdownDelegate(CallbackInfo ci) {
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
    @Inject(method = LOAD_WORLD, at = @At(value = "INVOKE", target = ENTITY_PLAYER_PREPARE_TO_SPAWN, shift = At.Shift.AFTER))
    private void onSpawn(WorldClient client, String name, CallbackInfo callbackInfo) {
        try {
            if (Sponge.isServerAvailable() && SpongeImpl.getServer().isSinglePlayer()) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                UUID uuid = player.getUniqueID();
                Optional<Instant> joined = SpongePlayerDataHandler.getFirstJoined(uuid);
                if (!joined.isPresent()) {
                    Instant newJoined = Instant.now();
                    SpongePlayerDataHandler.setPlayerInfo(uuid, newJoined, newJoined);
                }
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Could not retrieve the player instance or single player instance to get the join data.", e);
        }

    }

    @SuppressWarnings("deprecation")
    @Redirect(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/"
            + "client/LoadingScreenRenderer;displayLoadingString(Ljava/lang/String;)V", ordinal = 0))
    public void onLoadWorld(LoadingScreenRenderer loadingScreen, String message) {
        // TODO Minecrell should review this...
        if (this.kickMessage == null) {
            loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal"));
        } else {
            String loadingString;
            if (this.kickMessage instanceof TranslatableText) {
                loadingString = ((TranslatableText) this.kickMessage).getTranslation().get(Locale.forLanguageTag(this.mcLanguageManager.getCurrentLanguage()
                        .getLanguageCode()));
            } else {
                loadingString = TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.kickMessage);
            }
            loadingScreen.displayLoadingString(loadingString);
            this.kickMessage = null;
        }
    }

    @Override
    public Optional<World> getLoadedWorld() {
        return Optional.ofNullable((World) this.world);
    }

    @Override
    public Optional<ClientPlayer> getClientPlayer() {
        return Optional.ofNullable((ClientPlayer) this.player);
    }

    @Override
    public Optional<LocalServer> getLocalServer() {
        return Optional.ofNullable((LocalServer) this.theIntegratedServer);
    }

    @Override
    public void loadWorldSave(Path saveLocation) {
        try {
            Path saveDir = saveLocation.relativize(Sponge.getGame().getSavesDirectory());

            String save = saveDir.toString();
            String name = saveDir.getFileName().toString();

            launchIntegratedServer(save, name, null);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Save location is not in the saves folder: " + saveLocation, e);
        }
    }

    @Override
    public void joinServer(InetSocketAddress server) {
        String host = server.getHostString();
        int port = server.getPort();
        ServerData serverData = new ServerData("Sponge API Call", host + ":" + port, false);

        // connect to the server
        FMLClientHandler.instance().connectToServer(this.currentScreen, serverData);

    }

    @Override
    public Path getResourcePacksDirectory() {
        return this.fileResourcepacks.toPath();
    }

    @Override
    public Path getAssetsDirectory() {
        return this.fileAssets.toPath();
    }

    @Override
    public boolean isMainThread() {
        return isCallingFromMinecraftThread();
    }
}
