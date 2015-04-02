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
package org.spongepowered.granite.event;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.util.event.Event;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

class EventHandlerClassFactory implements EventHandlerFactory {

    private final String targetPackage;
    private final AtomicInteger id = new AtomicInteger();
    private final LocalClassLoader classLoader = new LocalClassLoader(getClass().getClassLoader());
    private final LoadingCache<Handler, Class<? extends EventHandler>> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .weakValues()
            .build(new CacheLoader<Handler, Class<? extends EventHandler>>() {
                @Override
                public Class<? extends EventHandler> load(Handler key) {
                    return createClass(key.type, key.method);
                }
            });

    public EventHandlerClassFactory(String targetPackage) {
        checkNotNull(targetPackage, "targetPackage");
        if (!targetPackage.isEmpty()) {
            targetPackage += '.';
        }
        this.targetPackage = targetPackage;
    }

    @Override
    public EventHandler get(Object handle, Method method) throws Exception {
        return this.cache.get(new Handler(handle.getClass(), method))
                .getConstructor(handle.getClass())
                .newInstance(handle);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends EventHandler> createClass(Class<?> handle, Method method) {
        Class<?> eventClass = method.getParameterTypes()[0];
        String name =
                this.targetPackage + eventClass.getSimpleName() + "Handler_" + handle.getSimpleName() + '_' + method.getName() + this.id
                        .incrementAndGet();
        return (Class<? extends EventHandler>) this.classLoader.defineClass(name, generateClass(name, handle, method, eventClass));
    }

    private static final String EVENT_HANDLER_CLASS = Type.getInternalName(EventHandler.class);
    private static final String EVENT_HANDLER_METHOD = '(' + Type.getDescriptor(Event.class) + ")V";

    private static byte[] generateClass(String name, Class<?> handle, Method method, Class<?> eventClass) {
        name = name.replace('.', '/');

        final String handleName = Type.getInternalName(handle);
        final String handleDescriptor = Type.getDescriptor(handle);
        final String eventName = Type.getInternalName(eventClass);

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, "java/lang/Object", new String[]{EVENT_HANDLER_CLASS});

        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "handle", handleDescriptor, null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", '(' + handleDescriptor + ")V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, name, "handle", handleDescriptor);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getHandle", "()Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "handle", handleDescriptor);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "handle", EVENT_HANDLER_METHOD, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "handle", handleDescriptor);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventName);
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.getName(), "(L" + eventName + ";)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static class Handler {

        private final Class<?> type;
        private final Method method;

        private Handler(Class<?> type, Method method) {
            this.type = checkNotNull(type, "type");
            this.method = checkNotNull(method, "method");
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Handler handler = (Handler) o;

            return this.type.equals(handler.type)
                    && this.method.equals(handler.method);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.type, this.method);
        }

    }

    private static class LocalClassLoader extends ClassLoader {

        public LocalClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }

    }

}
