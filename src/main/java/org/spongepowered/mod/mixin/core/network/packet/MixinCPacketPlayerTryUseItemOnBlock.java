package org.spongepowered.mod.mixin.core.network.packet;

import com.google.common.base.Objects;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CPacketPlayerTryUseItemOnBlock.class)
public class MixinCPacketPlayerTryUseItemOnBlock {
    @Shadow private BlockPos position;
    @Shadow private EnumFacing placedBlockDirection;
    @Shadow private EnumHand hand;
    @Shadow private float facingX;
    @Shadow private float facingY;
    @Shadow private float facingZ;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("position", this.position)
                .add("placedBlockDirection", this.placedBlockDirection)
                .add("hand", this.hand)
                .add("facingX", this.facingX)
                .add("facingY", this.facingY)
                .add("facingZ", this.facingZ)
                .toString();
    }
}
