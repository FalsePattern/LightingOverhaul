package com.lightingoverhaul.mixinmod.helper;

import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;

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

    public static void setTexCoord(float x, float y) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        tessellatorMixin.setTextureCoords(x, y);
    }
}
