package lightingoverhaul.helper;

import lightingoverhaul.LightingOverhaul;
import lightingoverhaul.mixin.interfaces.ITessellatorMixin;

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
        LightingOverhaul.emissivesEnabled = true;
    }

    public static void disableEmissives() {
        LightingOverhaul.emissivesEnabled = false;
    }

    public static void setTexCoord(float x, float y) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.setTextureCoords(x, y);
    }
}
