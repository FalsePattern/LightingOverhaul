package lightingoverhaul;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.falsepattern.lib.api.ComplexVersion;
import com.falsepattern.lib.api.DependencyLoader;
import com.falsepattern.lib.api.SemanticVersion;
import cpw.mods.fml.common.Mod;
import lightingoverhaul.fmlevents.ChunkDataEventHandler;
import lightingoverhaul.network.PacketHandler;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.mixin.interfaces.IBlockMixin;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModInfo.MODID,
     version = ModInfo.VERSION,
     name = ModInfo.MODNAME,
     dependencies = "required-after:falsepatternlib@[0.2.2,);" +
                    "required-after:triangulator@[1.1.1,);" +
                    "required-after:spongemixins@[1.3.3,);")
public class LightingOverhaul {
    public static Logger LOlog = LogManager.getLogger(ModInfo.MODNAME);
    public ChunkDataEventHandler chunkDataEventHandler;

    @Mod.Instance(ModInfo.MODID)
    public static LightingOverhaul instance;

    public static boolean emissivesEnabled = false;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights
    public static Object dynamicLights;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights.getLightValue
    public static Method getDynamicLight = null;

    @SneakyThrows
    public LightingOverhaul() {
        DependencyLoader.addMavenRepo("https://maven.falsepattern.com/");
        DependencyLoader.builder()
                        .loadingModId(ModInfo.MODID)
                        .groupId("com.falsepattern")
                        .artifactId("triangulator")
                        .minVersion(new ComplexVersion(new SemanticVersion(1, 7, 10), new SemanticVersion(1, 1, 1)))
                        .maxVersion(new ComplexVersion(new SemanticVersion(1, 7, 10), new SemanticVersion(1, Integer.MAX_VALUE, Integer.MAX_VALUE)))
                        .preferredVersion(new ComplexVersion(new SemanticVersion(1, 7, 10), new SemanticVersion(1, 1, 1)))
                        .devSuffix("dev")
                        .isMod(true)
                        .build();
        chunkDataEventHandler = new ChunkDataEventHandler();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {

        Config.loadConfig(new File(evt.getModConfigurationDirectory(), ModInfo.MODID + ".cfg"));

        LOlog.info("Starting up " + ModInfo.MODNAME);

        // Spin up network handler
        PacketHandler.init();

        // Hook into chunk events
        MinecraftForge.EVENT_BUS.register(chunkDataEventHandler);
    }

    private static void setLightValue(Block block, int metadata, int r, int g, int b) {
        IBlockMixin ib = (IBlockMixin) block;
        int light = LightingApi.makeRGBLightValue(r, g, b);
        if (metadata == 0)
            ib.setLightValue(light);
        else
            ib.setMetadataLightValue(metadata, light);
    }

    public static boolean postInitRun = false;
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

        LOlog.info("Beginning custom light value injection...");
        int[] valueBuffer = new int[3];
        int failCount = 0;
        int l;
        for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
            if (block == null) continue;

            l = ((IBlockMixin) block).getLightValue_INTERNAL();
            if ((l <= 0) || (l > 0xF)) continue;

            String name = GameData.getBlockRegistry().getNameForObject(block);
            String formatStr;
            int metadata = 0;
            int nextMetadata = 0;
            while (metadata != -1) {
                val result = Config.getLightValueForMetadata(name, metadata, l, valueBuffer);
                if (!result.isPresent() || (nextMetadata = result.getAsInt()) != -1) {
                    formatStr = "Found light value for %s/%d in config: (r: %d, g: %d, b: %d)";
                    if (!result.isPresent()) nextMetadata = -1;
                } else {
                    failCount++;
                    formatStr = "Could not find light value for %s/%d in config. Replacing with auto-computed value: (r: %d, g: %d, b: %d)";
                }
                LOlog.info(String.format(formatStr, name, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]));
                setLightValue(block, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]);
                metadata = nextMetadata;
            }
        }
        if (failCount == 0) {
            LOlog.info("No missed block lights detected! A winner is you!");
        } else {
            LOlog.warn(String.format("%d missed block lights were detected and converted!", failCount));
        }

        LOlog.info("Checking for DynamicLights...");
        Class<?> DynamicLightsClazz = null;
        try {
            DynamicLightsClazz = Class.forName("atomicstryker.dynamiclights.client.DynamicLights");
            Field instanceField = DynamicLightsClazz.getDeclaredField("instance");
            instanceField.setAccessible(true);
            dynamicLights = instanceField.get(null);
        } catch (ClassNotFoundException e) {
            LOlog.info("Dynamic Lights not found");
        } catch (NoSuchFieldException e) {
            LOlog.error("Missing field named \"instance\" in DynamicLights. Did versions change?");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOlog.error("We were denied access to the field \"instance\" in DynamicLights :(  Did versions change?");
            e.printStackTrace();
        }

        if (DynamicLightsClazz != null && dynamicLights != null) {
            LOlog.info("Hey DynamicLights... What's the plan?");

            try {
                getDynamicLight = DynamicLightsClazz.getDeclaredMethod("getLightValue", IBlockAccess.class, Block.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (NoSuchMethodException e) {
                LOlog.error("Missing method named \"getLightValue\" in DynamicLightsClazz. Did versions change?");
                e.printStackTrace();
            }
        }
        postInitRun = true;
        Config.syncConfig();
    }
}