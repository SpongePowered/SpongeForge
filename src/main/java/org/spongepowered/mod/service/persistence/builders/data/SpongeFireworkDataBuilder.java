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
package org.spongepowered.mod.service.persistence.builders.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkEffectBuilder;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.service.persistence.DataBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.mod.SpongeMod;

import java.awt.Color;
import java.util.List;

public class SpongeFireworkDataBuilder implements DataBuilder<FireworkEffect> {

    private static final DataQuery TYPE = new DataQuery("Type");
    private static final DataQuery COLORS = new DataQuery("Colors");
    private static final DataQuery FADES = new DataQuery("Fades");
    private static final DataQuery TRAILS = new DataQuery("Trails");
    private static final DataQuery FLICKERS = new DataQuery("Flickers");

    @Override
    public Optional<FireworkEffect> build(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(TYPE)
                || !container.contains(COLORS)
                || !container.contains(FADES)
                || !container.contains(TRAILS)
                || !container.contains(FLICKERS)) {
            throw new InvalidDataException("The container does not have data pertaining to FireworkEffect!");
        }
        String type = container.getString(TYPE).get();
        FireworkShape shape = FireworkShapes.BALL; // TODO, need to add getFireworkShape to GameRegistry...
        List<Integer> intColors = container.getIntegerList(COLORS).get();
        List<Color> colors = Lists.newArrayList();
        for (int colorInt : intColors) {
            colors.add(new Color(colorInt));
        }
        List<Integer> intFades = container.getIntegerList(FADES).get();
        List<Color> fades = Lists.newArrayList();
        for (int fadeInt : intFades) {
            fades.add(new Color(fadeInt));
        }
        boolean trails = container.getBoolean(TRAILS).get();
        boolean flickers = container.getBoolean(FLICKERS).get();
        FireworkEffectBuilder builder = SpongeMod.instance.getGame().getRegistry().getBuilderOf(FireworkEffectBuilder.class).get();
        return Optional.of(builder.colors(colors)
                                   .fades(fades)
                                   .flicker(flickers)
                                   .trail(trails)
                                   .build());
    }
}
