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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

public class ASMEventListenerFactory {
    
    private static final ASMFactoryClassLoader loader = new ASMFactoryClassLoader();
    private static final Map<HashTriple, Class<?>> cache = new ConcurrentHashMap<HashTriple, Class<?>>();
    private static final Object classCreationLock = new Object();
    
    private static int classId = 0;
    
    private ASMEventListenerFactory() {
    }
    
    public static <T> T getListener(Class<T> interf, Object target, Method output) {
        Method[] methods = interf.getMethods();
        if (methods.length != 1) {
            throw new IllegalArgumentException("Method to call must be given for interfaces with more than 1 method");
        }
        return getListener(interf, methods[0], target, output);
    }
    
    /**
     * Creates a listener that implements a given interface, to pass an event to.  
     * A new class is created for each call to this method, unless the required class
     * is in the cache.
     * 
     * @param interf the interface implemented by the listener for receiving the event
     * @param input the invoke method, from interf, that the event should be passed to
     * @param target the object which supports the output method that receives the event
     * @param output the method, from target, that the listener should pass the event to
     * @param <T> the interface implemented by the listener
     * @return the listener
     */
    @SuppressWarnings("unchecked")
    public static <T> T getListener(Class<T> interf, Method input, Object target, Method output) {
        if (!interf.isInterface()) {
            throw new IllegalArgumentException(interf + " is not an interface");
        }
        
        HashTriple t = new HashTriple(interf, input, output);

        try {
            Class<?> clazz = cache.get(t);
            if (clazz == null) {
                synchronized (classCreationLock) {
                    clazz = cache.get(t);
                    if (clazz == null) {
                        clazz = createClass(interf, input, output);
                        cache.put(t, clazz);
                    }
                }
            }
            return (T) clazz.getConstructor(Object.class).newInstance(target);
        } catch (InstantiationException e) {
            ;
        } catch (IllegalAccessException e) {
            ;
        } catch (IllegalArgumentException e) {
            ;
        } catch (InvocationTargetException e) {
            ;
        } catch (NoSuchMethodException e) {
            ;
        } catch (SecurityException e) {
            ;
        }
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
    private static <T> Class<T> createClass(Class<T> interf, Method input, Method output) {

        String className = getClassName(interf, input, output);
        
        ClassWriter cwBase = new ClassWriter(0);
        CheckClassAdapter cw = new CheckClassAdapter(cwBase);
        
        MethodVisitor mv;
        
        String classNameDesc = className.replace('.', '/');
        
        String interfaceInternalName = Type.getInternalName(interf);
        
        String inputName = input.getName();
        String inputMethodDescriptor = Type.getMethodDescriptor(input);
        
        String outputParameterTypeIntName = Type.getInternalName(output.getParameterTypes()[0]);
        String outputTargetTypeIntName = Type.getInternalName(output.getDeclaringClass());
        String outputMethodDescriptor = Type.getMethodDescriptor(output);
        String outputName = output.getName();
        
        boolean isOutputInterface = output.getDeclaringClass().isInterface();
        
        // A new class of the following form is created, with a unique name
        //
        // package org.spongepowered.mod.asm;
        // public class <className> extends java.lang.Object implements <interf>
        //
        //     private final Object target
        //
        //     public <className> (java.lang.Object target) {
        //         super();
        //         this.target = target;
        //         return;
        //     }
        //
        //     public void <inputMethod> (<inputMethodType event) {
        //         ((outputTargetType) this.target).outputMethod((outputParameteType) event);
        //         return
        //     }
        // }

        // package org.spongepowered.mod.asm;
        // public class <className> extends java.lang.Object implements <interf>
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, classNameDesc, null, "java/lang/Object", 
                new String[] {interfaceInternalName});

        // private final Object target
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "target", "Ljava/lang/Object;", null, null);
        
        // Constructor
        
        // public UniqueClass (java.lang.Object target) {
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        
        // super();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        
        // this.target = target;
        mv.visitVarInsn(Opcodes.ALOAD, 0); // Loads this
        mv.visitVarInsn(Opcodes.ALOAD, 1); // Loads target (from input)
        mv.visitFieldInsn(Opcodes.PUTFIELD, classNameDesc, "target", "Ljava/lang/Object;");
        
        // return;
        mv.visitInsn(Opcodes.RETURN);
        
        // }
        // 2 localvars due to inputs: this, target
        // 2 items on stack after double ALOAD
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        
        // Callback method
        
        // public void <inputMethod> (<inputMethodType event) {
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, inputName, inputMethodDescriptor, null, null);
        mv.visitCode();
        
        // push((casted) this.target)
        mv.visitVarInsn(Opcodes.ALOAD, 0); // Loads this
        mv.visitFieldInsn(Opcodes.GETFIELD, classNameDesc, "target", "Ljava/lang/Object;");
        mv.visitTypeInsn(Opcodes.CHECKCAST, outputTargetTypeIntName);

        // push((casted) event)
        mv.visitVarInsn(Opcodes.ALOAD, 1); // Loads method parameter 0
        mv.visitTypeInsn(Opcodes.CHECKCAST, outputParameterTypeIntName);
        
        // ((outputTargetType) this.target).outputMethod((outputParameteType) event);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, outputTargetTypeIntName, outputName, outputMethodDescriptor, isOutputInterface);

        // return
        mv.visitInsn(Opcodes.RETURN);
        
        // }
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        
        cw.visitEnd();
        
        byte[] bytes = cwBase.toByteArray();
        
        return (Class<T>) loader.defineClass(className, bytes);
    }
    
    private static String getClassName(Class<?> source, Method input, Method output) {
       int id = classId++;
       String prefix = ASMEventListenerFactory.class.getName();
       String sourceName = source.getSimpleName();
       String inputName = input.getParameterTypes()[0].getSimpleName();
       String outputName = output.getParameterTypes()[0].getSimpleName();
       return prefix + "_" + id + "_" + sourceName + "_" + inputName + "_" + outputName;
    }
    
    private static class HashTriple {
        
        private final Class<?> interf;
        private final Method input;
        private final Method output;
        private final int hashCode;
        
        public HashTriple(Class<?> interf, Method input, Method output) {
            this.interf = interf;
            this.input = input;
            this.output = output;
            this.hashCode = interf.hashCode() + input.hashCode() + output.hashCode();
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
                return t.input.equals(input) && t.output.equals(output) && t.interf.equals(interf);
            }
        }
    }
    
    private static class ASMFactoryClassLoader extends ClassLoader {
        private ASMFactoryClassLoader() {
            super(ASMEventListenerFactory.class.getClassLoader());
        }
        
        private Class<?> defineClass(String name, byte[] b) {
            return super.defineClass(name, b, 0, b.length);
        }
    }
}
