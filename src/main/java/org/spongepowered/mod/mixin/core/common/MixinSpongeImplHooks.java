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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.command.SpongeCommandFactory;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.block.TileEntityInvalidatingPhaseState;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.common.item.inventory.util.InventoryUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.util.SpawnerSpawnType;
import org.spongepowered.mod.command.SpongeForgeCommand;
import org.spongepowered.mod.interfaces.IMixinBlock;
import org.spongepowered.mod.interfaces.IMixinEventBus;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.item.inventory.adapter.IItemHandlerAdapter;
import org.spongepowered.mod.mixin.core.forge.IMixinVillagerRegistry;
import org.spongepowered.mod.plugin.SpongeModPluginContainer;
import org.spongepowered.mod.util.StaticMixinForgeHelper;
import org.spongepowered.mod.util.WrappedArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@Mixin(value = SpongeImplHooks.class, remap = false)
public abstract class MixinSpongeImplHooks {

    private static Boolean deobfuscatedEnvironment;

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isVanilla() {
        return false;
    }

    /**
     * @author gabizou - July 9th, 2018
     * @reason During client shutdown or the integrated server shut down, we have
     * to be able to detect the integrated server is shutting down and we should not
     * be bothering with the phase tracker or cause stack manager.
     * @return
     */
    @Overwrite
    public static boolean isClientAvailable() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isDeobfuscatedEnvironment() {
        if (deobfuscatedEnvironment != null) {
            return deobfuscatedEnvironment;
        }

        deobfuscatedEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return deobfuscatedEnvironment;
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static String getModIdFromClass(Class<?> clazz) {
        return StaticMixinForgeHelper.getModIdFromClass(clazz);
    }

    // Entity

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        return entity.isCreatureType(type, false);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isFakePlayer(Entity entity) {
        return entity instanceof FakePlayer;
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void firePlayerJoinSpawnEvent(EntityPlayerMP playerMP) {
        ((IMixinEventBus) MinecraftForge.EVENT_BUS).post(new EntityJoinWorldEvent(playerMP, playerMP.getEntityWorld()), true);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void handlePostChangeDimensionEvent(EntityPlayerMP playerIn, WorldServer fromWorld, WorldServer toWorld) {
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(playerIn, fromWorld.provider.getDimension(), toWorld.provider.getDimension());
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        return net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(entityPlayer, targetEntity);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static double getBlockReachDistance(EntityPlayerMP player) {
        return player.interactionManager.getBlockReachDistance();
    }

    // Entity registry

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Nullable
    @Overwrite
    public static Class<? extends Entity> getEntityClass(ResourceLocation name) {
        return EntityList.getClass(name);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static int getEntityId(Class<? extends Entity> entityClass) {
        return EntityList.getID(entityClass);
    }

    // Block

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isBlockFlammable(Block block, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.isFlammable(world, pos, face);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity(world, pos);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    public static int getChunkPosLight(IBlockState blockState, net.minecraft.world.World worldObj, BlockPos pos) {
        if (((IMixinBlock) blockState.getBlock()).requiresLocationCheckForLightValue()) {
            return blockState.getLightValue(worldObj, pos);
        }
        return blockState.getLightValue();
    }

    // Tile entity

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Nullable
    @Overwrite
    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        return block.createTileEntity(world, state);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean hasBlockTileEntity(Block block, IBlockState state) {
        return block.hasTileEntity(state);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return tile.shouldRefresh(world, pos, oldState, newState);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void onTileChunkUnload(TileEntity te) {
        if (!te.getWorld().isRemote) {
            try (final PhaseContext<?> o = BlockPhase.State.TILE_CHUNK_UNLOAD.createPhaseContext()
                .source(te)) {
                o.buildAndSwitch();
                te.onChunkUnload();
            }
        }
    }

    // World

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static Iterator<Chunk> getChunkIterator(WorldServer world) {
        return world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator());
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void registerPortalAgentType(@Nullable IMixinITeleporter teleporter) {
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

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean canDoLightning(WorldProvider provider, Chunk chunk) {
        return provider.canDoLightning(chunk);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean canDoRainSnowIce(WorldProvider provider, Chunk chunk) {
        return provider.canDoRainSnowIce(chunk);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static int getRespawnDimension(WorldProvider targetDimension, EntityPlayerMP player) {
        return targetDimension.getRespawnDimension(player);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static BlockPos getRandomizedSpawnPoint(WorldServer world) {
        return world.provider.getRandomizedSpawnPoint();
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static MapStorage getWorldMapStorage(World world) {
        return world.getPerWorldStorage();
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static int countEntities(WorldServer worldServer, net.minecraft.entity.EnumCreatureType type, boolean forSpawnCount) {
        return worldServer.countEntities(type, forSpawnCount);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static int getMaxSpawnPackSize(EntityLiving entityLiving) {
        return net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(entityLiving);
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static SpawnerSpawnType canEntitySpawnHere(EntityLiving entityLiving, boolean entityNotColliding) {
        final World world = entityLiving.world;
        final float x = (float) entityLiving.posX;
        final float y = (float) entityLiving.posY;
        final float z = (float) entityLiving.posZ;
        net.minecraftforge.fml.common.eventhandler.Event.Result canSpawn = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(entityLiving, world, x, y, z, false);
        if (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW || (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT && (entityLiving.getCanSpawnHere()) && entityNotColliding)) {
            if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityLiving, world, x, y, z)) {
                return SpawnerSpawnType.NORMAL;
            }
            return SpawnerSpawnType.SPECIAL;
        }

        return SpawnerSpawnType.NONE;
    }

    // Copied from Forge's World patches

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static void onEntityError(Entity entity, CrashReport crashReport) {
        if (ForgeModContainer.removeErroringEntities) {
            // Sponge - fix https://github.com/MinecraftForge/MinecraftForge/issues/3713
            net.minecraftforge.fml.relauncher.FMLRelaunchLog.log.getLogger().log(Level.ERROR, crashReport.getCompleteReport());
            entity.getEntityWorld().removeEntity(entity);
        } else {
            throw new ReportedException(crashReport);
        }
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static void onTileEntityError(TileEntity tileEntity, CrashReport crashReport) {
        if (ForgeModContainer.removeErroringTileEntities) {
            // Sponge - fix https://github.com/MinecraftForge/MinecraftForge/issues/3713
            net.minecraftforge.fml.relauncher.FMLRelaunchLog.log.getLogger().log(Level.ERROR, crashReport.getCompleteReport());
            tileEntity.invalidate();
            tileEntity.getWorld().removeTileEntity(tileEntity.getPos());
        } else {
            throw new ReportedException(crashReport);
        }
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void blockExploded(Block block, World world, BlockPos blockpos, Explosion explosion) {
        block.onBlockExploded(world, blockpos, explosion);
    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static boolean isRestoringBlocks(World world) {
        return world.restoringBlockSnapshots || PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS;

    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static void onTileEntityChunkUnload(net.minecraft.tileentity.TileEntity tileEntity) {
        tileEntity.onChunkUnload();
    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static boolean canConnectRedstone(Block block, IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return block.canConnectRedstone(state, world, pos, side);
    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static void setShouldLoadSpawn(net.minecraft.world.DimensionType dimensionType, boolean keepSpawnLoaded) {
        ((IMixinDimensionType)(Object) dimensionType).setShouldLoadSpawn(keepSpawnLoaded);
    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static BlockPos getBedLocation(EntityPlayer player, int dimension) {
        return player.getBedLocation(dimension);
    }

    /**
     * @author unknown
     * @reason forge
     */
    @Overwrite
    public static boolean isSpawnForced(EntityPlayer player, int dimension) {
        return player.isSpawnForced(dimension);
    }
    // Crafting

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static Optional<ItemStack> getContainerItem(ItemStack itemStack) {
        net.minecraft.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
        net.minecraft.item.ItemStack nmsContainerStack = ForgeHooks.getContainerItem(nmsStack);

        if(nmsContainerStack.isEmpty())
            return Optional.empty();
        else
            return Optional.of(ItemStackUtil.fromNative(nmsContainerStack));
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static void onCraftingRecipeRegister(CraftingRecipe recipe) {
        // Emptied out as this is performed during the Registry.Register event.
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static Optional<CraftingRecipe> findMatchingRecipe(CraftingGridInventory inventory, org.spongepowered.api.world.World world) {
        IRecipe recipe = CraftingManager.findMatchingRecipe(InventoryUtil.toNativeInventory(inventory), ((net.minecraft.world.World) world));
        return Optional.ofNullable(((CraftingRecipe) recipe));
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public static Collection<CraftingRecipe> getCraftingRecipes() {
        return Streams.stream(ForgeRegistries.RECIPES.getValues()).map(CraftingRecipe.class::cast).collect(ImmutableList.toImmutableList());
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static Optional<CraftingRecipe> getRecipeById(String id) {
        IRecipe recipe = ForgeRegistries.RECIPES.getValue(new ResourceLocation(id));
        if (recipe == null) {
            return Optional.empty();
        }
        return Optional.of(((CraftingRecipe) recipe));
    }

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static Optional<CraftingRecipe> getRecipeById(CatalogKey id) {
        IRecipe recipe = ForgeRegistries.RECIPES.getValue((ResourceLocation) (Object) id);
        if (recipe == null) {
            return Optional.empty();
        }
        return Optional.of(((CraftingRecipe) recipe));
    }

    /**
     * @author Grinch
     * @reason forge
     */
    @Overwrite
    public static Text getAdditionalCommandDescriptions() {
        return Text.of(SpongeCommandFactory.INDENT, SpongeCommandFactory.title("mods"), SpongeCommandFactory.LONG_INDENT, "List currently installed mods");
    }

    /**
     * @author Grinch
     * @reason forge
     */
    @Overwrite
    public static void registerAdditionalCommands(ChildCommandElementExecutor flagChildren, ChildCommandElementExecutor nonFlagChildren) {
        nonFlagChildren.register(SpongeForgeCommand.createSpongeModsCommand(), "mods");
    }

    /**
     * @author Grinch
     * @reason Filters out mods from the plugin listsp
     */
    @Overwrite
    public static Predicate<PluginContainer> getPluginFilterPredicate() {
        return plugin -> !SpongeCommandFactory.CONTAINER_LIST_STATICS.contains(plugin.getId()) && plugin instanceof SpongeModPluginContainer;
    }

    /**
     * @author Faithcaio
     * @reason Adds support for modded inventories
     */
    @Overwrite
    public static Inventory toInventory(Object inventory, @Nullable Object fallback) {
        if (inventory instanceof IInventory) {
            return ((Inventory) new InvWrapper(((IInventory) inventory)));
        }
        if (fallback instanceof IItemHandler) {
            return new IItemHandlerAdapter(((IItemHandler) fallback));
        }
        String fallbackName = fallback == null ? "no fallback" : fallback.getClass().getName();
        SpongeImpl.getLogger().error("Unknown inventory " + inventory.getClass().getName() + " and " + fallbackName + " report this to Sponge");
        return null;
    }

    /**
     * @author Zidane
     * @reason Switch to {@link TileEntityInvalidatingPhaseState} for mods who change the world but we don't want to capture.
     */
    @Overwrite
    public static void onTileEntityInvalidate(TileEntity te) {
        try (final PhaseContext<?> o = BlockPhase.State.TILE_ENTITY_INVALIDATING.createPhaseContext()
            .source(te)) {
            o.buildAndSwitch();
            te.invalidate();
        }
    }

    /**
     * @author gabizou
     * @reason Supports using the captured drop list for entities from forge.
     * @param phaseContext context
     * @param owner owner
     * @param entityitem item to drop
     */
    @Overwrite
    public static void capturePerEntityItemDrop(PhaseContext<?> phaseContext, Entity owner,
        EntityItem entityitem) {
        ArrayListMultimap<UUID, EntityItem> map = phaseContext.getPerEntityItemEntityDropSupplier().get();
        ArrayList<EntityItem> entityItems = new WrappedArrayList(owner, map.get(owner.getUniqueID()));
        // Re-assigns the list, to ensure that the list is being used.
        ArrayList<EntityItem> capturedDrops = owner.capturedDrops;
        if (capturedDrops != entityItems) {
            owner.capturedDrops = entityItems;
            // If the list was not empty, go ahead and populate sponge's since we had to re-assign the list.
            if (!capturedDrops.isEmpty()) {
                entityItems.addAll(capturedDrops);
            }
        }
        entityItems.add(entityitem);
    }

    /**
     * @author gabizou - April 21st, 2018
     * @reason Use ForgeHooks for looting level compatibility.
     *
     * @param entity The entity passed in
     * @return The modifier based on forge hooks.
     */
    @Overwrite
    public static int getLootingEnchantmentModifier(IMixinEntityLivingBase mixinEntityLivingBase, EntityLivingBase entity, DamageSource cause) {
        return ForgeHooks.getLootingLevel(EntityUtil.toNative(mixinEntityLivingBase), entity, cause);
    }

    /**
     * @author gabizou - June 19th, 2018
     * @reason Fallback to verify the VillagerProfession is available from forge first, then try
     * to get the sponge Profession from that.
     *
     * @param professionId
     * @return
     */
    @Overwrite
    public static Profession validateProfession(int professionId) {
        final VillagerRegistry.VillagerProfession
            profession =
            ((IMixinVillagerRegistry) VillagerRegistry.instance()).getREGISTRY().getObjectById(professionId);
        if (profession == null) {
            throw new RuntimeException("Attempted to set villager profession to unregistered profession: " + professionId + " " + profession);
        }
        final IMixinVillagerProfession mixinProfession = (IMixinVillagerProfession) profession;
        return mixinProfession.getSpongeProfession().orElseGet(() -> {
            final SpongeProfession newProfession = new SpongeProfession(professionId, mixinProfession.getId(), mixinProfession.getProfessionName());
            mixinProfession.setSpongeProfession(newProfession);
            ProfessionRegistryModule.getInstance().registerAdditionalCatalog(newProfession);
            return newProfession;
        });

    }

    /**
     * @author Aaron1011 - July 3rd, 2018
     * @reason Call the Forge hook
     */
    @Overwrite
    public static void onTETickStart(TileEntity tileentity) {
        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
    }

    /**
     * @author Aaron1011 - July 3rd, 2018
     * @reason Call the Forge hook
     */
    @Overwrite
    public static void onTETickEnd(TileEntity tileentity) {
        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
    }

    /**
     * @author Aaron1011 - July 3rd, 2018
     * @reason Call the Forge hook
     */
    @Overwrite
    public static void onEntityTickStart(Entity entity) {
        net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(entity);
    }

    /**
     * @author Aaron1011 - July 3rd, 2018
     * @reason Call the Forge hook
     */
    @Overwrite
    public static void onEntityTickEnd(Entity entity) {
        net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(entity);
    }


}
