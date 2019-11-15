package com.darkshadow44.lightoverhaul.helper;

import coloredlightscore.src.api.CLApi;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;

public class ChunkCacheHelper {
    public static int getLightBrightnessForSkyBlocks(ChunkCache instance, int x, int y, int z, int lightValue) {
        int skyBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        int light_l = (lightValue >> CLApi.bitshift_l) & 0xF;
        int light_r = (lightValue >> CLApi.bitshift_r) & CLApi.bitmask;
        int light_g = (lightValue >> CLApi.bitshift_g) & CLApi.bitmask;
        int light_b = (lightValue >> CLApi.bitshift_b) & CLApi.bitmask;

        int block_l = (blockBrightness >> CLApi.bitshift_l) & 0xF;
        int block_r = (blockBrightness >> CLApi.bitshift_r) & CLApi.bitmask;
        int block_g = (blockBrightness >> CLApi.bitshift_g) & CLApi.bitmask;
        int block_b = (blockBrightness >> CLApi.bitshift_b) & CLApi.bitmask;

        int sun_r = (skyBrightness >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
        int sun_g = (skyBrightness >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
        int sun_b = (skyBrightness >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        return (sun_r << CLApi.bitshift_sun_r2) | (sun_g << CLApi.bitshift_sun_g2) | (sun_b << CLApi.bitshift_sun_b2) | block_l << CLApi.bitshift_l2 | block_r << CLApi.bitshift_r2
                | block_g << CLApi.bitshift_g2 | block_b << CLApi.bitshift_b2;
    }
}
