package com.darkshadow44.lightoverhaul.interfaces;

public interface IChunkMixin {
    boolean canReallySeeTheSky(int x, int y, int z);

    int getRealSunColor(int x, int y, int z);

    void init();
}
