package com.lightingoverhaul.mixinmod.helper;

import com.lightingoverhaul.coremod.api.LightingApi;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;

public class ChunkCacheHelper {
    public static int getLightBrightnessForSkyBlocks(ChunkCache instance, int x, int y, int z, int lightValue) {
        int skyBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = instance.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        int light_l = (lightValue >> LightingApi.bitshift_l) & 0xF;
        int light_r = (lightValue >> LightingApi.bitshift_r) & LightingApi.bitmask;
        int light_g = (lightValue >> LightingApi.bitshift_g) & LightingApi.bitmask;
        int light_b = (lightValue >> LightingApi.bitshift_b) & LightingApi.bitmask;

        int block_l = (blockBrightness >> LightingApi.bitshift_l) & 0xF;
        int block_r = (blockBrightness >> LightingApi.bitshift_r) & LightingApi.bitmask;
        int block_g = (blockBrightness >> LightingApi.bitshift_g) & LightingApi.bitmask;
        int block_b = (blockBrightness >> LightingApi.bitshift_b) & LightingApi.bitmask;

        int sun_r = (skyBrightness >> LightingApi.bitshift_sun_r) & LightingApi.bitmask_sun;
        int sun_g = (skyBrightness >> LightingApi.bitshift_sun_g) & LightingApi.bitmask_sun;
        int sun_b = (skyBrightness >> LightingApi.bitshift_sun_b) & LightingApi.bitmask_sun;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        block_r = Math.min(15, block_r);
        block_g = Math.min(15, block_g);
        block_b = Math.min(15, block_b);

        int detectAsRGB = 1 << 30; // Dummy value so tesselator doesn't treat pure blue as vanilla light... This
                                   // will be ignored, except for in Tesselator.setBrightness

        return detectAsRGB | (sun_r << LightingApi.bitshift_sun_r2) | (sun_g << LightingApi.bitshift_sun_g2) | (sun_b << LightingApi.bitshift_sun_b2) | block_l << LightingApi.bitshift_l2 | block_r << LightingApi.bitshift_r2
                | block_g << LightingApi.bitshift_g2 | block_b << LightingApi.bitshift_b2;
    }
}
