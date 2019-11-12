package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Created by Murray on 11/30/2014.
 */
public class Chunk {
    public ExtendedBlockStorage[] storageArrays;
    public World worldObj;
    public int xPosition;
    public int zPosition;
    public static boolean isLit;

    public ExtendedBlockStorage[] getBlockStorageArray() {
        return storageArrays;
    }

    public ExtendedBlockStorage[] getStorageArrays() {
        return storageArrays;
    }

    public void setStorageArrays(ExtendedBlockStorage[] storageArrays) {
        this.storageArrays = storageArrays;
    }

    public int getBlockLightValue(int x, int y, int z, int skylightSubtracted) {
        return 0;
    }
    /* DUMMY */
    public Block getBlock(int paramInt1, int paramInt2, int paramInt3) {
        return null;
    }
    public int getTopFilledSegment() {
        return 0;
    }
    public boolean isModified;
    public int[] precipitationHeightMap;
    public int heightMapMinimum;
    public int[] heightMap;

    public void updateSkylightNeighborHeight(int k, int i, int i1, int i2) {
    }
}
