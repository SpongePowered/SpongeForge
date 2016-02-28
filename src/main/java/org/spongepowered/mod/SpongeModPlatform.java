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

import static org.spongepowered.common.SpongeImpl.API_ID;
import static org.spongepowered.common.SpongeImpl.API_NAME;
import static org.spongepowered.common.SpongeImpl.ECOSYSTEM_ID;
import static org.spongepowered.common.SpongeImpl.ECOSYSTEM_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.AbstractPlatform;

@Singleton
public final class SpongeModPlatform extends AbstractPlatform {

    @Inject
    public SpongeModPlatform(@Named(API_ID) PluginContainer api, @Named(ECOSYSTEM_ID) PluginContainer impl, MinecraftVersion minecraftVersion) {
        super(api, impl, minecraftVersion);
        this.platformMap.put("ForgeVersion", ForgeVersion.getVersion());
    }

    @Override
    public Type getType() {
        return switchOn(FMLCommonHandler.instance().getSide());
    }

    @Override
    public Type getExecutionType() {
        return switchOn(FMLCommonHandler.instance().getEffectiveSide());
    }

    private Type switchOn(Side side) {
        switch (side) {
            case CLIENT:
                return Type.CLIENT;
            case SERVER:
                return Type.SERVER;
            default:
                return Type.UNKNOWN;
        }
    }
}
