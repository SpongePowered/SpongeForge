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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.item.ItemType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpongeGameDictionary implements GameDictionary {

    public static final GameDictionary instance = new SpongeGameDictionary();

    private SpongeGameDictionary() {
    }

    @Override
    public void register(String key, ItemType type) {
        OreDictionary.registerOre(key, (Item) type);
    }

    @Override
    public Set<ItemType> get(String key) {
        HashSet<ItemType> items = Sets.newHashSet();
        for (net.minecraft.item.ItemStack itemStack : OreDictionary.getOres(key)) {
            items.add((ItemType) itemStack.getItem());
        }
        return items;
    }

    public boolean isFlowerPot() {
        return false;
    }

    @Override
    public Map<String, Set<ItemType>> getAllItems() {
        HashMap<String, Set<ItemType>> allItems = Maps.newHashMap();
        for (String key : OreDictionary.getOreNames()) {
            allItems.put(key, this.get(key));
        }
        return allItems;
    }
}
