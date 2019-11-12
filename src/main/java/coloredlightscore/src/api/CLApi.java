package coloredlightscore.src.api;

/**
 * Public API for ColoredLightsCore
 * 
 * @author CptSpaceToaster
 *
 */
public class CLApi {
    public static float l[] = new float[] { 0F, 1F / 15, 2F / 15, 3F / 15, 4F / 15, 5F / 15, 6F / 15, 7F / 15, 8F / 15, 9F / 15, 10F / 15, 11F / 15, 12F / 15, 13F / 15, 14F / 15, 1F };

    //                                  black red green brown blue purple cyan gray lightgray pink lime yellow lightblue magenta orange white
    public static int r[] = new int[] {     0, 15,    0,    8,   0,    10,   0,  10,        5,  15,   8,    15,        0,     15,    15,   15 };
    public static int g[] = new int[] {     0,  0,   15,    3,   0,     0,  15,  10,        5,  10,  15,    15,        8,      0,    12,   15 };
    public static int b[] = new int[] {     0,  0,    0,    0,  15,    15,  15,  10,        5,  13,   0,     0,       15,     15,    10,   15 };

    public static int bitshift_l = 0;
    public static int bitshift_r = 4;
    public static int bitshift_g = 9;
    public static int bitshift_b = 14;
    public static int bitshift_sun_r = 19;
    public static int bitshift_sun_g = 23;
    public static int bitshift_sun_b = 27;

    public static int bitshift_l2 = 4;
    public static int bitshift_r2 = 8;
    public static int bitshift_g2 = 13;
    public static int bitshift_b2 = 18;
    public static int bitshift_sun_r2 = 23;
    public static int bitshift_sun_g2 = 27;
    public static int bitshift_sun_b2 = 0;

    public static int bitmask = 0x1F;
    public static int bitmask_sun = 0xF;

    /**
     * Computes a 20-bit lighting word, containing red, green, blue settings, and brightness settings.
     * Automatically computes the Minecraft brightness value using the brightest of the r, g and b channels.
     * This value can be used directly for Block.lightValue 
     * 
     * Word format: 0BBBB 0GGGG 0RRRR 0LLLL
     * 
     * @param r Red intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @param g Green intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @param b Blue intensity, 0.0f to 1.0f. Resolution is 4 bits.
     * @return Integer describing RGB color for a block
     */
    public static int makeRGBLightValue(float r, float g, float b) {
        // Clamp color channels
        if (r < 0.0f)
            r = 0.0f;
        else if (r > 1.0f)
            r = 1.0f;

        if (g < 0.0f)
            g = 0.0f;
        else if (g > 1.0f)
            g = 1.0f;

        if (b < 0.0f)
            b = 0.0f;
        else if (b > 1.0f)
            b = 1.0f;

        int brightness = (int) (15.0f * Math.max(Math.max(r, g), b));
        return brightness | ((((int) (15.0F * b)) << bitshift_b) + (((int) (15.0F * g)) << bitshift_g) + (((int) (15.0F * r)) << bitshift_r));
    }

    /**
     * Computes a 20-bit lighting word, containing red, green, blue settings, and brightness settings.
     * Automatically computes the Minecraft brightness value using the brightest of the r, g and b channels.
     * This value can be used directly for Block.lightValue 
     * 
     * Word format: 0RRRR 0GGGG 0BBBB 0LLLL
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
        else if (r > bitmask)
            r = bitmask;

        if (g < 0)
            g = 0;
        else if (g > bitmask)
            g = bitmask;

        if (b < 0)
            b = 0;
        else if (b > bitmask)
            b = bitmask;

        int brightness = Math.max(Math.max(r, g), b);
        return brightness | ((b << bitshift_b) + (g << bitshift_g) + (r << bitshift_r));
    }
}
