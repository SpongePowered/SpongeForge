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
package org.spongepowered.mod.mixin.core.forge.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.mod.bridge.util.ForgeBlockSnapshotBridge_Forge;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = net.minecraftforge.common.util.BlockSnapshot.class, remap = false)
public abstract class BlockSnapshotMixin_Forge implements ForgeBlockSnapshotBridge_Forge {

    @Shadow @Final private BlockPos pos;
    @Shadow @Final private NBTTagCompound nbt;
    @Shadow @Nullable private  IBlockState replacedBlock;

    @Shadow public abstract TileEntity getTileEntity();

    @Shadow public abstract net.minecraft.world.World getWorld();

    @Override
    public BlockSnapshot forgeBridge$toSpongeSnapshot() {
        Location<World> location = new Location<>((World) this.getWorld(), VecHelper.toVector3i(this.pos));
        SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState(this.replacedBlock)
                .worldId(location.getExtent().getUniqueId())
                .position(location.getBlockPosition());
        if (this.nbt != null) {
            builder.unsafeNbt(this.nbt);
        }
        TileEntity te = getTileEntity();
        if (te != null) {
            if (!te.hasWorld()) {
                te.setWorld(this.getWorld());
            }
            for (DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) te).bridge$getCustomManipulators()) {
                builder.add(manipulator);
            }
        }
        return builder.build();
    }
}
