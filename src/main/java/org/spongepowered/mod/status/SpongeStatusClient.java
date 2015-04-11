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
package org.spongepowered.mod.status;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import net.minecraft.network.NetworkManager;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.status.StatusClient;
import org.spongepowered.mod.server.ConnectionInfo;

import java.net.InetSocketAddress;

public class SpongeStatusClient implements StatusClient {

    private final ConnectionInfo connection;

    public SpongeStatusClient(NetworkManager networkManager) {
        this.connection = (ConnectionInfo) networkManager;
    }

    @Override
    public InetSocketAddress getAddress() {
        return this.connection.getAddress();
    }

    @Override
    public MinecraftVersion getVersion() {
        return this.connection.getVersion();
    }

    @Override
    public Optional<InetSocketAddress> getVirtualHost() {
        return Optional.fromNullable(this.connection.getVirtualHost());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeStatusClient)) {
            return false;
        }

        SpongeStatusClient that = (SpongeStatusClient) o;
        return Objects.equal(this.connection, that.connection);

    }

    @Override
    public int hashCode() {
        return this.connection.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(this.connection)
                .toString();
    }
}
