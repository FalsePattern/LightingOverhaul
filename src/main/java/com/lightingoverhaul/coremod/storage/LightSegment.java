package com.lightingoverhaul.coremod.storage;

import com.google.common.primitives.Longs;

import java.io.*;

public class LightSegment {
    private static final int BLOCKS_PER_AXIS = 16;
    private static final int BLOCKS_PER_SEGMENT = BLOCKS_PER_AXIS * BLOCKS_PER_AXIS * BLOCKS_PER_AXIS;

    //Keep these two in sync
    public static final int FULL_BRIGHT = 255;
    private static final int FULL_BRIGHT_BYTE = -127;

    private long[] packedLight;

    private BlockLightFill blockLight = BlockLightFill.Zero;
    private SunLightFill sunLight = SunLightFill.Full;

    private boolean dirty = false;

    public void save(DataOutputStream out) throws IOException {
        gc(); //Obligatory cleanup before save
        out.write(sunLight.toFlag() | blockLight.toFlag());
        if (blockLight == BlockLightFill.Mixed || sunLight == SunLightFill.Mixed) {
            for (int i = 0; i < BLOCKS_PER_SEGMENT; i++) {
                out.writeLong(packedLight[i]);
            }
        }
    }

    public void load(DataInputStream in) throws IOException {
        int flag = in.read();
        blockLight = BlockLightFill.fromFlag(flag);
        sunLight = SunLightFill.fromFlag(flag);
        if (blockLight == BlockLightFill.Mixed || sunLight == SunLightFill.Mixed) {
            packedLight = new long[BLOCKS_PER_SEGMENT];
            for (int i = 0; i < BLOCKS_PER_SEGMENT; i++) {
                packedLight[i] = in.readLong();
            }
        }
    }

    public long getPacked(int x, int y, int z) {
        if (blockLight == BlockLightFill.Mixed || sunLight == SunLightFill.Mixed) {
            return packedLight[getIndex(x, y, z)];
        } else {
            return sunLight == SunLightFill.Full ? 0x00FFFFFF00000000L : 0L;
        }
    }

    public void setPacked(int x, int y, int z, long packed) {
        if (blockLight != BlockLightFill.Mixed && sunLight != SunLightFill.Mixed) {
            if (sunLight == SunLightFill.Full && ((packed & 0xFFFFFFFF00000000L) != 0x00FFFFFF00000000L)) sunLight = SunLightFill.Mixed;
            else if (sunLight == SunLightFill.Zero && ((packed & 0xFFFFFFFF00000000L) != 0L)) sunLight = SunLightFill.Mixed;
            if ((packed & 0x00000000FFFFFFFFL) != 0L) blockLight = BlockLightFill.Mixed;
            if (blockLight != BlockLightFill.Mixed && sunLight != SunLightFill.Mixed) {
                return;
            }
            packedLight = new long[BLOCKS_PER_SEGMENT];
        }
        dirty = true;
        packedLight[getIndex(x, y, z)] = packed;
    }

    /**
     * Dynamically discard storage arrays if they have a single state all over to save disk and memory space.
     */
    public void gc() {
        if (!dirty) return;
        boolean allBlocksZero = true;
        boolean allSunsZero = true;
        boolean allSunsFull = true;
        for (int i = 0; (allBlocksZero || allSunsZero || allSunsFull) && i < BLOCKS_PER_SEGMENT; i++) {
            switch ((int) ((packedLight[i] & 0xFFFFFFFF00000000L) >>> 32)) {
                case 0: allSunsFull = false; break;
                case 0x00FFFFFF: allSunsZero = false; break;
                default: allSunsZero = allSunsFull = false; break;
            }
            if ((packedLight[i] & 0x00000000FFFFFFFFL) != 0) {
                allBlocksZero = false;
            }
        }
        blockLight = allBlocksZero ? BlockLightFill.Zero : BlockLightFill.Mixed;
        sunLight = allSunsFull ? SunLightFill.Full : allSunsZero ? SunLightFill.Zero : SunLightFill.Mixed;
        if (allBlocksZero && (allSunsZero || allSunsFull)) {
            packedLight = null;
        }
        dirty = false;

    }

    private int getIndex(int x, int y, int z) {
        return (z << 8) | (y << 4) | x;
    }
}
