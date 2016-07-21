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
package org.spongepowered.mod.mixin.core.forge;

import net.minecraftforge.common.UsernameCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Map;
import java.util.UUID;

@Mixin(value = UsernameCache.class, remap = false)
public class MixinUsernameCache {

    @Overwrite
    protected static void setUsername(UUID uuid, String username) {
        SpongeUsernameCache.setUsername(uuid, username);
    }

    @Overwrite
    protected static boolean removeUsername(UUID uuid) {
        return SpongeUsernameCache.removeUsername(uuid);
    }

    @Overwrite
    public static String getLastKnownUsername(UUID uuid) {
        return SpongeUsernameCache.getLastKnownUsername(uuid);
    }

    @Overwrite
    public static boolean containsUUID(UUID uuid) {
        return SpongeUsernameCache.containsUUID(uuid);
    }

    @Overwrite
    public static Map<UUID, String> getMap() {
        return SpongeUsernameCache.getMap();
    }

    @Overwrite
    protected static void save() {
        SpongeUsernameCache.save();
    }

    @Overwrite
    protected static void load() {
        SpongeUsernameCache.load();
    }
}
