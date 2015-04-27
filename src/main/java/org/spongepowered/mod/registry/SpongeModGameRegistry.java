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
import org.spongepowered.common.data.types.SpongeCookedFish;
import org.spongepowered.common.data.types.SpongeNotePitch;
import org.spongepowered.common.data.types.SpongeSkullType;
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

    {
        catalogTypeMap = ImmutableMap.<Class<? extends CatalogType>, Map<String, ? extends CatalogType>>builder()
                .putAll(catalogTypeMap)
                .put(GeneratorType.class, this.generatorTypeMappings)
                .build();
    }

    public com.google.common.base.Optional<BlockType> getBlock(String id) {
        return com.google.common.base.Optional.fromNullable((BlockType) GameData.getBlockRegistry().getObject(id));
    }

    public com.google.common.base.Optional<ItemType> getItem(String id) {
        return com.google.common.base.Optional.fromNullable((ItemType) GameData.getItemRegistry().getObject(id));
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

    public void setGeneratorTypes() {
        this.generatorTypeMappings.put("DEFAULT", (GeneratorType) WorldType.DEFAULT);
        this.generatorTypeMappings.put("FLAT", (GeneratorType) WorldType.FLAT);
        this.generatorTypeMappings.put("DEBUG", (GeneratorType) WorldType.DEBUG_WORLD);
        this.generatorTypeMappings.put("NETHER", (GeneratorType) new SpongeWorldTypeNether());
        this.generatorTypeMappings.put("END", (GeneratorType) new SpongeWorldTypeEnd());
        this.generatorTypeMappings.put("OVERWORLD", (GeneratorType) new SpongeWorldTypeOverworld());
        RegistryHelper.mapFields(GeneratorTypes.class, this.generatorTypeMappings);
    }

    @Override
    public GameDictionary getGameDictionary() {
        return SpongeGameDictionary.instance;
    }

    @Override
    public void init() {
        super.init();

        setGeneratorTypes();
    }

    @Override
    public void postInit() {
        super.postInit();
        setBlockTypes();
        setItemTypes();

    }
}
