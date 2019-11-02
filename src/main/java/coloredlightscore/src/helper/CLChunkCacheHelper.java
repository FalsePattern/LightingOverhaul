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

        int light_l = (lightValue >> 0) & 0xF;
        int light_r = (lightValue >> CLApi.bitshift_r) & 0xF;
        int light_g = (lightValue >> CLApi.bitshift_g) & 0xF;
        int light_b = (lightValue >> CLApi.bitshift_b) & 0xF;

        int block_l = (blockBrightness >> 0) & 0xF;
        int block_r = (blockBrightness >> CLApi.bitshift_r) & 0xF;
        int block_g = (blockBrightness >> CLApi.bitshift_g) & 0xF;
        int block_b = (blockBrightness >> CLApi.bitshift_b) & 0xF;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        return skyBrightness << 20 | block_l << 4 | block_r << 8 | block_g << 12 | block_b << 16;
    }

}
