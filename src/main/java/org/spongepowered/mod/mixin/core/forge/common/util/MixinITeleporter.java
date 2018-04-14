package org.spongepowered.mod.mixin.core.forge.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;

@Mixin(value = ITeleporter.class, remap = false)
@Implements(@Interface(iface = IMixinTeleporter.class, prefix = "sponge$"))
public interface MixinITeleporter {

    @Shadow public abstract void placeEntity(World world, Entity entity, float yaw);

    @Intrinsic
    default void sponge$placeEntity(World world, Entity entity, float yaw) {
        placeEntity(world, entity, yaw);
    }

    @Intrinsic
    default boolean sponge$isVanilla()
    {
        return getClass().equals(Teleporter.class);
    }

}
