package org.spongepowered.mod.client.interfaces;

import org.spongepowered.api.entity.living.player.tab.TabList;

public interface IMixinNetworkPlayerInfo {

    void setTabList(TabList tabList);
}
