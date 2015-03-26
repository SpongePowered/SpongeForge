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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFishFood;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBanner.EnumBannerPattern;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.GameData;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.attribute.Attribute;
import org.spongepowered.api.attribute.AttributeBuilder;
import org.spongepowered.api.attribute.AttributeCalculator;
import org.spongepowered.api.attribute.AttributeModifierBuilder;
import org.spongepowered.api.attribute.Operation;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Banner;
import org.spongepowered.api.block.data.BrewingStand;
import org.spongepowered.api.block.data.Chest;
import org.spongepowered.api.block.data.CommandBlock;
import org.spongepowered.api.block.data.Comparator;
import org.spongepowered.api.block.data.DaylightDetector;
import org.spongepowered.api.block.data.Dispenser;
import org.spongepowered.api.block.data.Dropper;
import org.spongepowered.api.block.data.EnchantmentTable;
import org.spongepowered.api.block.data.EndPortal;
import org.spongepowered.api.block.data.EnderChest;
import org.spongepowered.api.block.data.Furnace;
import org.spongepowered.api.block.data.Hopper;
import org.spongepowered.api.block.data.MobSpawner;
import org.spongepowered.api.block.data.Note;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.block.data.Skull;
import org.spongepowered.api.block.meta.BannerPatternShape;
import org.spongepowered.api.block.meta.BannerPatternShapes;
import org.spongepowered.api.block.meta.NotePitch;
import org.spongepowered.api.block.meta.NotePitches;
import org.spongepowered.api.block.meta.SkullType;
import org.spongepowered.api.block.meta.SkullTypes;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.art.Art;
import org.spongepowered.api.entity.hanging.art.Arts;
import org.spongepowered.api.item.CoalType;
import org.spongepowered.api.item.CoalTypes;
import org.spongepowered.api.item.CookedFish;
import org.spongepowered.api.item.CookedFishes;
import org.spongepowered.api.item.DyeColor;
import org.spongepowered.api.item.DyeColors;
import org.spongepowered.api.entity.living.animal.HorseColor;
import org.spongepowered.api.entity.living.animal.HorseColors;
import org.spongepowered.api.entity.living.animal.HorseStyle;
import org.spongepowered.api.entity.living.animal.HorseStyles;
import org.spongepowered.api.entity.living.animal.HorseVariant;
import org.spongepowered.api.entity.living.animal.HorseVariants;
import org.spongepowered.api.entity.living.animal.OcelotType;
import org.spongepowered.api.entity.living.animal.OcelotTypes;
import org.spongepowered.api.entity.living.animal.RabbitType;
import org.spongepowered.api.entity.living.animal.RabbitTypes;
import org.spongepowered.api.entity.living.monster.SkeletonType;
import org.spongepowered.api.entity.living.monster.SkeletonTypes;
import org.spongepowered.api.entity.living.villager.Career;
import org.spongepowered.api.entity.living.villager.Careers;
import org.spongepowered.api.entity.living.villager.Profession;
import org.spongepowered.api.entity.living.villager.Professions;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkEffectBuilder;
import org.spongepowered.api.item.Fish;
import org.spongepowered.api.item.Fishes;
import org.spongepowered.api.item.GoldenApple;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.potion.PotionEffectTypes;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.text.translation.locale.Locales;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.mod.SpongeGame;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.block.meta.SpongeNotePitch;
import org.spongepowered.mod.block.meta.SpongeSkullType;
import org.spongepowered.mod.configuration.SpongeConfig;
import org.spongepowered.mod.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.mod.effect.particle.SpongeParticleType;
import org.spongepowered.mod.effect.sound.SpongeSound;
import org.spongepowered.mod.entity.SpongeCareer;
import org.spongepowered.mod.entity.SpongeEntityConstants;
import org.spongepowered.mod.entity.SpongeEntityMeta;
import org.spongepowered.mod.entity.SpongeEntityType;
import org.spongepowered.mod.entity.SpongeProfession;
import org.spongepowered.mod.entity.player.gamemode.SpongeGameMode;
import org.spongepowered.mod.item.SpongeCoalType;
import org.spongepowered.mod.item.SpongeFireworkBuilder;
import org.spongepowered.mod.item.SpongeItemStackBuilder;
import org.spongepowered.mod.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.mod.potion.SpongePotionBuilder;
import org.spongepowered.mod.rotation.SpongeRotation;
import org.spongepowered.mod.service.persistence.builders.block.data.SpongePatternLayerBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeBannerBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeBrewingStandBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeChestBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeCommandBlockBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeComparatorBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeDaylightBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeDispenserBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeDropperBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeEnchantmentTableBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeEndPortalBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeEnderChestBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeFurnaceBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeHopperBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeMobSpawnerBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeNoteBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeSignBuilder;
import org.spongepowered.mod.service.persistence.builders.block.tile.SpongeSkullBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeDyeBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeFireworkDataBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeHorseColorBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeHorseStyleBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeHorseVariantBuilder;
import org.spongepowered.mod.service.persistence.builders.data.SpongeOcelotTypeBuilder;
import org.spongepowered.mod.service.persistence.builders.potion.SpongePotionEffectBuilder;
import org.spongepowered.mod.status.SpongeFavicon;
import org.spongepowered.mod.text.SpongeTextFactory;
import org.spongepowered.mod.text.chat.SpongeChatType;
import org.spongepowered.mod.text.format.SpongeTextColor;
import org.spongepowered.mod.text.format.SpongeTextStyle;
import org.spongepowered.mod.text.translation.SpongeTranslation;
import org.spongepowered.mod.weather.SpongeWeather;
import org.spongepowered.mod.world.SpongeDimensionType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
@NonnullByDefault
public class SpongeGameRegistry implements GameRegistry {

    private final Map<String, BiomeType> biomeTypeMappings = Maps.newHashMap();

    public static final Map<Class<? extends WorldProvider>, SpongeConfig<SpongeConfig.DimensionConfig>> dimensionConfigs = Maps.newHashMap();

    public static final Map<String, TextColor> textColorMappings = Maps.newHashMap();
    public static final Map<EnumChatFormatting, SpongeTextColor> enumChatColor = Maps.newEnumMap(EnumChatFormatting.class);

    public static final ImmutableMap<String, TextStyle> textStyleMappings = new ImmutableMap.Builder<String, TextStyle>()
            .put("BOLD", SpongeTextStyle.of(EnumChatFormatting.BOLD))
            .put("ITALIC", SpongeTextStyle.of(EnumChatFormatting.ITALIC))
            .put("UNDERLINE", SpongeTextStyle.of(EnumChatFormatting.UNDERLINE))
            .put("STRIKETHROUGH", SpongeTextStyle.of(EnumChatFormatting.STRIKETHROUGH))
            .put("OBFUSCATED", SpongeTextStyle.of(EnumChatFormatting.OBFUSCATED))
            .put("RESET", SpongeTextStyle.of(EnumChatFormatting.RESET))
            .build();
    private static final ImmutableMap<String, ChatType> chatTypeMappings = new ImmutableMap.Builder<String, ChatType>()
            .put("CHAT", new SpongeChatType((byte) 0))
            .put("SYSTEM", new SpongeChatType((byte) 1))
            .put("ACTION_BAR", new SpongeChatType((byte) 2))
            .build();
    private static final ImmutableMap<String, Locale> localeCodeMappings = ImmutableMap.<String, Locale>builder()
            .put("af_ZA", new Locale("af", "ZA"))
            .put("ar_SA", new Locale("ar", "SA"))
            .put("ast_ES", new Locale("ast", "ES"))
            .put("az_AZ", new Locale("az", "AZ"))
            .put("bg_BG", new Locale("bg", "BG"))
            .put("ca_ES", new Locale("ca", "ES"))
            .put("cs_CZ", new Locale("cs", "CZ"))
            .put("cy_GB", new Locale("cy", "GB"))
            .put("da_DK", new Locale("da", "DK"))
            .put("de_DE", new Locale("de", "DE"))
            .put("el_GR", new Locale("el", "GR"))
            .put("en_AU", new Locale("en", "AU"))
            .put("en_CA", new Locale("en", "CA"))
            .put("en_GB", new Locale("en", "GB"))
            .put("en_PT", new Locale("en", "PT"))
            .put("en_US", new Locale("en", "US"))
            .put("eo_UY", new Locale("eo", "UY"))
            .put("es_AR", new Locale("es", "AR"))
            .put("es_ES", new Locale("es", "ES"))
            .put("es_MX", new Locale("es", "MX"))
            .put("es_UY", new Locale("es", "UY"))
            .put("es_VE", new Locale("es", "VE"))
            .put("et_EE", new Locale("et", "EE"))
            .put("eu_ES", new Locale("eu", "ES"))
            .put("fa_IR", new Locale("fa", "IR"))
            .put("fi_FI", new Locale("fi", "FI"))
            .put("fil_PH", new Locale("fil", "PH"))
            .put("fr_CA", new Locale("fr", "CA"))
            .put("fr_FR", new Locale("fr", "FR"))
            .put("ga_IE", new Locale("ga", "IE"))
            .put("gl_ES", new Locale("gl", "ES"))
            .put("gv_IM", new Locale("gv", "IM"))
            .put("he_IL", new Locale("he", "IL"))
            .put("hi_IN", new Locale("hi", "IN"))
            .put("hr_HR", new Locale("hr", "HR"))
            .put("hu_HU", new Locale("hu", "HU"))
            .put("hy_AM", new Locale("hy", "AM"))
            .put("id_ID", new Locale("id", "ID"))
            .put("is_IS", new Locale("is", "IS"))
            .put("it_IT", new Locale("it", "IT"))
            .put("ja_JP", new Locale("ja", "JP"))
            .put("ka_GE", new Locale("ka", "GE"))
            .put("ko_KR", new Locale("ko", "KR"))
            .put("kw_GB", new Locale("kw", "GB"))
            .put("la_LA", new Locale("la", "LA"))
            .put("lb_LU", new Locale("lb", "LU"))
            .put("lt_LT", new Locale("lt", "LT"))
            .put("lv_LV", new Locale("lv", "LV"))
            .put("mi_NZ", new Locale("mi", "NZ"))
            .put("ms_MY", new Locale("ms", "MY"))
            .put("mt_MT", new Locale("mt", "MT"))
            .put("nds_DE", new Locale("nds", "DE"))
            .put("nl_NL", new Locale("nl", "NL"))
            .put("nn_NO", new Locale("nn", "NO"))
            .put("no_NO", new Locale("no", "NO"))
            .put("oc_FR", new Locale("oc", "FR"))
            .put("pl_PL", new Locale("pl", "PL"))
            .put("pt_BR", new Locale("pt", "BR"))
            .put("pt_PT", new Locale("pt", "PT"))
            .put("qya_AA", new Locale("qya", "AA"))
            .put("ro_RO", new Locale("ro", "RO"))
            .put("ru_RU", new Locale("ru", "RU"))
            .put("se_NO", new Locale("se", "NO"))
            .put("sk_SK", new Locale("sk", "SK"))
            .put("sl_SI", new Locale("sl", "SI"))
            .put("sr_SP", new Locale("sr", "SP"))
            .put("sv_SE", new Locale("sv", "SE"))
            .put("th_TH", new Locale("th", "TH"))
            .put("tlh_AA", new Locale("tlh", "AA"))
            .put("tr_TR", new Locale("tr", "TR"))
            .put("uk_UA", new Locale("uk", "UA"))
            .put("val_ES", new Locale("val", "ES"))
            .put("vi_VN", new Locale("vi", "VN"))
            .put("zh_CN", new Locale("zh", "CN"))
            .put("zh_TW", new Locale("zh", "TW"))
            .build();
    private static final ImmutableMap<String, Locale> localeMappings = ImmutableMap.<String, Locale>builder()
            .put("AFRIKAANS", localeCodeMappings.get("af_ZA"))
            .put("ARABIC", localeCodeMappings.get("ar_SA"))
            .put("ASTURIAN", localeCodeMappings.get("ast_ES"))
            .put("AZERBAIJANI", localeCodeMappings.get("az_AZ"))
            .put("BULGARIAN", localeCodeMappings.get("bg_BG"))
            .put("CATALAN", localeCodeMappings.get("ca_ES"))
            .put("CZECH", localeCodeMappings.get("cs_CZ"))
            .put("WELSH", localeCodeMappings.get("cy_GB"))
            .put("DANISH", localeCodeMappings.get("da_DK"))
            .put("GERMAN", localeCodeMappings.get("de_DE"))
            .put("GREEK", localeCodeMappings.get("el_GR"))
            .put("AUSTRALIAN_ENGLISH", localeCodeMappings.get("en_AU"))
            .put("CANADIAN_ENGLISH", localeCodeMappings.get("en_CA"))
            .put("BRITISH_ENGLISH", localeCodeMappings.get("en_GB"))
            .put("PIRATE_ENGLISH", localeCodeMappings.get("en_PT"))
            .put("ENGLISH", localeCodeMappings.get("en_US"))
            .put("ESPERANTO", localeCodeMappings.get("eo_UY"))
            .put("ARGENTINIAN_SPANISH", localeCodeMappings.get("es_AR"))
            .put("SPANISH", localeCodeMappings.get("es_ES"))
            .put("MEXICAN_SPANISH", localeCodeMappings.get("es_MX"))
            .put("URUGUAYAN_SPANISH", localeCodeMappings.get("es_UY"))
            .put("VENEZUELAN_SPANISH", localeCodeMappings.get("es_VE"))
            .put("ESTONIAN", localeCodeMappings.get("et_EE"))
            .put("BASQUE", localeCodeMappings.get("eu_ES"))
            .put("PERSIAN", localeCodeMappings.get("fa_IR"))
            .put("FINNISH", localeCodeMappings.get("fi_FI"))
            .put("FILIPINO", localeCodeMappings.get("fil_PH"))
            .put("CANADIAN_FRENCH", localeCodeMappings.get("fr_CA"))
            .put("FRENCH", localeCodeMappings.get("fr_FR"))
            .put("IRISH", localeCodeMappings.get("ga_IE"))
            .put("GALICIAN", localeCodeMappings.get("gl_ES"))
            .put("MANX", localeCodeMappings.get("gv_IM"))
            .put("HEBREW", localeCodeMappings.get("he_IL"))
            .put("HINDI", localeCodeMappings.get("hi_IN"))
            .put("CROATIAN", localeCodeMappings.get("hr_HR"))
            .put("HUNGARIAN", localeCodeMappings.get("hu_HU"))
            .put("ARMENIAN", localeCodeMappings.get("hy_AM"))
            .put("INDONESIAN", localeCodeMappings.get("id_ID"))
            .put("ICELANDIC", localeCodeMappings.get("is_IS"))
            .put("ITALIAN", localeCodeMappings.get("it_IT"))
            .put("JAPANESE", localeCodeMappings.get("ja_JP"))
            .put("GEORGIAN", localeCodeMappings.get("ka_GE"))
            .put("KOREAN", localeCodeMappings.get("ko_KR"))
            .put("CORNISH", localeCodeMappings.get("kw_GB"))
            .put("LATIN", localeCodeMappings.get("la_LA"))
            .put("LUXEMBOURGISH", localeCodeMappings.get("lb_LU"))
            .put("LITHUANIAN", localeCodeMappings.get("lt_LT"))
            .put("LATVIAN", localeCodeMappings.get("lv_LV"))
            .put("MAORI", localeCodeMappings.get("mi_NZ"))
            .put("MALAY", localeCodeMappings.get("ms_MY"))
            .put("MALTESE", localeCodeMappings.get("mt_MT"))
            .put("LOW_GERMAN", localeCodeMappings.get("nds_DE"))
            .put("DUTCH", localeCodeMappings.get("nl_NL"))
            .put("NORWEGIAN_NYNORSK", localeCodeMappings.get("nn_NO"))
            .put("NORWEGIAN", localeCodeMappings.get("no_NO"))
            .put("OCCITAN", localeCodeMappings.get("oc_FR"))
            .put("POLISH", localeCodeMappings.get("pl_PL"))
            .put("BRAZILIAN_PORTUGUESE", localeCodeMappings.get("pt_BR"))
            .put("PORTUGUESE", localeCodeMappings.get("pt_PT"))
            .put("QUENYA", localeCodeMappings.get("qya_AA"))
            .put("ROMANIAN", localeCodeMappings.get("ro_RO"))
            .put("RUSSIAN", localeCodeMappings.get("ru_RU"))
            .put("NORTHERN_SAMI", localeCodeMappings.get("se_NO"))
            .put("SLOVAK", localeCodeMappings.get("sk_SK"))
            .put("SLOVENE", localeCodeMappings.get("sl_SI"))
            .put("SERBIAN", localeCodeMappings.get("sr_SP"))
            .put("SWEDISH", localeCodeMappings.get("sv_SE"))
            .put("THAI", localeCodeMappings.get("th_TH"))
            .put("KLINGON", localeCodeMappings.get("tlh_AA"))
            .put("TURKISH", localeCodeMappings.get("tr_TR"))
            .put("UKRAINIAN", localeCodeMappings.get("uk_UA"))
            .put("VALENCIAN", localeCodeMappings.get("val_ES"))
            .put("VIETNAMESE", localeCodeMappings.get("vi_VN"))
            .put("SIMPLIFIED_CHINESE", localeCodeMappings.get("zh_CN"))
            .put("TRADITIONAL_CHINESE", localeCodeMappings.get("zh_TW"))
            .build();

    private static final ImmutableMap<String, Rotation> rotationMappings = new ImmutableMap.Builder<String, Rotation>()
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
    private static final ImmutableMap<String, GameMode> gameModeMappings = new ImmutableMap.Builder<String, GameMode>()
            .put("SURVIVAL", new SpongeGameMode())
            .put("CREATIVE", new SpongeGameMode())
            .put("ADVENTURE", new SpongeGameMode())
            .put("SPECTATOR", new SpongeGameMode())
            .build();
    private static final ImmutableMap<String, Difficulty> difficultyMappings = new ImmutableMap.Builder<String, Difficulty>()
            .put("PEACEFUL", (Difficulty) (Object) EnumDifficulty.PEACEFUL)
            .put("EASY", (Difficulty) (Object) EnumDifficulty.EASY)
            .put("NORMAL", (Difficulty) (Object) EnumDifficulty.NORMAL)
            .put("HARD", (Difficulty) (Object) EnumDifficulty.HARD)
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
    private final Map<String, BannerPatternShape> bannerPatternShapeMappings = Maps.newHashMap();
    private final Map<String, BannerPatternShape> idToBannerPatternShapeMappings = Maps.newHashMap();
    private final Map<String, DyeColor> dyeColorMappings = Maps.newHashMap();
    private final Map<String, SoundType> soundNames = Maps.newHashMap();
    private final Map<String, CoalType> coaltypeMappings = Maps.newHashMap();
    private final Map<String, Fish> fishMappings = Maps.newHashMap();
    private final Map<String, CookedFish> cookedFishMappings = Maps.newHashMap();
    private final Map<String, GoldenApple> goldenAppleMappings = Maps.newHashMap();

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
        return Optional.fromNullable((EntityType) this.entityIdToTypeMappings.get(id));
    }

    @Override
    public List<EntityType> getEntities() {
        return ImmutableList.copyOf(this.entityTypeMappings.values());
    }

    @Override
    public Optional<BiomeType> getBiome(String id) {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null && biome.biomeName.equalsIgnoreCase(id)) {
                return Optional.of((BiomeType) biome);
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
        return Optional.fromNullable(this.particleByName.get(name));
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
        return Optional.fromNullable(this.soundNames.get(name));
    }

    @Override
    public List<SoundType> getSounds() {
        return ImmutableList.copyOf(this.soundNames.values());
    }

    @Override
    public List<String> getDefaultGameRules() {

        List<String> gameruleList = new ArrayList<String>();
        for (Field f : DefaultGameRules.class.getFields()) {
            try {
                gameruleList.add((String) f.get(null));
            } catch (Exception e) {
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
        return Optional.fromNullable(this.dyeColorMappings.get(id));
    }

    @Override
    public List<DyeColor> getDyes() {
        return ImmutableList.copyOf(this.dyeColorMappings.values());
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
        return ImmutableList.copyOf(gameModeMappings.values());
    }

    @Override
    public PotionEffectBuilder getPotionEffectBuilder() {
        return new SpongePotionBuilder();
    }

    @Override
    public Optional<Enchantment> getEnchantment(String id) {
        return Optional.fromNullable((Enchantment) net.minecraft.enchantment.Enchantment.getEnchantmentByLocation(id));
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
        for (Rotation rotation : rotationMappings.values()) {
            if (rotation.getAngle() == degrees) {
                return Optional.of(rotation);
            }
        }
        return Optional.absent();
    }

    @Override
    public List<Rotation> getRotations() {
        return ImmutableList.copyOf(rotationMappings.values());
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
        return Optional.fromNullable(this.bannerPatternShapeMappings.get(name));
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShapeById(String id) {
        return Optional.fromNullable(this.idToBannerPatternShapeMappings.get(id));
    }

    @Override
    public List<BannerPatternShape> getBannerPatternShapes() {
        return ImmutableList.copyOf(this.bannerPatternShapeMappings.values());
    }

    @Override
    public GameDictionary getGameDictionary() {
        return SpongeGameDictionary.instance;
    }

    @Override
    public RecipeRegistry getRecipeRegistry() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<TextColor> getTextColor(String name) {
        return Optional.fromNullable(textColorMappings.get(name));
    }

    @Override
    public Collection<TextColor> getTextColors() {
        return Collections.unmodifiableCollection(textColorMappings.values());
    }

    @Override
    public Optional<TextStyle> getTextStyle(String name) {
        return Optional.fromNullable(textStyleMappings.get(name));
    }

    @Override
    public Collection<TextStyle> getTextStyles() {
        return Collections.unmodifiableCollection(textStyleMappings.values());
    }

    @Override
    public Optional<ChatType> getChatType(String name) {
        return Optional.fromNullable(chatTypeMappings.get(name));
    }

    @Override
    public Collection<ChatType> getChatTypes() {
        return Collections.unmodifiableCollection(chatTypeMappings.values());
    }

    @Override
    public Optional<SelectorType> getSelectorType(String name) {
        return null;
    }

    @Override
    public Collection<ArgumentType<?>> getArgumentTypes() {
        return null;
    }

    @Override
    public Optional<ArgumentType<?>> getArgumentType(String name) {
        return null;
    }

    @Override
    public Collection<SelectorType> getSelectorTypes() {
        return null;
    }

    @Override
    public Optional<Locale> getLocale(String name) {
        return Optional.fromNullable(localeMappings.get(name));
    }

    @Override
    public Optional<Locale> getLocaleById(String id) {
        return Optional.fromNullable(localeCodeMappings.get(id));
    }

    @Override
    public Collection<Locale> getLocales() {
        return Collections.unmodifiableCollection(localeCodeMappings.values());
    }

    @Override
    public Optional<Translation> getTranslationById(String id) {
        return Optional.<Translation>of(new SpongeTranslation(id));
    }

    @Override
    public Collection<Difficulty> getDifficulties() {
        return difficultyMappings.values();
    }

    @Override
    public Optional<Difficulty> getDifficulty(String name) {
        return Optional.fromNullable(difficultyMappings.get(name));
    }

    @Override
    public Collection<EntityInteractionType> getEntityInteractionTypes() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<EntityInteractionType> getEntityInteractionType(String name) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Attribute> getAttribute(String name) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Collection<Attribute> getAttributes() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Operation> getOperation(String name) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Collection<Operation> getOperations() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public AttributeModifierBuilder getAttributeModifierBuilder() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public AttributeBuilder getAttributeBuilder() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<CoalType> getCoalType(String name) {
        return Optional.fromNullable(this.coaltypeMappings.get(name));
    }

    @Override
    public Collection<CoalType> getCoalTypes() {
        return ImmutableList.copyOf(this.coaltypeMappings.values());
    }

    @Override
    public Optional<Fish> getFishType(String name) {
        return Optional.fromNullable(this.fishMappings.get(name));
    }

    @Override
    public Collection<Fish> getFishTypes() {
        return ImmutableList.copyOf(this.fishMappings.values());
    }

    @Override
    public Optional<CookedFish> getCookedFishType(String name) {
        return Optional.fromNullable(this.cookedFishMappings.get(name));
    }

    @Override
    public Collection<CookedFish> getCookedFishTypes() {
        return ImmutableList.copyOf(this.cookedFishMappings.values());
    }

    @Override
    public Optional<GoldenApple> getGoldenAppleType(String name) {
        return null;
    }

    @Override
    public Collection<GoldenApple> getGoldenAppleTypes() {
        return null;
    }

    @Override
    public FireworkEffectBuilder getFireworkEffectBuilder() {
        return new SpongeFireworkBuilder();
    }

    @Override
    public AttributeCalculator getAttributeCalculator() {
        return null;
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
        this.addParticleType("ITEM_CRACK",
                new SpongeParticleType.Material(EnumParticleTypes.ITEM_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("BLOCK_CRACK",
                new SpongeParticleType.Material(EnumParticleTypes.BLOCK_CRACK, new net.minecraft.item.ItemStack(Blocks.air), true));
        this.addParticleType("BLOCK_DUST",
                new SpongeParticleType.Material(EnumParticleTypes.BLOCK_DUST, new net.minecraft.item.ItemStack(Blocks.air), true));
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
        this.enchantmentMappings.put("PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.protection);
        this.enchantmentMappings.put("FIRE_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.fireProtection);
        this.enchantmentMappings.put("FEATHER_FALLING", (Enchantment) net.minecraft.enchantment.Enchantment.featherFalling);
        this.enchantmentMappings.put("BLAST_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.blastProtection);
        this.enchantmentMappings.put("PROJECTILE_PROTECTION", (Enchantment) net.minecraft.enchantment.Enchantment.projectileProtection);
        this.enchantmentMappings.put("RESPIRATION", (Enchantment) net.minecraft.enchantment.Enchantment.respiration);
        this.enchantmentMappings.put("AQUA_AFFINITY", (Enchantment) net.minecraft.enchantment.Enchantment.aquaAffinity);
        this.enchantmentMappings.put("THORNS", (Enchantment) net.minecraft.enchantment.Enchantment.thorns);
        this.enchantmentMappings.put("DEPTH_STRIDER", (Enchantment) net.minecraft.enchantment.Enchantment.depthStrider);
        this.enchantmentMappings.put("SHARPNESS", (Enchantment) net.minecraft.enchantment.Enchantment.sharpness);
        this.enchantmentMappings.put("SMITE", (Enchantment) net.minecraft.enchantment.Enchantment.smite);
        this.enchantmentMappings.put("BANE_OF_ARTHROPODS", (Enchantment) net.minecraft.enchantment.Enchantment.baneOfArthropods);
        this.enchantmentMappings.put("KNOCKBACK", (Enchantment) net.minecraft.enchantment.Enchantment.knockback);
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
                PotionEffectType potionEffectType = (PotionEffectType) potion;
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

    private SpongeEntityType newEntityTypeFromName(String spongeName, String mcName) {
        return new SpongeEntityType((Integer) EntityList.stringToIDMapping.get(mcName), spongeName,
                (Class<? extends Entity>) EntityList.stringToClassMapping.get(mcName));
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
                this.biomeTypes.add((BiomeType) biome);
            }
        }

        this.biomeTypeMappings.put("OCEAN", (BiomeType) BiomeGenBase.ocean);
        this.biomeTypeMappings.put("PLAINS", (BiomeType) BiomeGenBase.plains);
        this.biomeTypeMappings.put("DESERT", (BiomeType) BiomeGenBase.desert);
        this.biomeTypeMappings.put("EXTREME_HILLS", (BiomeType) BiomeGenBase.extremeHills);
        this.biomeTypeMappings.put("FOREST", (BiomeType) BiomeGenBase.forest);
        this.biomeTypeMappings.put("TAIGA", (BiomeType) BiomeGenBase.taiga);
        this.biomeTypeMappings.put("SWAMPLAND", (BiomeType) BiomeGenBase.swampland);
        this.biomeTypeMappings.put("RIVER", (BiomeType) BiomeGenBase.river);
        this.biomeTypeMappings.put("HELL", (BiomeType) BiomeGenBase.hell);
        this.biomeTypeMappings.put("SKY", (BiomeType) BiomeGenBase.sky);
        this.biomeTypeMappings.put("FROZEN_OCEAN", (BiomeType) BiomeGenBase.frozenOcean);
        this.biomeTypeMappings.put("FROZEN_RIVER", (BiomeType) BiomeGenBase.frozenRiver);
        this.biomeTypeMappings.put("ICE_PLAINS", (BiomeType) BiomeGenBase.icePlains);
        this.biomeTypeMappings.put("ICE_MOUNTAINS", (BiomeType) BiomeGenBase.iceMountains);
        this.biomeTypeMappings.put("MUSHROOM_ISLAND", (BiomeType) BiomeGenBase.mushroomIsland);
        this.biomeTypeMappings.put("MUSHROOM_ISLAND_SHORE", (BiomeType) BiomeGenBase.mushroomIslandShore);
        this.biomeTypeMappings.put("BEACH", (BiomeType) BiomeGenBase.beach);
        this.biomeTypeMappings.put("DESERT_HILLS", (BiomeType) BiomeGenBase.desertHills);
        this.biomeTypeMappings.put("FOREST_HILLS", (BiomeType) BiomeGenBase.forestHills);
        this.biomeTypeMappings.put("TAIGA_HILLS", (BiomeType) BiomeGenBase.taigaHills);
        this.biomeTypeMappings.put("EXTREME_HILLS_EDGE", (BiomeType) BiomeGenBase.extremeHillsEdge);
        this.biomeTypeMappings.put("JUNGLE", (BiomeType) BiomeGenBase.jungle);
        this.biomeTypeMappings.put("JUNGLE_HILLS", (BiomeType) BiomeGenBase.jungleHills);
        this.biomeTypeMappings.put("JUNGLE_EDGE", (BiomeType) BiomeGenBase.jungleEdge);
        this.biomeTypeMappings.put("DEEP_OCEAN", (BiomeType) BiomeGenBase.deepOcean);
        this.biomeTypeMappings.put("STONE_BEACH", (BiomeType) BiomeGenBase.stoneBeach);
        this.biomeTypeMappings.put("COLD_BEACH", (BiomeType) BiomeGenBase.coldBeach);
        this.biomeTypeMappings.put("BIRCH_FOREST", (BiomeType) BiomeGenBase.birchForest);
        this.biomeTypeMappings.put("BIRCH_FOREST_HILLS", (BiomeType) BiomeGenBase.birchForestHills);
        this.biomeTypeMappings.put("ROOFED_FOREST", (BiomeType) BiomeGenBase.roofedForest);
        this.biomeTypeMappings.put("COLD_TAIGA", (BiomeType) BiomeGenBase.coldTaiga);
        this.biomeTypeMappings.put("COLD_TAIGA_HILLS", (BiomeType) BiomeGenBase.coldTaigaHills);
        this.biomeTypeMappings.put("MEGA_TAIGA", (BiomeType) BiomeGenBase.megaTaiga);
        this.biomeTypeMappings.put("MEGA_TAIGA_HILLS", (BiomeType) BiomeGenBase.megaTaigaHills);
        this.biomeTypeMappings.put("EXTREME_HILLS_PLUS", (BiomeType) BiomeGenBase.extremeHillsPlus);
        this.biomeTypeMappings.put("SAVANNA", (BiomeType) BiomeGenBase.savanna);
        this.biomeTypeMappings.put("SAVANNA_PLATEAU", (BiomeType) BiomeGenBase.savannaPlateau);
        this.biomeTypeMappings.put("MESA", (BiomeType) BiomeGenBase.mesa);
        this.biomeTypeMappings.put("MESA_PLATEAU_FOREST", (BiomeType) BiomeGenBase.mesaPlateau_F);
        this.biomeTypeMappings.put("MESA_PLATEAU", (BiomeType) BiomeGenBase.mesaPlateau);
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
            this.careerMappings.put(Careers.SHEPHERD.getName(), Careers.SHEPHERD);
            this.careerMappings.put(Careers.FLETCHER.getName(), Careers.FLETCHER);
            this.careerMappings.put(Careers.LIBRARIAN.getName(), Careers.LIBRARIAN);
            this.careerMappings.put(Careers.CLERIC.getName(), Careers.CLERIC);
            this.careerMappings.put(Careers.ARMORER.getName(), Careers.ARMORER);
            this.careerMappings.put(Careers.WEAPON_SMITH.getName(), Careers.WEAPON_SMITH);
            this.careerMappings.put(Careers.TOOL_SMITH.getName(), Careers.TOOL_SMITH);
            this.careerMappings.put(Careers.BUTCHER.getName(), Careers.BUTCHER);
            this.careerMappings.put(Careers.LEATHERWORKER.getName(), Careers.LEATHERWORKER);
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.FARMER).type,
                    Arrays.asList(Careers.FARMER, Careers.FISHERMAN, Careers.SHEPHERD, Careers.FLETCHER));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.LIBRARIAN).type, Arrays.asList(Careers.LIBRARIAN));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.PRIEST).type, Arrays.asList(Careers.CLERIC));
            this.professionToCareerMappings
                    .put(((SpongeEntityMeta) Professions.BLACKSMITH).type, Arrays.asList(Careers.ARMORER, Careers.WEAPON_SMITH, Careers.TOOL_SMITH));
            this.professionToCareerMappings.put(((SpongeEntityMeta) Professions.BUTCHER).type, Arrays.asList(Careers.BUTCHER, Careers.LEATHERWORKER));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTextColor(EnumChatFormatting handle, Color color) {
        SpongeTextColor spongeColor = new SpongeTextColor(handle, color);
        textColorMappings.put(handle.name(), spongeColor);
        enumChatColor.put(handle, spongeColor);
    }

    private void setTextColors() {
        addTextColor(EnumChatFormatting.BLACK, Color.BLACK);
        addTextColor(EnumChatFormatting.DARK_BLUE, new Color(0x0000AA));
        addTextColor(EnumChatFormatting.DARK_GREEN, new Color(0x00AA00));
        addTextColor(EnumChatFormatting.DARK_AQUA, new Color(0x00AAAA));
        addTextColor(EnumChatFormatting.DARK_RED, new Color(0xAA0000));
        addTextColor(EnumChatFormatting.DARK_PURPLE, new Color(0xAA00AA));
        addTextColor(EnumChatFormatting.GOLD, new Color(0xFFAA00));
        addTextColor(EnumChatFormatting.GRAY, new Color(0xAAAAAA));
        addTextColor(EnumChatFormatting.DARK_GRAY, new Color(0x555555));
        addTextColor(EnumChatFormatting.BLUE, new Color(0x5555FF));
        addTextColor(EnumChatFormatting.GREEN, new Color(0x55FF55));
        addTextColor(EnumChatFormatting.AQUA, new Color(0x00FFFF));
        addTextColor(EnumChatFormatting.RED, new Color(0xFF5555));
        addTextColor(EnumChatFormatting.LIGHT_PURPLE, new Color(0xFF55FF));
        addTextColor(EnumChatFormatting.YELLOW, new Color(0xFFFF55));
        addTextColor(EnumChatFormatting.WHITE, Color.WHITE);
        addTextColor(EnumChatFormatting.RESET, Color.WHITE);

        RegistryHelper.mapFields(TextColors.class, textColorMappings);
        RegistryHelper.mapFields(ChatTypes.class, chatTypeMappings);
        RegistryHelper.mapFields(TextStyles.class, textStyleMappings);
    }

    private void setLocales() {
        RegistryHelper.mapFields(Locales.class, localeMappings);
    }

    private void setDyeColors() {
        RegistryHelper.mapFields(DyeColors.class, new Function<String, DyeColor>() {

            @Override
            public DyeColor apply(String input) {
                DyeColor dyeColor = DyeColor.class.cast(EnumDyeColor.valueOf(input));
                SpongeGameRegistry.this.dyeColorMappings.put(dyeColor.getName(), dyeColor);
                return dyeColor;
            }

        });
    }

    private void setFishes() {
        RegistryHelper.mapFields(Fishes.class, new Function<String, Fish>() {
            @Override
            public Fish apply(String input) {
                Fish fish = Fish.class.cast(ItemFishFood.FishType.valueOf(input));
                if (fish != null) {
                    SpongeGameRegistry.this.fishMappings.put(fish.getId(), fish);
                    return fish;
                } else {
                    return null;
                }
            }
        });

        RegistryHelper.mapFields(CookedFishes.class, new Function<String, CookedFish>() {
            @Override
            public CookedFish apply(String input) {
                CookedFish fish = CookedFish.class.cast(ItemFishFood.FishType.valueOf(input));
                if (fish != null) {
                    SpongeGameRegistry.this.cookedFishMappings.put(fish.getId(), fish);
                    return fish;
                } else
                    return null;
            }
        });
    }

    private void setCoal() {
        // Because Minecraft doesn't have any enum stuff for this....
        this.coaltypeMappings.put("COAL", new SpongeCoalType(0, "COAL"));
        this.coaltypeMappings.put("CHARCOAL", new SpongeCoalType(1, "CHARCOAL"));
        RegistryHelper.mapFields(CoalTypes.class, this.coaltypeMappings);
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
        //RegistryHelper.setFactory(TextActions.class, new SpongeTextActionFactory());
    }

    private void setTextFactory() {
        RegistryHelper.setFactory(Texts.class, new SpongeTextFactory());
    }

    private void setSelectors() {
        /*try {
            SelectorTypes.class.getDeclaredField("ALL_PLAYERS").set(null, new SpongeSelectorType("a"));
            SelectorTypes.class.getDeclaredField("ALL_ENTITIES").set(null, new SpongeSelectorType("e"));
            SelectorTypes.class.getDeclaredField("NEAREST_PLAYER").set(null, new SpongeSelectorType("p"));
            SelectorTypes.class.getDeclaredField("RANDOM_PLAYER").set(null, new SpongeSelectorType("r"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RegistryHelper.setFactory(SelectorTypes.class, new SpongeSelectorTypeFactory());
        RegistryHelper.setFactory(Selectors.class, new SpongeSelectorFactory());*/
    }

    private void setTitleFactory() {
        //RegistryHelper.setFactory(Titles.class, new SpongeTitleFactory());
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

    private void setBannerPatternShapes() {
        RegistryHelper.mapFields(BannerPatternShapes.class, new Function<String, BannerPatternShape>() {

            @Override
            public BannerPatternShape apply(String input) {
                BannerPatternShape bannerPattern = BannerPatternShape.class.cast(EnumBannerPattern.valueOf(input));
                SpongeGameRegistry.this.bannerPatternShapeMappings.put(bannerPattern.getName(), bannerPattern);
                SpongeGameRegistry.this.idToBannerPatternShapeMappings.put(bannerPattern.getId(), bannerPattern);
                return bannerPattern;
            }

        });
    }

    private void setGameModes() {
        RegistryHelper.mapFields(GameModes.class, gameModeMappings);
    }

    private void setSounds() {
        final Map<String, String> soundMappings = Maps.newHashMap();
        soundMappings.put("AMBIENCE_CAVE", "ambient.cave.cave");
        soundMappings.put("AMBIENCE_RAIN", "ambient.weather.rain");
        soundMappings.put("AMBIENCE_THUNDER", "ambient.weather.thunder");
        soundMappings.put("ANVIL_BREAK", "random.anvil_break");
        soundMappings.put("ANVIL_LAND", "random.anvil_land");
        soundMappings.put("ANVIL_USE", "random.anvil_use");
        soundMappings.put("ARROW_HIT", "random.bowhit");
        soundMappings.put("BURP", "random.burp");
        soundMappings.put("CHEST_CLOSE", "random.chestclosed");
        soundMappings.put("CHEST_OPEN", "random.chestopen");
        soundMappings.put("CLICK", "random.click");
        soundMappings.put("DOOR_CLOSE", "random.door_close");
        soundMappings.put("DOOR_OPEN", "random.door_open");
        soundMappings.put("DRINK", "random.drink");
        soundMappings.put("EAT", "random.eat");
        soundMappings.put("EXPLODE", "random.explode");
        soundMappings.put("FALL_BIG", "game.player.hurt.fall.big");
        soundMappings.put("FALL_SMALL", "game.player.hurt.fall.small");
        soundMappings.put("FIRE", "fire.fire");
        soundMappings.put("FIRE_IGNITE", "fire.ignite");
        soundMappings.put("FIZZ", "random.fizz");
        soundMappings.put("FUSE", "game.tnt.primed");
        soundMappings.put("GLASS", "dig.glass");
        soundMappings.put("HURT_FLESH", "game.player.hurt");
        soundMappings.put("ITEM_BREAK", "random.break");
        soundMappings.put("ITEM_PICKUP", "random.pop");
        soundMappings.put("LAVA", "liquid.lava");
        soundMappings.put("LAVA_POP", "liquid.lavapop");
        soundMappings.put("LEVEL_UP", "random.levelup");
        soundMappings.put("MINECART_BASE", "minecart.base");
        soundMappings.put("MINECART_INSIDE", "minecart.inside");
        soundMappings.put("NOTE_BASS", "note.bass");
        soundMappings.put("NOTE_PIANO", "note.harp");
        soundMappings.put("NOTE_BASS_DRUM", "note.bd");
        soundMappings.put("NOTE_STICKS", "note.hat");
        soundMappings.put("NOTE_BASS_GUITAR", "note.bassattack");
        soundMappings.put("NOTE_SNARE_DRUM", "note.snare");
        soundMappings.put("NOTE_PLING", "note.pling");
        soundMappings.put("ORB_PICKUP", "random.orb");
        soundMappings.put("PISTON_EXTEND", "tile.piston.out");
        soundMappings.put("PISTON_RETRACT", "tile.piston.in");
        soundMappings.put("PORTAL", "portal.portal");
        soundMappings.put("PORTAL_TRAVEL", "portal.travel");
        soundMappings.put("PORTAL_TRIGGER", "portal.trigger");
        soundMappings.put("SHOOT_ARROW", "random.bow");
        soundMappings.put("SPLASH", "random.splash");
        soundMappings.put("SPLASH2", "game.player.swim.splash");
        soundMappings.put("STEP_GRASS", "step.grass");
        soundMappings.put("STEP_GRAVEL", "step.gravel");
        soundMappings.put("STEP_LADDER", "step.ladder");
        soundMappings.put("STEP_SAND", "step.sand");
        soundMappings.put("STEP_SNOW", "step.snow");
        soundMappings.put("STEP_STONE", "step.stone");
        soundMappings.put("STEP_WOOD", "step.wood");
        soundMappings.put("STEP_WOOL", "step.cloth");
        soundMappings.put("SWIM", "game.player.swim");
        soundMappings.put("WATER", "liquid.water");
        soundMappings.put("WOOD_CLICK", "random.wood_click");
        soundMappings.put("BAT_DEATH", "mob.bat.death");
        soundMappings.put("BAT_HURT", "mob.bat.hurt");
        soundMappings.put("BAT_IDLE", "mob.bat.idle");
        soundMappings.put("BAT_LOOP", "mob.bat.loop");
        soundMappings.put("BAT_TAKEOFF", "mob.bat.takeoff");
        soundMappings.put("BLAZE_BREATH", "mob.blaze.breathe");
        soundMappings.put("BLAZE_DEATH", "mob.blaze.death");
        soundMappings.put("BLAZE_HIT", "mob.blaze.hit");
        soundMappings.put("CAT_HISS", "mob.cat.hiss");
        soundMappings.put("CAT_HIT", "mob.cat.hitt");
        soundMappings.put("CAT_MEOW", "mob.cat.meow");
        soundMappings.put("CAT_PURR", "mob.cat.purr");
        soundMappings.put("CAT_PURREOW", "mob.cat.purreow");
        soundMappings.put("CHICKEN_IDLE", "mob.chicken.say");
        soundMappings.put("CHICKEN_HURT", "mob.chicken.hurt");
        soundMappings.put("CHICKEN_EGG_POP", "mob.chicken.plop");
        soundMappings.put("CHICKEN_WALK", "mob.chicken.step");
        soundMappings.put("COW_IDLE", "mob.cow.say");
        soundMappings.put("COW_HURT", "mob.cow.hurt");
        soundMappings.put("COW_WALK", "mob.cow.step");
        soundMappings.put("CREEPER_HISS", "creeper.primed");
        soundMappings.put("CREEPER_DEATH", "mob.creeper.death");
        soundMappings.put("ENDERDRAGON_DEATH", "mob.enderdragon.end");
        soundMappings.put("ENDERDRAGON_GROWL", "mob.enderdragon.growl");
        soundMappings.put("ENDERDRAGON_HIT", "mob.enderdragon.hit");
        soundMappings.put("ENDERDRAGON_WINGS", "mob.enderdragon.wings");
        soundMappings.put("ENDERMAN_DEATH", "mob.endermen.death");
        soundMappings.put("ENDERMAN_HIT", "mob.endermen.hit");
        soundMappings.put("ENDERMAN_IDLE", "mob.endermen.idle");
        soundMappings.put("ENDERMAN_TELEPORT", "mob.endermen.portal");
        soundMappings.put("ENDERMAN_SCREAM", "mob.endermen.scream");
        soundMappings.put("ENDERMAN_STARE", "mob.endermen.stare");
        soundMappings.put("GHAST_SCREAM", "mob.ghast.scream");
        soundMappings.put("GHAST_SCREAM2", "mob.ghast.affectionate_scream");
        soundMappings.put("GHAST_CHARGE", "mob.ghast.charge");
        soundMappings.put("GHAST_DEATH", "mob.ghast.death");
        soundMappings.put("GHAST_FIREBALL", "mob.ghast.fireball");
        soundMappings.put("GHAST_MOAN", "mob.ghast.moan");
        soundMappings.put("GUARDIAN_IDLE", "mob.guardian.idle");
        soundMappings.put("GUARDIAN_ELDER_IDLE", "mob.guardian.elder.idle");
        soundMappings.put("GUARDIAN_LAND_IDLE", "mob.guardian.land.idle");
        soundMappings.put("GUARDIAN_HIT", "mob.guardian.hit");
        soundMappings.put("GUARDIAN_ELDER_HIT", "mob.guardian.elder.hit");
        soundMappings.put("GUARDIAN_LAND_HIT", "mob.guardian.land.hit");
        soundMappings.put("GUARDIAN_DEATH", "mob.guardian.death");
        soundMappings.put("GUARDIAN_ELDER_DEATH", "mob.guardian.elder.death");
        soundMappings.put("GUARDIAN_LAND_DEATH", "mob.guardian.land.death");
        soundMappings.put("IRONGOLEM_DEATH", "mob.irongolem.death");
        soundMappings.put("IRONGOLEM_HIT", "mob.irongolem.hit");
        soundMappings.put("IRONGOLEM_THROW", "mob.irongolem.throw");
        soundMappings.put("IRONGOLEM_WALK", "mob.irongolem.walk");
        soundMappings.put("MAGMACUBE_WALK", "mob.magmacube.big");
        soundMappings.put("MAGMACUBE_WALK2", "mob.magmacube.small");
        soundMappings.put("MAGMACUBE_JUMP", "mob.magmacube.jump");
        soundMappings.put("PIG_IDLE", "mob.pig.say");
        soundMappings.put("PIG_DEATH", "mob.pig.death");
        soundMappings.put("PIG_WALK", "mob.pig.step");
        soundMappings.put("RABBIT_IDLE", "mob.rabbit.idle");
        soundMappings.put("RABBIT_HURT", "mob.rabbit.hurt");
        soundMappings.put("RABBIT_DEATH", "mob.rabbit.death");
        soundMappings.put("SHEEP_IDLE", "mob.sheep.say");
        soundMappings.put("SHEEP_SHEAR", "mob.sheep.shear");
        soundMappings.put("SHEEP_WALK", "mob.sheep.step");
        soundMappings.put("SILVERFISH_HIT", "mob.silverfish.hit");
        soundMappings.put("SILVERFISH_KILL", "mob.silverfish.kill");
        soundMappings.put("SILVERFISH_IDLE", "mob.silverfish.say");
        soundMappings.put("SILVERFISH_WALK", "mob.silverfish.step");
        soundMappings.put("SKELETON_IDLE", "mob.skeleton.say");
        soundMappings.put("SKELETON_DEATH", "mob.skeleton.death");
        soundMappings.put("SKELETON_HURT", "mob.skeleton.hurt");
        soundMappings.put("SKELETON_WALK", "mob.skeleton.step");
        soundMappings.put("SLIME_ATTACK", "mob.slime.attack");
        soundMappings.put("SLIME_WALK", "mob.slime.small");
        soundMappings.put("SLIME_WALK2", "mob.slime.big");
        soundMappings.put("SPIDER_IDLE", "mob.spider.say");
        soundMappings.put("SPIDER_DEATH", "mob.spider.death");
        soundMappings.put("SPIDER_WALK", "mob.spider.step");
        soundMappings.put("WITHER_DEATH", "mob.wither.death");
        soundMappings.put("WITHER_HURT", "mob.wither.hurt");
        soundMappings.put("WITHER_IDLE", "mob.wither.idle");
        soundMappings.put("WITHER_SHOOT", "mob.wither.shoot");
        soundMappings.put("WITHER_SPAWN", "mob.wither.spawn");
        soundMappings.put("WOLF_BARK", "mob.wolf.bark");
        soundMappings.put("WOLF_DEATH", "mob.wolf.death");
        soundMappings.put("WOLF_GROWL", "mob.wolf.growl");
        soundMappings.put("WOLF_HOWL", "mob.wolf.howl");
        soundMappings.put("WOLF_HURT", "mob.wolf.hurt");
        soundMappings.put("WOLF_PANT", "mob.wolf.panting");
        soundMappings.put("WOLF_SHAKE", "mob.wolf.shake");
        soundMappings.put("WOLF_WALK", "mob.wolf.step");
        soundMappings.put("WOLF_WHINE", "mob.wolf.whine");
        soundMappings.put("ZOMBIE_METAL", "mob.zombie.metal");
        soundMappings.put("ZOMBIE_WOOD", "mob.zombie.wood");
        soundMappings.put("ZOMBIE_WOODBREAK", "mob.zombie.woodbreak");
        soundMappings.put("ZOMBIE_IDLE", "mob.zombie.say");
        soundMappings.put("ZOMBIE_DEATH", "mob.zombie.death");
        soundMappings.put("ZOMBIE_HURT", "mob.zombie.hurt");
        soundMappings.put("ZOMBIE_INFECT", "mob.zombie.infect");
        soundMappings.put("ZOMBIE_UNFECT", "mob.zombie.unfect");
        soundMappings.put("ZOMBIE_REMEDY", "mob.zombie.remedy");
        soundMappings.put("ZOMBIE_WALK", "mob.zombie.step");
        soundMappings.put("ZOMBIE_PIG_IDLE", "mob.zombiepig.zpig");
        soundMappings.put("ZOMBIE_PIG_ANGRY", "mob.zombiepig.zpigangry");
        soundMappings.put("ZOMBIE_PIG_DEATH", "mob.zombiepig.zpigdeath");
        soundMappings.put("ZOMBIE_PIG_HURT", "mob.zombiepig.zpighurt");
        soundMappings.put("DIG_WOOL", "dig.cloth");
        soundMappings.put("DIG_GRASS", "dig.grass");
        soundMappings.put("DIG_GRAVEL", "dig.gravel");
        soundMappings.put("DIG_SAND", "dig.sand");
        soundMappings.put("DIG_SNOW", "dig.snow");
        soundMappings.put("DIG_STONE", "dig.stone");
        soundMappings.put("DIG_WOOD", "dig.wood");
        soundMappings.put("FIREWORK_BLAST", "fireworks.blast");
        soundMappings.put("FIREWORK_BLAST2", "fireworks.blast_far");
        soundMappings.put("FIREWORK_LARGE_BLAST", "fireworks.largeBlast");
        soundMappings.put("FIREWORK_LARGE_BLAST2", "fireworks.largeBlast_far");
        soundMappings.put("FIREWORK_TWINKLE", "fireworks.twinkle");
        soundMappings.put("FIREWORK_TWINKLE2", "fireworks.twinkle_far");
        soundMappings.put("FIREWORK_LAUNCH", "fireworks.launch");
        soundMappings.put("SUCCESSFUL_HIT", "random.successful_hit");
        soundMappings.put("HORSE_ANGRY", "mob.horse.angry");
        soundMappings.put("HORSE_ARMOR", "mob.horse.armor");
        soundMappings.put("HORSE_BREATHE", "mob.horse.breathe");
        soundMappings.put("HORSE_DEATH", "mob.horse.death");
        soundMappings.put("HORSE_GALLOP", "mob.horse.gallop");
        soundMappings.put("HORSE_HIT", "mob.horse.hit");
        soundMappings.put("HORSE_IDLE", "mob.horse.idle");
        soundMappings.put("HORSE_JUMP", "mob.horse.jump");
        soundMappings.put("HORSE_LAND", "mob.horse.land");
        soundMappings.put("HORSE_SADDLE", "mob.horse.leather");
        soundMappings.put("HORSE_SOFT", "mob.horse.soft");
        soundMappings.put("HORSE_WOOD", "mob.horse.wood");
        soundMappings.put("DONKEY_ANGRY", "mob.horse.donkey.angry");
        soundMappings.put("DONKEY_DEATH", "mob.horse.donkey.death");
        soundMappings.put("DONKEY_HIT", "mob.horse.donkey.hit");
        soundMappings.put("DONKEY_IDLE", "mob.horse.donkey.idle");
        soundMappings.put("HORSE_SKELETON_DEATH", "mob.horse.skeleton.death");
        soundMappings.put("HORSE_SKELETON_HIT", "mob.horse.skeleton.hit");
        soundMappings.put("HORSE_SKELETON_IDLE", "mob.horse.skeleton.idle");
        soundMappings.put("HORSE_ZOMBIE_DEATH", "mob.horse.zombie.death");
        soundMappings.put("HORSE_ZOMBIE_HIT", "mob.horse.zombie.hit");
        soundMappings.put("HORSE_ZOMBIE_IDLE", "mob.horse.zombie.idle");
        soundMappings.put("VILLAGER_DEATH", "mob.villager.death");
        soundMappings.put("VILLAGER_HAGGLE", "mob.villager.haggle");
        soundMappings.put("VILLAGER_HIT", "mob.villager.hit");
        soundMappings.put("VILLAGER_IDLE", "mob.villager.idle");
        soundMappings.put("VILLAGER_NO", "mob.villager.no");
        soundMappings.put("VILLAGER_YES", "mob.villager.yes");

        RegistryHelper.mapFields(SoundTypes.class, new Function<String, SoundType>() {

            @Override
            public SoundType apply(String fieldName) {
                String soundName = soundMappings.get(fieldName);
                SoundType sound = new SpongeSound(soundName);
                SpongeGameRegistry.this.soundNames.put(soundName, sound);
                return sound;
            }
        });
    }

    private void setDifficulties() {
        RegistryHelper.mapFields(Difficulties.class, difficultyMappings);
    }

    private void setupSerialization() {
        Game game = SpongeMod.instance.getGame();
        SerializationService service = game.getServiceManager().provide(SerializationService.class).get();
        // TileEntities
        service.registerBuilder(Banner.class, new SpongeBannerBuilder(game));
        service.registerBuilder(Banner.PatternLayer.class, new SpongePatternLayerBuilder(game));
        service.registerBuilder(BrewingStand.class, new SpongeBrewingStandBuilder(game));
        service.registerBuilder(Chest.class, new SpongeChestBuilder(game));
        service.registerBuilder(CommandBlock.class, new SpongeCommandBlockBuilder(game));
        service.registerBuilder(Comparator.class, new SpongeComparatorBuilder(game));
        service.registerBuilder(DaylightDetector.class, new SpongeDaylightBuilder(game));
        service.registerBuilder(Dispenser.class, new SpongeDispenserBuilder(game));
        service.registerBuilder(Dropper.class, new SpongeDropperBuilder(game));
        service.registerBuilder(EnchantmentTable.class, new SpongeEnchantmentTableBuilder(game));
        service.registerBuilder(EnderChest.class, new SpongeEnderChestBuilder(game));
        service.registerBuilder(EndPortal.class, new SpongeEndPortalBuilder(game));
        service.registerBuilder(Furnace.class, new SpongeFurnaceBuilder(game));
        service.registerBuilder(Hopper.class, new SpongeHopperBuilder(game));
        service.registerBuilder(MobSpawner.class, new SpongeMobSpawnerBuilder(game));
        service.registerBuilder(Note.class, new SpongeNoteBuilder(game));
        service.registerBuilder(Sign.class, new SpongeSignBuilder(game));
        service.registerBuilder(Skull.class, new SpongeSkullBuilder(game));

        // Meta
        service.registerBuilder(DyeColor.class, new SpongeDyeBuilder());
        service.registerBuilder(HorseColor.class, new SpongeHorseColorBuilder());
        service.registerBuilder(HorseStyle.class, new SpongeHorseStyleBuilder());
        service.registerBuilder(HorseVariant.class, new SpongeHorseVariantBuilder());
        service.registerBuilder(OcelotType.class, new SpongeOcelotTypeBuilder());
        service.registerBuilder(PotionEffect.class, new SpongePotionEffectBuilder());
        service.registerBuilder(FireworkEffect.class, new SpongeFireworkDataBuilder());

        // User
        // TODO someone needs to write a User implementation...
    }

    public void preInit() {
        setupSerialization();
    }

    public void init() {
        setDimensionTypes();
        setEnchantments();
        setArts();
        setCareersAndProfessions();
        setTextColors();
        setDyeColors();
        setRotations();
        setWeathers();
        setTextActionFactory();
        setTextFactory();
        setLocales();
        setSelectors();
        setTitleFactory();
        setParticles();
        setSkullTypes();
        setNotePitches();
        setBannerPatternShapes();
        setGameModes();
        setSounds();
        setDifficulties();
    }

    public void postInit() {
        setBlockTypes();
        setItemTypes();
        setPotionTypes();
        setEntityTypes();
        setBiomeTypes();
        setFishes();
        setCoal();
    }
}
