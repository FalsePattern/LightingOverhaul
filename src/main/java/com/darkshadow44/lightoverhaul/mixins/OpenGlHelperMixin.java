package com.darkshadow44.lightoverhaul.mixins;

import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.darkshadow44.lightoverhaul.interfaces.ITessellatorMixin;

import coloredlightscore.src.api.CLApi;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

@Mixin(OpenGlHelper.class)
public abstract class OpenGlHelperMixin {

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public static void setLightmapTextureCoords(int textureId, float x, float y) {

        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        if (tessellatorMixin.isProgramInUse()) {
            int brightness = ((int) y << 16) + (int) x;
            /*
             * brightness is of the form 0000 0000 SSSS BBBB GGGG RRRR LLLL 0000 and needs
             * to be decomposed.
             */
            int block_b = (brightness >> CLApi.bitshift_b2) & 0xF;
            int block_g = (brightness >> CLApi.bitshift_g2) & 0xF;
            int block_r = (brightness >> CLApi.bitshift_r2) & 0xF;
            int l = (brightness >> CLApi.bitshift_l2) & 0xF;
            if (l > block_r && l > block_g && l > block_b) {
                block_r = block_g = block_b = l;
            }

            int sun_r = (brightness >> CLApi.bitshift_sun_r2) & CLApi.bitmask_sun;
            int sun_g = (brightness >> CLApi.bitshift_sun_g2) & CLApi.bitmask_sun;
            int sun_b = (brightness >> CLApi.bitshift_sun_b2) & CLApi.bitmask_sun;

            GL20.glUniform4i(tessellatorMixin.getLightCoordUniform(), block_r, block_g, block_b, 0);
            GL20.glUniform4i(tessellatorMixin.getLightCoordSunUniform(), sun_r, sun_g, sun_b, 0);
        } // else noop; why is this ever called if enableLightmap hasn't been called?

        OpenGlHelper.lastBrightnessX = x;
        OpenGlHelper.lastBrightnessY = y;
    }
}
