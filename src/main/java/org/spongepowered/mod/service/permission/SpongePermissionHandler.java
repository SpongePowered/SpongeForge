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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

// https://github.com/SpongePowered/SpongeForge/issues/1049#issuecomment-269775514
public enum SpongePermissionHandler implements IPermissionHandler {
    INSTANCE;

    public void adopt() {
        PermissionAPI.setPermissionHandler(this);
    }

    public void forceAdoption() {
        SpongeImpl.getLogger().debug("Forcing adoption of the Sponge permission handler - previous handler was '{}'",
                PermissionAPI.getPermissionHandler().getClass().getName());
        RegistryHelper.setFinalStatic(PermissionAPI.class, "permissionHandler", this);
    }

    @Override
    public void registerNode(final String node, final DefaultPermissionLevel level, final String desc) {
        PluginContainer activeContainer = SpongeImplHooks.getActiveModContainer();
        PermissionDescription.Builder builder = this.getService().newDescriptionBuilder(activeContainer);
        builder.id(node).description(Text.of(desc)).register();
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return this.getService().getDescriptions().stream().map(desc -> desc.getId()).collect(Collectors.toCollection(() -> new LinkedList<>()));
    }

    @Override
    public boolean hasPermission(final GameProfile profile, final String node, @Nullable final IContext context) {
        @Nullable final Subject subject = this.getService().getUserSubjects().getSubject(profile.getId().toString()).orElse(null);
        if (subject != null) {
            if (context != null && context.getPlayer() != null) {
                return subject.hasPermission(((Player) context.getPlayer()).getActiveContexts(), node);
            } else {
                return subject.hasPermission(node);
            }
        }

        return false;
    }

    @Override
    public String getNodeDescription(final String node) {
        @Nullable final PermissionDescription desc = this.getService().getDescription(node).orElse(null);
        return desc == null ? "" : TextSerializers.FORMATTING_CODE.serialize(desc.getDescription().orElse(Text.EMPTY));
    }

    private PermissionService getService() {
        return Sponge.getServiceManager().provideUnchecked(PermissionService.class);
    }
}
