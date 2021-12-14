package com.lightingoverhaul.mixinmod.mixins;

import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    public Minecraft mc;

    @Shadow
    public float getNightVisionBrightness(EntityPlayer paramEntityPlayer, float paramFloat) {
        return 0;
    }

    @Inject(method = "updateLightmap",
            at = @At(value = "HEAD"),
            require = 1)
    private void updateUniforms(float partialTickTime, CallbackInfo ci) {
        WorldClient worldclient = this.mc.theWorld;
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        if (worldclient != null) {
            float nightVisionWeight = 0;
            if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                nightVisionWeight = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
            }
            float sunlightBase = worldclient.getSunBrightness(partialTickTime);
            if (worldclient.lastLightningBolt > 0) {
                sunlightBase = 1.0f;
            }
            float gamma;

            gamma = this.mc.gameSettings.gammaSetting;
            tessellatorMixin.updateShaders(gamma, sunlightBase, nightVisionWeight);
        }
    }

    @Inject(method = "enableLightmap",
            at = @At(value = "RETURN"),
            require = 1)
    public void enableShader(CallbackInfo ci) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.enableShader();
    }

    @Inject(method = "disableLightmap",
            at = @At(value = "HEAD"),
            require = 1)
    public void disableShader(CallbackInfo ci) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.disableShader();
    }
}
