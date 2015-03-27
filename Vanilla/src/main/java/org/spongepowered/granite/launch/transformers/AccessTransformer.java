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
package org.spongepowered.granite.launch.transformers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class AccessTransformer implements IClassTransformer {

    private static final Splitter SEPARATOR = Splitter.on(' ').trimResults();

    private final ImmutableMultimap<String, Modifier> modifiers;

    public AccessTransformer() throws IOException {
        this((URL[]) Launch.blackboard.get("granite.at"));
    }

    protected AccessTransformer(String file) throws IOException {
        this(getResource(file));
    }

    protected AccessTransformer(URL... files) throws IOException {
        Processor processor = new Processor();
        for (URL file : files) {
            readLines(file, Charsets.UTF_8, processor);
        }

        this.modifiers = processor.build();
    }

    private static String substringBefore(String s, char c) {
        int pos = s.indexOf(c);
        return pos >= 0 ? s.substring(0, pos) : s;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !this.modifiers.containsKey(transformedName)) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        for (Modifier m : this.modifiers.get(transformedName)) {
            if (m.isClass) { // Class
                classNode.access = m.transform(classNode.access);
            } else if (m.desc == null) { // Field
                for (FieldNode fieldNode : classNode.fields) {
                    if (m.wildcard || fieldNode.name.equals(m.name)) {
                        fieldNode.access = m.transform(fieldNode.access);
                        if (!m.wildcard) {
                            break;
                        }
                    }
                }
            } else {
                List<MethodNode> overridable = null;

                for (MethodNode methodNode : classNode.methods) {
                    if (m.wildcard || (methodNode.name.equals(m.name) && methodNode.desc.equals(m.desc))) {
                        boolean wasPrivate = (methodNode.access & ACC_PRIVATE) != 0;
                        methodNode.access = m.transform(methodNode.access);

                        // Constructors always use INVOKESPECIAL
                        // if we changed from private to something else we need to replace all INVOKESPECIAL calls to this method with INVOKEVIRTUAL
                        // so that overridden methods will be called. Only need to scan this class, because obviously the method was private.
                        if (!methodNode.name.equals("<init>") && wasPrivate && (methodNode.access & ACC_PRIVATE) == 0) {
                            if (overridable == null) {
                                overridable = Lists.newArrayListWithExpectedSize(3);
                            }

                            overridable.add(methodNode);
                        }

                        if (!m.wildcard) {
                            break;
                        }
                    }
                }

                if (overridable != null) {
                    for (MethodNode methodNode : classNode.methods) {
                        for (Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator(); itr.hasNext(); ) {
                            AbstractInsnNode insn = itr.next();
                            if (insn.getOpcode() == INVOKESPECIAL) {
                                MethodInsnNode mInsn = (MethodInsnNode) insn;
                                for (MethodNode replace : overridable) {
                                    if (replace.name.equals(mInsn.name) && replace.desc.equals(mInsn.desc)) {
                                        mInsn.setOpcode(INVOKEVIRTUAL);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static class Processor implements LineProcessor<Void> {

        private final ImmutableMultimap.Builder<String, Modifier> builder = ImmutableListMultimap.builder();

        @Override
        public boolean processLine(String line) throws IOException {
            line = substringBefore(line, '#').trim();
            if (line.isEmpty()) {
                return true;
            }

            List<String> parts = SEPARATOR.splitToList(line);
            checkArgument(parts.size() <= 3, "Invalid access transformer config line: " + line);

            String name = null;
            String desc = null;

            boolean isClass = parts.size() == 2;
            if (!isClass) {
                name = parts.get(2);
                int pos = name.indexOf('(');
                if (pos >= 0) {
                    desc = name.substring(pos);
                    name = name.substring(0, pos);
                }
            }

            String s = parts.get(0);
            int access = 0;
            if (s.startsWith("public")) {
                access = ACC_PUBLIC;
            } else if (s.startsWith("protected")) {
                access = ACC_PROTECTED;
            } else if (s.startsWith("private")) {
                access = ACC_PRIVATE;
            }

            Boolean markFinal = null;
            if (s.endsWith("+f")) {
                markFinal = true;
            } else if (s.endsWith("-f")) {
                markFinal = false;
            }

            String className = parts.get(1).replace('/', '.');
            this.builder.put(className, new Modifier(name, desc, isClass, access, markFinal));
            return true;
        }

        @Override
        public Void getResult() {
            return null;
        }

        public ImmutableMultimap<String, Modifier> build() {
            return this.builder.build();
        }

    }

    private static class Modifier {

        private final String name;
        private final String desc;
        private final boolean wildcard;
        private final boolean isClass;

        private final int targetAccess;
        private final Boolean markFinal;

        private Modifier(String name, String desc, boolean isClass, int targetAccess, Boolean markFinal) {
            boolean wildcard = false;
            if (name != null) {
                checkArgument(!name.isEmpty(), "name cannot be empty");
                wildcard = name.equals("*");
            }
            this.name = name;
            checkArgument(desc == null || !desc.isEmpty(), "desc cannot be empty");
            this.desc = desc;
            this.wildcard = wildcard;
            this.isClass = isClass;
            this.targetAccess = targetAccess;
            this.markFinal = markFinal;
        }

        private int transform(int access) {
            int result = access & ~7;

            switch (access & 4) {
                case ACC_PRIVATE:
                    result |= this.targetAccess;
                    break;
                case 0: // default
                    if (this.targetAccess != ACC_PRIVATE) {
                        result |= this.targetAccess;
                    }
                    break;
                case ACC_PROTECTED:
                    result |= this.targetAccess != 0 && this.targetAccess != ACC_PRIVATE ? this.targetAccess : ACC_PROTECTED;
                    break;
                case ACC_PUBLIC:
                    result |= this.targetAccess != 0 && this.targetAccess != ACC_PRIVATE && this.targetAccess != ACC_PROTECTED ? this.targetAccess
                            : ACC_PUBLIC;
                    break;
                default:
                    throw new AssertionError();
            }

            if (this.markFinal != null) {
                if (this.markFinal) {
                    result |= ACC_FINAL;
                } else {
                    result &= ~ACC_FINAL;
                }
            }

            return result;
        }
    }

}
