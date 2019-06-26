package org.spongepowered.mod.bridge.world;

import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;

public interface WorldBridge_Forge {

    void forgeBridge$setRedirectedWorldInfo(@Nullable WorldInfo info);

}
