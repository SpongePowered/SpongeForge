package org.spongepowered.mod.mixin.core.client.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.entity.living.player.ClientPlayer;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.player.MixinEntityPlayer;
import org.spongepowered.common.util.SkinUtil;

import java.util.Optional;
import java.util.Set;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer implements ClientPlayer {

    @Shadow private NetworkPlayerInfo playerInfo;

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return SkinUtil.fromFlags(this.dataManager.get(EntityPlayer.PLAYER_MODEL_FLAG));
    }

    @Override
    public Optional<TabListEntry> getPlayerInfo() {
        return Optional.ofNullable((TabListEntry) this.playerInfo);
    }
}
