/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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

package org.spongepowered.mod.service.persistence.builders.block.tile;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockJukebox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.service.persistence.DataSerializable;
import org.spongepowered.api.service.persistence.DataSerializableBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.DataView;
import org.spongepowered.api.world.World;

import java.util.Map;

public abstract class AbstractTileBuilder<T extends org.spongepowered.api.block.data.TileEntity & DataSerializable> implements DataSerializableBuilder<T> {

    private static final Map<Class<? extends TileEntity>, BlockType> classToTypeMap = Maps.newHashMap();

    protected final Game game;

    protected AbstractTileBuilder(Game game) {
        this.game = game;
    }

    static {
        // These are our known block types. We need to find a way to support the mod ones
        addBlockMapping(TileEntityDropper.class, BlockTypes.DROPPER);
        addBlockMapping(TileEntityChest.class, BlockTypes.CHEST);
        addBlockMapping(TileEntityEnderChest.class, BlockTypes.ENDER_CHEST);
        addBlockMapping(BlockJukebox.TileEntityJukebox.class, BlockTypes.JUKEBOX);
        addBlockMapping(TileEntityDispenser.class, BlockTypes.DISPENSER);
        addBlockMapping(TileEntityDropper.class, BlockTypes.DROPPER);
        addBlockMapping(TileEntitySign.class, BlockTypes.STANDING_SIGN);
        addBlockMapping(TileEntityMobSpawner.class, BlockTypes.MOB_SPAWNER);
        addBlockMapping(TileEntityNote.class, BlockTypes.NOTEBLOCK);
        addBlockMapping(TileEntityPiston.class, BlockTypes.PISTON);
        addBlockMapping(TileEntityFurnace.class, BlockTypes.FURNACE);
        addBlockMapping(TileEntityBrewingStand.class, BlockTypes.BREWING_STAND);
        addBlockMapping(TileEntityEnchantmentTable.class, BlockTypes.ENCHANTING_TABLE);
        addBlockMapping(TileEntityEndPortal.class, BlockTypes.END_PORTAL);
        addBlockMapping(TileEntityCommandBlock.class, BlockTypes.COMMAND_BLOCK);
        addBlockMapping(TileEntityBeacon.class, BlockTypes.BEACON);
        addBlockMapping(TileEntitySkull.class, BlockTypes.SKULL);
        addBlockMapping(TileEntityDaylightDetector.class, BlockTypes.DAYLIGHT_DETECTOR);
        addBlockMapping(TileEntityHopper.class, BlockTypes.HOPPER);
        addBlockMapping(TileEntityComparator.class, BlockTypes.UNPOWERED_COMPARATOR);
        addBlockMapping(TileEntityFlowerPot.class, BlockTypes.FLOWER_POT);
        addBlockMapping(TileEntityBanner.class, BlockTypes.STANDING_BANNER);
    }

    private static void addBlockMapping(Class<? extends TileEntity> tileClass, BlockType blocktype) {
        classToTypeMap.put(tileClass, blocktype);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> build(DataView container) throws InvalidDataException {
        checkNotNull(container);
        DataQuery worldQuery = new DataQuery("world");
        DataQuery xPosQuery = new DataQuery("x");
        DataQuery yPosQuery = new DataQuery("y");
        DataQuery zPosQuery = new DataQuery("z");
        DataQuery tileTypeQuery = new DataQuery("tileType");
        if (!container.contains(tileTypeQuery) || !container.contains(worldQuery) || !container.contains(xPosQuery) || !container.contains(yPosQuery)
            || !container.contains(zPosQuery)) {
            throw new InvalidDataException("The provided container does not contain the data to make a TileEntity!");
        }
        String worldName = container.getString(worldQuery).get();
        Optional<World> worldOptional = this.game.getServer().get().getWorld(worldName);
        if (!worldOptional.isPresent()) {
            throw new InvalidDataException("The provided container references a world that does not exist!");
        }
        int x = container.getInt(xPosQuery).get();
        int y = container.getInt(yPosQuery).get();
        int z = container.getInt(zPosQuery).get();
        // TODO find a better way to do this... Hopefully with API PR #475
        Class<? extends TileEntity> clazz = (Class<? extends TileEntity>) TileEntity.nameToClassMap.get(container.getString(tileTypeQuery).get());
        if (clazz == null) {
            return Optional.absent(); // TODO throw exception maybe?
        }
        BlockType type = classToTypeMap.get(clazz);
        if (type == null) {
            return Optional.absent(); // TODO throw exception maybe?
        }
        // Now we should be ready to actually deserialize the TileEntity with the right block.
        worldOptional.get().getFullBlock(x, y, z).replaceWith(type);
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tileEntity = ((net.minecraft.world.World) worldOptional.get()).getTileEntity(pos);
        if (tileEntity == null) {
            return Optional.absent(); // TODO throw exception maybe?
        } else {
            // We really need to validate only after the implementing class deems it ready...
            tileEntity.invalidate();
            return Optional.of((T) tileEntity);
        }
    }
}
