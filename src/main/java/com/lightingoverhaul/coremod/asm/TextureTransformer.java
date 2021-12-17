package com.lightingoverhaul.coremod.asm;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class TextureTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null; //Need this so that classes that aren't loaded don't cause an exception that hides the true reason.
        ClassReader classReader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();

        classReader.accept(classNode, 0);

        String helperName = "com/lightingoverhaul/mixinmod/helper/TextureHelper";

        if (transformedName.startsWith("com.lightingoverhaul")) {
            return bytes; // Don't transform our own classes, we know what we're doing.
        }

        boolean changed = false;
        for (MethodNode methodNode : classNode.methods) {
            for (int i = 0; i < methodNode.instructions.size(); i++) {
                AbstractInsnNode instruction = methodNode.instructions.get(i);
                if (instruction.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode call = (MethodInsnNode) instruction;

                    boolean isEnable = call.name.equals("glEnable");
                    boolean isDisable = call.name.equals("glDisable");
                    if (isEnable || isDisable) {
                        AbstractInsnNode instructionPrev = methodNode.instructions.get(i - 1);
                        if (instructionPrev.getOpcode() == Opcodes.SIPUSH) {
                            IntInsnNode intNode = (IntInsnNode) instructionPrev;
                            if (intNode.operand != GL11.GL_TEXTURE_2D) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                        AbstractInsnNode hook = new MethodInsnNode(Opcodes.INVOKESTATIC, helperName, isEnable ? "enableTexture" : "disableTexture", "()V", false);
                        methodNode.instructions.insert(instruction, hook);
                        changed = true;
                        CoreLoadingPlugin.CLLog.debug("Applied ASM transformation in method " + classNode.name + "." + methodNode.name + " for " + (isEnable ? "glEnable" : "glDisable") + "(GL_TEXTURE_2D);");
                    }

                    boolean isTexCoord = call.name.equals("glTexCoord2f");
                    if (isTexCoord) {
                        AbstractInsnNode hook = new MethodInsnNode(Opcodes.INVOKESTATIC, helperName, "setTexCoord", "(FF)V", false);
                        methodNode.instructions.insert(instruction, hook);
                        methodNode.instructions.remove(instruction);
                        changed = true;
                        CoreLoadingPlugin.CLLog.debug("Applied ASM transformation in method " + classNode.name + "." + methodNode.name + " for glTexCoord2f();");
                    }
                }
            }

        }

        if (changed) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            bytes = writer.toByteArray();
        }

        return bytes;
    }

}
