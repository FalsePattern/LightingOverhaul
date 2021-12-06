package com.lightingoverhaul.mixinmod.mixins;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;

import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

    @Shadow
    public float torchFlickerX;
    @Shadow
    public float bossColorModifier;
    @Shadow
    public float bossColorModifierPrev;

    @Shadow
    public int[] lightmapColors;

    private static final float f = (1.0F / 4096.0F);
    private static final float t = 8.0f;
    private static boolean ignoreNextEnableLightmap;

    private void updateLightmap_orig(float p_78472_1_) {
        WorldClient worldclient = this.mc.theWorld;

        if (worldclient != null) {
            for (int i = 0; i < 256; ++i) {
                float f1 = worldclient.getSunBrightness(1.0F) * 0.95F + 0.05F;
                float f2 = worldclient.provider.lightBrightnessTable[i / 16] * f1;
                float f3 = worldclient.provider.lightBrightnessTable[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);

                if (worldclient.lastLightningBolt > 0) {
                    f2 = worldclient.provider.lightBrightnessTable[i / 16];
                }

                float f4 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
                float f5 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
                float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
                float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
                float f8 = f4 + f3;
                float f9 = f5 + f6;
                float f10 = f2 + f7;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;
                float f11;

                if (this.bossColorModifier > 0.0F) {
                    f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * p_78472_1_;
                    f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
                    f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
                    f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
                }

                if (worldclient.provider.dimensionId == 1) {
                    f8 = 0.22F + f3 * 0.75F;
                    f9 = 0.28F + f6 * 0.75F;
                    f10 = 0.25F + f7 * 0.75F;
                }

                float f12;

                if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                    f11 = this.getNightVisionBrightness(this.mc.thePlayer, p_78472_1_);
                    f12 = 1.0F / f8;

                    if (f12 > 1.0F / f9) {
                        f12 = 1.0F / f9;
                    }

                    if (f12 > 1.0F / f10) {
                        f12 = 1.0F / f10;
                    }

                    f8 = f8 * (1.0F - f11) + f8 * f12 * f11;
                    f9 = f9 * (1.0F - f11) + f9 * f12 * f11;
                    f10 = f10 * (1.0F - f11) + f10 * f12 * f11;
                }

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F) {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F) {
                    f10 = 1.0F;
                }

                f11 = this.mc.gameSettings.gammaSetting;
                f12 = 1.0F - f8;
                float f13 = 1.0F - f9;
                float f14 = 1.0F - f10;
                f12 = 1.0F - f12 * f12 * f12 * f12;
                f13 = 1.0F - f13 * f13 * f13 * f13;
                f14 = 1.0F - f14 * f14 * f14 * f14;
                f8 = f8 * (1.0F - f11) + f12 * f11;
                f9 = f9 * (1.0F - f11) + f13 * f11;
                f10 = f10 * (1.0F - f11) + f14 * f11;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F) {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F) {
                    f10 = 1.0F;
                }

                if (f8 < 0.0F) {
                    f8 = 0.0F;
                }

                if (f9 < 0.0F) {
                    f9 = 0.0F;
                }

                if (f10 < 0.0F) {
                    f10 = 0.0F;
                }

                short short1 = 255;
                int j = (int) (f8 * 255.0F);
                int k = (int) (f9 * 255.0F);
                int l = (int) (f10 * 255.0F);
                this.lightmapColors[i] = short1 << 24 | j << 16 | k << 8 | l;
            }

            this.lightmapTexture.updateDynamicTexture();
            this.lightmapUpdateNeeded = false;
        }
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    private void updateLightmap(float partialTickTime) {
        WorldClient worldclient = this.mc.theWorld;
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;

        float nightVisionWeight = 0;
        if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
            nightVisionWeight = this.getNightVisionBrightness(this.mc.thePlayer, partialTickTime);
        }

        if (worldclient != null) {
            float sunlightBase = worldclient.getSunBrightness(partialTickTime);
            if (worldclient.lastLightningBolt > 0) {
                sunlightBase = 1.0f;
            }
            float gamma;

            gamma = this.mc.gameSettings.gammaSetting;
            tessellatorMixin.updateShaders(gamma, sunlightBase, nightVisionWeight);
        }

        updateLightmap_orig(partialTickTime);
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
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

    /***
     * @author darkshadow44
     * @reason TODO
     */
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
