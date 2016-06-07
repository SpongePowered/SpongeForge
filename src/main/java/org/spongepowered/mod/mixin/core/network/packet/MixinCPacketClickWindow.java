package org.spongepowered.mod.mixin.core.network.packet;

import com.google.common.base.Objects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CPacketClickWindow.class)
public class MixinCPacketClickWindow {

    @Shadow private int windowId;
    @Shadow private int slotId;
    @Shadow private int usedButton;
    @Shadow private short actionNumber;
    @Shadow private ItemStack clickedItem;
    @Shadow private ClickType mode;


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("windowId", this.windowId)
                .add("slotId", this.slotId)
                .add("usedButton", this.usedButton)
                .add("actionNumber", this.actionNumber)
                .add("clickedItem", this.clickedItem)
                .add("mode", this.mode)
                .toString();
    }
}
