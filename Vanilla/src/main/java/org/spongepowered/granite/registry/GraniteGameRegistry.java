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
package org.spongepowered.granite.registry;

import com.google.common.base.Optional;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.attribute.Attribute;
import org.spongepowered.api.attribute.AttributeBuilder;
import org.spongepowered.api.attribute.AttributeCalculator;
import org.spongepowered.api.attribute.AttributeModifierBuilder;
import org.spongepowered.api.attribute.Operation;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tile.TileEntityType;
import org.spongepowered.api.block.tile.data.BannerPatternShape;
import org.spongepowered.api.block.tile.data.NotePitch;
import org.spongepowered.api.block.tile.data.SkullType;
import org.spongepowered.api.effect.particle.ParticleEffectBuilder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.hanging.art.Art;
import org.spongepowered.api.entity.living.animal.HorseColor;
import org.spongepowered.api.entity.living.animal.HorseStyle;
import org.spongepowered.api.entity.living.animal.HorseVariant;
import org.spongepowered.api.entity.living.animal.OcelotType;
import org.spongepowered.api.entity.living.animal.RabbitType;
import org.spongepowered.api.entity.living.monster.SkeletonType;
import org.spongepowered.api.entity.living.villager.Career;
import org.spongepowered.api.entity.living.villager.Profession;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.item.CoalType;
import org.spongepowered.api.item.CookedFish;
import org.spongepowered.api.item.DyeColor;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.FireworkEffectBuilder;
import org.spongepowered.api.item.Fish;
import org.spongepowered.api.item.GoldenApple;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.potion.PotionEffectBuilder;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.ScoreboardBuilder;
import org.spongepowered.api.scoreboard.TeamBuilder;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.ObjectiveBuilder;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.stats.BlockStatistic;
import org.spongepowered.api.stats.EntityStatistic;
import org.spongepowered.api.stats.ItemStatistic;
import org.spongepowered.api.stats.Statistic;
import org.spongepowered.api.stats.StatisticBuilder;
import org.spongepowered.api.stats.StatisticFormat;
import org.spongepowered.api.stats.StatisticGroup;
import org.spongepowered.api.stats.TeamStatistic;
import org.spongepowered.api.stats.achievement.Achievement;
import org.spongepowered.api.stats.achievement.AchievementBuilder;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Singleton;

@Singleton
public class GraniteGameRegistry implements GameRegistry {

    @Override
    public Optional<BlockType> getBlock(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BlockType> getBlocks() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ItemType> getItem(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ItemType> getItems() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<TileEntityType> getTileEntityType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<TileEntityType> getTileEntityTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BiomeType> getBiome(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BiomeType> getBiomes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ItemStackBuilder getItemBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public TradeOfferBuilder getTradeOfferBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public PotionEffectBuilder getPotionEffectBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ObjectiveBuilder getObjectiveBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public TeamBuilder getTeamBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ScoreboardBuilder getScoreboardBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ParticleType> getParticleType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ParticleType> getParticleTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ParticleEffectBuilder getParticleEffectBuilder(ParticleType particle) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SoundType> getSound(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SoundType> getSounds() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<EntityType> getEntity(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<EntityType> getEntities() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Art> getArt(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Art> getArts() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DyeColor> getDye(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<DyeColor> getDyes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseColor> getHorseColor(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseColor> getHorseColors() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseStyle> getHorseStyle(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseStyle> getHorseStyles() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<HorseVariant> getHorseVariant(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<HorseVariant> getHorseVariants() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<OcelotType> getOcelotType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<OcelotType> getOcelotTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<RabbitType> getRabbitType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<RabbitType> getRabbitTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SkeletonType> getSkeletonType(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SkeletonType> getSkeletonTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Career> getCareer(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Career> getCareers() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Career> getCareers(Profession profession) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Profession> getProfession(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Profession> getProfessions() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<GameMode> getGameModes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<PotionEffectType> getPotionEffects() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Enchantment> getEnchantment(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Enchantment> getEnchantments() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<String> getDefaultGameRules() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Statistic> getStatistic(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<EntityStatistic> getEntityStatistic(StatisticGroup statisticGroup, EntityType entityType) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ItemStatistic> getItemStatistic(StatisticGroup statisticGroup, ItemType itemType) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BlockStatistic> getBlockStatistic(StatisticGroup statisticGroup, BlockType blockType) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<TeamStatistic> getTeamStatistic(StatisticGroup statisticGroup, TextColor teamColor) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Statistic> getStatistics(StatisticGroup statisticGroup) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Statistic> getStatistics() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public StatisticBuilder getStatisticBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public StatisticBuilder.EntityStatisticBuilder getEntityStatisticBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public StatisticBuilder.BlockStatisticBuilder getBlockStatisticBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public StatisticBuilder.ItemStatisticBuilder getItemStatisticBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public StatisticBuilder.TeamStatisticBuilder getTeamStatisticBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void registerStatistic(Statistic stat) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<StatisticFormat> getStatisticFormat(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<StatisticFormat> getStatisticFormats() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Achievement> getAchievement(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Achievement> getAchievements() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AchievementBuilder getAchievementBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DimensionType> getDimensionType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<DimensionType> getDimensionTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Rotation> getRotations() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public GameProfile createGameProfile(UUID uuid, String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(String raw) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(File file) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(URL url) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(InputStream in) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Favicon loadFavicon(BufferedImage image) throws IOException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<NotePitch> getNotePitch(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<NotePitch> getNotePitches() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SkullType> getSkullType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SkullType> getSkullTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShape(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<BannerPatternShape> getBannerPatternShapeById(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<BannerPatternShape> getBannerPatternShapes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public GameDictionary getGameDictionary() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public RecipeRegistry getRecipeRegistry() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Difficulty> getDifficulties() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Difficulty> getDifficulty(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<EntityInteractionType> getEntityInteractionTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<EntityInteractionType> getEntityInteractionType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Attribute> getAttribute(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Attribute> getAttributes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Operation> getOperation(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Operation> getOperations() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AttributeModifierBuilder getAttributeModifierBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AttributeCalculator getAttributeCalculator() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AttributeBuilder getAttributeBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<CoalType> getCoalType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<CoalType> getCoalTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Fish> getFishType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Fish> getFishTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<CookedFish> getCookedFishType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<CookedFish> getCookedFishTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<GoldenApple> getGoldenAppleType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<GoldenApple> getGoldenAppleTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public FireworkEffectBuilder getFireworkEffectBuilder() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<TextColor> getTextColor(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<TextColor> getTextColors() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<TextStyle> getTextStyle(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<TextStyle> getTextStyles() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ChatType> getChatType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ChatType> getChatTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<SelectorType> getSelectorType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<SelectorType> getSelectorTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ArgumentType<?>> getArgumentType(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ArgumentType<?>> getArgumentTypes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Locale> getLocale(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Locale> getLocaleById(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Locale> getLocales() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Translation> getTranslationById(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ResourcePack> getById(String id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DisplaySlot> getDisplaySlot(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<DisplaySlot> getDisplaySlotForColor(TextColor color) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<DisplaySlot> getDisplaySlots() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Visibility> getVisibility(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Visibility> getVisibilities() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<Criterion> getCriterion(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<Criterion> getCriteria() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Optional<ObjectiveDisplayMode> getObjectiveDisplayMode(String name) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Collection<ObjectiveDisplayMode> getObjectiveDisplayModes() {
        throw new NotImplementedException("TODO");
    }
}
