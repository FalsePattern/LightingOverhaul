package lightingoverhaul.coremod.mixin.mixins.client.minecraft;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.coremod.asm.LightingOverhaul;
import lightingoverhaul.coremod.mixin.interfaces.ITessellatorMixin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenGlHelper.class)
@SideOnly(Side.CLIENT)
public abstract class OpenGLHelperMixin {

    @Shadow public static int lightmapTexUnit;

    @Inject(method = "setLightmapTextureCoords",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void setLightmapTextureCoords(int textureID, float x, float y, CallbackInfo ci) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        if (tessellatorMixin.isProgramInUse()) {
            ci.cancel();
            int brightness = ((int) y << 16) + (int) x;
            /*
             * brightness is of the form 0000 0000 SSSS BBBB GGGG RRRR LLLL 0000 and needs
             * to be decomposed.
             */
            int block_b = (brightness >> LightingApi._bitshift_b2) & 0xF;
            int block_g = (brightness >> LightingApi._bitshift_g2) & 0xF;
            int block_r = (brightness >> LightingApi._bitshift_r2) & 0xF;
            int l = (brightness >> LightingApi._bitshift_l2) & 0xF;
            if (LightingOverhaul.emissivesEnabled) {
                block_r = Math.max(block_r, l);
                block_g = Math.max(block_g, l);
                block_b = Math.max(block_b, l);
            } else {
                if (l > block_r && l > block_g && l > block_b) {
                    block_r = block_g = block_b = l;
                }
            }

            int sun_r = (brightness >> LightingApi._bitshift_sun_r2) & LightingApi._bitmask_sun;
            int sun_g = (brightness >> LightingApi._bitshift_sun_g2) & LightingApi._bitmask_sun;
            int sun_b = (brightness >> LightingApi._bitshift_sun_b2) & LightingApi._bitmask_sun;

            tessellatorMixin.getShader().lightCoordUniform.set(block_r, block_g, block_b, 0);
            tessellatorMixin.getShader().lightCoordSunUniform.set(sun_r, sun_g, sun_b, 0);
            if (textureID == lightmapTexUnit) {
                OpenGlHelper.lastBrightnessX = x;
                OpenGlHelper.lastBrightnessY = y;
            }
        }
        // else default behaviour; why is this ever called if enableLightmap hasn't been called? (probably a mod's custom lightmap)
    }
}
