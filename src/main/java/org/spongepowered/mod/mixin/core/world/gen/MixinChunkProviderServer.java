package org.spongepowered.mod.mixin.core.world.gen;

import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.WorldManager;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {

    @Shadow @Final public WorldServer worldObj;

    @Redirect(method = "unloadQueuedChunks", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/DimensionManager;unloadWorld(I)V"))
    private void unloadWorld(int dimId) {
        WorldManager.unloadWorld(this.worldObj, true, true);
    }
}
