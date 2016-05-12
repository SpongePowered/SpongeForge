package org.spongepowered.mod.mixin.core.common.world;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.world.WorldManager;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(WorldManager.class)
public abstract class MixinWorldManager {
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
}
