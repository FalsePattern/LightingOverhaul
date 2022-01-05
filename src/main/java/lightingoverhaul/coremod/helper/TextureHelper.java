package lightingoverhaul.coremod.helper;

import lightingoverhaul.coremod.asm.LightingOverhaulCore;
import lightingoverhaul.coremod.mixin.interfaces.ITessellatorMixin;

import net.minecraft.client.renderer.Tessellator;

public class TextureHelper {

    public static void enableTexture() {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.enableTexture();
    }

    public static void disableTexture() {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.disableTexture();
    }

    public static void enableEmissives() {
        LightingOverhaulCore.emissivesEnabled = true;
    }

    public static void disableEmissives() {
        LightingOverhaulCore.emissivesEnabled = false;
    }

    public static void setTexCoord(float x, float y) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.setTextureCoords(x, y);
    }
}
