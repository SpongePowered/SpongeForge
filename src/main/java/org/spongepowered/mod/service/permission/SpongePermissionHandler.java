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

import static com.google.common.base.Preconditions.checkNotNull;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.IContext;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.service.permission.SpongePermissionService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

// https://github.com/SpongePowered/SpongeForge/issues/1049#issuecomment-269775514
public enum SpongePermissionHandler implements IPermissionHandler {
    INSTANCE;

    private static final int DEFAULT_OP_LEVEL = 2; // Op level 2 is reasonable?

    private final Map<String, NodeInfo> nodes = new HashMap<>();
    private PermissionService service;

    public void adopt() {
        this.service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        PermissionAPI.setPermissionHandler(this);
    }

    public void forceAdoption() {
        SpongeImpl.getLogger().debug("Forcing adoption of the Sponge permission handler - previous handler was '{}'",
                PermissionAPI.getPermissionHandler().getClass().getName());
        RegistryHelper.setFinalStatic(PermissionAPI.class, "permissionHandler", this);
    }

    private PermissionService getService() {
        return checkNotNull(this.service, "SpongePermissionHandler is not yet adopted.");
    }

    @Override
    public void registerNode(final String node, final DefaultPermissionLevel level, final String desc) {
        final ModContainer activeMod = Loader.instance().activeModContainer();
        checkNotNull(activeMod, "Active mod shouldn't be null when registering nodes.");

        final NodeInfo info = new NodeInfo(activeMod, Text.of(desc), level);
        this.nodes.put(node, info);

        registerNode(node, info);
    }

    private void registerNode(String node, NodeInfo info) {
        final PermissionDescription.Builder builder = getService().newDescriptionBuilder(info.mod)
                .id(node).description(info.desc);
        if (info.level == DefaultPermissionLevel.OP &&
                service instanceof SpongePermissionService) { // OP levels are only supported by the sponge permission service
            builder.assign(((SpongePermissionService) getService()).getGroupForOpLevel(DEFAULT_OP_LEVEL).getIdentifier(), true);
        } else {
            builder.assign(PermissionService.SUBJECTS_DEFAULT, info.level == DefaultPermissionLevel.ALL);
        }

        builder.register();
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return Collections.unmodifiableSet(this.nodes.keySet());
    }

    @Override
    public boolean hasPermission(final GameProfile profile, final String node, @Nullable final IContext context) {
        @Nullable Player player = context == null ? null : (Player) context.getPlayer();
        if (player == null) {
            player = Sponge.getServer().getPlayer(profile.getId()).orElse(null);
        }

        if (player != null) {
            return player.hasPermission(getContexts(player, context), node);
        }

        return getService().getUserSubjects().getSubject(profile.getId().toString())
                .map(subject -> subject.hasPermission(getContexts(subject, context), node))
                .orElse(false);
    }

    private Set<Context> getContexts(Contextual contextual, @Nullable final IContext context) {
        Set<Context> contexts = contextual.getActiveContexts();
        if (context != null) {
            final World world = (World) context.getWorld();
            if (world != null) {
                contexts = new HashSet<>(contexts);
                contexts.add(new Context(Context.WORLD_KEY, world.getName()));
            }
        }
        // TODO: Add contexts based on ContextKeys? Which ones are useful?
        // - POS (block pos)
        // - TARGET (target entity)
        // - FACING
        // - AREA (protection plugins?)
        // - BLOCK_STATE
        return contexts;
    }

    @Override
    public String getNodeDescription(final String node) {
        return getService().getDescription(node)
                .map(PermissionDescription::getDescription)
                .map(text -> text.orElse(null))
                .orElse(Text.EMPTY).toPlain();
    }

    @Listener
    public void onChangeService(ChangeServiceProviderEvent event) {
        final Object provider = event.getNewProvider();
        if (!(provider instanceof PermissionService)) {
            return;
        }
        this.service = (PermissionService) provider;
        for (Map.Entry<String, NodeInfo> entry : this.nodes.entrySet()) {
            registerNode(entry.getKey(), entry.getValue());
        }
    }

    static final class NodeInfo {

        final ModContainer mod;
        final Text desc;
        final DefaultPermissionLevel level;

        NodeInfo(ModContainer mod, Text desc, DefaultPermissionLevel permissionLevel) {
            this.mod = mod;
            this.desc = desc;
            this.level = permissionLevel;
        }
    }
}
