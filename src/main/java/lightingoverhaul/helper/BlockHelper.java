package lightingoverhaul.helper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public class BlockHelper {
    public static int getMixedBrightnessForBlockWithColor(IBlockAccess blockAccess, int x, int y, int z) {
        int l;
        Block block = blockAccess.getBlock(x, y, z);
        l = blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));

        if (l == 0 && block instanceof BlockSlab) {
            --y;
            block = blockAccess.getBlock(x, y, z);
            return blockAccess.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
        } else {
            return l;
        }
    }
}
