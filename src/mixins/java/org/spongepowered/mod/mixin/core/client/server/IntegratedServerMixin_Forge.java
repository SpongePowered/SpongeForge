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
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.server.integrated.IntegratedServerBridge;
import org.spongepowered.common.mixin.core.server.MinecraftServerMixin;

@NonnullByDefault
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin_Forge extends MinecraftServerMixin implements IntegratedServerBridge {

    @Shadow @Final private WorldSettings worldSettings;
    @Shadow @Final private Minecraft mc;

    private boolean forgeImpl$isNewSave;

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
    public void loadAllWorlds(final String overworldFolder, final String unused, final long seed, final WorldType type, final String generator) {
        super.loadAllWorlds(overworldFolder, unused, seed, type, generator);
    }

    @Override
    public WorldSettings bridge$getSettings() {
        return this.worldSettings;
    }

    @Override
    public void bridge$markNewSave() {
        this.forgeImpl$isNewSave = true;
    }

    @Override
    public boolean bridge$isNewSave() {
        return this.forgeImpl$isNewSave;
    }
}
