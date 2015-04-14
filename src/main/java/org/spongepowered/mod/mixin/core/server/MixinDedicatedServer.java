package org.spongepowered.mod.mixin.core.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer {

    @Shadow public boolean guiIsEnabled;

    public MixinDedicatedServer(Proxy proxy, File workDir) {
        super(proxy, workDir);
    }

    /**
     * @author Zidane
     *
     * Purpose: At the time of writing, this turns off the default Minecraft Server GUI that exists in non-headless environment.
     * Reasoning: The GUI console can easily consume a sizable chunk of each CPU core (20% or more is common) on the computer being ran on and has
     * been proven to cause quite a bit of latency issues.
     */
    @Overwrite
    public void setGuiEnabled()
    {
        //MinecraftServerGui.createServerGui(this);
        this.guiIsEnabled = true;
    }

}
