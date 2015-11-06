package org.spongepowered.mod.mixin.core.fml.common.registry;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.interfaces.IMixinVillagerCareer;

@Mixin(value = VillagerRegistry.VillagerCareer.class, remap = false)
public class MixinVillagerCareer implements IMixinVillagerCareer {

    @Shadow private int id;
    @Shadow private VillagerRegistry.VillagerProfession profession;

    @Override
    public VillagerRegistry.VillagerProfession getProfession() {
        return this.profession;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return null;
    }
}
