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

package org.spongepowered.mod.service.persistence.builders.block.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.service.persistence.data.DataQuery.of;

import com.google.common.base.Optional;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tile.data.BannerData.PatternLayer;
import org.spongepowered.api.block.tile.data.BannerPatternShape;
import org.spongepowered.api.item.DyeColor;
import org.spongepowered.api.service.persistence.DataSerializableBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.DataView;
import org.spongepowered.mod.block.meta.SpongePatternLayer;

public class SpongePatternLayerBuilder implements DataSerializableBuilder<PatternLayer> {

    private static final DataQuery ID = of("id");
    private static final DataQuery COLOR = of("color");
    private final Game game;

    public SpongePatternLayerBuilder(Game game) {
        this.game = game;
    }

    @Override
    public Optional<PatternLayer> build(final DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(ID) || !container.contains(COLOR)) {
            throw new InvalidDataException("The provided container does not contain the data to make a PatternLayer!");
        }
        String id = container.getString(ID).get();
        Optional<BannerPatternShape> shapeOptional = this.game.getRegistry().getBannerPatternShape(id);
        if (!shapeOptional.isPresent()) {
            throw new InvalidDataException("The provided container has an invalid banner pattern shape entry!");
        }
        SerializationService service = this.game.getServiceManager().provide(SerializationService.class).get();
        Optional<DyeColor> colorOptional = container.getSerializable(COLOR, DyeColor.class, service);
        if (!colorOptional.isPresent()) {
            throw new InvalidDataException("The provided container has an invalid dye color entry!");
        }
        return Optional.<PatternLayer>of(new SpongePatternLayer(shapeOptional.get(), colorOptional.get()));
    }
}
