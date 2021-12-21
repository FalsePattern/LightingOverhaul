package com.lightingoverhaul.mixinmod.interfaces;

import com.lightingoverhaul.mixinmod.helper.shader.RGBShader;

public interface ITessellatorMixin {

    boolean isProgramInUse();

    RGBShader getShader();

    void updateShaders(float newGamma, float newSunlevel, float newNightVisionWeight);

    void disableShader();

    void enableShader();

    void setLockedBrightness(boolean locked);

    void disableTexture();

    void enableTexture();

    void setTextureCoords(float x, float y);
}