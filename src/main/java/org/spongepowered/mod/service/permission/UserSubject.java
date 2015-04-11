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

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.mod.service.permission.base.SingleParentMemorySubjectData;
import org.spongepowered.mod.service.permission.base.SpongeSubject;

import java.util.Set;

/**
 * An implementation of vanilla minecraft's 4 op groups.
 */
public class UserSubject extends SpongeSubject {
    private final GameProfile player;
    private final MemorySubjectData data;
    private final UserCollection collection;

    public UserSubject(final GameProfile player, final UserCollection users) {
        super(users.getService());
        this.player = player;
        this.data = new SingleParentMemorySubjectData(users.getService()) {
            @Override
            public Subject getParent() {
                int opLevel = getOpLevel();
                return opLevel == 0 ? null : users.getService().getGroupForOpLevel(opLevel);
            }

            @Override
            public boolean setParent(Subject parent) {
                int opLevel;
                if (parent == null) {
                    opLevel = 0;
                } else {
                    if (!(parent instanceof OpLevelCollection.OpLevelSubject)) {
                        return false;
                    }
                    opLevel = ((OpLevelCollection.OpLevelSubject) parent).getOpLevel();
                }
                if (opLevel > 0) {
                    SpongePermissionService.getOps().addEntry(new UserListOpsEntry(player, opLevel));
                } else {
                    SpongePermissionService.getOps().removeEntry(player);
                }
                return true;
            }
        };
        this.collection = users;
    }

    @Override
    public String getIdentifier() {
        return this.player.getId().toString();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.fromNullable((CommandSource) MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(this.player.getId()));
    }

    int getOpLevel() {
        // Query op level from server ops list based on player's game profile
        UserListOpsEntry entry = ((UserListOpsEntry) SpongePermissionService.getOps().getEntry(this.player));
        if (entry == null) {
            return MinecraftServer.getServer().getConfigurationManager().canSendCommands(this.player) ? MinecraftServer.getServer()
                    .getOpPermissionLevel() : 0; // Take care of singleplayer commands -- unless an op level is specified, this player follows
                    // global rules
        } else {
            return entry.getPermissionLevel();
        }
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.collection;
    }

    @Override
    public MemorySubjectData getData() {
        return this.data;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Tristate ret = super.getPermissionValue(contexts, permission);
        if (ret == Tristate.UNDEFINED) {
            ret = getDataPermissionValue(this.collection.getService().getDefaultData(), permission);
        }
        if (ret == Tristate.UNDEFINED && getOpLevel() >= this.collection.getService().getServerOpLevel()) {
            ret = Tristate.TRUE;
        }
        return ret;

    }
}
