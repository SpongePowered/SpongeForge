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

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

public class SpongeApiModContainer extends DummyModContainer {

    public static PluginContainer instance;

    public SpongeApiModContainer() {
        super(new ModMetadata());

        ModMetadata md = getMetadata();
        md.modId = Platform.API_ID;
        md.name = SpongeImpl.API_NAME;
        md.version = SpongeImpl.API_VERSION.orElse("");

        instance = (PluginContainer) this;
    }

    // Implement methods in PluginContainer

    public Optional<String> getMinecraftVersion() {
        // Return the same Minecraft version as returned by the implementation
        return SpongeImpl.getPlugin().getMinecraftVersion();
    }

    public Logger getLogger() {
        return SpongeImpl.getSlf4jLogger();
    }

    @Override
    public Object getMod() {
        return SpongeImpl.getGame();
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

}
