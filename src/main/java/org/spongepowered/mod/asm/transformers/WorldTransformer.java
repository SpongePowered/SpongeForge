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

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.api.world.World;

public class WorldTransformer extends Transformer {

    private String classname = "net.minecraft.world.WorldServer";
    private String interfacename = Type.getInternalName(World.class);

    @Override
    public boolean transform(String s, String s2, ClassNode classNode) {
        if (s.equalsIgnoreCase(classname)) {
            classNode.interfaces.add(interfacename);
            classNode.methods.add(createGetNameMethode());
            return true;
        }
        return false;
    }

    public MethodNode createGetNameMethode(){
        MethodNode methodNode = new MethodNode(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
        methodNode.visitCode();
        Label l0 = new Label();
        methodNode.visitLabel(l0);
        methodNode.visitLineNumber(37, l0);
        methodNode.visitVarInsn(ALOAD, 0);
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/WorldServer", "getWorldInfo",
                                "()Lnet/minecraft/world/storage/WorldInfo;", false);
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/storage/WorldInfo", "getWorldName", "()Ljava/lang/String;", false);
        methodNode.visitInsn(ARETURN);
        Label l1 = new Label();
        methodNode.visitLabel(l1);
        methodNode.visitLocalVariable("this", "Lnet/minecraft/world/WorldServer;", null, l0, l1, 0);
        methodNode.visitMaxs(1, 1);
        methodNode.visitEnd();
        return methodNode;
    }
}
