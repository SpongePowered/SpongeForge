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
package org.spongepowered.mod.mixin.core.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.mixin.core.entity.player.EntityPlayerMixin;

import javax.annotation.Nullable;

@Mixin(value = EntityPlayerMP.class, priority = 1001)
public abstract class EntityPlayerMPMixin_Forge extends EntityPlayerMixin implements ServerPlayerEntityBridge {

    @Shadow public NetHandlerPlayServer connection;
    @Shadow private boolean invulnerableDimensionChange;
    @Shadow private Vec3d enteredNetherPosition;
    @Shadow public boolean queuedEndExit;
    @Shadow private boolean seenCredits;
    @Shadow @Final public MinecraftServer server;
    @Shadow private int lastExperience;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;

    @Override
    public boolean bridge$usesCustomClient() {
        return this.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
    }

    @Inject(method = "sendSlotContents", at = @At("HEAD"))
    private void forgeImpl$detectSentChangesDuringCapture(final Container containerToSend, final int slotInd, final ItemStack itemstack1, final CallbackInfo ci) {
        final TrackedInventoryBridge tracked = (TrackedInventoryBridge) containerToSend;
        // todo - write a detect if container overrides detectAndSendChanges.
        final ContainerBridge bridge = (ContainerBridge) containerToSend;
        final Slot slot = bridge.bridge$getContainerSlot(slotInd);
        final ItemStack itemstack = ((net.minecraft.inventory.Slot) slot).getStack();
        if (tracked.bridge$capturingInventory()) {
            final ItemStackSnapshot originalItem = itemstack1.isEmpty() ? ItemStackSnapshot.NONE
                                                                        : ((org.spongepowered.api.item.inventory.ItemStack) (Object) itemstack1).createSnapshot();
            final ItemStackSnapshot newItem = itemstack.isEmpty() ? ItemStackSnapshot.NONE
                                                                  : ((org.spongepowered.api.item.inventory.ItemStack) (Object) itemstack).createSnapshot();

            org.spongepowered.api.item.inventory.Slot adapter = null;
            try {
                adapter = slot;
                SlotTransaction newTransaction = new SlotTransaction(adapter, originalItem, newItem);
                if (bridge.bridge$isShiftCrafting()) {
                    bridge.bridge$getPreviewTransactions().add(newTransaction);
                } else {
                    if (!bridge.bridge$getPreviewTransactions().isEmpty()) { // Check if Preview transaction is this transaction
                        final SlotTransaction previewTransaction = bridge.bridge$getPreviewTransactions().get(0);
                        if (previewTransaction.equals(newTransaction)) {
                            newTransaction = null;
                        }
                    }
                    if (newTransaction != null) {
                        tracked.bridge$getCapturedSlotTransactions().add(newTransaction);
                    }
                }
            } catch (final IndexOutOfBoundsException e) {
                SpongeImpl.getLogger().error("SlotIndex out of LensBounds! Did the Container change after creation?", e);
            }
        }
    }

    /**
     * @author Zidane - June 2019 - 1.12.2
     * @reason Re-route dimension changes to common hook
     */
    @Nullable
    @Overwrite(remap = false)
    public net.minecraft.entity.Entity changeDimension(final int toDimensionId, final ITeleporter teleporter) {
        if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension((EntityPlayerMP) (Object) this, toDimensionId)) {
            return (EntityPlayerMP) (Object) this;
        }

        this.invulnerableDimensionChange = true;

        if (this.dimension == 0 && toDimensionId == -1) {
            this.enteredNetherPosition = new Vec3d(this.posX, this.posY, this.posZ);
        } else if (this.dimension != -1 && toDimensionId != 0) {
            this.enteredNetherPosition = null;
        }

        if (this.dimension == 1 && toDimensionId == 1 && teleporter.isVanilla()) {
            this.world.removeEntity((EntityPlayerMP) (Object) this);

            if (!this.queuedEndExit) {
                this.queuedEndExit = true;
                this.connection.sendPacket(new SPacketChangeGameState(4, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }

            return (EntityPlayerMP) (Object) this;
        } else {
            // Sponge Start - Use Sponge Common hook instead of using the dimension id detection
            // coming from the overworld to the end.
            // if (this.dimension == 0 && dimensionIn == 1) {
            //     dimensionIn = 1;
            // }
            // this.server.getPlayerList().transferPlayerToDimension(this, dimensionIn, teleporter);
            EntityUtil.transferPlayerToWorld((EntityPlayerMP) (Object) this, null, this.server.getWorld(toDimensionId), (ForgeITeleporterBridge) teleporter);
            // Sponge end
            this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
            return (EntityPlayerMP) (Object) this;
        }
    }
}
