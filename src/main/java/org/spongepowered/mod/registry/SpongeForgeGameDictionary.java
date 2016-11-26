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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.common.registry.SpongeGameDictionaryEntry;

import java.util.Set;

public class SpongeForgeGameDictionary implements GameDictionary {

    public static final GameDictionary instance = new SpongeForgeGameDictionary();

    private SpongeForgeGameDictionary() {
    }

    @Override
    public void register(String key, Entry entry) {
        ItemStack stack = ((SpongeGameDictionaryEntry) entry).createDictionaryStack(OreDictionary.WILDCARD_VALUE);
        stack.setCount(1);
        OreDictionary.registerOre(key, stack);
    }

    @Override
    public Set<Entry> get(String key) {
        ImmutableSet.Builder<Entry> items = ImmutableSet.builder();
        for (ItemStack itemStack : OreDictionary.getOres(key)) {
        	itemStack = itemStack.copy();
        	itemStack.setCount(1);
            items.add(SpongeGameDictionaryEntry.of(itemStack, OreDictionary.WILDCARD_VALUE));
        }
        return items.build();
    }

    @Override
    public SetMultimap<String, Entry> getAll() {
        ImmutableSetMultimap.Builder<String, Entry> allItems = ImmutableSetMultimap.builder();
        for (String key : OreDictionary.getOreNames()) {
            allItems.putAll(key, this.get(key));
        }
        return allItems.build();
    }
}
