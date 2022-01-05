package lightingoverhaul.mixin.interfaces;

import net.minecraft.world.chunk.NibbleArray;

public interface IExtendedBlockStorageMixin {
    void setRedColorArray(NibbleArray array);

    void setGreenColorArray(NibbleArray array);

    void setBlueColorArray(NibbleArray array);

    void setRedColorArraySun(NibbleArray array);

    void setGreenColorArraySun(NibbleArray array);

    void setBlueColorArraySun(NibbleArray array);

    NibbleArray getRedColorArray();

    NibbleArray getGreenColorArray();

    NibbleArray getBlueColorArray();

    NibbleArray getRedColorArraySun();

    NibbleArray getGreenColorArraySun();

    NibbleArray getBlueColorArraySun();
}
