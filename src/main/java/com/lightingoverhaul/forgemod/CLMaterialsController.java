package com.lightingoverhaul.forgemod;

import com.lightingoverhaul.Tags;
import com.lightingoverhaul.forgemod.blocks.CLLamp;
import com.lightingoverhaul.forgemod.blocks.CLStone;
import com.lightingoverhaul.forgemod.lib.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.lightingoverhaul.forgemod.items.CLDust;
import com.lightingoverhaul.forgemod.items.ItemCLBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static com.lightingoverhaul.forgemod.lib.BlockInfo.*;

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
        OreDictionary.registerOre(OD_DUST, new ItemStack(CLDust, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre(OD_GLOWSTONE, new ItemStack(CLStone, 1, OreDictionary.WILDCARD_VALUE));
        for (int i = 0; i < 16; i++) {
            OreDictionary.registerOre(OD_DUST + COLOR[i], new ItemStack(CLDust, 1, i));
            OreDictionary.registerOre(OD_GLOWSTONE + COLOR[i], new ItemStack(CLStone, 1, i));
        }
    }

    public static void addRecipes() {
        for (int i = 0; i < 16; i++) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(CLDust, 8, i), OD_DUST, OD_DUST, OD_DUST, OD_DUST, OD_DUST, OD_DUST, OD_DUST, OD_DUST, "dye" + COLOR[i]));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CLStone, 1, i), "cc", "cc",
                    'c', OD_DUST + COLOR[i]));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CLBlockIdle, 1, i), " r ", "rsr", " r ",
                    'r', "dustRedstone",
                    's', OD_GLOWSTONE + COLOR[i]
            ));
        }
        GameRegistry.addShapelessRecipe(new ItemStack(Items.glowstone_dust, 1), new ItemStack(CLDust, 1, OreDictionary.WILDCARD_VALUE));
    }
}
