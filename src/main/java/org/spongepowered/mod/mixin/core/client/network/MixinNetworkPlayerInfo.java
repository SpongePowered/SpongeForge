package org.spongepowered.mod.mixin.core.client.network;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.mod.client.interfaces.IMixinNetworkPlayerInfo;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(NetworkPlayerInfo.class)
public class MixinNetworkPlayerInfo implements TabListEntry, IMixinNetworkPlayerInfo {

    @Shadow @Final private com.mojang.authlib.GameProfile gameProfile;

    @Shadow @Nullable private ITextComponent displayName;
    @Shadow private int responseTime;
    @Shadow private GameType gameType;

    private TabList tabList;

    @Override
    public void setTabList(TabList tabList) {
        this.tabList = tabList;
    }

    @Override
    public TabList getList() {
        return this.tabList;
    }

    @Override
    public GameProfile getProfile() {
        return (GameProfile) this.gameProfile;
    }

    @Override
    public Optional<Text> getDisplayName() {
        if (this.displayName == null) {
            return Optional.empty();
        }
        return Optional.of(SpongeTexts.toText(this.displayName));
    }

    @Override
    public TabListEntry setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName == null ? null : SpongeTexts.toComponent(displayName);
        return this;
    }

    @Override
    public int getLatency() {
        return this.responseTime;
    }

    @Override
    public TabListEntry setLatency(int latency) {
        this.responseTime = latency;
        return this;
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.gameType;
    }

    @Override
    public TabListEntry setGameMode(GameMode gameMode) {
        this.gameType = (GameType) (Object) gameMode;
        return this;
    }
}
