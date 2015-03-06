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
package org.spongepowered.mod;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Map;

public class SpongeCoremod implements IFMLLoadingPlugin {

    public SpongeCoremod() {
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.sponge.core.json");
        env.addConfiguration("mixins.sponge.api.json");
        env.addConfiguration("mixins.sponge.entityactivation.json");

        // Transformer exclusions
        Launch.classLoader.addTransformerExclusion("ninja.leaping.configurate");
        Launch.classLoader.addTransformerExclusion("org.apache.commons.lang3");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                MixinBootstrap.TRANSFORMER_CLASS,
        };
    }

    @Override
    public String getModContainerClass() {
        return "org.spongepowered.mod.SpongeMod";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return "org.spongepowered.mod.asm.transformers.SpongeAccessTransformer";
    }

}
