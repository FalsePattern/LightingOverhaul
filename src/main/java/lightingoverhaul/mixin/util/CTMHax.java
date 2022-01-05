package lightingoverhaul.mixin.util;

import lightingoverhaul.api.LightingApi;

public class CTMHax {
    public static int avg(int... lightVals) {
        int r = 0;
        int g = 0;
        int b = 0;
        int l = 0;
        int sr = 0;
        int sg = 0;
        int sb = 0;
        int length = lightVals.length;
        for (int i = 0; i < length; i++) {
            int light = lightVals[i];
            r += LightingApi.extractR(light);
            g += LightingApi.extractG(light);
            b += LightingApi.extractB(light);
            l += LightingApi.extractL(light);
            sr += LightingApi.extractSunR(light);
            sg += LightingApi.extractSunG(light);
            sb += LightingApi.extractSunB(light);
        }
        r /= length;
        g /= length;
        b /= length;
        l /= length;
        sr /= length;
        sg /= length;
        sb /= length;
        return LightingApi.toRenderLight(r, g, b, l, sr, sg, sb);
    }
}
