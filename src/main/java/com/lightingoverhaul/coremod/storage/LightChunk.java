package com.lightingoverhaul.coremod.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class LightChunk implements LightContainer{
    private final LightSegment[] segments = new LightSegment[16];

    public void save(DataOutputStream out) {

    }

    public void load(DataInputStream in) {

    }

    @Override
    public int getBlockR(int x, int y, int z) {
        return segments[y >>> 4].getBlockR(x, y & 0x0F, z);
    }

    @Override
    public void setBlockR(int x, int y, int z, int value) {
        segments[y >>> 4].setBlockR(x, y & 0x0F, z, value);
    }

    @Override
    public int getBlockG(int x, int y, int z) {
        return segments[y >>> 4].getBlockG(x, y & 0x0F, z);
    }

    @Override
    public void setBlockG(int x, int y, int z, int value) {
        segments[y >>> 4].setBlockG(x, y & 0x0F, z, value);
    }

    @Override
    public int getBlockB(int x, int y, int z) {
        return segments[y >>> 4].getBlockB(x, y & 0x0F, z);
    }

    @Override
    public void setBlockB(int x, int y, int z, int value) {
        segments[y >>> 4].setBlockB(x, y & 0x0F, z, value);
    }

    @Override
    public int getSunR(int x, int y, int z) {
        return segments[y >>> 4].getSunR(x, y & 0x0F, z);
    }

    @Override
    public void setSunR(int x, int y, int z, int value) {
        segments[y >>> 4].setSunR(x, y & 0x0F, z, value);
    }

    @Override
    public int getSunG(int x, int y, int z) {
        return segments[y >>> 4].getSunG(x, y & 0x0F, z);
    }

    @Override
    public void setSunG(int x, int y, int z, int value) {
        segments[y >>> 4].setSunG(x, y & 0x0F, z, value);
    }

    @Override
    public int getSunB(int x, int y, int z) {
        return segments[y >>> 4].getSunB(x, y & 0x0F, z);
    }

    @Override
    public void setSunB(int x, int y, int z, int value) {
        segments[y >>> 4].setSunB(x, y & 0x0F, z, value);
    }

    @Override
    public int getBlockPacked(int x, int y, int z) {
        return segments[y >>> 4].getBlockPacked(x, y, z);
    }

    @Override
    public void setBlockPacked(int x, int y, int z, int packed) {
        segments[y >>> 4].setBlockPacked(x, y, z, packed);
    }

    @Override
    public int getSunPacked(int x, int y, int z) {
        return segments[y >>> 4].getSunPacked(x, y, z);
    }

    @Override
    public void setSunPacked(int x, int y, int z, int packed) {
        segments[y >>> 4].setSunPacked(x, y, z, packed);
    }

    @Override
    public long getPacked(int x, int y, int z) {
        return segments[y >>> 4].getPacked(x, y, z);
    }

    @Override
    public void setPacked(int x, int y, int z, long packed) {
        segments[y >>> 4].setPacked(x, y, z, packed);
    }
}
