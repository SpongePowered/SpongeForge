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
package org.spongepowered.mod.mixin.core.tileentity;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Set;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin_Forge implements DataCompoundHolder {

    @Shadow(remap = false) private NBTTagCompound customTileData;

    @Shadow(remap = false) public abstract NBTTagCompound getTileData();

    private boolean forge$isGettingTileData = false;
    private static final Set<TileEntityType> FORGEIMPL$REPORTED_RECURSIVE_TILES = new ConcurrentSet<>();


    @Override
    public boolean data$hasRootCompound() {
        return this.customTileData != null;
    }

    @Override
    public NBTTagCompound data$getRootCompound() {
        try {
            if (this.forge$isGettingTileData) {
                // A short circuit because some mod is overriding `getTileData()` and causing recursion
                this.forge$isGettingTileData = false;
                return this.customTileData;
            }
            this.forge$isGettingTileData = true;
            final NBTTagCompound tileData = getTileData();
            this.forge$isGettingTileData = false;
            return tileData;
        } catch (StackOverflowError error) {
            // Basically at this point, it means a mod has cause infinite
            // recursion by recursively calling `writeToNbt` which calls getTileData...
            // So, let's just redo it entirely on our own.
            final TileEntityType type = ((org.spongepowered.api.block.tileentity.TileEntity) this).getType();
            if (!FORGEIMPL$REPORTED_RECURSIVE_TILES.contains(type)) {
                FORGEIMPL$REPORTED_RECURSIVE_TILES.add(type);
                final PrettyPrinter printer = new PrettyPrinter(60).add("Recursive TileEntity Error").centre().hr()
                    .addWrapped(70, "Hi! We're so sorry about this, but, Sponge is not at fault for "
                                    + "trying to do things the right way here, but we have a mod "
                                    + "that is causing infinite recursion when we're trying to take "
                                    + "a snapshot of this TileEntity by overriding getTileData(). "
                                    + "Fortunately, we can try to work around this a little, but "
                                    + "the mod author must be made aware about the issue as it is "
                                    + "an unintended side effect.")
                    .add()
                    .add(" %s : %s", "Affected TileEntity Type", type)
                    .add()
                    .addWrapped(60, "What can be done, however, is provide the offending mod "
                                    + "author some information about how to avoid this being printed out. "
                                    + "Please link the following:")
                    .table("Explanation", "Link")
                    .tr("Initial issue with Jurassicraft", "https://github.com/JurassiCraftTeam/JurassiCraft2/issues/561")
                    .tr("MinecraftForge PR adding getTileData()", "https://github.com/MinecraftForge/MinecraftForge/pull/1755")
                    .tr("Explanation from Sponge Devs", "https://github.com/JurassiCraftTeam/JurassiCraft2/issues/561#issuecomment-483715610")
                    .tr("SpongeForge's Usage of getTileData()", "https://github.com/SpongePowered/SpongeCommon/blob/stable-7/src/main/java/org/spongepowered/common/interfaces/block/tile/IMixinTileEntity.java#L45-L65")
                    .tr("Example of incorrect implementation of getTileData()", "https://github.com/JurassiCraftTeam/JurassiCraft2/blob/f2575cfb72008092e6470923c0de9f05f771bfc9/src/main/java/org/jurassicraft/server/block/entity/TourRailBlockEntity.java#L40")
                    .add()
                    .add("Please remember, this isn't a sponge bug, it's sponge exposing a bug with a mod.")
                    .add();
                PhaseTracker.printPhaseStackWithException(PhaseTracker.getInstance(), printer, error);
                printer.log(SpongeImpl.getLogger(), Level.WARN);
            }
            this.customTileData = new NBTTagCompound();
            return this.customTileData;
        }
    }


}
