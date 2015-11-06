package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeProfession;
import org.spongepowered.common.registry.type.ProfessionRegistryModule;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeVillagerRegistry;

@Mixin(value = VillagerRegistry.class, remap = false)
public class MixinVillagerRegistry {

    @Inject(method = "register(Lnet/minecraftforge/fml/common/registry/VillagerProfession;I)V", at = @At("RETURN"))
    private void registerForgeVillager(VillagerRegistry.VillagerProfession profession, int id, CallbackInfo ci) {
        if (id != -1) {
            Profession spongeProfession = new SpongeProfession(id, ((IMixinVillagerProfession) profession).getId());
            SpongeVillagerRegistry.registerProfession(profession, spongeProfession);
            ProfessionRegistryModule.getInstance().registerAdditionalCatalog(spongeProfession);
        }
    }

}
