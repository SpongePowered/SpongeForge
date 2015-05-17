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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.registry.GameData;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.mod.world.SpongeWorldTypeEnd;
import org.spongepowered.mod.world.SpongeWorldTypeNether;
import org.spongepowered.mod.world.SpongeWorldTypeOverworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@NonnullByDefault
public class SpongeModGameRegistry extends SpongeGameRegistry {
    private final Map<String, GeneratorType> generatorTypeMappings = Maps.newHashMap();
    private final List<BlockType> blockList = new ArrayList<BlockType>();
    private final List<ItemType> itemList = new ArrayList<ItemType>();

    {
        this.catalogTypeMap = ImmutableMap.<Class<? extends CatalogType>, Map<String, ? extends CatalogType>>builder()
                .putAll(this.catalogTypeMap)
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
        for (ResourceLocation resourceLocation : (Iterable<ResourceLocation>) GameData.getBlockRegistry().getKeys()) {
            this.blockList.add(getBlock(resourceLocation.toString()).get());
        }

        RegistryHelper.mapFields(BlockTypes.class, new Function<String, BlockType>() {

            @Override
            public BlockType apply(String fieldName) {
                return getBlock(fieldName.toLowerCase()).get();
            }
        });
    }

    private void setItemTypes() {
        for (ResourceLocation resourceLocation : (Iterable<ResourceLocation>) GameData.getItemRegistry().getKeys()) {
            this.itemList.add(getItem(resourceLocation.toString()).get());
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
