package lightingoverhaul.asm;

import lightingoverhaul.LightingOverhaul;
import lightingoverhaul.mixin.interfaces.ITessellatorMixin;

import net.minecraft.client.renderer.Tessellator;

/**
 * All the methods of this class are called by ASM-injected calls! Do not rename without also renaming it in Transformer
 */
public class Helper {

    public static void enableTexture() {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.enableTexture();
    }

    public static void disableTexture() {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.disableTexture();
    }

    public static void setTexCoord(float x, float y) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.setTextureCoords(x, y);
    }
}
