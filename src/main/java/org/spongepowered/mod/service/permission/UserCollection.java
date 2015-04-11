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
package org.spongepowered.mod.service.permission;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListOpsEntry;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.mod.service.permission.base.SpongeSubjectCollection;

import java.util.UUID;

import javax.annotation.Nullable;


/**
 * User collection keeping track of opped users.
 */
public class UserCollection extends SpongeSubjectCollection {
    private final SpongePermissionService service;

    public UserCollection(SpongePermissionService service) {
        super(PermissionService.SUBJECTS_USER);
        this.service = service;
    }

    @Override
    public Subject get(String identifier) {
        UUID uid = identToUuid(identifier);
        if (uid == null) {
            throw new IllegalArgumentException("Provided identifier must be a uuid, was " + identifier);
        }
        return get(uuidToGameProfile(uid));
    }

    protected Subject get(GameProfile profile) {
        return new UserSubject(profile, this);
    }

    private GameProfile uuidToGameProfile(UUID uid) {
        PlayerProfileCache cache = MinecraftServer.getServer().getPlayerProfileCache();
        GameProfile profile = cache.getProfileByUUID(uid); // Get already cached profile by uuid
        if (profile == null) {
            profile = MinecraftServer.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(uid, null), false);
            cache.addEntry(profile); // Cache newly looked up profile
            cache.save(); // Save
        }
        return profile;
    }

    @Override
    public boolean hasRegistered(String identifier) {
        UUID uid = identToUuid(identifier);
        if (uid == null) {
            return false;
        }
        GameProfile profile = uuidToGameProfile(uid);
        return SpongePermissionService.getOps().getEntry(profile) != null;
    }

    private UUID identToUuid(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Subject> getAllSubjects() {
        return Iterables.<Object, Subject>transform(SpongePermissionService.getOps().getValues().values(), new Function<Object, Subject>() {
            @Nullable
            @Override
            public Subject apply(Object input) {
                GameProfile profile = ((GameProfile) ((UserListOpsEntry) input).value);
                return get(profile);
            }
        });
    }

    public SpongePermissionService getService() {
        return this.service;
    }
}
