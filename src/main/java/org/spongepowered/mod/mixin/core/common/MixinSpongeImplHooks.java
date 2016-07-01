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
package org.spongepowered.mod.mixin.core.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.WorldEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.mod.SpongeMod;

import javax.annotation.Nullable;

@Mixin(value = SpongeImplHooks.class, remap = false)
public abstract class MixinSpongeImplHooks {

    @Overwrite
    public static LoadWorldEvent createLoadWorldEvent(World world) {
        return (LoadWorldEvent) new WorldEvent.Load((net.minecraft.world.World) world);
    }

    @Overwrite
    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block.hasTileEntity(state);
    }

    @Overwrite
    public static int getBlockLightValue(Block block, BlockPos pos, IBlockAccess world) {
        return block.getLightValue(world, pos);
    }

    @Overwrite
    public static int getBlockLightOpacity(Block block, IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity(world, pos);
    }

    @Overwrite
    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return tile.shouldRefresh(world, pos, oldState, newState);
    }

    @Overwrite
    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        return block.createTileEntity(world, state);
    }

    @Overwrite
    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        return net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(entityPlayer, targetEntity);
    }

    @Overwrite
    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        return entity.isCreatureType(type, false);
    }

    @Overwrite
    public static boolean isFakePlayer(Entity entity) {
        return entity instanceof FakePlayer;
    }

    @Overwrite
    public static boolean onDroppedByPlayer(Item item, ItemStack stack, EntityPlayer player) {
        if (item == null) {
            return false;
        }

        return item.onDroppedByPlayer(stack, player);
    }

    @Overwrite
    public static String getModIdFromClass(Class<?> clazz) {
        return SpongeMod.instance.getModIdFromClass(clazz);
    }

    @Overwrite
    public static void registerPortalAgentType(@Nullable Teleporter teleporter) {
        if (teleporter == null) {
            return;
        }

        // ignore default
        if (PortalAgentTypes.DEFAULT.equals(((PortalAgent) teleporter).getType())) {
            return;
        }

        // handle mod registration
        PortalAgentRegistryModule.getInstance().validatePortalAgent(teleporter);
    }

    @Overwrite
    public static boolean canDoLightning(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return provider.canDoLightning(chunk);
    }

    @Overwrite
    public static boolean canDoRainSnowIce(WorldProvider provider, net.minecraft.world.chunk.Chunk chunk) {
        return provider.canDoRainSnowIce(chunk);
    }
}
