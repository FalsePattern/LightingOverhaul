package lightingoverhaul.helper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lightingoverhaul.api.LightingApi;

@SideOnly(Side.CLIENT)
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

        return LightingApi.toRenderLight(block_r, block_g, block_b, block_l, sun_r, sun_g, sun_b);
    }
}
