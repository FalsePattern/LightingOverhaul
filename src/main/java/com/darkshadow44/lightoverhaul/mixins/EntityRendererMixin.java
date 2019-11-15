package com.darkshadow44.lightoverhaul.mixins;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.darkshadow44.lightoverhaul.interfaces.ITessellatorMixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    private ResourceLocation locationLightMap;

    @Shadow
    private Minecraft mc;

    @Shadow
    private boolean lightmapUpdateNeeded;

    @Shadow
    private float getNightVisionBrightness(EntityPlayer paramEntityPlayer, float paramFloat) {
        return 0;
    }

    @Shadow
    private DynamicTexture lightmapTexture;

    private static final float f = (1.0F / 4096.0F);
    private static final float t = 8.0f;
    private static final float nightVisionMinBrightness = 0.7f;
    private static boolean ignoreNextEnableLightmap;

    @Overwrite
    public void updateLightmap(float partialTickTime) {
        WorldClient worldclient = this.mc.theWorld;
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        float min = 0.05F;
        float max = 1.0F;
        float nightVisionWeight = 0;
        if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
            nightVisionWeight = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
            min = min * (1.0f - nightVisionWeight) + nightVisionMinBrightness * nightVisionWeight;
        }

        if (worldclient != null) {
            int[] map = new int[16 * 16 * 16 * 16];
            float sunlightBase = worldclient.getSunBrightness(partialTickTime);
            if (worldclient.lastLightningBolt > 0) {
                sunlightBase = 1.0f;
            }
            float sunlight, bSunlight, gSunlight, rSunlight, bLight, gLight, rLight, gamma;

            gamma = this.mc.gameSettings.gammaSetting;
            tessellatorMixin.updateShaders(gamma, sunlightBase, nightVisionWeight);
            for (int s = 0; s < 16; s++) {
                sunlight = sunlightBase * worldclient.provider.lightBrightnessTable[s];
                if (worldclient.lastLightningBolt > 0) {
                    sunlight = worldclient.provider.lightBrightnessTable[s];
                }
                rSunlight = sunlight;
                gSunlight = sunlight;
                bSunlight = sunlight;

                for (int b = 0; b < 16; b++) {
                    bLight = worldclient.provider.lightBrightnessTable[b] + bSunlight;
                    bLight = applyGamma(bLight, gamma) * (max - min) + min;
                    for (int g = 0; g < 16; g++) {
                        gLight = worldclient.provider.lightBrightnessTable[g] + gSunlight;
                        gLight = applyGamma(gLight, gamma) * (max - min) + min;
                        for (int r = 0; r < 16; r++) {
                            rLight = worldclient.provider.lightBrightnessTable[r] + rSunlight;
                            rLight = applyGamma(rLight, gamma) * (max - min) + min;
                            map[g << 12 | s << 8 | r << 4 | b] = 255 << 24 | (int) (rLight * 255) << 16 | (int) (gLight * 255) << 8 | (int) (bLight * 255);
                        }
                    }
                }
            }
            this.lightmapTexture.dynamicTextureData = map;

            this.lightmapTexture.updateDynamicTexture();
            this.lightmapUpdateNeeded = false;
        }
    }

    private static float applyGamma(float light, float gamma) {
        float lightC;
        light = clamp(light, 0.0f, 1.0f);
        lightC = 1 - light;
        light = light * (1 - gamma) + (1 - lightC * lightC * lightC * lightC) * gamma;
        light = 0.96f * light + 0.03f;
        light = clamp(light, 0.0f, 1.0f);
        return light;
    }

    private static float clamp(float x, float lower, float upper) {
        if (lower > upper) {
            throw new IllegalArgumentException("Lower bound cannot be greater than upper bound!");
        }
        if (x < lower) {
            x = lower;
        }
        if (x > upper) {
            x = upper;
        }
        return x;
    }

    @Overwrite
    public void enableLightmap(double par1) {
        if (ignoreNextEnableLightmap) {
            ignoreNextEnableLightmap = false;
            return;
        }
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glScalef(f, f, f);
        GL11.glTranslatef(t, t, t);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        this.mc.getTextureManager().bindTexture(this.locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.enableShader();
    }

    @Overwrite
    public void disableLightmap(double par1) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.disableShader();
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        glDisable(GL_TEXTURE_2D);

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void disableLightmap(double par1, boolean forRealz) {
        if (!forRealz) {
            ignoreNextEnableLightmap = true;
            return;
        }
        disableLightmap(par1);
    }
}
