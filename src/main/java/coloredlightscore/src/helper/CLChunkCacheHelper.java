package coloredlightscore.src.helper;

import coloredlightscore.src.api.CLApi;
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

        int light_l = (lightValue >> CLApi.bitshift_l) & 0xF;
        int light_r = (lightValue >> CLApi.bitshift_r) & CLApi.bitmask;
        int light_g = (lightValue >> CLApi.bitshift_g) & CLApi.bitmask;
        int light_b = (lightValue >> CLApi.bitshift_b) & CLApi.bitmask;

        int block_l = (blockBrightness >> CLApi.bitshift_l) & CLApi.bitmask;
        int block_r = (blockBrightness >> CLApi.bitshift_r) & CLApi.bitmask;
        int block_g = (blockBrightness >> CLApi.bitshift_g) & CLApi.bitmask;
        int block_b = (blockBrightness >> CLApi.bitshift_b) & CLApi.bitmask;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        return skyBrightness << CLApi.bitshift_s2 | block_l << CLApi.bitshift_l2 | block_r << CLApi.bitshift_r2 | block_g << CLApi.bitshift_g2 | block_b << CLApi.bitshift_b2;
    }

}
