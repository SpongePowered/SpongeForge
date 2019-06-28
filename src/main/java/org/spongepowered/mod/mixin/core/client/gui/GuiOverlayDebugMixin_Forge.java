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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.network.message.MessageTrackerDataRequest;
import org.spongepowered.common.network.message.SpongeMessageHandler;
import org.spongepowered.mod.bridge.client.gui.GUIOverlayDebugBridge_Forge;
import org.spongepowered.mod.bridge.client.MinecraftBridge_Forge;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class GuiOverlayDebugMixin_Forge implements GUIOverlayDebugBridge_Forge {

    private String forgeImpl$blockOwner = "";
    private String forgeImpl$blockNotifier = "";
    private BlockPos forgeImpl$cursorPos = new BlockPos(0, 0, 0);

    @Shadow @Final private Minecraft mc;

    @Inject(method = "<init>", at = @At(value = "RETURN") )
    private void forgeImpl$setDebugGui(final Minecraft mc, final CallbackInfo ci) {
        final MinecraftBridge_Forge spongeMc = (MinecraftBridge_Forge) mc;
        spongeMc.forgeBridge$setDebugGui((GuiOverlayDebug) (Object) this);
    }

    @Inject(method = "call()Ljava/util/List;", at = @At(value = "RETURN", ordinal = 1))
    private void forgeImpl$addOwnerInfo(final CallbackInfoReturnable<List<String>> cir) {
        final List<String> arraylist = cir.getReturnValue();
        if (this.mc.objectMouseOver != null
                && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK
                && this.mc.objectMouseOver.getBlockPos() != null) {
            final BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
            if (!blockpos1.equals(this.forgeImpl$cursorPos)) {
                SpongeMessageHandler.getChannel().sendToServer(
                        new MessageTrackerDataRequest(0, -1, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }
            arraylist.add("Block Owner: " + this.forgeImpl$blockOwner);
            arraylist.add("Block Notifier: " + this.forgeImpl$blockNotifier);
            this.forgeImpl$cursorPos = this.mc.objectMouseOver.getBlockPos();
        } else if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
            final Entity target = this.mc.objectMouseOver.entityHit;
            final BlockPos blockPos = target.getPosition();
            if (!blockPos.equals(this.forgeImpl$cursorPos)) {
                SpongeMessageHandler.getChannel().sendToServer(
                        new MessageTrackerDataRequest(1, target.getEntityId(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
            arraylist.add("Entity Owner: " + this.forgeImpl$blockOwner);
            arraylist.add("Entity Notifier: " + this.forgeImpl$blockNotifier);
            this.forgeImpl$cursorPos = blockPos;
        }
    }

    @Override
    public void forgeBridge$setPlayerTrackerData(final String owner, final String notifier) {
        this.forgeImpl$blockOwner = owner;
        this.forgeImpl$blockNotifier = notifier;
    }
}
