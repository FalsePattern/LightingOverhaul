package coloredlightscore.src.asm.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import coloredlightscore.src.asm.transformer.core.HelperMethodTransformer;
import coloredlightscore.src.asm.transformer.core.NameMapper;
import net.minecraft.world.IBlockAccess;

public class TransformEntity extends HelperMethodTransformer {

	String methodsToReplace[] = {
		"getBrightnessForRender (F)I",
	};

    public TransformEntity() {
        // Inform HelperMethodTransformer which class we are interested in
        super("net.minecraft.entity.Entity");
    }

    @Override
    protected Class<?> getHelperClass() {
        // We should promote a 1:1 correlation between vanilla classes and helper classes
        return coloredlightscore.src.helper.CLEntity.class;
    }

    @Override
    protected boolean transforms(ClassNode classNode, MethodNode methodNode) {
        for (String name : methodsToReplace) {
            if (NameMapper.getInstance().isMethod(methodNode, super.className, name))
                return true;
        }
        return false;
    }

    @Override
    protected boolean transform(ClassNode classNode, MethodNode methodNode) {
        for (String name : methodsToReplace) {
            if (NameMapper.getInstance().isMethod(methodNode, super.className, name)) {
                return redefineMethod(classNode, methodNode, name);
            }
        }
        return false;
    }
}
