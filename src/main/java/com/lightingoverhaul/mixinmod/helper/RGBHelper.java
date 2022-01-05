package com.lightingoverhaul.mixinmod.helper;

import com.lightingoverhaul.coremod.api.LightingApi;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;

public class RGBHelper {

    public static int computeLightBrightnessForSkyBlocks(int skyBrightness, int blockBrightness, int lightValue) {
        int light_l = LightingApi.extractL(lightValue);
        int light_r = LightingApi.extractR(lightValue);
        int light_g = LightingApi.extractG(lightValue);
        int light_b = LightingApi.extractB(lightValue);

        int block_l = LightingApi.extractL(blockBrightness);
        int block_r = LightingApi.extractR(blockBrightness);
        int block_g = LightingApi.extractG(blockBrightness);
        int block_b = LightingApi.extractB(blockBrightness);

        int sun_r = LightingApi.extractSunR(skyBrightness);
        int sun_g = LightingApi.extractSunG(skyBrightness);
        int sun_b = LightingApi.extractSunB(skyBrightness);

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        block_r = Math.min(15, block_r);
        block_g = Math.min(15, block_g);
        block_b = Math.min(15, block_b);

        int detectAsRGB = 1 << 30; // Dummy value so tesselator doesn't treat pure blue as vanilla light... This
        // will be ignored, except for in Tesselator.setBrightness

        return detectAsRGB | (sun_r << LightingApi._bitshift_sun_r2) | (sun_g << LightingApi._bitshift_sun_g2) | (sun_b << LightingApi._bitshift_sun_b2) | block_l << LightingApi._bitshift_l2 | block_r << LightingApi._bitshift_r2
                | block_g << LightingApi._bitshift_g2 | block_b << LightingApi._bitshift_b2;

    }

    public static float average(int packedChannel) {
        int sunR = (packedChannel) >>> LightingApi._bitshift_sun_r2;
        int sunG = (packedChannel) >>> LightingApi._bitshift_sun_g2;
        int sunB = (packedChannel) >>> LightingApi._bitshift_sun_b2;
        int blockR = (packedChannel) >>> LightingApi._bitshift_r2;
        int blockG = (packedChannel) >>> LightingApi._bitshift_g2;
        int blockB = (packedChannel) >>> LightingApi._bitshift_b2;
        int blockL = (packedChannel) >>> LightingApi._bitshift_l2;

        return (sunR + sunG + sunB + blockR + blockG + blockB + blockL) / 7f;
    }
}
