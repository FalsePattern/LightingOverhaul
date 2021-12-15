package com.lightingoverhaul.mixinmod.helper.shader;

public interface IRGBShader {
    int getTexCoordAttrib();
    int getLightCoordAttrib();

    int getLightCoordUniform();
    int getLightCoordSunUniform();
    int getGammaUniform();
    int getSunLevelUniform();
    int getNightVisionWeightUniform();
    int getEnableTextureUniform();
    int getTextureUniform();
}
