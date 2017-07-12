package org.spongepowered.mod.mixin.core.fml.common.network.handshake;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(FMLHandshakeMessage.RegistryData.class)
public abstract class MixinFMLHandshakeMessageRegistryData {

    @Shadow private Map<ResourceLocation, Integer> ids;

    @Inject(method = "<init>(ZLnet/minecraft/util/ResourceLocation;Lnet/minecraftforge/registries/ForgeRegistry$Snapshot;)V", at = @At("RETURN"))
    private void onInit(boolean hasMore, ResourceLocation name, ForgeRegistry.Snapshot entry, CallbackInfo ci) {
        this.ids.remove(new ResourceLocation(EntityTypes.HUMAN.getId()));
    }
}
