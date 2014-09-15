package org.spongepowered.mod.asm.transformers;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by thomas on 9/15/2014.
 */
public abstract class Transformer implements IClassTransformer,Opcodes {

    @Override
    public byte[] transform(String s, String s2, byte[] bytes) {
        if (bytes == null)
            return bytes;
        try {
            ClassReader cr = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);
            if (transform(s, s2, classNode)) {
                ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS | COMPUTE_FRAMES);
                classNode.accept(cw);
                return cw.toByteArray();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bytes;
    }

    public abstract boolean transform(String s, String s2, ClassNode classNode);
}
