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
package org.spongepowered.mod.mixin.plugin.entityactivation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.interfaces.IMixinEntity;
import org.spongepowered.mod.interfaces.IMixinWorld;
import org.spongepowered.mod.interfaces.IMixinWorldProvider;
import org.spongepowered.mod.mixin.plugin.CoreMixinPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivationRange {

    static AxisAlignedBB maxBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB miscBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB creatureBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB monsterBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB aquaticBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB ambientBB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    static final Object[] PATH_ACTIVATION_RANGE_CREATURE = new String[] {SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE,
            SpongeConfig.ENTITY_ACTIVATION_RANGE_CREATURE};
    static final Object[] PATH_ACTIVATION_RANGE_MONSTER = new String[] {SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE,
            SpongeConfig.ENTITY_ACTIVATION_RANGE_MONSTER};
    static final Object[] PATH_ACTIVATION_RANGE_AQUATIC = new String[] {SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE,
            SpongeConfig.ENTITY_ACTIVATION_RANGE_AQUATIC};
    static final Object[] PATH_ACTIVATION_RANGE_AMBIENT = new String[] {SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE,
            SpongeConfig.ENTITY_ACTIVATION_RANGE_AMBIENT};
    static final Object[] PATH_ACTIVATION_RANGE_MISC = new String[] {SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE,
            SpongeConfig.ENTITY_ACTIVATION_RANGE_MISC};

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity
     * @return group id
     */
    public static byte initializeEntityActivationType(Entity entity) {

        // account for entities that dont extend EntityMob, EntityAmbientCreature, EntityCreature
        if (((IMob.class.isAssignableFrom(entity.getClass()) || IRangedAttackMob.class.isAssignableFrom(entity.getClass())) && (entity.getClass() != EntityMob.class))
                || entity.isCreatureType(EnumCreatureType.MONSTER, false)) {
            return 1; // Monster
        } else if (((EntityAnimal.class.isAssignableFrom(entity.getClass()) && !entity.isCreatureType(EnumCreatureType.AMBIENT, false)) || entity
                .isCreatureType(EnumCreatureType.CREATURE, false))) {
            return 2; // Creature
        } else if (EntityWaterMob.class.isAssignableFrom(entity.getClass()) || entity.isCreatureType(EnumCreatureType.WATER_CREATURE, true)) {
            return 3; // Aquatic
        } else if (EntityAmbientCreature.class.isAssignableFrom(entity.getClass()) || entity.isCreatureType(EnumCreatureType.AMBIENT, false)) {
            return 4; // Ambient
        } else {
            return 5; // Misc
        }
    }

    /**
     * These entities are excluded from Activation range checks.
     *
     * @param entity
     * @param world
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity) {
        if (entity.worldObj.isRemote) {
            return true;
        }
        SpongeConfig config = getActiveConfig(entity.worldObj);

        if ((((IMixinEntity) entity).getActivationType() == 5 && config.getRootNode().getNode(PATH_ACTIVATION_RANGE_MISC).getInt() == 0)
                || (((IMixinEntity) entity).getActivationType() == 4 && config.getRootNode().getNode(PATH_ACTIVATION_RANGE_AMBIENT)
                        .getInt() == 0)
                || (((IMixinEntity) entity).getActivationType() == 3 && config.getRootNode().getNode(PATH_ACTIVATION_RANGE_AQUATIC)
                        .getInt() == 0)
                || (((IMixinEntity) entity).getActivationType() == 2 && config.getRootNode().getNode(PATH_ACTIVATION_RANGE_CREATURE)
                        .getInt() == 0)
                || (((IMixinEntity) entity).getActivationType() == 1 && config.getRootNode().getNode(PATH_ACTIVATION_RANGE_MISC)
                        .getInt() == 0)
                || (entity instanceof EntityPlayer && !(entity instanceof FakePlayer))
                || entity instanceof EntityThrowable
                || entity instanceof EntityDragon
                || entity instanceof EntityDragonPart
                || entity instanceof EntityWither
                || entity instanceof EntityFireball
                || entity instanceof EntityWeatherEffect
                || entity instanceof EntityTNTPrimed
                || entity instanceof EntityEnderCrystal
                || entity instanceof EntityFireworkRocket) {
            return true;
        }

        return false;
    }

    /**
     * Utility method to grow an AABB without creating a new AABB or touching
     * the pool, so we can re-use ones we have.
     *
     * @param target
     * @param source
     * @param x
     * @param y
     * @param z
     */
    public static void growBB(AxisAlignedBB target, AxisAlignedBB source, int x, int y, int z)
    {
        target.minX = source.minX - x;
        target.minY = source.minY - y;
        target.minZ = source.minZ - z;
        target.maxX = source.maxX + x;
        target.maxY = source.maxY + y;
        target.maxZ = source.maxZ + z;
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world
     */
    public static void activateEntities(World world) {
        SpongeConfig config = getActiveConfig(world);
        final int miscActivationRange =
                config.getRootNode().getNode(PATH_ACTIVATION_RANGE_MISC).getInt();
        final int creatureActivationRange =
                config.getRootNode().getNode(PATH_ACTIVATION_RANGE_CREATURE).getInt();
        final int monsterActivationRange =
                config.getRootNode().getNode(PATH_ACTIVATION_RANGE_MONSTER).getInt();
        final int aquaticActivationRange =
                config.getRootNode().getNode(PATH_ACTIVATION_RANGE_AQUATIC).getInt();
        final int ambientActivationRange =
                config.getRootNode().getNode(PATH_ACTIVATION_RANGE_AMBIENT).getInt();

        int[] ranges = {miscActivationRange, creatureActivationRange, monsterActivationRange, aquaticActivationRange, ambientActivationRange};
        int maxRange = 0;
        for (int range : ranges) {
            if (range > maxRange) {
                maxRange = range;
            }
        }

        maxRange = Math.max(maxRange, miscActivationRange);
        maxRange = Math.min((6 << 4) - 8, maxRange);

        for (Object entity : world.playerEntities) {

            Entity player = (Entity) entity;
            ((IMixinEntity) player).setActivatedTick(world.getWorldInfo().getWorldTotalTime());
            growBB(maxBB, player.getEntityBoundingBox(), maxRange, 256, maxRange);
            growBB(miscBB, player.getEntityBoundingBox(), miscActivationRange, 256, miscActivationRange);
            growBB(creatureBB, player.getEntityBoundingBox(), creatureActivationRange, 256, creatureActivationRange);
            growBB(monsterBB, player.getEntityBoundingBox(), monsterActivationRange, 256, monsterActivationRange);
            growBB(aquaticBB, player.getEntityBoundingBox(), aquaticActivationRange, 256, aquaticActivationRange);
            growBB(ambientBB, player.getEntityBoundingBox(), ambientActivationRange, 256, ambientActivationRange);

            int i = MathHelper.floor_double(maxBB.minX / 16.0D);
            int j = MathHelper.floor_double(maxBB.maxX / 16.0D);
            int k = MathHelper.floor_double(maxBB.minZ / 16.0D);
            int l = MathHelper.floor_double(maxBB.maxZ / 16.0D);

            for (int i1 = i; i1 <= j; ++i1) {
                for (int j1 = k; j1 <= l; ++j1) {
                    WorldServer worldserver = (WorldServer) world;
                    if (worldserver.theChunkProviderServer.chunkExists(i1, j1)) {
                        activateChunkEntities(world.getChunkFromChunkCoords(i1, j1));
                    }
                }
            }
        }
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param chunk
     */
    @SuppressWarnings("rawtypes")
    private static void activateChunkEntities(Chunk chunk) {
        for (int i = 0; i < chunk.getEntityLists().length; ++i) {
            Iterator iterator = chunk.getEntityLists()[i].iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();
                SpongeConfig config = getActiveConfig(entity.worldObj);
                SpongeEntityType type = (SpongeEntityType) ((org.spongepowered.api.entity.Entity) entity).getType();
                if (entity.worldObj.getWorldInfo().getWorldTotalTime() > ((IMixinEntity) entity).getActivatedTick()) {
                    if (((IMixinEntity) entity).getDefaultActivationState()) {
                        ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                        continue;
                    }
                    if (!config.getRootNode().getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId(), "enabled").getBoolean()
                            || !config.getRootNode()
                                    .getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId(), "entities", type.getEntityName())
                                    .getBoolean()) {
                        continue;
                    }
                    switch (((IMixinEntity) entity).getActivationType()) {
                        case 1:
                            if (monsterBB.intersectsWith(entity.getEntityBoundingBox())) {
                                ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                            }
                            break;
                        case 2:
                            if (creatureBB.intersectsWith(entity.getEntityBoundingBox())) {
                                ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                            }
                            break;
                        case 3:
                            if (aquaticBB.intersectsWith(entity.getEntityBoundingBox())) {
                                ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                            }
                            break;
                        case 4:
                            if (ambientBB.intersectsWith(entity.getEntityBoundingBox())) {
                                ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                            }
                            break;
                        case 5:
                        default:
                            if (miscBB.intersectsWith(entity.getEntityBoundingBox())) {
                                ((IMixinEntity) entity).setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime());
                            }
                    }
                }
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static boolean checkEntityImmunities(Entity entity) {
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity) {
        if (entity.worldObj.isRemote) {
            return true;
        }

        IMixinEntity spongeEntity = (IMixinEntity) entity;
        boolean isActive =
                spongeEntity.getActivatedTick() >= entity.worldObj.getWorldInfo().getWorldTotalTime() || spongeEntity.getDefaultActivationState();

        // Should this entity tick?
        if (!isActive) {
            if ((entity.worldObj.getWorldInfo().getWorldTotalTime() - spongeEntity.getActivatedTick() - 1) % 20 == 0) {
                // Check immunities every 20 ticks.
                if (checkEntityImmunities(entity)) {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    spongeEntity.setActivatedTick(entity.worldObj.getWorldInfo().getWorldTotalTime() + 20);
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (!spongeEntity.getDefaultActivationState() && entity.ticksExisted % 4 == 0 && !checkEntityImmunities(entity)) {
            isActive = false;
        }

        // Make sure not on edge of unloaded chunk
        int x = net.minecraft.util.MathHelper.floor_double(entity.posX);
        int z = net.minecraft.util.MathHelper.floor_double(entity.posZ);
        if (isActive && !entity.worldObj.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
            isActive = false;
        }

        return isActive;
    }

    public static void addEntityToConfig(World world, SpongeEntityType type, byte activationType) {
        List<SpongeConfig> configs = new ArrayList<SpongeConfig>();
        configs.add(CoreMixinPlugin.getGlobalConfig());
        configs.add(((IMixinWorldProvider) world.provider).getDimensionConfig());
        configs.add(((IMixinWorld) world).getWorldConfig());
        String entityType = "misc";
        if (activationType == 1) {
            entityType = "monster";
        } else if (activationType == 2) {
            entityType = "creature";
        } else if (activationType == 3) {
            entityType = "aquatic";
        } else if (activationType == 4) {
            entityType = "ambient";
        }

        for (SpongeConfig config : configs) {
            if (config.getRootNode().getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId()).isVirtual()) {
                config.getRootNode().getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId(), "enabled").setValue(true);
            }

            if (config.getRootNode().getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId(), entityType, type.getEntityName())
                    .isVirtual()) {
                config.getRootNode().getNode(SpongeConfig.MODULE_ENTITY_ACTIVATION_RANGE, type.getModId(), entityType, type.getEntityName())
                        .setValue(true);
                config.save();
            }
        }
    }

    public static SpongeConfig getActiveConfig(World world) {
        SpongeConfig config = ((IMixinWorld) world).getWorldConfig();
        if (config.getRootNode().getNode(SpongeConfig.CONFIG_ENABLED).getBoolean()) {
            return config;
        } else if (((IMixinWorldProvider) world.provider).getDimensionConfig() != null && ((IMixinWorldProvider) world.provider)
                .getDimensionConfig().getRootNode().getNode(SpongeConfig.CONFIG_ENABLED).getBoolean()) {
            return ((IMixinWorldProvider) world.provider).getDimensionConfig();
        } else {
            return CoreMixinPlugin.getGlobalConfig();
        }
    }
}
