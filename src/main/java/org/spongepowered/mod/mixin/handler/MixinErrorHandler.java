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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTargetAlreadyLoadedException;
import org.spongepowered.asm.util.ConstraintParser;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.mixin.handler.TerminateVM;
import org.spongepowered.launch.Main;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Error handler for Sponge mixins
 */
@SuppressWarnings({"deprecation", "unused"}) // This class is registered in SpongeCoremod
public class MixinErrorHandler implements IMixinErrorHandler {

    /**
     * Everyone wants a Log
     * You're gonna love it, Log!
     * Come on and get your Log
     * Everyone needs a Log
     * Log, Log, Log!
     */
    private final Logger log = LogManager.getLogger("Sponge");

    private PrettyPrinter forgeVersionNotValid(final ConstraintParser.Constraint constraint) {
        final String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        final String forgeMessage = forgeVer == null ? String.valueOf(constraint.getMin()) : forgeVer;

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

    private PrettyPrinter patchConstraintFailed(final ConstraintParser.Constraint constraint, final ConstraintViolationException ex) {
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

    private PrettyPrinter badCoreMod(final MixinTargetAlreadyLoadedException ex) {
        final PrettyPrinter pp = new PrettyPrinter().kvWidth(20)
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
            for (final String loadedClass : MixinErrorHandler.getLoadedClasses("net.minecraftforge")) {
                pp.add("    %s", loadedClass);
            }
        }

        return pp;
    }

    private PrettyPrinter itsAllGoneHorriblyWrong() {
        final String forgeVer = Main.getManifestAttribute("TargetForgeVersion", null);
        final String forgeMessage = forgeVer == null ? "is usually specified in the sponge mod's jar filename" : "version is for " + forgeVer;
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
            .addWrapped("     correct version of Forge when running Sponge, this %s or later (you are running %s)", forgeMessage, ForgeVersion.getVersion())
            .add()
            .add("   * An error exists in Sponge itself. Ensure you are running the latest version")
            .add("     of Sponge.")
            .add()
            .add("   * Gremlins are invading your computer. Did you feed a Mogwai after midnight?");
    }

    private PrettyPrinter appendTechnicalInfo(final PrettyPrinter errorPrinter, final String targetClassName, final Throwable th, final IMixinInfo mixin) {
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
    public ErrorAction onPrepareError(final IMixinConfig config, final Throwable th, final IMixinInfo mixin, final ErrorAction action) {
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            final PrettyPrinter errorPrinter = this.getPrettyPrinter(th);
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
    public ErrorAction onApplyError(final String targetClassName, final Throwable th, final IMixinInfo mixin, final ErrorAction action) {
        if ("net.minecraft.util.math.BlockPos$MutableBlockPos".equals(targetClassName)) {
            new PrettyPrinter(60).add("!!! FoamFix Incompatibility !!!").centre().hr()
                .addWrapped("Hello! You are running SpongeForge and \"likely\" FoamFix on the same server, and we've discoverd"
                            + " a missing field that would otherwise cause some of Sponge not to work, because foamfix removes "
                            + "that field. As the issue stands, it's not possible to \"patch fix\", but we can suggest the "
                            + "configuration option change in foamfix's config to allow your game to start! Please change the "
                            + "following options in foamfix'es config.")
                .add()
                .add("In config/foamfix.cfg, change these values: ")
                .add("B:optimizedBlockPos=false")
                .add("B:patchChunkSerialization=false")
                .add()
                .addWrapped("We at Sponge appreciate your patience as this can be frustrating when the game doesn't start "
                            + "right away, or that SpongeForge isn't an easy drop-in-and-get-running sometimes. Thank you "
                            + "for your consideration, and have a nice day!")
                .add()
                .add(new IncompatibleClassChangeError("FoamFix Incompatibility Detected"))
                .log(SpongeImpl.getLogger(), Level.FATAL);
            TerminateVM.terminate("net.minecraftforge.fml", 1);
        }
        if (action == ErrorAction.ERROR && mixin.getConfig().getMixinPackage().startsWith("org.spongepowered.")) {
            this.appendTechnicalInfo(this.getPrettyPrinter(th), targetClassName, th, mixin).log(this.log);
            TerminateVM.terminate("net.minecraftforge.fml", -1);
        }

        return null;
    }

    public PrettyPrinter getPrettyPrinter(final Throwable th) {
        if (th.getCause() instanceof ConstraintViolationException) {
            final ConstraintViolationException ex = (ConstraintViolationException) th.getCause();
            final ConstraintParser.Constraint constraint = ex.getConstraint();
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
    private static Set<String> getLoadedClasses(final String filter) {
        final Map<String, Class<?>> cachedClasses = net.minecraftforge.fml.relauncher.ReflectionHelper.<Map<String, Class<?>>, LaunchClassLoader>getPrivateValue(LaunchClassLoader.class,
                Launch.classLoader, "cachedClasses");
        
        if (cachedClasses == null) {
            return ImmutableSet.<String>of("Unable to determine classloader state");
        }
        
        final Set<String> loadedClasses = new HashSet<String>();
        for (final String className : cachedClasses.keySet()) {
            if (filter == null || className.startsWith(filter)) {
                loadedClasses.add(className);
            }
        }
        return loadedClasses;
    }

}
