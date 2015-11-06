package org.spongepowered.mod.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.api.data.type.Profession;

import java.util.Optional;

public class SpongeVillagerRegistry {

    private static final BiMap<VillagerRegistry.VillagerProfession, Profession> professionMap = HashBiMap.create();

    public static void registerProfession(VillagerRegistry.VillagerProfession villagerProfession, Profession profession) {
        professionMap.put(villagerProfession, profession);
    }

    public static Optional<Profession> getProfession(VillagerRegistry.VillagerProfession profession) {
        return Optional.ofNullable(professionMap.get(profession));
    }

    public static Optional<VillagerRegistry.VillagerProfession> getProfession(Profession profession) {
        return Optional.ofNullable(professionMap.inverse().get(profession));
    }

}
