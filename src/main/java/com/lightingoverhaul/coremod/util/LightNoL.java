package com.lightingoverhaul.coremod.util;

public class LightNoL implements Holder<LightNoL> {
    public static final int SIZE = 2 * RGB.SIZE;

    final RGB color;
    final RGB sun;
    LightNoL() {
        color = new RGB();
        sun = new RGB();
    }

    public LightNoL(int r, int g, int b, int sr, int sg, int sb) {
        color = new RGB(r, g, b);
        sun = new RGB(sr, sg, sb);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public int get(int index) {
        return index < RGB.SIZE ? color.get(index) : sun.get(index - RGB.SIZE);
    }

    @Override
    public void set(int index, int v) {
        if (index < RGB.SIZE) {
            color.set(index, v);
        } else {
            sun.set(index - RGB.SIZE, v);
        }
    }
}
