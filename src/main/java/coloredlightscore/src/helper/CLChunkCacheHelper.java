package coloredlightscore.src.helper;

import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;

public class CLChunkCacheHelper {

    public CLChunkCacheHelper() {
    }

    /**
     * Any Light rendered on a 1.8 Block goes through here
     * Light value returned is SSSS BBBB GGGG RRRR LLLL 0000
     * 
     * Modified by CptSpaceToaster
     */
    public static int getLightBrightnessForSkyBlocks(ChunkCache instance, int x, int y, int z, int lightValue) {
        int skyBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        lightValue = ((lightValue & 0xf) | ((lightValue & 0x1e0) >> 1) | ((lightValue & 0x3c00) >> 2) | ((lightValue & 0x78000) >> 3));

        blockBrightness = ((blockBrightness & 0xf) | ((blockBrightness & 0x1e0) >> 1) | ((blockBrightness & 0x3c00) >> 2) | ((blockBrightness & 0x78000) >> 3));

        int block_l = (blockBrightness >> 0) & 0xF;
        int block_r = (blockBrightness >> 4) & 0xF;
        int block_g = (blockBrightness >> 8) & 0xF;
        int block_b = (blockBrightness >> 12) & 0xF;

        int light_l = (lightValue >> 0) & 0xF;
        int light_r = (lightValue >> 4) & 0xF;
        int light_g = (lightValue >> 8) & 0xF;
        int light_b = (lightValue >> 12) & 0xF;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        return skyBrightness << 20 | block_l << 4 | block_r << 8 | block_g << 12 | block_b << 16;
    }

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     * 
     * Modified by CptSpaceToaster
     * 
     * Not present in 1.7.2... where it go?    - heaton84
    public float getLightBrightness(ChunkCache instance, int par1, int par2, int par3)
    {
        return instance.worldObj.provider.lightBrightnessTable[instance.getLightValue(par1, par2, par3)%15];
    }    
     */

}
