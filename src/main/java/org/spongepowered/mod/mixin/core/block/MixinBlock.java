package org.spongepowered.mod.mixin.core.block;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "removedByPlayer", at = @At("HEAD"))
    private void onRemovedByPlayerCheckWorldPhase()

}
