package org.spongepowered.mod.interfaces;

import net.minecraftforge.fml.common.registry.VillagerRegistry;

public interface IMixinVillagerCareer {

    VillagerRegistry.VillagerProfession getProfession();

    int getId();

    String getName();

}
