package org.spongepowered.mod.mixin.core.common;

import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Sponge.class, remap = false)
public abstract class MixinSponge {

    /**
     * @author unknown
     * @reason Forge compatibility
     */
    @Overwrite
    public static boolean isVanilla() {
        return false;
    }

}
