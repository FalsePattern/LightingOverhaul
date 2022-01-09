package coloredlightscore.src.asm;

/**
 * FastCraft compatibility.
 * Use this class for detecting the presence of LightingOverhaul. This class is guaranteed to exist in every future
 * version. This class is an artifact from when LightingOverhaul was still the ColoredLights mod. Due to the class
 * structure change, FastCraft could not detect the mod. FastCraft looks for this class on the classpath for enabling
 * internal compatibility. Due to FastCraft being unnecessarily obfuscated, this compatibility tweak cannot be done in
 * the other direction, so this class now stays here permanently.
 */
public class ColoredLightsCoreLoadingPlugin {
    public static void touch() {

    }
}
