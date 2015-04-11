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
package org.spongepowered.mod.service.persistence.builders.block.tile;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tile.Banner;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.tileentities.BannerData;
import org.spongepowered.api.data.types.DyeColors;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.mod.block.meta.SpongeBannerData;

import java.util.List;

public class SpongeBannerBuilder extends AbstractTileBuilder<Banner> {

    public static final DataQuery BASE = of("Base");
    public static final DataQuery PATTERNS = of("Patterns");

    public SpongeBannerBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Banner> build(DataView container) throws InvalidDataException {
        Optional<Banner> bannerOptional = super.build(container);
        if (!bannerOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a Banner tile entity!");
        }
        final Banner banner = bannerOptional.get();
        if (!container.contains(BASE) || !container.contains(PATTERNS)) {
            throw new InvalidDataException("The provided container does not contain the data to make a Banner!");
        }
        final SerializationService service = this.game.getServiceManager().provide(SerializationService.class).get();

        BannerData bannerData = new SpongeBannerData();

        bannerData.setBaseColor(DyeColors.WHITE); // TODO handle this from registry and datacontainer
        List<BannerData.PatternLayer> patternsList = container.getSerializableList(PATTERNS, BannerData.PatternLayer.class, service).get();
        for (BannerData.PatternLayer pattern : patternsList) {
            bannerData.addPatternLayer(pattern);
        }
        banner.offer(bannerData);
        ((TileEntityBanner) banner).validate();
        return Optional.of(banner);
    }
}
