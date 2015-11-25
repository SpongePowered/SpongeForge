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
package org.spongepowered.mod.asm.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.spongepowered.mod.asm.transformer.adapter.ISpongeAdviceAdapter;
import org.spongepowered.mod.asm.transformer.adapter.SpongeClassVisitorAdapter;

public class WorldGeneratorTransformer implements IClassTransformer, ISpongeAdviceAdapter {

    private static final String GENERATOR_METHOD_NAME = "generate";
    private static final String GENERATOR_SUPER_CLASS = "net/minecraft/world/gen/feature/WorldGenerator";
    private static final String GENERATOR_METHOD_DESC = "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/BlockPos;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new SpongeClassVisitorAdapter(this, cw, cr.getClassName(), GENERATOR_SUPER_CLASS,
                GENERATOR_METHOD_NAME, GENERATOR_METHOD_DESC);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }

    @Override
    public MethodVisitor createMethodAdviceAdapter(int api, String owner, int access, String name, String desc, MethodVisitor mv) {
        return new LocalAdviceAdapter(api, owner, access, name, desc, mv);
    }

    class LocalAdviceAdapter extends AdviceAdapter {

        public LocalAdviceAdapter(int api, String owner, int access, String name, String desc, MethodVisitor mv) {
            super(api, mv, access, name, desc);
        }

        public LocalAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/spongepowered/common/util/StaticMixinHelper", "runningGenerator", "Ljava/lang/Class;");
        }

        @Override
        protected void onMethodExit(int opcode) {
            if (opcode != Opcodes.ATHROW) {
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/spongepowered/common/util/StaticMixinHelper", "runningGenerator", "Ljava/lang/Class;");
            }
        }
    }
}
