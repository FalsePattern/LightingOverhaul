package com.darkshadow44.lightoverhaul.interfaces;

public interface ITessellatorMixin {

    boolean isProgramInUse();

    int getLightCoordUniform();

    int getLightCoordSunUniform();

    void updateShaders(float newGamma, float newSunlevel, float newNightVisionWeight);

    void disableShader();

    void enableShader();

    void setLockedBrightness(boolean locked);

    void disableTexture();

    void enableTexture();
}
