/**
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
package org.spongepowered.mod.asm.util;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.event.EventListenerHolder;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;

public class ASMEventListenerHolderFactory {

    private static volatile ClassLoaderAccess loaderAccess = null;
    private static final Map<HashTriple, Class<?>> cache = new ConcurrentHashMap<HashTriple, Class<?>>();
    private static final Object classCreationLock = new Object();

    private static int classId = 0;

    @SuppressWarnings("unchecked")
    public static EventListenerHolder<Event> getNewEventListenerHolder(Class<?> eventClass, EventPriority priority, boolean canceled) {
        HashTriple t = new HashTriple(eventClass, priority, canceled);

        Class<?> clazz = cache.get(t);
        if (clazz == null) {
            synchronized (classCreationLock) {
                clazz = cache.get(t);
                if (clazz == null) {
                    clazz = getClass(eventClass, priority, canceled);
                    if (clazz != null) {
                        cache.put(t, clazz);
                    }
                }
            }
        }
        try {
            return (EventListenerHolder<Event>) clazz.newInstance();
        } catch (InstantiationException e) {
            ;
        } catch (IllegalAccessException e) {
            ;
        }
        return null;
    }

    public static Class<?> getClass(Class<?> eventClass, EventPriority priority, boolean canceled) {
        ClassWriter cwRaw = new ClassWriter(0);
        CheckClassAdapter cw = new CheckClassAdapter(cwRaw);

        MethodVisitor mv;
        AnnotationVisitor av0;

        String className = getClassName(eventClass, priority, canceled);
        String classNameInternal = className.replace('.', '/');

        String invokeMethodDesc = "(" + Type.getDescriptor(eventClass) + ")V";

        String eventPriorityName = priority.name();

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, classNameInternal, null, "org/spongepowered/mod/event/EventListenerHolder", null);

        cw.visitInnerClass("net/minecraftforge/event/world/BlockEvent$BreakEvent", "net/minecraftforge/event/world/BlockEvent", "BreakEvent", 
                ACC_PUBLIC + ACC_STATIC);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/mod/event/EventListenerHolder", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "invoke", invokeMethodDesc, null, null);
            {
                av0 = mv.visitAnnotation("Lcpw/mods/fml/common/eventhandler/SubscribeEvent;", true);
                av0.visitEnum("priority", "Lcpw/mods/fml/common/eventhandler/EventPriority;", eventPriorityName);
                av0.visit("receiveCanceled", (Boolean) canceled);
                av0.visitEnd();
            }
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/mod/event/EventListenerHolder", "invoke", "(Ljava/lang/Object;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        if (loaderAccess == null) {
            loaderAccess = new ClassLoaderAccess();
        }
        return loaderAccess.defineClass(className, cwRaw.toByteArray());
    }

    private static class HashTriple {

        private final Class<?> eventClass;
        private final EventPriority priority;
        private final Boolean canceled;
        private final int hashCode;

        public HashTriple(Class<?> eventClass, EventPriority priority, boolean canceled) {
            this.eventClass = eventClass;
            this.priority = priority;
            this.canceled = canceled;
            this.hashCode = eventClass.hashCode() + priority.hashCode() + this.canceled.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof HashTriple)) {
                return false;
            } else {
                HashTriple t = (HashTriple) o;
                if (t.hashCode != hashCode) {
                    return false;
                }
                return t.eventClass.equals(eventClass) && t.priority.equals(priority) && t.canceled.equals(canceled);
            }
        }
    }

    private static String getClassName(Class<?> eventClass, EventPriority priority, boolean canceled) {
        int id = classId++;
        String prefix = ASMEventListenerHolderFactory.class.getName();
        String eventClassName = eventClass.getSimpleName();
        String priorityString = priority.name();
        String canceledString = canceled ? "Cancel" : "NoCancel";
        return prefix + "_" + id + "_" + eventClassName + "_" + priorityString + "_" + canceledString;
    }

    private static class ClassLoaderAccess {
        private final Method defineClassMethod;
        private final ClassLoader loader;

        public ClassLoaderAccess() {
            loader = SpongeMod.instance.getClass().getClassLoader();
            Method m = null;
            try {
                Class<?> clazz = loader.getClass();
                while (clazz != null) {
                    try {
                        m = clazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                        m.setAccessible(true);
                        break;
                    } catch (NoSuchMethodException e) {
                        clazz = clazz.getSuperclass();
                        continue;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            defineClassMethod = m;
        }

        public Class<?> defineClass(String className, byte[] b) {
            try {
                return (Class<?>) defineClassMethod.invoke(this.loader, className, b, 0, b.length);
            } catch (IllegalAccessException e) {
                ;
            } catch (IllegalArgumentException e) {
                ;
            } catch (InvocationTargetException e) {
                ;
            }
            return null;
        }
    }
}