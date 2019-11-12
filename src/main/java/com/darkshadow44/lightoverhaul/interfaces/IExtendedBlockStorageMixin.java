package com.darkshadow44.lightoverhaul.interfaces;

import net.minecraft.world.chunk.NibbleArray;

public interface IExtendedBlockStorageMixin {
    void setRedColorArray(NibbleArray array);

    void setGreenColorArray(NibbleArray array);

    void setBlueColorArray(NibbleArray array);

    void setRedColorArray2(NibbleArray array);

    void setGreenColorArray2(NibbleArray array);

    void setBlueColorArray2(NibbleArray array);

    void setRedColorArraySun(NibbleArray array);

    void setGreenColorArraySun(NibbleArray array);

    void setBlueColorArraySun(NibbleArray array);

    NibbleArray getRedColorArray();

    NibbleArray getGreenColorArray();

    NibbleArray getBlueColorArray();

    NibbleArray getRedColorArray2();

    NibbleArray getGreenColorArray2();

    NibbleArray getBlueColorArray2();

    NibbleArray getRedColorArraySun();

    NibbleArray getGreenColorArraySun();

    NibbleArray getBlueColorArraySun();
}
