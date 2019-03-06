package org.spongepowered.mod.mixin.core.world;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;

/**
 * Created to be able to override Forge specific injections that we only want
 * to have for the WorldServer.
 */
@Mixin(value = WorldServer.class, priority = 1010)
public abstract class MixinWorldServer_Forge extends MixinWorld {

    @Override
    protected void onUpdateComparatorDuringTileRemoval(World world, BlockPos pos, Block blockIn, BlockPos samePos) {
        if (!this.isFake()) {
            if (PhaseTracker.getInstance().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
                return;
            }
        }
        this.updateComparatorOutputLevel(pos, blockIn);
    }
}
