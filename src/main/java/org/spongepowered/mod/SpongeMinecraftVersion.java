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

import com.google.common.base.MoreObjects;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class SpongeMinecraftVersion implements ProtocolMinecraftVersion {

    private final String name;
    private final int protocol;

    public SpongeMinecraftVersion(String name, int protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    public static int compare(ProtocolMinecraftVersion version, MinecraftVersion to) {
        if (version.equals(to)) {
            return 0;
        } else if (to.isLegacy()) {
            return 1;
        } else {
            return version.getProtocol() - ((ProtocolMinecraftVersion) to).getProtocol();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getProtocol() {
        return this.protocol;
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProtocolMinecraftVersion)) {
            return false;
        }

        ProtocolMinecraftVersion that = (ProtocolMinecraftVersion) o;
        return this.getProtocol() == that.getProtocol();

    }

    @Override
    public int hashCode() {
        return this.protocol;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("protocol", this.protocol)
                .toString();
    }
}
