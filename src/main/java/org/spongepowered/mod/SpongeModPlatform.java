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

package org.spongepowered.mod;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.common.AbstractPlatform;

public class SpongeModPlatform extends AbstractPlatform {

    public SpongeModPlatform(MinecraftVersion minecraftVersion, String apiVersion, String version) {
        super(minecraftVersion, apiVersion, version);
        this.platformMap.put("ForgeVersion", ForgeVersion.getVersion());
    }

    @Override
    public Type getType() {
        switch (FMLCommonHandler.instance().getSide()) {
            case CLIENT:
                return Type.CLIENT;
            case SERVER:
                return Type.SERVER;
            default:
                return Type.UNKNOWN;
        }
    }

    @Override
    public Type getExecutionType() {
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT:
                return Type.CLIENT;
            case SERVER:
                return Type.SERVER;
            default:
                return Type.UNKNOWN;
        }
    }

}
