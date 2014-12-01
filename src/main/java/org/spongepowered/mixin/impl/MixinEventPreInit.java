/**
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

package org.spongepowered.mixin.impl;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.slf4j.Logger;
import org.slf4j.impl.SLF4JLogger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.mixin.Mixin;
import org.spongepowered.mod.mixin.Shadow;

import java.io.File;

@NonnullByDefault
@Mixin(FMLPreInitializationEvent.class)
public abstract class MixinEventPreInit extends FMLStateEvent implements PreInitializationEvent {

    @Shadow
    private ModContainer modContainer;

    @Shadow
    public abstract org.apache.logging.log4j.Logger getModLog();

    @Shadow
    public abstract File getSuggestedConfigurationFile();

    @Shadow
    public abstract File getModConfigurationDirectory();

    @Override
    public Logger getPluginLog() {
        org.apache.logging.log4j.Logger logger = getModLog();
        return new SLF4JLogger((org.apache.logging.log4j.spi.AbstractLogger) logger, logger.getName());
    }

    @Override
    public File getRecommendedConfigurationFile() {
        return new File(getModConfigurationDirectory(), modContainer.getModId() + ".conf");
    }

    @Override
    public File getRecommendedConfigurationDirectory() {
        return new File(getModConfigurationDirectory(), modContainer.getModId() + "/");
    }

    @Override
    public File getConfigurationDirectory() {
        return getModConfigurationDirectory();
    }

    @Override
    public Game getGame() {
        return SpongeMod.instance.getGame();
    }
}
