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

import com.google.common.collect.ImmutableSet;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTargetAlreadyLoadedException;
import org.spongepowered.asm.util.ConstraintParser.Constraint;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.launch.Main;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Error handler for Sponge mixins
 */
public class MixinErrorHandler implements IMixinErrorHandler {

    /**
     * Everyone wants a Log
     * You're gonna love it, Log!
     * Come on and get your Log
     * Everyone needs a Log
     * Log, Log, Log!
     */
    private final Logger log = LogManager.getLogger("Sponge");

    private PrettyPrinter forgeVersionNotValid(Constraint constraint) {
        String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        String forgeMessage = forgeVer == null ? String.valueOf(constraint.getMin()) : forgeVer;

        return new PrettyPrinter()
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
            .addWrapped("  The patch which failed requires Forge a build of %s but you are running build %d",
                    constraint.getRangeHumanReadable(), ForgeVersion.getBuildVersion());
    }

    private PrettyPrinter patchConstraintFailed(Constraint constraint, ConstraintViolationException ex) {
        return new PrettyPrinter().kvWidth(20)
            .add()
            .add("Oh dear. Sponge could not apply one or more patches. A constraint check failed!")
            .add()
            .hr('-')
            .add()
            .add("A patch constraint violation was encountered whilst patching:")
            .add()
            .kv("Constraint Name", constraint.getToken())
            .kv("Your value", ex.getBadValue())
            .kv("Allowed range", constraint.getRangeHumanReadable());
    }

    private PrettyPrinter badCoreMod(MixinTargetAlreadyLoadedException ex) {
        PrettyPrinter pp = new PrettyPrinter().kvWidth(20)
            .add()
            .add("Oh dear. Sponge could not apply one or more patches. A required class was loaded prematurely!")
            .add()
            .hr('-')
            .add()
            .add("An essential class was loaded before Sponge could patch it, this usually means")
            .add("that another coremod has caused the class to load prematurely.")
            .add()
            .kv("Class Name", ex.getTarget())
            .add();

        if (ex.getTarget().startsWith("net.minecraftforge")) {
            pp.hr('-').add().add("Loaded forge classes: ").add();
            for (String loadedClass : MixinErrorHandler.getLoadedClasses("net.minecraftforge")) {
                pp.add("    %s", loadedClass);
            }
        }

        return pp;
    }

    private PrettyPrinter itsAllGoneHorriblyWrong() {
        String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        if (forgeVer != null && !forgeVer.equals(ForgeVersion.getVersion())) {
            return new PrettyPrinter()
                .add()
                .add("Oh dear. It seems like this version of Sponge is not compatible with the version")
                .add("of Forge you are running.")
                .add()
                .hr('-')
                .add()
                .add("A error was encountered whilst patching:")
                .add()
                .add("  One or more Sponge patches could not be applied whilst loading Sponge, this is")
                .add("  a permanent error and you must either:")
                .add()
                .add("   * Use the correct build of Forge for this version of Sponge (%s)", forgeVer)
                .add()
                .add("   * Use a version of Sponge for built for your version of Forge")
                .add()
                .addWrapped("  The patch which failed requires Forge build: %s", forgeVer)
                .addWrapped("  but you are running build:                   %s", ForgeVersion.getVersion());
        }
        String forgeMessage = forgeVer == null ? "is usually specified in the sponge mod's jar filename" : "version is for " + forgeVer;

        return new PrettyPrinter()
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
        return errorPrinter.kvWidth(20).add()
            .hr('-')
            .add()
            .add("Technical details:")
            .add()
            .kv("Failed on class", targetClassName)
            .kv("During phase", mixin.getPhase())
            .kv("Mixin", mixin.getName())
            .kv("Config", mixin.getConfig().getName())
            .kv("Error Type", th.getClass().getName())
            .kv("Caused by", th.getCause() == null ? "Unknown" : th.getCause().getClass().getName())
            .kv("Message", th.getMessage())
            .add();
    }

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            PrettyPrinter errorPrinter = this.getPrettyPrinter(th);
            String targetClassName = "N/A";
            if (th instanceof MixinTargetAlreadyLoadedException) {
                targetClassName = ((MixinTargetAlreadyLoadedException) th).getTarget();
            }
            this.appendTechnicalInfo(errorPrinter, targetClassName, th, mixin).log(this.log);
            TerminateVM.terminate("net.minecraftforge.fml", -1);
        }
        return null;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            this.appendTechnicalInfo(this.getPrettyPrinter(th), targetClassName, th, mixin).log(this.log);
            TerminateVM.terminate("net.minecraftforge.fml", -1);
        }
        return null;
    }

    public PrettyPrinter getPrettyPrinter(Throwable th) {
        if (th.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) th.getCause();
            Constraint constraint = ex.getConstraint();
            if ("FORGE".equals(constraint.getToken())) {
                return this.forgeVersionNotValid(constraint);
            }
            return this.patchConstraintFailed(constraint, ex);
        } else if (th instanceof MixinTargetAlreadyLoadedException) {
            return this.badCoreMod((MixinTargetAlreadyLoadedException) th);
        }
        return this.itsAllGoneHorriblyWrong();
    }

    
    /**
     * Get the names of loaded classes from the cache, filter using the supplied
     * filter string
     * 
     * @param filter filter string or null
     * @return set of class names
     */
    private static Set<String> getLoadedClasses(String filter) {
        Map<String, Class<?>> cachedClasses = ReflectionHelper.<Map<String, Class<?>>, LaunchClassLoader>getPrivateValue(LaunchClassLoader.class,
                Launch.classLoader, "cachedClasses");
        
        if (cachedClasses == null) {
            return ImmutableSet.<String>of("Unable to determine classloader state");
        }
        
        Set<String> loadedClasses = new HashSet<String>();
        for (String className : cachedClasses.keySet()) {
            if (filter == null || className.startsWith(filter)) {
                loadedClasses.add(className);
            }
        }
        return loadedClasses;
    }

}
