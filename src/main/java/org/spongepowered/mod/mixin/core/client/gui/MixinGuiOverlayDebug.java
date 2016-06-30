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
import org.spongepowered.mod.client.interfaces.IMixinGuiOverlayDebug;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug implements IMixinGuiOverlayDebug {

    private String blockOwner = "";
    private String blockNotifier = "";
    private BlockPos cursorPos = new BlockPos(0, 0, 0);

    @Shadow @Final private Minecraft mc;

    @Inject(method = "<init>", at = @At(value = "RETURN") )
    public void onConstructDebugGui(Minecraft mc, CallbackInfo ci) {
        IMixinMinecraft spongeMc = (IMixinMinecraft) mc;
        spongeMc.setDebugGui((GuiOverlayDebug) (Object) this);
    }

    @Inject(method = "call()Ljava/util/List;", at = @At(value = "RETURN", ordinal = 1))
    private void addOwnerInfo(CallbackInfoReturnable<List<String>> cir) {
        List<String> arraylist = cir.getReturnValue();
        if (this.mc.objectMouseOver != null
                && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK
                && this.mc.objectMouseOver.getBlockPos() != null) {
            BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
            if (!blockpos1.equals(this.cursorPos)) {
                SpongeMessageHandler.getChannel().sendToServer(
                        new MessageTrackerDataRequest(0, -1, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }
            arraylist.add("Block Owner: " + this.blockOwner);
            arraylist.add("Block Notifier: " + this.blockNotifier);
            this.cursorPos = this.mc.objectMouseOver.getBlockPos();
        } else if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
            Entity target = this.mc.objectMouseOver.entityHit;
            BlockPos blockPos = target.getPosition();
            if (!blockPos.equals(this.cursorPos)) {
                SpongeMessageHandler.getChannel().sendToServer(
                        new MessageTrackerDataRequest(1, target.getEntityId(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
            arraylist.add("Entity Owner: " + this.blockOwner);
            arraylist.add("Entity Notifier: " + this.blockNotifier);
            this.cursorPos = blockPos;
        }
    }

    @Override
    public void setPlayerTrackerData(String owner, String notifier) {
        this.blockOwner = owner;
        this.blockNotifier = notifier;
    }
}
