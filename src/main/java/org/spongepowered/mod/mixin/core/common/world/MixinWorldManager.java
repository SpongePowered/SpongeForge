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
package org.spongepowered.mod.mixin.core.common.world;

import static com.google.common.base.Preconditions.checkNotNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.world.WorldManager;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(value = WorldManager.class, remap = false)
public abstract class MixinWorldManager {

    @Shadow @Final private static Int2ObjectMap<Path> dimensionPathByDimensionId;
 
    /**
     * @author Zidane - May 11th, 2016
     * @reason Rewrites our save directory finding to use FML for client and server.
     *
     * @return The path if available
     */
    @Overwrite
    public static Optional<Path> getCurrentSavesDirectory() {
        final Optional<WorldServer> optWorldServer = WorldManager.getWorldByDimensionId(0);

        if (optWorldServer.isPresent()) {
            return Optional.of(optWorldServer.get().getSaveHandler().getWorldDirectory().toPath());
        }

        if (Sponge.getPlatform().getType().isClient()) {
            return Optional.ofNullable(FMLCommonHandler.instance().getSavesDirectory().toPath());
        }
        return Optional.empty();
    }

    @Overwrite
    public static void sendDimensionRegistration(EntityPlayerMP player, WorldProvider provider) {
        // register dimension on client-side
        if (((IMixinEntityPlayerMP) player).usesCustomClient()) {
            FMLEmbeddedChannel serverChannel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
            serverChannel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(provider.getDimension(), provider.getDimensionType().name()));
        }
    }

    /**
     * @author blood - August 10th, 2016
     * @reason Constructs a WorldProvider instance in order to locate the save folder name to use.
     *
     * @return The path if available
     */
    @Overwrite
    public static Optional<Path> getWorldFolder(DimensionType dimensionType) {
        if (dimensionType == null) {
            return Optional.empty();
        }
        Path path = dimensionPathByDimensionId.get(dimensionType.getId());
        if (path == null) {
            try {
                WorldProvider provider = dimensionType.createDimension();
                provider.setDimension(dimensionType.getId());
                String worldFolder = provider.getSaveFolder();
                path = Sponge.getGame().getSavesDirectory().resolve(worldFolder);
                WorldManager.registerDimensionPath(dimensionType.getId(), path);
            } catch (Throwable t) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(path);
    }

    /**
     * @author blood - August 10th, 2016
     * @reason Registers DimensionType with passed ID to not break mods.
     *
     * @return true if successful
     */
    @Overwrite
    public static boolean registerDimensionType(DimensionType type) {
        checkNotNull(type);
        return WorldManager.registerDimensionType(type.getId(), type);
    }
}
