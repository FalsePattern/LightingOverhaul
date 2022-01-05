package lightingoverhaul.forgemod.blocks;

import java.util.List;
import java.util.Random;

import lightingoverhaul.Tags;
import lightingoverhaul.coremod.api.LightingApi;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CLLamp extends Block {
    /** Whether this lamp block is the powered version of the block. */
    protected final boolean powered;
    /** The Block the lamp is supposed to switch to **/
    protected Block switchBlock = null;

    public CLLamp(boolean isPowered) {
        super(Material.redstoneLight);
        this.powered = isPowered;

        setHardness(0.3F);
        setStepSound(soundTypeGlass);

        if (isPowered)
            setLightLevel(LightingApi.makeRGBLightValue(1f, 1f, 1f)); //Placeholder value, doesn't do anything
        else
            setCreativeTab(CreativeTabs.tabDecorations);
    }

    public void setSwitchBlock(Block switchBlock) {
        this.switchBlock = switchBlock;
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) { // registerIcons()
        icons = new IIcon[16];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(Tags.MODID + ":" + BlockInfo.CLLamp + (powered ? "On" : "") + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[meta];
    }

    @Override
    public String getUnlocalizedName() {
        return "tile." + BlockInfo.CLLamp;
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return Item.getItemFromBlock((powered) ? switchBlock : this);
    }

    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return Item.getItemFromBlock((powered) ? switchBlock : this);
    }

    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack((powered) ? switchBlock : this);
    }

    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
            if (this.powered && !world.isBlockIndirectlyGettingPowered(x, y, z)) {
                world.scheduleBlockUpdate(x, y, z, this, 4);
            } else if (!this.powered && world.isBlockIndirectlyGettingPowered(x, y, z)) {
                int temp = world.getBlockMetadata(x, y, z);
                world.setBlock(x, y, z, switchBlock, 0, 0);
                world.setBlockMetadataWithNotify(x, y, z, temp, 2);
            }
        }
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which
     * neighbor changed (coordinates passed are their own) Args: x, y, z, neighbor
     * blockID
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (!world.isRemote) {
            if (this.powered && !world.isBlockIndirectlyGettingPowered(x, y, z)) {
                world.scheduleBlockUpdate(x, y, z, this, 4);
            } else if (!this.powered && world.isBlockIndirectlyGettingPowered(x, y, z)) {
                int temp = world.getBlockMetadata(x, y, z);
                world.setBlock(x, y, z, switchBlock, 0, 0);
                world.setBlockMetadataWithNotify(x, y, z, temp, 2);
            }
        }
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote && this.powered && !world.isBlockIndirectlyGettingPowered(x, y, z)) {
            int temp = world.getBlockMetadata(x, y, z);
            world.setBlock(x, y, z, switchBlock, 0, 0);
            world.setBlockMetadataWithNotify(x, y, z, temp, 2);
        }
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

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (powered) {
            return LightingApi.makeRGBLightValue(LightingApi.r[meta], LightingApi.g[meta], LightingApi.b[meta]);
        } else {
            return 0;
        }
    }
}
