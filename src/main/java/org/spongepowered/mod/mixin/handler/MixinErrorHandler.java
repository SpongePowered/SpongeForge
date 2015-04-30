/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.handler;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.launch.Main;

/**
 * Error handler for Sponge mixins
 */
public class MixinErrorHandler implements IMixinErrorHandler {

    /**
     * Captain's log, stardate 68788.5
     */
    private final Logger log = LogManager.getLogger("Sponge");

    @Override
    public ErrorAction onError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
            String forgeMessage = forgeVer == null ? "is usually specified in the sponge mod's jar filename" : "version is for Forge " + forgeVer;
            
            new PrettyPrinter()
                .add()
                .add("Oh dear. Something went wrong and the server had to shut down!")
                .add()
                .hr('-')
                .add()
                .add("A critical error was encountered while blending Sponge with Forge!")
                .add()
                .add("  Possible causes are:")
                .add()
                .add("   * An incompatible Forge \"core mod\" is present. Try removing other mods to see if the problem")
                .add("     goes away.")
                .add()
                .add("   * You are using the wrong version of Minecraft Forge. You must use the correct version of Forge")
                .add("     when running Sponge, this %s.", forgeMessage)
                .add()
                .add("   * An error exists in Sponge itself. Ensure you are running the latest version of Sponge.")
                .add()
                .add("   * Gremlins are invading your computer. Did you feed a Mogwai after midnight?")
                .add()
                .hr('-')
                .add("Technical details:")
                .add()
                .add("%20s : %s", "Failed on class", targetClassName)
                .add("%20s : %s", "During phase", mixin.getPhase())
                .add("%20s : %s", "Mixin", mixin.getName())
                .add("%20s : %s", "Config", mixin.getConfig().getName())
                .add("%20s : %s", "Error Type", th.getClass().getName())
                .add("%20s : %s", "Message", th.getMessage())
                .add()
                .log(this.log);
            
            FMLCommonHandler.instance().exitJava(1, false);
        }
        
        return null;
    }

}
