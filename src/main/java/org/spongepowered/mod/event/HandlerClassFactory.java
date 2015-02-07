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

package org.spongepowered.mod.event;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

class HandlerClassFactory implements HandlerFactory {

    private final AtomicInteger index = new AtomicInteger();
    private final LocalClassLoader classLoader = new LocalClassLoader(HandlerClassFactory.class.getClassLoader());
    private final String targetPackage;
    private final LoadingCache<CacheKey, Class<?>> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .weakValues()
            .build(
                    new CacheLoader<CacheKey, Class<?>>() {
                        @Override
                        public Class<?> load(CacheKey key) {
                            return createClass(key.type, key.method, key.ignoreCancelled);
                        }
                    });

    /**
     * Creates a new class factory.
     *
     * <p>Different instances of this class should use different packages.</p>
     *
     * @param targetPackage The target package
     */
    public HandlerClassFactory(String targetPackage) {
        checkNotNull(targetPackage, "targetPackage");
        this.targetPackage = targetPackage;
    }

    @Override
    public Handler createHandler(Object object, Method method, boolean ignoreCancelled) {
        synchronized (this.cache) {
            CacheKey key = new CacheKey(object.getClass(), method, ignoreCancelled);
            try {
                return (Handler) this.cache.getUnchecked(key)
                        .getConstructor(object.getClass(), Method.class)
                        .newInstance(object, method);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create a handler", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Handler> createClass(Class<?> type, Method method, boolean ignoreCancelled) {
        Class<?> eventClass = method.getParameterTypes()[0];
        String
                name =
                this.targetPackage + "." + eventClass.getSimpleName() + "Handler_" + type.getSimpleName() + "_" + method.getName() + this.index
                        .incrementAndGet();
        byte[] bytes = generateClass(type, method, eventClass, ignoreCancelled, name);
        return (Class<? extends Handler>) this.classLoader.defineClass(name, bytes);
    }

    public byte[] generateClass(Class<?> objectClass, Method method, Class<?> eventClass, boolean ignoreCancelled, String className) {
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
        FieldVisitor fv;
        MethodVisitor mv;

        String createdInternalName = className.replace(".", "/");
        String invokedInternalName = Type.getInternalName(objectClass);
        String eventInternalName = Type.getInternalName(eventClass);

        cw.visit(Opcodes.V1_6, ACC_PUBLIC + ACC_SUPER, createdInternalName, null, "java/lang/Object",
                 new String[]{Type.getInternalName(Handler.class)});

        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "object", "L" + invokedInternalName + ";", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "method", "Ljava/lang/reflect/Method;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(L" + invokedInternalName + ";Ljava/lang/reflect/Method;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, createdInternalName, "object", "L" + invokedInternalName + ";");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(PUTFIELD, createdInternalName, "method", "Ljava/lang/reflect/Method;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "handle", "(Lorg/spongepowered/api/util/event/Event;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "object", "L" + invokedInternalName + ";");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventInternalName);
            mv.visitMethodInsn(INVOKEVIRTUAL, "" + invokedInternalName + "", method.getName(), "(L" + eventInternalName + ";)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            Label l0 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l0);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 1);
            Label l1 = new Label();
            mv.visitJumpInsn(IFNULL, l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            Label l2 = new Label();
            mv.visitJumpInsn(IF_ACMPEQ, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, createdInternalName);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "method", "Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "method", "Ljava/lang/reflect/Method;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "equals", "(Ljava/lang/Object;)Z", false);
            Label l3 = new Label();
            mv.visitJumpInsn(IFNE, l3);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l3);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "object", "L" + invokedInternalName + ";");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "object", "L" + invokedInternalName + ";");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
            Label l4 = new Label();
            mv.visitJumpInsn(IFNE, l4);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l4);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "object", "L" + invokedInternalName + ";");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
            mv.visitVarInsn(ISTORE, 1);
            mv.visitIntInsn(BIPUSH, 31);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IMUL);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, createdInternalName, "method", "Ljava/lang/reflect/Method;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "hashCode", "()I", false);
            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, 1);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static class LocalClassLoader extends ClassLoader {

        public LocalClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    private static class CacheKey {

        private final Class<?> type;
        private final Method method;
        private final boolean ignoreCancelled;

        private CacheKey(Class<?> type, Method method, boolean ignoreCancelled) {
            this.type = type;
            this.method = method;
            this.ignoreCancelled = ignoreCancelled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            if (this.ignoreCancelled != cacheKey.ignoreCancelled) {
                return false;
            }
            if (!this.method.equals(cacheKey.method)) {
                return false;
            }
            if (!this.type.equals(cacheKey.type)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.type.hashCode();
            result = 31 * result + this.method.hashCode();
            result = 31 * result + (this.ignoreCancelled ? 1 : 0);
            return result;
        }
    }

}
