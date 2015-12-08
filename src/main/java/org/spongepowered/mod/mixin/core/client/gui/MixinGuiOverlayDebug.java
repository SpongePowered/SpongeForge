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

import com.google.common.collect.Lists;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.client.interfaces.IMixinGuiOverlayDebug;
import org.spongepowered.mod.client.interfaces.IMixinMinecraft;
import org.spongepowered.mod.network.SpongeModMessageHandler;
import org.spongepowered.mod.network.message.MessageTrackerDataRequest;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug implements IMixinGuiOverlayDebug {

    private String blockOwner = "";
    private String blockNotifier = "";
    private BlockPos cursorPos = new BlockPos(0, 0, 0);

    @Shadow private Minecraft mc;

    @Shadow public abstract boolean isReducedDebug();

    @Inject(method = "<init>", at = @At(value = "RETURN") )
    public void onConstructDebugGui(Minecraft mc, CallbackInfo ci) {
        IMixinMinecraft spongeMc = (IMixinMinecraft) mc;
        spongeMc.setDebugGui((GuiOverlayDebug) (Object) this);
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Sends a packet to server requesting block tracking data. If
     * player has permission, the block owner and notifier data will be 
     * received and displayed on debug screen.
     */
    @Overwrite
    protected List<String> call() {
        BlockPos blockpos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY,
                this.mc.getRenderViewEntity().posZ);

        if (this.isReducedDebug()) {
            return Lists.newArrayList(new String[] {"Minecraft 1.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")",
                    this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(),
                    "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(),
                    this.mc.theWorld.getProviderName(), "", String.format("Chunk-relative: %d %d %d", new Object[] {
                            Integer.valueOf(blockpos.getX() & 15), Integer.valueOf(blockpos.getY() & 15), Integer.valueOf(blockpos.getZ() & 15)})});
        } else {
            Entity entity = this.mc.getRenderViewEntity();
            EnumFacing enumfacing = entity.getHorizontalFacing();
            String s = "Invalid";

            switch (enumfacing.ordinal() - 1) {
                case 1:
                    s = "Towards negative Z";
                    break;
                case 2:
                    s = "Towards positive Z";
                    break;
                case 3:
                    s = "Towards negative X";
                    break;
                case 4:
                    s = "Towards positive X";
            }

            ArrayList<String> arraylist =
                    Lists.newArrayList(
                            new String[] {"Minecraft 1.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")",
                                    this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(),
                                    "P: " + this.mc.effectRenderer.getStatistics() + ". T: "
                                            + this.mc.theWorld
                                                    .getDebugLoadedEntities(),
                                    this.mc.theWorld.getProviderName(), "",
                                    String.format("XYZ: %.3f / %.5f / %.3f",
                                            new Object[] {Double.valueOf(this.mc.getRenderViewEntity().posX),
                                                    Double.valueOf(this.mc.getRenderViewEntity().getEntityBoundingBox().minY),
                                                    Double.valueOf(this.mc.getRenderViewEntity().posZ)}),
                    String.format("Block: %d %d %d",
                            new Object[] {Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ())}),
                    String.format("Chunk: %d %d %d in %d %d %d",
                            new Object[] {Integer.valueOf(blockpos.getX() & 15), Integer.valueOf(blockpos.getY() & 15),
                                    Integer.valueOf(blockpos.getZ() & 15), Integer.valueOf(blockpos.getX() >> 4),
                                    Integer.valueOf(blockpos.getY() >> 4), Integer.valueOf(blockpos.getZ() >> 4)}),
                    String.format("Facing: %s (%s) (%.1f / %.1f)",
                            new Object[] {enumfacing, s, Float.valueOf(MathHelper.wrapAngleTo180_float(entity.rotationYaw)),
                                    Float.valueOf(MathHelper.wrapAngleTo180_float(entity.rotationPitch))})});

            if (this.mc.theWorld != null && this.mc.theWorld.isBlockLoaded(blockpos)) {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(blockpos);
                arraylist.add("Biome: " + chunk.getBiome(blockpos, this.mc.theWorld.getWorldChunkManager()).biomeName);
                arraylist.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, "
                        + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
                DifficultyInstance difficultyinstance = this.mc.theWorld.getDifficultyForLocation(blockpos);

                if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
                    EntityPlayerMP entityplayermp =
                            this.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(this.mc.thePlayer.getUniqueID());

                    if (entityplayermp != null) {
                        difficultyinstance = entityplayermp.worldObj.getDifficultyForLocation(new BlockPos(entityplayermp));
                    }
                }

                arraylist.add(String.format("Local Difficulty: %.2f (Day %d)", new Object[] {
                        Float.valueOf(difficultyinstance.getAdditionalDifficulty()), Long.valueOf(this.mc.theWorld.getWorldTime() / 24000L)}));
            }

            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
                arraylist.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && this.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
                arraylist.add(String.format("Looking at: %d %d %d",
                        new Object[] {Integer.valueOf(blockpos1.getX()), Integer.valueOf(blockpos1.getY()), Integer.valueOf(blockpos1.getZ())}));
                // Sponge start
                if (!this.mc.objectMouseOver.getBlockPos().equals(this.cursorPos)) {
                    SpongeModMessageHandler.INSTANCE
                            .sendToServer(new MessageTrackerDataRequest(0, -1, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
                }
                arraylist.add("Block Owner: " + this.blockOwner);
                arraylist.add("Block Notifier: " + this.blockNotifier);
                this.cursorPos = this.mc.objectMouseOver.getBlockPos();
                // Sponge end
            } else if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity target = this.mc.objectMouseOver.entityHit;
                BlockPos blockPos = target.getPosition();
                if (!blockPos.equals(this.cursorPos)) {
                    SpongeModMessageHandler.INSTANCE
                            .sendToServer(new MessageTrackerDataRequest(1, target.getEntityId(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                }
                arraylist.add("Entity Owner: " + this.blockOwner);
                arraylist.add("Entity Notifier: " + this.blockNotifier);
                this.cursorPos = blockPos;
            }

            return arraylist;
        }
    }

    @Override
    public void setPlayerTrackerData(String owner, String notifier) {
        this.blockOwner = owner;
        this.blockNotifier = notifier;
    }
}
