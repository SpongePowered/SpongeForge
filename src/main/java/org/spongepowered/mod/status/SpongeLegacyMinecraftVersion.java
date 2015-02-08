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
package org.spongepowered.mod.status;

import org.spongepowered.api.MinecraftVersion;

public class SpongeLegacyMinecraftVersion implements MinecraftVersion {

    public static final SpongeLegacyMinecraftVersion V1_3 = new SpongeLegacyMinecraftVersion("<=1.3", 39);
    public static final SpongeLegacyMinecraftVersion V1_5 = new SpongeLegacyMinecraftVersion("1.4-1.5", 61);
    public static final SpongeLegacyMinecraftVersion V1_6 = new SpongeLegacyMinecraftVersion("1.6", 78);

    private final String name;
    private final int latestVersion;

    private SpongeLegacyMinecraftVersion(String name, int latestVersion) {
        this.name = name;
        this.latestVersion = latestVersion;
    }

    public SpongeLegacyMinecraftVersion(SpongeLegacyMinecraftVersion base, int version) {
        this.name = base.name;
        this.latestVersion = version;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isLegacy() {
        return true;
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (o == this) {
            return 0;
        }
        return o.isLegacy() ? this.latestVersion - ((SpongeLegacyMinecraftVersion) o).latestVersion : -1;
    }
}
