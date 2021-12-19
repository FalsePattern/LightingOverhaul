package com.lightingoverhaul.coremod.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class LightChunk {
    private final LightSegment[] segments = new LightSegment[16];

    public void save(DataOutputStream out) {

    }

    public void load(DataInputStream in) {

    }

    public long getPacked(int x, int y, int z) {
        return segments[y >>> 4].getPacked(x, y, z);
    }

    public void setPacked(int x, int y, int z, long packed) {
        segments[y >>> 4].setPacked(x, y, z, packed);
    }
}
