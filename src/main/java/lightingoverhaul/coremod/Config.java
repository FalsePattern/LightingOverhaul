package lightingoverhaul.coremod;

import lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import lightingoverhaul.coremod.helper.ResourceHelper;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.*;

public class Config {
    private static Configuration config;
    private static final Map<String, TIntObjectMap<String>> builtins = new HashMap<>();

    private static void parse(String color, int[] num) throws NumberFormatException {
        num[0] = Integer.parseInt(color.substring(0, 1), 16);
        num[1] = Integer.parseInt(color.substring(1, 2), 16);
        num[2] = Integer.parseInt(color.substring(2, 3), 16);
    }

    private static String getBlockID(String name) {
        int slash = name.lastIndexOf('/');
        return slash > 0 ? name.substring(0, slash) : name;
    }

    private static int getMetadata(String name) {
        int slash = name.lastIndexOf('/');
        try {
            if (slash > 0) {
                return Integer.parseInt(name.substring(slash + 1));
            }
        } catch (NumberFormatException e) {
            CoreLoadingPlugin.CLLog.warn(e);
        }
        return 0;
    }

    static {
        val cfg = ResourceHelper.readResourceAsString("/default_lightvals");
        if (cfg != null) Arrays.stream(cfg.split("\\n"))
                .map(line -> (line.contains("#") ? line.substring(0, line.indexOf('#')) : line).trim())
                .filter((line) -> line.contains("="))
                .forEach((line) -> {
                    val parts = line.split("=");
                    var id = parts[0].trim();
                    int meta = getMetadata(id);
                    id = getBlockID(id);
                    val color = parts[1].trim();
                    builtins.computeIfAbsent(id, (ignored) -> new TIntObjectHashMap<>()).put(meta, color);
                });
    }

    private static final Map<String, TIntObjectMap<String>> lightValues = new HashMap<>();

    private static final TIntObjectMap<String> EMPTY = new TUnmodifiableIntObjectMap<>(new TIntObjectHashMap<>());

    private static final int[] REMAP_R = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final int[] REMAP_G = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 14};
    private static final int[] REMAP_B = new int[]{0, 1, 2, 2, 3, 4, 5, 5, 6, 7,  8,  8,  9, 10, 11, 12};

    public static OptionalInt getLightValueForMetadata(String blockID, int metadata, int currentL, int[] outputBuffer) {
        int recursion = 0;
        String value = "";
        try {
            value = blockID;
            int subMeta = metadata;
            var map = lightValues.getOrDefault(value, EMPTY);
            while ((value = map.get(subMeta)).startsWith("@")) {
                recursion++;
                if (recursion >= refRecursionLimit) {
                    CoreLoadingPlugin.CLLog.warn("Recursion limit hit while trying to evaluate value for " + blockID + "! Returning default value for now, but CHECK THE CONFIG!!!");
                    throw new NullPointerException(); //jump to the default behaviour
                }
                value = value.substring(1);
                val id = getBlockID(value);
                subMeta = getMetadata(value);
                map = lightValues.getOrDefault(id, EMPTY);
            }
            parse(value, outputBuffer);
            return Arrays.stream(lightValues.getOrDefault(blockID, EMPTY).keys()).filter((key) -> key > metadata).min();
        } catch (NullPointerException ignored){}
        catch (NumberFormatException e) {
            String result = "Invalid light value for " + blockID + ": " +
                            value + " is an invalid color value!";
            CoreLoadingPlugin.CLLog.error(result);
        }
        outputBuffer[0] = REMAP_R[currentL & 0xf];
        outputBuffer[1] = REMAP_G[currentL & 0xf];
        outputBuffer[2] = REMAP_B[currentL & 0xf];
        if (emitDiscoveredMissingMappingsToConfig) {
            lightValues.computeIfAbsent(blockID, (ignored) -> new TIntObjectHashMap<>()).put(metadata, "@__vanilla__:" + (currentL & 0xf));
        }
        return OptionalInt.of(-1);
    }

    @Getter
    @Accessors(fluent = true)
    private static boolean enableInGameContent;

    @Getter
    @Accessors(fluent = true)
    private static boolean emitDiscoveredMissingMappingsToConfig;

    @Getter
    @Accessors(fluent = true)
    private static boolean debug;

    private static int refRecursionLimit;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();
        if (!config.hasCategory("lightvalues")) {
            config.setCategoryComment("lightvalues", "Custom light values for specific blocks. Note that these cannot change light values in blocks that override the getLightValue call.");
        }
        if (!config.hasCategory("configuration")) {
            config.setCategoryComment("configuration", "Changes the behaviour of LightingOverhaul.");
        }

        config.setCategoryRequiresMcRestart("lightvalues", true);
        config.setCategoryRequiresMcRestart("configuration", true);

        lightValues.clear();
        builtins.forEach((key, value) -> {
            val mapping = lightValues.compute(key, (i, l) -> new TIntObjectHashMap<>());
            value.forEachEntry((meta, color) -> {
                if (color.startsWith("@") && !color.contains("/")) {
                    color += "/0";
                }
                mapping.put(meta, color);
                config.get("lightvalues", key + "/" + meta, color);
                return true;
            });
            lightValues.put(key, value);
        });
        config.getCategory("lightvalues").forEach((key, value) -> {
            String data = value.getString();
            if (data.startsWith("@") && !data.contains("/")) {
                data += "/0";
            }
            val id = getBlockID(key);
            val meta = getMetadata(key);
            lightValues.computeIfAbsent(id, (ignored) -> new TIntObjectHashMap<>()).put(meta, data);
        });

        enableInGameContent = config.getBoolean("enableInGameContent", "configuration", true, "Whether the mod should also add colored glowstone and colored lamps. When disabled, the mod does not add ingame content, and only behaves as a coremod/api.");
        emitDiscoveredMissingMappingsToConfig = config.getBoolean("emitDiscoveredMissingMappingsToConfig", "configuration", false, "Whether to put any detected, but missing block light values into the config file. Recommended for modpack makers. Set it back to false after using to avoid unnecessarily lengthening load times. Bypasses the useSmartLightMapping option.");
        refRecursionLimit = config.getInt("referenceRecursionLimit", "configuration", 256, 0, 1024, "The maximum depth a color value reference chain can be inside the lightvalues category before the parser stops evaluating and returns the default value.");
        debug = config.getBoolean("debug", "configuration", false, "Debug mode. Displays low-level, low-importance messages in the log.");
        config.save();
    }

    public static void syncConfig() {
        if (emitDiscoveredMissingMappingsToConfig)
            lightValues.forEach((key, value) -> {
                if (key.startsWith("__vanilla__")) return;
                value.forEachEntry((meta, color) -> {
                    config.get("lightvalues", key + "/" + meta, color);
                    return true;
                });
            });
        config.save();
    }

}
