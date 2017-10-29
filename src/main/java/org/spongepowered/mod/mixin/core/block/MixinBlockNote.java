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
package org.spongepowered.mod.mixin.core.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockNote;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.type.SpongeInstrumentType;
import org.spongepowered.mod.event.NoteBlockPlayEvent;

@Mixin(BlockNote.class)
public abstract class MixinBlockNote extends BlockContainer {

    @Shadow protected abstract SoundEvent getInstrument(int eventId);

    protected MixinBlockNote(Material materialIn) {
        super(materialIn);
    }

    /**
     * @author Cybermaxke
     * @reason Forge removes the possibility to play the added PLING
     *         instrument when throwing the event.
     */
    @Overwrite
    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        // SPONGE & FORGE START
        final int plingId = ((SpongeInstrumentType) InstrumentTypes.PLING).getInternalId();
        final NoteBlockPlayEvent e = new NoteBlockPlayEvent(worldIn, pos, state, param, id, id == plingId);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        }
        id = e.pling ? plingId : e.getInstrument().ordinal();
        param = e.getVanillaNoteId();
        // SPONGE & FORGE END
        final float f = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);
        worldIn.playSound(null, pos, getInstrument(id), SoundCategory.RECORDS, 3.0F, f);
        worldIn.spawnParticle(EnumParticleTypes.NOTE, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.2D,
                (double) pos.getZ() + 0.5D, (double) param / 24.0D, 0.0D, 0.0D);
        return true;
    }
}
