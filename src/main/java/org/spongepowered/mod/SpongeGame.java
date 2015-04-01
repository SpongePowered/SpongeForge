/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.mod;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.service.scheduler.AsyncScheduler;
import org.spongepowered.mod.service.scheduler.SyncScheduler;

import com.google.common.base.Optional;

@NonnullByDefault
public final class SpongeGame implements Game {

    @Nullable private static final String apiVersion = Game.class.getPackage().getImplementationVersion();
    @Nullable private static final String implementationVersion = SpongeGame.class.getPackage().getImplementationVersion();

    private static final MinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion("1.8", 47); // TODO:
                                                                                                     // Keep
                                                                                                     // updated

    private final PluginManager pluginManager;
    private final EventManager eventManager;
    private final GameRegistry gameRegistry;
    private final ServiceManager serviceManager;

    @Inject
    public SpongeGame(PluginManager plugin, EventManager event, GameRegistry registry, ServiceManager service) {
        this.pluginManager = plugin;
        this.eventManager = event;
        this.gameRegistry = registry;
        this.serviceManager = service;
    }

    @Override
    public Platform getPlatform() {
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT :
                return Platform.CLIENT;
            default :
                return Platform.SERVER;
        }
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public String getApiVersion() {
        return apiVersion != null ? apiVersion : "UNKNOWN";
    }

    @Override
    public String getImplementationVersion() {
        return implementationVersion != null ? implementationVersion : "UNKNOWN";
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return MINECRAFT_VERSION;
    }

    @Override
    public GameRegistry getRegistry() {
        return this.gameRegistry;
    }

    @Override
    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    @Override
    public SynchronousScheduler getSyncScheduler() {
        return SyncScheduler.getInstance();
    }

    @Override
    public AsynchronousScheduler getAsyncScheduler() {
        return AsyncScheduler.getInstance();
    }

    @Override
    public CommandService getCommandDispatcher() {
        return this.serviceManager.provideUnchecked(CommandService.class);
    }

    @Override
    public Optional<Server> getServer() {
        return Optional.fromNullable((Server) FMLCommonHandler.instance().getMinecraftServerInstance());
    }

    @Override
    public boolean isFlowerPot() {
        return new FlowerPotCheckBuilder().isFlowerPot();
    }

    private static class FlowerPotCheckBuilder implements Opcodes, Checker {

        FlowerPotClassLoader cl = new FlowerPotClassLoader();
        Class<?> cls;

        public FlowerPotCheckBuilder() {
            try {
                this.cls = this.cl.defineClass("org.spongepowered.mod.SpongeGame$FlowerPotChecker", build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isFlowerPot() {
            try {
                return ((Checker) cls.newInstance()).isFlowerPot();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }

        public byte[] build() throws Exception {

            ClassWriter cw = new ClassWriter(0);
            FieldVisitor fv;
            MethodVisitor mv;
            AnnotationVisitor av0;

            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/spongepowered/mod/SpongeGame$FlowerPotChecker", null, "java/lang/Object",
                    new String[] { "org/spongepowered/mod/SpongeGame$Checker" });

            cw.visitSource("SpongeGame.java", null);

            cw.visitInnerClass("org/spongepowered/mod/SpongeGame$Checker", "org/spongepowered/mod/SpongeGame", "Checker", ACC_STATIC + ACC_ABSTRACT
                    + ACC_INTERFACE);

            cw.visitInnerClass("org/spongepowered/mod/SpongeGame$FlowerPotChecker", "org/spongepowered/mod/SpongeGame", "FlowerPotChecker", 0);

            {
                fv = cw.visitField(ACC_FINAL + ACC_SYNTHETIC, "this$0", "Lorg/spongepowered/mod/SpongeGame;", null, null);
                fv.visitEnd();
            }
            {
                mv = cw.visitMethod(0, "<init>", "(Lorg/spongepowered/mod/SpongeGame;)V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(245, l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, "org/spongepowered/mod/SpongeGame$FlowerPotChecker", "this$0", "Lorg/spongepowered/mod/SpongeGame;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLocalVariable("this", "Lorg/spongepowered/mod/SpongeGame$FlowerPotChecker;", null, l0, l1, 0);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_SYNCHRONIZED, "isFlowerPot", "()Z", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(249, l0);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(IRETURN);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLocalVariable("this", "Lorg/spongepowered/mod/SpongeGame$FlowerPotChecker;", null, l0, l1, 0);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            cw.visitEnd();

            return cw.toByteArray();
        }

        class FlowerPotClassLoader extends ClassLoader {

            public Class<?> defineClass(String name, byte[] cls) {
                return defineClass(name, cls, 0, cls.length);
            }
        }

    }

    public static interface Checker {

        boolean isFlowerPot();
    }
}
