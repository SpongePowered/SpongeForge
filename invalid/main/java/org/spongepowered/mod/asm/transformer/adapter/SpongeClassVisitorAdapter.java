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
package org.spongepowered.mod.asm.transformer.adapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

public class SpongeClassVisitorAdapter extends ClassVisitor {

    private final ISpongeAdviceAdapter adapter;
    private final String className;
    private final String superClassName;
    private final String methodName;
    private final String methodDesc;

    public SpongeClassVisitorAdapter(ISpongeAdviceAdapter adapter, ClassVisitor cv, String className, String superClassName, String methodName,
            String methodDesc) {
        super(Opcodes.ASM5, cv);
        this.adapter = adapter;
        this.className = className;
        this.superClassName = superClassName;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // if the method is the one we want to transform
        if (name.equals(this.methodName) && desc.equals(this.methodDesc) && findSuperClass(this.className)) {
            MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
            return this.adapter.createMethodAdviceAdapter(Opcodes.ASM5, this.className, access, name, desc, mv);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public boolean findSuperClass(String className) {
        ClassReader cr = null;
        try {
            cr = new ClassReader(className);
        } catch (IOException e) {
            return false;
        }

        String superName = cr.getSuperName();
        if (superName != null && !superName.equals("java/lang/Object")) {
            if (superName.equals(this.superClassName)) {
                return true;
            }
            String superClass = superName.replace('.', '/');
            findSuperClass(superClass);
        }
        return false;
    }
}
