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
package org.spongepowered.mod.item.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.items.EnchantmentData;
import org.spongepowered.api.item.Enchantment;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SpongeEnchantmentItemData extends AbstractDataManipulator<EnchantmentData> implements EnchantmentData {

    private static final DataTransactionResult SUCCESS_NO_DATA = new DataTransactionResult() {
        @Override
        public Type getType() {
            return Type.SUCCESS;
        }

        @Override
        public Optional<Collection<DataManipulator<?>>> getRejectedData() {
            return Optional.absent();
        }

        @Override
        public Optional<Collection<DataManipulator<?>>> getReplacedData() {
            return Optional.absent();
        }
    };
    private Map<Enchantment, Integer> enchantments = Maps.newHashMap();

    public SpongeEnchantmentItemData() {
    }

    public SpongeEnchantmentItemData(Map<Enchantment, Integer> enchantments) {
        // Defensively copy
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            this.setUnsafe(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<Enchantment> getKeys() {
        return ImmutableSet.copyOf(this.enchantments.keySet());
    }


    @Override
    public Map<Enchantment, Integer> asMap() {
        return ImmutableMap.copyOf(this.enchantments);
    }

    @Override
    public Optional<Integer> get(Enchantment key) {
        checkNotNull(key);
        return Optional.fromNullable(this.enchantments.get(key));
    }

    @Override
    public DataTransactionResult set(Enchantment key, Integer value) {
        checkNotNull(key);
        checkNotNull(value);
        if (key.getMinimumLevel() > value || value > key.getMaximumLevel()) {
            return rejectSelf(key, value);
        }
        Set<Enchantment> incompatible = getIncompatibleEnchantments(key);
        if (!incompatible.isEmpty()) {
            return rejectSelf(key, value);
        } else {
            final DataTransactionResult result;
            if (this.enchantments.containsKey(key)) {
                result = replace(key, value);
            } else {
                result = SUCCESS_NO_DATA;
            }
            this.enchantments.put(key, value);
            return result;
        }
    }

    @Override
    public DataTransactionResult set(Map<Enchantment, Integer> mapped) {
        checkNotNull(mapped);
        ImmutableSet.Builder<DataManipulator<?>> builder = ImmutableSet.builder();
        for (Map.Entry<Enchantment, Integer> entry : mapped.entrySet()) {
            DataTransactionResult result = set(entry.getKey(), entry.getValue());
            if (result.getType() == DataTransactionResult.Type.SUCCESS) {
                if (result.getReplacedData().isPresent()) {
                    builder.addAll(result.getReplacedData().get());
                }
            } else {
                rejected(result, builder.build());
            }
        }
        return succesReplaced(builder.build());
    }


    @Override
    public DataTransactionResult set(Enchantment... mapped) {
        checkNotNull(mapped);
        ImmutableSet.Builder<DataManipulator<?>> builder = ImmutableSet.builder();
        for (Enchantment enchantment : mapped) {
            DataTransactionResult result = set(enchantment, enchantment.getMinimumLevel());
            if (result.getType() == DataTransactionResult.Type.SUCCESS) {
                if (result.getReplacedData().isPresent()) {
                    builder.addAll(result.getReplacedData().get());
                }
            } else {
                rejected(result, builder.build());
            }
        }
        return succesReplaced(builder.build());
    }

    @Override
    public void setUnsafe(Enchantment key, Integer value) {
        checkNotNull(key);
        checkNotNull(value);
        this.enchantments.put(key, value);
    }

    @Override
    public void setUnsafe(Map<Enchantment, Integer> mapped) {
        checkNotNull(mapped);
        for (Map.Entry<Enchantment, Integer> entry : mapped.entrySet()) {
            setUnsafe(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void remove(Enchantment key) {
        checkNotNull(key);
        this.enchantments.remove(key);
    }

    @Override
    public int compareTo(EnchantmentData enchantmentData) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        // We have to use a DataView.
        ImmutableList.Builder<DataView> enchantments = ImmutableList.builder();
        for (Map.Entry<Enchantment, Integer> entry : asMap().entrySet()) {
            // Since we can't directly create data views.
            SpongeEnchantment enchantment = new SpongeEnchantment(entry.getKey(), entry.getValue());
            enchantments.add(enchantment.toContainer());
        }
        container.set(of("ench"), enchantments);
        return container;
    }

    private Set<Enchantment> getIncompatibleEnchantments(final Enchantment query) {
        ImmutableSet.Builder<Enchantment> builder = ImmutableSet.builder();
        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            if (!entry.getKey().isCompatibleWith(query)) {
                builder.add(entry.getKey());
            }
        }
        return builder.build();
    }

    private DataTransactionResult rejectSelf(final Enchantment enchantment, final Integer value) {
        return new DataTransactionResult() {
            @Override
            public Type getType() {
                return Type.FAILURE;
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getRejectedData() {
                return Optional.<Collection<DataManipulator<?>>>of(ImmutableList.<DataManipulator<?>>of(
                        new SpongeEnchantmentItemData(ImmutableMap.of(enchantment, value))));
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getReplacedData() {
                return Optional.absent();
            }
        };
    }

    private DataTransactionResult replace(final Enchantment key, final Integer value) {
        return new DataTransactionResult() {
            @Override
            public Type getType() {
                return Type.SUCCESS;
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getRejectedData() {
                return Optional.<Collection<DataManipulator<?>>>of(ImmutableList.<DataManipulator<?>>of(
                        new SpongeEnchantmentItemData(ImmutableMap.of(key, value))));
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getReplacedData() {
                return Optional.absent();
            }
        };
    }

    private DataTransactionResult succesReplaced(final ImmutableSet<DataManipulator<?>> set) {
        return new DataTransactionResult() {
            @Override
            public Type getType() {
                return Type.SUCCESS;
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getRejectedData() {
                return Optional.absent();
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getReplacedData() {
                return Optional.<Collection<DataManipulator<?>>>of(set);
            }
        };
    }

    private DataTransactionResult rejected(final DataTransactionResult result, final ImmutableSet<DataManipulator<?>> build) {
        return new DataTransactionResult() {
            @Override
            public Type getType() {
                return result.getType();
            }

            @Override
            public Optional<? extends Collection<? extends DataManipulator<?>>> getRejectedData() {
                return result.getRejectedData();
            }

            @Override
            public Optional<Collection<DataManipulator<?>>> getReplacedData() {
                return Optional.<Collection<DataManipulator<?>>>of(build);
            }
        };
    }

    @Override
    public Optional<EnchantmentData> fill(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<EnchantmentData> fill(DataHolder dataHolder, DataPriority overlap) {
        return null;
    }

    @Override
    public Optional<EnchantmentData> from(DataContainer container) {
        return null;
    }
}
