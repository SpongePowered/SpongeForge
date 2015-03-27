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
package org.spongepowered.common.launch;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import javax.annotation.Nullable;

public class SpongeLaunch {

    @Nullable private static File gameDir;
    @Nullable private static File configDir;
    @Nullable private static File pluginsDir;

    private SpongeLaunch() {
    }

    public static void initialize(File gameDir, File configDir, File pluginsDir) {
        if (gameDir == null) {
            gameDir = new File(".");
        }

        SpongeLaunch.gameDir = gameDir;
        SpongeLaunch.configDir = configDir != null ? configDir : new File(gameDir, "config");
        SpongeLaunch.pluginsDir = pluginsDir != null ? pluginsDir : new File(gameDir, "mods");
    }

    public static File getGameDirectory() {
        checkState(gameDir != null, "Sponge was not initialized");
        return gameDir;
    }

    public static File getConfigDirectory() {
        checkState(configDir != null, "Sponge was not initialized");
        return configDir;
    }

    public static File getPluginsDirectory() {
        checkState(pluginsDir != null, "Sponge was not initialized");
        return pluginsDir;
    }

}
