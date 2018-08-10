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

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.world.WorldManager;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = WorldManager.class, priority = 999, remap = false)
public abstract class MixinWorldManager {

    @Shadow @Final private static Int2ObjectMap<DimensionType> dimensionTypeByTypeId;
    @Shadow @Final private static Int2ObjectMap<DimensionType> dimensionTypeByDimensionId;
    @Shadow @Final private static Int2ObjectMap<Path> dimensionPathByDimensionId;
    @Shadow @Final private static Int2ObjectOpenHashMap<WorldServer> worldByDimensionId;
    @Shadow @Final private static Map<String, WorldProperties> worldPropertiesByFolderName;
    @Shadow @Final private static Map<UUID, WorldProperties> worldPropertiesByWorldUuid;
    @Shadow @Final private static Map<Integer, String> worldFolderByDimensionId;
    @Shadow @Final private static BiMap<String, UUID> worldUuidByFolderName;
    @Shadow @Final private static BitSet dimensionBits;

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
        if (SpongeImpl.getGame().getState().ordinal() >= GameState.SERVER_ABOUT_TO_START.ordinal()) {
            SaveHandler saveHandler = (SaveHandler) SpongeImpl.getServer().getActiveAnvilConverter().getSaveLoader(SpongeImpl.getServer().getFolderName(), false);
            return Optional.of(saveHandler.getWorldDirectory().toPath());
        }
        return Optional.empty();
    }

    /**
     * @author unknown
     * @reason forge
     */
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
    @Nullable
    @Overwrite
    private static Path getWorldFolder(DimensionType dimensionType, int dimensionId) {
        Path path = dimensionPathByDimensionId.get(dimensionId);
        if (path != null) {
            return path;
        }

        if (dimensionType == null) {
            return null;
        }

        try {
            final WorldProvider provider = dimensionType.createDimension();
            provider.setDimension(dimensionId);
            final String worldFolder = provider.getSaveFolder();
            path = WorldManager.getCurrentSavesDirectory().get().resolve(worldFolder);
            WorldManager.registerDimensionPath(dimensionId, path);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return path;
    }

    /**
     * @author blood - August 10th, 2016
     * @reason Registers DimensionType with passed ID to not break mods.
     */
    @Overwrite
    public static void registerDimensionType(DimensionType type) {
        checkNotNull(type);
        WorldManager.registerDimensionType(type.getId(), type);
    }

    /**
     * @author blood - February 5th, 2017
     * @reason Registers dimension id with passed ID and type to not break mods.
     */
    @Overwrite
    public static void registerDimension(int dimensionId, DimensionType type) {
        checkNotNull(type);

        if (dimensionTypeByDimensionId.containsKey(dimensionId)) {
            return;
        }
        dimensionTypeByDimensionId.put(dimensionId, type);
        dimensionTypeByTypeId.put(dimensionId, type);
        if (dimensionId >= 0) {
            dimensionBits.set(dimensionId);
        }
    }

    /**
     * @author unknown
     * @reason hell if I know... I believe it's to properly unregister dimensions from forge mods that may not have been
     * loaded or registered before.
     * @param dimensionId the dimension id.
     */
    @Overwrite
    public static void unregisterDimension(int dimensionId) {
        dimensionTypeByDimensionId.remove(dimensionId);
        dimensionTypeByTypeId.remove(dimensionId);
        dimensionPathByDimensionId.remove(dimensionId);
        if (dimensionId >= 0) {
            dimensionBits.clear(dimensionId);
        }
        String worldFolder = worldFolderByDimensionId.get(dimensionId);
        UUID worldUniqueId;
        if (worldFolder != null) {
            worldPropertiesByFolderName.remove(worldFolder);
            worldUniqueId = worldUuidByFolderName.get(worldFolder);
            if (worldUniqueId != null) {
                worldPropertiesByWorldUuid.remove(worldUniqueId);
            }
            worldUuidByFolderName.remove(worldFolder);
            worldPropertiesByFolderName.remove(worldFolder);
        }
        worldByDimensionId.remove(dimensionId);
        worldFolderByDimensionId.remove(dimensionId);
    }
}
