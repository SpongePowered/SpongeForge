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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.mod.asm.util.ASMHelper;
import org.spongepowered.mod.mixin.InvalidMixinException;
import org.spongepowered.mod.mixin.Mixin;

/**
 * Runtime information bundle about a mixin
 */
class MixinInfo implements Comparable<MixinInfo> {
    
    /**
     * Global order of mixin infos, used to determine ordering between mixins with equivalent priority
     */
    static int mixinOrder = 0;
    
    /**
     * Logger
     */
    private final transient Logger logger = LogManager.getLogger("sponge");
    
    /**
     * Parent configuration which declares this mixin 
     */
    private final transient MixinConfig parent;

    /**
     * Name of the mixin class itself, dotted notation
     */
    private final String className;

    /**
     * Name of the mixin class, internal notation
     */
    private final transient String classRef;
    
    /**
     * Mixin priority, read from the {@link Mixin} annotation on the mixin class
     */
    private final int priority;
    
    /**
     * Mixin targets, read from the {@link Mixin} annotation on the mixin class
     */
    private final List<String> targetClasses; 

    /**
     * Intrinsic order (for sorting mixins with identical priority)
     */
    private final transient int order = MixinInfo.mixinOrder++;
    
    /**
     * Mixin bytes (read once, generate tree on demand)
     */
    private final transient byte[] mixinBytes;

    /**
     * Internal ctor, called by {@link MixinConfig}
     * 
     * @param parent
     * @param mixinName
     * @param runTransformers
     */
    MixinInfo(MixinConfig parent, String mixinName, boolean runTransformers) {
        this.parent = parent;
        this.className = parent.getMixinPackage() + mixinName;
        this.classRef = this.className.replace('.', '/');
        
        // Read the class bytes and transform
        this.mixinBytes = this.loadMixinClass(this.className, runTransformers);
        
        ClassNode classNode = this.getClassNode(0);
        this.targetClasses = this.readTargetClasses(classNode);
        this.priority = this.readPriority(classNode);
    }

    /**
     * Read the target class names from the {@link Mixin} annotation
     * 
     * @param classNode
     * @return
     */
    private List<String> readTargetClasses(ClassNode classNode) {
        AnnotationNode mixin = ASMHelper.getInvisibleAnnotation(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }
        
        List<Type> targetClasses = ASMHelper.getAnnotationValue(mixin);
        List<String> targetClassNames = new ArrayList<String>();
        for (Type targetClass : targetClasses) {
            targetClassNames.add(targetClass.getClassName());
        }
        
        return targetClassNames;
    }

    /**
     * Read the priority from the {@link Mixin} annotation
     * 
     * @param classNode
     * @return
     */
    private int readPriority(ClassNode classNode) {
        AnnotationNode mixin = ASMHelper.getInvisibleAnnotation(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }
        
        Integer priority = ASMHelper.getAnnotationValue(mixin, "priority");
        return priority == null ? 1000 : priority.intValue();
    }
    
    /**
     * Get the parent config which declares this mixin
     */
    public MixinConfig getParent() {
        return this.parent;
    }

    /**
     * Get the name of the mixin class
     */
    public String getClassName() {
        return this.className;
    }
    
    /**
     * Get the ref (internal name) of the mixin class
     */
    public String getClassRef() {
        return this.classRef;
    }

    /**
     * Get the class bytecode
     */
    public byte[] getClassBytes() {
        return this.mixinBytes;
    }

    /**
     * Get a new tree for the class bytecode
     */
    public ClassNode getClassNode(int flags) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(this.mixinBytes);
        classReader.accept(classNode, flags);
        return classNode;
    }
    
    /**
     * Get a new mixin data container for this info
     * 
     * @return
     */
    public MixinData getData() {
        return new MixinData(this);
    }
    
    /**
     * Get the target classes for this mixin
     */
    public List<String> getTargetClasses() {
        return Collections.unmodifiableList(this.targetClasses);
    }
    
    /**
     * Get the mixin priority
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @param mixinClassName
     * @param runTransformers
     * @return
     */
    private byte[] loadMixinClass(String mixinClassName, boolean runTransformers) {
        byte[] mixinBytes = null;

        try {
            if ((mixinBytes = MixinInfo.getClassBytes(mixinClassName)) == null) {
                throw new InvalidMixinException(String.format("The specified mixin '%s' was not found", mixinClassName));
            }

            if (runTransformers) {
                mixinBytes = MixinInfo.applyTransformers(mixinClassName, mixinBytes);
            }
        } catch (IOException ex) {
            this.logger.warn("Failed to load mixin %s, the specified mixin will not be applied", mixinClassName);
            throw new InvalidMixinException("An error was encountered whilst loading the mixin class", ex);
        }

        return mixinBytes;
    }

    /**
     * @param mixinClassName
     * @return
     * @throws IOException
     */
    static byte[] getClassBytes(String mixinClassName) throws IOException {
        return Launch.classLoader.getClassBytes(mixinClassName);
    }

    /**
     * Since we obtain the mixin class bytes with getClassBytes(), we need to
     * apply the transformers ourself
     * 
     * @param name
     * @param basicClass
     * @return
     */
    static byte[] applyTransformers(String name, byte[] basicClass) {
        final List<IClassTransformer> transformers = Launch.classLoader.getTransformers();

        for (final IClassTransformer transformer : transformers) {
            if (!(transformer instanceof MixinTransformer)) {
                basicClass = transformer.transform(name, name, basicClass);
            }
        }

        return basicClass;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MixinInfo other) {
        if (other == null) {
            return 0;
        }
        if (other.priority == this.priority) {
            return this.order - other.order;
        }
        return (this.priority - other.priority);
    }
}
