package lightingoverhaul.forgemod.blocks;

import java.util.List;
import java.util.Random;

import lightingoverhaul.Tags;
import lightingoverhaul.forgemod.CLMaterialsController;
import lightingoverhaul.forgemod.lib.BlockInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

public class CLStone extends Block {
    public CLStone() {
        super(Material.glass);
        setHardness(0.3F);
        setStepSound(soundTypeGlass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setLightLevel(1f); //Placeholder value, doesn't do anything. Real light value applied from configs.
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[16];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(Tags.MODID + ":" + BlockInfo.CLStone + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[meta];
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int i = 0; i < 16; i++) {
            par3List.add(new ItemStack(par1, 1, i));
        }
    }

    /**
     * Returns the usual quantity dropped by the block plus a bonus of 1 to 'i'
     * (inclusive).
     */
    public int quantityDroppedWithBonus(int p_149679_1_, Random p_149679_2_) {
        return MathHelper.clamp_int(this.quantityDropped(p_149679_2_) + p_149679_2_.nextInt(p_149679_1_ + 1), 1, 4);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random p_149745_1_) {
        return 2 + p_149745_1_.nextInt(3);
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return CLMaterialsController.CLDust;
    }
}
