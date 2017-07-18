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

import com.mojang.authlib.GameProfile;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.IContext;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

// https://github.com/SpongePowered/SpongeForge/issues/1049#issuecomment-269775514
public enum SpongePermissionHandler implements IPermissionHandler {
    INSTANCE;

    private final Set<String> nodes = new HashSet<>();

    public void adopt() {
        PermissionAPI.setPermissionHandler(this);
    }

    public void forceAdoption() {
        SpongeImpl.getLogger().debug("Forcing adoption of the Sponge permission handler - previous handler was '{}'", PermissionAPI.getPermissionHandler().getClass().getName());
        RegistryHelper.setFinalStatic(PermissionAPI.class, "permissionHandler", this);
    }

    @Override
    public void registerNode(final String node, final DefaultPermissionLevel level, final String desc) {
        this.nodes.add(node);
        // TODO: do.... what with level and desc?
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return Collections.unmodifiableSet(this.nodes);
    }

    @Override
    public boolean hasPermission(final GameProfile profile, final String node, @Nullable final IContext context) {
        @Nullable final Player player = Sponge.getServer().getPlayer(profile.getId()).orElse(null);
        if (player != null) {
            // TODO contexts?
            return player.hasPermission(node);
        }

        // TODO contexts?
        return Sponge.getServiceManager().getRegistration(UserStorageService.class)
                .map(ProviderRegistration::getProvider)
                .flatMap(service -> service.get(profile.getId()))
                .map(user1 -> user1.hasPermission(node))
                .orElse(false);
    }

    @Override
    public String getNodeDescription(final String node) {
        return this.getService().getDescription(node)
                .map(PermissionDescription::getDescription)
                .map(text -> text.orElse(null))
                .orElse(Text.EMPTY).toPlain();
    }

    private PermissionService getService() {
        return Sponge.getServiceManager().provideUnchecked(PermissionService.class);
    }
}
