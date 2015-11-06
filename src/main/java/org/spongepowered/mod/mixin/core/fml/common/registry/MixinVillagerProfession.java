package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.registry.type.CareerRegistryModule;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;
import org.spongepowered.mod.interfaces.IMixinVillagerProfession;
import org.spongepowered.mod.registry.SpongeVillagerRegistry;

// TODO for now, these are all disabled.
@Mixin(value = VillagerRegistry.VillagerProfession.class, remap = false)
public abstract class MixinVillagerProfession implements IMixinVillagerProfession {

    @Shadow private ResourceLocation name;

    @Override
    public String getId() {
        return this.name.getResourcePath();
    }

    @Inject(method = "register(Lnet/minecraftfroge/fml/common/registry/VillagerRegistry/VillagerCareer;)V", at = @At("RETURN"), remap = false)
    private void registerForgeCareer(VillagerRegistry.VillagerCareer career, CallbackInfo callbackInfo) {
        Profession profession = SpongeVillagerRegistry.getProfession(((IMixinVillagerCareer) career).getProfession()).get();
        Career career1 = new SpongeCareer(((IMixinVillagerCareer) career).getId(), ((IMixinVillagerCareer) career).getName(), profession);
        CareerRegistryModule.getInstance().registerCareer(career1);
    }

}
