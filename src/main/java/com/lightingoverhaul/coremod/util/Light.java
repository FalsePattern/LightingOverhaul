package com.lightingoverhaul.coremod.util;

import com.lightingoverhaul.coremod.api.LightingApi;

public class Light implements Holder<Light>, AutoCloseable{
    public static final int SIZE = LightNoL.SIZE + 1;

    public int l = 0;
    public final LightNoL noL;
    public final RGB color;
    public final RGB sun;

    public Light(){
        noL = new LightNoL();
        color = noL.color;
        sun = noL.sun;
    }

    public Light(int l, int r, int g, int b, int sr, int sg, int sb) {
        this.l = l;
        noL = new LightNoL(r, g, b, sr, sg, sb);
        color = noL.color;
        sun = noL.sun;
    }

    public void set(int l, int cGray, int sGray) {
        this.l = l;
        color.set(cGray);
        sun.set(sGray);
    }

    public void set(int l, int r, int g, int b, int sr, int sg, int sb) {
        this.l = l;
        color.set(r, g, b);
        sun.set(sr, sg, sb);
    }

    public void fromLight(int light) {
        apply(LightingApi.bitshift_light, LightingApi.bitmask_light, (shift, mask) -> (light >> shift) & mask);
    }

    public int toLight() {
        return (sun.r << LightingApi._bitshift_sun_r) | (sun.g << LightingApi._bitshift_sun_g) | (sun.b << LightingApi._bitshift_sun_b)
                | (color.r << LightingApi._bitshift_r) | (color.g << LightingApi._bitshift_g) | (color.b << LightingApi._bitshift_b)
                | (l << LightingApi._bitshift_l);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public int get(int index) {
        if (index == 0) {
            return l;
        }
        return noL.get(index - 1);
    }

    @Override
    public void set(int index, int v) {
        if (index == 0) {
            l = v;
        } else {
            noL.set(index - 1, v);
        }
    }

}
