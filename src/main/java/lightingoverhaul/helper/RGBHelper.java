package lightingoverhaul.helper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lightingoverhaul.api.LightingApi;

@SideOnly(Side.CLIENT)
public class RGBHelper {

    public static int computeLightBrightnessForSkyBlocks(int skyBrightness, int blockBrightness, int lightValue) {
        int light_r = LightingApi.extractR(lightValue);
        int light_g = LightingApi.extractG(lightValue);
        int light_b = LightingApi.extractB(lightValue);

        int block_r = LightingApi.extractR(blockBrightness);
        int block_g = LightingApi.extractG(blockBrightness);
        int block_b = LightingApi.extractB(blockBrightness);

        int sun_r = LightingApi.extractSunR(skyBrightness);
        int sun_g = LightingApi.extractSunG(skyBrightness);
        int sun_b = LightingApi.extractSunB(skyBrightness);

        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        return LightingApi.toRenderLight(block_r, block_g, block_b, sun_r, sun_g, sun_b);
    }
}
