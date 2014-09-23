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
package org.spongepowered.mod.asm.transformers;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.mod.asm.util.ASMHelper;

public class BaseEventTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {

        if (bytes == null || name == null || !name.equals("org.spongepowered.api.event.BaseEvent")) {
            return bytes;
        }
        
        try {
            ClassReader cr = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);
            
            // Add forwarding methods
            ASMHelper.addAndReplaceMethod(classNode, EventTransformer.createGetGameMethod());
            ASMHelper.addAndReplaceMethod(classNode, EventTransformer.createGetSimpleNameMethod());
            ASMHelper.addAndReplaceMethod(classNode, EventTransformer.createIsCancellableMethod());
            ASMHelper.addAndReplaceMethod(classNode, EventTransformer.createIsCancelledMethod());
            ASMHelper.addAndReplaceMethod(classNode, EventTransformer.createSetCancelledMethod());
            
            // Change super-class
            classNode.superName = "cpw/mods/fml/common/eventhandler/Event";
            
            // Replace super() call in constructor so that it points to the new super-class
            MethodNode method = ASMHelper.findMethod(classNode, "<init>", "()V");
            
            ListIterator<AbstractInsnNode> instructions = method.instructions.iterator();
            
            while (instructions.hasNext()) {
                AbstractInsnNode insn = instructions.next();
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsn = new MethodInsnNode(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false);
                    instructions.remove();
                    instructions.add(methodInsn);
                    break;
                }
            }

            ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS | COMPUTE_FRAMES);
            classNode.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return bytes;
        }
    }

}
