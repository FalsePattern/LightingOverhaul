package coloredlightscore.src.asm.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import coloredlightscore.src.asm.transformer.core.HelperMethodTransformer;
import coloredlightscore.src.asm.transformer.core.NameMapper;
import net.minecraft.world.IBlockAccess;

public class TransformBlock extends HelperMethodTransformer {

	String methodsToReplace[] = {
		"getMixedBrightnessForBlock (Lnet/minecraft/world/IBlockAccess;III)I",
	};

    public TransformBlock() {
        // Inform HelperMethodTransformer which class we are interested in
        super("net.minecraft.block.Block");
    }

    @Override
    protected Class<?> getHelperClass() {

        // We should promote a 1:1 correlation between vanilla classes and helper classes
        return coloredlightscore.src.helper.CLBlockHelper.class;
    }

    @Override
    protected boolean transforms(ClassNode classNode, MethodNode methodNode) {
        for (String name : methodsToReplace) {
            if (NameMapper.getInstance().isMethod(methodNode, super.className, name))
            return true;
        }
        if (NameMapper.getInstance().isMethod(methodNode, super.className, "setLightLevel (F)Lnet/minecraft/block/Block;"))
            return true;
        return false;
    }

    @Override
    protected boolean transform(ClassNode classNode, MethodNode methodNode) {
        for (String name : methodsToReplace) {
            if (NameMapper.getInstance().isMethod(methodNode, super.className, name)) {
                return redefineMethod(classNode, methodNode, name);
            }
        }

        if (NameMapper.getInstance().isMethod(methodNode, super.className, "setLightLevel (F)Lnet/minecraft/block/Block;"))
        //if ((methodNode.name + " " + methodNode.desc).equals("setLightLevel (F)Lnet/minecraft/block/Block;"))
        {
            return addReturnMethod(classNode, methodNode, "setLightLevel");
        } else
            return false;
    }
}
