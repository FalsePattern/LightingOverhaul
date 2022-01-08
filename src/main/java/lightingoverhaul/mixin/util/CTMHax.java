package lightingoverhaul.mixin.util;

import lightingoverhaul.api.LightingApi;

public class CTMHax {
    public static int avg(int... lightVals) {
        int r = 0;
        int g = 0;
        int b = 0;
        int sr = 0;
        int sg = 0;
        int sb = 0;
        int length = lightVals.length;
        for (int light : lightVals) {
            r += LightingApi.extractR(light);
            g += LightingApi.extractG(light);
            b += LightingApi.extractB(light);
            sr += LightingApi.extractSunR(light);
            sg += LightingApi.extractSunG(light);
            sb += LightingApi.extractSunB(light);
        }
        r /= length;
        g /= length;
        b /= length;
        sr /= length;
        sg /= length;
        sb /= length;
        return LightingApi.toRenderLight(r, g, b, sr, sg, sb);
    }
}
