package coloredlightscore.src.asm.transformer;

import static coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin.CLLog;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import coloredlightscore.src.api.CLApi;
import coloredlightscore.src.asm.transformer.core.ASMUtils;
import coloredlightscore.src.asm.transformer.core.MethodTransformer;
import coloredlightscore.src.asm.transformer.core.NameMapper;

/**
 * Fields added to net.minecraft.world.chunk.storage.ExtendedBlockStorage:
 *   NibbleArray rColorArray
 *   NibbleArray gColorArray
 *   NibbleArray bColorArray
 *   NibbleArray rColorArray2
 *   NibbleArray gColorArray2
 *   NibbleArray bColorArray2
 *   
 * Methods added to net.minecraft.world.chunk.storage.ExtendedBlockStorage:
 *   setRedColorArray
 *   setGreenColorArray
 *   setBlueColorArray
 *   getRedColorArray
 *   getGreenColorArray
 *   getBlueColorArray
 *   setRedColorArray2
 *   setGreenColorArray2
 *   setBlueColorArray2
 *   getRedColorArray2
 *   getGreenColorArray2
 *   getBlueColorArray2
 * 
 * Methods modified on net.minecraft.world.chunk.storage.ExtendedBlockStorage:
 *   setExtBlocklightValue
 *   getExtBlocklightValue
 * 
 * @author Josh
 *
 */
public class TransformExtendedBlockStorage extends MethodTransformer {

    private boolean addedFields = false;
    private FieldNode rColorArray;
    private FieldNode gColorArray;
    private FieldNode bColorArray;
    private FieldNode rColorArray2;
    private FieldNode gColorArray2;
    private FieldNode bColorArray2;
    private FieldNode blockLSBArray;

    private final String NIBBLE_ARRAY = "Lnet.minecraft.world.chunk.NibbleArray;";
    private final String EBS_CLASSNAME = "net.minecraft.world.chunk.storage.ExtendedBlockStorage";
    private final String SET_BLOCK_LIGHT_VALUE = "setExtBlocklightValue (IIII)V";
    private final String GET_BLOCK_LIGHT_VALUE = "getExtBlocklightValue (III)I";

    private boolean addedMethods = false;

    @Override
    protected boolean transforms(ClassNode clazz, MethodNode method) {

        return NameMapper.getInstance().isMethod(method, EBS_CLASSNAME, SET_BLOCK_LIGHT_VALUE) | NameMapper.getInstance().isMethod(method, EBS_CLASSNAME, GET_BLOCK_LIGHT_VALUE)
                | method.name.equals("<init>");
    }

    @Override
    protected boolean transform(ClassNode clazz, MethodNode method) {

        boolean changed = false;

        if (!addedFields) {
            addRGBNibbleArrays(clazz);
            changed = true;
        }

        if (NameMapper.getInstance().isMethod(method, EBS_CLASSNAME, SET_BLOCK_LIGHT_VALUE)) {
            transformSetExtBlocklightValue(clazz, method);
            changed = true;
        }

        if (NameMapper.getInstance().isMethod(method, EBS_CLASSNAME, GET_BLOCK_LIGHT_VALUE)) {
            transformGetExtBlocklightValue(clazz, method);
            changed = true;
        }

        if (method.name.equals("<init>")) {
            transformConstructor(clazz, method);
            changed = true;
        }

        return changed;
    }

    @Override
    protected boolean postTransformClass(ClassNode clazz) {
        if (!addedMethods) {
            addRGBNibbleArrayMethods(clazz);
            return true;
        } else
            return false;
    }

    @Override
    protected boolean transforms(String className) {
        //return className.equals(NameMapper.getInstance().getClassName(EBS_CLASSNAME).replace('/', '.'));
        return className.equals(EBS_CLASSNAME);
    }

    private void addRGBNibbleArrays(ClassNode clazz) {
        Type typeNibbleArray = NameMapper.getInstance().getType(NIBBLE_ARRAY);
        //Type typeNibbleArray = Type.getType(net.minecraft.world.chunk.NibbleArray.class);

        for (FieldNode f : clazz.fields)
            if (f.name.equals("blockLSBArray") || f.name.equals("field_76680_d") || (f.name.equals("d") && f.desc.equals("[B")))
                blockLSBArray = f;

        if (blockLSBArray == null)
            CLLog.error("TransformExtendedBlockStorage: Failed to find blockLSBArray!");

        rColorArray = new FieldNode(Opcodes.ACC_PUBLIC, "rColorArray", typeNibbleArray.getDescriptor(), null, null);
        gColorArray = new FieldNode(Opcodes.ACC_PUBLIC, "gColorArray", typeNibbleArray.getDescriptor(), null, null);
        bColorArray = new FieldNode(Opcodes.ACC_PUBLIC, "bColorArray", typeNibbleArray.getDescriptor(), null, null);
        rColorArray2 = new FieldNode(Opcodes.ACC_PUBLIC, "rColorArray2", typeNibbleArray.getDescriptor(), null, null);
        gColorArray2 = new FieldNode(Opcodes.ACC_PUBLIC, "gColorArray2", typeNibbleArray.getDescriptor(), null, null);
        bColorArray2 = new FieldNode(Opcodes.ACC_PUBLIC, "bColorArray2", typeNibbleArray.getDescriptor(), null, null);

        clazz.fields.add(rColorArray);
        clazz.fields.add(gColorArray);
        clazz.fields.add(bColorArray);
        clazz.fields.add(rColorArray2);
        clazz.fields.add(gColorArray2);
        clazz.fields.add(bColorArray2);

        CLLog.info("Added RGB color arrays to ExtendedBlockStorage, type " + typeNibbleArray.getDescriptor());

        addedFields = true;
    }

    private boolean addRGBNibbleArrayMethods(ClassNode clazz) {
        if (addedFields && !addedMethods) {
            // These new methods are required for storing/loading the new nibble arrays to disk

            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setRedColorArray", rColorArray.name, rColorArray.desc));
            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setGreenColorArray", gColorArray.name, gColorArray.desc));
            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setBlueColorArray", bColorArray.name, bColorArray.desc));
            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setRedColorArray2", rColorArray2.name, rColorArray2.desc));
            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setGreenColorArray2", gColorArray2.name, gColorArray2.desc));
            clazz.methods.add(ASMUtils.generateSetterMethod(clazz.name, "setBlueColorArray2", bColorArray2.name, bColorArray2.desc));

            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getRedColorArray", rColorArray.name, rColorArray.desc));
            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getGreenColorArray", gColorArray.name, gColorArray.desc));
            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getBlueColorArray", bColorArray.name, bColorArray.desc));
            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getRedColorArray2", rColorArray2.name, rColorArray2.desc));
            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getGreenColorArray2", gColorArray2.name, gColorArray2.desc));
            clazz.methods.add(ASMUtils.generateGetterMethod(clazz.name, "getBlueColorArray2", bColorArray2.name, bColorArray2.desc));

            addedMethods = true;
        }

        return addedMethods;
    }

    private void transformConstructor_add_color_array(ClassNode clazz, InsnList instructions, FieldNode array)
    {
        String ebsInternalName = clazz.name;
        Type typeNibbleArray = NameMapper.getInstance().getType(NIBBLE_ARRAY);

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new TypeInsnNode(Opcodes.NEW, typeNibbleArray.getInternalName()));
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, ebsInternalName, blockLSBArray.name, blockLSBArray.desc));
        instructions.add(new InsnNode(Opcodes.ARRAYLENGTH));
        instructions.add(new InsnNode(Opcodes.ICONST_4));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, typeNibbleArray.getInternalName(), "<init>", "(II)V"));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, ebsInternalName, array.name, array.desc));
    }

    private void transformConstructor(ClassNode clazz, MethodNode m) {
        // Initializes array the same length as blockLSBArray:
        //	    18  aload_0 [this]
        // 	    19  new net.minecraft.world.chunk.NibbleArray [4]
        // 	    22  dup
        //		23  aload_0 [this]
        //	    24  getfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.blockLSBArray : byte[] [3]
        //	    27  arraylength
        //	    28  iconst_4
        //	    29  invokespecial net.minecraft.world.chunk.NibbleArray(int, int) [5]
        //	    32  putfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.blockMetadataArray : net.minecraft.world.chunk.NibbleArray [6]

        // Remove the return, to be re-inserted later

        AbstractInsnNode returnNode = ASMUtils.findLastReturn(m);

        if (returnNode == null) {
            CLLog.error(String.format("Failed to find RETURN statement on {}/{} {}", clazz.name, m.name, m.desc));
        } else
            m.instructions.remove(returnNode);

        // Initialize rColorArray
        transformConstructor_add_color_array(clazz, m.instructions, rColorArray);
        transformConstructor_add_color_array(clazz, m.instructions, rColorArray2);
        transformConstructor_add_color_array(clazz, m.instructions, gColorArray);
        transformConstructor_add_color_array(clazz, m.instructions, gColorArray2);
        transformConstructor_add_color_array(clazz, m.instructions, bColorArray);
        transformConstructor_add_color_array(clazz, m.instructions, bColorArray2);

        m.instructions.add(returnNode);
    }

    private void transformSetExtBlocklightValue_store_array(ClassNode clazz, InsnList instructions, FieldNode array, int shift, boolean extended)
    {
        String ebsInternalName = clazz.name;
        Type typeNibbleArray = NameMapper.getInstance().getType(NIBBLE_ARRAY);
        String nibbleArraySet = NameMapper.getInstance().getMethodName("net/minecraft/world/chunk/NibbleArray", "set (IIII)V");

        // Colored light mod: Store color value
        //      12  aload_0 [this]
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        //      13  getfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.rColorArray : net.minecraft.world.chunk.NibbleArray [98]
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, ebsInternalName, array.name, array.desc));
        //      16  iload_1 [x]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        //      17  iload_2 [y]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        //      18  iload_3 [z]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        //      19  iload 4 [lightValue]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
        //      21  bipush
        instructions.add(new IntInsnNode(Opcodes.BIPUSH, shift));
        //      22  ishr
        instructions.add(new InsnNode(Opcodes.ISHR));
        //      23  bipush
        instructions.add(new IntInsnNode(Opcodes.BIPUSH, CLApi.bitmask));
        //      25  iand
        instructions.add(new InsnNode(Opcodes.IAND));
        if (extended)
        {
            instructions.add(new InsnNode(Opcodes.ICONST_4));
            instructions.add(new InsnNode(Opcodes.ISHR));
            instructions.add(new IntInsnNode(Opcodes.BIPUSH, CLApi.bitmask >> 4));
            instructions.add(new InsnNode(Opcodes.IAND));
        }
        //      26  invokevirtual net.minecraft.world.chunk.NibbleArray.set(int, int, int, int) : void [93]
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, typeNibbleArray.getInternalName(), nibbleArraySet, "(IIII)V"));
    }

    private void transformSetExtBlocklightValue(ClassNode clazz, MethodNode m) {
        // Already in stock method:
        //		 0  aload_0 [this]
        //		 1  getfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.blocklightArray : net.minecraft.world.chunk.NibbleArray [91]
        //		 4  iload_1 [x]
        //		 5  iload_2 [y]
        //		 6  iload_3 [z]
        //		 7  iload 4 [lightValue]
        //		 9  invokevirtual net.minecraft.world.chunk.NibbleArray.set(int, int, int, int) : void [93]
        // return is there by default - remove for now

        AbstractInsnNode oldReturn = ASMUtils.findLastReturn(m);

        if (oldReturn != null)
            m.instructions.remove(oldReturn);

        transformSetExtBlocklightValue_store_array(clazz, m.instructions, rColorArray, CLApi.bitshift_r, false);
        transformSetExtBlocklightValue_store_array(clazz, m.instructions, rColorArray2, CLApi.bitshift_r, true);
        transformSetExtBlocklightValue_store_array(clazz, m.instructions, gColorArray, CLApi.bitshift_g, false);
        transformSetExtBlocklightValue_store_array(clazz, m.instructions, gColorArray2, CLApi.bitshift_g, true);
        transformSetExtBlocklightValue_store_array(clazz, m.instructions, bColorArray, CLApi.bitshift_b, false);
        transformSetExtBlocklightValue_store_array(clazz, m.instructions, bColorArray2, CLApi.bitshift_b, true);

        //		65  return
        m.instructions.add(new InsnNode(Opcodes.RETURN));

    }

    private void transformGetExtBlocklightValue_load_array(ClassNode clazz, InsnList instructions, FieldNode array, int shift, boolean extended)
    {
        String ebsInternalName = clazz.name;
        Type typeNibbleArray = NameMapper.getInstance().getType(NIBBLE_ARRAY);
        String nibbleArrayGet = NameMapper.getInstance().getMethodName("net/minecraft/world/chunk/NibbleArray", "get (III)I");

        //      10  aload_0 [this]
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        //      11  getfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.rColorArray : net.minecraft.world.chunk.NibbleArray [98]
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, ebsInternalName, array.name, array.desc));
        //      14  iload_1 [par1]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        //      15  iload_2 [par2]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        //      16  iload_3 [par3]
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        //      17  invokevirtual net.minecraft.world.chunk.NibbleArray.get(int, int, int) : int [110]
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, typeNibbleArray.getInternalName(), nibbleArrayGet, "(III)I"));
        //      20  bipush
        instructions.add(new IntInsnNode(Opcodes.BIPUSH, shift));
        //      21  ishl
        instructions.add(new InsnNode(Opcodes.ISHL));
        if (extended)
        {
            instructions.add(new InsnNode(Opcodes.ICONST_4));
            instructions.add(new InsnNode(Opcodes.ISHL));
        }

        //      22  ior
        instructions.add(new InsnNode(Opcodes.IOR));
    }

    private void transformGetExtBlocklightValue(ClassNode clazz, MethodNode m) {
        // Already in stock method:
        //       0  aload_0 [this]
        //       1  getfield net.minecraft.world.chunk.storage.ExtendedBlockStorage.blocklightArray : net.minecraft.world.chunk.NibbleArray [91]
        //       4  iload_1 [par1]
        //       5  iload_2 [par2]
        //       6  iload_3 [par3]
        //       7  invokevirtual net.minecraft.world.chunk.NibbleArray.get(int, int, int) : int [110]
        // ireturn is there by default - remove for now
        AbstractInsnNode returnNode = ASMUtils.findLastReturn(m);

        if (returnNode != null)
            m.instructions.remove(returnNode);

        transformGetExtBlocklightValue_load_array(clazz, m.instructions, rColorArray, CLApi.bitshift_r, false);
        transformGetExtBlocklightValue_load_array(clazz, m.instructions, rColorArray2, CLApi.bitshift_r, true);
        transformGetExtBlocklightValue_load_array(clazz, m.instructions, gColorArray, CLApi.bitshift_g, false);
        transformGetExtBlocklightValue_load_array(clazz, m.instructions, gColorArray2, CLApi.bitshift_g, true);
        transformGetExtBlocklightValue_load_array(clazz, m.instructions, bColorArray, CLApi.bitshift_b, false);
        transformGetExtBlocklightValue_load_array(clazz, m.instructions, bColorArray2, CLApi.bitshift_b, true);

        if (returnNode != null)
            //      51  ireturn
            m.instructions.add(returnNode);
    }

}
