package lightingoverhaul.mixin.mixins.client.minecraft;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.LightingOverhaul;
import lightingoverhaul.mixin.interfaces.ITessellatorMixin;
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
            if ((brightness & (1 << 30)) != (1 << 30)) {
                brightness = LightingApi.convertLightMapCoordsToPackedLight(x, y);
            }
            /*
             * brightness is of the form 0100 0000 bbbb gggg rrrr BBBB GGGG RRRR and needs
             * to be decomposed.
             */
            int block_r = LightingApi.extractR(brightness);
            int block_g = LightingApi.extractG(brightness);
            int block_b = LightingApi.extractB(brightness);
            int l = Math.max(Math.max(block_r, block_g), block_b);
            if (LightingOverhaul.emissivesEnabled) {
                block_r = l;
                block_g = l;
                block_b = l;
            }

            int sun_r = LightingApi.extractSunR(brightness);
            int sun_g = LightingApi.extractSunG(brightness);
            int sun_b = LightingApi.extractSunB(brightness);

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
