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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.mod.SpongeMod;

import java.util.Iterator;

import javax.annotation.Nullable;

@Mixin(value = SpongeImplHooks.class, remap = false)
public abstract class MixinSpongeImplHooks {

    private static Boolean deobfuscatedEnvironment;

    @Overwrite
    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block.hasTileEntity(state);
    }

    @Overwrite
    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity(world, pos);
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
    public static void handlePostChangeDimensionEvent(EntityPlayerMP playerIn, WorldServer fromWorld, WorldServer toWorld) {
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(playerIn, fromWorld.provider.getDimension(), toWorld.provider.getDimension());
    }

    @Overwrite
    public static boolean canDoLightning(WorldProvider provider, Chunk chunk) {
        return provider.canDoLightning(chunk);
    }

    @Overwrite
    public static boolean canDoRainSnowIce(WorldProvider provider, Chunk chunk) {
        return provider.canDoRainSnowIce(chunk);
    }

    @Overwrite
    public static Iterator<Chunk> getChunkIterator(WorldServer world) {
        return world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator());
    }


    @Overwrite
    public static boolean isDeobfuscatedEnvironment() {
        if (deobfuscatedEnvironment != null) {
            return deobfuscatedEnvironment;
        }

        deobfuscatedEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return deobfuscatedEnvironment;
    }

    @SuppressWarnings("deprecation")
    @Overwrite
    public static int getChunkPosLight(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos pos) {
        if (((IMixinBlock) blockState.getBlock()).requiresLocationCheckForLightValue()) {
            return blockState.getLightValue(worldObj, pos);
        }
        return blockState.getLightValue();
    }

    @SuppressWarnings("deprecation")
    @Overwrite
    public static int getChunkBlockLightOpacity(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos pos) {

        return blockState.getLightOpacity();
    }

    @SuppressWarnings("deprecation")
    @Overwrite
    public static int getChunkBlockLightOpacity(IBlockState state, net.minecraft.world.World worldObj, int x, int y, int z) {
        return state.getLightOpacity();
    }
}
