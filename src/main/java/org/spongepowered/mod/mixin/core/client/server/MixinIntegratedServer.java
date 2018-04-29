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
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinIntegratedServer;
import org.spongepowered.common.mixin.core.server.MixinMinecraftServer;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

@NonnullByDefault
@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MixinMinecraftServer implements IMixinIntegratedServer {
    @Shadow @Final private WorldSettings worldSettings;
    @Shadow @Final private Minecraft mc;
    private boolean isNewSave;

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
    public void loadAllWorlds(String overworldFolder, String unused, long seed, WorldType type, String generator) {
        super.loadAllWorlds(overworldFolder, unused, seed, type, generator);
    }

    @Override
    public void shutdown() {
        if (!this.mc.isIntegratedServerRunning()) {
            return;
        }

        this.mc.addScheduledTask(() -> {
            // Vanilla calls this, but it's completely unecessary.
            // It can also inherently racy, and can cause the client
            // thread to hang in unusual circumstances. For more information,
            // see github.com/Aaron1011/McTester
            /*if (this.mc.world != null) {
                this.mc.world.sendQuittingDisconnectingPacket();
            }*/

            this.mc.loadWorld((WorldClient)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        });
    }

    @Override
    public void shutdown(Text kickMessage) {
        checkNotNull(kickMessage);
        ((IMixinMinecraft) Minecraft.getMinecraft()).setSinglePlayerKickMessage(kickMessage);
        shutdown();
    }

    @Override
    public WorldSettings getSettings() {
        return this.worldSettings;
    }

    @Override
    public void markNewSave() {
        this.isNewSave = true;
    }

    @Override
    public boolean isNewSave() {
        return this.isNewSave;
    }
}
