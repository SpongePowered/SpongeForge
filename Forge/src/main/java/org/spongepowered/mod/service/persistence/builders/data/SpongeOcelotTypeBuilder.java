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

package org.spongepowered.mod.service.persistence.builders.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import org.spongepowered.api.entity.living.animal.OcelotType;
import org.spongepowered.api.service.persistence.DataSerializableBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.DataView;
import org.spongepowered.mod.entity.SpongeEntityConstants;

public class SpongeOcelotTypeBuilder implements DataSerializableBuilder<OcelotType> {

    @Override
    public Optional<OcelotType> build(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(new DataQuery("id")) || !container.contains(new DataQuery("name"))) {
            throw new InvalidDataException("The container does not have data pertaining to OcelotType!");
        }
        int id = container.getInt(new DataQuery("id")).get();
        OcelotType ocelotType = SpongeEntityConstants.OCELOT_IDMAP.get(id);
        if (ocelotType == null) {
            throw new InvalidDataException("The container has an invalid OcelotType id!");
        }
        return Optional.of(ocelotType);
    }
}
