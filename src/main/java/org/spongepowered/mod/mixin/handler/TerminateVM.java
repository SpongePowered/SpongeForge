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

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.Opcodes;

import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Really wish this wasn't necessary but unfortunately FML doesn't have any
 * mechanism to shut down the VM when a fatal error occurs.
 */
public final class TerminateVM implements IExitHandler {
    
    static class MasqueradeClassLoader extends ClassLoader {

        final LaunchClassLoader parent;
        final String className, classRef;

        MasqueradeClassLoader(LaunchClassLoader parent, String masqueradePackage) {
            super(parent);
            this.parent = parent;
            this.className = masqueradePackage + ".TerminateVM";
            this.classRef = this.className.replace('.', '/');
        }
        
        String getClassName() {
            return this.className;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                if (this.className.equals(name)) {
                    ClassWriter cw = new ClassWriter(0);
                    ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            super.visit(version, access | Opcodes.ACC_PUBLIC, MasqueradeClassLoader.this.classRef, signature, superName, interfaces);
                        }
                        
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                            if ("terminate".equals(name)) {
                                return null;
                            }
                            MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
                            return new MethodVisitor(Opcodes.ASM5, mv) {
                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                                    if (owner.endsWith("TerminateVM")) {
                                        owner = MasqueradeClassLoader.this.classRef;
                                        if ("systemExit".equals(name)) {
                                            owner = System.class.getName().replace('.', '/');
                                            name = "exit";
                                        }
                                    }
                                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                                }
                            };
                        }
                    };
                    new ClassReader(this.parent.getClassBytes(TerminateVM.class.getName())).accept(cv, 0);
                    byte[] classBytes = cw.toByteArray();
                    return this.defineClass(this.className, classBytes, 0, classBytes.length, null);
                }
            } catch (IOException ex) {
                // sad face
            }
            
            return this.parent.findClass(name);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void terminate(String masqueradePackage, int status) {
        final Logger log = LogManager.getLogger("Sponge");
        
        try {
            log.info("Attempting to shut down the VM cleanly");
            FMLCommonHandler.instance().exitJava(status, true);
        } catch (Throwable th) {
            log.info("Clean shutdown failed, forcing VM termination");
        }

        IExitHandler handler = null;
        try {
            MasqueradeClassLoader cl = new MasqueradeClassLoader(Launch.classLoader, masqueradePackage);
            Constructor<IExitHandler> ctor = ((Class<IExitHandler>) Class.forName(cl.getClassName(), true, cl)).getDeclaredConstructor();
            ctor.setAccessible(true);
            handler = ctor.newInstance();
        } catch (Throwable th) {
            log.catching(th);
            handler = new TerminateVM();
        }
        handler.exit(status);
    }

    @Override
    public void exit(int status) {
        TerminateVM.systemExit(status);
    }

    private static void systemExit(int status) {
        throw new IllegalStateException("Not transformed");
    }
}
