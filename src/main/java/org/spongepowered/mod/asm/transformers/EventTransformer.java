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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.voxel.VoxelEvent;
import org.spongepowered.mod.asm.util.ASMHelper;

import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public class EventTransformer implements IClassTransformer {
    
    private static final Map<String, Class<?>> events = new HashMap<String, Class<?>>();
    
    static {
        events.put("cpw.mods.fml.common.event.FMLPreInitializationEvent", PreInitializationEvent.class);
        events.put("cpw.mods.fml.common.event.FMLInitializationEvent", InitializationEvent.class);
        events.put("cpw.mods.fml.common.event.FMLServerStartingEvent", ServerStartingEvent.class);
        
        events.put("net.minecraftforge.event.world.BlockEvent$BreakEvent", VoxelEvent.class);
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        
        if (bytes == null 
                || transformedName.startsWith("net.minecraft.")
                || transformedName.equals("org.spongepowered.api.event.BaseEvent")
                || transformedName.equals("cpw.mods.fml.common.event.FMLEvent")
                || transformedName.equals("cpw.mods.fml.common.eventhandler.Event") 
                || transformedName.indexOf('.') == -1) {
            return bytes;
        }

        try {
            ClassReader cr = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);
            
            String parentName = classNode.superName.replace('/', '.');
            
            Class<?> parent = this.getClass().getClassLoader().loadClass(parentName);
            
            // Only process FMLEvents and Forge Events sub-classes
            
            if ((!Event.class.isAssignableFrom(parent)) && (!FMLEvent.class.isAssignableFrom(parent))) {
                return bytes;
            }
            
            Class<?> interf = events.get(transformedName);
            if (interf != null && interf.isInterface()) {
                String interfaceName = Type.getInternalName(interf);

                classNode.interfaces.add(interfaceName);
            }

            // Method forwarding for all events
            classNode.methods.add(createGetGameMethod());
            classNode.methods.add(createGetSimpleNameMethod());
            
            if (Event.class.isAssignableFrom(parent)) {
                // Forge Event method forwarding
                if (classNode.interfaces.contains("org/spongepowered/api/event/Cancellable")) {
                    if (classNode.visibleAnnotations == null) {
                        classNode.visibleAnnotations = new ArrayList<AnnotationNode>();
                    }
                    classNode.visibleAnnotations.add(new AnnotationNode("Lcpw/mods/fml/common/eventhandler/Cancelable;"));
                    classNode.methods.add(createIsCancelledMethod());
                    classNode.methods.add(createSetCancelledMethod());
                }
                classNode.methods.add(createIsCancellableMethod());
            }

            // TODO: This is a temporary thing to make PreInit work. The different things needed to make different events work should be abstracted.
            if (interf != null && PreInitializationEvent.class.isAssignableFrom(interf)) {
                ASMHelper.generateSelfForwardingMethod(classNode, "getConfigurationDirectory", "getModConfigurationDirectory",
                                                       Type.getType(File.class));
                ASMHelper.generateSelfForwardingMethod(classNode, "getPluginLog", "getModLog",
                                                       Type.getType(Logger.class));
            }
            
            ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS | COMPUTE_FRAMES);
                    
            classNode.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return bytes;
        }
    }
    
    protected static MethodNode createGetGameMethod() {
        MethodNode methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "getGame", "()Lorg/spongepowered/api/Game;", null, null);
        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/spongepowered/mod/SpongeMod", "instance", 
                "Lorg/spongepowered/mod/SpongeMod;"));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/spongepowered/mod/SpongeMod", "getGame", 
                "()Lorg/spongepowered/mod/SpongeGame;", false));
        methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        return methodNode;
    }
    
    protected static MethodNode createGetSimpleNameMethod() {
        MethodNode methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "getSimpleName", "()Ljava/lang/String;", null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false));
        methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        return methodNode;
    }
    
    protected static MethodNode createIsCancellableMethod() {
        MethodNode methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "isCancellable", "()Z", null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "cpw/mods/fml/common/eventhandler/Event", "isCancelable", 
                "()Z", false));
        methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        return methodNode;
    }
    
    protected static MethodNode createIsCancelledMethod() {
        MethodNode methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "isCancelled", "()Z", null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "cpw/mods/fml/common/eventhandler/Event", "isCanceled", "()Z", false));
        methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        return methodNode;
    }
    
    protected static MethodNode createSetCancelledMethod() {
        MethodNode methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "setCancelled", "(Z)V", null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "cpw/mods/fml/common/eventhandler/Event", "setCanceled", 
                "(Z)V", false));
        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        return methodNode;
    }
}
