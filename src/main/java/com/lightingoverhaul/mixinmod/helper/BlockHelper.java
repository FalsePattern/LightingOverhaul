package com.lightingoverhaul.mixinmod.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockHelper {
    public static int getMixedBrightnessForBlockWithColor(IBlockAccess blockAccess, int x, int y, int z) {
        int l;
        Block block = blockAccess.getBlock(x, y, z);
        if (blockAccess instanceof World)
            l = ((World) blockAccess).getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
        else if (blockAccess instanceof ChunkCache)
            l = RGBHelper.getLightBrightnessForSkyBlocks((ChunkCache) blockAccess, x, y, z, block.getLightValue(blockAccess, x, y, z));
        else
            l = 0;

        if (l == 0 && block instanceof BlockSlab) {
            --y;
            block = blockAccess.getBlock(x, y, z);
            if (blockAccess instanceof World)
                return ((World) blockAccess).getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(blockAccess, x, y, z));
            else if (blockAccess instanceof ChunkCache)
                return RGBHelper.getLightBrightnessForSkyBlocks((ChunkCache) blockAccess, x, y, z, block.getLightValue(blockAccess, x, y, z));
            else
                return 0;
        } else {
            return l;
        }
    }
}
