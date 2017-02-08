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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.RecipeSorter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = SpongeImplHooks.class, remap = false)
public abstract class MixinSpongeImplHooks {

    private static Boolean deobfuscatedEnvironment;

    @Overwrite
    public static boolean isVanilla() {
        return false;
    }

    @Overwrite
    public static boolean isDeobfuscatedEnvironment() {
        if (deobfuscatedEnvironment != null) {
            return deobfuscatedEnvironment;
        }

        deobfuscatedEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return deobfuscatedEnvironment;
    }

    @Overwrite
    public static String getModIdFromClass(Class<?> clazz) {
        return StaticMixinForgeHelper.getModIdFromClass(clazz);
    }

    // Entity

    @Overwrite
    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        return entity.isCreatureType(type, false);
    }

    @Overwrite
    public static boolean isFakePlayer(Entity entity) {
        return entity instanceof FakePlayer;
    }

    @Overwrite
    public static void firePlayerJoinSpawnEvent(EntityPlayerMP playerMP) {
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new EntityJoinWorldEvent(playerMP, playerMP.getEntityWorld()), true);
    }

    @Overwrite
    public static void handlePostChangeDimensionEvent(EntityPlayerMP playerIn, WorldServer fromWorld, WorldServer toWorld) {
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(playerIn, fromWorld.provider.getDimension(), toWorld.provider.getDimension());
    }

    @Overwrite
    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        return net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(entityPlayer, targetEntity);
    }

    @Overwrite
    public static double getBlockReachDistance(EntityPlayerMP player) {
        return player.interactionManager.getBlockReachDistance();
    }

    // Entity registry

    @Overwrite
    public static Class<? extends Entity> getEntityClass(ResourceLocation name) {
        return EntityList.getClass(name);
    }

    @Overwrite
    public static int getEntityId(Class<? extends Entity> entityClass) {
        return EntityList.getID(entityClass);
    }

    // Block

    @Overwrite
    public static boolean isBlockFlammable(Block block, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.isFlammable(world, pos, face);
    }

    @Overwrite
    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity(world, pos);
    }

    // Tile entity

    @Overwrite
    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        return block.createTileEntity(world, state);
    }

    @Overwrite
    public static boolean hasBlockTileEntity(Block block, IBlockState state) {
        return block.hasTileEntity(state);
    }

    @Overwrite
    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return tile.shouldRefresh(world, pos, oldState, newState);
    }

    @Overwrite
    public static void onTileChunkUnload(TileEntity te) {
        te.onChunkUnload();
    }

    // World

    @Overwrite
    public static Iterator<Chunk> getChunkIterator(WorldServer world) {
        return world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator());
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

    // World provider

    @Overwrite
    public static boolean canDoLightning(WorldProvider provider, Chunk chunk) {
        return provider.canDoLightning(chunk);
    }

    @Overwrite
    public static boolean canDoRainSnowIce(WorldProvider provider, Chunk chunk) {
        return provider.canDoRainSnowIce(chunk);
    }

    @Overwrite
    public static int getRespawnDimension(WorldProvider targetDimension, EntityPlayerMP player) {
        return targetDimension.getRespawnDimension(player);
    }

    @Overwrite
    public static BlockPos getRandomizedSpawnPoint(WorldServer world) {
        return world.provider.getRandomizedSpawnPoint();
    }

    @Overwrite
    public static MapStorage getWorldMapStorage(World world) {
        return world.getPerWorldStorage();
    }

    // Crafting

    @Overwrite
    public static Optional<ItemStack> getContainerItem(ItemStack itemStack) {
        net.minecraft.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
        net.minecraft.item.ItemStack nmsContainerStack = ForgeHooks.getContainerItem(nmsStack);

        if(nmsContainerStack.isEmpty())
            return Optional.empty();
        else
            return Optional.of(ItemStackUtil.fromNative(nmsContainerStack));
    }

    @Overwrite
    public static void onCraftingRecipeRegister(CraftingRecipe recipe) {
        // Generate a unique name for this recipe and register it in the RecipeSorter
        int index = Sponge.getRegistry().getCraftingRecipeRegistry().getRecipes().size() - 1;
        String name = recipe.getClass().getCanonicalName() + "@" + index;
        RecipeSorter.Category category = recipe instanceof ShapedRecipes && recipe instanceof ShapedCraftingRecipe
                ? RecipeSorter.Category.SHAPED : RecipeSorter.Category.SHAPELESS;
        RecipeSorter.register(name, recipe.getClass(), category, "");
    }
}
