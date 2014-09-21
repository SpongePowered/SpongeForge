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

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMHelper {

    /**
     * Generate a new method "boolean name()", which returns a constant value.
     *
     * @param clazz Class to add method to
     * @param name Name of method
     * @param retval Return value of method
     */
    public static void generateBooleanMethodConst(ClassNode clazz, String name, boolean retval) {
        MethodNode method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()Z", null, null);
        InsnList code = method.instructions;

        code.add(pushIntConstant(retval ? 1 : 0));
        code.add(new InsnNode(Opcodes.IRETURN));

        clazz.methods.add(method);
    }

    /**
     * Generate a new method "int name()", which returns a constant value.
     *
     * @param clazz Class to add method to
     * @param name Name of method
     * @param retval Return value of method
     */
    public static void generateIntegerMethodConst(ClassNode clazz, String name, short retval) {
        MethodNode method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()I", null, null);
        InsnList code = method.instructions;

        code.add(pushIntConstant(retval));
        code.add(new InsnNode(Opcodes.IRETURN));

        clazz.methods.add(method);
    }

    /**
     * Generate a forwarding method of the form
     * "T name() { return this.forward(); }".
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     */
    public static void generateSelfForwardingMethod(ClassNode clazz, String name, String forwardname, Type rettype) {
        MethodNode method =
                new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor(), null, null);

        populateSelfForwardingMethod(method, forwardname, rettype, Type.getObjectType(clazz.name));

        clazz.methods.add(method);
    }

    /**
     * Generate a forwarding method of the form
     * "static T name(S object) { return object.forward(); }".
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     */
    public static void generateStaticForwardingMethod(ClassNode clazz, String name, String forwardname, Type rettype, Type argtype) {
        MethodNode method = new MethodNode(Opcodes.ASM5,
                                           Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name,
                                           "()" + rettype.getDescriptor(), null, null);

        populateSelfForwardingMethod(method, forwardname, rettype, argtype);

        clazz.methods.add(method);
    }

    /**
     * Generate a forwarding method of the form
     * "T name() { return Class.forward(this); }".
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     */
    public static void generateForwardingToStaticMethod(ClassNode clazz, String name, String forwardname, Type rettype, Type fowardtype) {
        MethodNode method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name,
                                           "()" + rettype.getDescriptor(), null, null);

        populateForwardingToStaticMethod(method, forwardname, rettype, Type.getObjectType(clazz.name), fowardtype);

        clazz.methods.add(method);
    }

    /**
     * Generate a forwarding method of the form
     * "T name() { return Class.forward(this); }".
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param thistype Type to treat 'this' as for overload searching purposes
     */
    public static void generateForwardingToStaticMethod(ClassNode clazz, String name, String forwardname, Type rettype, Type fowardtype,
                                                        Type thistype) {
        MethodNode
                method =
                new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor(), null, null);

        populateForwardingToStaticMethod(method, forwardname, rettype, thistype, fowardtype);

        clazz.methods.add(method);
    }

    /**
     * Replace a method's code with a forward to another method on itself
     * or the first argument of a static method, as the argument takes the
     * place of this.
     *
     * @param method Method to replace code of
     * @param forwardname Name of method to forward to
     * @param thistype Type of object method is being replaced on
     */
    public static void replaceSelfForwardingMethod(MethodNode method, String forwardname, Type thistype) {
        Type methodType = Type.getMethodType(method.desc);

        method.instructions.clear();

        populateSelfForwardingMethod(method, forwardname, methodType.getReturnType(), thistype);
    }

    /**
     * Generate a forwarding method of the form
     * "T name(S object) { return object.forward(); }".
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param argtype Type of object to call method on
     */
    public static void generateForwardingMethod(ClassNode clazz, String name, String forwardname, Type rettype, Type argtype) {
        MethodNode
                method =
                new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor(), null, null);

        populateForwardingMethod(method, forwardname, rettype, argtype, Type.getObjectType(clazz.name));

        clazz.methods.add(method);
    }

    /**
     * Replace a method's code with a forward to an method on its first argument.
     *
     * @param method Method to replace code of
     * @param forwardname Name of method to forward to
     * @param thistype Type of object method is being replaced on
     */
    public static void replaceForwardingMethod(MethodNode method, String forwardname, Type thistype) {
        Type methodType = Type.getMethodType(method.desc);

        method.instructions.clear();

        populateForwardingMethod(method, forwardname, methodType.getReturnType(), methodType.getArgumentTypes()[0], thistype);
    }

    /**
     * Populate a forwarding method of the form
     * "T name() { return Class.forward(this); }".
     *
     * @param method Method to generate code for
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param thistype Type of object method is being generated on
     * @param forwardtype Type to forward method to
     */
    public static void populateForwardingToStaticMethod(MethodNode method, String forwardname, Type rettype, Type thistype, Type forwardtype) {
        InsnList code = method.instructions;

        code.add(new VarInsnNode(thistype.getOpcode(Opcodes.ILOAD), 0));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, forwardtype.getInternalName(), forwardname, Type.getMethodDescriptor(rettype, thistype),
                                    false));
        code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)));
    }

    /**
     * Populate a forwarding method of the form
     * "T name() { return this.forward(); }". This is also valid for methods of
     * the form "static T name(S object) { return object.forward() }".
     *
     * @param method Method to generate code for
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param thistype Type of object method is being generated on
     */
    public static void populateSelfForwardingMethod(MethodNode method, String forwardname, Type rettype, Type thistype) {
        InsnList code = method.instructions;

        code.add(new VarInsnNode(thistype.getOpcode(Opcodes.ILOAD), 0));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, thistype.getInternalName(), forwardname, "()" + rettype.getDescriptor(), false));
        code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)));
    }

    /**
     * Populate a forwarding method of the form
     * "T name(S object) { return object.forward(); }".
     *
     * @param method Method to generate code for
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param argtype Type of object to call method on
     * @param thistype Type of object method is being generated on
     */
    public static void populateForwardingMethod(MethodNode method, String forwardname, Type rettype, Type argtype, Type thistype) {
        InsnList code = method.instructions;

        code.add(new VarInsnNode(argtype.getOpcode(Opcodes.ILOAD), 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, argtype.getInternalName(), forwardname, "()" + rettype.getDescriptor(), false));
        code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)));
    }
    
    private final static int[] intConstants = new int[] {Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5};
    
    /**
     * Gets an instruction that pushes a integer onto the stack.  The instruction uses
     * the smallest push possible (ICONST_*, BIPUSH, SIPUSH or Integer constant).
     * 
     * @param c the integer to push onto the stack
     */
    public static AbstractInsnNode pushIntConstant(int c) {
        if (c == -1) {
            return new InsnNode(Opcodes.ICONST_M1);
        } else if (c >= 0 && c <= 5) {
            return new InsnNode(intConstants[c]);
        } else if (c >= Byte.MIN_VALUE && c <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, c);
        } else if (c >= Short.MIN_VALUE && c <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, c);
        } else {
            return new LdcInsnNode(c);
        }
    }
    
    /** 
     * Finds a method given the method descriptor
     * 
     * @param clazz the class to scan
     * @param name the method name
     * @param desc the method descriptor
     * @param 
     */
    public static MethodNode findMethod(ClassNode clazz, String name, String desc) {
        Iterator<MethodNode> i = clazz.methods.iterator();
        while (i.hasNext()) {
            MethodNode m = i.next();
            if (m.name.equals(name) && m.desc.equals(desc)) {
                return m;
            }
        }
        return null;
    }
}