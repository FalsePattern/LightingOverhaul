package coloredlightscore.src.helper;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL20;

import coloredlightscore.src.api.CLApi;

/**
 * Created by Murray on 11/30/2014.
 */
public class CLOpenGlHelper {
    public static void setLightmapTextureCoords(int textureId, float x, float y) {
        if (CLTessellatorHelper.isProgramInUse()) {
            int brightness = ((int) y << 16) + (int) x;
            /*
                brightness is of the form
                0000 0000 SSSS BBBB GGGG RRRR LLLL 0000
                and needs to be decomposed.
             */
            int s = (brightness >> CLApi.bitshift_s2) & 0xF;
            int block_b = (brightness >> CLApi.bitshift_b2) & CLApi.bitmask;
            int block_g = (brightness >> CLApi.bitshift_g2) & CLApi.bitmask;
            int block_r = (brightness >> CLApi.bitshift_r2) & CLApi.bitmask;
            int l = (brightness >> CLApi.bitshift_l2) & 0xF;
            if (l > block_r && l > block_g && l > block_b) {
                block_r = block_g = block_b = l;
            }

            block_r = Math.min(15, block_r);
            block_g = Math.min(15, block_g);
            block_b = Math.min(15, block_b);

            int sun_r = s;
            int sun_g = s;
            int sun_b = s;

            GL20.glUniform4i(CLTessellatorHelper.lightCoordUniform, block_r, block_g, block_b, 0);
            GL20.glUniform4i(CLTessellatorHelper.lightCoordSunUniform, sun_r, sun_g, sun_b, 0);
        } // else noop; why is this ever called if enableLightmap hasn't been called?

        OpenGlHelper.lastBrightnessX = x;
        OpenGlHelper.lastBrightnessY = y;
    }
}
