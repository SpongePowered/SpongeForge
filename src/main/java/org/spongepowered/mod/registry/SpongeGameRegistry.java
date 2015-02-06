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

package org.spongepowered.mod.registry;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityPainting.EnumArt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameData;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.meta.BannerPatternShape;
import org.spongepowered.api.block.meta.NotePitch;
import org.spongepowered.api.block.meta.NotePitches;
import org.spongepowered.api.block.meta.SkullType;
import org.spongepowered.api.block.meta.SkullTypes;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.art.Art;
import org.spongepowered.api.entity.hanging.art.Arts;
import org.spongepowered.api.entity.living.meta.DyeColor;
import org.spongepowered.api.entity.living.meta.HorseColor;
import org.spongepowered.api.entity.living.meta.HorseColors;
import org.spongepowered.api.entity.living.meta.HorseStyle;
import org.spongepowered.api.entity.living.meta.HorseStyles;
import org.spongepowered.api.entity.living.meta.HorseVariant;
import org.spongepowered.api.entity.living.meta.HorseVariants;
import org.spongepowered.api.entity.living.meta.OcelotType;
import org.spongepowered.api.entity.living.meta.OcelotTypes;
import org.spongepowered.api.entity.living.meta.RabbitType;
import org.spongepowered.api.entity.living.meta.RabbitTypes;
import org.spongepowered.api.entity.living.meta.SkeletonType;
import org.spongepowered.api.entity.living.meta.SkeletonTypes;
import org.spongepowered.api.entity.living.villager.Career;
import org.spongepowered.api.entity.living.villager.Careers;
import org.spongepowered.api.entity.living.villager.Profession;
import org.spongepowered.api.entity.living.villager.Professions;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.potion.PotionEffectTypes;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.text.action.SpongeTextActionFactory;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.message.Messages;
import org.spongepowered.api.text.message.SpongeMessageFactory;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.api.text.selector.Selectors;
import org.spongepowered.api.text.selector.SpongeSelectorFactory;
import org.spongepowered.api.text.selector.SpongeSelectorTypeFactory;
import org.spongepowered.api.text.title.SpongeTitleFactory;
import org.spongepowered.api.text.title.Titles;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.mod.block.meta.SpongeNotePitch;
import org.spongepowered.mod.block.meta.SpongeSkullType;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.mod.effect.particle.SpongeParticleType;
import org.spongepowered.mod.entity.SpongeCareer;
import org.spongepowered.mod.entity.SpongeEntityConstants;
import org.spongepowered.mod.entity.SpongeEntityMeta;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.entity.SpongeProfession;
import org.spongepowered.mod.entity.player.gamemode.SpongeGameMode;
import org.spongepowered.mod.item.SpongeItemStackBuilder;
import org.spongepowered.mod.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.mod.potion.SpongePotionBuilder;
import org.spongepowered.mod.rotation.SpongeRotation;
import org.spongepowered.mod.status.SpongeFavicon;
import org.spongepowered.mod.text.chat.SpongeChatType;
import org.spongepowered.mod.text.format.SpongeTextColor;
import org.spongepowered.mod.text.selector.SpongeSelectorType;
import org.spongepowered.mod.weather.SpongeWeather;
import org.spongepowered.mod.world.SpongeDimensionType;

@SuppressWarnings("unchecked")
@NonnullByDefault
public class SpongeGameRegistry implements GameRegistry {

    private final Map<String, BiomeType> biomeTypeMappings = Maps.newHashMap();
    public static final Map<String, SpongeTextColor> textColorMappings = Maps.newHashMap();
    public static final Map<TextColor, EnumChatFormatting> textColorToEnumMappings = Maps.newHashMap();
    public static final Map<Class<? extends WorldProvider>, SpongeConfig> dimensionConfigs = Maps.newHashMap();
    public static final ImmutableMap<String, TextStyle.Base> textStyleMappings = new ImmutableMap.Builder<String, TextStyle.Base>()
                                                                                .put("OBFUSCATED", new TextStyle.Base("OBFUSCATED", 'k'))
                                                                                .put("BOLD", new TextStyle.Base("BOLD", 'l'))
                                                                                .put("STRIKETHROUGH", new TextStyle.Base("STRIKETHROUGH", 'm'))
                                                                                .put("UNDERLINE", new TextStyle.Base("UNDERLINE", 'n'))
                                                                                .put("ITALIC", new TextStyle.Base("ITALIC", 'o'))
                                                                                .put("RESET", new TextStyle.Base("RESET", 'r'))
                                                                                .build();
    private static final ImmutableMap<String, ChatType> chatTypeMappings = new ImmutableMap.Builder<String, ChatType>()
                                                                           .put("CHAT", new SpongeChatType("CHAT", (byte) 0))
                                                                           .put("SYSTEM", new SpongeChatType("SYSTEM", (byte) 1))
                                                                           .put("ACTION_BAR", new SpongeChatType("ACTION_BAR", (byte) 2))
                                                                           .build();
    private static final ImmutableMap<String, Rotation> rotationMappings =  new ImmutableMap.Builder<String, Rotation>()
                                                                           .put("TOP", new SpongeRotation(0))
                                                                           .put("TOP_RIGHT", new SpongeRotation(45))
                                                                           .put("RIGHT", new SpongeRotation(90))
                                                                           .put("BOTTOM_RIGHT", new SpongeRotation(135))
                                                                           .put("BOTTOM", new SpongeRotation(180))
                                                                           .put("BOTTOM_LEFT", new SpongeRotation(225))
                                                                           .put("LEFT", new SpongeRotation(270))
                                                                           .put("TOP_LEFT", new SpongeRotation(315))
                                                                           .build();
    public static final ImmutableBiMap<Direction, EnumFacing> directionMap = ImmutableBiMap.<Direction, EnumFacing>builder()
                                                                            .put(Direction.NORTH, EnumFacing.NORTH)
                                                                            .put(Direction.EAST, EnumFacing.EAST)
                                                                            .put(Direction.SOUTH, EnumFacing.SOUTH)
                                                                            .put(Direction.WEST, EnumFacing.WEST)
                                                                            .put(Direction.UP, EnumFacing.UP)
                                                                            .put(Direction.DOWN, EnumFacing.DOWN)
                                                                            .build();
    private static final ImmutableMap<String, GameMode> gameModeMappings =  new ImmutableMap.Builder<String, GameMode>()
            .put("SURVIVAL", new SpongeGameMode())
            .put("CREATIVE", new SpongeGameMode())
            .put("ADVENTURE", new SpongeGameMode())
            .put("SPECTATOR", new SpongeGameMode())
            .build();
    private final Map<String, Art> artMappings = Maps.newHashMap();
    private final Map<String, EntityType> entityTypeMappings = Maps.newHashMap();
    public final Map<String, SpongeEntityType> entityIdToTypeMappings = Maps.newHashMap();
    public final Map<Class<? extends Entity>, SpongeEntityType> entityClassToTypeMappings = Maps.newHashMap();
    public final Map<String, Enchantment> enchantmentMappings = Maps.newHashMap();
    private final Map<String, Career> careerMappings = Maps.newHashMap();
    private final Map<String, Profession> professionMappings = Maps.newHashMap();
    private final Map<Integer, List<Career>> professionToCareerMappings = Maps.newHashMap();
    private final Map<String, DimensionType> dimensionTypeMappings = Maps.newHashMap();
    public final Map<Class<? extends Dimension>, DimensionType> dimensionClassMappings = Maps.newHashMap();
    private final Map<String, SpongeParticleType> particleMappings = Maps.newHashMap();
    private final Map<String, ParticleType> particleByName = Maps.newHashMap();
    private final List<BlockType> blockList = new ArrayList<BlockType>();
    private final List<ItemType> itemList = new ArrayList<ItemType>();
    private final List<PotionEffectType> potionList = new ArrayList<PotionEffectType>();
    private final List<BiomeType> biomeTypes = new ArrayList<BiomeType>();
    private final Map<String, SkullType> skullTypeMappings = Maps.newHashMap();
    private final Map<String, NotePitch> notePitchMappings = Maps.newHashMap();

    @Override
    public Optional<BlockType> getBlock(String id) {
        return Optional.fromNullable((BlockType) GameData.getBlockRegistry().getObject(id));
    }

    @Override
    public Optional<ItemType> getItem(String id) {
        return Optional.fromNullable((ItemType) GameData.getItemRegistry().getObject(id));
    }

    public Optional<PotionEffectType> getPotion(String id) {
        return Optional.fromNullable((PotionEffectType) Potion.getPotionFromResourceLocation(id));
    }

    @Override
    public ItemStackBuilder getItemBuilder() {
        return new SpongeItemStackBuilder();
    }

    @Override
    public TradeOfferBuilder getTradeOfferBuilder() {
        return new SpongeTradeOfferBuilder();
    }

    @Override
    public List<PotionEffectType> getPotionEffects() {
        return ImmutableList.copyOf(this.potionList);
    }

    @Override
    public Optional<EntityType> getEntity(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.fromNullable((EntityType)this.entityIdToTypeMappings.get(id));
    }

    @Override
    public List<EntityType> getEntities() {
        return ImmutableList.copyOf(this.entityTypeMappings.values());
    }

    @Override
    public Optional<BiomeType> getBiome(String id) {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null && biome.biomeName.equalsIgnoreCase(id)) {
                return Optional.of((BiomeType)biome);
            }
        }
        return Optional.absent();
    }

    @Override
    public List<BiomeType> getBiomes() {
        return ImmutableList.copyOf(this.biomeTypes);
    }

    @Override
    public List<BlockType> getBlocks() {
        return ImmutableList.copyOf(this.blockList);
    }

    @Override
    public List<ItemType> getItems() {
        return ImmutableList.copyOf(this.itemList);
    }

    @Override
    public Optional<ParticleType> getParticleType(String name) {
        return Optional.fromNullable((ParticleType) this.particleByName.get(name));
    }

    @Override
    public List<ParticleType> getParticleTypes() {
        return ImmutableList.copyOf(this.particleByName.values());
    }

    @Override
    public ParticleEffectBuilder getParticleEffectBuilder(ParticleType particle) {
        Preconditions.checkNotNull(particle);

        if (particle instanceof SpongeParticleType.Colorable) {
            return new SpongeParticleEffectBuilder.BuilderColorable((SpongeParticleType.Colorable) particle);
        } else if (particle instanceof SpongeParticleType.Resizable) {
            return new SpongeParticleEffectBuilder.BuilderResizable((SpongeParticleType.Resizable) particle);
        } else if (particle instanceof SpongeParticleType.Note) {
            return new SpongeParticleEffectBuilder.BuilderNote((SpongeParticleType.Note) particle);
        } else if (particle instanceof SpongeParticleType.Material) {
            return new SpongeParticleEffectBuilder.BuilderMaterial((SpongeParticleType.Material) particle);
        } else {
            return new SpongeParticleEffectBuilder((SpongeParticleType) particle);
        }
    }

    @Override
    public Optional<SoundType> getSound(String name) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<SoundType> getSounds() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<String> getDefaultGameRules() {

        List<String> gameruleList = new ArrayList<String>();
        for(Field f : DefaultGameRules.class.getFields()) {
            try {
                gameruleList.add((String)f.get(null));
            } catch(Exception e) {
                //Ignoring error
            }
        }
        return gameruleList;
    }

    @Override
    public Optional<Art> getArt(String id) {
        return Optional.fromNullable(this.artMappings.get(id));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Art> getArts() {
        return (List) Arrays.asList(EnumArt.values());
    }

    @Override
    public Optional<DyeColor> getDye(String id) {

        //TODO implement.
        return Optional.absent();
    }

    @Override
    public List<DyeColor> getDyes() {

        //TODO implement.
        return null;
    }

    @Override
    public Optional<HorseColor> getHorseColor(String id) {
        return Optional.fromNullable(SpongeEntityConstants.HORSE_COLORS.get(id));
    }

    @Override
    public List<HorseColor> getHorseColors() {
        return ImmutableList.copyOf(SpongeEntityConstants.HORSE_COLORS.values());
    }

    @Override
    public Optional<HorseStyle> getHorseStyle(String id) {
        return Optional.fromNullable(SpongeEntityConstants.HORSE_STYLES.get(id));
    }

    @Override
    public List<HorseStyle> getHorseStyles() {
        return ImmutableList.copyOf(SpongeEntityConstants.HORSE_STYLES.values());
    }

    @Override
    public Optional<HorseVariant> getHorseVariant(String id) {
        return Optional.fromNullable(SpongeEntityConstants.HORSE_VARIANTS.get(id));
    }

    @Override
    public List<HorseVariant> getHorseVariants() {
        return ImmutableList.copyOf(SpongeEntityConstants.HORSE_VARIANTS.values());
    }

    @Override
    public Optional<OcelotType> getOcelotType(String id) {
        return Optional.fromNullable(SpongeEntityConstants.OCELOT_TYPES.get(id));
    }

    @Override
    public List<OcelotType> getOcelotTypes() {
        return ImmutableList.copyOf(SpongeEntityConstants.OCELOT_TYPES.values());
    }

    @Override
    public Optional<RabbitType> getRabbitType(String id) {
        return Optional.fromNullable(SpongeEntityConstants.RABBIT_TYPES.get(id));
    }

    @Override
    public List<RabbitType> getRabbitTypes() {
        return ImmutableList.copyOf(SpongeEntityConstants.RABBIT_TYPES.values());
    }

    @Override
    public Optional<SkeletonType> getSkeletonType(String id) {
        return Optional.fromNullable(SpongeEntityConstants.SKELETON_TYPES.get(id));
    }

    @Override
    public List<SkeletonType> getSkeletonTypes() {
        return ImmutableList.copyOf(SpongeEntityConstants.SKELETON_TYPES.values());
    }

    @Override
    public Optional<Career> getCareer(String id) {
        return Optional.fromNullable(this.careerMappings.get(id));
    }

    @Override
    public List<Career> getCareers() {
        return ImmutableList.copyOf(this.careerMappings.values());
    }

    @Override
    public List<Career> getCareers(Profession profession) {
        return this.professionToCareerMappings.get(((SpongeEntityMeta) profession).type);
    }

    @Override
    public Optional<Profession> getProfession(String id) {
        return Optional.fromNullable(this.professionMappings.get(id));
    }

    @Override
    public List<Profession> getProfessions() {
        return ImmutableList.copyOf(this.professionMappings.values());
    }

    @Override
    public List<GameMode> getGameModes() {
        return ImmutableList.copyOf(this.gameModeMappings.values());
    }

    @Override
    public PotionEffectBuilder getPotionEffectBuilder() {
        return new SpongePotionBuilder();
    }

    @Override
    public Optional<Enchantment> getEnchantment(String id) {
        return Optional.fromNullable((Enchantment) net.minecraft.enchantment.Enchantment.func_180305_b(id));
    }

    @Override
    public List<Enchantment> getEnchantments() {
        return ImmutableList.copyOf(this.enchantmentMappings.values());
    }

    @Override
    public Optional<DimensionType> getDimensionType(String name) {
        return Optional.fromNullable(this.dimensionTypeMappings.get(name));
    }

    @Override
    public List<DimensionType> getDimensionTypes() {
        return ImmutableList.copyOf(this.dimensionTypeMappings.values());
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<Rotation> getRotations() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public GameProfile createGameProfile(UUID uuid, String name) {
        return (GameProfile) new com.mojang.authlib.GameProfile(uuid, name);
    }

    @Override
    public Favicon loadFavicon(String raw) throws IOException {
        return SpongeFavicon.load(raw);
    }

    @Override
    public Favicon loadFavicon(File file) throws IOException {
        return SpongeFavicon.load(file);
    }

    @Override
    public Favicon loadFavicon(URL url) throws IOException {
        return SpongeFavicon.load(url);
    }

    @Override
    public Favicon loadFavicon(InputStream in) throws IOException {
        return SpongeFavicon.load(in);
    }

    @Override
    public Favicon loadFavicon(BufferedImage image) throws IOException {
        return SpongeFavicon.load(image);
    }

    @Override
    public Optional<NotePitch> getNotePitch(String name) {
        return Optional.fromNullable(this.notePitchMappings.get(name));
    }

    @Override
    public List<NotePitch> getNotePitches() {
        return ImmutableList.copyOf(this.notePitchMappings.values());
    }

    @Override
    public Optional<SkullType> getSkullType(String name) {
        return Optional.fromNullable(this.skullTypeMappings.get(name));
    }

    @Override
    public List<SkullType> getSkullTypes() {
        return ImmutableList.copyOf(this.skullTypeMappings.values());
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShape(String name) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShapeById(String id) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<BannerPatternShape> getBannerPatternShapes() {
        throw new UnsupportedOperationException(); // TODO
    }

    public void registerEnvironment(DimensionType env) {
        this.dimensionTypeMappings.put(env.getName(), env);
        this.dimensionClassMappings.put(env.getDimensionClass(), env);
    }

    private void setParticles() {
        this.addParticleType("EXPLOSION_NORMAL", new SpongeParticleType(EnumParticleTypes.EXPLOSION_NORMAL, true));
        this.addParticleType("EXPLOSION_LARGE", new SpongeParticleType.Resizable(EnumParticleTypes.EXPLOSION_LARGE, 1f));
        this.addParticleType("EXPLOSION_HUGE", new SpongeParticleType(EnumParticleTypes.EXPLOSION_HUGE, false));
        this.addParticleType("FIREWORKS_SPARK", new SpongeParticleType(EnumParticleTypes.FIREWORKS_SPARK, true));
        this.addParticleType("WATER_BUBBLE", new SpongeParticleType(EnumParticleTypes.WATER_BUBBLE, true));
        this.addParticleType("WATER_SPLASH", new SpongeParticleType(EnumParticleTypes.WATER_SPLASH, true));
        this.addParticleType("WATER_WAKE", new SpongeParticleType(EnumParticleTypes.WATER_WAKE, true));
        this.addParticleType("SUSPENDED", new SpongeParticleType(EnumParticleTypes.SUSPENDED, false));
        this.addParticleType("SUSPENDED_DEPTH", new SpongeParticleType(EnumParticleTypes.SUSPENDED_DEPTH, false));
        this.addParticleType("CRIT", new SpongeParticleType(EnumParticleTypes.CRIT, true));
        this.addParticleType("CRIT_MAGIC", new SpongeParticleType(EnumParticleTypes.CRIT_MAGIC, true));
        this.addParticleType("SMOKE_NORMAL", new SpongeParticleType(EnumParticleTypes.SMOKE_NORMAL, true));
        this.addParticleType("SMOKE_LARGE", new SpongeParticleType(EnumParticleTypes.SMOKE_LARGE, true));
        this.addParticleType("SPELL", new SpongeParticleType(EnumParticleTypes.SPELL, false));
        this.addParticleType("SPELL_INSTANT", new SpongeParticleType(EnumParticleTypes.SPELL_INSTANT, false));
        this.addParticleType("SPELL_MOB", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB, Color.BLACK));
        this.addParticleType("SPELL_MOB_AMBIENT", new SpongeParticleType.Colorable(EnumParticleTypes.SPELL_MOB_AMBIENT, Color.BLACK));
        this.addParticleType("SPELL_WITCH", new SpongeParticleType(EnumParticleTypes.SPELL_WITCH, false));
        this.addParticleType("DRIP_WATER", new SpongeParticleType(EnumParticleTypes.DRIP_WATER, false));
        this.addParticleType("DRIP_LAVA", new SpongeParticleType(EnumParticleTypes.DRIP_LAVA, false));
        this.addParticleType("VILLAGER_ANGRY", new SpongeParticleType(EnumParticleTypes.VILLAGER_ANGRY, false));
        this.addParticleType("VILLAGER_HAPPY", new SpongeParticleType(EnumParticleTypes.VILLAGER_HAPPY, true));
        this.addParticleType("TOWN_AURA", new SpongeParticleType(EnumParticleTypes.TOWN_AURA, true));
        this.addParticleType("NOTE", new SpongeParticleType.Note(EnumParticleTypes.NOTE, 0f));
        this.addParticleType("PORTAL", new SpongeParticleType(EnumParticleTypes.PORTAL, true));
        this.addParticleType("ENCHANTMENT_TABLE", new SpongeParticleType(EnumParticleTypes.ENCHANTMENT_TABLE, true));
        this.addParticleType("FLAME", new SpongeParticleType(EnumParticleTypes.FLAME, true));
        this.addParticleType("LAVA", new SpongeParticleType(EnumParticleTypes.LAVA, false));
        this.addParticleType("FOOTSTEP", new SpongeParticleType(EnumParticleTypes.FOOTSTEP, false));
        this.addParticleType("CLOUD", new SpongeParticleType(EnumParticleTypes.CLOUD, true));
        this.addParticleType("REDSTONE", new SpongeParticleType.Colorable(EnumParticleTypes.REDSTONE, Color.RED));
        this.addParticleType("SNOWBALL", new SpongeParticleType(EnumParticleTypes.SNOWBALL, false));
        this.addParticleType("SNOW_SHOVEL", new SpongeParticleType(EnumParticleTypes.SNOW_SHOVEL, true));
        this.addParticleType("SLIME", new SpongeParticleType(EnumParticleTypes.SLIME, false));
        this.addParticleType("HEART", new SpongeParticleType(EnumParticleTypes.HEART, false));
        this.addParticleType("BARRIER", new SpongeParticleType(EnumParticleTypes.BARRIER, false));
        this.addParticleType("ITEM_CRACK", new SpongeParticleType.Material(EnumParticleTypes.ITEM_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("BLOCK_CRACK", new SpongeParticleType.Material(EnumParticleTypes.BLOCK_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("BLOCK_DUST", new SpongeParticleType.Material(EnumParticleTypes.BLOCK_DUST, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("WATER_DROP", new SpongeParticleType(EnumParticleTypes.WATER_DROP, false));
        // Is this particle available to be spawned? It's not registered on the client though
        this.addParticleType("ITEM_TAKE", new SpongeParticleType(EnumParticleTypes.ITEM_TAKE, false));
        this.addParticleType("MOB_APPEARANCE", new SpongeParticleType(EnumParticleTypes.MOB_APPEARANCE, false));

        RegistryHelper.mapFields(ParticleTypes.class, this.particleMappings);
    }

    private void addParticleType(String mapping, SpongeParticleType particle) {
        this.particleMappings.put(mapping, particle);
        this.particleByName.put(particle.getName(), particle);
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

    private void setEnchantments() {
        this.enchantmentMappings.put("PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.field_180310_c);
        this.enchantmentMappings.put("FIRE_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.fireProtection);
        this.enchantmentMappings.put("FEATHER_FALLING", (Enchantment) net.minecraft.enchantment.Enchantment.field_180309_e);
        this.enchantmentMappings.put("BLAST_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.blastProtection);
        this.enchantmentMappings.put("PROJECTILE_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.field_180308_g);
        this.enchantmentMappings.put("RESPIRATION", (Enchantment) net.minecraft.enchantment.Enchantment.field_180317_h);
        this.enchantmentMappings.put("AQUA_AFFINITY", (Enchantment) net.minecraft.enchantment.Enchantment.aquaAffinity);
        this.enchantmentMappings.put("THORNS", (Enchantment) net.minecraft.enchantment.Enchantment.thorns);
        this.enchantmentMappings.put("DEPTH_STRIDER", (Enchantment) net.minecraft.enchantment.Enchantment.field_180316_k);
        this.enchantmentMappings.put("SHARPNESS", (Enchantment) net.minecraft.enchantment.Enchantment.field_180314_l);
        this.enchantmentMappings.put("SMITE", (Enchantment) net.minecraft.enchantment.Enchantment.field_180315_m);
        this.enchantmentMappings.put("BANE_OF_ARTHROPODS", (Enchantment) net.minecraft.enchantment.Enchantment.field_180312_n);
        this.enchantmentMappings.put("KNOCKBACK", (Enchantment) net.minecraft.enchantment.Enchantment.field_180313_o);
        this.enchantmentMappings.put("FIRE_ASPECT", (Enchantment) net.minecraft.enchantment.Enchantment.fireAspect);
        this.enchantmentMappings.put("LOOTING", (Enchantment) net.minecraft.enchantment.Enchantment.looting);
        this.enchantmentMappings.put("EFFICIENCY", (Enchantment) net.minecraft.enchantment.Enchantment.efficiency);
        this.enchantmentMappings.put("SILK_TOUCH", (Enchantment) net.minecraft.enchantment.Enchantment.silkTouch);
        this.enchantmentMappings.put("UNBREAKING", (Enchantment) net.minecraft.enchantment.Enchantment.unbreaking);
        this.enchantmentMappings.put("FORTUNE", (Enchantment) net.minecraft.enchantment.Enchantment.fortune);
        this.enchantmentMappings.put("POWER", (Enchantment) net.minecraft.enchantment.Enchantment.power);
        this.enchantmentMappings.put("PUNCH", (Enchantment) net.minecraft.enchantment.Enchantment.punch);
        this.enchantmentMappings.put("FLAME", (Enchantment) net.minecraft.enchantment.Enchantment.flame);
        this.enchantmentMappings.put("INFINITY", (Enchantment) net.minecraft.enchantment.Enchantment.infinity);
        this.enchantmentMappings.put("LUCK_OF_THE_SEA", (Enchantment) net.minecraft.enchantment.Enchantment.luckOfTheSea);
        this.enchantmentMappings.put("LURE", (Enchantment) net.minecraft.enchantment.Enchantment.lure);

        RegistryHelper.mapFields(Enchantments.class, this.enchantmentMappings);
    }
    
    // Note: This is probably fairly slow, but only needs to be run rarely.
    private void setPotionTypes() {
        for (Potion potion : Potion.potionTypes) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType)potion;
                this.potionList.add(potionEffectType);
            }
        }
        RegistryHelper.mapFields(PotionEffectTypes.class, new Function<String, PotionEffectType>() {
            @Override
            public PotionEffectType apply(String fieldName) {
                return getPotion(fieldName.toLowerCase()).get();
            }
        });
    }

    private void setArts() {
        RegistryHelper.mapFields(Arts.class, new Function<String, Art>() {
            @Override
            public Art apply(String fieldName) {
                Art art = (Art) (Object) EnumArt.valueOf(fieldName);
                SpongeGameRegistry.this.artMappings.put(art.getName(), art);
                return art;
            }
        });
    }

    private Map<String, Integer> mcEntityNameToId = null;

    private SpongeEntityType newEntityTypeFromName(String spongeName, String mcName) {
        if (this.mcEntityNameToId == null) {
            try {
                Field field_180126_g = EntityList.class.getDeclaredField("field_180126_g");
                field_180126_g.setAccessible(true);
                this.mcEntityNameToId = (Map<String, Integer>) field_180126_g.get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new SpongeEntityType(this.mcEntityNameToId.get(mcName), spongeName, (Class<? extends Entity>) EntityList.stringToClassMapping.get(mcName));
    }

    private SpongeEntityType newEntityTypeFromName(String name) {
        return newEntityTypeFromName(name, name);
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
                EntityType entityType = SpongeGameRegistry.this.entityTypeMappings.get(fieldName);
                SpongeGameRegistry.this.entityClassToTypeMappings.put(((SpongeEntityType) entityType).entityClass, (SpongeEntityType) entityType);
                SpongeGameRegistry.this.entityIdToTypeMappings.put(((SpongeEntityType) entityType).getId(), ((SpongeEntityType) entityType));
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

    private void setBiomeTypes() {
        BiomeGenBase[] biomeArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeArray) {
            if (biome != null) {
                this.biomeTypes.add((BiomeType)biome);
            }
        }

        this.biomeTypeMappings.put("OCEAN", (BiomeType)BiomeGenBase.ocean);
        this.biomeTypeMappings.put("PLAINS", (BiomeType)BiomeGenBase.plains);
        this.biomeTypeMappings.put("DESERT", (BiomeType)BiomeGenBase.desert);
        this.biomeTypeMappings.put("EXTREME_HILLS", (BiomeType)BiomeGenBase.extremeHills);
        this.biomeTypeMappings.put("FOREST", (BiomeType)BiomeGenBase.forest);
        this.biomeTypeMappings.put("TAIGA", (BiomeType)BiomeGenBase.taiga);
        this.biomeTypeMappings.put("SWAMPLAND", (BiomeType)BiomeGenBase.swampland);
        this.biomeTypeMappings.put("RIVER", (BiomeType)BiomeGenBase.river);
        this.biomeTypeMappings.put("HELL", (BiomeType)BiomeGenBase.hell);
        this.biomeTypeMappings.put("SKY", (BiomeType)BiomeGenBase.sky);
        this.biomeTypeMappings.put("FROZEN_OCEAN", (BiomeType)BiomeGenBase.frozenOcean);
        this.biomeTypeMappings.put("FROZEN_RIVER", (BiomeType)BiomeGenBase.frozenRiver);
        this.biomeTypeMappings.put("ICE_PLAINS", (BiomeType)BiomeGenBase.icePlains);
        this.biomeTypeMappings.put("ICE_MOUNTAINS", (BiomeType)BiomeGenBase.iceMountains);
        this.biomeTypeMappings.put("MUSHROOM_ISLAND", (BiomeType)BiomeGenBase.mushroomIsland);
        this.biomeTypeMappings.put("MUSHROOM_ISLAND_SHORE", (BiomeType)BiomeGenBase.mushroomIslandShore);
        this.biomeTypeMappings.put("BEACH", (BiomeType)BiomeGenBase.beach);
        this.biomeTypeMappings.put("DESERT_HILLS", (BiomeType)BiomeGenBase.desertHills);
        this.biomeTypeMappings.put("FOREST_HILLS", (BiomeType)BiomeGenBase.forestHills);
        this.biomeTypeMappings.put("TAIGA_HILLS", (BiomeType)BiomeGenBase.taigaHills);
        this.biomeTypeMappings.put("EXTREME_HILLS_EDGE", (BiomeType)BiomeGenBase.extremeHillsEdge);
        this.biomeTypeMappings.put("JUNGLE", (BiomeType)BiomeGenBase.jungle);
        this.biomeTypeMappings.put("JUNGLE_HILLS", (BiomeType)BiomeGenBase.jungleHills);
        this.biomeTypeMappings.put("JUNGLE_EDGE", (BiomeType)BiomeGenBase.jungleEdge);
        this.biomeTypeMappings.put("DEEP_OCEAN", (BiomeType)BiomeGenBase.deepOcean);
        this.biomeTypeMappings.put("STONE_BEACH", (BiomeType)BiomeGenBase.stoneBeach);
        this.biomeTypeMappings.put("COLD_BEACH", (BiomeType)BiomeGenBase.coldBeach);
        this.biomeTypeMappings.put("BIRCH_FOREST", (BiomeType)BiomeGenBase.birchForest);
        this.biomeTypeMappings.put("BIRCH_FOREST_HILLS", (BiomeType)BiomeGenBase.birchForestHills);
        this.biomeTypeMappings.put("ROOFED_FOREST", (BiomeType)BiomeGenBase.roofedForest);
        this.biomeTypeMappings.put("COLD_TAIGA", (BiomeType)BiomeGenBase.coldTaiga);
        this.biomeTypeMappings.put("COLD_TAIGA_HILLS", (BiomeType)BiomeGenBase.coldTaigaHills);
        this.biomeTypeMappings.put("MEGA_TAIGA", (BiomeType)BiomeGenBase.megaTaiga);
        this.biomeTypeMappings.put("MEGA_TAIGA_HILLS", (BiomeType)BiomeGenBase.megaTaigaHills);
        this.biomeTypeMappings.put("EXTREME_HILLS_PLUS", (BiomeType)BiomeGenBase.extremeHillsPlus);
        this.biomeTypeMappings.put("SAVANNA", (BiomeType)BiomeGenBase.savanna);
        this.biomeTypeMappings.put("SAVANNA_PLATEAU", (BiomeType)BiomeGenBase.savannaPlateau);
        this.biomeTypeMappings.put("MESA", (BiomeType)BiomeGenBase.mesa);
        this.biomeTypeMappings.put("MESA_PLATEAU_FOREST", (BiomeType)BiomeGenBase.mesaPlateau_F);
        this.biomeTypeMappings.put("MESA_PLATEAU", (BiomeType)BiomeGenBase.mesaPlateau);
        this.biomeTypeMappings.put("SUNFLOWER_PLAINS", (BiomeType) biomeArray[BiomeGenBase.plains.biomeID + 128]);
        this.biomeTypeMappings.put("DESERT_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.desert.biomeID + 128]);
        this.biomeTypeMappings.put("FLOWER_FOREST", (BiomeType) biomeArray[BiomeGenBase.forest.biomeID + 128]);
        this.biomeTypeMappings.put("TAIGA_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.taiga.biomeID + 128]);
        this.biomeTypeMappings.put("SWAMPLAND_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.swampland.biomeID + 128]);
        this.biomeTypeMappings.put("ICE_PLAINS_SPIKES", (BiomeType) biomeArray[BiomeGenBase.icePlains.biomeID + 128]);
        this.biomeTypeMappings.put("JUNGLE_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.jungle.biomeID + 128]);
        this.biomeTypeMappings.put("JUNGLE_EDGE_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.jungleEdge.biomeID + 128]);
        this.biomeTypeMappings.put("COLD_TAIGA_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.coldTaiga.biomeID + 128]);
        this.biomeTypeMappings.put("SAVANNA_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.savanna.biomeID + 128]);
        this.biomeTypeMappings.put("SAVANNA_PLATEAU_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.savannaPlateau.biomeID + 128]);
        this.biomeTypeMappings.put("MESA_BRYCE", (BiomeType) biomeArray[BiomeGenBase.mesa.biomeID + 128]);
        this.biomeTypeMappings.put("MESA_PLATEAU_FOREST_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.mesaPlateau_F.biomeID + 128]);
        this.biomeTypeMappings.put("MESA_PLATEAU_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.mesaPlateau.biomeID + 128]);
        this.biomeTypeMappings.put("BIRCH_FOREST_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.birchForest.biomeID + 128]);
        this.biomeTypeMappings.put("BIRCH_FOREST_HILLS_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.birchForestHills.biomeID + 128]);
        this.biomeTypeMappings.put("ROOFED_FOREST_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.roofedForest.biomeID + 128]);
        this.biomeTypeMappings.put("MEGA_SPRUCE_TAIGA", (BiomeType) biomeArray[BiomeGenBase.megaTaiga.biomeID + 128]);
        this.biomeTypeMappings.put("EXTREME_HILLS_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.extremeHills.biomeID + 128]);
        this.biomeTypeMappings.put("EXTREME_HILLS_PLUS_MOUNTAINS", (BiomeType) biomeArray[BiomeGenBase.extremeHillsPlus.biomeID + 128]);
        this.biomeTypeMappings.put("MEGA_SPRUCE_TAIGA_HILLS", (BiomeType) biomeArray[BiomeGenBase.megaTaigaHills.biomeID + 128]);

        RegistryHelper.mapFields(BiomeTypes.class, this.biomeTypeMappings);
    }

    private void setCareersAndProfessions() {
        try {
            Professions.class.getDeclaredField("FARMER").set(null, new SpongeProfession(0, "farmer"));
            Careers.class.getDeclaredField("FARMER").set(null, new SpongeCareer(0, "farmer", Professions.FARMER));
            Careers.class.getDeclaredField("FISHERMAN").set(null, new SpongeCareer(1, "fisherman", Professions.FARMER));
            Careers.class.getDeclaredField("SHEPHERD").set(null, new SpongeCareer(2, "shepherd", Professions.FARMER));
            Careers.class.getDeclaredField("FLETCHER").set(null, new SpongeCareer(3, "fletcher", Professions.FARMER));
            
            Professions.class.getDeclaredField("LIBRARIAN").set(null, new SpongeProfession(1, "librarian"));
            Careers.class.getDeclaredField("LIBRARIAN").set(null, new SpongeCareer(0, "librarian", Professions.LIBRARIAN));
            
            Professions.class.getDeclaredField("PRIEST").set(null, new SpongeProfession(2, "priest"));
            Careers.class.getDeclaredField("CLERIC").set(null, new SpongeCareer(0, "cleric", Professions.PRIEST));
            
            Professions.class.getDeclaredField("BLACKSMITH").set(null, new SpongeProfession(3, "blacksmith"));
            Careers.class.getDeclaredField("ARMORER").set(null, new SpongeCareer(0, "armor", Professions.BLACKSMITH));
            Careers.class.getDeclaredField("WEAPON_SMITH").set(null, new SpongeCareer(1, "weapon", Professions.BLACKSMITH));
            Careers.class.getDeclaredField("TOOL_SMITH").set(null, new SpongeCareer(2, "tool", Professions.BLACKSMITH));
            
            Professions.class.getDeclaredField("BUTCHER").set(null, new SpongeProfession(4, "butcher"));
            Careers.class.getDeclaredField("BUTCHER").set(null, new SpongeCareer(0, "butcher", Professions.BUTCHER));
            Careers.class.getDeclaredField("LEATHERWORKER").set(null, new SpongeCareer(1, "leatherworker", Professions.BUTCHER));
            
            this.professionMappings.put(Professions.FARMER.getName(), Professions.FARMER);
            this.professionMappings.put(Professions.LIBRARIAN.getName(), Professions.LIBRARIAN);
            this.professionMappings.put(Professions.PRIEST.getName(), Professions.PRIEST);
            this.professionMappings.put(Professions.BLACKSMITH.getName(), Professions.BLACKSMITH);
            this.professionMappings.put(Professions.BUTCHER.getName(), Professions.BUTCHER);
            this.careerMappings.put(Careers.FARMER.getName(), Careers.FARMER);
            this.careerMappings.put(Careers.FISHERMAN.getName(), Careers.FISHERMAN);
            this.careerMappings.put(Careers.SHEPHERD .getName(), Careers.SHEPHERD );
            this.careerMappings.put(Careers.FLETCHER .getName(), Careers.FLETCHER );
            this.careerMappings.put(Careers.LIBRARIAN.getName(), Careers.LIBRARIAN);
            this.careerMappings.put(Careers.CLERIC.getName(), Careers.CLERIC);
            this.careerMappings.put(Careers.ARMORER.getName(), Careers.ARMORER);
            this.careerMappings.put(Careers.WEAPON_SMITH.getName(), Careers.WEAPON_SMITH);
            this.careerMappings.put(Careers.TOOL_SMITH.getName(), Careers.TOOL_SMITH);
            this.careerMappings.put(Careers.BUTCHER.getName(), Careers.BUTCHER);
            this.careerMappings.put(Careers.LEATHERWORKER.getName(), Careers.LEATHERWORKER);
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.FARMER).type, Arrays.asList(Careers.FARMER, Careers.FISHERMAN, Careers.SHEPHERD, Careers.FLETCHER));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.LIBRARIAN).type, Arrays.asList(Careers.LIBRARIAN));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.PRIEST).type, Arrays.asList(Careers.CLERIC));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.BLACKSMITH).type, Arrays.asList(Careers.ARMORER, Careers.WEAPON_SMITH, Careers.TOOL_SMITH));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.BUTCHER).type, Arrays.asList(Careers.BUTCHER, Careers.LEATHERWORKER));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setTextColors() {
        textColorMappings.put("AQUA", new SpongeTextColor(Color.decode("0x00FFFF"), EnumChatFormatting.AQUA));
        textColorMappings.put("BLACK", new SpongeTextColor(Color.BLACK, EnumChatFormatting.BLACK));
        textColorMappings.put("BLUE", new SpongeTextColor(Color.decode("0x5555FF"), EnumChatFormatting.BLUE));
        textColorMappings.put("DARK_AQUA", new SpongeTextColor(Color.decode("0x00AAAA"), EnumChatFormatting.DARK_AQUA));
        textColorMappings.put("DARK_BLUE", new SpongeTextColor(Color.decode("0x0000AA"), EnumChatFormatting.DARK_BLUE));
        textColorMappings.put("DARK_GRAY", new SpongeTextColor(Color.decode("0x555555"), EnumChatFormatting.DARK_GRAY));
        textColorMappings.put("DARK_GREEN", new SpongeTextColor(Color.decode("0x00AA00"), EnumChatFormatting.DARK_GREEN));
        textColorMappings.put("DARK_PURPLE", new SpongeTextColor(Color.decode("0xAA00AA"), EnumChatFormatting.DARK_PURPLE));
        textColorMappings.put("DARK_RED", new SpongeTextColor(Color.decode("0xAA0000"), EnumChatFormatting.DARK_RED));
        textColorMappings.put("GOLD", new SpongeTextColor(Color.decode("0xFFAA00"), EnumChatFormatting.GOLD));
        textColorMappings.put("GRAY", new SpongeTextColor(Color.decode("0xAAAAAA"), EnumChatFormatting.GRAY));
        textColorMappings.put("GREEN", new SpongeTextColor(Color.decode("0x55FF55"), EnumChatFormatting.GREEN));
        textColorMappings.put("LIGHT_PURPLE", new SpongeTextColor(Color.decode("0xFF55FF"), EnumChatFormatting.LIGHT_PURPLE));
        textColorMappings.put("RED", new SpongeTextColor(Color.decode("0xFF5555"), EnumChatFormatting.RED));
        textColorMappings.put("RESET", new SpongeTextColor(Color.WHITE, EnumChatFormatting.RESET));
        textColorMappings.put("YELLOW", new SpongeTextColor(Color.decode("0xFFFF55"), EnumChatFormatting.YELLOW));

        textColorToEnumMappings.put(textColorMappings.get("AQUA"), EnumChatFormatting.AQUA);
        textColorToEnumMappings.put(textColorMappings.get("BLACK"), EnumChatFormatting.BLACK);
        textColorToEnumMappings.put(textColorMappings.get("BLUE"), EnumChatFormatting.BLUE);
        textColorToEnumMappings.put(textColorMappings.get("DARK_AQUA"), EnumChatFormatting.DARK_AQUA);
        textColorToEnumMappings.put(textColorMappings.get("DARK_BLUE"), EnumChatFormatting.DARK_BLUE);
        textColorToEnumMappings.put(textColorMappings.get("DARK_GRAY"), EnumChatFormatting.DARK_GRAY);
        textColorToEnumMappings.put(textColorMappings.get("DARK_GREEN"), EnumChatFormatting.DARK_GREEN);
        textColorToEnumMappings.put(textColorMappings.get("DARK_PURPLE"), EnumChatFormatting.DARK_PURPLE);
        textColorToEnumMappings.put(textColorMappings.get("DARK_RED"), EnumChatFormatting.DARK_RED);
        textColorToEnumMappings.put(textColorMappings.get("GOLD"), EnumChatFormatting.GOLD);
        textColorToEnumMappings.put(textColorMappings.get("GRAY"), EnumChatFormatting.GRAY);
        textColorToEnumMappings.put(textColorMappings.get("GREEN"), EnumChatFormatting.GREEN);
        textColorToEnumMappings.put(textColorMappings.get("LIGHT_PURPLE"), EnumChatFormatting.LIGHT_PURPLE);
        textColorToEnumMappings.put(textColorMappings.get("RED"), EnumChatFormatting.RED);
        textColorToEnumMappings.put(textColorMappings.get("RESET"), EnumChatFormatting.RESET);
        textColorToEnumMappings.put(textColorMappings.get("YELLOW"), EnumChatFormatting.YELLOW);

        RegistryHelper.mapFields(TextColors.class, textColorMappings);
        RegistryHelper.mapFields(TextStyles.class, textStyleMappings, Lists.newArrayList("NONE", "ZERO"));
        RegistryHelper.mapFields(ChatTypes.class, chatTypeMappings);
    }

    private void setRotations() {
        RegistryHelper.mapFields(Rotations.class, rotationMappings);
    }
    
    private void setWeathers() {
        RegistryHelper.mapFields(Weathers.class, new Function<String, Weather>() {
            @Override
            public Weather apply(String fieldName) {
                return new SpongeWeather();
            }
        });
    }

    private void setTextActionFactory() {
        RegistryHelper.setFactory(TextActions.class, new SpongeTextActionFactory());
    }

    private void setMessageFactory() {
        RegistryHelper.setFactory(Messages.class, new SpongeMessageFactory());
    }

    private void setSelectors() {
        try {
            SelectorTypes.class.getDeclaredField("ALL_PLAYERS").set(null, new SpongeSelectorType("a"));
            SelectorTypes.class.getDeclaredField("ALL_ENTITIES").set(null, new SpongeSelectorType("e"));
            SelectorTypes.class.getDeclaredField("NEAREST_PLAYER").set(null, new SpongeSelectorType("p"));
            SelectorTypes.class.getDeclaredField("RANDOM_PLAYER").set(null, new SpongeSelectorType("r"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RegistryHelper.setFactory(SelectorTypes.class, new SpongeSelectorTypeFactory());
        RegistryHelper.setFactory(Selectors.class, new SpongeSelectorFactory());
    }

    private void setTitleFactory() {
        RegistryHelper.setFactory(Titles.class, new SpongeTitleFactory());
    }

    private void setDimensionTypes() {
        try {
            DimensionTypes.class.getDeclaredField("NETHER").set(null, new SpongeDimensionType("NETHER", true, WorldProviderHell.class));
            DimensionTypes.class.getDeclaredField("OVERWORLD").set(null, new SpongeDimensionType("OVERWORLD", true, WorldProviderSurface.class));
            DimensionTypes.class.getDeclaredField("END").set(null, new SpongeDimensionType("END", false, WorldProviderEnd.class));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
  
    private void setNotePitches() {
        RegistryHelper.mapFields(NotePitches.class, new Function<String, NotePitch>() {

            @Override
            public NotePitch apply(String input) {
                NotePitch pitch = new SpongeNotePitch((byte) SpongeGameRegistry.this.notePitchMappings.size(), input);
                SpongeGameRegistry.this.notePitchMappings.put(input, pitch);
                return pitch;
            }
            
        });
    }
    
    private void setSkullTypes() {
        RegistryHelper.mapFields(SkullTypes.class, new Function<String, SkullType>() {

            @Override
            public SkullType apply(String input) {
                SkullType skullType = new SpongeSkullType((byte) SpongeGameRegistry.this.skullTypeMappings.size(), input);
                SpongeGameRegistry.this.skullTypeMappings.put(input, skullType);
                return skullType;
            }
            
        });
    }

    private void setGameModes() {
        RegistryHelper.mapFields(GameModes.class, gameModeMappings);
    }

    public void init() {
        setDimensionTypes();
        setEnchantments();
        setArts();
        setCareersAndProfessions();
        setTextColors();
        setRotations();
        setWeathers();
        setTextActionFactory();
        setMessageFactory();
        setSelectors();
        setTitleFactory();
        setParticles();
        setSkullTypes();
        setNotePitches();
        setGameModes();
    }

    public void postInit() {
        setBlockTypes();
        setItemTypes();
        setPotionTypes();
        setEntityTypes();
        setBiomeTypes();
    }
}
