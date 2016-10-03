package org.spongepowered.mod.mixin.core.forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.mod.util.StaticMixinForgeHelper;

@Mixin(ForgeInternalHandler.class)
public abstract class MixinForgeInternalHandler {

    @Redirect(method = "onEntityJoinWorld", at = @At(value = "INVOKE", target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z", ordinal = 0))
    public boolean onHandleCustomItemEntity(Object clazz, Object otherClass, EntityJoinWorldEvent event) {
        boolean equals = clazz.equals(otherClass);
        // Prevent the if block from running if Sponge is handling it
        if (equals && StaticMixinForgeHelper.preventInternalForgeEntityListener) {
            // Handle possible re-entrant firing of spawn events. We only want to bypassthe normal logic for the first,
            // Sponge-fired one - not any events fired by Forge mods.
            StaticMixinForgeHelper.preventInternalForgeEntityListener = false;
            return false;
        }
        return equals;
    }

}
