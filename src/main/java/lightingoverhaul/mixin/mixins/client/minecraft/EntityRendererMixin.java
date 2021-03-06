package lightingoverhaul.mixin.mixins.client.minecraft;

import lightingoverhaul.mixin.interfaces.ITessellatorMixin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class EntityRendererMixin {

    @Shadow
    public Minecraft mc;

    @Shadow
    public float getNightVisionBrightness(EntityPlayer paramEntityPlayer, float paramFloat) {
        return 0;
    }

    private static float mapSunR(float sunLightBase) {
        return 0.1f + 0.9f * sunLightBase;
    }
    private static float mapSunG(float sunLightBase) {
        return 0.1f + 0.9f * sunLightBase;
    }
    private static float mapSunB(float sunLightBase) {
        return 0.3f + 0.9f * sunLightBase;
    }

    @Inject(method = "updateLightmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void updateUniforms(float partialTickTime, CallbackInfo ci) {
        ci.cancel();
        WorldClient worldclient = this.mc.theWorld;
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        if (worldclient != null) {
            float nightVisionWeight = 0;
            if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                nightVisionWeight = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
            }
            float sunlightBase = worldclient.getSunBrightness(partialTickTime);
            sunlightBase = (sunlightBase * 1.25f) - 0.2f;
            sunlightBase = Math.max(0, Math.min(1, sunlightBase));
            if (worldclient.lastLightningBolt > 0) {
                sunlightBase = 1.0f;
            }
            float gamma;

            gamma = this.mc.gameSettings.gammaSetting;
            tessellatorMixin.updateShaders(gamma, sunlightBase, sunlightBase, sunlightBase, nightVisionWeight);
        }
    }

    @Redirect(method = "enableLightmap",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/OpenGlHelper;setActiveTexture(I)V",
                       ordinal = 1),
              require = 1)
    public void enableShader(int p_77473_0_) {
        OpenGlHelper.setActiveTexture(p_77473_0_);
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.enableShader();
    }

    @Redirect(method = "disableLightmap",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/OpenGlHelper;setActiveTexture(I)V",
                     ordinal = 0),
            require = 1)
    public void disableShader(int p_77473_0_) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.disableShader();
        OpenGlHelper.setActiveTexture(p_77473_0_);
    }
}
