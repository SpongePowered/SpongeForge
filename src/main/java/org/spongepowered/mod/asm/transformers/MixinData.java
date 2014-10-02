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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.mod.asm.util.ASMHelper;
import org.spongepowered.mod.mixin.Shadow;


/**
 * Data for applying a mixin. Keeps a copy of the mixin tree and also handles any pre-transformations required by the mixin class itself such as
 * method renaming and any other pre-processing of the mixin bytecode.
 */
public class MixinData {
    
    /**
     * Tree
     */
    private final ClassNode classNode;
    
    /**
     * Methods we need to rename
     */
    private final Map<String, String> renamedMethods = new HashMap<String, String>();

    /**
     * ctor
     * 
     * @param info
     */
    MixinData(MixinInfo info) {
        this.classNode = info.getClassNode(ClassReader.EXPAND_FRAMES);
        this.prepare();
    }

    /**
     * Get the mixin tree
     * 
     * @return
     */
    public ClassNode getClassNode() {
        return this.classNode;
    }

    /**
     * Prepare the mixin, applies any pre-processing transformations
     */
    private void prepare() {
        this.findRenamedMethods();
        this.transformMethods();
    }

    /**
     * Let's do this
     */
    private void findRenamedMethods() {
        for (MethodNode mixinMethod : this.classNode.methods) {
            AnnotationNode shadowAnnotation = ASMHelper.getVisibleAnnotation(mixinMethod, Shadow.class);
            if (shadowAnnotation == null) {
                continue;
            }
            
            String prefix = MixinData.getAnnotationValue(shadowAnnotation, "prefix", Shadow.class);
            if (mixinMethod.name.startsWith(prefix)) {
                String newName = mixinMethod.name.substring(prefix.length());
                this.renamedMethods.put(mixinMethod.name + mixinMethod.desc, newName);
                mixinMethod.name = newName;
            }
        }
    }

    /**
     * Apply discovered method renames to method invokations in the mixin
     */
    private void transformMethods() {
        for (MethodNode mixinMethod : this.classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = mixinMethod.instructions.iterator(); iter.hasNext();) {
                AbstractInsnNode insn = iter.next();
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode)insn;
                    String newName = this.renamedMethods.get(methodNode.name + methodNode.desc);
                    if (newName != null) {
                        methodNode.name = newName;
                    }
                }
            }
        }
    }

    /**
     * Gets an annotation value or returns the default if the annotation value is not present
     * 
     * @param annotation
     * @param key
     * @param annotationClass
     * @return
     */
    private static String getAnnotationValue(AnnotationNode annotation, String key, Class<?> annotationClass) {
        String value = ASMHelper.getAnnotationValue(annotation, key);
        if (value == null) {
            try {
                value = (String)Shadow.class.getDeclaredMethod(key).getDefaultValue();
            } catch (NoSuchMethodException ex) {
                // Don't care
            }
        }
        return value;
    }
}
