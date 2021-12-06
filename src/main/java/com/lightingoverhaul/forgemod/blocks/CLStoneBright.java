package com.lightingoverhaul.forgemod.blocks;

import java.util.List;

import com.lightingoverhaul.Tags;
import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.forgemod.lib.BlockInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class CLStoneBright extends Block {
    public CLStoneBright() {
        super(Material.glass);
        setHardness(0.3F);
        setStepSound(soundTypeGlass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @SideOnly(Side.CLIENT)
    private IIcon icons[];

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[16];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(Tags.MODID + ":" + BlockInfo.CLStoneBright + i);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for (int i = 0; i < 16; i++) {
            par3List.add(new ItemStack(par1, 1, i));
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0) {
            // Temporary
            return LightingApi.makeRGBLightValue(15, 15, 15);
        } else {
            return LightingApi.makeRGBLightValue(LightingApi.r[meta] * 2 + 1, LightingApi.g[meta] * 2 + 1, LightingApi.b[meta] * 2 + 1);
        }
    }
}
