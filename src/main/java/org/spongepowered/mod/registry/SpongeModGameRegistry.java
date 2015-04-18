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
package org.spongepowered.mod.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFishFood;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.registry.GameData;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.types.Art;
import org.spongepowered.api.data.types.Arts;
import org.spongepowered.api.data.types.BannerPatternShape;
import org.spongepowered.api.data.types.BannerPatternShapes;
import org.spongepowered.api.data.types.CookedFish;
import org.spongepowered.api.data.types.CookedFishes;
import org.spongepowered.api.data.types.DyeColor;
import org.spongepowered.api.data.types.DyeColors;
import org.spongepowered.api.data.types.Fish;
import org.spongepowered.api.data.types.Fishes;
import org.spongepowered.api.data.types.HorseColors;
import org.spongepowered.api.data.types.HorseStyles;
import org.spongepowered.api.data.types.HorseVariants;
import org.spongepowered.api.data.types.NotePitch;
import org.spongepowered.api.data.types.NotePitches;
import org.spongepowered.api.data.types.OcelotTypes;
import org.spongepowered.api.data.types.RabbitTypes;
import org.spongepowered.api.data.types.SkeletonTypes;
import org.spongepowered.api.data.types.SkullType;
import org.spongepowered.api.data.types.SkullTypes;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.meta.SpongeNotePitch;
import org.spongepowered.common.meta.SpongeSkullType;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.entity.SpongeEntityInteractionType;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.mod.world.SpongeWorldTypeEnd;
import org.spongepowered.mod.world.SpongeWorldTypeNether;
import org.spongepowered.mod.world.SpongeWorldTypeOverworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@NonnullByDefault
public class SpongeModGameRegistry extends SpongeGameRegistry {
    private final Map<String, GeneratorType> generatorTypeMappings = Maps.newHashMap();
    private final List<BlockType> blockList = new ArrayList<BlockType>();
    private final List<ItemType> itemList = new ArrayList<ItemType>();
    private final Map<String, NotePitch> notePitchMappings = Maps.newHashMap();
    private final Map<String, SkullType> skullTypeMappings = Maps.newHashMap();
    private final Map<String, BannerPatternShape> bannerPatternShapeMappings = Maps.newHashMap();
    private final Map<String, BannerPatternShape> idToBannerPatternShapeMappings = Maps.newHashMap();
    private final Map<String, Fish> fishMappings = Maps.newHashMap();
    private final Map<String, CookedFish> cookedFishMappings = Maps.newHashMap();
    private final Map<String, DyeColor> dyeColorMappings = Maps.newHashMap();
    private final Map<String, EntityType> entityTypeMappings = Maps.newHashMap();
    private final Map<String, Art> artMappings = Maps.newHashMap();
    private static final ImmutableMap<String, EntityInteractionType> entityInteractionTypeMappings =
            new ImmutableMap.Builder<String, EntityInteractionType>()
                    .put("ATTACK", new SpongeEntityInteractionType("ATTACK"))
                    .put("PICK_BLOCK", new SpongeEntityInteractionType("PICK_BLOCK"))
                    .put("USE", new SpongeEntityInteractionType("USE"))
                    .build();

    {
        catalogTypeMap = ImmutableMap.<Class<? extends CatalogType>, Map<String, ? extends CatalogType>>builder()
                .putAll(catalogTypeMap)
                .put(Art.class, this.artMappings)
                .put(BannerPatternShape.class, this.bannerPatternShapeMappings)
                .put(CookedFish.class, this.cookedFishMappings)
                .put(DyeColor.class, this.dyeColorMappings)
                .put(EntityInteractionType.class, entityInteractionTypeMappings)
                .put(EntityType.class, this.entityTypeMappings)
                .put(Fish.class, this.fishMappings)
                .put(GeneratorType.class, this.generatorTypeMappings)
                .put(NotePitch.class, this.notePitchMappings)
                .put(SkullType.class, this.skullTypeMappings)
                .build();
    }

    public com.google.common.base.Optional<BlockType> getBlock(String id) {
        return com.google.common.base.Optional.fromNullable((BlockType) GameData.getBlockRegistry().getObject(id));
    }

    public com.google.common.base.Optional<ItemType> getItem(String id) {
        return com.google.common.base.Optional.fromNullable((ItemType) GameData.getItemRegistry().getObject(id));
    }

    public void setGeneratorTypes() {
        this.generatorTypeMappings.put("DEFAULT", (GeneratorType) WorldType.DEFAULT);
        this.generatorTypeMappings.put("FLAT", (GeneratorType) WorldType.FLAT);
        this.generatorTypeMappings.put("DEBUG", (GeneratorType) WorldType.DEBUG_WORLD);
        this.generatorTypeMappings.put("NETHER", (GeneratorType) new SpongeWorldTypeNether());
        this.generatorTypeMappings.put("END", (GeneratorType) new SpongeWorldTypeEnd());
        this.generatorTypeMappings.put("OVERWORLD", (GeneratorType) new SpongeWorldTypeOverworld());
        RegistryHelper.mapFields(GeneratorTypes.class, this.generatorTypeMappings);
    }

    private void setBlockTypes() {
        Iterator<ResourceLocation> iter = GameData.getBlockRegistry().getKeys().iterator();
        while (iter.hasNext()) {
            this.blockList.add(getBlock(iter.next().toString()).get());
        }

        RegistryHelper.mapFields(BlockTypes.class, new Function<String, BlockType>() {

            @Override
            public BlockType apply(String fieldName) {
                return getBlock(fieldName.toLowerCase()).get();
            }
        });
    }

    private void setItemTypes() {
        Iterator<ResourceLocation> iter = GameData.getItemRegistry().getKeys().iterator();
        while (iter.hasNext()) {
            this.itemList.add(getItem(iter.next().toString()).get());
        }

        RegistryHelper.mapFields(ItemTypes.class, new Function<String, ItemType>() {

            @Override
            public ItemType apply(String fieldName) {
                return getItem(fieldName.toLowerCase()).get();
            }
        });
    }

    private void setNotePitches() {
        RegistryHelper.mapFields(NotePitches.class, new Function<String, NotePitch>() {

            @Override
            public NotePitch apply(String input) {
                NotePitch pitch = new SpongeNotePitch((byte) SpongeModGameRegistry.this.notePitchMappings.size(), input);
                SpongeModGameRegistry.this.notePitchMappings.put(input, pitch);
                return pitch;
            }

        });
    }

    private void setSkullTypes() {
        RegistryHelper.mapFields(SkullTypes.class, new Function<String, SkullType>() {

            @Override
            public SkullType apply(String input) {
                SkullType skullType = new SpongeSkullType((byte) SpongeModGameRegistry.this.skullTypeMappings.size(), input);
                SpongeModGameRegistry.this.skullTypeMappings.put(input, skullType);
                return skullType;
            }

        });
    }

    private void setBannerPatternShapes() {
        RegistryHelper.mapFields(BannerPatternShapes.class, new Function<String, BannerPatternShape>() {

            @Override
            public BannerPatternShape apply(String input) {
                BannerPatternShape bannerPattern = (BannerPatternShape) (Object) TileEntityBanner.EnumBannerPattern.valueOf(input);
                SpongeModGameRegistry.this.bannerPatternShapeMappings.put(bannerPattern.getName(), bannerPattern);
                SpongeModGameRegistry.this.idToBannerPatternShapeMappings.put(bannerPattern.getId(), bannerPattern);
                return bannerPattern;
            }

        });
    }

    private void setFishes() {
        RegistryHelper.mapFields(Fishes.class, new Function<String, Fish>() {

            @Override
            public Fish apply(String input) {
                Fish fish = (Fish) (Object) ItemFishFood.FishType.valueOf(input);
                if (fish != null) {
                    SpongeModGameRegistry.this.fishMappings.put(fish.getId(), fish);
                    return fish;
                } else {
                    return null;
                }
            }
        });

        RegistryHelper.mapFields(CookedFishes.class, new Function<String, CookedFish>() {

            @Override
            public CookedFish apply(String input) {
                CookedFish fish = (CookedFish) (Object) ItemFishFood.FishType.valueOf(input);
                if (fish != null) {
                    SpongeModGameRegistry.this.cookedFishMappings.put(fish.getId(), fish);
                    return fish;
                } else {
                    return null;
                }
            }
        });
    }

    private void setDyeColors() {
        RegistryHelper.mapFields(DyeColors.class, new Function<String, DyeColor>() {

            @Override
            public DyeColor apply(String input) {
                DyeColor dyeColor = (DyeColor) (Object) EnumDyeColor.valueOf(input);
                SpongeModGameRegistry.this.dyeColorMappings.put(dyeColor.getName(), dyeColor);
                return dyeColor;
            }

        });
    }

    private void setEntityTypes() {
        // internal mapping of our EntityTypes to actual MC names
        this.entityTypeMappings.put("DROPPED_ITEM", newEntityTypeFromName("Item"));
        this.entityTypeMappings.put("EXPERIENCE_ORB", newEntityTypeFromName("XPOrb"));
        this.entityTypeMappings.put("LEASH_HITCH", newEntityTypeFromName("LeashKnot"));
        this.entityTypeMappings.put("PAINTING", newEntityTypeFromName("Painting"));
        this.entityTypeMappings.put("ARROW", newEntityTypeFromName("Arrow"));
        this.entityTypeMappings.put("SNOWBALL", newEntityTypeFromName("Snowball"));
        this.entityTypeMappings.put("FIREBALL", newEntityTypeFromName("LargeFireball", "Fireball"));
        this.entityTypeMappings.put("SMALL_FIREBALL", newEntityTypeFromName("SmallFireball"));
        this.entityTypeMappings.put("ENDER_PEARL", newEntityTypeFromName("ThrownEnderpearl"));
        this.entityTypeMappings.put("EYE_OF_ENDER", newEntityTypeFromName("EyeOfEnderSignal"));
        this.entityTypeMappings.put("SPLASH_POTION", newEntityTypeFromName("ThrownPotion"));
        this.entityTypeMappings.put("THROWN_EXP_BOTTLE", newEntityTypeFromName("ThrownExpBottle"));
        this.entityTypeMappings.put("ITEM_FRAME", newEntityTypeFromName("ItemFrame"));
        this.entityTypeMappings.put("WITHER_SKULL", newEntityTypeFromName("WitherSkull"));
        this.entityTypeMappings.put("PRIMED_TNT", newEntityTypeFromName("PrimedTnt"));
        this.entityTypeMappings.put("FALLING_BLOCK", newEntityTypeFromName("FallingSand"));
        this.entityTypeMappings.put("FIREWORK", newEntityTypeFromName("FireworksRocketEntity"));
        this.entityTypeMappings.put("ARMOR_STAND", newEntityTypeFromName("ArmorStand"));
        this.entityTypeMappings.put("BOAT", newEntityTypeFromName("Boat"));
        this.entityTypeMappings.put("RIDEABLE_MINECART", newEntityTypeFromName("MinecartRideable"));
        this.entityTypeMappings.put("CHESTED_MINECART", newEntityTypeFromName("MinecartChest"));
        this.entityTypeMappings.put("FURNACE_MINECART", newEntityTypeFromName("MinecartFurnace"));
        this.entityTypeMappings.put("TNT_MINECART", newEntityTypeFromName("MinecartTnt", "MinecartTNT"));
        this.entityTypeMappings.put("HOPPER_MINECART", newEntityTypeFromName("MinecartHopper"));
        this.entityTypeMappings.put("MOB_SPAWNER_MINECART", newEntityTypeFromName("MinecartSpawner"));
        this.entityTypeMappings.put("COMMANDBLOCK_MINECART", newEntityTypeFromName("MinecartCommandBlock"));
        this.entityTypeMappings.put("CREEPER", newEntityTypeFromName("Creeper"));
        this.entityTypeMappings.put("SKELETON", newEntityTypeFromName("Skeleton"));
        this.entityTypeMappings.put("SPIDER", newEntityTypeFromName("Spider"));
        this.entityTypeMappings.put("GIANT", newEntityTypeFromName("Giant"));
        this.entityTypeMappings.put("ZOMBIE", newEntityTypeFromName("Zombie"));
        this.entityTypeMappings.put("SLIME", newEntityTypeFromName("Slime"));
        this.entityTypeMappings.put("GHAST", newEntityTypeFromName("Ghast"));
        this.entityTypeMappings.put("PIG_ZOMBIE", newEntityTypeFromName("PigZombie"));
        this.entityTypeMappings.put("ENDERMAN", newEntityTypeFromName("Enderman"));
        this.entityTypeMappings.put("CAVE_SPIDER", newEntityTypeFromName("CaveSpider"));
        this.entityTypeMappings.put("SILVERFISH", newEntityTypeFromName("Silverfish"));
        this.entityTypeMappings.put("BLAZE", newEntityTypeFromName("Blaze"));
        this.entityTypeMappings.put("MAGMA_CUBE", newEntityTypeFromName("LavaSlime"));
        this.entityTypeMappings.put("ENDER_DRAGON", newEntityTypeFromName("EnderDragon"));
        this.entityTypeMappings.put("WITHER", newEntityTypeFromName("WitherBoss"));
        this.entityTypeMappings.put("BAT", newEntityTypeFromName("Bat"));
        this.entityTypeMappings.put("WITCH", newEntityTypeFromName("Witch"));
        this.entityTypeMappings.put("ENDERMITE", newEntityTypeFromName("Endermite"));
        this.entityTypeMappings.put("GUARDIAN", newEntityTypeFromName("Guardian"));
        this.entityTypeMappings.put("PIG", newEntityTypeFromName("Pig"));
        this.entityTypeMappings.put("SHEEP", newEntityTypeFromName("Sheep"));
        this.entityTypeMappings.put("COW", newEntityTypeFromName("Cow"));
        this.entityTypeMappings.put("CHICKEN", newEntityTypeFromName("Chicken"));
        this.entityTypeMappings.put("SQUID", newEntityTypeFromName("Squid"));
        this.entityTypeMappings.put("WOLF", newEntityTypeFromName("Wolf"));
        this.entityTypeMappings.put("MUSHROOM_COW", newEntityTypeFromName("MushroomCow"));
        this.entityTypeMappings.put("SNOWMAN", newEntityTypeFromName("SnowMan"));
        this.entityTypeMappings.put("OCELOT", newEntityTypeFromName("Ozelot"));
        this.entityTypeMappings.put("IRON_GOLEM", newEntityTypeFromName("VillagerGolem"));
        this.entityTypeMappings.put("HORSE", newEntityTypeFromName("EntityHorse"));
        this.entityTypeMappings.put("RABBIT", newEntityTypeFromName("Rabbit"));
        this.entityTypeMappings.put("VILLAGER", newEntityTypeFromName("Villager"));
        this.entityTypeMappings.put("ENDER_CRYSTAL", newEntityTypeFromName("EnderCrystal"));
        this.entityTypeMappings.put("EGG", new SpongeEntityType(-1, "Egg", EntityEgg.class));
        this.entityTypeMappings.put("FISHING_HOOK", new SpongeEntityType(-2, "FishingHook", EntityFishHook.class));
        this.entityTypeMappings.put("LIGHTNING", new SpongeEntityType(-3, "Lightning", EntityLightningBolt.class));
        this.entityTypeMappings.put("WEATHER", new SpongeEntityType(-4, "Weather", EntityWeatherEffect.class));
        this.entityTypeMappings.put("PLAYER", new SpongeEntityType(-5, "Player", EntityPlayerMP.class));
        this.entityTypeMappings.put("COMPLEX_PART", new SpongeEntityType(-6, "ComplexPart", EntityDragonPart.class));

        RegistryHelper.mapFields(EntityTypes.class, new Function<String, EntityType>() {

            @Override
            public EntityType apply(String fieldName) {
                if (fieldName.equals("UNKNOWN")) {
                    // TODO Something for Unknown?
                    return null;
                }
                EntityType entityType = SpongeModGameRegistry.this.entityTypeMappings.get(fieldName);
                SpongeModGameRegistry.this.entityClassToTypeMappings
                        .put(((SpongeEntityType) entityType).entityClass, (SpongeEntityType) entityType);
                SpongeModGameRegistry.this.entityIdToTypeMappings.put(((SpongeEntityType) entityType).getId(), ((SpongeEntityType) entityType));
                return entityType;
            }
        });

        RegistryHelper.mapFields(SkeletonTypes.class, SpongeEntityConstants.SKELETON_TYPES);
        RegistryHelper.mapFields(HorseColors.class, SpongeEntityConstants.HORSE_COLORS);
        RegistryHelper.mapFields(HorseVariants.class, SpongeEntityConstants.HORSE_VARIANTS);
        RegistryHelper.mapFields(HorseStyles.class, SpongeEntityConstants.HORSE_STYLES);
        RegistryHelper.mapFields(OcelotTypes.class, SpongeEntityConstants.OCELOT_TYPES);
        RegistryHelper.mapFields(RabbitTypes.class, SpongeEntityConstants.RABBIT_TYPES);
    }

    private SpongeEntityType newEntityTypeFromName(String spongeName, String mcName) {
        return new SpongeEntityType((Integer) EntityList.stringToIDMapping.get(mcName), spongeName,
                (Class<? extends Entity>) EntityList.stringToClassMapping.get(mcName));
    }

    private void setArts() {
        RegistryHelper.mapFields(Arts.class, new Function<String, Art>() {

            @Override
            public Art apply(String fieldName) {
                Art art = (Art) (Object) EntityPainting.EnumArt.valueOf(fieldName);
                SpongeModGameRegistry.this.artMappings.put(art.getName(), art);
                return art;
            }
        });
    }

    private SpongeEntityType newEntityTypeFromName(String name) {
        return newEntityTypeFromName(name, name);
    }


    @Override
    public GameDictionary getGameDictionary() {
        return SpongeGameDictionary.instance;
    }

    private void setEntityInteractionTypes() {
        RegistryHelper.mapFields(EntityInteractionTypes.class, SpongeModGameRegistry.entityInteractionTypeMappings);
    }

    @Override
    public void init() {
        super.init();
        setArts();
        setDyeColors();
        setSkullTypes();
        setNotePitches();
        setBannerPatternShapes();
        setEntityInteractionTypes();
        setGeneratorTypes();
    }

    @Override
    public void postInit() {
        super.postInit();
        setBlockTypes();
        setItemTypes();
        setEntityTypes();
        setFishes();
    }
}
