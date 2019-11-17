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
package org.spongepowered.mod.mixin.core.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(Teleporter.class)
public abstract class TeleporterMixin_Forge implements TeleporterBridge {

    @Shadow @Final protected WorldServer world;
    @Shadow public abstract void placeInPortal(Entity entityIn, float rotationYaw);
    @Shadow public abstract boolean placeInExistingPortal(Entity entityIn, float rotationYaw);
    @Shadow public abstract void placeEntity(World world, Entity entity, float yaw);

    @Override
    public void bridge$placeEntity(net.minecraft.world.World world, Entity entity, float yaw) {
        boolean didPort;

        if (this.bridge$isVanilla()) {
            if (entity instanceof EntityPlayerMP) {
                this.placeInPortal(entity, yaw);
                didPort = true;
            } else {
                if (((WorldServerBridge) this.world).bridge$getDimensionId() == 1) {
                    didPort = true;
                } else {
                    didPort = this.placeInExistingPortal(entity, yaw);
                }
            }
        } else {
            // Mods take over here, ones that extend Teleporter and do not implement ITeleporter on their own.
            this.placeEntity(world, entity, yaw);

            // Assume mod placements always "succeed"
            // TODO Maybe a way to check if they don't? Would be nice if Forge returned a boolean..
            didPort = true;
        }

        if (didPort) {
            ((IPhaseState) PhaseTracker.getInstance().getCurrentState()).markTeleported(PhaseTracker.getInstance().getCurrentContext());
        }
    }

    @Override
    public boolean bridge$isVanilla() {
        return ((ITeleporter) this).isVanilla();
    }
}
