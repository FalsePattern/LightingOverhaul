package net.minecraft.world.chunk.storage;

import net.minecraft.world.chunk.NibbleArray;

/**
 * Dummy class to enable access to runtime-added methods/fields
 */
public class ExtendedBlockStorage
{
    // Added by TransformExtendedBlockStorage
    private NibbleArray rColorArray;
    private NibbleArray gColorArray;
    private NibbleArray bColorArray;
    private NibbleArray rColorArray2;
    private NibbleArray gColorArray2;
    private NibbleArray bColorArray2;

    // Added by TransformExtendedBlockStorage
    public NibbleArray getRedColorArray() {
        return rColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public void setRedColorArray(NibbleArray rColorArray) {
        this.rColorArray = rColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public NibbleArray getGreenColorArray() {
        return gColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public void setGreenColorArray(NibbleArray gColorArray) {
        this.gColorArray = gColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public NibbleArray getBlueColorArray() {
        return bColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public void setBlueColorArray(NibbleArray bColorArray) {
        this.bColorArray = bColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public NibbleArray getRedColorArray2() {
        return rColorArray2;
    }

    // Added by TransformExtendedBlockStorage
    public void setRedColorArray2(NibbleArray rColorArray) {
        this.rColorArray2 = rColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public NibbleArray getGreenColorArray2() {
        return gColorArray2;
    }

    // Added by TransformExtendedBlockStorage
    public void setGreenColorArray2(NibbleArray gColorArray) {
        this.gColorArray2 = gColorArray;
    }

    // Added by TransformExtendedBlockStorage
    public NibbleArray getBlueColorArray2() {
        return bColorArray2;
    }

    // Added by TransformExtendedBlockStorage
    public void setBlueColorArray2(NibbleArray bColorArray) {
        this.bColorArray2 = bColorArray;
    }

    public int getYLocation() {
        return 0;
    }

    public int getExtBlocklightValue(int p_76629_1_, int i, int p_76629_3_) {
        return 0;
    }

    public int getExtSkylightValue(int p_76629_1_, int i, int p_76629_3_) {
        return 0;
    }
}
