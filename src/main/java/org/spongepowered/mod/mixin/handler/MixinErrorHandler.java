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

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.ConstraintParser.Constraint;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.launch.Main;

import javax.annotation.Nullable;

/**
 * Error handler for Sponge mixins
 */
public class MixinErrorHandler implements IMixinErrorHandler {

    /**
     * Captain's log, stardate 68788.5
     */
    private final Logger log = LogManager.getLogger("Sponge");

    private PrettyPrinter forgeVersionNotValid(PrettyPrinter errorPrinter, Constraint constraint) {
        String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        String forgeMessage = forgeVer == null ? String.valueOf(constraint.getMin()) : forgeVer;

        return errorPrinter
            .add()
            .add("Oh dear. It seems like this version of Sponge is not compatible with the version")
            .add("of Forge you are running.")
            .add()
            .hr('-')
            .add()
            .add("A patch constraint violation was encountered whilst patching:")
            .add()
            .add("  One or more Sponge patches could not be applied whilst loading Sponge, this is")
            .add("  a permanent error and you must either:")
            .add()
            .add("   * Use the correct build of Forge for this version of Sponge (%s)", forgeMessage)
            .add()
            .add("   * Use a version of Sponge for built for your version of Forge")
            .add()
            .addWrapped("  The patch which failed requires Forge a build of %s but you are running build %d", constraint.getRangeHumanReadable(), ForgeVersion.getBuildVersion());
    }

    private PrettyPrinter patchConstraintFailed(PrettyPrinter errorPrinter, Constraint constraint, ConstraintViolationException ex) {
        return errorPrinter
            .add()
            .add("Oh dear. Sponge could not apply one or more patches. A constraint check failed!")
            .add()
            .hr('-')
            .add()
            .add("A patch constraint violation was encountered whilst patching:")
            .add()
            .add("%20s : %s", "Constraint Name", constraint.getToken())
            .add("%20s : %s", "Your value", ex.getBadValue())
            .add("%20s : %s", "Allowed range", constraint.getRangeHumanReadable());
    }

    private PrettyPrinter itsAllGoneHorriblyWrong(PrettyPrinter errorPrinter) {
        String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        String forgeMessage = forgeVer == null ? "is usually specified in the sponge mod's jar filename" : "version is for " + forgeVer;

        return errorPrinter
            .add()
            .add("Oh dear. Something went wrong and the server had to shut down!")
            .add()
            .hr('-')
            .add()
            .add("A critical error was encountered while blending Sponge with Forge!")
            .add()
            .add("  Possible causes are:")
            .add()
            .add("   * An incompatible Forge \"core mod\" is present. Try removing other mods to")
            .add("     see if the problem goes away.")
            .add()
            .add("   * You are using the wrong version of Minecraft Forge. You must use the")
            .addWrapped("     correct version of Forge when running Sponge, this %s (you are running %s)", forgeMessage, ForgeVersion.getVersion())
            .add()
            .add("   * An error exists in Sponge itself. Ensure you are running the latest version")
            .add("     of Sponge.")
            .add()
            .add("   * Gremlins are invading your computer. Did you feed a Mogwai after midnight?");
    }

    private PrettyPrinter appendTechnicalInfo(PrettyPrinter errorPrinter, String targetClassName, Throwable th, IMixinInfo mixin) {
        return errorPrinter.add()
            .hr('-')
            .add()
            .add("Technical details:")
            .add()
            .add("%20s : %s", "Failed on class", targetClassName)
            .add("%20s : %s", "During phase", mixin.getPhase())
            .add("%20s : %s", "Mixin", mixin.getName())
            .add("%20s : %s", "Config", mixin.getConfig().getName())
            .add("%20s : %s", "Error Type", th.getClass().getName())
            .add("%20s : %s", "Caused by", th.getCause() == null ? "Unknown" : th.getCause().getClass().getName())
            .add("%20s : %s", "Message", th.getMessage())
            .add();
    }

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            PrettyPrinter errorPrinter = new PrettyPrinter();

            errorPrinter = getPrettyPrinter(th, errorPrinter);

            this.appendTechnicalInfo(errorPrinter, "N/A", th, mixin).log(this.log);

            FMLCommonHandler.instance().exitJava(1, true);
        }
        return null;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            PrettyPrinter errorPrinter = new PrettyPrinter();

            errorPrinter = getPrettyPrinter(th, errorPrinter);

            this.appendTechnicalInfo(errorPrinter, targetClassName, th, mixin).log(this.log);

            FMLCommonHandler.instance().exitJava(1, true);
        }
        return null;
    }

    public PrettyPrinter getPrettyPrinter(Throwable th, PrettyPrinter errorPrinter) {
        if (th.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) th.getCause();
            Constraint constraint = ex.getConstraint();
            if ("FORGE".equals(constraint.getToken())) {
                errorPrinter = this.forgeVersionNotValid(errorPrinter, constraint);
            } else {
                errorPrinter = this.patchConstraintFailed(errorPrinter, constraint, ex);
            }
        } else {
            errorPrinter = this.itsAllGoneHorriblyWrong(errorPrinter);
        }
        return errorPrinter;
    }
}
