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
package org.spongepowered.mod.mixin.core.client.gui;

import net.minecraft.client.gui.GuiOptions;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.WorldManager;

/**
 * In Sponge's Multi-World implementation, we give each world its own {@link WorldInfo}. While this is a great idea all around (allows us to
 * make things world-specific), it has some unintended consequences, such as breaking being able to change your difficulty in SinglePlayer. While
 * most people would tell us to not worry about it as Sponge isn't really client-side oriented, I have no tolerance for breaking Vanilla
 * functionality (barring some exceptions) so this cannot stand.
 */
@Mixin(GuiOptions.class)
public abstract class MixinGuiOptions {

  @Redirect(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setDifficulty(Lnet/minecraft/world/EnumDifficulty;)V"))
  private void syncDifficulty(WorldInfo worldInfo, EnumDifficulty newDifficulty) {
    // Sync server
    WorldManager.getWorlds().forEach(worldServer -> WorldManager.adjustWorldForDifficulty(worldServer, newDifficulty, true));
    // Sync client
    worldInfo.setDifficulty(newDifficulty);
  }

  @Redirect(method = "confirmClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setDifficultyLocked(Z)V"))
  private void syncDifficultyLocked(WorldInfo worldInfo, boolean locked) {
    // Sync server
    WorldManager.getWorlds().forEach(worldServer -> worldServer.getWorldInfo().setDifficultyLocked(locked));
    // Sync client
    worldInfo.setDifficultyLocked(locked);
  }
}
