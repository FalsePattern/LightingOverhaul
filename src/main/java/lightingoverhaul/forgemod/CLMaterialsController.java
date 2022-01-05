package lightingoverhaul.forgemod;

import lightingoverhaul.Tags;
import lightingoverhaul.forgemod.blocks.CLLamp;
import lightingoverhaul.forgemod.blocks.CLStone;
import lightingoverhaul.forgemod.lib.BlockInfo;
import lightingoverhaul.forgemod.items.CLDust;
import lightingoverhaul.forgemod.items.ItemCLBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class CLMaterialsController {
    public static CLLamp CLBlockIdle;
    public static CLLamp CLBlockOn;
    public static Block CLStone;
    public static Item CLDust;

    public static void init() {
        CLBlockIdle = (CLLamp) new CLLamp(false).setBlockName(BlockInfo.CLLamp);
        CLBlockOn = (CLLamp) new CLLamp(true).setBlockName(BlockInfo.CLLamp + "On");
        CLStone = new CLStone().setBlockName(BlockInfo.CLStone);
        CLDust = new CLDust().setUnlocalizedName(BlockInfo.CLDust);
        
        CLBlockIdle.setSwitchBlock(CLBlockOn);
        CLBlockOn.setSwitchBlock(CLBlockIdle);
    }

    public static void registerMaterials() {
        GameRegistry.registerBlock(CLBlockIdle, ItemCLBlock.class, Tags.MODID + BlockInfo.CLLamp);
        GameRegistry.registerBlock(CLBlockOn, ItemCLBlock.class, Tags.MODID + BlockInfo.CLLamp + "On");
        GameRegistry.registerBlock(CLStone, ItemCLBlock.class, Tags.MODID + BlockInfo.CLStone);
        GameRegistry.registerItem(CLDust, Tags.MODID + BlockInfo.CLDust);
        OreDictionary.registerOre(BlockInfo.OD_DUST, new ItemStack(CLDust, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre(BlockInfo.OD_GLOWSTONE, new ItemStack(CLStone, 1, OreDictionary.WILDCARD_VALUE));
        for (int i = 0; i < 16; i++) {
            OreDictionary.registerOre(BlockInfo.OD_DUST + BlockInfo.COLOR[i], new ItemStack(CLDust, 1, i));
            OreDictionary.registerOre(BlockInfo.OD_GLOWSTONE + BlockInfo.COLOR[i], new ItemStack(CLStone, 1, i));
        }
    }

    public static void addRecipes() {
        for (int i = 0; i < 16; i++) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CLDust, 8, i), BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, BlockInfo.OD_DUST, "dye" + BlockInfo.COLOR[i]));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CLStone, 1, i), "cc", "cc",
                    'c', BlockInfo.OD_DUST + BlockInfo.COLOR[i]));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CLBlockIdle, 1, i), " r ", "rsr", " r ",
                    'r', "dustRedstone",
                    's', BlockInfo.OD_GLOWSTONE + BlockInfo.COLOR[i]
            ));
        }
        GameRegistry.addShapelessRecipe(new ItemStack(Items.glowstone_dust, 1), new ItemStack(CLDust, 1, OreDictionary.WILDCARD_VALUE));
    }
}
