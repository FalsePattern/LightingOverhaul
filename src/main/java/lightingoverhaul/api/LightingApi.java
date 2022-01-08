package lightingoverhaul.api;

import com.google.common.primitives.Ints;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Public API for LightingOverhaul
 * 
 * @author CptSpaceToaster, FalsePattern
 *
 */
public class LightingApi {
    //                                        black red green brown blue purple cyan lightgray gray pink lime yellow lightblue magenta orange white
    public static final int[] r = new int[] {     0, 15,    0,    8,   0,    10,   0,       10,   5,  15,   8,    15,        0,     15,    15,   15 };
    public static final int[] g = new int[] {     0,  0,   15,    3,   0,     0,  15,       10,   5,  10,  15,    15,        8,      0,    12,   15 };
    public static final int[] b = new int[] {     0,  0,    0,    0,  15,    15,  15,       10,   5,  13,   0,     0,       15,     15,    10,   15 };

    public static final int _bitshift_r = 0;
    public static final int _bitshift_g = 4;
    public static final int _bitshift_b = 8;
    public static final int _bitshift_sun_r = 12;
    public static final int _bitshift_sun_g = 16;
    public static final int _bitshift_sun_b = 20;
    
    public static final int _bitmask = 0xF;

    /**
     * Computes a 20-bit lighting word, containing red, green, blue settings, and brightness settings.
     * Automatically computes the Minecraft brightness value using the brightest of the r, g and b channels.
     * This value can be used directly for Block.lightValue 
     * 
     * Word format: 0000 0000 0000 0000 0000 BBBB GGGG RRRR
     * 
     * @param r Red intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @param g Green intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @param b Blue intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @return Integer describing RGB color for a block
     */
    public static int makeRGBLightValue(float r, float g, float b) {
        return makeRGBLightValue((int)(r * 15.0F), (int)(g * 15.0F), (int)(b * 15.0F));
    }

    /**
     * Computes a 20-bit lighting word, containing red, green, blue settings, and brightness settings.
     * Automatically computes the Minecraft brightness value using the brightest of the r, g and b channels.
     * This value can be used directly for Block.lightValue 
     * 
     * Word format: 0000 0000 0000 0000 0000 BBBB GGGG RRRR
     * 
     * @param r Red intensity, 0 to 15. Resolution is 4 bits.
     * @param g Green intensity, 0 to 15. Resolution is 4 bits.
     * @param b Blue intensity, 0 to 15. Resolution is 4 bits.
     * @return Integer describing RGB color for a block
     */
    public static int makeRGBLightValue(int r, int g, int b) {
        // Clamp color channels
        if (r < 0)
            r = 0;
        else if (r > _bitmask)
            r = _bitmask;

        if (g < 0)
            g = 0;
        else if (g > _bitmask)
            g = _bitmask;

        if (b < 0)
            b = 0;
        else if (b > _bitmask)
            b = _bitmask;

        return toLightBlock(r, g, b);
    }

    public static int extractR(int light) {
        return (light >>> _bitshift_r) & _bitmask;
    }

    public static int extractG(int light) {
        return (light >>> _bitshift_g) & _bitmask;
    }

    public static int extractB(int light) {
        return (light >>> _bitshift_b) & _bitmask;
    }

    public static int extractSunR(int light) {
        return (light >>> _bitshift_sun_r) & _bitmask;
    }

    public static int extractSunG(int light) {
        return (light >>> _bitshift_sun_g) & _bitmask;
    }

    public static int extractSunB(int light) {
        return (light >>> _bitshift_sun_b) & _bitmask;
    }

    public static int toLightBlock(int r, int g, int b) {
        return ((r & _bitmask) << _bitshift_r) | ((g & _bitmask) << _bitshift_g) | ((b & _bitmask) << _bitshift_b);
    }

    public static int toLightSun(int sr, int sg, int sb) {
        return ((sr & _bitmask) << _bitshift_sun_r) | ((sg & _bitmask) << _bitshift_sun_g) | ((sb & _bitmask) << _bitshift_sun_b);
    }

    public static int toLight(int r, int g, int b, int sr, int sg, int sb) {
        return toLightBlock(r, g, b) | toLightSun(sr, sg, sb);
    }

    @SideOnly(Side.CLIENT)
    public static int toRenderLight(int r, int g, int b, int sr, int sg, int sb) {
        return (1 << 30) | toLight(r, g, b, sr, sg, sb);
    }
}
