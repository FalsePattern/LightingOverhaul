package com.lightingoverhaul.mixinmod.interfaces;

public interface IBlockMixin {
    void setMetadataLightValue(int metadata, int lightValue);
    void setLightValue(int newValue);
    int getLightValue_INTERNAL();
}
