package org.spongepowered.launch.transformer.itemhandler;

import static org.spongepowered.asm.lib.Opcodes.ACC_PRIVATE;
import static org.spongepowered.asm.lib.Opcodes.ACC_STATIC;
import static org.spongepowered.asm.lib.Opcodes.ALOAD;
import static org.spongepowered.asm.lib.Opcodes.ARETURN;
import static org.spongepowered.asm.lib.Opcodes.ASM5;
import static org.spongepowered.asm.lib.Opcodes.CHECKCAST;
import static org.spongepowered.asm.lib.Opcodes.DLOAD;
import static org.spongepowered.asm.lib.Opcodes.DRETURN;
import static org.spongepowered.asm.lib.Opcodes.FLOAD;
import static org.spongepowered.asm.lib.Opcodes.FRETURN;
import static org.spongepowered.asm.lib.Opcodes.F_SAME;
import static org.spongepowered.asm.lib.Opcodes.IFEQ;
import static org.spongepowered.asm.lib.Opcodes.ILOAD;
import static org.spongepowered.asm.lib.Opcodes.INSTANCEOF;
import static org.spongepowered.asm.lib.Opcodes.INVOKEINTERFACE;
import static org.spongepowered.asm.lib.Opcodes.INVOKESTATIC;
import static org.spongepowered.asm.lib.Opcodes.INVOKEVIRTUAL;
import static org.spongepowered.asm.lib.Opcodes.IRETURN;
import static org.spongepowered.asm.lib.Opcodes.LLOAD;
import static org.spongepowered.asm.lib.Opcodes.LRETURN;
import static org.spongepowered.asm.lib.Opcodes.RETURN;

import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.mod.itemhandler.ItemHandlerHelper;
import org.spongepowered.mod.itemhandler.ItemHandlerMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class ItemHandlerTrackerClassTransformer implements IClassTransformer {

    private static final String I_ITEM_HANDLER = "net.minecraftforge.items.IItemHandler";
    private static final String I_ITEM_HANDLER_MODIFIABLE = "net.minecraftforge.items.IItemHandlerModifiable";

    private static final HandlerType I_ITEM_HANDLER_TYPE =
            new HandlerType(I_ITEM_HANDLER.replace('.', '/'));
    private static final HandlerType I_ITEM_HANDLER_MODIFIABLE_TYPE =
            new HandlerType(I_ITEM_HANDLER_MODIFIABLE.replace('.', '/'));

    private static final HandlerType[] HANDLER_TYPES = {
            I_ITEM_HANDLER_TYPE,
            I_ITEM_HANDLER_MODIFIABLE_TYPE,
    };

    private static final Map<String, MethodEntry> METHOD_ENTRIES = new HashMap<>();
    private static class MethodEntry {

        final String desc;
        final HandlerType type;
        final Type[] paramTypes;
        final Type returnType;

        private MethodEntry(String desc, HandlerType type, Type[] paramTypes, Type returnType) {
            this.returnType = returnType;
            this.paramTypes = paramTypes;
            this.desc = desc;
            this.type = type;
        }
    }
    private static class HandlerType {

        final String name;
        final Set<String> knownSubtypes;

        private HandlerType(String name, String... knownSubtypes) {
            this.knownSubtypes = new HashSet<>();
            this.knownSubtypes.add(name);
            Collections.addAll(this.knownSubtypes, knownSubtypes);
            this.name = name;
        }
    }

    static {
        for (Method method : ItemHandlerHelper.class.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) ||
                    method.getAnnotation(ItemHandlerMethod.class) == null) {
                continue;
            }
            final String desc = Type.getMethodDescriptor(method);
            String oldDesc = null;
            HandlerType type = null;
            for (HandlerType handler : HANDLER_TYPES) {
                final String handlerDesc = 'L' + handler.name + ';';
                if (desc.indexOf(handlerDesc, 1) != -1) {
                    oldDesc = '(' + desc.substring(handlerDesc.length() + 1);
                    type = handler;
                }
            }
            if (oldDesc == null) {
                throw new IllegalStateException("Invalid delegated method: " + method.getName() + ';' + desc);
            }
            METHOD_ENTRIES.put(method.getName() + ';' + oldDesc, new MethodEntry(desc, type,
                    Type.getArgumentTypes(method), Type.getReturnType(method)));
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        final ClassReader classReader = new ClassReader(basicClass);

        final ClassNode classNode = new ClassNode(ASM5);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        final Set<String> addedMethods = new HashSet<>();
        boolean change = false;
        for (MethodNode methodNode : new ArrayList<>(classNode.methods)) {
            if (processMethodNode(classNode, methodNode, addedMethods)) {
                change = true;
            }
        }

        if (!change) {
            return basicClass;
        }

        final ClassWriter classWriter = new ClassWriter(ASM5);
        classNode.accept(classWriter);

        return classWriter.toByteArray();
    }

    private static boolean processMethodNode(ClassNode classNode, MethodNode methodNode, Set<String> addedMethods) {
        final ListIterator<AbstractInsnNode> it = methodNode.instructions.iterator();
        boolean change = false;
        while (it.hasNext()) {
            final AbstractInsnNode node = it.next();
            if (node.getOpcode() != INVOKEVIRTUAL &&
                    node.getOpcode() != INVOKEINTERFACE) {
                continue;
            }
            final MethodInsnNode methodInsnNode = (MethodInsnNode) node;
            final String id = methodInsnNode.name + ';' + methodInsnNode.desc;
            final MethodEntry entry = METHOD_ENTRIES.get(id);
            if (entry == null) {
                continue;
            }
            // Check if we can directly redirect the method
            if (entry.type.knownSubtypes.contains(methodInsnNode.owner)) {
                final MethodInsnNode newMethodInsnNode = new MethodInsnNode(INVOKESTATIC,
                        Type.getInternalName(ItemHandlerHelper.class), methodInsnNode.name, entry.desc, false);
                it.set(newMethodInsnNode);
            } else {
                String simpleOwner = methodInsnNode.owner;
                final int index = simpleOwner.lastIndexOf('/');
                if (index != -1) {
                    simpleOwner = simpleOwner.substring(index + 1);
                }
                final String methodName = simpleOwner + '$' + methodInsnNode.name + '$' + Integer.toHexString(methodInsnNode.desc.hashCode());
                final String desc = "(L" + methodInsnNode.owner + ';' + methodInsnNode.desc.substring(1);
                if (!addedMethods.contains(methodName)) {
                    // Generate a static method that checks the instance
                    final MethodNode m = (MethodNode) classNode.visitMethod(ACC_PRIVATE | ACC_STATIC, methodName, desc, null, null);
                    addedMethods.add(methodName);
                    m.visitCode();
                    // Check instance of the possible IItemHandler
                    m.visitVarInsn(ALOAD, 0);
                    m.visitTypeInsn(INSTANCEOF, entry.type.name);
                    final Label ifLabel = new Label();
                    m.visitJumpInsn(IFEQ, ifLabel);
                    m.visitVarInsn(ALOAD, 0);
                    // Call the IItemHandler method if the instance check is a success
                    m.visitTypeInsn(CHECKCAST, entry.type.name);
                    for (int i = 1; i < entry.paramTypes.length; i++) {
                        m.visitVarInsn(getLoadOpcode(entry.paramTypes[i]), i);
                    }
                    m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ItemHandlerHelper.class),
                            methodInsnNode.name, entry.desc, false);
                    m.visitInsn(getReturnOpcode(entry.returnType));
                    m.visitLabel(ifLabel);
                    m.visitFrame(F_SAME, 0, null, 0, null);
                    m.visitVarInsn(ALOAD, 0);
                    // Call the original method
                    for (int i = 1; i < entry.paramTypes.length; i++) {
                        m.visitVarInsn(getLoadOpcode(entry.paramTypes[i]), i);
                    }
                    m.visitMethodInsn(methodInsnNode.getOpcode(), methodInsnNode.owner,
                            methodInsnNode.name, methodInsnNode.desc, methodInsnNode.itf);
                    m.visitInsn(getReturnOpcode(entry.returnType));
                    final int locals = entry.paramTypes.length;
                    m.visitMaxs(locals, locals);
                    m.visitEnd();
                }
                final MethodInsnNode newMethodInsnNode = new MethodInsnNode(INVOKESTATIC,
                        classNode.name, methodName, desc, false);
                it.set(newMethodInsnNode);
            }
            change = true;
        }
        return change;
    }

    private static int getReturnOpcode(Type type) {
        if (type == Type.VOID_TYPE) {
            return RETURN;
        } else if (type == Type.INT_TYPE ||
                type == Type.BOOLEAN_TYPE ||
                type == Type.BYTE_TYPE ||
                type == Type.CHAR_TYPE) {
            return IRETURN;
        } else if (type == Type.DOUBLE_TYPE) {
            return DRETURN;
        } else if (type == Type.FLOAT_TYPE) {
            return FRETURN;
        } else if (type == Type.FLOAT_TYPE) {
            return LRETURN;
        }
        return ARETURN;
    }

    private static int getLoadOpcode(Type type) {
        if (type == Type.INT_TYPE ||
                type == Type.BOOLEAN_TYPE ||
                type == Type.BYTE_TYPE ||
                type == Type.CHAR_TYPE) {
            return ILOAD;
        } else if (type == Type.DOUBLE_TYPE) {
            return DLOAD;
        } else if (type == Type.FLOAT_TYPE) {
            return FLOAD;
        } else if (type == Type.LONG_TYPE) {
            return LLOAD;
        }
        return ALOAD;
    }
}
