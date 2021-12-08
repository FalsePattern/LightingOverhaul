package com.lightingoverhaul.coremod.util;

public class RGB implements Holder<RGB> {
    public static final int SIZE = 3;
    public int r;
    public int g;
    public int b;

    public RGB(){}

    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void set(int gray) {
        set(gray, gray, gray);
    }

    public void set(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public int get(int index) {
        switch (index) {
            case 0: return r;
            case 1: return g;
            case 2: return b;
            default: throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    @Override
    public void set(int index, int v) {
        switch (index) {
            case 0: r = v; break;
            case 1: g = v; break;
            case 2: b = v; break;
            default: throw new ArrayIndexOutOfBoundsException(index);
        }
    }
}
