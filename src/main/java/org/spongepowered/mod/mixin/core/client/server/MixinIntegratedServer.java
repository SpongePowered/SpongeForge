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
package org.spongepowered.mod.mixin.core.client.server;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

import java.io.File;
import java.net.Proxy;

@NonnullByDefault
@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    public MixinIntegratedServer(File workDir, Proxy proxy, File profileCacheDir) {
        super(workDir, proxy, profileCacheDir);
    }

    /**
     * @author bloodmc
     *
     * @reason In order to guarantee that both client and server load worlds the
     * same using our custom logic, we call super and handle any client specific
     * cases there.
     * Reasoning: This avoids duplicate code and makes it easier to maintain.
     */
    @Override
    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String unused, long seed, WorldType type, String generator) {
        super.loadAllWorlds(overworldFolder, unused, seed, type, generator);
    }

    public void shutdown() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            boolean flag = Minecraft.getMinecraft().isIntegratedServerRunning();
            // Need to null check in-case this is invoked very early in login
            if (Minecraft.getMinecraft().theWorld != null) {
                Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
            }

            Minecraft.getMinecraft().loadWorld((WorldClient)null);

            if (flag) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
            }
            else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            }
        });
    }

    public void shutdown(Text kickMessage) {
        checkNotNull(kickMessage);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ((IMixinMinecraft) Minecraft.getMinecraft()).setSinglePlayerKickMessage(kickMessage);
            boolean flag = Minecraft.getMinecraft().isIntegratedServerRunning();
            // Need to null check in-case this is invoked very early in login
            if (Minecraft.getMinecraft().theWorld != null) {
                // Different from the above so that we send our message
                Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(SpongeTexts.toComponent(kickMessage));
            }

            Minecraft.getMinecraft().loadWorld((WorldClient)null);

            if (flag) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
            }
            else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            }
        });
    }
}
