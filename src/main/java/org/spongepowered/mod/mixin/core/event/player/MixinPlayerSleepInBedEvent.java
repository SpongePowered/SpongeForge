package org.spongepowered.mod.mixin.core.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerSleepInBedEvent.class, remap = false)
public abstract class MixinPlayerSleepInBedEvent extends MixinEventPlayer implements SleepingEvent.Pre {

    @Shadow public final BlockPos pos = null;
    private BlockSnapshot bed;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(EntityPlayer player, BlockPos pos, CallbackInfo ci) {
        this.bed = ((World) player.worldObj).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
    }
    
    @Override
    public BlockSnapshot getBed() {
        return this.bed;
    }

}
